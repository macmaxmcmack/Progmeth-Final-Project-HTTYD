package httyd.gui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Plays the sound effects and the background music. All sounds are synthesised in memory
 * (simple chiptune tones), so no audio files are needed. If a file {@code /sounds/<name>.wav}
 * exists in the resources it is used instead. If the computer has no sound device the game
 * simply stays silent.
 */
public class SoundPlayer {
    private static final float RATE = 22050f;

    /** The sound effects. */
    public enum Sound {
        CLICK, HIT, CRIT, DODGE, HEAL, FAINT, WIN, LOSE, EGG_CRACK, EGG_OPEN, ULTIMATE, STATUS
    }

    private final Map<Sound, byte[]> effects = new EnumMap<>(Sound.class);
    private Clip music;
    private boolean muted;
    private boolean available = true;

    /** Creates the player and prepares all sounds. */
    public SoundPlayer() {
        for (Sound s : Sound.values()) {
            byte[] data = loadResource(s.name().toLowerCase());
            effects.put(s, data != null ? data : synthesize(s));
        }
    }

    /** @return true if sound is switched off */
    public boolean isMuted() {
        return muted;
    }

    /**
     * Switches sound on or off.
     *
     * @param muted true to silence everything
     */
    public void setMuted(boolean muted) {
        this.muted = muted;
        if (muted) {
            stopMusic();
        }
    }

    /**
     * Plays a sound effect once.
     *
     * @param sound the effect
     */
    public void play(Sound sound) {
        if (muted || !available) {
            return;
        }
        Clip clip = open(effects.get(sound), 0.9f);
        if (clip != null) {
            clip.start();
            clip.addLineListener(e -> {
                if (e.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
                    clip.close();
                }
            });
        }
    }

