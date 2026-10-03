package orbitjump;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Orbit Jump - MIDlet entry point. */
public class OrbitJumpMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new OrbitGame();
    }
}
