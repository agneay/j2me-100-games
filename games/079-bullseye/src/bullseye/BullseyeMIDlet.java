package bullseye;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Bullseye - MIDlet entry point. */
public class BullseyeMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new ArcheryGame();
    }
}
