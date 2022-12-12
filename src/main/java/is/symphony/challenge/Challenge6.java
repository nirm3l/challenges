package is.symphony.challenge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*

The expedition can depart as soon as the final supplies have been unloaded from the ships. Supplies are stored in stacks of marked crates,
but because the needed supplies are buried under many other crates, the crates need to be rearranged.

The ship has a giant cargo crane capable of moving crates between stacks. To ensure none of the crates get crushed or fall over,
the crane operator will rearrange them in a series of carefully-planned steps. After the crates are rearranged, the desired crates
will be at the top of each stack.

The Elves don't want to interrupt the crane operator during this delicate procedure, but they forgot to ask her which crate will
end up where, and they want to be ready to unload them as soon as possible so they can embark.

They do, however, have a drawing of the starting stacks of crates and the rearrangement procedure (your puzzle input). For example:

    [D]
[N] [C]
[Z] [M] [P]
 1   2   3

move 1 from 2 to 1
move 3 from 1 to 3
move 2 from 2 to 1
move 1 from 1 to 2
In this example, there are three stacks of crates. Stack 1 contains two crates: crate Z is on the bottom, and crate N is on top.
Stack 2 contains three crates; from bottom to top, they are crates M, C, and D. Finally, stack 3 contains a single crate, P.

Then, the rearrangement procedure is given. In each step of the procedure, a quantity of crates is moved from one stack to a
different stack. In the first step of the above rearrangement procedure, one crate is moved from stack 2 to stack 1, resulting
in this configuration:

[D]
[N] [C]
[Z] [M] [P]
 1   2   3
In the second step, three crates are moved from stack 1 to stack 3. Crates are moved one at a time, so the first crate to be
moved (D) ends up below the second and third crates:

        [Z]
        [N]
    [C] [D]
    [M] [P]
 1   2   3
Then, both crates are moved from stack 2 to stack 1. Again, because crates are moved one at a time, crate C ends up below crate M:

        [Z]
        [N]
[M]     [D]
[C]     [P]
 1   2   3
Finally, one crate is moved from stack 1 to stack 2:

        [Z]
        [N]
        [D]
[C] [M] [P]
 1   2   3
The Elves just need to know which crate will end up on top of each stack; in this example, the top crates are C in stack 1,
M in stack 2, and Z in stack 3, so you should combine these together and give the Elves the message CMZ.

After the rearrangement procedure completes, what crate ends up on top of each stack?

Your puzzle answer was VWLCWGSDQ.

--- Part Two ---
As you watch the crane operator expertly rearrange the crates, you notice the process isn't following your prediction.

Some mud was covering the writing on the side of the crane, and you quickly wipe it away. The crane isn't a CrateMover 9000 -
it's a CrateMover 9001.

The CrateMover 9001 is notable for many new and exciting features: air conditioning, leather seats, an extra cup holder, and
the ability to pick up and move multiple crates at once.

Again considering the example above, the crates begin in the same configuration:

    [D]
[N] [C]
[Z] [M] [P]
 1   2   3
Moving a single crate from stack 2 to stack 1 behaves the same as before:

[D]
[N] [C]
[Z] [M] [P]
 1   2   3
However, the action of moving three crates from stack 1 to stack 3 means that those three moved crates stay in the same order,
resulting in this new configuration:

        [D]
        [N]
    [C] [Z]
    [M] [P]
 1   2   3
Next, as both crates are moved from stack 2 to stack 1, they retain their order as well:

        [D]
        [N]
[C]     [Z]
[M]     [P]
 1   2   3
Finally, a single crate is still moved from stack 1 to stack 2, but now it's crate C that gets moved:

        [D]
        [N]
        [Z]
[M] [C] [P]
 1   2   3
In this example, the CrateMover 9001 has put the crates in a totally different order: MCD.

Before the rearrangement process finishes, update your simulation so that the Elves know where they should stand to be ready
to unload the final supplies. After the rearrangement procedure completes, what crate ends up on top of each stack?

 */
public class Challenge5 {

    private final Pattern MOVE_PATTERN = Pattern.compile("move (\\d*) from (\\d*) to (\\d*)");
    private final Stack<String> tempStack = new Stack<>();

    public static void main(final String[] args) throws IOException {
        System.out.println(new Challenge5().execute(
                "src/main/resources/input5.txt", false));
    }

    public String execute(final String filePath, boolean multiple) throws IOException {
        try (final Stream<String> lines = (Files.lines(Paths.get(filePath)))) {
            return lines.reduce(new ArrayList<Stack<String>>(), (matrix, value) -> {
                if (value.contains("[")) {
                    value += " ";

                    final int size = initializeMatrix(matrix, value.length());

                    for (int i = 0; i < size; i++) {
                        final int position = i * 4;
                        final String crate = cleanCrate(value.substring(position, position + 4));

                        if (!crate.isEmpty()) {
                            matrix.get(i).add(0, crate);
                        }
                    }
                }
                else {
                    handleMove(matrix, value, multiple);
                }

                return matrix;
            }, ((stacks, stacks2) -> stacks)).stream().map(Stack::peek).collect(Collectors.joining());
        }
    }

    private String cleanCrate(final String crate) {
        return crate.replace("[", "").replace("]", "").strip();
    }

    private int initializeMatrix(final List<Stack<String>> matrix, final int length) {
        if (matrix.size() == 0) {
            final int size = length / 4;

            for (int i = 0; i < size; i++) {
                matrix.add(new Stack<>());
            }
        }

        return matrix.size();
    }

    private void handleMove(final List<Stack<String>> matrix, final String line, boolean multiple) {
        final Matcher matcher = MOVE_PATTERN.matcher(line);

        while (matcher.find()) {
            final int count = Integer.parseInt(matcher.group(1));
            final int from = Integer.parseInt(matcher.group(2));
            final int to = Integer.parseInt(matcher.group(3));

            for (int i = 0; i < count; i++) {
                final String crate = matrix.get(from - 1).pop();

                if (!multiple) {
                    matrix.get(to - 1).push(crate);
                }
                else {
                    tempStack.add(0, crate);
                }
            }

            if (multiple) {
                matrix.get(to - 1).addAll(tempStack);
                tempStack.clear();
            }
        }
    }
}
