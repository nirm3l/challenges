package is.symphony.challenge;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Challenge10 {

    public static void main(final String[] args) throws IOException {
        System.out.println(new Challenge10().execute("src/main/resources/input10.txt", 20, 60, 100, 140, 180, 220));
    }

    private final List<List<String>> display = IntStream.range(0, 6).boxed()
            .map(i -> IntStream.range(0, 40).mapToObj(v -> ".").collect(Collectors.toList())).toList();

    private void printDisplay() {
        System.out.println(display.stream()
                .map(l -> String.join("", l))
                .collect(Collectors.joining("\n")));
    }

    private Integer execute(final String filePath, Integer... signalPositions) throws IOException {
        display.get(0).set(0, "#");

        try (final Stream<String> lines = (Files.lines(Paths.get(filePath)))) {
            final int sum = getSignal(lines)
                    .peek(value -> {
                        final int horizontalPosition = (value.getT1() - 1) % 40;
                        final int verticalPosition = (value.getT1() - 1) / 40;

                        if (Math.abs(horizontalPosition - value.getT2()) <= 1) {
                            display.get(verticalPosition).set(horizontalPosition, "#");
                        }
                    })
                    .map(v -> Tuples.of(v.getT1(), v.getT1() * v.getT2()))
                    .filter(value -> Arrays.asList(signalPositions).contains(value.getT1()))
                    .mapToInt(Tuple2::getT2).sum();

            printDisplay();

            return sum;
        }
    }

    private Stream<Tuple2<Integer, Integer>> getSignal(final Stream<String> lines) {
        final AtomicInteger counter = new AtomicInteger(1);

        final AtomicInteger signal = new AtomicInteger(1);

        return lines.flatMap(line -> {
            if (line.equals("noop")) {
                return Stream.of(Tuples.of(counter.incrementAndGet(), signal.get()));
            }

            int value = Integer.parseInt(line.replace("addx ", ""));

            return IntStream.range(0, 2)
                    .mapToObj(i -> i == 0 ? Tuples.of(counter.incrementAndGet(), signal.get()) : Tuples.of(counter.incrementAndGet(), signal.addAndGet(value)));
        });
    }
}
