package boxclaim;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Box Claim - MIDlet entry point. */
public class BoxClaimMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new BoxGame();
    }
}
