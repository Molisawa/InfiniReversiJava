package reversi.gameengine;

public class MovementExt extends Movement{
    public MovementExt(PlayerType playerType, int x, int y) {
        super();
        setPieceType(playerType);
        setX(x);
        setY(y);
    }
}
