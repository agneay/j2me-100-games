package lakefishing;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Lake Fishing - MIDlet entry point. */
public class LakeFishingMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new FishingGame();
    }
}
