package retrosnake;

import gamekit.Game;
import gamekit.GameMIDlet;

public class RetroSnakeMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new SnakeGame();
    }
}
