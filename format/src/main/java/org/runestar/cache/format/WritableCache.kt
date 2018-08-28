package org.runestar.cache.format

interface WritableCache : ReadableCache {

    fun setReference(value: StoreReference)

    fun setIndexReference(index: Int, value: IndexReference)

    fun setVolume(index: Int, volume: Int, value: Volume)
}