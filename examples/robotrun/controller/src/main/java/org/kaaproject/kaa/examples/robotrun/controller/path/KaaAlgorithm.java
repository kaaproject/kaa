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
package org.kaaproject.kaa.examples.robotrun.controller.path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.kaaproject.kaa.examples.robotrun.labyrinth.BorderType;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Cell;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Direction;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Labyrinth;
import org.kaaproject.kaa.examples.robotrun.labyrinth.RobotPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KaaAlgorithm implements ExitSearchAlgorithm {
    private static final Logger LOG = LoggerFactory.getLogger(KaaAlgorithm.class);

    private final LinkedList<Cell> steps;
    private final Map<Cell, CellMetaData> cellData;

    public KaaAlgorithm() {
        super();
        this.steps = new LinkedList<>();
        this.cellData = Collections.synchronizedMap(new HashMap<Cell, CellMetaData>());
    }

    @Override
    public Cell getNextCell(Labyrinth labyrinth, Cell current, Map<String, RobotPosition> robotPositions) {
        LOG.trace("Calculating next cell for current cell {}", current);
        if (steps.size() == 0 || !steps.getLast().equals(current)) {
            CellMetaData md = cellData.get(current);
            if (md == null) {
                md = new CellMetaData();
                cellData.put(current, md);
            }
            md.visitCount++;
            LOG.trace("Adding current cell to steps queue");
            steps.addLast(current);
        }
        Cell result = null;
        List<Cell> neighbors = getNeighbors(labyrinth, current);
        LOG.info("Current cell has {} neighbors", neighbors.size());
        List<Cell> options = filterDeadEnds(getAllButPrevious(current, neighbors));
        LOG.info("Neighbors has {} options", options.size());
        boolean deadend = isDeadEnd(labyrinth, current, options);
        if (deadend) {
            LOG.info("Deadend found!");
            current.markDeadEnd();
            steps.removeLast();
            if (steps.size() > 0 && isValid(steps.getLast(), robotPositions)) {
                result = steps.getLast();
            } else if (filterDeadEnds(neighbors).size() == 1) {
                result = filterDeadEnds(neighbors).get(0);
            } else {
                LOG.info("Surrounded by deadends. Going to start wave algorithm!");
                result = getNextCellByWaveAlgorithm(labyrinth, current, robotPositions);
            }
        } else {
            for (Cell option : options) {
                LOG.info("Analyzing option {} ", option);
                boolean isVisited = isVisited(option);
                boolean isConflict = isConflict(option, robotPositions);
                LOG.info("Option is visited by us or other robots = {} is in conflict with other robots = {}",
                        isVisited, isConflict);
                if (!isVisited && !isConflict) {
                    result = option;
                    break;
                }
            }
            if (result == null) {
                LOG.info("Can't find suitable option. Going to start wave algorithm!");
                result = getNextCellByWaveAlgorithm(labyrinth, current, robotPositions);
            }
        }
        if (filterDeadEnds(neighbors).size() == 1) {
            LOG.info("Neighbors has only one not dead end. This is a dead end!");
            current.markDeadEnd();
        }
        if (result == null) {
            LOG.trace("Can't find any option!");
            result = current;
        }
        LOG.info("Next cell will be {}", result);
        return result;
    }

    private boolean isValid(Cell step, Map<String, RobotPosition> robotPositions) {
        if(step.isDeadEnd()){
            return false;
        }else{
            for(RobotPosition position : robotPositions.values()){
                if(step.equals(position)){
                    return false;
                }
            }
            return true;
        }
    }

    private Cell getNextCellByWaveAlgorithm(Labyrinth labyrinth, Cell current, Map<String, RobotPosition> robotPositions) {
        Cell result = null;
        List<Cell> path = getAlternatePath(labyrinth, current, robotPositions);
        if (path != null && path.size() > 0) {
            LOG.trace("Wave algorithm returned {}", path.get(0));
            result = path.get(0);
        }
        return result;
    }

    private boolean isConflict(Cell option, Map<String, RobotPosition> robotPositions) {
        for (RobotPosition robot : robotPositions.values()) {
            if (option.equals(robot.getCell())) {
                return true;
            }
        }
        return false;
    }

    private boolean isVisited(Cell option) {
        CellMetaData md = cellData.get(option);
        return md != null && (md.visitCount > 0 || md.visitedByOtherCount > 0);
    }

    @Override
    public void onRobotLocationChanged(String key, Cell cell) {
        CellMetaData md = cellData.get(cell);
        if (md == null) {
            md = new CellMetaData();
            cellData.put(cell, md);
        }
        md.visitedByOtherCount++;
    }

    private boolean isDeadEnd(Labyrinth labyrinth, Cell current, List<Cell> options) {
        if (options.size() == 0) {
            return true;
        }
        if (options.size() == 1) {
            Cell next = options.get(0);
            CellMetaData md = cellData.get(next);
            if (md != null && md.visitCount > 0 && isJunction(labyrinth, next)) {
                return true;
            }
        }
        return false;
    }

    private List<Cell> filterDeadEnds(List<Cell> options) {
        List<Cell> result = new ArrayList<>();
        for (Cell cell : options) {
            if (!cell.isDeadEnd()) {
                result.add(cell);
            }
        }
        return result;
    }

    private List<Cell> getAllButPrevious(Cell current, List<Cell> neighbors) {
        List<Cell> result = new ArrayList<>(neighbors);
        Cell previous = getPrevious(current);
        if (previous != null) {
            result.remove(previous);
        }
        return result;
    }

    private Cell getPrevious(Cell current) {
        Cell result = null;
        if (steps.size() > 1) {
            result = steps.get(steps.size() - 2);
        }
        return result;
    }

    private boolean isJunction(Labyrinth labyrinth, Cell cell) {
        return getNeighbors(labyrinth, cell).size() > 2;
    }

    private List<Cell> getNeighbors(Labyrinth labyrinth, Cell current) {
        List<Cell> result = new LinkedList<>();
        for (Direction direction : Direction.values()) {
            BorderType border = current.getBorder(direction);
            if (border == BorderType.FREE) {
                Cell neighbor = getNeightbor(labyrinth, current, direction);
                if (neighbor != null) {
                    result.add(neighbor);
                }
            }
        }
        return result;
    }

    private Cell getNeightbor(Labyrinth labyrinth, Cell current, Direction direction) {
        Cell result = null;
        switch (direction) {
        case WEST:
            if (current.getX() >= 1) {
                result = labyrinth.getCell(current.getX() - 1, current.getY());
            }
            break;
        case EAST:
            if (current.getX() < labyrinth.getWidth() - 1) {
                result = labyrinth.getCell(current.getX() + 1, current.getY());
            }
            break;
        case NORTH:
            if (current.getY() >= 1) {
                result = labyrinth.getCell(current.getX(), current.getY() - 1);
            }
            break;
        case SOUTH:
            if (current.getY() < labyrinth.getHeight() - 1) {
                result = labyrinth.getCell(current.getX(), current.getY() + 1);
            }
            break;
        default:
            throw new IllegalArgumentException("Unknown direction: " + direction);
        }
        return result;
    }

    private static class CellMetaData {
        private int visitCount;
        private int visitedByOtherCount;
    }

    private List<Cell> getAlternatePath(Labyrinth labyrinth, Cell source, Map<String, RobotPosition> others) {
        Cell target = null;
        List<Cell> path = null;

        /* Breadth first search stuff */
        Queue<Cell> queue = new LinkedList<>();
        Map<Cell/* CHILD */, Cell/* PARENT */> visitGraph = new HashMap<>();

        queue.add(source);
        visitGraph.put(source, null);
        LOG.trace("Constructing graph");
        /* Use BFS to find a target cell */
        while (queue.size() > 0) {
            Cell node = queue.poll();
            LOG.trace("Processing node {}", node);
            if (isTargetFound(node)) {
                LOG.trace("Target candidate found {}", node);
                target = node;

                if (!isOtherNearer(visitGraph, others, target)) {
                    LOG.trace("Target found {}", node);
                    break;
                }
            } else {
                List<Cell> neightbors = getNeighbors(labyrinth, node);
                LOG.trace("Processing {} neightbors", neightbors.size());
                if (neightbors != null && neightbors.size() > 0) {
                    for (Cell neighbor : neightbors) {
                        if (!visitGraph.containsKey(neighbor)) {
                            LOG.trace("Adding to queue {}", neighbor);
                            queue.add(neighbor);
                            visitGraph.put(neighbor, node);
                        }
                    }
                }
            }
        }

        if (target != null) {
            LOG.trace("Compiling path");
            path = compilePath(visitGraph, target);
        }

        return path;
    }

    private boolean isOtherNearer(Map<Cell, Cell> graph, Map<String, RobotPosition> other, Cell target) {
        boolean isOtherNearer = false;
        for (Map.Entry<String, RobotPosition> cursorEntry : other.entrySet()) {
            Cell robotPosition = cursorEntry.getValue().getCell();
            if (robotPosition.equals(target) ||
            /* Robot position and target should be on a way of wave expansion */
            (isNeightbors(robotPosition, target) && graph.get(robotPosition) != null)) {
                isOtherNearer = true;
                break;
            }
        }

        return isOtherNearer;
    }

    private boolean isNeightbors(Cell one, Cell two) {
        if ((one.getY() == two.getY() && Math.abs(one.getX() - two.getX()) == 1)
                || (one.getX() == two.getX() && Math.abs(one.getY() - two.getY()) == 1)) {
            return true;
        }
        return false;
    }

    private boolean isTargetFound(Cell cell) {
        boolean result = false;
        if(cell.isDiscovered() && cell.isExit()) {
            return true;
        }
        for (Direction direction : Direction.values()) {
            if (cell.getBorder(direction) == BorderType.UNKNOWN) {
                result = true;
                break;
            }
        }

        return result;
    }

    private List<Cell> compilePath(Map<Cell, Cell> graph, Cell target) {
        LinkedList<Cell> path = new LinkedList<>();
        path.push(target);

        Cell parent = graph.get(target);
        while (parent != null) {
            path.push(parent);
            parent = graph.get(parent);
        }

        /* Remove source position from path */
        if (path.size() > 0) {
            path.pop();
        }

        return path;
    }
}
