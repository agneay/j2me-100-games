package factoryline;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Factory Line - MIDlet entry point. */
public class FactoryLineMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new FactoryGame();
    }
}
