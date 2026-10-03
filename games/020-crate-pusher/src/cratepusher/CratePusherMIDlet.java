package cratepusher;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Crate Pusher - MIDlet entry point. */
public class CratePusherMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new CrateGame();
    }
}
