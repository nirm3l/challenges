package is.symphony.challenge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class Challenge7 {

    private static final Long TOTAL_SPACE = 70000000L;
    private static final Long NEEDED_SPACE = 30000000L;
    private static final Long FOLDER_THRESHOLD = 100000L;

    public static void main(final String[] args) throws IOException {
        final Challenge7 challenge = new Challenge7();

        final MyDirectory root = challenge.execute("src/main/resources/input7.txt");

        final List<MyDirectory> directories = challenge.getAllDirectoriesStream(root).sorted().toList();

        long magicSum = directories.stream()
                .filter(myContent -> myContent.getSize() <= FOLDER_THRESHOLD).mapToLong(MyContent::getSize).sum();

        final Long freeSpace = TOTAL_SPACE - root.getSize();

        final Long spaceToFree = NEEDED_SPACE - freeSpace;

        final MyDirectory magicDirectory = directories.stream().filter(dir -> dir.getSize() > spaceToFree).findFirst().orElse(null);

        assert magicDirectory != null;

        System.out.println(magicSum + " " + magicDirectory.getSize());
    }

    public MyDirectory execute(final String filePath) throws IOException {
        final Stack<MyDirectory> currentPath = new Stack<>();

        try (final Stream<String> lines = (Files.lines(Paths.get(filePath)))) {
            lines.forEach(line -> {
                if (line.equals("$ ls")) {
                    return; // Skip ls
                }

                if (line.equals("$ cd ..")) {
                    currentPath.pop();
                }
                else if (line.startsWith("$ cd ")) {
                    final String directory = line.replace("$ cd ", "");

                    currentPath.push(
                            currentPath.size() == 0 ? new MyDirectory("", "/") : currentPath.peek().getDirectory(directory));
                }
                else if (line.startsWith("dir ")) {
                    final String name = line.replace("dir ", "");
                    currentPath.peek().addContent(new MyDirectory(name, currentPath.peek().getPath() + name + "/"));
                }
                else {
                    final String[] parts = line.split(" ");
                    currentPath.peek().addContent(new MyFile(parts[1], Long.parseLong(parts[0])));
                }
            });

            return currentPath.firstElement();
        }
    }

    private Stream<MyDirectory> getAllDirectoriesStream(final MyDirectory root) {
        return Stream.concat(
                Stream.of(root),
                root.getContentStream().filter(myContent -> myContent instanceof MyDirectory)
                        .map(myContent -> (MyDirectory) myContent)
                        .flatMap(this::getAllDirectoriesStream));
    }

    interface MyContent {
        Long getSize();
    }

    static class MyDirectory implements MyContent, Comparable<MyDirectory> {
        private final List<MyContent> content = new ArrayList<>();

        private final String name;

        private final String path;

        public MyDirectory(final String name, final String path) {
            this.name = name;
            this.path = path;
        }

        public Stream<MyContent> getContentStream() {
            return content.stream();
        }

        public void addContent(final MyContent myContent) {
            content.add(myContent);
        }

        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }

        public MyDirectory getDirectory(final String name) {
            return (MyDirectory) content.stream()
                    .filter(value -> value instanceof MyDirectory && ((MyDirectory) value).getName().equals(name))
                    .findFirst().orElse(null);
        }

        public Long getSize() {
            return content.stream().map(MyContent::getSize).mapToLong(Long::longValue).sum();
        }

        @Override
        public int compareTo(final MyDirectory o) {
            return getSize().compareTo(o.getSize());
        }
    }

    record MyFile(String name, Long size) implements MyContent {
        @Override
        public Long getSize() {
            return size;
        }
    }
}
