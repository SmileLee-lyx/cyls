package org.smileLee.cyls

class InitSafeMap<K, V>(val m: HashMap<K, V>, val init: (K) -> V) : SafeMap<K, V> {
    override fun put(key: K, value: V) = m.put(key, value)
    override fun putAll(from: Map<K, V>) = m.putAll(from)
    override fun iterator() = m.iterator()
    override fun get(key: K): V {
        val nullableValue = m[key]
        if (nullableValue != null) return nullableValue else {
            val value = init(key)
            m.put(key, value)
            return value
        }
    }
}