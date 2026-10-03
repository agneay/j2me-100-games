package echotones;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Echo Tones - MIDlet entry point. */
public class EchoTonesMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new EchoGame();
    }
}
