package is.symphony.challenge;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Challenge23 {

    private List<List<Character>> map;

    private List<List<Direction>> rules;

    private final static Character ELF = '#';

    public static void main(final String[] args) throws IOException {
        System.out.println(new Challenge23().execute("src/main/resources/input23.txt", 10));
        System.out.println(new Challenge23().execute("src/main/resources/input23.txt", null));
    }

    private long execute(final String filePath, final Integer rounds) throws IOException {
        try (final Stream<String> lines = (Files.lines(Paths.get(filePath)))) {
            map = lines.map(line -> line.chars().mapToObj(i -> (char) i).collect(Collectors.toList())).collect(Collectors.toList());

            rules = createRulesList();

            if (rounds != null) {
                IntStream.range(0, rounds).boxed()
                        .forEach(round -> handleRound());

                return shrinkMap(map).stream().flatMap(Collection::stream).filter(c -> c != ELF).count();
            }

            return IntStream.iterate(1, i -> handleRound(), i -> i + 1).max().orElseThrow() + 1;
        }
    }

    private boolean handleRound() {
        try {
            final Map<Tuple2<Integer, Integer>, Tuple2<Integer, Integer>> considerations = new HashMap<>();
            final List<List<AtomicInteger>> considerationsMap = createConsiderationsMap();

            IntStream.range(0, map.size())
                    .forEach(i -> IntStream.range(0, map.get(i).size())
                            .filter(j -> map.get(i).get(j) == ELF)
                            .forEach(j -> {
                                final Tuple2<Integer, Integer> position = Tuples.of(i, j);
                                final Direction nextDirection = getNextDirection(position);

                                if (nextDirection != null) {
                                    final Tuple2<Integer, Integer> nextPosition = nextDirection.move(position);

                                    considerations.put(position, Tuples.of(nextPosition.getT1(), nextPosition.getT2()));
                                    considerationsMap.get(nextPosition.getT1()).get(nextPosition.getT2()).incrementAndGet();
                                }
                            }));

            final AtomicBoolean moved = new AtomicBoolean();

            considerations.forEach((position, nextConsideration) -> {
                if (considerationsMap.get(nextConsideration.getT1()).get(nextConsideration.getT2()).get() == 1) {
                    map.get(position.getT1()).set(position.getT2(), '.');
                    map.get(nextConsideration.getT1()).set(nextConsideration.getT2(), ELF);

                    moved.set(true);
                }
            });

            rules.add(rules.get(0));
            rules.remove(0);

            return moved.get();
        }
        catch (IndexOutOfBoundsException e) {
            map = extendMap(map);

            return handleRound();
        }
    }

    private List<List<Character>> shrinkMap(final List<List<Character>> map) {
        final Tuple2<Integer, Integer> x = Tuples.of(
                IntStream.range(0, map.size()).filter(i -> map.get(i).contains(ELF)).findFirst().orElseThrow(),
                IntStream.rangeClosed(1, map.size()).filter(i -> map.get(map.size() - i).contains(ELF))
                        .map(i -> map.size() - i).findFirst().orElseThrow());

        final int width = map.get(0).size();

        final Tuple2<Integer, Integer> y = Tuples.of(
                IntStream.range(0, width).filter(j -> map.stream().mapToInt(list -> list.get(j)).anyMatch(c -> c == ELF)).findFirst().orElseThrow(),
                IntStream.rangeClosed(1, width).filter(j -> map.stream().mapToInt(list -> list.get(width - j)).anyMatch(c -> c == ELF))
                        .map(i -> width - i).findFirst().orElseThrow());

        return IntStream.rangeClosed(x.getT1(), x.getT2()).boxed()
                .map(i -> IntStream.rangeClosed(y.getT1(), y.getT2()).boxed().map(j -> map.get(i).get(j)).toList()).toList();
    }

    private List<List<Character>> extendMap(final List<List<Character>> map) {
        return IntStream.range(0, map.size() + 2).boxed()
                .map(i -> IntStream.range(0, map.get(0).size() + 2).boxed().map(j -> '.').collect(Collectors.toList()))
                .collect(Collectors.collectingAndThen(Collectors.toList(), newMap -> {
                    IntStream.range(0, map.size()).boxed()
                            .forEach(i -> IntStream.range(0, map.get(i).size()).boxed()
                                    .forEach(j -> newMap.get(i + 1).set(j + 1, map.get(i).get(j))));

                    return newMap;
                }));
    }

    private Direction getNextDirection(final Tuple2<Integer, Integer> position) {
        if (Arrays.stream(Direction.values()).noneMatch(current -> current.checkIfNextIsNotEmpty(map, position))) {
            return null;
        }

        return rules.stream().filter(rule -> rule.stream().noneMatch(current -> current.checkIfNextIsNotEmpty(map, position)))
                .map(rule -> rule.get(0)).findFirst().orElse(null);
    }

    private List<List<Direction>> createRulesList() {
        final List<List<Direction>> queue = new ArrayList<>();

        queue.add(List.of(Direction.N, Direction.NE, Direction.NW));
        queue.add(List.of(Direction.S, Direction.SE, Direction.SW));
        queue.add(List.of(Direction.W, Direction.NW, Direction.SW));
        queue.add(List.of(Direction.E, Direction.NE, Direction.SE));

        return queue;
    }

    private List<List<AtomicInteger>> createConsiderationsMap() {
        return IntStream.range(0, map.size()).boxed()
                .map(i -> IntStream.range(0, map.get(i).size()).mapToObj(j -> new AtomicInteger()).collect(Collectors.toList())).toList();
    }

    enum Direction {
        N, S, E, W, NE, NW, SE, SW;

        private boolean checkIfNextIsNotEmpty(final List<List<Character>> map, final Tuple2<Integer, Integer> position) {
            return switch (this) {
                case N -> map.get(position.getT1() - 1).get(position.getT2()) == ELF;
                case S -> map.get(position.getT1() + 1).get(position.getT2()) == ELF;
                case E -> map.get(position.getT1()).get(position.getT2() + 1) == ELF;
                case W -> map.get(position.getT1()).get(position.getT2() - 1) == ELF;
                case NE -> map.get(position.getT1() - 1).get(position.getT2() + 1) == ELF;
                case NW -> map.get(position.getT1() - 1).get(position.getT2() - 1) == ELF;
                case SE -> map.get(position.getT1() + 1).get(position.getT2() + 1) == ELF;
                case SW -> map.get(position.getT1() + 1).get(position.getT2() - 1) == ELF;
            };
        }

        private Tuple2<Integer, Integer> move(final Tuple2<Integer, Integer> position) {
            return switch (this) {
                case N -> Tuples.of(position.getT1() - 1, position.getT2());
                case S -> Tuples.of(position.getT1() + 1, position.getT2());
                case E -> Tuples.of(position.getT1(), position.getT2() + 1);
                case W -> Tuples.of(position.getT1(), position.getT2() - 1);
                default -> throw new IllegalStateException("Unexpected value: " + this);
            };
        }
    }
}
