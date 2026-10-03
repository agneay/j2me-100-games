package hilocards;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Hi-Lo Cards - MIDlet entry point. */
public class HiLoCardsMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new HiLoGame();
    }
}