    /** Starts the looping background music. */
    public void startMusic() {
        if (muted || !available || music != null) {
            return;
        }
        music = open(composeMusic(), 0.35f);
        if (music != null) {
            music.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    /** Stops the background music. */
    public void stopMusic() {
        if (music != null) {
            music.stop();
            music.close();
            music = null;
        }
    }

    private Clip open(byte[] pcm, float volume) {
        try {
            AudioFormat format = new AudioFormat(RATE, 16, 1, true, false);
            AudioInputStream stream = new AudioInputStream(new ByteArrayInputStream(pcm), format, pcm.length / 2);
            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            try {
                javax.sound.sampled.FloatControl gain =
                        (javax.sound.sampled.FloatControl) clip.getControl(javax.sound.sampled.FloatControl.Type.MASTER_GAIN);
                gain.setValue(20f * (float) Math.log10(Math.max(0.0001, volume)));
            } catch (IllegalArgumentException ignored) {
                // no gain control on this line
            }
            return clip;
        } catch (LineUnavailableException | IOException | RuntimeException e) {
            available = false;
            return null;
        }
    }

    private byte[] loadResource(String name) {
        try (InputStream in = SoundPlayer.class.getResourceAsStream("/sounds/" + name + ".wav")) {
            if (in == null) {
                return null;
            }
            AudioInputStream source = AudioSystem.getAudioInputStream(new java.io.BufferedInputStream(in));
            AudioFormat target = new AudioFormat(RATE, 16, 1, true, false);
            AudioInputStream converted = AudioSystem.getAudioInputStream(target, source);
            return converted.readAllBytes();
        } catch (IOException | UnsupportedAudioFileException | RuntimeException e) {
            return null;
        }
    }

    // ------------------------------------------------------------------ synthesis

    private static byte[] synthesize(Sound s) {
        Tone t = new Tone();
        switch (s) {
            case CLICK -> t.note(880, 0.05, Wave.SQUARE, 0.4);
            case HIT -> {
                t.sweep(300, 90, 0.12, Wave.NOISE, 0.7);
                t.note(160, 0.06, Wave.SQUARE, 0.5);
            }
            case CRIT -> {
                t.sweep(700, 120, 0.16, Wave.NOISE, 0.8);
                t.note(520, 0.07, Wave.SQUARE, 0.6);
                t.note(780, 0.1, Wave.SQUARE, 0.6);
            }
            case DODGE -> t.sweep(400, 1200, 0.14, Wave.SINE, 0.5);
            case HEAL -> {
                t.note(523, 0.08, Wave.SQUARE, 0.4);
                t.note(659, 0.08, Wave.SQUARE, 0.4);
                t.note(784, 0.14, Wave.SQUARE, 0.4);
            }
            case FAINT -> t.sweep(500, 80, 0.5, Wave.SQUARE, 0.5);
            case WIN -> {
                for (int f : new int[]{523, 659, 784, 1047}) {
                    t.note(f, 0.13, Wave.SQUARE, 0.45);
                }
                t.note(1047, 0.35, Wave.SQUARE, 0.45);
            }
            case LOSE -> {
                for (int f : new int[]{392, 349, 311, 262}) {
                    t.note(f, 0.22, Wave.TRIANGLE, 0.5);
                }
            }
            case EGG_CRACK -> {
                t.note(200, 0.03, Wave.NOISE, 0.8);
                t.note(140, 0.05, Wave.NOISE, 0.6);
            }
            case EGG_OPEN -> {
                t.sweep(300, 1500, 0.25, Wave.SQUARE, 0.4);
                t.note(1319, 0.2, Wave.SQUARE, 0.4);
            }
            case ULTIMATE -> {
                t.sweep(200, 1400, 0.35, Wave.SAW, 0.5);
                t.sweep(1400, 100, 0.25, Wave.NOISE, 0.7);
            }
            case STATUS -> {
                t.note(330, 0.07, Wave.TRIANGLE, 0.5);
                t.note(247, 0.1, Wave.TRIANGLE, 0.5);
            }
            default -> { }
        }
        return t.toBytes();
    }

    private static byte[] composeMusic() {
        Tone t = new Tone();
        int[][] chords = {{262, 330, 392}, {220, 262, 330}, {175, 220, 262}, {196, 247, 294}};
        double step = 0.18;
        for (int bar = 0; bar < 8; bar++) {
            int[] chord = chords[bar % 4];
            for (int i = 0; i < 8; i++) {
                int note = chord[i % 3] * (i >= 6 ? 2 : 1);
                t.note(note, step, Wave.TRIANGLE, 0.28);
            }
        }
        return t.toBytes();
    }

    private enum Wave { SINE, SQUARE, TRIANGLE, SAW, NOISE }

    /** Builds a mono 16 bit sample buffer note by note. */
    private static final class Tone {
        private final java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        private final java.util.Random noise = new java.util.Random(1);

        void note(double freq, double seconds, Wave wave, double volume) {
            sweep(freq, freq, seconds, wave, volume);
        }

        void sweep(double from, double to, double seconds, Wave wave, double volume) {
            int n = (int) (seconds * RATE);
            double phase = 0;
            for (int i = 0; i < n; i++) {
                double p = (double) i / n;
                double freq = from + (to - from) * p;
                phase += freq / RATE;
                double frac = phase - Math.floor(phase);
                double v = switch (wave) {
                    case SINE -> Math.sin(phase * 2 * Math.PI);
                    case SQUARE -> frac < 0.5 ? 1 : -1;
                    case TRIANGLE -> 4 * Math.abs(frac - 0.5) - 1;
                    case SAW -> 2 * frac - 1;
                    case NOISE -> noise.nextDouble() * 2 - 1;
                };
                double envelope = Math.min(1.0, p * 20) * Math.min(1.0, (1 - p) * 6);
                int sample = (int) (v * envelope * volume * 12000);
                out.write(sample & 0xFF);
                out.write((sample >> 8) & 0xFF);
            }
        }

        byte[] toBytes() {
            return out.toByteArray();
        }
    }
}
