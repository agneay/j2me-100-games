package gravityflip;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Gravity Flip - MIDlet entry point. */
public class GravityFlipMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new FlipGame();
    }
}
