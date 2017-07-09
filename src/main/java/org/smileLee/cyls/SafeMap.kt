package org.smileLee.cyls

import kotlin.collections.MutableMap.*

interface SafeMap<K, V> {
    fun put(key: K, value: V): V?
    fun putAll(from: Map<K, V>)
    operator fun get(key: K): V
    operator fun iterator(): MutableIterator<MutableEntry<K, V>>
}