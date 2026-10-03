package ringracer;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Ring Racer - MIDlet entry point. */
public class RingRacerMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new RingGame();
    }
}
