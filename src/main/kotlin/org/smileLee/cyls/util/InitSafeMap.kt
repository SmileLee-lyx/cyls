package org.smileLee.cyls.util

class InitSafeMap<K, V>(val m: HashMap<K, V>, val init: (K) -> V) : SafeMap<K, V> {
    override fun put(key: K, value: V) = m.put(key, value)
    override fun putAll(from: Map<out K, V>) = m.putAll(from)
    override fun iterator() = m.iterator()
    override fun get(key: K): V = m[key] ?: let {
        val value = init(key)
        m.put(key, value)
        value
    }

    override val size: Int
        get() = m.size

    override fun containsKey(key: K) = m.containsKey(key)
    override fun containsValue(value: V) = m.containsValue(value)
    override fun isEmpty() = m.isEmpty()
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = m.entries
    override val keys: MutableSet<K>
        get() = m.keys
    override val values: MutableCollection<V>
        get() = m.values

    override fun clear() {
        m.clear()
    }

    override fun remove(key: K) = m.remove(key)
}