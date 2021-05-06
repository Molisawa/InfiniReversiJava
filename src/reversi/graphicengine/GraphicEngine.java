package reversi.graphicengine;


import reversi.gameengine.*;
import reversi.graphicengine.enums.ScreenFlags;
import reversi.graphicengine.models.MenuFeatures;
import reversi.graphicengine.models.Slider;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import static com.raylib.Colors.*;
import static com.raylib.Raylib.*;
import static java.lang.Math.*;

public class GraphicEngine {
    final private ScreenFeatures screenFeatures;
    private Board board;
    private Music music;
    private MenuFeatures gameMenuFeatures;
    private Difficulty difficulty = Difficulty.EASY;
    private int customBoardSize = 0;
    private MenuFeatures mainMenuFeatures;
    private final boolean DEBUG_MODE = java.lang.management.ManagementFactory.getRuntimeMXBean().
            getInputArguments().toString().indexOf("jdwp") > 0;
    private final String resourceFolder = DEBUG_MODE ? "src/resources/" : "";
    private ScreenFlags screenFlag;
    private ScreenFlags nextScreen;
    private int frameCounter = 0;
    private final Slider slider;

    private boolean clicked;
    private Vector2 mousePosition;

    private String filename = "";
    private final File folder = new File("saved");

    private PlayerType selectedPiece = PlayerType.BLACK_PLAYER;

    public GraphicEngine() {
        this.board = new Board();
        screenFeatures = new ScreenFeatures(1000, 800, 8);
        screenFlag = ScreenFlags.MENU;
        slider = new Slider();
    }

    public void initGame() {
        SetTargetFPS(60);
        InitWindow(screenFeatures.getScreenWidth(), screenFeatures.getScreenHeight(), "Reversi");
        InitAudioDevice();
        setMenuOptions();
        setMenu();
        System.out.println();
        music = LoadMusicStream(resourceFolder + "resources/background.mp3");
        PlayMusicStream(music);
    }

    private void drawGrid() {

        for (int i = 0; i < board.getSize() + 1; i++) {
            DrawLineV(new Vector2().x(screenFeatures.getSquareSize() * i).y(0),
                    new Vector2().x(screenFeatures.getSquareSize() * i).y(board.getSize() * screenFeatures.getSquareSize()), BLACK);
            DrawLineV(new Vector2().y(screenFeatures.getSquareSize() * i).x(0),
                    new Vector2().y(screenFeatures.getSquareSize() * i).x(board.getSize() * screenFeatures.getSquareSize()), BLACK);
        }

        DrawRectangle((int) (board.getSize() * screenFeatures.getSquareSize() + 1), 0, screenFeatures.getScreenWidth() - 1,
                screenFeatures.getScreenHeight(), WHITE);
        DrawRectangle((int) (board.getSize() * screenFeatures.getSquareSize() + 1), 0, screenFeatures.getScreenWidth() - 1,
                screenFeatures.getScreenHeight(), Fade(DARKGREEN, 0.5f));
    }

