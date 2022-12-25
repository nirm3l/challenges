package is.symphony.challenge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Challenge25 {
    public static void main(final String[] args) throws IOException {
        System.out.println(new Challenge25().execute("src/main/resources/input25.txt"));
    }

    private String execute(final String filePath) throws IOException {
        try (final Stream<String> lines = (Files.lines(Paths.get(filePath)))) {
            return decimalToSnafu(
                    lines.map(this::snafuToDecimal)
                            .mapToLong(i -> i).sum());
        }
    }

    private long snafuToDecimal(final String snafu) {
        final AtomicLong base = new AtomicLong(1);

        return IntStream.rangeClosed(0, snafu.length() - 1)
                .mapToLong(i -> {
                    final String c = snafu.substring(snafu.length() - i - 1, snafu.length() - i);

                    if (c.equals("=")) {
                        return -2;
                    }
                    else if (c.equals("-")) {
                        return -1;
                    }

                    return Long.parseLong(c);
                }).reduce(0, (total, current) -> total + current * base.getAndSet(base.get() * 5));
    }

    private String decimalToSnafu(long decimal) {
        final AtomicLong current = new AtomicLong(decimal);

        return IntStream.iterate(0, i -> current.get() > 0, i -> i + 1).boxed()
                .map(i -> {
                    long value = current.get();
                    long mod = value % 5;

                    if (mod == 3) {
                        current.set((value + 2) / 5);

                        return "=";
                    }
                    else if (mod == 4) {
                        current.set((value + 1) / 5);

                        return "-";
                    }
                    else {
                        current.set(value / 5);
                        return String.valueOf(mod);
                    }
                }).reduce("", (s, c) -> c + s);
    }
}
