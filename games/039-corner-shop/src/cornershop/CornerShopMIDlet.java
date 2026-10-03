package cornershop;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Corner Shop - MIDlet entry point. */
public class CornerShopMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new ShopGame();
    }
}