    private void PlayScreen() {
        ClearBackground(DARKGREEN);
        drawGrid();
        DrawRectangleRec(gameMenuFeatures.getButton1(), WHITE);
        DrawRectangleRec(gameMenuFeatures.getButton2(), WHITE);
        DrawRectangleRec(gameMenuFeatures.getButton3(), WHITE);

        DrawText("Go back", (int) (gameMenuFeatures.getButton1().x() - MeasureText("Go back", 30) / 2 + gameMenuFeatures.getButton1().width() / 2),
                (int) (gameMenuFeatures.getButton1().y() + gameMenuFeatures.getButton1().height() / 2 - 15), 30,
                reversi_game_engine.canGoBack(board) ? BLACK : GRAY);
        DrawText("Go foward", (int) (gameMenuFeatures.getButton2().x() - MeasureText("Go foward",
                30) / 2 + gameMenuFeatures.getButton2().width() / 2),
                (int) (gameMenuFeatures.getButton2().y() + gameMenuFeatures.getButton2().height() / 2 - 15), 30,
                reversi_game_engine.canGoFoward(board) ? BLACK : GRAY);
        DrawText("Save game", (int) (gameMenuFeatures.getButton3().x() - MeasureText("Save game", 30) / 2
                        + gameMenuFeatures.getButton3().width() / 2),
                (int) (gameMenuFeatures.getButton3().y() + gameMenuFeatures.getButton3().height() / 2 - 15), 30, BLACK);
        CheckButtonPressed();
        DrawDrawingState();
        switch (reversi_game_engine.nextTurn(board)) {

            case BLACK_PLAYER -> {
                reversi_game_engine.SetHelpers(board, PlayerType.BLACK_PLAYER);
                CheckPiecePlayed();
            }
            case WHITE_PLAYER -> reversi_game_engine.computerMove(board, PlayerType.WHITE_PLAYER);
            case NONE -> {
            }
        }
        DrawText("Your score:", (int) (gameMenuFeatures.getButton3().x()), (int) (gameMenuFeatures.getButton3().height() + gameMenuFeatures.getButton3().y() + 30), 20, WHITE);
        DrawText(String.valueOf(reversi_game_engine.getScore(board, PlayerType.BLACK_PLAYER.swigValue())),
                (int) (gameMenuFeatures.getButton3().x()), (int) (gameMenuFeatures.getButton3().height() + gameMenuFeatures.getButton3().y() + 50), 20,
                WHITE);
        DrawText("CPU score:", (int) (gameMenuFeatures.getButton3().x()), (int) (gameMenuFeatures.getButton3().height() + gameMenuFeatures.getButton3().y() + 100), 20, WHITE);
        DrawText(String.valueOf(reversi_game_engine.getScore(board, PlayerType.WHITE_PLAYER.swigValue())),
                (int) (gameMenuFeatures.getButton3().x()), (int) (gameMenuFeatures.getButton3().height() + gameMenuFeatures.getButton3().y() + 120), 20,
                WHITE);
        if (reversi_game_engine.isGameOver(board)) {
            DrawText("Game Over", screenFeatures.getScreenHeight() / 2 - MeasureText("Game Over", 80) / 2,
                    screenFeatures.getScreenHeight() / 2 - 40, 80, GRAY);
            String text = "";
            Color color = WHITE;
            switch (Winners.swigToEnum(reversi_game_engine.getWinner(board))) {
                case WINNER -> {
                    text = "You win!";
                    color = GREEN;
                }
                case LOSER -> {
                    text = "You lose!";
                    color = RED;
                }
                case TIE -> {
                    text = "It's a tie!";
                    color = GRAY;
                }
            }
            DrawText(text, (screenFeatures.getScreenHeight()) / 2 - MeasureText(text, 60) / 2,
                    screenFeatures.getScreenHeight() / 2 - 30 + 80 + 10, 60, color);
        }

        int freeSpace = screenFeatures.getScreenWidth() - screenFeatures.getScreenHeight();
        Rectangle exitButton = new Rectangle()
                .x(screenFeatures.getScreenHeight() + 30)
                .y(screenFeatures.getScreenHeight() - 150)
                .width((float) freeSpace - 60)
                .height(100);
        DrawRectangleRec(exitButton, WHITE);
        DrawText("Exit", (int) (exitButton.x() + exitButton.width() / 2 - MeasureText("Exit", 30) / 2), (int) (exitButton.y() + exitButton.height() / 2 - 15), 30, BLACK);
        if (checkObjectClicked(exitButton)) {
            reversi_game_engine.destructBoard(board);
            screenFlag = ScreenFlags.MENU;
        }
    }

    private void CheckPiecePlayed() {
        for (int i = 0; i < board.getSize(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                Vector2 vector = new Vector2();
                if (reversi_game_engine.getJavaState(board, i, j).getPieceType() == StateFlags.HELPER) {
                    vector = vector
                            .x((i) * screenFeatures.getSquareSize() + screenFeatures.getSquareSize() / 2)
                            .y((j) * screenFeatures.getSquareSize() + screenFeatures.getSquareSize() / 2);
                    if (CheckCollisionPointCircle(mousePosition, vector, screenFeatures.getSquareSize() / 2 - 5)) {
                        if (clicked) {
                            MovementExt movement = new MovementExt(PlayerType.BLACK_PLAYER, i, j);
                            reversi_game_engine.makeRealMove(board, movement);
                            reversi_game_engine.removeHistoryFoward(board);
                            DrawDrawingState();
                        } else {
                            DrawRectangle((int) ((i) * screenFeatures.getSquareSize() + 1), (int) ((j) * screenFeatures.getSquareSize() + 1),
                                    (int) (screenFeatures.getSquareSize() - 2),
                                    (int) (screenFeatures.getSquareSize() - 2), DARKGREEN);
                            DrawCircle((int) ((i) * screenFeatures.getSquareSize() + screenFeatures.getSquareSize() / 2),
                                    (int) ((j) * screenFeatures.getSquareSize() + screenFeatures.getSquareSize() / 2),
                                    screenFeatures.getSquareSize() / 2 - 5, Fade(BLACK, 0.4f));
                        }
                    }
                }

            }

        }
    }

