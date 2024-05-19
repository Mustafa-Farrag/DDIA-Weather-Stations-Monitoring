package org.example.bitcask;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public interface BitcaskHandle<K extends Serializable, V extends Serializable> {
    public V get(K key);
    public boolean put(K key, V value) throws IOException;
    public List<K> listKeys();
    public boolean merge() throws IOException;
    public boolean sync() throws IOException;
    public boolean close() throws IOException;
}
