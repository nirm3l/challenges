package is.symphony.challenge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

public class Challenge4 {

    public static void main(final String[] args) throws IOException {
        System.out.println(new Challenge4().execute(
                "src/main/resources/input4.txt", false));
    }

    public int execute(final String filePath, boolean fullOverlap) throws IOException {
        try (final Stream<String> lines = (Files.lines(Paths.get(filePath)))) {
            return lines
                    .parallel()
                    .map(value -> {
                        final String[] pairs = value.split(",");

                        final Integer[] tuple1 = Arrays.stream(pairs[0].split("-")).map(Integer::parseInt).toArray(Integer[]::new);
                        final Integer[] tuple2 = Arrays.stream(pairs[1].split("-")).map(Integer::parseInt).toArray(Integer[]::new);

                        if (fullOverlap) {
                            if (tuple1[0] >= tuple2[0] && tuple1[1] <= tuple2[1] || tuple2[0] >= tuple1[0] && tuple2[1] <= tuple1[1]) {
                                return  1;
                            }
                        }
                        else {
                            if (tuple1[0] >= tuple2[0] && tuple1[0] <= tuple2[1] ||
                                    tuple1[1] <= tuple2[1] && tuple1[1] >= tuple2[0] ||
                                    tuple2[0] >= tuple1[0] && tuple2[0] <= tuple1[1] ||
                                    tuple2[1] <= tuple1[1] && tuple2[1] >= tuple1[0]) {
                                return 1;
                            }
                        }

                        return 0;
                    })
                    .mapToInt(Integer::intValue).sum();
        }
    }
}