    private void ConfigGameScreen() {
        ClearBackground(RAYWHITE);

        int size = 6 + 2 * customBoardSize;
        DrawText(String.valueOf(size), screenFeatures.getScreenWidth() / 3 - MeasureText(String.valueOf(size), 100) / 2,
                screenFeatures.getScreenHeight() / 2 - 180,
                100, BLACK);

        DrawText("Chose your board size", screenFeatures.getScreenWidth() / 3 - MeasureText("Chose your board size", 30) / 2,
                screenFeatures.getScreenHeight() / 2 - 300,
                30, GRAY);
        int margin = (2 * (screenFeatures.getScreenWidth() / 3) - 2 * 130) / 3;
        Rectangle sum = new Rectangle().x(margin).y((float) screenFeatures.getScreenHeight() / 2 - 30).width(130).height(130);
        Rectangle subs = new Rectangle().x(margin + sum.x() + sum.width()).y(sum.y()).width(sum.width()).height(sum.height());
        boolean overSum = CheckCollisionPointRec(mousePosition, sum);
        boolean overSubs = CheckCollisionPointRec(mousePosition, subs);


        DrawRectangleRec(sum, overSum ? LIGHTGRAY : RAYWHITE);
        DrawText("+", (int) (sum.x() + sum.width() / 2 - MeasureText("+", 70) / 2), (int) (sum.y() + sum.height() / 2 - 35), 70, BLACK);
        DrawRectangleRec(subs, overSubs ? LIGHTGRAY : RAYWHITE);
        DrawText("-", (int) (subs.x() + subs.width() / 2 - MeasureText("-", 70) / 2), (int) (subs.y() + subs.height() / 2 - 35), 70, BLACK);

        Rectangle acceptButton = new Rectangle()
                .x((float) screenFeatures.getScreenWidth() / 2 - 100)
                .y((float) screenFeatures.getScreenHeight() / 2 + 120)
                .width(200)
                .height(80);
        Rectangle cancelButton = new Rectangle()
                .x(acceptButton.x())
                .y(acceptButton.y() + acceptButton.height() + 50)
                .width(acceptButton.width())
                .height(acceptButton.height());
        DrawRectangleRec(acceptButton, LIGHTGRAY);
        DrawRectangleRec(cancelButton, LIGHTGRAY);

        DrawText("Accept", (int) (acceptButton.x() + acceptButton.width() / 2 - MeasureText("Accept", 40) / 2),
                (int) (acceptButton.y() + acceptButton.height() / 2 - 20), 40, WHITE);
        DrawText("Cancel", (int) (cancelButton.x() + cancelButton.width() / 2 - MeasureText("Cancel", 40) / 2),
                (int) (cancelButton.y() + cancelButton.height() / 2 - 20), 40, WHITE);

        float marginDifficulty = ((float) screenFeatures.getScreenHeight() - 340) / 2;
        Rectangle easyButton = new Rectangle()
                .x((float) screenFeatures.getScreenWidth() * 3 / 4 - ((float) screenFeatures.getScreenWidth() / 3 - 80) / 2)
                .y(marginDifficulty)
                .width((float) screenFeatures.getScreenWidth() / 3 - 80)
                .height(80);
        Rectangle intermediateButton = new Rectangle()
                .x(easyButton.x())
                .y(easyButton.y() + easyButton.height() + 50)
                .width(easyButton.width())
                .height(easyButton.height());
        Rectangle hardButton = new Rectangle()
                .x(easyButton.x())
                .y(intermediateButton.y() + intermediateButton.height() + 50)
                .width(easyButton.width())
                .height(easyButton.height());
        DrawRectangleRec(easyButton, difficulty == Difficulty.EASY ? GRAY : LIGHTGRAY);
        DrawRectangleRec(intermediateButton, difficulty == Difficulty.INTERMEDIATE ? GRAY : LIGHTGRAY);
        DrawRectangleRec(hardButton, difficulty == Difficulty.HARD ? GRAY : LIGHTGRAY);
        DrawText("EASY", (int) (easyButton.x() + easyButton.width() / 2 - MeasureText("EASY", 20) / 2),
                (int) (easyButton.y() + easyButton.height() / 2 - 10), 20, WHITE);
        DrawText("INTERMEDIATE", (int) (intermediateButton.x() + intermediateButton.width() / 2 - MeasureText("INTERMEDIATE", 20) / 2),
                (int) (intermediateButton.y() + intermediateButton.height() / 2 - 10), 20, WHITE);
        DrawText("HARD", (int) (hardButton.x() + hardButton.width() / 2 - MeasureText("HARD", 20) / 2),
                (int) (hardButton.y() + hardButton.height() / 2 - 10), 20, WHITE);

        if (checkObjectClicked(easyButton)) difficulty = Difficulty.EASY;
        if (checkObjectClicked(intermediateButton)) difficulty = Difficulty.INTERMEDIATE;
        if (checkObjectClicked(hardButton)) difficulty = Difficulty.HARD;
        if (checkObjectClicked(sum)) customBoardSize++;
        if (checkObjectClicked(subs) && customBoardSize > 0) customBoardSize--;

        if (checkObjectClicked(acceptButton)) {
            reversi_game_engine.initializeGame(board, size, difficulty.swigValue(), nextScreen != ScreenFlags.GAME,
                    new PlayerExt(true), new PlayerExt(false));
            screenFlag = nextScreen;
            difficulty = Difficulty.EASY;
            customBoardSize = 0;
            screenFeatures.setBoardSize(size);
        }

        if (checkObjectClicked(cancelButton)) {
            screenFlag = ScreenFlags.MENU;
            difficulty = Difficulty.EASY;
            customBoardSize = 0;
        }

    }

