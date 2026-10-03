package trafficdash;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Traffic Dash - MIDlet entry point. */
public class TrafficDashMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new TrafficGame();
    }
}
