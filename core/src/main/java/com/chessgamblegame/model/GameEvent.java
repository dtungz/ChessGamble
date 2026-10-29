package com.chessgamblegame.model;

import com.chessgamblegame.model.enums.PieceType;

// Một việc đã xảy ra trong lượt. TurnManager tính xong luật rồi trả về danh sách event theo đúng thứ tự,
// giao diện chỉ cần phát lại từng event thành animation (trượt quân, nháy đỏ...).
// from/to: ô liên quan (bằng nhau nếu sự việc xảy ra tại 1 ô). piece: quân địch liên quan (null nếu không có).
// newType: chỉ dùng cho ENEMY_PROMOTED.
public record GameEvent(Kind kind, Position from, Position to, Piece piece, PieceType newType)
{
    public enum Kind
    {
        PLAYER_MOVED,             // người chơi đi from -> to
        PLAYER_CAPTURED_ENEMY,    // người chơi ăn quân piece tại to
        ENEMY_MOVED,              // quân piece đi from -> to
        PLAYER_HIT,               // quân piece ở from đánh trúng người chơi ở to (mất 1 máu)
        ENEMY_REMOVED_AFTER_HIT,  // quân piece biến mất sau khi đánh trúng
        ENEMY_PROMOTED,           // Tốt piece tại to phong cấp thành newType
        HEAL,                     // người chơi hồi 1 máu tại to (Endless)
        ENEMY_SPAWNED             // quân piece mới xuất hiện tại to (Endless)
    }

    public static GameEvent playerMoved(Position from, Position to)
    {
        return new GameEvent(Kind.PLAYER_MOVED, from, to, null, null);
    }

    public static GameEvent playerCaptured(Piece enemy, Position at)
    {
        return new GameEvent(Kind.PLAYER_CAPTURED_ENEMY, at, at, enemy, null);
    }

    public static GameEvent enemyMoved(Piece enemy, Position from, Position to)
    {
        return new GameEvent(Kind.ENEMY_MOVED, from, to, enemy, null);
    }

    public static GameEvent playerHit(Piece enemy, Position from, Position playerPos)
    {
        return new GameEvent(Kind.PLAYER_HIT, from, playerPos, enemy, null);
    }

    public static GameEvent enemyRemovedAfterHit(Piece enemy, Position at)
    {
        return new GameEvent(Kind.ENEMY_REMOVED_AFTER_HIT, at, at, enemy, null);
    }

    public static GameEvent enemyPromoted(Piece enemy, Position at, PieceType newType)
    {
        return new GameEvent(Kind.ENEMY_PROMOTED, at, at, enemy, newType);
    }

    public static GameEvent heal(Position playerPos)
    {
        return new GameEvent(Kind.HEAL, playerPos, playerPos, null, null);
    }

    public static GameEvent enemySpawned(Piece enemy, Position at)
    {
        return new GameEvent(Kind.ENEMY_SPAWNED, at, at, enemy, null);
    }
}
