package railswitcher;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Rail Switcher - MIDlet entry point. */
public class RailSwitcherMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new RailGame();
    }
}
