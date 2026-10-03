package arenachampion;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Arena Champion - MIDlet entry point. */
public class ArenaChampionMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new ArenaGame();
    }
}
