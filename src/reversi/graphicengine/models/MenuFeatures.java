package reversi.graphicengine.models;

import com.raylib.Raylib.*;

public class MenuFeatures {
    final Rectangle button1, button2, button3;

    public MenuFeatures(Rectangle startGameButton, Rectangle loadGameButton, Rectangle editorButton) {
        this.button1 = startGameButton;
        this.button2 = loadGameButton;
        this.button3 = editorButton;
    }

    public Rectangle getButton1() {
        return button1;
    }

    public Rectangle getButton2() {
        return button2;
    }

    public Rectangle getButton3() {
        return button3;
    }
}