    private void DrawDrawingState() {
        int offset = (int) ((screenFeatures.getSquareSize() / 2 - 5) * 0.25);
        for (int i = 0; i < board.getSize(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                switch (reversi_game_engine.getJavaState(board, i, j).getPieceType()) {

                    case VOID -> {
                    }
                    case BLACK_PIECE -> {
                        DrawCircle((int) ((i) * screenFeatures.getSquareSize() + screenFeatures.getSquareSize() / 2),
                                (int) ((j) * screenFeatures.getSquareSize() + screenFeatures.getSquareSize() / 2),
                                screenFeatures.getSquareSize() / 2 - 5, BLACK);
                        DrawCircleGradient((int) ((i) * screenFeatures.getSquareSize() + screenFeatures.getSquareSize() / 2 - offset),
                                (int) ((j) * screenFeatures.getSquareSize() + screenFeatures.getSquareSize() / 2 - offset),
                                (int) ((screenFeatures.getSquareSize() / 2 - 5) * 0.5), Fade(WHITE, 0.15f), Fade(WHITE, 0));

                    }
                    case WHITE_PIECE -> {
                        DrawCircle((int) ((i) * screenFeatures.getSquareSize() + screenFeatures.getSquareSize() / 2),
                                (int) ((j) * screenFeatures.getSquareSize() + screenFeatures.getSquareSize() / 2),
                                screenFeatures.getSquareSize() / 2 - 5, RAYWHITE);
                        DrawCircleGradient((int) ((i) * screenFeatures.getSquareSize() + screenFeatures.getSquareSize() / 2 - offset),
                                (int) ((j) * screenFeatures.getSquareSize() + screenFeatures.getSquareSize() / 2 - offset),
                                (int) ((screenFeatures.getSquareSize() / 2 - 5) * 0.5), Fade(BLACK, 0.15f), Fade(BLACK, 0));
                    }
                    case HELPER -> {
                        DrawCircle((int) ((i) * screenFeatures.getSquareSize() + screenFeatures.getSquareSize() / 2),
                                (int) ((j) * screenFeatures.getSquareSize() + screenFeatures.getSquareSize() / 2),
                                screenFeatures.getSquareSize() / 2 - 5, DARKGRAY);
                        DrawCircle((int) ((i) * screenFeatures.getSquareSize() + screenFeatures.getSquareSize() / 2),
                                (int) ((j) * screenFeatures.getSquareSize() + screenFeatures.getSquareSize() / 2),
                                screenFeatures.getSquareSize() / 2 - 7, DARKGREEN);
                    }
                }

            }

        }
    }

