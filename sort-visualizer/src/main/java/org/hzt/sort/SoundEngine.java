package org.hzt.sort;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

final class SoundEngine {

    private final MidiChannel channel;

    public SoundEngine() {
        try {
            final Synthesizer synth = MidiSystem.getSynthesizer();
            synth.open();
            channel = synth.getChannels()[0];
            // 80 is a "Square Lead" - great for retro tech sounds
            channel.programChange(80);
        } catch (MidiUnavailableException e) {
            throw new IllegalStateException("Unable to initialize MIDI", e);
        }
    }

    public void playNote(int value) {
        // Map bar value (10-460) to MIDI range (30-100)
        int note = 30 + (int) ((value / 460.0) * 70);

        // Use a short, punchy duration
        channel.noteOn(note, 80); // Velocity 80
        Thread.ofVirtual().start(() -> {
            try {
                Thread.sleep(20);
            } catch (InterruptedException _) {
                Thread.currentThread().interrupt();
            }
            channel.noteOff(note);
        });
    }
}
