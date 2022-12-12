package com.example.challenge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

/*
 */
public class Challenge2 {

    public static void main(final String[] args) throws IOException {
        System.out.println(new Challenge2().execute("src/main/resources/input2.txt"));
    }

    public int execute(final String filePath) throws IOException {
        try (final Stream<String> lines = (Files.lines(Paths.get(filePath)))) {
            return -1;
        }
    }
}
