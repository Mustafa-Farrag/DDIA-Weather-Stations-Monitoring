package bitcask;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.function.BiFunction;

public interface BitcaskHandle<K extends Serializable, V extends Serializable> {
    public V get(K key);
    public boolean put(K key, V value) throws IOException;
    public List<K> listKeys();
    public boolean merge();
    public boolean sync();
    public boolean close();
}
