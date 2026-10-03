package tilemerge;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Tile Merge - MIDlet entry point. */
public class TileMergeMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new MergeGame();
    }
}
