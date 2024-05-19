package org.example.bitcask;

import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

public class BitcaskHandleImpl<K extends Serializable, V extends Serializable> implements BitcaskHandle<K, V> {

    private static class BitcaskKeyDirRecord {
        int fileId;
        int valueSz;
        long valuePos;
        int timestamp;

        public BitcaskKeyDirRecord(int fileId, int valueSz, long valuePos, int timestamp) {
            this.fileId = fileId;
            this.valueSz = valueSz;
            this.valuePos = valuePos;
            this.timestamp = timestamp;
        }
    }

    private final File activeDirectory;
    private boolean write = true;
    private boolean syncOnPut = true;
    private int maxFileSize = 1; // 100 MiB
    private ConcurrentMap<K, BitcaskKeyDirRecord> keyDir;
    private File activeCask;
    private RandomAccessFile activeCaskPointer;
    private int activeCaskId = -1;
    private final Object pointerLock = new Object();
    private final ReadWriteLock keyDirLock = new ReentrantReadWriteLock();

    public BitcaskHandleImpl(String directoryName) throws IOException {
        System.out.println("hello from constructor");
        Path baseDir = Paths.get(directoryName).resolve("bitcask");
        this.activeDirectory = baseDir.toFile();
        if (!activeDirectory.isDirectory()) {
            activeDirectory.mkdirs();
            baseDir.resolve("mergeCask").toFile().mkdir();
            baseDir.resolve("mergeHint").toFile().mkdir();
            baseDir.resolve("hint").toFile().mkdir();
        }
        boolean hasFiles;
        try (Stream<Path> entries = Files.list(activeDirectory.toPath()).filter(o1 -> o1.toFile().isFile())) {
             hasFiles = entries.findFirst().isPresent();
        }
        if (hasFiles) {
            this.regenKeyDir();
        } else {
            System.out.println("hello, from else");
            this.keyDir = new ConcurrentHashMap<>();
        }
    }

    public BitcaskHandleImpl(String directoryName, Map<String, Integer> opts) throws IOException {
        Path baseDir = Paths.get(directoryName).resolve("bitcask");
        this.activeDirectory = baseDir.toFile();
        if (!activeDirectory.isDirectory()) {
            activeDirectory.mkdirs();
            baseDir.resolve("mergeCask").toFile().mkdir();
            baseDir.resolve("mergeHint").toFile().mkdir();
            baseDir.resolve("hint").toFile().mkdir();
        }
        this.write = opts.getOrDefault("readWrite", this.write ? 1 : 0) == 1;
        this.syncOnPut = opts.getOrDefault("syncOnPut", this.syncOnPut ? 1 : 0) == 1;
        this.maxFileSize = opts.getOrDefault("maxFileSize", this.maxFileSize);
        boolean hasFiles;
        try (Stream<Path> entries = Files.list(activeDirectory.toPath()).filter(o1 -> o1.toFile().isFile())) {
            hasFiles = entries.findFirst().isPresent();
        }
        if (hasFiles) {
            this.regenKeyDir();
        } else {
            this.keyDir = new ConcurrentHashMap<>();
        }
    }

    @Override
    public V get(K key) {
        keyDirLock.readLock().lock();
        try {
            BitcaskKeyDirRecord record = keyDir.get(key);
            if (record == null) return null;
            Path path = activeDirectory.toPath().resolve(record.fileId + ".cask");
            try (RandomAccessFile file = new RandomAccessFile(path.toFile(), "r")) {
                file.seek(record.valuePos + 4);
                short keySize = file.readShort();
                int valueSize = file.readInt();
                file.seek(file.getFilePointer() + keySize);
                byte[] valueBytes = new byte[valueSize];
                file.read(valueBytes);
                ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(valueBytes));
                return (V) objectInputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } finally {
            keyDirLock.readLock().unlock();
        }
    }

    @Override
    public boolean put(K key, V value) throws IOException {
        System.out.println("hello2");
        if (!canWrite()) return false;
        if (shouldGenerateNewCask()) generateActiveCask();
        int timestamp = Long.valueOf(Instant.now().getEpochSecond()).intValue();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(0);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(key);
        objectOutputStream.flush();
        objectOutputStream.close();
        byte[] keyBytes = byteArrayOutputStream.toByteArray();

        byteArrayOutputStream = new ByteArrayOutputStream();
        objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(value);
        objectOutputStream.flush();
        objectOutputStream.close();
        byte[] valueBytes = byteArrayOutputStream.toByteArray();

        long offset;
        synchronized (this.pointerLock) {
            offset = this.activeCaskPointer.getFilePointer();
            this.activeCaskPointer.writeInt(timestamp);
            this.activeCaskPointer.writeShort(keyBytes.length);
            this.activeCaskPointer.writeInt(valueBytes.length);
            this.activeCaskPointer.write(keyBytes);
            this.activeCaskPointer.write(valueBytes);
        }

        this.keyDir.put(key, new BitcaskKeyDirRecord(this.activeCaskId, valueBytes.length, offset, timestamp));
        return true;
    }

