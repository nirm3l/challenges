package is.symphony.challenge;


import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Challenge16 {
    private final Pattern PATTERN = Pattern.compile("Valve (.*) has .*=(\\d+).* to valves? (.*)", Pattern.MULTILINE);

    private static final Integer INFINITE = Integer.MAX_VALUE / 2;

    public static void main(final String[] args) throws IOException {
        System.out.println(new Challenge16().execute("src/main/resources/input16.txt", 30, false));
        System.out.println(new Challenge16().execute("src/main/resources/input16.txt", 26, true));
    }

    private long execute(final String filePath, final Integer minutesLimit, final boolean elephant) throws IOException {
        final Graph graph = new Graph();

        try (final Stream<String> lines = (Files.lines(Paths.get(filePath)))) {
            lines.flatMap(value -> getNodeWithReferences(value).stream())
                    .peek(nodeWithRefs -> graph.addNode(nodeWithRefs.getT1()))
                    .collect(Collectors.toSet())
                    .forEach(nodeRefs -> {
                        for (final String ref : nodeRefs.getT2()) {
                            nodeRefs.getT1().addReference(graph.getNode(ref));
                        }
                    });

            graph.calculateDistances();

            final Node rootNode = graph.getNode("AA");

            return elephant ?
                    getMaxFlowWithElephant(graph, new LinkedList<>(List.of(graph.getNode("AA"))), minutesLimit)
                    : getMaxFlow(graph, () -> Stream.of(rootNode), rootNode, minutesLimit);
        }
    }

    private Integer getMaxFlow(final Graph graph, final Supplier<Stream<Node>> nodes, Node lastNode, final Integer minutes) {
        return graph.getFlowNodes().stream()
                .filter(node -> nodes.get().noneMatch(n -> n.equals(node)))
                .flatMap(node -> {
            int rem = minutes - (1 + graph.getDistances().get(lastNode).get(node));

            if (rem >= 0) {
                return Stream.of(rem * node.getFlow() + getMaxFlow(graph, () -> Stream.concat(nodes.get(), Stream.of(node)), node, rem));
            }

            return Stream.empty();
        }).mapToInt(i -> i).max().orElse(0);
    }

    private List<List<Node>> dfs(final Graph graph, final LinkedList<Node> nodes, final Integer minutes, final List<List<Node>> paths) {
        paths.add(nodes);

        graph.getFlowNodes().stream().filter(node -> !nodes.contains(node))
                .forEach(node -> {
                    int rem = minutes - (1 + graph.getDistances().get(nodes.getLast()).get(node));

                    if (rem >= 0) {
                        dfs(graph, Stream.concat(nodes.stream(), Stream.of(node))
                                .collect(Collectors.toCollection(LinkedList::new)), rem, paths);
                    }
                });

        return paths;
    }

    private List<List<Node>> dfs(final Graph graph, final LinkedList<Node> nodes, final Integer minutes) {
        return dfs(graph, nodes, minutes, new ArrayList<>());
    }

    private Integer getMaxFlowWithElephant(final Graph graph, final LinkedList<Node> nodes, final Integer minutes) {
        final List<List<Node>> paths = dfs(graph, nodes, minutes);

        final List<Tuple2<Integer, List<Node>>> map = paths.stream().map(path -> {
            final AtomicReference<Node> currentRef = new AtomicReference<>(path.get(0));

            final AtomicInteger timeCounter = new AtomicInteger(minutes);
            final AtomicInteger counter = new AtomicInteger();

            path.stream().skip(1).forEach(node -> {
                timeCounter.addAndGet( -graph.getDistances().get(currentRef.get()).get(node) - 1);
                counter.addAndGet(timeCounter.get() * node.getFlow());
                currentRef.set(node);
            });

            return Tuples.of(counter.get(), path.stream().skip(1).collect(Collectors.toList()));
        }).sorted(Comparator.comparing((Tuple2<Integer, List<Node>> o) -> o.getT1()).reversed()).toList();

        return IntStream.range(0, map.size() - 1)
                .map(i -> {
                    int elephant = IntStream.range(i + 1, paths.size()).boxed()
                            .filter(j -> map.get(i).getT2().stream().noneMatch(map.get(j).getT2()::contains))
                            .map(j -> map.get(j).getT1()).findFirst().orElse(0);

                    return map.get(i).getT1() + elephant;
                }).max().orElseThrow();
    }

    private Optional<Tuple2<Node, Set<String>>> getNodeWithReferences(final String line) {
        return Stream.iterate(PATTERN.matcher(line), Matcher::find, m -> m)
                .map(matcher -> {
                    final Node node = new Node(matcher.group(1), Integer.parseInt(matcher.group(2)));

                    return Tuples.of(node, Arrays.stream(matcher.group(3).split(", ")).collect(Collectors.toSet()));
                }).findFirst();
    }

    static class Graph {
        private final Map<String, Node> nodes = new LinkedHashMap<>();

        private final Set<Node> flowNodes = new HashSet<>();

        private final Map<Node, Map<Node, Integer>> distances = new HashMap<>();

        public void addNode(final Node node) {
            nodes.put(node.getName(), node);

            if(node.getFlow() > 0) {
                flowNodes.add(node);
            }
        }

        public void calculateDistances() {
            for (final Node node : nodes.values()) {
                if (!distances.containsKey(node)) {
                    distances.put(node, new HashMap<>());
                }

                distances.get(node).put(node, 0);

                for (final Node child : node.getReferences()) {
                    distances.get(node).put(child, 1);
                }
            }

            nodes.values().forEach(
                    k -> nodes.values().forEach(
                            i -> nodes.values().forEach(
                                    j -> distances.get(i)
                                            .put(j, Math.min(
                                                    distances.get(i).getOrDefault(j, INFINITE),
                                                    distances.get(i).getOrDefault(k, INFINITE) +
                                                            distances.get(k).getOrDefault(j, INFINITE))))));
        }

        public Set<Node> getFlowNodes() {
            return flowNodes;
        }

        public Node getNode(final String name) {
            return nodes.get(name);
        }

        public Map<Node, Map<Node, Integer>> getDistances() {
            return distances;
        }
    }

    static class Node {
        private final String name;

        private final int flow;

        private final List<Node> references = new ArrayList<>();

        public Node(final String name, final int flow) {
            this.name = name;
            this.flow = flow;
        }

        public String getName() {
            return name;
        }

        public int getFlow() {
            return flow;
        }

        public void addReference(final Node node) {
            references.add(node);
        }

        public List<Node> getReferences() {
            return references;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final Node node = (Node) o;
            return name.equals(node.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }
}
