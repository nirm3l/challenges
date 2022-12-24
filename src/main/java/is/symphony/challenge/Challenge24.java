package is.symphony.challenge;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Challenge24 {
    private List<List<Character>> map;

    private List<Tuple3<Integer, Integer, Direction>> blizzards;

    public static void main(final String[] args) throws IOException {
        System.out.println(new Challenge24().execute("src/main/resources/input24.txt", false));
        System.out.println(new Challenge24().execute("src/main/resources/input24.txt", true));
    }

    private long execute(final String filePath, final boolean goBack) throws IOException {
        try (final Stream<String> lines = (Files.lines(Paths.get(filePath)))) {
            map = lines.map(line -> line.chars().mapToObj(i -> (char) i)
                    .collect(Collectors.toList())).collect(Collectors.toList());

            blizzards = getBlizzards(map);

            final Tuple2<Integer, Integer> start = Tuples.of(0, 1);
            final Tuple2<Integer, Integer> end = Tuples.of(map.size() - 1, map.get(0).size() - 2);

            return goBack ? findExit(start, end) + findExit(end, start) + findExit(start, end) + 2 : findExit(start, end);
        }
    }

    private void moveBlizzards() {
        IntStream.range(0, blizzards.size())
                .forEach(j -> {
                    final Tuple3<Integer, Integer, Direction> blizzard = blizzards.get(j);

                    final Tuple2<Integer, Integer> newPosition = blizzard.getT3()
                            .move(map, Tuples.of(blizzard.getT1(), blizzard.getT2()), true);

                    blizzards.set(j, Tuples.of(newPosition.getT1(), newPosition.getT2(), blizzard.getT3()));

                    map.get(blizzard.getT1()).set(blizzard.getT2(), '.');
                });

        blizzards.forEach(blizzard -> map.get(blizzard.getT1()).set(blizzard.getT2(), blizzard.getT3().getName()));
    }

    private int findExit(final Tuple2<Integer, Integer> start, final Tuple2<Integer, Integer> end) {
        return findExit(0, end, List.of(start)).findFirst().orElseThrow();
    }

    private Stream<Integer> findExit(int round, final Tuple2<Integer, Integer> end, final Collection<Tuple2<Integer, Integer>> states) {
        moveBlizzards();

        final AtomicBoolean stop = new AtomicBoolean();

        final Collection<Tuple2<Integer, Integer>> newStates = states.parallelStream()
                .takeWhile(current -> !stop.get())
                .flatMap(current -> {
                    if (current.equals(end)) {
                        stop.set(true);
                    }

                    final List<Tuple2<Integer, Integer>> validMoves = getValidMoves(current);

                    if (map.get(current.getT1()).get(current.getT2()) == '.') {
                        validMoves.add(current);
                    }

                    return validMoves.stream();
                }).collect(Collectors.toSet());

        if (stop.get()) {
            return Stream.of(round);
        }

        return findExit(round + 1, end, newStates);
    }

    public List<Tuple2<Integer, Integer>> getValidMoves(final Tuple2<Integer, Integer> current) {
        return Arrays.stream(Direction.values())
                .flatMap(direction -> {
                    final Tuple2<Integer, Integer> newPosition = direction.move(map, current, false);

                    if (newPosition.getT1() < 0 || newPosition.getT2() < 0 ||
                            newPosition.getT1() >= map.size() || newPosition.getT2() >= map.get(0).size()) {
                        return Stream.empty();
                    }

                    if (map.get(newPosition.getT1()).get(newPosition.getT2()) == '.') {
                        return Stream.of(Tuples.of(newPosition.getT1(), newPosition.getT2()));
                    }

                    return Stream.empty();
                }).collect(Collectors.toList());
    }

    private List<Tuple3<Integer, Integer, Direction>> getBlizzards(final List<List<Character>> map) {
        return IntStream.range(0, map.size()).boxed()
                .flatMap(i -> IntStream.range(0, map.get(0).size()).boxed()
                        .flatMap(j -> Direction.getByName(map.get(i).get(j))
                                .map(d -> Tuples.of(i, j, d)).stream())).collect(Collectors.toList());
    }

    enum Direction {
        U('^'), D('v'), R('>'), L('<');

        private Character name;

        Direction(final Character name) {
            this.name = name;
        }

        public Character getName() {
            return name;
        }

        private Tuple2<Integer, Integer> move(final List<List<Character>> map, final Tuple2<Integer, Integer> position, boolean correct) {
            int width = map.get(0).size();

            if (this == D && position.getT1() == 0 && position.getT2() == 1) {
                return Tuples.of(1, 1);
            }

            if (this == U && position.getT1() == map.size() - 1 && position.getT2() == width - 2) {
                return Tuples.of(map.size() - 2, width - 2);
            }

            return switch (this) {
                case U -> Tuples.of(!correct || position.getT1() > 1 ? position.getT1() - 1 : map.size() - 2, position.getT2());
                case D -> Tuples.of(!correct || position.getT1() < map.size() - 2 ? position.getT1() + 1 : 1, position.getT2());
                case R -> Tuples.of(position.getT1(), !correct || position.getT2() < width - 2 ? position.getT2() + 1 : 1);
                case L -> Tuples.of(position.getT1(), !correct || position.getT2() > 1 ? position.getT2() - 1 : width - 2);
            };
        }

        static Optional<Direction> getByName(final Character name) {
            return Arrays.stream(Direction.values()).filter(d -> d.getName().equals(name)).findFirst();
        }
    }
}
