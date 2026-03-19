package com.assgui.gourmandine.data.cache

import java.util.concurrent.ConcurrentHashMap

/**
 * Thread-safe, TTL-based in-memory cache.
 * [K] is the key type, [V] is the cached value type.
 */
class TypedCache<K, V>(private val ttlMs: Long) {

    private data class Entry<V>(
        val data: V,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        fun isExpired(ttl: Long): Boolean =
            System.currentTimeMillis() - timestamp > ttl
    }

    private val store = ConcurrentHashMap<K, Entry<V>>()

    fun put(key: K, value: V) {
        store[key] = Entry(value)
    }

    fun get(key: K): V? {
        val entry = store[key] ?: return null
        if (entry.isExpired(ttlMs)) {
            store.remove(key)
            return null
        }
        return entry.data
    }

    fun getAll(): List<V> =
        store.values.filter { !it.isExpired(ttlMs) }.map { it.data }

    fun getAllEntries(): Map<K, V> =
        store.entries
            .filter { !it.value.isExpired(ttlMs) }
            .associate { it.key to it.value.data }

    fun remove(key: K) {
        store.remove(key)
    }

    fun update(key: K, transform: (V) -> V) {
        store[key]?.let { store[key] = Entry(transform(it.data)) }
    }

    fun clear() {
        store.clear()
    }
}

/**
 * Thread-safe, TTL-based single-value cache.
 * Use when only one value needs to be cached (e.g., current user, favorites list).
 */
class SingleCache<V>(private val ttlMs: Long) {

    private data class Entry<V>(
        val data: V,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        fun isExpired(ttl: Long): Boolean =
            System.currentTimeMillis() - timestamp > ttl
    }

    @Volatile
    private var entry: Entry<V>? = null

    fun put(value: V) {
        entry = Entry(value)
    }

    fun get(): V? {
        val e = entry ?: return null
        if (e.isExpired(ttlMs)) {
            entry = null
            return null
        }
        return e.data
    }

    fun clear() {
        entry = null
    }
}
