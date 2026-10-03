package pixeljumper;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Pixel Jumper - MIDlet entry point. */
public class PixelJumperMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new JumperGame();
    }
}
