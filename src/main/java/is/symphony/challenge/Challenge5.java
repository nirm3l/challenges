package is.symphony.challenge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Challenge5 {

    private final Pattern MOVE_PATTERN = Pattern.compile("move (\\d*) from (\\d*) to (\\d*)");
    private final Stack<String> tempStack = new Stack<>();

    public static void main(final String[] args) throws IOException {
        System.out.println(new Challenge5().execute(
                "src/main/resources/input5.txt", false));
    }

    public String execute(final String filePath, boolean multiple) throws IOException {
        try (final Stream<String> lines = (Files.lines(Paths.get(filePath)))) {
            return lines.reduce(new ArrayList<Stack<String>>(), (matrix, value) -> {
                if (value.contains("[")) {
                    final String finalValue = value + " ";

                    IntStream.range(0, initializeMatrix(matrix, finalValue.length()))
                            .forEach(i -> {
                                final int position = i * 4;
                                final String crate = cleanCrate(finalValue.substring(position, position + 4));

                                if (!crate.isEmpty()) {
                                    matrix.get(i).add(0, crate);
                                }
                            });
                }
                else {
                    handleMove(matrix, value, multiple);
                }

                return matrix;
            }, ((stacks, stacks2) -> stacks)).stream().map(Stack::peek).collect(Collectors.joining());
        }
    }

    private String cleanCrate(final String crate) {
        return crate.replace("[", "").replace("]", "").strip();
    }

    private int initializeMatrix(final List<Stack<String>> matrix, final int length) {
        if (matrix.size() == 0) {
            final int size = length / 4;

            IntStream.range(0, size)
                    .forEach(i -> matrix.add(new Stack<>()));
        }

        return matrix.size();
    }

    private void handleMove(final List<Stack<String>> matrix, final String line, boolean multiple) {
        Stream.iterate(MOVE_PATTERN.matcher(line), Matcher::find, m -> m)
                .forEach(matcher -> {
                    final int count = Integer.parseInt(matcher.group(1));
                    final int from = Integer.parseInt(matcher.group(2));
                    final int to = Integer.parseInt(matcher.group(3));

                    IntStream.range(0, count)
                            .forEach(i -> {
                                final String crate = matrix.get(from - 1).pop();

                                if (!multiple) {
                                    matrix.get(to - 1).push(crate);
                                }
                                else {
                                    tempStack.add(0, crate);
                                }
                            });

                    if (multiple) {
                        matrix.get(to - 1).addAll(tempStack);
                        tempStack.clear();
                    }
                });
    }
}
