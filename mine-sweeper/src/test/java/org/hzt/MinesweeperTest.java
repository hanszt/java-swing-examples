package org.hzt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MinesweeperTest {

    @Test
    void testParameterNumTilesCorrect() {
        final var arguments = Minesweeper.parseArguments("-numTiles", "6");

        assertEquals(6, arguments.numberOfTiles());
    }

    @Test
    void testParameterAll() {
        final var arguments = Minesweeper.parseArguments("-numTiles", "6", "-seed", "3", "-mineProb", "3.2");

        assertEquals(new Minesweeper.Arguments(6, 3.2, 3), arguments);
    }

    @Test
    void testNonValidParams() {
        final var arguments = Minesweeper.parseArguments("Not", "valid");

        assertEquals(new Minesweeper.Arguments(0, -1, -1), arguments);
    }

}
