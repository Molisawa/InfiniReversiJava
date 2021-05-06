package reversi.graphicengine;

public class ScreenFeatures {
    final private int screenWidth;
    final private int screenHeight;
    private int boardSize;


    public ScreenFeatures(int screenWidth, int screenHeight, int boardSize) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.boardSize = boardSize;
    }

    public float getSquareSize() {
        return (float) screenHeight / (float) boardSize;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }
}