    @Override
    public List<K> listKeys() {
        return this.keyDir.keySet().stream().toList();
    }

    @Override
    public boolean merge() throws IOException {
        long dirSize;
        try (Stream<Path> files = Files.list(this.activeDirectory.toPath()).filter(o1 -> o1.toFile().isFile())) {
            dirSize = files.count();
        }
        if (dirSize < 3) return true;
        Map<K, BitcaskKeyDirRecord> keyDirSnapshot = new HashMap<>(this.keyDir);
        int thresholdCaskId = this.activeCaskId == -1 ? this.getMaxId()+1 : this.activeCaskId;
        int mergedId = 0;
        Path mergeCaskDir = this.activeDirectory.toPath().resolve("mergeCask");
        Path mergeHintDir = this.activeDirectory.toPath().resolve("mergeHint");
        RandomAccessFile mergedFile = new RandomAccessFile(mergeCaskDir.resolve(mergedId + ".cask").toFile(), "rws");
        RandomAccessFile hintFile = new RandomAccessFile(mergeHintDir.resolve(mergedId + ".hint").toFile(), "rws");
        keyDirSnapshot.entrySet().removeIf(kBitcaskKeyDirRecordEntry -> kBitcaskKeyDirRecordEntry.getValue().fileId >= thresholdCaskId);
        for (var entry : keyDirSnapshot.entrySet()) {
            if(mergedFile.length() > (long) this.maxFileSize * 1024 * 1024) {
                mergedFile.close();
                hintFile.close();
                ++mergedId;
                mergedFile = new RandomAccessFile(mergeCaskDir.resolve(mergedId + ".cask").toFile(), "rws");
                hintFile = new RandomAccessFile(mergeHintDir.resolve(mergedId + ".hint").toFile(), "rws");
            }
            BitcaskKeyDirRecord record = entry.getValue();
            long newOffset = mergedFile.getFilePointer();
            RandomAccessFile recordFile = new RandomAccessFile(this.activeDirectory.toPath().resolve(record.fileId + ".cask").toFile(), "r");
            recordFile.seek(record.valuePos);
            int timestamp = recordFile.readInt();
            short keySize = recordFile.readShort();
            int valueSize = recordFile.readInt();
            byte[] keyBytes = new byte[keySize];
            recordFile.read(keyBytes);
            byte[] valueBytes = new byte[valueSize];
            recordFile.read(valueBytes);
            recordFile.close();
            mergedFile.writeInt(timestamp);
            mergedFile.writeShort(keySize);
            mergedFile.writeInt(valueSize);
            mergedFile.write(keyBytes);
            mergedFile.write(valueBytes);
            hintFile.writeInt(timestamp);
            hintFile.writeShort(keySize);
            hintFile.writeInt(valueSize);
            hintFile.writeLong(newOffset);
            hintFile.write(keyBytes);
            record.fileId = mergedId;
            record.valuePos = newOffset;
        }
        mergedFile.close();
        hintFile.close();
        keyDirLock.writeLock().lock();
        try {
            for (var entry : keyDirSnapshot.entrySet()) {
                if (this.keyDir.get(entry.getKey()).timestamp != entry.getValue().timestamp) continue;
                this.keyDir.put(entry.getKey(), entry.getValue());
            }
        } finally {
            keyDirLock.writeLock().unlock();
        }
        for (int i = 0; i < thresholdCaskId; ++i) {
            Files.deleteIfExists(this.activeDirectory.toPath().resolve(i + ".cask"));
            Files.deleteIfExists(this.activeDirectory.toPath().resolve("hint").resolve(i + ".hint"));
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(mergeCaskDir)) {
            for (Path path : stream) {
                Files.move(path, this.activeDirectory.toPath().resolve(path.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(mergeHintDir)) {
            for (Path path : stream) {
                Files.move(path, this.activeDirectory.toPath().resolve("hint").resolve(path.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            }
        }

        return false;
    }

    @Override
    public boolean sync() throws IOException {
        if (this.syncOnPut) return true;
        this.syncOnPut = true;
        if (this.activeCaskPointer != null) {
            synchronized (this.pointerLock) {
                this.activeCaskPointer.close();
                this.activeCaskPointer = new RandomAccessFile(this.activeCask, "rws");
            }
        }
        return true;
    }

    @Override
    public boolean close() throws IOException {
        if (this.activeCaskPointer != null) {
            synchronized (this.pointerLock) {
                this.activeCaskPointer.close();
            }
        }
        return true;
    }

    private void regenKeyDir() throws IOException {
        int maxTimestamp = 0;
        Set<Integer> readFiles = new HashSet<>();
        Map<K, BitcaskKeyDirRecord> newKeyDir = new HashMap<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(this.activeDirectory.toPath().resolve("hint"))) {
            for (Path path : stream) {
                int fileId = Integer.parseInt(path.getFileName().toString().substring(0, path.getFileName().toString().indexOf('.')));
                readFiles.add(fileId);
                RandomAccessFile hintFile = new RandomAccessFile(path.toFile(), "r");
                while (true) {
                    try {
                        int timestamp = hintFile.readInt();
                        short keySize = hintFile.readShort();
                        int valueSize = hintFile.readInt();
                        long valuePos = hintFile.readLong();
                        byte[] keyBytes = new byte[keySize];
                        hintFile.read(keyBytes);
                        ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(keyBytes));
                        maxTimestamp = Math.max(maxTimestamp, timestamp);
                        K key = (K) objectInputStream.readObject();
                        BitcaskKeyDirRecord entry = new BitcaskKeyDirRecord(fileId, valueSize, valuePos, timestamp);
                        newKeyDir.put(key, entry);
                    } catch (EOFException ex) {
                        break;
                    }
                }
                hintFile.close();
                break;
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(this.activeDirectory.toPath())) {
            for (Path path : stream) {
                if (path.toFile().isDirectory()) continue;
                int fileId = Integer.parseInt(path.getFileName().toString().substring(0, path.getFileName().toString().indexOf('.')));
                if (readFiles.contains(fileId)) continue;
                readFiles.add(fileId);
                RandomAccessFile file = new RandomAccessFile(path.toFile(), "r");
                long offset = file.getFilePointer();
                int timestamp = file.readInt();
                if (timestamp < maxTimestamp) {
                    continue;
                }
                short keySize = file.readShort();
                int valueSize = file.readInt();
                byte[] keyBytes = new byte[keySize];
                file.read(keyBytes);
                file.seek(file.getFilePointer() + valueSize);
                ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(keyBytes));
                K key = (K) objectInputStream.readObject();
                var entry = newKeyDir.getOrDefault(key, null);
                if (entry != null) {
                    if (entry.timestamp < timestamp) {
                        entry = new BitcaskKeyDirRecord(fileId, valueSize, offset, timestamp);
                        newKeyDir.put(key, entry);
                    }
                } else {
                    entry = new BitcaskKeyDirRecord(fileId, valueSize, offset, timestamp);
                    newKeyDir.put(key, entry);
                }
                while (true) {
                    try {
                        offset = file.getFilePointer();
                        timestamp = file.readInt();
                        keySize = file.readShort();
                        valueSize = file.readInt();
                        keyBytes = new byte[keySize];
                        file.read(keyBytes);
                        file.seek(file.getFilePointer() + valueSize);
                        objectInputStream = new ObjectInputStream(new ByteArrayInputStream(keyBytes));
                        key = (K) objectInputStream.readObject();
                        entry = newKeyDir.getOrDefault(key, null);
                        if (entry != null) {
                            if (entry.timestamp < timestamp) {
                                entry = new BitcaskKeyDirRecord(fileId, valueSize, offset, timestamp);
                                newKeyDir.put(key, entry);
                            }
                        } else {
                            entry = new BitcaskKeyDirRecord(fileId, valueSize, offset, timestamp);
                            newKeyDir.put(key, entry);
                        }
                    } catch (EOFException ex) {
                        break;
                    }
                }
                file.close();
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        keyDirLock.writeLock().lock();
        try {
            this.keyDir = new ConcurrentHashMap<>(newKeyDir);
        } finally {
            keyDirLock.writeLock().unlock();
        }
    }

    private void generateActiveCask() throws IOException {
        if (this.activeCaskId == -1) {
            this.activeCaskId = this.getMaxId();
        }
        ++this.activeCaskId;
        File newCask = this.activeDirectory.toPath().resolve(this.activeCaskId + ".cask").toFile();
        while (newCask.exists()) {
            ++this.activeCaskId;
            newCask = this.activeDirectory.toPath().resolve(Integer.toUnsignedLong(this.activeCaskId) + ".cask").toFile();
        }
        newCask.createNewFile();
        RandomAccessFile newCaskPointer;
        if (this.syncOnPut) {
            newCaskPointer = new RandomAccessFile(newCask, "rws");
        } else {
            newCaskPointer = new RandomAccessFile(newCask, "rw");
        }
        this.activeCask = newCask;
        synchronized (this.pointerLock) {
            if (this.activeCaskPointer != null) this.activeCaskPointer.close();
            this.activeCaskPointer = newCaskPointer;
        }
    }

    private int getMaxId() throws IOException {
        int maxId = -1;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(activeDirectory.toPath())) {
            for (Path path : stream) {
                File file = path.toFile();
                if (!file.isFile()) continue;
                int id = Integer.parseInt(file.getName().substring(0, file.getName().indexOf('.')));
                maxId = Math.max(maxId, id);
            }
        }
        return maxId;
    }

    private boolean shouldGenerateNewCask() {
        return this.activeCask == null || (this.activeCask.length() > (long) this.maxFileSize * 1024 * 1024);
    }

    private boolean canWrite() {
        return this.write;
    }
}
