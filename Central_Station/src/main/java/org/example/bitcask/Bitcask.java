package org.example.bitcask;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Bitcask<K extends Serializable, V extends Serializable> {

    public Bitcask() {}

    /*
        Open a new or existing Bitcask datastore with additional options.
        Valid options include read write (if this process is going to be a
        writer and not just a reader) and sync on put (if this writer would
        prefer to sync the write file after every write operation).
        The directory must be readable and writable by this process, and
        only one process may open a Bitcask with read write at a time.
     */
    public BitcaskHandle<K, V> open(String directoryName, Map<String, Integer> opts) throws IOException {
        return new BitcaskHandleImpl<K, V>(directoryName, opts);
    }

    /*
        Open a new or existing Bitcask datastore for read-only access.
        The directory and all files in it must be readable by this process.
     */
    public BitcaskHandle<K, V> open(String directoryName) throws IOException {
        return new BitcaskHandleImpl<K, V>(directoryName);
    }

    /*
        Retrieve a value by key from a Bitcask datastore.
     */
    public V get(BitcaskHandle<K, V> handle, K key) {
        return handle.get(key);
    }

    /*
        Store a key and value in a Bitcask datastore.
     */
    public boolean put(BitcaskHandle<K, V> handle, K key, V value) throws IOException {
        System.out.println("Hello");
        return handle.put(key, value);
    }

    /*
        List all keys in a Bitcask datastore.
     */
    public List<K> listKeys(BitcaskHandle<K, V> handle) {
        return handle.listKeys();
    }

    /*
        Merge several data files within a Bitcask datastore into a more
        compact form. Also, produce hintfiles for faster startup.
     */
    public boolean merge(BitcaskHandle<K, V> handle) throws IOException {
        return handle.merge();
    }

    /*
        Force any writes to sync to disk.
     */
    public boolean sync(BitcaskHandle<K, V> handle) throws IOException {
        return handle.sync();
    }

    /*
        Close a Bitcask data store and flush all pending writes
        (if any) to disk.
     */
    public boolean close(BitcaskHandle<K, V> handle) throws IOException {
        return handle.close();
    }

    /*
        Schedule a Bitcask data store for procedual merging.
     */
    public void scheduleMerge(BitcaskHandle<K, V> handle, long delay) throws IOException {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(delay);
                merge(handle);
                scheduleMerge(handle, delay);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


}
