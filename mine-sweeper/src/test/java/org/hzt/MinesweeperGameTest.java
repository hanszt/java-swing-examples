package org.hzt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MinesweeperGameTest {

    @Test
    void testGridAsString() {
        final var defaultProbability = -1;
        final var minesweeperGame = MinesweeperGame.start(4, defaultProbability, 0);
        final var boardAsString = minesweeperGame.boardAsString();

        final var expected = """
                0000
                0011
                222*
                **21"""
                .stripIndent();

        assertEquals(expected, boardAsString);
    }

}
