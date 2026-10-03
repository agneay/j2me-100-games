package morsesignal;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Morse Signal - MIDlet entry point. */
public class MorseSignalMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new MorseGame();
    }
}
