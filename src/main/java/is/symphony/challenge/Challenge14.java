package is.symphony.challenge;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Challenge14 {

    final static Character SAND_CHARACTER = 'o';
    final static Character ROCK_CHARACTER = '#';
    final static Character EMPTY_CHARACTER = '.';
    final static Character SOURCE_CHARACTER = '+';
    final static List<Character> END_CHARACTERS = List.of(ROCK_CHARACTER, SAND_CHARACTER);

    public static void main(final String[] args) throws IOException {
        System.out.println(new Challenge14().execute("src/main/resources/input14.txt", 2));
    }

    private Integer execute(final String filePath, final Integer floor) throws IOException {
        try (final Stream<String> lines = (Files.lines(Paths.get(filePath)))) {
            final List<Tuple2<Integer, Integer>> rocks = lines.flatMap(this::getRockPositions).toList();

            final int maxX = rocks.stream().mapToInt(Tuple2::getT2).max().orElse(0);
            int minY = rocks.stream().mapToInt(Tuple2::getT1).min().orElse(0);
            int maxY = rocks.stream().mapToInt(Tuple2::getT1).max().orElse(0);

            if (floor != null && floor > 0) {
                minY -= maxX + floor;
                maxY += maxX + floor;
            }

            final int[] sourcePosition = new int[] {0, 500 - minY};

            final List<List<Character>> matrix = initializeMatrix(maxX, maxY - minY);

            if (floor != null && floor > 0) {
                addFloors(matrix, floor);
            }

            matrix.get(sourcePosition[0]).set(sourcePosition[1], SOURCE_CHARACTER);

            for (final Tuple2<Integer, Integer> rock : rocks) {
                matrix.get(rock.getT2()).set(rock.getT1() - minY, ROCK_CHARACTER);
            }

            printMatrix(matrix);

            return IntStream.iterate(0, counter -> counter + 1)
                    .filter(i -> !addSand(matrix, sourcePosition))
                    .peek(counter -> printMatrix(matrix))
                    .findFirst().orElseThrow(() -> new IllegalStateException("Invalid input file!"));
        }
    }

    private void addFloors(final List<List<Character>> matrix, final int floor) {
        final int floorWidth = matrix.get(0).size();

        matrix.addAll(IntStream.range(0, floor).boxed()
                .map(i -> IntStream.range(0, floorWidth + 1).boxed()
                        .map(j -> i == floor - 1 ? ROCK_CHARACTER : EMPTY_CHARACTER).collect(Collectors.toList()))
                .toList());
    }

    private boolean addSand(final List<List<Character>> matrix, final int[] sourcePosition) {
        final int[] sandPosition = sourcePosition.clone();

        if (matrix.get(sandPosition[0]).get(sandPosition[1]).equals(SAND_CHARACTER)) {
            return false;
        }

        return IntStream.iterate(0, i -> i + 1).boxed()
                .flatMap(i -> {
                    try {
                        if (END_CHARACTERS.contains(matrix.get(sandPosition[0] + 1).get(sandPosition[1]))) {
                            if (!END_CHARACTERS.contains(matrix.get(sandPosition[0] + 1).get(sandPosition[1] - 1))) {
                                sandPosition[1] = sandPosition[1] - 1;
                            }
                            else if (!END_CHARACTERS.contains(matrix.get(sandPosition[0] + 1).get(sandPosition[1] + 1))) {
                                sandPosition[1] = sandPosition[1] + 1;
                            }
                            else {
                                matrix.get(sandPosition[0]).set(sandPosition[1], SAND_CHARACTER);

                                return Stream.of(true);
                            }
                        }

                        sandPosition[0] = sandPosition[0] + 1;
                    }
                    catch (IndexOutOfBoundsException e) {
                        return Stream.of(false);
                    }

                    return Stream.empty();
                }).findAny().orElseThrow(() -> new IllegalStateException("Invalid input file!"));
    }

    private void printMatrix(final List<List<Character>> matrix) {
        matrix.forEach(row -> {
            row.forEach(System.out::print);
            System.out.println();
        });

        System.out.println();
    }

    private List<List<Character>> initializeMatrix(final int sizeX, final int sizeY) {
        return IntStream.range(0, sizeX + 1).boxed()
                .map(i -> IntStream.range(0, sizeY + 1)
                        .mapToObj(j -> EMPTY_CHARACTER).collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    private Stream<Tuple2<Integer, Integer>> getRockPositions(final String line) {
        final AtomicReference<Tuple2<Integer, Integer>> currentPosition = new AtomicReference<>();

        return Arrays.stream(line.split(" -> "))
                .map(this::parsePosition)
                .flatMap(position -> {
                    final Tuple2<Integer, Integer> previousPosition = currentPosition.get();

                    currentPosition.set(position);

                    if (previousPosition != null) {
                        if (position.getT1().equals(previousPosition.getT1())) {
                            return getStream(previousPosition.getT2(), position.getT2())
                                    .map(pos -> Tuples.of(position.getT1(), pos));
                        }
                        else {
                            return getStream(previousPosition.getT1(), position.getT1())
                                    .map(pos -> Tuples.of(pos, position.getT2()));
                        }
                    }

                    return Stream.empty();
                });
    }

    private Stream<Integer> getStream(final int from, final int to) {
        if (from < to) {
            return IntStream.range(from, to + 1).boxed();
        }

        return IntStream.range(to, from + 1).boxed().sorted(Collections.reverseOrder());
    }

    private Tuple2<Integer, Integer> parsePosition(final String value) {
        final String[] parts = value.split(",");

        return Tuples.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }
}
