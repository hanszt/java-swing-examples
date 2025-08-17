package org.hzt.ant;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.assertThat;

class BoardTest {

    @Test
    void randomizeTest() {
        final var board = new Board(new Random(0));
        board.randomize();

        final var nodes = board.getNodes();

        final var results = Arrays.stream(nodes).collect(
                teeing(
                        filtering(n -> "food".equals(n.getOccupant()), toList()),
                        filtering(n -> "water".equals(n.getOccupant()), toList()),
                        List::of
                )
        );

        assertThat(nodes).hasSize(900);
        assertThat(results.getFirst()).hasSize(112);
        assertThat(results.getLast()).hasSize(118);
    }

}