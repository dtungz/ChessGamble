package com.chessgamblegame.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PositionTest {
    @Test void parseE1()
    {
        assertEquals(new Position(4, 0), Position.parse("e1"));
    }
    @Test void toNotationH8()
    {
        assertEquals("h8", new Position(7, 7).toNotation());
    }
    @Test void outOfBoard()
    {
        assertFalse(new Position(8, 0).inBounds());
    }
}