    private void CheckButtonPressed() {
        if (checkObjectClicked(gameMenuFeatures.getButton1())) {
            reversi_game_engine.goBack(board);
        }
        if (checkObjectClicked(gameMenuFeatures.getButton2())) {
            reversi_game_engine.goForward(board);
        }
        if (checkObjectClicked(gameMenuFeatures.getButton3())) {
            screenFlag = ScreenFlags.SAVE;
            nextScreen = ScreenFlags.GAME;
        }
    }

    private boolean checkObjectClicked(Rectangle rec) {
        return clicked && CheckCollisionPointRec(mousePosition, rec);
    }

    private boolean checkObjectClicked(Vector2 circle, float radius) {
        return clicked && CheckCollisionPointCircle(mousePosition, circle, radius);
    }

    public void drawGame() {
        while (!WindowShouldClose()) // Detect window close button or ESC key
        {
            clicked = IsMouseButtonPressed(MOUSE_LEFT_BUTTON);
            mousePosition = GetMousePosition();
            frameCounter = (frameCounter + 1) % 60;

            int key = GetKeyPressed();

            while (key > 0) {
                if ((key >= 32) && (key <= 125)) {

                    filename = new StringBuilder().append(filename).append((char) key).toString();
                }
                key = GetKeyPressed();
            }

            if (IsKeyPressed(KEY_BACKSPACE) && filename.length() > 0) {
                filename = filename.substring(0, filename.length() - 1);

            }
            BeginDrawing();
            switch (screenFlag) {

                case MENU -> {
                    UpdateMusicStream(music);
                    MenuScreen();
                }
                case GAME -> {
                    PlayScreen();
                    EndDrawing();
                }
                case SAVE -> {
                    SaveScreen();
                    EndDrawing();
                }
                case LOAD -> {
                    LoadGameScreen();
                    EndDrawing();
                }
                case EDITOR -> {
                    EditorScreen();
                    EndDrawing();
                }
                case CONFIG_GAME -> {
                    ConfigGameScreen();
                    EndDrawing();
                }
            }

        }
        UnloadMusicStream(music);
        CloseAudioDevice();
        CloseWindow();
        reversi_game_engine.destructBoard(board);
    }


    private void SaveScreen() {
        ClearBackground(RAYWHITE);
        int width = max(MeasureText(filename, 30), MeasureText("XXXXXXXX", 30)) + 30;
        int saveWidth = MeasureText("Save", 20) + 20;
        int cancelWidth = MeasureText("Cancel", 20) + 20;

        Rectangle saveRec = new Rectangle()
                .x((float) screenFeatures.getScreenWidth() / 2 - (float) (saveWidth + cancelWidth + 40) / 2)
                .y((float) screenFeatures.getScreenHeight() / 2 + 50)
                .width(saveWidth)
                .height(30);
        Rectangle cancelRec = new Rectangle()
                .x(saveRec.x() + saveRec.width() + 40)
                .y(saveRec.y())
                .width(cancelWidth)
                .height(saveRec.height());

        boolean overSave = CheckCollisionPointRec(mousePosition, saveRec);
        boolean overCancel = CheckCollisionPointRec(mousePosition, cancelRec);

        DrawRectangleRec(saveRec, overSave ? LIGHTGRAY : GRAY);
        DrawText("Save", (int) (saveRec.x() + 10), (int) (saveRec.y() + 5), 20, WHITE);
        DrawRectangleRec(cancelRec, overCancel ? LIGHTGRAY : GRAY);
        DrawText("Cancel", (int) (cancelRec.x() + 10), (int) (saveRec.y() + 5), 20, WHITE);

        DrawRectangleLines(screenFeatures.getScreenWidth() / 2 - width / 2, screenFeatures.getScreenHeight() / 2 - 20, width, 40,
                BLACK);
        DrawText(filename, screenFeatures.getScreenWidth() / 2 - width / 2 + 10, screenFeatures.getScreenHeight() / 2 - 15, 30,
                BLACK);
        if (((frameCounter / 20) % 2) == 0)
            DrawText("_", screenFeatures.getScreenWidth() / 2 - width / 2 + MeasureText(filename, 30) + 10,
                    screenFeatures.getScreenHeight() / 2 - 15, 30, BLACK);

        if (checkObjectClicked(saveRec)) {

            if (folder.exists() || folder.mkdir()) {
                SaveFileText("saved/" + filename + ".brd", reversi_game_engine.saveGame(board).getBytes());
                filename = "";
                screenFlag = nextScreen;
            }
        }
        if (checkObjectClicked(cancelRec)) {
            filename = "";
            screenFlag = nextScreen;
        }
    }

