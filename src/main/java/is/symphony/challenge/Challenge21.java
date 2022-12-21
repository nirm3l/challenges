package is.symphony.challenge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class Challenge21 {
    private static final String HUMAN_KEY = "humn";
    private static final String ROOT_KEY = "root";

    public static void main(final String[] args) throws IOException {
        System.out.println(new Challenge21().execute("src/main/resources/input21.txt", false));
        System.out.println(new Challenge21().execute("src/main/resources/input21.txt", true));
    }

    private long execute(final String filePath, final boolean guessNumber) throws IOException {
        try (final Stream<String> lines = (Files.lines(Paths.get(filePath)))) {
            final Map<String, Monkey> monkeyMap = new HashMap<>();

            lines.map(line -> parseMonkey(line, monkeyMap)).forEach(monkey -> monkeyMap.put(monkey.getName(), monkey));

            return guessNumber ? findGuessNumber(monkeyMap) : (long) monkeyMap.get(ROOT_KEY).getResult();
        }
    }

    private Stream<Long> guessSearch(
            final Consumer<Long> prepareHumanMonkey, final Supplier<Double> diffSupplier,
            final Long start, final Long end, final Integer sign) {
        long step = Math.floorDiv(end - start, 2);

        final AtomicBoolean stop = new AtomicBoolean();
        final AtomicReference<Integer> signHolder = new AtomicReference<>(sign);

        return LongStream.iterate(start, i -> !stop.get() && i < end + step, i -> i + step).boxed()
                .flatMap(guessNumber -> {
                    prepareHumanMonkey.accept(guessNumber);

                    double diff = diffSupplier.get();

                    if (diff == 0) {
                        stop.set(true);
                        return Stream.of(guessNumber);
                    }

                    if (signHolder.get() == null) {
                        signHolder.set(diff > 0 ? 1 : -1);
                    }

                    if (diff * signHolder.get() < 0) {
                        stop.set(true);

                        return guessSearch(prepareHumanMonkey, diffSupplier, guessNumber - step, guessNumber, signHolder.get());
                    }

                    return Stream.empty();
                });
    }

    private Long findGuessNumber(final Map<String, Monkey> monkeyMap) {
        final Monkey root = monkeyMap.get(ROOT_KEY);

        final Monkey monkey1 = monkeyMap.get(root.getMonkey1());
        final Monkey monkey2 = monkeyMap.get(root.getMonkey2());

        return guessSearch(
                guessNumber -> monkeyMap.put(HUMAN_KEY, new Monkey(HUMAN_KEY, monkeyMap::get).setNumber(guessNumber)),
                () -> monkey2.getResult() - monkey1.getResult(), 0L, 10000000000000L, null)
                .findFirst().orElseThrow();
    }

    private Monkey parseMonkey(final String line, final Map<String, Monkey> monkeyMap) {
        final String[] parts = line.split(": ");

        final Monkey monkey = new Monkey(parts[0], monkeyMap::get);
        try {
            monkey.setNumber(Long.parseLong(parts[1]));
        }
        catch (final NumberFormatException e) {
            final String[] operationParts = parts[1].split(" ");

            monkey.setMonkey1(operationParts[0]);
            monkey.setMonkey2(operationParts[2]);
            monkey.setOperation(operationParts[1].charAt(0));
        }

        return monkey;
    }

    static class Monkey {
        private final String name;

        private String monkey1;

        private String monkey2;

        private Character operation;

        private Long number;

        private final Function<String, Monkey> monkeyProvider;

        Monkey(final String name, final Function<String, Monkey> monkeyProvider) {
            this.name = name;
            this.monkeyProvider = monkeyProvider;
        }

        public double getResult() {
            if (number != null) {
                return number;
            }

            return switch (operation) {
                case '+' -> monkeyProvider.apply(monkey1).getResult() + monkeyProvider.apply(monkey2).getResult();
                case '*' -> monkeyProvider.apply(monkey1).getResult() * monkeyProvider.apply(monkey2).getResult();
                case '-' -> monkeyProvider.apply(monkey1).getResult() - monkeyProvider.apply(monkey2).getResult();
                case '/' -> monkeyProvider.apply(monkey1).getResult() / monkeyProvider.apply(monkey2).getResult();
                default -> throw new IllegalArgumentException("Unknown operation: " + operation);
            };
        }

        public String getName() {
            return name;
        }

        public String getMonkey1() {
            return monkey1;
        }

        public void setMonkey1(final String monkey1) {
            this.monkey1 = monkey1;
        }

        public String getMonkey2() {
            return monkey2;
        }

        public void setMonkey2(final String monkey2) {
            this.monkey2 = monkey2;
        }

        public void setOperation(final Character operation) {
            this.operation = operation;
        }

        public Monkey setNumber(final Long number) {
            this.number = number;

            return this;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
