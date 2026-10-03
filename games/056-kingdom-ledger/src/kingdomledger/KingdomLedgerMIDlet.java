package kingdomledger;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Kingdom Ledger - MIDlet entry point. */
public class KingdomLedgerMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new LedgerGame();
    }
}
