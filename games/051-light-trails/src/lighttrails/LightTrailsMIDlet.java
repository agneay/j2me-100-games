package lighttrails;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Light Trails - MIDlet entry point. */
public class LightTrailsMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new TrailsGame();
    }
}
