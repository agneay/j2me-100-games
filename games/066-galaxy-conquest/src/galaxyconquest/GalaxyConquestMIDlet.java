package galaxyconquest;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Galaxy Conquest - MIDlet entry point. */
public class GalaxyConquestMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new GalaxyGame();
    }
}
