package is.symphony.challenge;

import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Challenge3 {

    public static void main(final String[] args) throws IOException {
        System.out.println(new Challenge3().executeGroup(
                "src/main/resources/input3.txt"));
    }

    public int execute(final String filePath) throws IOException {
        try (final Stream<String> lines = (Files.lines(Paths.get(filePath)))) {
            return lines
                    .parallel()
                    .flatMap(line -> {
                        final String secondPart = line.substring(line.length() / 2);

                        return IntStream.range(0, line.length() / 2).boxed()
                                .flatMap(i -> {
                                    char c = line.charAt(i);

                                    if (secondPart.contains(String.valueOf(c))) {
                                        return Stream.of(c);
                                    }

                                    return Stream.empty();
                                }).findFirst().stream();
                    })
                    .map(this::calculatePriority)
                    .mapToInt(Integer::intValue).sum();
        }
    }

    public int executeGroup(final String filePath) throws IOException {
        try (final Stream<String> lines = (Files.lines(Paths.get(filePath)))) {
            return Flux.fromStream(lines)
                    .window(3)
                    .flatMap(Flux::collectList)
                    .toStream()
                    .flatMap(group -> {
                        final String firstLine = group.get(0);
                        final String secondLine = group.get(1);
                        final String thirdLine = group.get(2);

                        return IntStream.range(0, firstLine.length()).boxed()
                                .flatMap(i -> {
                                    final String currentChar = String.valueOf(firstLine.charAt(i));

                                    if (secondLine.contains(currentChar) && thirdLine.contains(currentChar)) {
                                        return Stream.of(currentChar.charAt(0));
                                    }

                                    return Stream.empty();
                                }).findFirst().stream();
                    })
                    .map(this::calculatePriority)
                    .mapToInt(Integer::intValue).sum();
        }
    }


    private int calculatePriority(final Character character) {
        final int ordinalValue = (int)character;

        if (ordinalValue >= 65 && ordinalValue <= 90) {
            return ordinalValue - 38;
        }
        else if (ordinalValue >= 97 && ordinalValue <= 122) {
            return ordinalValue - 96;
        }

        return 0;
    }
}
