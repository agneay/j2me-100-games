package gridtactics;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Grid Tactics - MIDlet entry point. */
public class GridTacticsMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new TacticsGame();
    }
}
