package multitaprush;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Multitap Rush - MIDlet entry point. */
public class MultitapRushMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new MultitapGame();
    }
}
