package com.irfancen.musicbot.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.concurrent.*;

public class TrackScheduler extends AudioEventAdapter {
    public final AudioPlayer player;
    public final BlockingQueue<AudioTrack> queue;
    public boolean repeating = false;
    public final Guild guild;
    private ScheduledFuture<?> timer;

    public TrackScheduler(AudioPlayer player, Guild guild) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
        this.guild = guild;
    }

    public void queue(AudioTrack track) {
        if (!this.player.startTrack(track, true)) {
            this.queue.offer(track);
        }
        // Resets the timer
        timer.cancel(true);
    }

    public void nextTrack() {
        this.player.startTrack(this.queue.poll(), false);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (queue.isEmpty()) {
             timer = Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                // Only disconnect if nothing has been played during the time.
                if (player.getPlayingTrack() == null) {
                    this.player.startTrack(null, false);

                    AudioManager audioManager = this.guild.getAudioManager();
                    audioManager.closeAudioConnection();
                }
            }, 5, TimeUnit.MINUTES);
        }

        if (endReason.mayStartNext) {
            if (this.repeating) {
                this.player.startTrack(track.makeClone(), false);
                return;
            }
            nextTrack();
        }
    }
}
