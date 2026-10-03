package dungeonquest;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Dungeon Quest - MIDlet entry point. */
public class DungeonQuestMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new DungeonGame();
    }
}
