package is.symphony.challenge;


import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Challenge17 {
    public static void main(final String[] args) throws IOException {
        System.out.println(new Challenge17().execute("src/main/resources/input17.txt", 2022L));
        System.out.println(new Challenge17().execute("src/main/resources/input17.txt", 1000000000000L));
    }

    private long execute(final String filePath, final Long rocksNumber) throws IOException {
        try (final Stream<String> lines = (Files.lines(Paths.get(filePath)))) {
            List<Direction> directions = lines.flatMap(line -> line.chars()
                    .mapToObj(i -> (char) i)).map(Direction::getByCharacter).toList();

            if (rocksNumber == 1000000000000L) {
                final Chamber start = new Chamber().execute(directions, (rockCount, cycleCount) -> cycleCount == 0);
                final Chamber next = new Chamber().execute(directions, (rockCount, cycleCount) -> cycleCount <= 1);

                long rocksPerCycle = next.getRockCount() - start.getRockCount();
                long cyclesCount = rocksNumber / rocksPerCycle;
                long totalRocks = rocksPerCycle * cyclesCount + start.getRockCount();
                long heightPerCycle = next.getHighestPosition() - start.getHighestPosition();
                long totalHeight = heightPerCycle * cyclesCount + start.getHighestPosition();

                final Chamber overshootChamber = new Chamber().execute(
                        directions, (rockCount, cycleCount) -> rockCount < start.getRockCount() - totalRocks - rocksNumber);

                return totalHeight - (start.getHighestPosition() - overshootChamber.getHighestPosition());
            }

            return new Chamber().execute(directions, (rockCount, cycleCount) -> rockCount < rocksNumber).getHighestPosition();
        }
    }

    static class Chamber {
        Integer nextRockId = 0;

        final List<List<Character>> content = new ArrayList<>();

        Integer highestPosition = 0;

        Rock currentRock = null;

        Tuple2<Integer, Integer> rockPosition = null;

        Tuple2<Integer, Integer> previousPosition = null;

        final AtomicInteger rockCounter = new AtomicInteger();
        final AtomicInteger cycleCounter = new AtomicInteger();

        final int width = 7;

        public Chamber() {
        }

        public Chamber execute(final List<Direction> gasStream, BiPredicate<Integer, Integer> condition) {
            Stream.iterate(cycleCounter, i -> condition.test(rockCounter.get(), cycleCounter.get()), c -> {
                        c.incrementAndGet();

                        return c;
                    })
                    .flatMap(i -> gasStream.stream().takeWhile(direction -> condition.test(rockCounter.get(), cycleCounter.get())))
                    .forEach(direction -> Stream.of(false, true)
                            .forEach(fall -> {
                                if (currentRock == null) {
                                    getNewRock();

                                    rockPosition = Tuples.of(rockPosition.getT1(), rockPosition.getT2());

                                    applyRock('@', fall);
                                    if (!condition.test(rockCounter.get() + 1, cycleCounter.get())) {
                                        //print();
                                    }

                                    removeRock();
                                }

                                previousPosition = rockPosition;
                                rockPosition = getNewPosition(direction, fall);

                                boolean end = applyRock('@', fall);

                                if (!end) {
                                    removeRock();
                                }
                            }));
            return this;
        }

        private Tuple2<Integer, Integer> getNewPosition(final Direction direction, final boolean fall) {
            int xPosition = direction == Direction.LEFT ? rockPosition.getT1() - 1 : rockPosition.getT1() + 1;
            int yPosition = rockPosition.getT2();

            if (fall) {
                xPosition = rockPosition.getT1();
                yPosition = rockPosition.getT2() - 1;
            }

            if (xPosition < 0) {
                xPosition = rockPosition.getT1();
            }
            else if (xPosition + currentRock.getContent().get(0).size() > width) {
                xPosition = rockPosition.getT1();
            }

            return Tuples.of(xPosition, yPosition);
        }

        public void addLine() {
            content.add(0, IntStream.range(0, width).boxed().map(i -> ' ').collect(Collectors.toList()));
        }

        public void getNewRock() {
            currentRock = getNextRock();

            final int topPosition = highestPosition + currentRock.getContent().size() + 3;

            IntStream.rangeClosed(content.size(), topPosition).forEach(i -> addLine());

            rockPosition = Tuples.of(2, topPosition);
        }

        public Rock getNextRock() {
            int rock = nextRockId % 5;

            nextRockId++;

            return switch (rock) {
                case 0 -> new Minus();
                case 1 -> new Plus();
                case 2 -> new RockL();
                case 3 -> new RockI();
                case 4 -> new Block();
                default -> throw new IllegalStateException("Unexpected value: " + rock);
            };
        }

        public void print() {
            if (content.size() == 0) {
                return;
            }

            final int width = content.get(0).size();

            IntStream.rangeClosed(0, content.size())
                    .forEach(i -> {
                        IntStream.range(0, width)
                                .forEach(j -> {
                                    if (j == 0) {
                                        System.out.print(i == content.size() ? '+' : '|');
                                    }

                                    if (i == content.size()) {
                                        System.out.print('-');
                                    }
                                    else {
                                        System.out.print(content.get(i).get(j));
                                    }

                                    if (j == width - 1) {
                                        System.out.print(i == content.size() ? '+' : '|');
                                    }
                                });

                        System.out.println();
                    });

            System.out.println();
        }

        public void removeRock() {
            IntStream.range(0, content.size())
                    .forEach(i -> IntStream.range(0, width)
                            .forEach(j -> {
                                if (content.get(i).get(j) == '@') {
                                    content.get(i).set(j, ' ');
                                }
                            }));
        }

        public boolean applyRock(final Character character, final boolean fall) {
            int width = currentRock.getContent().get(0).size();

            boolean invalidMove = rockPosition.getT2() == 0 || IntStream.range(0, currentRock.getContent().size())
                    .anyMatch(i -> IntStream.range(0, width)
                            .anyMatch(j -> currentRock.getContent().get(i).get(j) == '#' &&
                                    content.get(content.size() - rockPosition.getT2() + i).get(rockPosition.getT1() + j) == '#'));

            final Character useCharacter = invalidMove && fall ? '#' : character;

            if (invalidMove) {
                if (fall) {
                    rockPosition = previousPosition;
                }
                else {
                    rockPosition = Tuples.of(previousPosition.getT1(), rockPosition.getT2());
                }
            }

            IntStream.range(0, currentRock.getContent().size())
                    .forEach(i -> IntStream.range(0, width)
                            .forEach(j -> {
                                Character ch = currentRock.getContent().get(i).get(j);

                                if (ch == '#') {
                                    ch = useCharacter;

                                    content.get(content.size() - rockPosition.getT2() + i).set(rockPosition.getT1() + j, ch);
                                }
                            }));

            if (useCharacter.equals('#')) {
                highestPosition = (int) content.stream().filter(row -> row.contains('#')).count();
                currentRock = null;
                rockCounter.incrementAndGet();
            }

            return invalidMove;
        }

        public Integer getHighestPosition() {
            return highestPosition;
        }

        public Integer getRockCount() {
            return rockCounter.get();
        }

        public Integer getCycleCount() {
            return cycleCounter.get();
        }
    }

    static class Minus extends Rock {
        public Minus() {
            content.add(Stream.of('#','#','#','#').collect(Collectors.toList()));
        }
    }

    static class Plus extends Rock {
        public Plus() {
            content.addAll(Stream.of(
                    Stream.of(' ', '#', ' ').collect(Collectors.toList()),
                    Stream.of('#', '#', '#').collect(Collectors.toList()),
                    Stream.of(' ', '#', ' ').collect(Collectors.toList())).toList());
        }
    }

    static class RockL extends Rock {
        public RockL() {
            content.addAll(Stream.of(
                    Stream.of(' ', ' ', '#').collect(Collectors.toList()),
                    Stream.of(' ', ' ', '#').collect(Collectors.toList()),
                    Stream.of('#', '#', '#').collect(Collectors.toList())).toList());
        }
    }

    static class RockI extends Rock {
        public RockI() {
            content.addAll(Stream.of('#', '#', '#', '#')
                    .flatMap(v -> Stream.of(Stream.of(v).collect(Collectors.toList()))).toList());
        }
    }

    static class Block extends Rock {
        public Block() {
            content.addAll(Stream.of('#', '#')
                    .flatMap(v -> Stream.of(Stream.of(v, v).collect(Collectors.toList()))).toList());
        }
    }

    static class Rock {
        protected final List<List<Character>> content = new ArrayList<>();

        public List<List<Character>> getContent() {
            return content;
        }
    }

    enum Direction {
        RIGHT('>'), LEFT('<');

        private final Character character;

        Direction(final Character character) {
            this.character = character;
        }

        static Direction getByCharacter(final Character character) {
            return Arrays.stream(Direction.values()).filter(direction -> direction.character.equals(character)).findFirst().orElseThrow();
        }
    }
}
