package bitcask;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

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
        //TODO: Implement this.
    }

    /*
        Open a new or existing Bitcask datastore for read-only access.
        The directory and all files in it must be readable by this process.
     */
    public BitcaskHandle<K, V> open(String directoryName) throws IOException {
        //TODO: Implement this.
    }

    /*
        Retrieve a value by key from a Bitcask datastore.
     */
    public V get(BitcaskHandle<K, V> handle, K key) {
        //TODO: Implement this.
    }

    /*
        Store a key and value in a Bitcask datastore.
     */
    public boolean put(BitcaskHandle<K, V> handle, K key, V value) {
        //TODO: Implement this.
    }

    /*
        List all keys in a Bitcask datastore.
     */
    public List<K> listKeys(BitcaskHandle<K, V> handle) {
        //TODO: Implement this.
    }

    /*
        Merge several data files within a Bitcask datastore into a more
        compact form. Also, produce hintfiles for faster startup.
     */
    public boolean merge(BitcaskHandle<K, V> handle) {
        //TODO: Implement this.
    }

    /*
        Force any writes to sync to disk.
     */
    public boolean sync(BitcaskHandle<K, V> handle) {
        //TODO: Implement this.
    }

    /*
        Close a Bitcask data store and flush all pending writes
        (if any) to disk.
     */
    public boolean close(BitcaskHandle<K, V> handle) {
        //TODO: Implement this.
    }


}
