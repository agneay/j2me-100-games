package deepmine;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Deep Mine - MIDlet entry point. */
public class DeepMineMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new MineGame();
    }
}
