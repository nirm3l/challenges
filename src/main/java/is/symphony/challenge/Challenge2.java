package is.symphony.challenge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;

public class Challenge2 {

    final static Map<String, Integer> MOVE_POINTS = Map.of(
            "X", 1, "Y", 2, "Z", 3, "A", 1, "B", 2, "C", 3);

    public static void main(final String[] args) throws IOException {
        System.out.println(new Challenge2().execute("src/main/resources/input2.txt", true));
    }

    public int execute(final String filePath, boolean calculateMove) throws IOException {
        try (final Stream<String> lines = (Files.lines(Paths.get(filePath)))) {
            return lines.map(line -> line.split(" "))
                    .parallel()
                    .peek(moves -> {
                        if (calculateMove) {
                            moves[1] = calculateMove(moves);
                        }
                    })
                    .map(moves -> {
                        int total = MOVE_POINTS.get(moves[1]);

                        if (MOVE_POINTS.get(moves[0]).equals(MOVE_POINTS.get(moves[1]))) {
                            total += 3;
                        }
                        else {
                            final int result = MOVE_POINTS.get(moves[1]) - MOVE_POINTS.get(moves[0]);

                            if (result == 1 || result == -2) {
                                total += 6;
                            }
                        }

                        return total;
                    }).mapToInt(Integer::intValue).sum();
        }
    }

    private String calculateMove(final String[] moves) {
        if ("X".equals(moves[1])) {
            return "A".equals(moves[0]) ? "Z" : "B".equals(moves[0]) ? "X" : "Y";
        }
        else if ("Z".equals(moves[1])) {
            return "A".equals(moves[0]) ? "Y" : "B".equals(moves[0]) ? "Z" : "X";
        }

        return moves[0];
    }
}
