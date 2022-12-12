package is.symphony.challenge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class Challenge1 {

    public static void main(final String[] args) throws IOException {
        System.out.println(new Challenge1().execute("src/main/resources/input1.txt", 3));
    }

    public int execute(final String filePath, final int sumOfTop) throws IOException {
        try (final Stream<String> lines = (Files.lines(Paths.get(filePath)))) {
            return lines
                    .map(line -> line.isEmpty() ? -1 : Integer.parseInt(line))
                    .reduce(new ArrayList<>(List.of(0)), (list, value) -> {
                        if (value == -1) {
                            list.add(0);
                        }
                        else {
                            final int lastIndex = list.size() - 1;

                            list.set(lastIndex, list.get(lastIndex) + value);
                        }

                        return list;
                    }, (i1, i2) -> i1)
                            .stream()
                            .sorted(Comparator.comparingInt(i -> (int) i).reversed())
                            .limit(sumOfTop).mapToInt(Integer::intValue).sum();
        }
    }
}
