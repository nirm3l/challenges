package is.symphony.challenge;

import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
                    .map(line -> {
                        final String secondPart = line.substring(line.length() / 2);

                        for (int i = 0; i < line.length() / 2; i++) {
                            char c = line.charAt(i);

                            if (secondPart.contains(String.valueOf(c))) {
                                return c;
                            }
                        }

                        throw new IllegalStateException("Cannot find item, bad input file!");
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
                    .map(group -> {
                        final String firstLine = group.get(0);
                        final String secondLine = group.get(1);
                        final String thirdLine = group.get(2);

                        for (int i = 0; i < firstLine.length(); i++) {
                            final String currentChar = String.valueOf(firstLine.charAt(i));

                            if (secondLine.contains(currentChar) && thirdLine.contains(currentChar)) {
                                return currentChar.charAt(0);
                            }
                        }

                        throw new IllegalStateException("Cannot find item, bad input file!");
                    })
                    .map(this::calculatePriority)
                    .toStream()
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
