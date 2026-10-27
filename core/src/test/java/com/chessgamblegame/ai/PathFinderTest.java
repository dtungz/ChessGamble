package com.chessgamblegame.ai;

import com.chessgamblegame.model.Board;
import com.chessgamblegame.model.MoveGenerator;
import com.chessgamblegame.model.Piece;
import com.chessgamblegame.model.Position;
import com.chessgamblegame.model.enums.PieceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PathFinderTest
{
    private Board board;

    @BeforeEach
    void setUp()
    {
        board = new Board();
    }

    private Piece addEnemy(PieceType type, String square)
    {
        Piece e = new Piece(type, Position.parse(square));
        board.addEnemy(e);
        return e;
    }

    private void playerAt(String square)
    {
        board.playerPos = Position.parse(square);
    }

    private static Position at(String square)
    {
        return Position.parse(square);
    }

    // Mỗi bước trên đường đi phải là 1 nước đi hợp lệ từ ô trước đó, và kết thúc ở ô người chơi.
    private void assertValidPath(Piece enemy, List<Position> path)
    {
        assertNotNull(path);
        assertFalse(path.isEmpty());
        Position prev = enemy.pos;
        for (Position step : path)
        {
            assertTrue(MoveGenerator.enemyMoves(board, enemy.type, prev).contains(step),
                "Bước " + prev.toNotation() + " -> " + step.toNotation() + " không hợp lệ");
            prev = step;
        }
        assertEquals(board.playerPos, path.get(path.size() - 1));
    }

    // Tấn công vua
    @Test
    void rookOnSameColumnAttacksImmediately()
    {
        playerAt("a1");
        Piece rook = addEnemy(PieceType.Rook, "a8");

        assertEquals(at("a1"), PathFinder.nextStep(board, rook));
        assertEquals(1, PathFinder.shortestPath(board, rook).size());
    }

    @Test
    void pawnDiagonallyAbovePlayerAttacksImmediately()
    {
        playerAt("e3");
        Piece pawn = addEnemy(PieceType.Pawn, "d4");

        assertEquals(at("e3"), PathFinder.nextStep(board, pawn));
    }

    // Đường đi nhiều bước

    @Test
    void rookNeedsTwoMovesWhenNotAligned()
    {
        playerAt("h1");
        Piece rook = addEnemy(PieceType.Rook, "a8");

        List<Position> path = PathFinder.shortestPath(board, rook);

        assertEquals(2, path.size());
        assertTrue(Set.of(at("a1"), at("h8")).contains(path.get(0)));
        assertValidPath(rook, path);
    }

    @Test
    void knightFromCornerToOppositeCornerTakesSixMoves()
    {
        playerAt("h8");
        Piece knight = addEnemy(PieceType.Knight, "a1");

        List<Position> path = PathFinder.shortestPath(board, knight);

        assertEquals(6, path.size());
        assertValidPath(knight, path);
    }

    @Test
    void knightOneJumpAway()
    {
        playerAt("b3");
        Piece knight = addEnemy(PieceType.Knight, "a1");

        assertEquals(at("b3"), PathFinder.nextStep(board, knight));
    }

    @Test
    void bishopReachesPlayerOnSameColorInTwoMoves()
    {
        playerAt("h2");
        Piece bishop = addEnemy(PieceType.Bishop, "a1");

        List<Position> path = PathFinder.shortestPath(board, bishop);

        assertEquals(2, path.size());
        assertValidPath(bishop, path);
    }

    @Test
    void pawnWalksDownThenAttacksDiagonally()
    {
        playerAt("e3");
        Piece pawn = addEnemy(PieceType.Pawn, "d7");

        List<Position> path = PathFinder.shortestPath(board, pawn);

        assertEquals(List.of(at("d6"), at("d5"), at("d4"), at("e3")), path);
        assertEquals(at("d6"), PathFinder.nextStep(board, pawn));
    }

    // Vật cản

    @Test
    void rookGoesAroundBlockingEnemy()
    {
        playerAt("a1");
        Piece rook = addEnemy(PieceType.Rook, "a8");
        addEnemy(PieceType.Pawn, "a4");

        List<Position> path = PathFinder.shortestPath(board, rook);

        assertEquals(3, path.size());
        assertFalse(path.contains(at("a4")));
        assertValidPath(rook, path);
    }

    @Test
    void pathNeverPassesThroughOtherEnemies()
    {
        playerAt("h1");
        Piece queen = addEnemy(PieceType.Queen, "a8");
        addEnemy(PieceType.Pawn, "d5");
        addEnemy(PieceType.Pawn, "e4");
        addEnemy(PieceType.Pawn, "h8");
        addEnemy(PieceType.Pawn, "a1");

        List<Position> path = PathFinder.shortestPath(board, queen);

        assertValidPath(queen, path);
        for (Position p : path)
        {
            assertNull(board.getEnemyAt(p));
        }
    }

    // Không có đường

    @Test
    void bishopCannotReachPlayerOnOtherColor()
    {
        playerAt("b1");
        Piece bishop = addEnemy(PieceType.Bishop, "a1");

        assertNull(PathFinder.shortestPath(board, bishop));
        assertNull(PathFinder.nextStep(board, bishop));
    }

    @Test
    void pawnBelowPlayerCannotReachIt()
    {
        playerAt("d6");
        Piece pawn = addEnemy(PieceType.Pawn, "d3");

        assertNull(PathFinder.nextStep(board, pawn));
    }

    @Test
    void pawnInSameColumnAsPlayerCannotReachIt()
    {
        playerAt("d2");
        Piece pawn = addEnemy(PieceType.Pawn, "d7");

        assertNull(PathFinder.nextStep(board, pawn));
    }

    @Test
    void blockedPawnHasNoPath()
    {
        playerAt("e5");
        Piece pawn = addEnemy(PieceType.Pawn, "d7");
        addEnemy(PieceType.Rook, "d6");

        assertNull(PathFinder.nextStep(board, pawn));
    }

    @Test
    void surroundedQueenHasNoPath()
    {
        playerAt("h1");
        Piece queen = addEnemy(PieceType.Queen, "a8");
        addEnemy(PieceType.Pawn, "a7");
        addEnemy(PieceType.Pawn, "b7");
        addEnemy(PieceType.Pawn, "b8");

        assertNull(PathFinder.nextStep(board, queen));
    }

    // Khoảng cách tới người chơi

    @Test
    void distanceIsOneWhenEnemyCanAttackNow()
    {
        playerAt("a1");
        Piece rook = addEnemy(PieceType.Rook, "a8");

        assertEquals(1, PathFinder.distanceToPlayer(board, rook));
    }

    @Test
    void distanceMatchesHandCalculation()
    {
        playerAt("h8");
        assertEquals(6, PathFinder.distanceToPlayer(board, addEnemy(PieceType.Knight, "a1")));
        assertEquals(2, PathFinder.distanceToPlayer(board, addEnemy(PieceType.Rook, "a7")));
        assertEquals(2, PathFinder.distanceToPlayer(board, addEnemy(PieceType.Queen, "d1")));
    }

    @Test
    void knightNeedsThreeMovesToReachNeighbourSquare()
    {
        playerAt("d5");
        Piece knight = addEnemy(PieceType.Knight, "d4");

        assertEquals(3, PathFinder.distanceToPlayer(board, knight));
    }

    @Test
    void pawnDistanceCountsWalkAndDiagonalAttack()
    {
        playerAt("e3");
        Piece pawn = addEnemy(PieceType.Pawn, "d7");

        assertEquals(4, PathFinder.distanceToPlayer(board, pawn));
    }

    @Test
    void distanceIsMinusOneWhenUnreachable()
    {
        playerAt("d6");
        assertEquals(-1, PathFinder.distanceToPlayer(board, addEnemy(PieceType.Pawn, "d3")));

        playerAt("b1");
        assertEquals(-1, PathFinder.distanceToPlayer(board, addEnemy(PieceType.Bishop, "a1")));
    }

    @Test
    void blockerIncreasesDistance()
    {
        playerAt("a1");
        Piece rook = addEnemy(PieceType.Rook, "a8");
        assertEquals(1, PathFinder.distanceToPlayer(board, rook));

        addEnemy(PieceType.Pawn, "a4");

        assertEquals(3, PathFinder.distanceToPlayer(board, rook));
    }

    @Test
    void distanceAgreesWithNextStepEverywhere()
    {
        playerAt("e4");
        addEnemy(PieceType.Pawn, "c6");

        for (PieceType type : new PieceType[]{PieceType.Pawn, PieceType.Knight, PieceType.Bishop, PieceType.Rook, PieceType.Queen})
        {
            for (int x = 0; x < Board.SIZE; x++)
            {
                for (int y = 0; y < Board.SIZE; y++)
                {
                    Position from = new Position(x, y);
                    if (!board.isEmpty(from))
                    {
                        continue;
                    }
                    Piece e = addEnemy(type, from.toNotation());
                    int d = PathFinder.distanceToPlayer(board, e);
                    Position step = PathFinder.nextStep(board, e);

                    assertEquals(d == -1, step == null, type + " tại " + from.toNotation());
                    assertEquals(d == 1, board.playerPos.equals(step), type + " tại " + from.toNotation());
                    board.removeEnemy(e);
                }
            }
        }
    }

    // An toàn

    @Test
    void findingPathDoesNotChangeTheBoard()
    {
        playerAt("h1");
        Piece knight = addEnemy(PieceType.Knight, "a8");
        addEnemy(PieceType.Pawn, "c7");

        PathFinder.shortestPath(board, knight);

        assertEquals(at("a8"), knight.pos);
        assertEquals(2, board.getEnemies().size());
        assertEquals(at("h1"), board.playerPos);
    }

    @Test
    void everyPieceFromEverySquareGivesValidPathOrNull()
    {
        playerAt("e4");
        addEnemy(PieceType.Pawn, "c6");
        addEnemy(PieceType.Rook, "f3");

        for (PieceType type : new PieceType[]{PieceType.Pawn, PieceType.Knight, PieceType.Bishop, PieceType.Rook, PieceType.Queen})
        {
            for (int x = 0; x < Board.SIZE; x++)
            {
                for (int y = 0; y < Board.SIZE; y++)
                {
                    Position from = new Position(x, y);
                    if (!board.isEmpty(from))
                    {
                        continue;
                    }
                    Piece e = addEnemy(type, from.toNotation());
                    List<Position> path = PathFinder.shortestPath(board, e);
                    if (path != null)
                    {
                        assertValidPath(e, path);
                    }
                    board.removeEnemy(e);
                }
            }
        }
    }
}
