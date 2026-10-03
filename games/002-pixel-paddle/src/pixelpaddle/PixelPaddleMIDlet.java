package pixelpaddle;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Pixel Paddle - MIDlet entry point. */
public class PixelPaddleMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new PaddleGame();
    }
}
