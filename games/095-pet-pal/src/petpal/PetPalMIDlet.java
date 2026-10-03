package petpal;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Pet Pal - MIDlet entry point. */
public class PetPalMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new PetGame();
    }
}
