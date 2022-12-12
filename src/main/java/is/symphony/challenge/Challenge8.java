package is.symphony.challenge;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Challenge8 {

    public static void main(final String[] args) throws IOException {
        System.out.println(
                new Challenge8().execute("src/main/resources/input8.txt"));
    }

    public Tuple2<Integer, Integer> execute(final String filePath) throws IOException {
        try (final Stream<String> lines = (Files.lines(Paths.get(filePath)))) {
            final List<List<Integer>> matrix = parseMatrix(lines);

            return Tuples.of(findVisibleTrees(matrix).size(), findHighestScenicScore(matrix));
        }
    }

    private List<List<Integer>> parseMatrix(final Stream<String> linesStream) {
        return linesStream.reduce(new ArrayList<>(), (currentMatrix, rowString) -> {
            final List<Integer> row = new ArrayList<>();

            for (int i = 0; i < rowString.length(); i++) {
                row.add(Integer.parseInt(rowString.substring(i, i + 1)));
            }

            currentMatrix.add(row);

            return currentMatrix;
        }, (a, b) -> a);
    }

    private Set<Tuple2<Integer, Integer>> findVisibleTrees(final List<List<Integer>> matrix) {
        final Set<Tuple2<Integer, Integer>> result = new HashSet<>();

        for (int i = 0; i < matrix.size(); i++) {
            int rowLength = matrix.get(i).size();

            int highestHorizontal = -1, highestVertical = -1;

            for (int j = 0; j < rowLength; j++) {
                highestHorizontal = checkIfTreeIsVisible(matrix, i, j, highestHorizontal, result);
                highestVertical = checkIfTreeIsVisible(matrix, j, i, highestVertical, result);
            }

            highestHorizontal = -1;
            highestVertical = -1;

            for (int j = rowLength - 1; j >=0; j--) {
                highestHorizontal = checkIfTreeIsVisible(matrix, i, j, highestHorizontal, result);
                highestVertical = checkIfTreeIsVisible(matrix, j, i, highestVertical, result);
            }
        }

        return result;
    }

    private int checkIfTreeIsVisible(
            final List<List<Integer>> matrix, int i, int j, int highest, final Set<Tuple2<Integer, Integer>> visibleTrees) {
        int current = matrix.get(i).get(j);

        if (current > highest) {
            visibleTrees.add(Tuples.of(i, j));
            highest = current;
        }

        return highest;
    }

    private Integer findHighestScenicScore(final List<List<Integer>> matrix) {
        return IntStream.range(0, matrix.size())
                .flatMap(i -> IntStream.range(0, matrix.get(i).size())
                        .flatMap(j -> IntStream.of(getScenicScore(matrix, i, j)))).max().orElse(-1);
    }

    private Integer getScenicScore(final List<List<Integer>> matrix, int x, int y) {
        final List<Tuple2<AtomicInteger, AtomicBoolean>> counters = IntStream.range(0, 4)
                .mapToObj(i -> Tuples.of(new AtomicInteger(0), new AtomicBoolean(true))).toList();

        int rowLength = matrix.get(x).size();

        final AtomicInteger counter = new AtomicInteger(0);

        return Stream.generate(counter::incrementAndGet)
                .takeWhile(i -> counters.stream().anyMatch(tuple -> tuple.getT2().get()))
                .reduce(counters, (currentCounters, i) -> {
                    handleDirection(currentCounters.get(0),
                            () -> x + i < matrix.size(), () -> matrix.get(x).get(y) <= matrix.get(x + i).get(y));

                    handleDirection(currentCounters.get(1),
                            () -> x - i >= 0, () -> matrix.get(x).get(y) <= matrix.get(x - i).get(y));

                    handleDirection(currentCounters.get(2),
                            () -> y + i < rowLength, () -> matrix.get(x).get(y) <= matrix.get(x).get(y + i));

                    handleDirection(currentCounters.get(3),
                            () -> y - i >= 0, () -> matrix.get(x).get(y) <= matrix.get(x).get(y - i));

                    return currentCounters;
                }, (i, j) -> i)
                .stream().mapToInt(tuple -> tuple.getT1().get()).reduce(1, (a, b) -> a * b);
    }

    private void handleDirection(
            final Tuple2<AtomicInteger, AtomicBoolean> counter,
            final Supplier<Boolean> boundCondition, final Supplier<Boolean> treeHeightCondition) {
        if (counter.getT2().get() && boundCondition.get()) {
            counter.getT1().incrementAndGet();

            if (treeHeightCondition.get()) {
                counter.getT2().set(false);
            }
        }
        else {
            counter.getT2().set(false);
        }
    }
}
