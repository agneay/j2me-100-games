package brickbuster;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Brick Buster - MIDlet entry point. */
public class BrickBusterMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new BrickGame();
    }
}
