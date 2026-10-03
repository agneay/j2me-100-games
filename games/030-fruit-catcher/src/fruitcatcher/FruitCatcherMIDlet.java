package fruitcatcher;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Fruit Catcher - MIDlet entry point. */
public class FruitCatcherMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new CatcherGame();
    }
}
