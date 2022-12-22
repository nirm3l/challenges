package is.symphony.challenge;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Challenge22 {
    private Direction direction = Direction.RIGHT;

    private Tuple2<Integer, Integer> position;

    private List<List<Character>> map;

    private final List<Object> moves = new ArrayList<>();

    private Map<Tuple3<Integer, Integer, Direction>, Tuple3<Integer, Integer, Direction>> edgesMap;

    public static void main(final String[] args) throws IOException {
        System.out.println(new Challenge22().execute("src/main/resources/input22.txt", false));
        System.out.println(new Challenge22().execute("src/main/resources/input22.txt", true));
    }

    private long execute(final String filePath, final boolean cube) throws IOException {
        try (final Stream<String> lines = (Files.lines(Paths.get(filePath)))) {
            map = lines.filter(line -> {
                        if (line.contains("L") || line.contains("R")) {
                            moves.addAll(getMoves(line));

                            return false;
                        }

                        return !line.isBlank();
                    })
                    .map(line -> line.chars().mapToObj(i -> (char) i)
                            .collect(Collectors.toList())).collect(Collectors.toList());

            position = getInitialPosition();
            edgesMap = createEdgeMapping();

            moves.forEach(move -> move(move, cube));

            return getPassword();
        }
    }

    // -1, 6 ->

    private Map<Tuple3<Integer, Integer, Direction>, Tuple3<Integer, Integer, Direction>> createEdgeMapping() {
        final Map<Tuple3<Integer, Integer, Direction>, Tuple3<Integer, Integer, Direction>> edgesMap = new HashMap<>();

        int tileSize = map.size() / 4;

        IntStream.range(0, tileSize)
                .forEach(i -> {
                    edgesMap.put(Tuples.of(-1, tileSize + i, Direction.UP), Tuples.of(tileSize * 3 + i, 0, Direction.RIGHT));
                    edgesMap.put(Tuples.of(tileSize * 3 + i, -1, Direction.LEFT), Tuples.of(0, tileSize + i, Direction.DOWN));

                    edgesMap.put(Tuples.of(-1, 2 * tileSize + i, Direction.UP), Tuples.of(tileSize * 4 - 1, i, Direction.UP));
                    edgesMap.put(Tuples.of(4 * tileSize, i, Direction.DOWN), Tuples.of(0, 2 * tileSize + i, Direction.DOWN));

                    edgesMap.put(Tuples.of(2 * tileSize - 1, i, Direction.UP), Tuples.of(tileSize + i, tileSize, Direction.RIGHT));
                    edgesMap.put(Tuples.of(tileSize + i, tileSize - 1, Direction.LEFT), Tuples.of(2 * tileSize, i, Direction.DOWN));

                    edgesMap.put(Tuples.of(i, tileSize - 1, Direction.LEFT), Tuples.of(3 * tileSize - i - 1, 0, Direction.RIGHT));
                    edgesMap.put(Tuples.of(3 * tileSize - i - 1, -1, Direction.LEFT), Tuples.of(i, tileSize, Direction.RIGHT));

                    edgesMap.put(Tuples.of(tileSize, tileSize * 2 + i, Direction.DOWN), Tuples.of(tileSize + i, 2 * tileSize - 1, Direction.LEFT));
                    edgesMap.put(Tuples.of(tileSize + i, tileSize * 2, Direction.RIGHT), Tuples.of(tileSize - 1, 2 * tileSize + i, Direction.UP));

                    edgesMap.put(Tuples.of(i, tileSize * 3, Direction.RIGHT), Tuples.of(tileSize * 3 - i - 1, 2 * tileSize - 1, Direction.LEFT));
                    edgesMap.put(Tuples.of(2 * tileSize + i, tileSize * 2, Direction.RIGHT), Tuples.of(tileSize - i - 1, 3 * tileSize - 1, Direction.LEFT));

                    edgesMap.put(Tuples.of(3 * tileSize, tileSize + i, Direction.DOWN), Tuples.of(3 * tileSize + i, tileSize - 1, Direction.LEFT));
                    edgesMap.put(Tuples.of(3 * tileSize + i, tileSize, Direction.RIGHT), Tuples.of(3 * tileSize - 1, tileSize + i, Direction.UP));
                });

        return edgesMap;
    }

    private long getPassword() {
        return 1000L * (position.getT1() + 1) + 4L * (position.getT2() + 1) + direction.getFacing();
    }

    private void move(final Object move, final boolean cube) {
        if (move instanceof Integer) {
            IntStream.range(0, (int)move).boxed()
                    .flatMap(i -> {
                        Tuple2<Integer, Integer> newPosition = move(direction, !cube);

                        map.get(position.getT1()).set(position.getT2(), direction.getName());

                        Direction newDirection = null;

                        if (cube) {
                            final Tuple3<Integer, Integer, Direction> triple = Tuples.of(newPosition.getT1(), newPosition.getT2(), direction);

                            if (edgesMap.containsKey(triple)) {
                                newPosition = edgesMap.get(triple);
                                newDirection = edgesMap.get(triple).getT3();
                            }
                        }
                        else {
                            if (getCharacter(newPosition).equals(' ')) {
                                newPosition = switch (direction) {
                                    case RIGHT -> Tuples.of(newPosition.getT1(), getFirstNonEmptyInRow(position.getT1()));
                                    case DOWN -> Tuples.of(getFirstNonEmptyInColumn(position.getT2()), newPosition.getT2());
                                    case LEFT -> Tuples.of(newPosition.getT1(), getLastNonEmptyInRow(position.getT1()));
                                    case UP -> Tuples.of(getLastNonEmptyInColumn(position.getT2()), newPosition.getT2());
                                };
                            }
                        }

                        if (getCharacter(newPosition) != '#') {
                            position = newPosition;

                            if (newDirection != null) {
                                direction = newDirection;
                            }

                            return Stream.empty();
                        }

                        return Stream.of(true);
                    }).findFirst().ifPresent(result -> {});
        }
        else if (move.equals('R')) {
            direction = direction.turnRight();
        }
        else if (move.equals('L')) {
            direction = direction.turnLeft();
        }

        map.get(position.getT1()).set(position.getT2(), direction.getName());
    }

    private Integer getFirstNonEmptyInRow(final Integer row) {
        return IntStream.iterate(0, i -> getCharacter(Tuples.of(row, i)) == ' ', i -> i + 1)
                .max().orElse(-1) + 1;
    }

    private Integer getLastNonEmptyInColumn(final Integer column) {
        return IntStream.iterate(map.size() - 1, i -> i > 0 && getCharacter(Tuples.of(i, column)) == ' ', i -> i - 1)
                .min().orElse(map.size()) - 1;
    }

    private Integer getLastNonEmptyInRow(final Integer row) {
        return map.get(row).size() - 1;
    }

    private Integer getFirstNonEmptyInColumn(final Integer column) {
        return IntStream.iterate(0, i -> getCharacter(Tuples.of(i, column)) == ' ', i -> i + 1)
                .max().orElse(-1) + 1;
    }

    private Tuple2<Integer, Integer> move(final Direction direction, final boolean correct) {
        final int width = map.get(position.getT1()).size();

        return switch (direction) {
            case LEFT -> Tuples.of(position.getT1(), !correct || position.getT2() - 1 >= 0 ? position.getT2() - 1 : width - 1);
            case RIGHT -> Tuples.of(position.getT1(), !correct || position.getT2() + 1 < width ? position.getT2() + 1 : 0);
            case UP -> Tuples.of(!correct || position.getT1() - 1 >= 0 ? position.getT1() - 1 : map.size() - 1, position.getT2());
            case DOWN -> Tuples.of(!correct || position.getT1() + 1 < map.size() ? position.getT1() + 1 : 0, position.getT2());
        };
    }

    private Tuple2<Integer, Integer> getInitialPosition() {
        return Tuples.of(0, map.get(0).indexOf('.'));
    }

    private Character getCharacter(final Tuple2<Integer, Integer> position) {
        final List<Character> row = map.get(position.getT1());

        return position.getT2() <= row.size() - 1 ? row.get(position.getT2()) : ' ';
    }

    private List<Object> getMoves(final String line) {
        return Arrays.stream(line
                        .replaceAll("R", " R ")
                        .replaceAll("L", " L ")
                        .split(" "))
                .map(s -> {
                    try {
                        return (Object) Integer.parseInt(s);
                    } catch (final NumberFormatException e) {
                        return s.charAt(0);
                    }
                }).toList();
    }

    enum Direction {
        UP('^', 3), RIGHT('>', 0), DOWN('v', 1), LEFT('<', 2);

        private final Character name;

        private final Integer facing;

        Direction(final Character name, final Integer facing) {
            this.name = name;
            this.facing = facing;
        }

        public Character getName() {
            return name;
        }

        public Integer getFacing() {
            return facing;
        }

        public Direction turnRight() {
            return switch (this) {
                case UP -> RIGHT;
                case RIGHT -> DOWN;
                case DOWN -> LEFT;
                case LEFT -> UP;
            };
        }

        public Direction turnLeft() {
            return switch (this) {
                case UP -> LEFT;
                case RIGHT -> UP;
                case DOWN -> RIGHT;
                case LEFT -> DOWN;
            };
        }
    }
}
