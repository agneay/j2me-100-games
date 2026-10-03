package cellgarden;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Cell Garden - MIDlet entry point. */
public class CellGardenMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new GardenGame();
    }
}
