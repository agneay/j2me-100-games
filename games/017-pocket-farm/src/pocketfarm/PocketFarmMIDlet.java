package pocketfarm;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Pocket Farm - MIDlet entry point. */
public class PocketFarmMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new FarmGame();
    }
}
