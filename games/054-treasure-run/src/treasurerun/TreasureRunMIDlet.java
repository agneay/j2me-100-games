package treasurerun;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Treasure Run - MIDlet entry point. */
public class TreasureRunMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new RunGame();
    }
}
