package spelldeck;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Spell Deck - MIDlet entry point. */
public class SpellDeckMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new SpellGame();
    }
}
