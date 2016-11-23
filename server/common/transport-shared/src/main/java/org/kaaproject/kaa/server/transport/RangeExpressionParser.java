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

package org.kaaproject.kaa.server.transport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Converts a range expression ("1-3") into a sequence of numbers from that range ([1, 2, 3]).
 * If ranges overlap it returns only unique numbers. E.g: "1-3, 2-5" => (1, 2, 3, 4, 5).
 *
 * @author Oleg Klishch
 */
public final class RangeExpressionParser {

  private static final String RANGE_DELIMITER = ",";
  private static final String CORNERS_DELIMITER = "-";
  private static final int RANGE_START = 0;
  private static final int RANGE_END = 1;

  /**
   * Parse string, that represents range of values (like "1-3") and get all integers value
   * corresponding this range.
   *
   * @param expression is expression for parsing
   * @return list of integers values in range
   */
  public List<Integer> getNumbersFromRanges(String expression) {
    if (expression == null) {
      throw new IllegalArgumentException("Expression can not be null");
    }

    List<Integer> numbersFromRanges = new ArrayList<>();
    List<Range> ranges = parseRanges(expression);
    for (Range range : mergeRanges(ranges)) {
      for (int i = range.from; i <= range.to; i++) {
        numbersFromRanges.add(i);
      }
    }
    return numbersFromRanges;
  }

  private List<Range> parseRanges(String expression) {
    String[] rangeStrings = expression.split(RANGE_DELIMITER);
    List<Range> ranges = new ArrayList<>();
    for (String rangeString : rangeStrings) {
      ranges.add(parseRange(rangeString.trim()));
    }
    return ranges;
  }

  private Range parseRange(String rangeString) {
    if (!rangeString.contains(CORNERS_DELIMITER)) {
      int from = Integer.parseInt(rangeString);
      return new Range(from, from);
    }

    String[] cornerStrings = rangeString.split(CORNERS_DELIMITER);
    int from = Integer.parseInt(cornerStrings[RANGE_START]);
    int to = Integer.parseInt(cornerStrings[RANGE_END]);
    return new Range(from, to);
  }

  private List<Range> mergeRanges(List<Range> ranges) {
    if (ranges.isEmpty()) {
      return ranges;
    }

    List<Range> mergedRanges = new ArrayList<>();
    Collections.sort(ranges);
    Range currentRange = ranges.get(0);
    for (int i = 1; i < ranges.size(); i++) {
      if (currentRange.to >= ranges.get(i).from) {
        currentRange.to = Math.max(currentRange.to, ranges.get(i).to);
      } else {
        mergedRanges.add(currentRange);
        currentRange = ranges.get(i);
      }
    }
    mergedRanges.add(currentRange);
    return mergedRanges;
  }

  private static class Range implements Comparable<Range> {

    public int from;
    public int to;

    private Range(int from, int to) {
      this.from = from;
      this.to = to;
    }

    @Override
    public int compareTo(Range other) {
      int cmp = Integer.compare(from, other.from);
      if (cmp != 0) {
        return cmp;
      }
      return Integer.compare(to, other.to);
    }
  }
}