package spacetrader;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Space Trader - MIDlet entry point. */
public class SpaceTraderMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new TraderGame();
    }
}
