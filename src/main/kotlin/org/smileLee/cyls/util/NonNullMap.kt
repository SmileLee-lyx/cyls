package org.smileLee.cyls.util

import kotlin.collections.MutableMap.MutableEntry

interface NonNullMap<K, V> : MutableMap<K, V>, Iterable<Map.Entry<K, V>> {
    override operator fun get(key: K): V
    override operator fun iterator(): MutableIterator<MutableEntry<K, V>>
    override fun put(key: K, value: V): V?
    override fun putAll(from: Map<out K, V>)
    override fun containsKey(key: K): Boolean
    override fun containsValue(value: V): Boolean
    override fun isEmpty(): Boolean
    override fun remove(key: K): V?
    override fun clear()
    override val size: Int
    override val entries: MutableSet<MutableEntry<K, V>>
    override val keys: MutableSet<K>
    override val values: MutableCollection<V>
}