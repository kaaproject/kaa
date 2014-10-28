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

import java.util.Random;

import org.kaaproject.kaa.examples.robotrun.labyrinth.Labyrinth;
import org.kaaproject.kaa.examples.robotrun.labyrinth.LabyrinthGenerator;

/**
 * Create labyrinth generator based on Union-Find algorithm
 */
public class BasicLabyrinthGenerator implements LabyrinthGenerator {
    private final int[] id;
    private final int[] sz;

    private final int rowNum;
    private final int columnNum;
    private final int count;

    boolean [][] hBdrs;
    boolean [][] vBdrs;

    /**
     * Create labyrinth generator based on Union-Find algorithm
     *
     * @param width Number of columns
     * @param height Number of rows
     */
    public BasicLabyrinthGenerator(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException();
        }

        rowNum = height;
        columnNum = width;
        count = rowNum * columnNum;

        id = new int[count];
        sz = new int[count];

        hBdrs = new boolean[columnNum][rowNum + 1];
        vBdrs = new boolean[columnNum + 1][rowNum];
    }

    private void init() {
        initBorders(hBdrs, true);
        initBorders(vBdrs, true);

        for (int i = 0; i < count; ++i) {
            id[i] = i;
            sz[i] = i;
        }
    }

    private void initBorders(boolean borders[][], boolean value) {
        for (int i = 0; i < borders.length; i++) {
            boolean[] row = borders[i];
            for (int j = 0; j < row.length; j++) {
                row[j] = value;
            }
        }
    }

    private int find(int p) {
        while (p != id[p]) {
            p = id[p];
        }
        return p;
    }

    private boolean connected(int p, int q) {
        return find(p) == find(q);
    }

    private void union(int p, int q) {
        int rootP = find(p);
        int rootQ = find(q);

        if (rootP == rootQ) {
            return;
        }

        if (sz[rootP] < sz[rootQ]) {
            id[rootP] = rootQ;
            sz[rootQ] += sz[rootP];
        } else {
            id[rootQ] = rootP;
            sz[rootP] += sz[rootQ];
        }
    }

    private void checkBounds(int position, int leftBound, int rightBound) throws IndexOutOfBoundsException {
        if (leftBound > position || position > rightBound) {
            throw new IndexOutOfBoundsException();
        }
    }

    private int getAdjacentCellIndex(int cellIndex) {
        final int MAX_ATTEMPTS = 4;
        int adjacentCellIndex = -1;
        Random directionRandomizer = new Random();
        int direction = directionRandomizer.nextInt(MAX_ATTEMPTS);
        int attempt = MAX_ATTEMPTS;

        while ((attempt > 0) && (0 > adjacentCellIndex || adjacentCellIndex >= count)) {
            switch (direction) {
            case 0: /* LEFT */
                adjacentCellIndex = cellIndex - 1;
                if ((cellIndex % columnNum) == 0) {
                    adjacentCellIndex = cellIndex - columnNum;
                }
                break;
            case 1: /* TOP */
                adjacentCellIndex = cellIndex - columnNum;
                break;
            case 2: /* RIGHT */
                adjacentCellIndex = cellIndex + 1;
                if ((adjacentCellIndex % columnNum) == 0) {
                    adjacentCellIndex = cellIndex + columnNum;
                }
                break;
            case 3: /* DOWN */
                adjacentCellIndex = cellIndex + columnNum;
                break;
            default:
                break;
            }

            if (++direction >= MAX_ATTEMPTS) {
                direction = 0;
            }
            --attempt;
        }

        return adjacentCellIndex;
    }

    @Override
    public Labyrinth generate(int startX, int startY, int finishX, int finishY) throws IndexOutOfBoundsException {
        checkBounds(startY, -1, rowNum); checkBounds(startX, -1, columnNum);
        checkBounds(finishY, -1, rowNum); checkBounds(finishX, -1, columnNum);

        init();

        int startCellIndex = startX + startY * columnNum;
        int finishCellIndex = finishX + finishY * columnNum;

        Random cellRandomizer = new Random(System.currentTimeMillis());

        while (!connected(startCellIndex, finishCellIndex)) {
            int randomCellIndex = cellRandomizer.nextInt(count);
            int adjacentCellIndex = getAdjacentCellIndex(randomCellIndex);

            if (!connected(randomCellIndex, adjacentCellIndex)) {
                union(randomCellIndex, adjacentCellIndex);
                updateBorders(randomCellIndex, adjacentCellIndex);
            }
        }

        tryOpenFinishBorder(finishX, finishY);

        BasicLabyrinth lab = new BasicLabyrinth(columnNum, rowNum);
        lab.fill(hBdrs, vBdrs);
        return lab;
    }

    private void updateBorders(int i, int j) {
        int iX = i / columnNum;
        int iY = i % columnNum;
        int jX = j / columnNum;
        int jY = j % columnNum;

        if (Math.abs(i - j) == 1) {
            vBdrs[Math.max(iY, jY)][iX] = false;
        } else {
            hBdrs[iY][Math.max(iX, jX)] = false;
        }
    }

    private void tryOpenFinishBorder(int finishX, int finishY) {
        /**
         * Start from LEFT and move clockwise
         */
        if (finishX == 0) {
            vBdrs[0][finishY] = false;
        } else if (finishY == 0) {
            hBdrs[finishX][0] = false;
        } else if ((finishX + 1) == columnNum) {
            vBdrs[columnNum][finishY] = false;
        } else if ((finishY + 1) == rowNum) {
            hBdrs[finishX][rowNum] = false;
        }
    }
}
