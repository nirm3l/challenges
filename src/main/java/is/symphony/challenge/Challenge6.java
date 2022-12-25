package is.symphony.challenge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Challenge6 {

    private final Map<Character, AtomicInteger> duplicatesCounter = new HashMap<>();

    public static void main(final String[] args) throws IOException {
        System.out.println(new Challenge6().execute(
                "src/main/resources/input6.txt", 4));
    }

    public Integer execute(final String filePath, final int numOfCharacters) throws IOException {
        try (final Stream<String> lines = (Files.lines(Paths.get(filePath)))) {
            final String line = lines.findFirst().orElseThrow();

            //return quickSolution(line, numOfCharacters);

            return goodSolution(line, numOfCharacters);
        }
    }

    private int quickSolution(final String line, final int numOfCharacters) {
        return IntStream.range(numOfCharacters, line.length()).boxed()
                .flatMap(i -> {
                    final String current = line.substring(i - numOfCharacters, i);

                    if (current.length() == (int)current.chars().distinct().count()) {
                        return Stream.of(i);
                    }

                    return Stream.empty();
                }).findFirst().orElseThrow();
    }

    private int goodSolution(final String line, final int numOfCharacters) {
        return IntStream.range(0, line.length()).boxed()
                .flatMap(i -> {
                    int size = Math.min(i, numOfCharacters);

                    final String currentString = line.substring(i - size, i);

                    checkDuplicate(line.charAt(i), currentString);

                    if (i >= numOfCharacters) {
                        removeLastIfNeeded(currentString.charAt(0));
                    }

                    if (size == numOfCharacters && duplicatesCounter.isEmpty()) {
                        return Stream.of(i + 1);
                    }

                    return Stream.empty();
                }).findFirst().orElseThrow();
    }

    private void checkDuplicate(final Character current, final String stringToCheck) {
        if (stringToCheck.indexOf(current) != -1) {
            final AtomicInteger counter = duplicatesCounter.get(current);

            if (counter == null) {
                duplicatesCounter.put(current, new AtomicInteger(1));
            }
            else {
                counter.incrementAndGet();
            }
        }
    }

    private void removeLastIfNeeded(final Character remove) {
        final AtomicInteger counter = duplicatesCounter.get(remove);

        if (counter != null && counter.decrementAndGet() == 0) {
            duplicatesCounter.remove(remove);
        }
    }
}
