package mathblitz;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Math Blitz - MIDlet entry point. */
public class MathBlitzMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new MathGame();
    }
}
