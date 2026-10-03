package monsterduel;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Monster Duel - MIDlet entry point. */
public class MonsterDuelMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new DuelGame();
    }
}
