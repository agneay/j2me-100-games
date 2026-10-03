package deepcrawl;

import gamekit.Game;
import gamekit.GameMIDlet;

/** Deep Crawl 3D - MIDlet entry point. */
public class DeepCrawl3DMIDlet extends GameMIDlet {
    protected Game createGame() {
        return new CrawlGame();
    }
}
