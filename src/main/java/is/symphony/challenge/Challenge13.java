package is.symphony.challenge;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Challenge13 {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(final String[] args) throws IOException {
        System.out.println(new Challenge13().getSumOfIndices("src/main/resources/input13.txt"));
        System.out.println(new Challenge13().getDecoderKey("src/main/resources/input13.txt"));
    }

    private Stream<List<List>> getGroupedPairs(final Stream<String> lines) {
        return Flux.fromStream(getPairs(lines))
                .window(2)
                .flatMap(Flux::collectList).toStream();
    }

    private Stream<List> getPairs(final Stream<String> lines) {
        return lines
                .filter(line -> !line.isEmpty())
                .map(line -> {
                    try {
                        return objectMapper.readValue(line, List.class);
                    } catch (JsonProcessingException e) {
                        throw new IllegalStateException("Invalid input file");
                    }
                });
    }

    private Integer getDecoderKey(final String filePath) throws IOException {
        final List<List<List<Integer>>> dividers = List.of(List.of(List.of(2)), List.of(List.of(6)));

        try (final Stream<String> lines = (Files.lines(Paths.get(filePath)))) {
            return Stream.concat(getPairs(lines), dividers.stream())
                    .sorted(Comparator.comparing((obj) -> obj, (o1, o2) -> {
                        final Boolean check = checkOrder(o1, o2);

                        if (check == null) {
                            return 0;
                        }

                        return check ? -1 : 1;
                    }))
                    .reduce(Tuples.of(1, new AtomicInteger(0)), (result, pair) -> {
                        result.getT2().incrementAndGet();

                        if (dividers.contains(pair)) {
                            return Tuples.of(result.getT1() * result.getT2().get(), result.getT2());
                        }

                        return Tuples.of(result.getT1(), result.getT2());
                    }, (l1, l2) -> l1).getT1();
        }
    }

    private Integer getSumOfIndices(final String filePath) throws IOException {
        try (final Stream<String> lines = (Files.lines(Paths.get(filePath)))) {
            return getGroupedPairs(lines)
                    .reduce(Tuples.of(0, new AtomicInteger(0)), (result, pairs) -> {
                        final Boolean checkResult = checkOrder(pairs.get(0), pairs.get(1));

                        result.getT2().incrementAndGet();

                        if (checkResult == null || checkResult) {
                            return Tuples.of(result.getT1() + result.getT2().get(), result.getT2());
                        }

                        return Tuples.of(result.getT1(), result.getT2());
                    }, (l1, l2) -> l1).getT1();
        }
    }

    private Boolean checkOrder(final List list1, final List list2) {
        return IntStream.range(0, list1.size()).boxed()
                .flatMap(i -> {
                    Object value1 = list1.get(i);

                    if (list2.size() <= i) {
                        return Stream.of(false);
                    }

                    Object value2 = list2.get(i);

                    if (!value1.getClass().equals(value2.getClass())) {
                        if (value1 instanceof Integer) {
                            value1 = List.of(value1);
                        }
                        else if (value2 instanceof Integer) {
                            value2 = List.of(value2);
                        }
                    }

                    if (!value1.getClass().equals(Integer.class) && !value2.getClass().equals(Integer.class)) {
                        final Boolean result = checkOrder((List) value1, (List) value2);

                        if (result != null) {
                            return Stream.of(result);
                        }

                        return Stream.empty();
                    }

                    if ((int) value1 == (int) value2) {
                        return Stream.empty();
                    }

                    return Stream.of((int) value1 < (int) value2);
                }).findFirst().orElse(list1.size() < list2.size() ? true : null);
    }
}
