package hoopshot;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Hoop Shot - MIDlet entry point. */
public class HoopShotMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new HoopGame();
    }
}
