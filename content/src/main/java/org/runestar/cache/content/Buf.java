package org.runestar.cache.content;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Buf {

    private Buf() {}

    public static final Charset CHARSET = Charset.forName("windows-1252");

    public static String getString(ByteBuffer buffer) {
        int origPos = buffer.position();
        int length = 0;
        while (buffer.get() != 0) length++;
        if (length == 0) return "";
        var byteArray = new byte[length];
        buffer.position(origPos);
        buffer.get(byteArray);
        buffer.position(buffer.position() + 1);
        return new String(byteArray, CHARSET);
    }

    public static int getUnsignedByte(ByteBuffer buffer) {
        return Byte.toUnsignedInt(buffer.get());
    }

    public static int getUnsignedShort(ByteBuffer buffer) {
        return Short.toUnsignedInt(buffer.getShort());
    }

    public static int getUnsignedShortM1(ByteBuffer buffer) {
        int n = getUnsignedShort(buffer);
        if (n == 0xFFFF) n = -1;
        return n;
    }

    public static int getMedium(ByteBuffer buffer) {
        return (buffer.getShort() << 8) | (buffer.get() & 0xFF);
    }

    public static Map<Integer, Object> decodeParams(ByteBuffer buffer) {
        int length = getUnsignedByte(buffer);
        var params = new LinkedHashMap<Integer, Object>(length);
        for (int i = 0; i < length; i++) {
            boolean isString = buffer.get() != 0;
            params.put(getMedium(buffer), isString ? getString(buffer) : buffer.getInt());
        }
        return params;
    }
}