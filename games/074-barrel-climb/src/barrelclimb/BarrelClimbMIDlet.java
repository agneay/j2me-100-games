package barrelclimb;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Barrel Climb - MIDlet entry point. */
public class BarrelClimbMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new ClimbGame();
    }
}
