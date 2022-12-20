package is.symphony.challenge;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Challenge20 {

    public static void main(final String[] args) throws IOException {
        System.out.println(new Challenge20()
                .execute("src/main/resources/input20.txt", null, 1));
        System.out.println(new Challenge20()
                .execute("src/main/resources/input20.txt", 811589153L, 10));
    }

    private long execute(final String filePath, final Long decriptionKey, final Integer rounds) throws IOException {
        try (final Stream<String> lines = (Files.lines(Paths.get(filePath)))) {
            final AtomicInteger counter = new AtomicInteger(0);

            final List<Tuple2<Integer, Long>> workingList = lines.map(Long::parseLong)
                    .map(i -> decriptionKey != null ? i * decriptionKey : i)
                    .map(i -> Tuples.of(counter.getAndIncrement(), i)).collect(Collectors.toList());

            IntStream.range(0, rounds).boxed().forEach(
                    round -> IntStream.range(0, workingList.size()).boxed()
                            .map(i -> workingList.stream().filter(pair -> pair.getT1().equals(i)).findFirst().orElseThrow())
                            .forEach(pair -> handlePair(pair, workingList)));

            final AtomicInteger start = new AtomicInteger(-1);

            return Stream.iterate(0, i -> i - start.get() <= 3000, i -> i + 1)
                    .flatMap(i -> {
                        long value = workingList.get(i % workingList.size()).getT2();

                        if (start.get() > 0 && (i - start.get()) % 1000 == 0) {
                            return Stream.of(value);
                        }

                        if (value == 0 && start.get() == -1) {
                            start.set(i);
                        }

                        return Stream.empty();
                    }).mapToLong(v -> v).sum();
        }
    }

    private void handlePair(final Tuple2<Integer, Long> pair, final List<Tuple2<Integer, Long>> workingList) {
        if (pair.getT2().equals(0L)) {
            return;
        }

        int pairIndex = workingList.indexOf(pair);

        int newIndex = pairIndex + (int)(pair.getT2() % (workingList.size() - 1));

        if (pairIndex == newIndex) {
            return;
        }

        if (pair.getT2() > 0 && newIndex >= workingList.size()) {
            newIndex -= workingList.size() - 1;
        }
        else if (newIndex <= 0) {
            newIndex += workingList.size() - 1;
        }

        workingList.remove(pairIndex);

        if (newIndex > workingList.size()) {
            newIndex--;
        }
        else if (newIndex == workingList.size() && pair.getT2() > 0) {
            newIndex = 0;
        }

        workingList.add(newIndex, pair);
    }
}
