package game.engine.views;

import javafx.scene.media.AudioClip;
import java.net.URL;

/**
 * SoundManager — loads and plays all game sound effects.
 *
 * PUT THESE FILES in src/game/engine/views/sounds/
 *   roll.wav     — dice rolling sound
 *   door.wav     — door creak
 *   card.wav     — card flip
 *   win.wav      — victory fanfare
 *   freeze.wav   — freeze/ice sound
 *   powerup.wav  — power activation
 *   energy.wav   — energy gain/loss
 */
public class SoundManager {

    private static AudioClip rollClip;
    private static AudioClip doorClip;
    private static AudioClip cardClip;
    private static AudioClip winClip;
    private static AudioClip freezeClip;
    private static AudioClip powerupClip;
    private static AudioClip energyClip;
    private static AudioClip bonusClip;
    public static void load() {
        rollClip    = load("sounds/roll.wav");
        doorClip    = load("sounds/door.wav");
        cardClip    = load("sounds/card.wav");
        winClip     = load("sounds/win.wav");
        freezeClip  = load("sounds/freeze.wav");
        powerupClip = load("sounds/powerup.wav");
        energyClip  = load("sounds/energy.wav");
        bonusClip = load("sounds/bonus.wav");
    }

    private static AudioClip load(String path) {
        try {
            URL url = SoundManager.class.getResource(path);
            if (url != null) return new AudioClip(url.toExternalForm());
        } catch (Exception e) {
            System.out.println("Sound not found: " + path);
        }
        return null;
    }

    public static void playRoll()    { play(rollClip);    }
    public static void playDoor()    { play(doorClip);    }
    public static void playCard()    { play(cardClip);    }
    public static void playWin()     { play(winClip);     }
    public static void playFreeze()  { play(freezeClip);  }
    public static void playPowerup() { play(powerupClip); }
    public static void playEnergy()  { play(energyClip);  }
    public static void playBonus()  { play(bonusClip);  }
    private static void play(AudioClip clip) {
        if (clip != null) clip.play();
    }
}