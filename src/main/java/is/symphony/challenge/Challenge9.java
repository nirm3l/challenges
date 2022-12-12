package is.symphony.challenge;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Challenge9 {

    public static void main(final String[] args) throws IOException {
        System.out.println(
                new Challenge9().execute("src/main/resources/input9.txt", 1));

        System.out.println(
                new Challenge9().execute("src/main/resources/input9.txt", 9));
    }

    private Integer execute(final String filePath, int historySize) throws IOException {
        try (final Stream<String> lines = (Files.lines(Paths.get(filePath)))) {
            final var result = lines.map(line -> {
                final String[] parts = line.split(" ");

                return Tuples.of(Direction.valueOf(parts[0]), Integer.parseInt(parts[1]));
            }).reduce(initHeadAndTails(historySize), this::playMove, (l1, l2) -> l1);

            return new HashSet<>(result.getLast()).size();
        }
    }

    private LinkedList<LinkedList<Tuple2<Integer, Integer>>> initHeadAndTails(int historySize) {
        return IntStream.range(0, historySize + 1)
                .mapToObj(i -> new LinkedList<>(List.of(Tuples.of(0, 0))))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private LinkedList<LinkedList<Tuple2<Integer, Integer>>> playMove(
            final LinkedList<LinkedList<Tuple2<Integer, Integer>>> moveCollections, final Tuple2<Direction, Integer> move) {
        for (int i = 0; i < move.getT2(); i++) {
            final LinkedList<Tuple2<Integer, Integer>> currentHead = moveCollections.getFirst();

            Tuple2<Integer, Integer> newPosition = applyDelta(currentHead.getLast(), move.getT1().getDelta());

            currentHead.add(newPosition);

            for (int j = 1; j < moveCollections.size(); j++) {
                final LinkedList<Tuple2<Integer, Integer>> currentTail = moveCollections.get(j);

                final Tuple2<Integer, Integer> lastTailMove = currentTail.getLast();

                int deltaX = newPosition.getT1() - lastTailMove.getT1();
                int deltaY = newPosition.getT2() - lastTailMove.getT2();

                if (Math.abs(deltaX) < 2 && Math.abs(deltaY) < 2) {
                    break;
                }

                if (Math.abs(deltaX) == 2) {
                    deltaX = deltaX / 2;
                }

                if (Math.abs(deltaY) == 2) {
                    deltaY = deltaY / 2;
                }

                newPosition = applyDelta(lastTailMove, Tuples.of(deltaX, deltaY));

                currentTail.add(newPosition);
            }
        }

        return moveCollections;
    }

    Tuple2<Integer, Integer> applyDelta(final Tuple2<Integer, Integer> position, final Tuple2<Integer, Integer> delta) {
        return Tuples.of(position.getT1() + delta.getT1(), position.getT2() + delta.getT2());
    }

    enum Direction {
        R(Tuples.of(0, 1)),
        L(Tuples.of(0, -1)),
        U(Tuples.of(1, 0)),
        D(Tuples.of(-1, 0));
        private final Tuple2<Integer, Integer> delta;

        Direction(final Tuple2<Integer, Integer> delta) {
            this.delta = delta;
        }

        public Tuple2<Integer, Integer> getDelta() {
            return delta;
        }
    }
}
