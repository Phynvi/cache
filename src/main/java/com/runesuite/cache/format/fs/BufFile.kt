package com.runesuite.cache.format.fs

import com.runesuite.cache.extensions.closeQuietly
import com.runesuite.cache.extensions.freeDirect
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import java.io.Closeable
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption

internal class BufFile
@Throws(IOException::class)
constructor(val file: Path, val maxSize: Int) : Closeable {

    private val fileChannel: FileChannel = FileChannel.open(file, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE)

    private val mappedByteBuffer: MappedByteBuffer

    val buffer: ByteBuf

    init {
        val originalSize: Long
        try {
            originalSize = fileChannel.size()
        } catch (sizeException: IOException) {
            fileChannel.closeQuietly()
            throw sizeException
        }
        try {
            mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, maxSize.toLong())
        } catch (mapException: IOException) {
            try {
                fileChannel.truncate(originalSize)
            } catch (truncateException: IOException) {
                //
            } finally {
                fileChannel.closeQuietly()
            }
            throw mapException
        }
        buffer = Unpooled.wrappedBuffer(mappedByteBuffer)
        buffer.writerIndex(originalSize.toInt())
        check(buffer.capacity() == maxSize)
        check(buffer.readerIndex() == 0)
        check(buffer.writerIndex() == originalSize.toInt())
    }

    override fun close() {
        if (fileChannel.isOpen) {
            val writtenSize = buffer.writerIndex()
            buffer.release()
            mappedByteBuffer.force().freeDirect()
            fileChannel.use {
                it.truncate(writtenSize.toLong())
            }
        }
    }
}