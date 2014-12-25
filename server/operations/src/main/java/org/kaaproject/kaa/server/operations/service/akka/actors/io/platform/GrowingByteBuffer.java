package org.kaaproject.kaa.server.operations.service.akka.actors.io.platform;

import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.Arrays;

public class GrowingByteBuffer {

    private static final int SIZE_OF_BYTE = 1;
    private static final int SIZE_OF_SHORT = 2;
    private static final int SIZE_OF_INT = 4;
    private static final int SIZE_OF_LONG = 8;
    private ByteBuffer data;

    public GrowingByteBuffer(int size) {
        super();
        data = ByteBuffer.wrap(new byte[size]);
    }

    public GrowingByteBuffer put(byte b) {
        resizeIfNeeded(SIZE_OF_BYTE);
        data.put(b);
        return this;
    }

    public GrowingByteBuffer put(byte[] bytes) {
        resizeIfNeeded(bytes.length);
        data.put(bytes);
        return this;
    }

    public GrowingByteBuffer putShort(short s) {
        resizeIfNeeded(SIZE_OF_SHORT);
        data.putShort(s);
        return this;
    }

    public GrowingByteBuffer putShort(int position, short s) {
        checkPosition(position + SIZE_OF_SHORT);
        int tmp = data.position();
        data.position(position);
        data.putShort(s);
        data.position(tmp);
        return this;
    }

    public GrowingByteBuffer putInt(int i) {
        resizeIfNeeded(SIZE_OF_INT);
        data.putInt(i);
        return this;
    }

    public GrowingByteBuffer putInt(int position, int i) {
        checkPosition(position + SIZE_OF_INT);
        int tmp = data.position();
        data.position(position);
        data.putInt(i);
        data.position(tmp);
        return this;
    }

    public GrowingByteBuffer putLong(long value) {
        resizeIfNeeded(SIZE_OF_LONG);
        data.putLong(value);
        return this;
    }

    public int position() {
        return data.position();
    }

    private void checkPosition(int position) {
        if (data.capacity() < position) {
            throw new IllegalArgumentException(MessageFormat.format("Position {0} is greater then capacity {1}", position, data.capacity()));
        }
    }

    private void resizeIfNeeded(int size) {
        if (size > data.remaining()) {
            int position = data.position();
            data = ByteBuffer.wrap(Arrays.copyOf(data.array(), Math.max(data.position() + size, data.array().length * 2)));
            data.position(position);
        }
    }

    public byte[] toByteArray() {
        return Arrays.copyOf(data.array(), data.position());
    }

}
