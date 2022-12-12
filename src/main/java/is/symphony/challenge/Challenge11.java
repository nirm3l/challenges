package is.symphony.challenge;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Challenge11 {

    private Monkey currentMonkey;

    public static void main(final String[] args) throws IOException {
        System.out.println(new Challenge11().execute("src/main/resources/input11.txt", 20, true));
        System.out.println(new Challenge11().execute("src/main/resources/input11.txt", 10000, false));
    }

    private Long execute(final String filePath, final int rounds, final boolean divideWorries) throws IOException {
        try (final Stream<String> lines = (Files.lines(Paths.get(filePath)))) {
            return lines.map(line -> createOrUpdateMonkey(line, divideWorries, currentMonkey))
                    .filter(monkey -> currentMonkey != monkey)
                    .peek(monkey -> currentMonkey = monkey)
                    .collect(Collectors.collectingAndThen(Collectors.toList(),
                            monkeys -> processRounds(monkeys, rounds)
                                    .stream().sorted(Comparator.comparing(Monkey::getInspectionsCounter).reversed())
                                    .limit(2).map(Monkey::getInspectionsCounter).reduce(1L, (a, b) -> a * b)));
        }
    }

    private Monkey createOrUpdateMonkey(String line, final boolean divideWorries, final Monkey monkey) {
        line = line.strip();

        if (line.startsWith("Monkey")) {
            return new Monkey(divideWorries);
        }
        else {
            if (line.startsWith("Starting items:")) {
                line = line.replace("Starting items: ", "");

                monkey.addItems(Arrays.stream(line.split(", ")).map(Integer::parseInt).map(Long::valueOf).toList());
            }
            if (line.startsWith("Operation:")) {
                line = line.replace("Operation: new = ", "");

                final String[] parts = line.split(" ");

                Stream.of(parts[0], parts[2]).forEach(part -> {
                    if (!part.equals("old")) {
                        monkey.setOperationValue(Long.parseLong(part));
                    }
                });

                monkey.setOperation(Operation.getOperation(parts[1]));
            }
            else if (line.startsWith("Test:")) {
                line = line.replace("Test: divisible by ", "");

                monkey.setDivisibleCheck(Long.parseLong(line));
            }
            else if (line.startsWith("If ")) {
                line = line.replace("If ", "");

                if (line.startsWith("true")) {
                    line = line.replace("true: throw to monkey ", "");

                    monkey.getThrowMap().put(true, Integer.parseInt(line));
                }
                else if (line.startsWith("false")) {
                    line = line.replace("false: throw to monkey ", "");

                    monkey.getThrowMap().put(false, Integer.parseInt(line));
                }
            }

            return monkey;
        }
    }

    private List<Monkey> processRounds(final List<Monkey> monkeys, final int rounds) {
        long corrector = monkeys.stream().map(Monkey::getDivisibleCheck).reduce(1L, (a, b) -> a * b);

        IntStream.range(0, rounds).forEach(i -> monkeys.forEach(
                monkey -> monkey.throwItems(corrector).forEach(
                        action -> monkeys.get(action.getT1()).getItems().add(action.getT2()))));

        return monkeys;
    }

    static class Monkey {
        private final List<Long> items = new ArrayList<>();

        private Operation operation;

        private Long divisibleCheck;

        private Long operationValue;

        private final AtomicLong inspectionsCounter = new AtomicLong(0);

        private final Map<Boolean, Integer> throwMap = new HashMap<>();

        private final boolean divideWorries;

        public Monkey(boolean divideWorries) {
            this.divideWorries = divideWorries;
        }

        public List<Tuple2<Integer, Long>> throwItems(long corrector) {
            return items.stream().map(item -> {
                Long newItem = operation.execute(item, operationValue);

                if (divideWorries) {
                    newItem = newItem / 3;
                }

                inspectionsCounter.incrementAndGet();

                return Tuples.of(throwMap.get(newItem % divisibleCheck == 0), newItem % corrector);
            }).collect(Collectors.collectingAndThen(Collectors.toList(), result -> {
                items.clear();

                return result;
            }));
        }

        public Map<Boolean, Integer> getThrowMap() {
            return throwMap;
        }

        public void setOperation(final Operation operation) {
            this.operation = operation;
        }

        public void setDivisibleCheck(final Long divisibleCheck) {
            this.divisibleCheck = divisibleCheck;
        }

        public void addItems(final List<Long> items) {
            this.items.addAll(items);
        }

        public void setOperationValue(final Long operationValue) {
            this.operationValue = operationValue;
        }

        public List<Long> getItems() {
            return items;
        }

        public Long getInspectionsCounter() {
            return inspectionsCounter.get();
        }

        public Long getDivisibleCheck() {
            return divisibleCheck;
        }
    }

    enum Operation {
        ADD("+", (a, b) -> a + b),
        MULTIPLY("*", (a, b) -> a * b);

        private final String sign;

        private final BiFunction<Long, Long, Long> function;

        Operation(final String sign, final BiFunction<Long, Long, Long> operationFunction) {
            this.sign = sign;
            this.function = operationFunction;
        }

        Long execute(final Long old, final Long value) {
            return value == null ? function.apply(old, old) : function.apply(old, value);
        }

        static Operation getOperation(final String sign) {
            return Arrays.stream(Operation.values()).filter(o -> o.sign.equals(sign)).findFirst().orElse(null);
        }
    }
}
