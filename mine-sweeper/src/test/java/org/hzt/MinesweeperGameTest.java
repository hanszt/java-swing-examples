package org.hzt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MinesweeperGameTest {

    @Test
    void testGridAsString() {
        final var defaultProbability = -1;
        final var minesweeperGame = new MinesweeperGame(4, defaultProbability, 0);
        final var boardAsString = minesweeperGame.boardAsString();

        System.out.println(boardAsString);

        String expected = String.format("0000%n0011%n222*%n**21");

        assertEquals(expected, boardAsString);
    }

}
