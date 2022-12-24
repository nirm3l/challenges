package is.symphony.challenge;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Challenge12 {

    public static void main(final String[] args) throws IOException {
        System.out.println(new Challenge12().execute("src/main/resources/input12.txt", 'S'));
        System.out.println(new Challenge12().execute("src/main/resources/input12.txt", 'a'));
    }

    private Integer execute(final String filePath, final Character character) throws IOException {
        try (final Stream<String> lines = (Files.lines(Paths.get(filePath)))) {
            final Tuple2<List<List<Character>>, Tuple2<Integer, Integer>> matrixInfo = readMatrix(lines);

            // Go from end to start
            return findShortestPathSize(matrixInfo.getT1(), matrixInfo.getT2(), character);
        }
    }

    private Integer findShortestPathSize(
            final List<List<Character>> matrix, final Tuple2<Integer, Integer> start, final Character endCharacter) {
        final Set<Tuple2<Integer, Integer>> visited = new HashSet<>();
        visited.add(start);

        final AtomicReference<Set<Tuple2<Integer, Integer>>> next = new AtomicReference<>(Set.of(start));

        return Stream.iterate(1, counter -> counter + 1)
                .filter(counter -> {
                    final AtomicBoolean stop = new AtomicBoolean();

                    final Set<Tuple2<Integer, Integer>> nextSet = next.get().stream()
                            .takeWhile(position -> !stop.get())
                            .flatMap(position -> {
                                final Character currentChar = Stream.of(matrix.get(position.getT1()).get(position.getT2()))
                                        .map(c -> c.equals('E') ? 'z' : c).findFirst().get();

                                return findNeighbours(matrix, position)
                                        .takeWhile(neighbour -> !stop.get())
                                        .filter(neighbour -> {
                                            final Character neighbourChar = matrix.get(neighbour.getT1()).get(neighbour.getT2());

                                            if (List.of('b', 'a').contains(currentChar) && neighbourChar.equals(endCharacter)) {
                                                stop.set(true);
                                            }

                                            return neighbourChar + 1 >= currentChar && visited.add(neighbour);
                                        });
                            }).collect(Collectors.toSet());

                    next.set(nextSet);

                    return stop.get();
                }).findFirst().orElseThrow();
    }

    private Stream<Tuple2<Integer, Integer>> findNeighbours(final List<List<Character>> matrix, final Tuple2<Integer, Integer> point) {
        return Stream.of(Tuples.of(point.getT1() + 1, point.getT2()), Tuples.of(point.getT1() - 1, point.getT2()),
                Tuples.of(point.getT1(), point.getT2() + 1), Tuples.of(point.getT1(), point.getT2() - 1))
                .filter(tuple -> tuple.getT1() >= 0 &&
                        tuple.getT1() < matrix.size() &&
                        tuple.getT2() >= 0 &&
                        tuple.getT2() < matrix.get(tuple.getT1()).size());
    }

    private Tuple2<List<List<Character>>, Tuple2<Integer, Integer>> readMatrix(final Stream<String> lines) {
        final AtomicReference<Tuple2<Integer, Integer>> endPosition = new AtomicReference<>();

        final List<List<Character>> matrix = indexStream(lines)
                .map(indexedLine -> indexStream(indexedLine.getT2().chars().mapToObj(c -> (char) c))
                        .peek(indexedCharacter -> {
                            if (indexedCharacter.getT2().equals('E')) {
                                endPosition.set(Tuples.of(indexedLine.getT1(), indexedCharacter.getT1()));
                            }
                        }).map(Tuple2::getT2).collect(Collectors.toList())).collect(Collectors.toList());

        return Tuples.of(matrix, endPosition.get());
    }

    private <T> Stream<Tuple2<Integer, T>> indexStream(final Stream<T> stream) {
        final AtomicInteger atomicInteger = new AtomicInteger(0);

        return stream.map(value -> Tuples.of(atomicInteger.getAndIncrement(), value));
    }
}
