package org.smileLee.cyls.util

import kotlin.collections.MutableMap.*

interface SafeMap<K, V> : MutableMap<K, V>, Iterable<Map.Entry<K, V>> {
    override fun put(key: K, value: V): V?
    override fun putAll(from: Map<out K, V>)
    override operator fun get(key: K): V
    override operator fun iterator(): MutableIterator<MutableEntry<K, V>>
}