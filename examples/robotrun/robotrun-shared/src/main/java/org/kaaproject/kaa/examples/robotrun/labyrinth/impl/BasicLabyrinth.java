/*
 * Copyright 2014 CyberVision, Inc.
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

/**
 * Copyright 2014 CyberVision, Inc.
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
package org.kaaproject.kaa.examples.robotrun.labyrinth.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.kaaproject.kaa.examples.robotrun.labyrinth.BorderType;
import org.kaaproject.kaa.examples.robotrun.labyrinth.BorderUpdate;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Cell;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Direction;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Labyrinth;

public class BasicLabyrinth implements Labyrinth, Serializable {

    private static final long serialVersionUID = 1L;

    private static final String ARRAYS_SHOULD_HAVE_SAME_DIMENTIONS = "Arrays should have same dimentions";
    private static final String CELL_IS_NOT_YET_DISCOVERED = "Cell is not yet discovered!";
    private static final String CELL_IS_NOT_LABYRINTH_EXIT = "Cell is not a labyrinth exit!";
    private final int width;
    private final int height;

    protected final BorderType[][] hBorder;
    protected final BorderType[][] vBorder;
    protected final boolean[][] deadends;

    public BasicLabyrinth(int width, int height) {
        super();
        this.width = width;
        this.height = height;
        this.deadends = new boolean[width][height];
        this.hBorder = fill(new BorderType[width][height + 1], BorderType.UNKNOWN);
        this.vBorder = fill(new BorderType[width + 1][height], BorderType.UNKNOWN);
    }

    public void fill(boolean[][] hBorderSrc, boolean[][] vBorderSrc) {
        fill(this.hBorder, hBorderSrc);
        fill(this.vBorder, vBorderSrc);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public Cell getCell(int x, int y) {
        return new BasicCell(x, y);
    }

    @Override
    public boolean isEmpty() {
        return width == 0 && height == 0;
    }

    private BorderType[][] fill(BorderType[][] dest, boolean[][] source) {
        if (dest.length != source.length) {
            throw new IllegalArgumentException(ARRAYS_SHOULD_HAVE_SAME_DIMENTIONS + " Dest: " + dest.length
                    + " Source: " + source.length);
        }
        for (int i = 0; i < dest.length; i++) {
            BorderType[] row = dest[i];
            if (row.length != source[i].length) {
                throw new IllegalArgumentException(ARRAYS_SHOULD_HAVE_SAME_DIMENTIONS + " Dest: " + "[" + i + "]"
                        + dest.length + " Source: " + "[" + i + "]" + source[i].length);
            }
            for (int j = 0; j < row.length; j++) {
                row[j] = source[i][j] ? BorderType.SOLID : BorderType.FREE;
            }
        }
        return dest;
    }

    private BorderType[][] fill(BorderType[][] dest, BorderType source) {
        for (int i = 0; i < dest.length; i++) {
            BorderType[] row = dest[i];
            for (int j = 0; j < row.length; j++) {
                row[j] = source;
            }
        }
        return dest;
    }

    @Override
    public String toString() {
        return "BasicLabyrinth [width=" + width + ", height=" + height + "]";
    }

    @Override
    public void update(BorderType[][] hBorders, BorderType[][] vBorders) {
        updateBorders(this.hBorder, hBorders);
        updateBorders(this.vBorder, vBorders);
    }

    private void updateBorders(BorderType[][] dest, BorderType[][] source) {
        boolean isCleanup = isClean(source);
        for (int i = 0; i < dest.length; i++) {
            for (int j = 0; j < dest[i].length; j++) {
                if (isCleanup || source[i][j] != BorderType.UNKNOWN) {
                    dest[i][j] = source[i][j];
                }
            }
        }
    }

    private boolean isClean(BorderType[][] source) {
        for (int i = 0; i < source.length; i++) {
            for (int j = 0; j < source[i].length; j++) {
                if (source[i][j] != BorderType.UNKNOWN) {
                    return false;
                }
            }
        }
        return true;
    }

    private class BasicCell implements Cell {
        private final int x;
        private final int y;

        public BasicCell(int x, int y) {
            super();
            this.x = x;
            this.y = y;
        }

        @Override
        public BorderType getBorder(Direction side) {
            switch (side) {
            case WEST:
                return vBorder[x][y];
            case EAST:
                return vBorder[x + 1][y];
            case NORTH:
                return hBorder[x][y];
            case SOUTH:
                return hBorder[x][y + 1];
            default:
                throw new IllegalArgumentException("Side " + side.name() + " is not supported");
            }
        }

        @Override
        public BorderUpdate setBorder(Direction side, BorderType border) {
            switch (side) {
            case WEST:
                if (vBorder[x][y] != border) {
                    vBorder[x][y] = border;
                    return BasicBorderUpdate.vBorder(x, y, border);
                } else {
                    break;
                }
            case EAST:
                if (vBorder[x + 1][y] != border) {
                    vBorder[x + 1][y] = border;
                    return BasicBorderUpdate.vBorder(x + 1, y, border);
                } else {
                    break;
                }
            case NORTH:
                if (hBorder[x][y] != border) {
                    hBorder[x][y] = border;
                    return BasicBorderUpdate.hBorder(x, y, border);
                } else {
                    break;
                }
            case SOUTH:
                if (hBorder[x][y + 1] != border) {
                    hBorder[x][y + 1] = border;
                    return BasicBorderUpdate.hBorder(x, y + 1, border);
                } else {
                    break;
                }
            default:
                throw new IllegalArgumentException("Side " + side.name() + " is not supported");
            }
            return null;
        }

        @Override
        public boolean isDiscovered() {
            for (Direction s : Direction.values()) {
                if (getBorder(s) == BorderType.UNKNOWN) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean isExit() {
            if (!isDiscovered()) {
                throw new IllegalStateException(CELL_IS_NOT_YET_DISCOVERED);
            } else {
                return (x == 0 && getBorder(Direction.WEST) == BorderType.FREE)
                        || (x == (BasicLabyrinth.this.getWidth() - 1) && getBorder(Direction.EAST) == BorderType.FREE)
                        || (y == 0 && getBorder(Direction.NORTH) == BorderType.FREE)
                        || (y == (BasicLabyrinth.this.getHeight() - 1) && getBorder(Direction.SOUTH) == BorderType.FREE);
            }
        }

        @Override
        public Direction getExitDirection() {
            if (!isExit()) {
                throw new IllegalStateException(CELL_IS_NOT_LABYRINTH_EXIT);
            } else {
                if (x == 0 && getBorder(Direction.WEST) == BorderType.FREE) {
                    return Direction.WEST;
                } else if (x == (BasicLabyrinth.this.getWidth() - 1) && getBorder(Direction.EAST) == BorderType.FREE) {
                    return Direction.EAST;
                } else if (y == 0 && getBorder(Direction.NORTH) == BorderType.FREE) {
                    return Direction.NORTH;
                } else {
                    return Direction.SOUTH;
                }
            }
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + x;
            result = prime * result + y;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            BasicCell other = (BasicCell) obj;
            if (x != other.x)
                return false;
            if (y != other.y)
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "BasicCell [x=" + x + ", y=" + y + "]";
        }

        @Override
        public boolean isDeadEnd() {
            return deadends != null && deadends[x][y];
        }

        @Override
        public void markDeadEnd() {
            deadends[x][y] = true;
        }
    }

    @Override
    public Labyrinth copy() {
        Labyrinth copy = new BasicLabyrinth(width, height);
        copy.update(hBorder, vBorder);
        return copy;
    }

    @Override
    public void store(OutputStream os) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(os);
        out.writeObject(this);
    }

    public static Labyrinth load(InputStream is) throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = new ObjectInputStream(is);
        Labyrinth labyrinth = (Labyrinth) objectInputStream.readObject();
        return labyrinth;
    }

    @Override
    public void clearDeadends() {
        for (int i = 0; i < deadends.length; i++) {
            for (int j = 0; j < deadends[i].length; j++) {
                deadends[i][j] = false;
            }
        }
    }
}
