package com.modulytic.dalia.include;

import java.util.Hashtable;
import java.util.Map;

// https://stackoverflow.com/a/3430209/1420247
public class BiHashMap<K, V> {

    private final Map<K,V> forward = new Hashtable<>();
    private final Map<V,K> backward = new Hashtable<>();

    public synchronized boolean isEmpty() {
        return forward.isEmpty() || backward.isEmpty();
    }

    public synchronized void put(K key, V value) {
        forward.put(key, value);
        backward.put(value, key);
    }

    public synchronized V getForward(K key) {
        return forward.get(key);
    }

    public synchronized K getBackward(V key) {
        return backward.get(key);
    }
}