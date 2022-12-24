package is.symphony.challenge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Challenge18 {
    public static void main(final String[] args) throws IOException {
        System.out.println(new Challenge18().execute("src/main/resources/input18.txt", false));
        System.out.println(new Challenge18().execute("src/main/resources/input18.txt", true));
    }

    private Integer execute(final String filePath, final boolean removeTrapped) throws IOException {
        try (final Stream<String> lines = (Files.lines(Paths.get(filePath)))) {
            final List<List<Integer>> locations =
                    lines.map(line -> Arrays.stream(line.split(","))
                            .map(Integer::parseInt).collect(Collectors.toList())).toList();

            final List<List<Integer>> connections = locations.stream()
                    .flatMap(loc -> Arrays.stream(Direction.values())
                            .map(direction -> direction.moveAndGet(loc))).collect(Collectors.toList());

            if (!removeTrapped) {
                return locations.size() * 6 - (int) connections.stream().filter(locations::contains).count();
            }
            else {
                return removeTrappedAndCalculate(locations, connections);
            }
        }
    }

    private Integer removeTrappedAndCalculate(
            final List<List<Integer>> locations, final List<List<Integer>> connections) {
        final Set<List<Integer>> in = new HashSet<>();
        final Set<List<Integer>> out = new HashSet<>();

        final AtomicInteger counter = new AtomicInteger();

        connections.forEach(loc -> {
            if (out.contains(loc)) {
                counter.incrementAndGet();
            }
            else {
                if (!in.contains(loc)) {
                    final Set<List<Integer>> seen = new HashSet<>();

                    final AtomicReference<Set<List<Integer>>> next = new AtomicReference<>(Set.of(loc));
                    final AtomicBoolean stop = new AtomicBoolean();

                    Stream.generate(() -> !stop.get() && !next.get().isEmpty())
                            .takeWhile(condition -> condition)
                            .forEach(i -> {
                                final Set<List<Integer>> nextSet = next.get().stream()
                                        .takeWhile(currentLoc -> !stop.get())
                                        .filter(currentLoc -> !locations.contains(currentLoc) && !seen.contains(currentLoc))
                                        .peek(seen::add)
                                        .filter(currentLoc -> {
                                            if (seen.size() > 5000) {
                                                out.addAll(seen);
                                                counter.incrementAndGet();

                                                stop.set(true);

                                                return false;
                                            }

                                            return true;
                                        }).flatMap(currentLoc -> Arrays.stream(Direction.values())
                                                .map(direction -> direction.moveAndGet(currentLoc))).collect(Collectors.toSet());

                                next.set(nextSet);
                            });

                    if (!stop.get()) {
                        in.addAll(seen);
                    }
                }
            }
        });

        return counter.get();
    }


    enum Direction {
        E, W, SW, NE, SE, NW;

        public List<Integer> moveAndGet(final List<Integer> loc) {
            return switch (this) {
                case E -> List.of(loc.get(0) + 1, loc.get(1), loc.get(2));
                case W -> List.of(loc.get(0) - 1, loc.get(1), loc.get(2));
                case SW -> List.of(loc.get(0), loc.get(1) + 1, loc.get(2));
                case NE -> List.of(loc.get(0), loc.get(1) - 1, loc.get(2));
                case SE -> List.of(loc.get(0), loc.get(1), loc.get(2) + 1);
                case NW -> List.of(loc.get(0), loc.get(1), loc.get(2) - 1);
            };
        }
    }
}
