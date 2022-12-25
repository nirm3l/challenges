package is.symphony.challenge;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class Challenge15 {
    private final Pattern PATTERN = Pattern.compile(".*x=(-?\\d*).*y=(-?\\d*).*x=(-?\\d*).*y=(-?\\d*)");

    public static void main(final String[] args) throws IOException {
        System.out.println(new Challenge15().getCoveredPositionsCount("src/main/resources/input15.txt", 10));
        System.out.println(new Challenge15().getTuningFrequencyFor("src/main/resources/input15.txt", 20));
    }

    private long getTuningFrequencyFor(final String filePath, long searchSpace) throws IOException {
        final List<Tuple3<Tuple2<Long, Long>, Tuple2<Long, Long>, Long>> input = getSensorsAndBeaconsWithDistances(filePath);

        return getBorderPositions(input)
                .filter(pos -> pos.getT1() >= 0 && pos.getT1() <= searchSpace && pos.getT2() >= 0 && pos.getT2() <= searchSpace)
                .filter(pos -> input.stream().allMatch(p -> calculateManhattanDistance(pos, p.getT1()) > p.getT3()))
                .findFirst().map(this::getTuningFrequency).orElseThrow();
    }

    private long getCoveredPositionsCount(final String filePath, final long y) throws IOException {
        final List<Tuple3<Tuple2<Long, Long>, Tuple2<Long, Long>, Long>> sensorsAndBeacons = getSensorsAndBeaconsWithDistances(filePath);

        final Set<Tuple2<Long, Long>> beaconsSet = sensorsAndBeacons.stream().map(Tuple2::getT2).collect(Collectors.toSet());

        return getMinMaxRange(sensorsAndBeacons)
                .mapToObj(i -> Tuples.of(i, y))
                .filter(l -> !beaconsSet.contains(l))
                .filter(l -> sensorsAndBeacons.stream().anyMatch(p -> calculateManhattanDistance(l, p.getT1()) <= p.getT3())).count();
    }

    private LongStream getMinMaxRange(final List<Tuple3<Tuple2<Long, Long>, Tuple2<Long, Long>, Long>> sensorsAndBeacons) {
        return LongStream.range(
                sensorsAndBeacons.stream().mapToLong(v -> v.getT1().getT1() - v.getT3()).min().orElseThrow(),
                sensorsAndBeacons.stream().mapToLong(v -> v.getT1().getT1() + v.getT3()).max().orElseThrow());
    }

    private Stream<Tuple2<Long, Long>> getBorderPositions(
            final List<Tuple3<Tuple2<Long, Long>, Tuple2<Long, Long>, Long>> sensorsAndBeacons) {
        return sensorsAndBeacons.stream()
                .flatMap(pair -> LongStream.rangeClosed(0, pair.getT3() + 1).boxed()
                        .flatMap(i -> Stream.of(-pair.getT3() + i - 1, pair.getT3() + 1 - i)
                                .flatMap(j -> Stream.of(
                                        Tuples.of(pair.getT1().getT1() + j, pair.getT1().getT2() + i),
                                        Tuples.of(pair.getT1().getT1() + j, pair.getT1().getT2() - i)))));
    }

    private List<Tuple3<Tuple2<Long, Long>, Tuple2<Long, Long>, Long>> getSensorsAndBeaconsWithDistances(final String filePath) throws IOException {
        try (final Stream<String> lines = (Files.lines(Paths.get(filePath)))) {
            return lines.flatMap(line -> getSensorAndBeacon(line).stream())
                    .map(pair -> Tuples.of(pair.getT1(), pair.getT2(), calculateManhattanDistance(pair.getT1(), pair.getT2()))).collect(Collectors.toList());
        }
    }

    private Optional<Tuple2<Tuple2<Long, Long>, Tuple2<Long, Long>>> getSensorAndBeacon(final String line) {
        return Stream.iterate(PATTERN.matcher(line), Matcher::find, m -> m)
                .map(matcher -> Tuples.of(
                        Tuples.of(Long.parseLong(matcher.group(1)), Long.parseLong(matcher.group(2))),
                        Tuples.of(Long.parseLong(matcher.group(3)), Long.parseLong(matcher.group(4))))).findFirst();
    }

    private long calculateManhattanDistance(final Tuple2<Long, Long> pos1, final Tuple2<Long, Long> pos2) {
        return Math.abs(pos1.getT1() - pos2.getT1()) + Math.abs(pos1.getT2() - pos2.getT2());
    }

    private long getTuningFrequency(final Tuple2<Long, Long> position) {
        return position.getT1() * 4000000L + position.getT2();
    }
}
