package is.symphony.challenge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Challenge19 {

    public static void main(final String[] args) throws IOException {
        long start = System.currentTimeMillis();

        System.out.println(new Challenge19().execute("src/main/resources/input19.txt", 24, false));
        System.out.println("Run first part in: " + (System.currentTimeMillis() - start) / 1000 + " seconds.");

        start = System.currentTimeMillis();
        System.out.println(new Challenge19().execute("src/main/resources/input19.txt", 32, true));
        System.out.println("Run second part in: " + (System.currentTimeMillis() - start) / 1000 + " seconds.");
    }

    private long execute(final String filePath, final Integer minutes, final boolean topThree) throws IOException {
        try (final Stream<String> lines = (Files.lines(Paths.get(filePath)))) {
            final List<Blueprint> blueprints = lines.map(this::getBlueprint).toList();

            if (topThree) {
                return blueprints.stream().limit(3).parallel()
                        .map(blueprint -> getMaxGeodes(blueprint, minutes)).mapToInt(i -> i).reduce(1, (p, v) -> p * v);
            }
            else {
                return blueprints.parallelStream()
                        .map(blueprint -> {
                            int max = getMaxGeodes(blueprint, minutes);

                            return blueprint.getId() * max;
                        }).mapToInt(i -> i).sum();
            }
        }
    }

    public Stream<Collector> streamCollectors(
            final Collector current, final Blueprint blueprint, final Integer minutes, final Set<Collector> duplicatesCheck) {
        if (minutes.equals(current.getMinute())) {
            return Stream.of(current);
        }

        current.optimise(minutes - current.getMinute());

        if (duplicatesCheck.contains(current)) {
            return Stream.of(current);
        }

        duplicatesCheck.add(current);

        return Stream.concat(streamCollectors(current.clone().collect(), blueprint, minutes, duplicatesCheck),
                blueprint.getRobots().stream().filter(current::hasEnoughMoney)
                        .flatMap(robot -> streamCollectors(current.clone().collect().buyRobot(robot.getCurrency()), blueprint, minutes, duplicatesCheck)));
    }

    public Integer getMaxGeodes(final Blueprint blueprint, final Integer minutes) {
        final Set<Collector> duplicatesCheck = new HashSet<>();

        return streamCollectors(new Collector(blueprint), blueprint, minutes, duplicatesCheck)
                .mapToInt(c -> c.getCosts().get(Currency.GEODE)).max().orElseThrow();
    }

    static class Collector implements Cloneable {
        private Map<Robot, Integer> robotCounters = new HashMap<>();

        private Map<Currency, Integer> costs = new HashMap<>();

        private Integer minute;

        private final Blueprint blueprint;

        public Collector(final Blueprint blueprint) {
            this.blueprint = blueprint;
            minute = 0;

            Arrays.stream(Currency.values()).forEach(currency -> {
                costs.put(currency, 0);
                robotCounters.put(blueprint.getRobot(currency), 0);
            });

            robotCounters.put(blueprint.getRobot(Currency.ORE), 1);

        }

        private Collector collect() {
            robotCounters.forEach((robot, count) -> costs.put(robot.getCurrency(), costs.get(robot.getCurrency()) + count));

            minute += 1;

            return this;
        }

        private Collector buyRobot(final Currency currency) {
            final Robot robot = blueprint.getRobot(currency);
            robotCounters.put(robot, robotCounters.get(robot) + 1);

            robot.getCost().forEach((curr, amount) -> costs.put(curr, costs.get(curr) - amount));

            return this;
        }

        private void optimise(int minutesLeft) {
            int maxOreCost = blueprint.getRobots().stream().map(robot -> robot.getCost()
                    .getOrDefault(Currency.ORE, 0)).mapToInt(i -> i).max().orElseThrow();

            final Robot oreRobot = blueprint.getRobot(Currency.ORE);
            final Robot clayRobot = blueprint.getRobot(Currency.CLAY);
            final Robot obsidianRobot = blueprint.getRobot(Currency.OBSIDIAN);
            final Robot geodeRobot = blueprint.getRobot(Currency.GEODE);

            if (robotCounters.get(oreRobot) >= maxOreCost) {
                robotCounters.put(oreRobot, maxOreCost);
            }

            if (robotCounters.get(clayRobot) >= obsidianRobot.getCost().get(Currency.CLAY)) {
                robotCounters.put(clayRobot, obsidianRobot.getCost().get(Currency.CLAY));
            }

            if (robotCounters.get(obsidianRobot) >= geodeRobot.getCost().get(Currency.OBSIDIAN)) {
                robotCounters.put(obsidianRobot, geodeRobot.getCost().get(Currency.OBSIDIAN));
            }

            int newOre = minutesLeft * maxOreCost - robotCounters.get(oreRobot) * (minutesLeft - 1);

            if (costs.get(Currency.ORE) > newOre) {
                costs.put(Currency.ORE, newOre);
            }

            int newClay = minutesLeft * obsidianRobot.getCost().get(Currency.CLAY) - robotCounters.get(clayRobot) * (minutesLeft - 1);

            if (costs.get(Currency.CLAY) > newClay) {
                costs.put(Currency.CLAY, newClay);
            }

            int newObsidian = minutesLeft * geodeRobot.getCost().get(Currency.OBSIDIAN) - robotCounters.get(obsidianRobot) * (minutesLeft - 1);

            if (costs.get(Currency.OBSIDIAN) > newObsidian) {
                costs.put(Currency.OBSIDIAN, newObsidian);
            }
        }

        private boolean hasEnoughMoney(final Robot robot) {
            return robot.getCost().entrySet().stream()
                    .allMatch(entry -> costs.getOrDefault(entry.getKey(), 0) >= entry.getValue());
        }

        public Map<Currency, Integer> getCosts() {
            return costs;
        }

        public Integer getMinute() {
            return minute;
        }

        @Override
        public Collector clone() {
            final Collector clone = new Collector(blueprint);
            clone.costs = new HashMap<>(costs);
            clone.robotCounters = new HashMap<>(robotCounters);
            clone.minute = minute;

            return clone;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final Collector collector = (Collector) o;
            return Objects.equals(robotCounters, collector.robotCounters) && Objects.equals(costs, collector.costs) &&
                    Objects.equals(minute, collector.minute) && Objects.equals(blueprint.getId(), collector.blueprint.getId());
        }

        @Override
        public int hashCode() {
            return Objects.hash(robotCounters, costs, minute, blueprint.getId());
        }

        @Override
        public String toString() {
            return "Collector{" +
                    "robots=" + robotCounters +
                    ", money=" + costs +
                    ", minute=" + minute +
                    '}';
        }
    }

    private Blueprint getBlueprint(final String line) {
        final String cleaned = line.replaceAll("\\.", "").replaceAll(":", "");

        final String[] parts = cleaned.split("Each ");

        final Blueprint blueprint = new Blueprint();

        final Map<Currency, Robot> robots = Arrays.stream(parts).flatMap(part -> {
            String[] words = part.split(" ");

            if (words[0].equals("Blueprint")) {
                blueprint.setId(Integer.parseInt(words[1]));
                return Stream.empty();
            }

            final Robot robot = new Robot(Currency.getByName(words[0]));

            String previousWord = null;

            for (final String word : words) {
                final Currency currency = Currency.getByName(word);

                if (previousWord != null && currency != null) {
                    robot.addCost(currency, Integer.parseInt(previousWord));
                }

                previousWord = word;
            }

            return Stream.of(robot);
        }).collect(Collectors.toMap(Robot::getCurrency, Function.identity()));

        blueprint.setRobots(robots);

        return blueprint;
    }

    static class Blueprint {
        private Integer id;
        private Map<Currency, Robot> robots;

        public void setRobots(final Map<Currency, Robot> robots) {
            this.robots = robots;
        }

        public void setId(final Integer id) {
            this.id = id;
        }

        public Integer getId() {
            return id;
        }

        public Collection<Robot> getRobots() {
            return robots.values();
        }

        public Robot getRobot(final Currency currency) {
            return robots.get(currency);
        }

        @Override
        public String toString() {
            return "Blueprint{" +
                    "id=" + id +
                    ", robots=" + robots +
                    '}';
        }
    }

    static class Robot {
        private final Currency currency;
        private final Map<Currency, Integer> cost = new HashMap<>();

        Robot(final Currency currency) {
            this.currency = currency;
        }

        public void addCost(final Currency currency, final Integer amount) {
            cost.put(currency, amount);
        }

        public Currency getCurrency() {
            return currency;
        }

        public Map<Currency, Integer> getCost() {
            return cost;
        }

        @Override
        public String toString() {
            return "Robot{" +
                    "currency=" + currency +
                    ", cost=" + cost +
                    '}';
        }
    }

    enum Currency {
        ORE("ore"), CLAY("clay"), OBSIDIAN("obsidian"), GEODE("geode");

        private final String name;

        Currency(final String name) {
            this.name = name;
        }

        String getName() {
            return name;
        }

        static Currency getByName(final String name) {
            return Arrays.stream(Currency.values()).filter(currency -> currency.getName().equals(name)).findFirst().orElse(null);
        }

        @Override
        public String toString() {
            return "Currency{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }
}
