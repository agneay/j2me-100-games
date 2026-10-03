package hillclimb;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Hill Climb - MIDlet entry point. */
public class HillClimbMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new ClimbGame();
    }
}
