package pixelracer;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Pixel Racer - MIDlet entry point. */
public class PixelRacerMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new RacerGame();
    }
}
