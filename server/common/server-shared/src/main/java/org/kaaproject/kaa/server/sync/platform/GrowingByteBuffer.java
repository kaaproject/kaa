/*
 * Copyright 2014-2016 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.sync.platform;

import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.Arrays;

/**
 * 
 * This class wraps {@link ByteBuffer} and provides ability to automatically resize the buffer when needed.
 * 
 * @author Andrew Shvayka
 *
 */
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
