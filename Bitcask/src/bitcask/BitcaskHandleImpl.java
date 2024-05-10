package bitcask;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

public class BitcaskHandleImpl<K extends Serializable, V extends Serializable> implements BitcaskHandle<K, V>{

    private static class BitcaskKeyDirRecord {
        int fileId;
        int valueSz;
        long value_pos;
        int timestamp;

        public BitcaskKeyDirRecord(int fileId, int valueSz, long value_pos) {
            this.fileId = fileId;
            this.valueSz = valueSz;
            this.value_pos = value_pos;
            this.timestamp = Long.valueOf(Instant.now().getEpochSecond()).intValue();
        }
    }

    /*
    private class BitcaskFileRecord<K extends Serializable, V extends Serializable> {
        int timestamp;
        short kSz;
        int valueSz;
        K key;
        V value;

        public BitcaskFileRecord(int timestamp, short kSz, int valueSz, K key, V value) {
            this.timestamp = timestamp;
            this.kSz = kSz;
            this.valueSz = valueSz;
            this.key = key;
            this.value = value;
        }
    }
    */


    private final File activeDirectory;
    private boolean write = false;
    private boolean syncOnPut = false;
    private int maxFileSize = 100; // 100 MiB
    private int batchSize = 1;
    private Queue<byte[]> batch;
    private Map<K, BitcaskKeyDirRecord> keyDir;
    private File activeCask;
    private RandomAccessFile activeCaskPointer;
    private long activeCaskId = -1;

    public BitcaskHandleImpl(String directoryName) throws IOException {
        this.activeDirectory = Paths.get(directoryName).toFile();
        if (!activeDirectory.isDirectory()) {
            if (!activeDirectory.mkdirs()) throw new IOException("Failed to create directory: " + activeDirectory);
        }
        boolean hasFiles = false;
        try (Stream<Path> entries = Files.list(activeDirectory.toPath())) {
             hasFiles = entries.findFirst().isPresent();
             entries.close();
        }
        if (hasFiles) {
            this.regenKeyDir();
        } else {
            this.keyDir = new HashMap<>();
        }
    }

    public BitcaskHandleImpl(String directoryName, Map<String, Integer> opts) throws IOException {
        this.activeDirectory = Paths.get(directoryName).toFile();
        if (!activeDirectory.isDirectory()) {
            if (!activeDirectory.mkdirs()) throw new IOException("Failed to create directory: " + activeDirectory);
        }
        this.write = opts.getOrDefault("readWrite", 0) == 1;
        this.syncOnPut = opts.getOrDefault("syncOnPut", 0) == 1;
        this.maxFileSize = opts.getOrDefault("maxFileSize", this.maxFileSize);
        this.batchSize = opts.getOrDefault("batchSize", this.batchSize);
        if (!this.syncOnPut) this.batch = new ArrayDeque<>(this.batchSize);
        boolean hasFiles = false;
        try (Stream<Path> entries = Files.list(activeDirectory.toPath())) {
            hasFiles = entries.findFirst().isPresent();
            entries.close();
        }
        if (hasFiles) {
            this.regenKeyDir();
        } else {
            this.keyDir = new HashMap<>();
        }
    }

    @Override
    public V get(K key) {
        BitcaskKeyDirRecord record = keyDir.get(key);
        if (record == null) return null;
        Path path = activeDirectory.toPath().resolve(record.fileId + ".cask");
        try (RandomAccessFile file = new RandomAccessFile(path.toFile(), "r")) {
            file.seek(record.value_pos + 4);
            short keySize = file.readShort();
            int valueSize = file.readInt();
            file.seek(keySize);
            byte[] valueBytes = new byte[valueSize];
            file.read(valueBytes);
            ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(valueBytes));
            return (V) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean put(K key, V value) throws IOException {
        if (!canWrite()) return false;
        if (shouldGenerateNewCask()) generateActiveCask();
        this.activeCaskPointer.writeInt(Long.valueOf(Instant.now().getEpochSecond()).intValue());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
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

        this.activeCaskPointer.writeShort(keyBytes.length);
        this.activeCaskPointer.writeInt(valueBytes.length);
        this.activeCaskPointer.write(keyBytes);
        this.activeCaskPointer.write(valueBytes);
        return true;
    }

    @Override
    public List<K> listKeys() {
        return this.keyDir.keySet().stream().toList();
    }

    @Override
    public boolean merge() {
        //TODO: Implement this.
        return false;
    }

    @Override
    public boolean sync() {
        //TODO: Implement this.
        return false;
    }

    @Override
    public boolean close() {
        //TODO: Implement this.
        return false;
    }

    private void regenKeyDir() throws IOException {
        //TODO: Implement this.
    }

    private void generateActiveCask() throws IOException {
        if (this.activeCaskId == -1) {
            long maxId = 0;
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(activeDirectory.toPath())) {
                for (Path path : stream) {
                    File file = path.toFile();
                    if (file.isFile()) {
                        long id = Long.parseLong(file.getName().substring(0, file.getName().indexOf('.')));
                        maxId = Math.max(maxId, id);
                    }
                }
            }
            this.activeCaskId = maxId;
        }
        ++this.activeCaskId;
        File newCask = this.activeDirectory.toPath().resolve(this.activeCaskId + 1 + ".cask").toFile();
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
        if (this.activeCaskPointer != null) this.activeCaskPointer.close();
        this.activeCaskPointer = newCaskPointer;
    }

    private boolean shouldGenerateNewCask() {
        return this.activeCask == null || (this.activeCask.length() > (long) this.maxFileSize * 1024 * 1024);
    }

    private boolean canWrite() {
        return this.write;
    }
}
