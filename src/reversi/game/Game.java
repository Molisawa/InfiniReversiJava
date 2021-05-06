package reversi.game;

import reversi.gameengine.*;
import reversi.graphicengine.GraphicEngine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Game {

    static void loadGameEngine() {
        String nativeLibrary = "/lib/win64/libreversi_game_engine.dll"; // TODO: Multiplatform.
        String nativeLibraryExtension = ".dll";
        String nativeLibraryPrefix = "lib"; // Linux has "lib" prefix.

        // Copy the correct system-native version of raylib to a temp folder.
        InputStream fin = Game.class.getResourceAsStream(nativeLibrary);
        if (fin == null) {
            System.err.println("Failed to locate system native file in resources.");
            System.exit(-1);
        }
        File temp = null;
        try {
            byte[] buffer = new byte[4096]; // Most new disks have 4096-byte chunks in the FS.
            temp = File.createTempFile(nativeLibraryPrefix + "reversi_game_engine", nativeLibraryExtension);
            FileOutputStream fout = new FileOutputStream(temp);
            int bytesRead;
            while ((bytesRead = fin.read(buffer)) != -1) {
                for (int i = 0; i < bytesRead; i++) {
                    fout.write(buffer[i]);
                }
            }
            fout.close();
            fin.close();
        } catch (IOException ioe) {
            System.err.println("Failed to unpack Game Engine.  This can happen if there are no write permission to temp.");
            System.exit(-1);
        }

        // temp.getPath now has the full path to the library.  Remove filename.
        String loadPath = temp.getPath();
        loadPath = loadPath.substring(0, loadPath.lastIndexOf(File.separatorChar));
        // Need to remove the .dll or .so and, maybe, get rid of "lib" at the start.
        // We need to remove the filename from the full path and remove the ".dll" suffix from the filename.
        // Note that we can't remove the ".dll" from the suffix above or the library won't be found by LibLoader.
        // It expects just "foo" when trying to load "libfoo" or "foo.dll".
        // In being smart it has made our lives hard.
        String loadName = temp.getName();
        loadName = loadName.replaceFirst(nativeLibraryPrefix, "");
        loadName = loadName.substring(0, loadName.length() - nativeLibraryExtension.length());

        // Invoke loader.
        System.out.println("Trying to load game engine from " + loadPath + " with name " + loadName);
        System.load(temp.getAbsolutePath());
        temp.deleteOnExit();
    }

    static void loadLibraries() {
        loadGameEngine();
    }

    public static void main(String[] args) {
        loadLibraries();
        GraphicEngine graphicEngine = new GraphicEngine();
        graphicEngine.initGame();
        graphicEngine.drawGame();
    }


}
