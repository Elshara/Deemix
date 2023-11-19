package com.nick80835.add

class ByteArrayChunkIterator(private val initialArray: ByteArray, private val chunkSize: Int) : Iterator<ByteArray> {
    private var place = 0

    override fun hasNext(): Boolean {
        if (place+chunkSize < initialArray.size) {
            return true
        }

        return false
    }

    override fun next(): ByteArray {
        val currentChunk = if (place+chunkSize < initialArray.size) {
            initialArray.copyOfRange(place, place+chunkSize)
        } else {
            initialArray.copyOfRange(place, initialArray.size)
        }

        place += chunkSize
        return currentChunk
    }
}