    private void LoadGameScreen() {
        File[] files = folder.listFiles((dir, name) -> name.contains(".brd"));
        ClearBackground(RAYWHITE);

        Rectangle cancelRect = new Rectangle().x(25).y(screenFeatures.getScreenHeight() - 60).width(screenFeatures.getScreenWidth() - 50).height(50);
        if (files != null) {
            boolean bar = (50 * files.length + 10) > (screenFeatures.getScreenHeight() - 80);

            float barSize =
                    ((float) (screenFeatures.getScreenHeight() - 80) / (float) (50 * files.length + 10)) *
                            (screenFeatures.getScreenHeight() - 70);


            Rectangle scrollRect = new Rectangle().x(screenFeatures.getScreenWidth() - 20).y(
                    5 + min(max(0, slider.getOffset() + slider.getDifference()),
                            screenFeatures.getScreenHeight() - 70 - barSize)).width(
                    15).height(
                    barSize);

            if (bar) {
                DrawRectangle(screenFeatures.getScreenWidth() - 20, 5, 15, (int) (cancelRect.y() - 10), Fade(LIGHTGRAY, 0.6f));
                if (slider.isCollision()) {
                    slider.setOffset(mousePosition.y());
                    if (!IsMouseButtonDown(MOUSE_LEFT_BUTTON)) {
                        slider.setCollision(false);
                    }
                }
                if (checkObjectClicked(scrollRect) && clicked) {
                    slider.setOffset(mousePosition.y());
                    slider.setDifference(scrollRect.y() - slider.getOffset());
                    slider.setCollision(true);
                }
                DrawRectangleRec(scrollRect, slider.isCollision() ? GRAY : Fade(GRAY, 0.65f));
            }
            float percent = bar ? (scrollRect.y() - 5) / (float) (screenFeatures.getScreenHeight() - 70 - barSize) : 0;
            int i = 0;
            for (File boardFile :
                    files) {
                Rectangle rec = new Rectangle()
                        .x(10)
                        .y(i * 50 + 10 - (((50 * files.length + 10) - screenFeatures.getScreenHeight() +
                                60) * percent))
                        .width(MeasureText(boardFile.getName(), 20) + 20)
                        .height(30);
                boolean over = CheckCollisionPointRec(GetMousePosition(), rec);
                if (checkObjectClicked(rec)) {
                    Scanner scanner = null;
                    try {
                        scanner = new Scanner(boardFile);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    assert scanner != null;
                    Board boardTemp = reversi_game_engine.loadGame(scanner.next());
                    if (!boardTemp.getInitialized()) {
                        screenFlag = ScreenFlags.MENU;
                        return;
                    }
                    board = boardTemp;
                    screenFeatures.setBoardSize(boardTemp.getSize());
                    screenFlag = ScreenFlags.GAME;
                }
                DrawRectangleRec(rec, over ? LIGHTGRAY : RAYWHITE);
                DrawText(boardFile.getName(), 20, (int) rec.y() + 5, 20, BLACK);
                i++;
            }
        }
        DrawRectangle(0, screenFeatures.getScreenHeight() - 70, screenFeatures.getScreenWidth(), 70, RAYWHITE);


        boolean over = CheckCollisionPointRec(GetMousePosition(), cancelRect);
        DrawRectangleRec(cancelRect, over ? LIGHTGRAY : GRAY);
        if (over && IsMouseButtonPressed(MOUSE_LEFT_BUTTON)) screenFlag = ScreenFlags.MENU;
        DrawText("CANCEL", (int) (screenFeatures.getScreenWidth() / 2 - MeasureText("CANCEL", 30) / 2 + cancelRect.x() / 2),
                (int) (cancelRect.y() + 10), 30, WHITE);
    }

    private void MenuScreen() {
        ClearBackground(DARKGREEN);
        DrawText("INFINIREVERSI", (screenFeatures.getScreenWidth() / 2 - MeasureText("INFINIREVERSI", 30) / 2),
                (screenFeatures.getScreenHeight() / 2 - 375), 30, WHITE);
        int frame = (int) floor(this.frameCounter * 0.383);
        boolean putZero = frame < 10;
        Image image = LoadImage(
                resourceFolder + "resources/frames/frame_" + (putZero ? "0" : "") + frame + "_delay-0.03s.gif");
        Texture2D texture = LoadTextureFromImage(image);
        DrawTexture(texture, screenFeatures.getScreenWidth() / 2 - image.width() / 2, (int) (screenFeatures.getScreenHeight() * 0.1), WHITE);
        DrawRectangleRec(mainMenuFeatures.getButton1(), LIGHTGRAY);
        DrawRectangleRec(mainMenuFeatures.getButton2(), LIGHTGRAY);
        DrawRectangleRec(mainMenuFeatures.getButton3(), LIGHTGRAY);

        DrawText("Start",
                (int) (mainMenuFeatures.getButton1().x() + mainMenuFeatures.getButton1().width() / 2 - MeasureText("Start", 30) / 2),
                (int) (mainMenuFeatures.getButton1().y() + mainMenuFeatures.getButton1().height() / 2 - 15), 30, WHITE);
        DrawText("Load game",
                (int) (mainMenuFeatures.getButton2().x() + mainMenuFeatures.getButton2().width() / 2 - MeasureText("Load game", 30) / 2),
                (int) (mainMenuFeatures.getButton2().y() + mainMenuFeatures.getButton2().height() / 2 - 15), 30, WHITE);
        DrawText("Game editor",
                (int) (mainMenuFeatures.getButton3().x() + mainMenuFeatures.getButton3().width() / 2 - MeasureText("Game editor", 30) / 2),
                (int) (mainMenuFeatures.getButton3().y() + mainMenuFeatures.getButton3().height() / 2 - 15), 30, WHITE);
        DrawText("v1.1", screenFeatures.getScreenWidth() / 2 - MeasureText("v1.1", 30) / 2,
                screenFeatures.getScreenHeight() - 30,
                15, WHITE);
        DrawText("Created by Molisawa and Jorge",
                screenFeatures.getScreenWidth() - MeasureText("Created by Molisawa and Jorge", 30) / 2,
                screenFeatures.getScreenHeight() - 30,
                15, WHITE);
        CheckMenuButtonPressed();
        EndDrawing();
        UnloadTexture(texture);
        UnloadImage(image);
    }


    private void EditorScreen() {
        ClearBackground(DARKGREEN);
        drawGrid();
        int margin = screenFeatures.getScreenHeight();
        int freeSpace = screenFeatures.getScreenWidth() - margin;
        float radius = (float) freeSpace / 4;
        Vector2 black = new Vector2().x(2 * radius + margin).y(50 + radius);
        Vector2 white = new Vector2().x(2 * radius + margin).y(50 + black.y() + 2 * radius);
        DrawCircleV(black, radius, BLACK);
        DrawCircleV(white, radius, WHITE);
        boolean isBlack = selectedPiece == PlayerType.BLACK_PLAYER;
        DrawCircleV(isBlack ? black : white, radius / 10, RED);
        DrawDrawingState();

        boolean clickedState;

        if (mousePosition.x() >= 0 && mousePosition.x() < margin && mousePosition.y() >= 0 && mousePosition.y() < screenFeatures.getScreenHeight()) {
            clickedState = IsMouseButtonDown(MOUSE_LEFT_BUTTON);
            int x = (int) floor(mousePosition.x() / screenFeatures.getSquareSize());
            int y = (int) floor(mousePosition.y() / screenFeatures.getSquareSize());
            Vector2 helper = new Vector2()
                    .x(x * screenFeatures.getSquareSize() + radius)
                    .y(y * screenFeatures.getSquareSize() + radius);

            Rectangle helperRec = new Rectangle()
                    .x(helper.x() + 1 - radius)
                    .y(helper.y() + 1 - radius)
                    .width(screenFeatures.getSquareSize() - 2)
                    .height(screenFeatures.getSquareSize() - 2);

            DrawRectangleRec(helperRec, DARKGREEN);

            Vector2 circle = new Vector2()
                    .x(x * screenFeatures.getSquareSize() + screenFeatures.getSquareSize() / 2)
                    .y(y * screenFeatures.getSquareSize() + screenFeatures.getSquareSize() / 2);

            switch (selectedPiece) {

                case BLACK_PLAYER -> DrawCircleV(circle,
                        screenFeatures.getSquareSize() / 2 - 5, Fade(BLACK, 0.5f));
                case WHITE_PLAYER -> DrawCircleV(circle,
                        screenFeatures.getSquareSize() / 2 - 5, Fade(WHITE, 0.5f));
            }
            if (clickedState && CheckCollisionPointRec(mousePosition, helperRec)) {
                reversi_game_engine.setEditorPieceType(board, x, y, isBlack ? PlayerType.BLACK_PLAYER.swigValue() : PlayerType.WHITE_PLAYER.swigValue());
            }
            if ((IsMouseButtonPressed(MOUSE_RIGHT_BUTTON)) ||
                    IsMouseButtonDown(MOUSE_RIGHT_BUTTON) && CheckCollisionPointRec(mousePosition, helperRec)) {
                reversi_game_engine.setEditorPieceType(board, x, y, StateFlags.VOID.swigValue());
            }

        }
        Rectangle exitButton = new Rectangle()
                .x(margin + 30)
                .y(margin - 150)
                .width((float) freeSpace - 60)
                .height(100);
        DrawRectangleRec(exitButton, LIGHTGRAY);
        DrawText("Exit", (int) (exitButton.x() + exitButton.width() / 2 - MeasureText("Exit", 30) / 2), (int) (exitButton.y() + exitButton.height() / 2 - 15), 30, WHITE);
        Rectangle saveButton = new Rectangle()
                .x(exitButton.x())
                .y(exitButton.y() - exitButton.height() - 50)
                .width(exitButton.width())
                .height(exitButton.height());
        DrawRectangleRec(saveButton, LIGHTGRAY);
        DrawText("Save", (int) (saveButton.x() + saveButton.width() / 2 - MeasureText("Save", 30) / 2), (int) (saveButton.y() + saveButton.height() / 2 - 15), 30, WHITE);

        if (checkObjectClicked(black, radius)) {
            selectedPiece = PlayerType.BLACK_PLAYER;
        }
        if (checkObjectClicked(white, radius)) {
            selectedPiece = PlayerType.WHITE_PLAYER;
        }

        if (checkObjectClicked(saveButton)) {
            screenFlag = ScreenFlags.SAVE;
            nextScreen = ScreenFlags.EDITOR;
        }

        if (checkObjectClicked(exitButton)) {
            reversi_game_engine.destructBoard(board);
            screenFlag = ScreenFlags.MENU;
        }

    }

    private void CheckMenuButtonPressed() {
        if (checkObjectClicked(mainMenuFeatures.getButton1())) {
            screenFlag = ScreenFlags.CONFIG_GAME;
            nextScreen = ScreenFlags.GAME;
        }
        if (checkObjectClicked(mainMenuFeatures.getButton2())) {
            screenFlag = ScreenFlags.LOAD;
        }
        if (checkObjectClicked(mainMenuFeatures.getButton3())) {
            screenFlag = ScreenFlags.CONFIG_GAME;
            nextScreen = ScreenFlags.EDITOR;
            board.setCustom(true);
        }

    }

    private void setMenuOptions() {
        int bussyScreen = 400 + (int) (screenFeatures.getScreenHeight() * 0.1);
        int freeScreen = screenFeatures.getScreenHeight() - bussyScreen;
        int number = (screenFeatures.getScreenWidth() - (3 * 250)) / 4;
        Rectangle startGameButton = new Rectangle()
                .x(number)
                .y(bussyScreen + 100)
                .width(250)
                .height(freeScreen - 200);
        Rectangle loadGameButton = new Rectangle()
                .x(number + startGameButton.x() + startGameButton.width())
                .y(startGameButton.y())
                .width(startGameButton.width())
                .height(startGameButton.height());
        Rectangle editorButton = new Rectangle()
                .x(number + loadGameButton.x() + loadGameButton.width())
                .y(startGameButton.y())
                .width(startGameButton.width())
                .height(startGameButton.height());
        mainMenuFeatures = new MenuFeatures(startGameButton, loadGameButton, editorButton);
    }

    private void setMenu() {
        Rectangle goBackButton = new Rectangle()
                .x(screenFeatures.getScreenHeight() + 20)
                .y(30)
                .width(screenFeatures.getScreenWidth() - screenFeatures.getScreenHeight() - 40)
                .height(75);
        Rectangle goFowardButton = new Rectangle()
                .x(goBackButton.x())
                .y(goBackButton.y() + goBackButton.height() + 10)
                .width(goBackButton.width())
                .height(goBackButton.height());
        Rectangle saveGameButton = new Rectangle()
                .x(goFowardButton.x())
                .y(goFowardButton.y() + goFowardButton.height() + 10)
                .width(goFowardButton.width())
                .height(goFowardButton.height());
        gameMenuFeatures = new MenuFeatures(goBackButton, goFowardButton, saveGameButton);
    }
}
