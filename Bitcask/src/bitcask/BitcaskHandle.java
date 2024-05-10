package bitcask;

import java.util.List;
import java.util.function.BiFunction;

public interface BitcaskHandle<K, V> {
    public V get(K key);
    public boolean put(K key, V value);
    public List<K> listKeys();
    public <L> L fold(BiFunction<K, V, L> function);
    public boolean merge();
    public boolean sync();
    public boolean close();
}
