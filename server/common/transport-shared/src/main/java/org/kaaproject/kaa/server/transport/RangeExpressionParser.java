package org.kaaproject.kaa.server.transport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Converts a range expression ("1-3, 2-7, 15") into a sequence of numbers from that range
 *
 * @author Oleg Klishch
 * 
 */
public final class RangeExpressionParser {

    private static final String RANGE_DELIMITER = ",";
    private static final String CORNERS_DELIMITER = "-";
    private static final int RANGE_START = 0;
    private static final int RANGE_END = 1;

    public List<Integer> getNumbersFromRanges(String expression) {
        if (expression == null) {
            throw new IllegalArgumentException("Expression can not be null");
        }

        List<Integer> numbersFromRanges = new ArrayList<>();
        List<Range> ranges = parseRanges(expression);
        for (Range range : mergeRanges(ranges)) {
            for (int i = range.getFrom(); i <= range.getTo(); i++) {
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
        Collections.sort(ranges, new RangeComparator());
        Range currentRange = ranges.get(0);
        for (int i = 1; i < ranges.size(); i++) {
            if (currentRange.getTo() >= ranges.get(i).getFrom()) {
                currentRange.setTo(Math.max(currentRange.getTo(), ranges.get(i).getTo()));
            } else {
                mergedRanges.add(currentRange);
                currentRange = ranges.get(i);
            }
        }
        mergedRanges.add(currentRange);
        return mergedRanges;
    }

    private static class Range {

        private int from;
        private int to;

        private Range(int from, int to) {
            this.from = from;
            this.to = to;
        }
        
        public int getFrom() {
            return from;
        }

        public void setFrom(int from) {
            this.from = from;
        }

        public int getTo() {
            return to;
        }

        public void setTo(int to) {
            this.to = to;
        }
    }

    private static class RangeComparator implements Comparator<Range> {

        @Override
        public int compare(Range first, Range second) {
            int cmp = Integer.compare(first.getFrom(), second.getFrom());
            if (cmp != 0) {
                return cmp;
            }
            return Integer.compare(second.getTo(), second.getTo());
        }
    }
}
