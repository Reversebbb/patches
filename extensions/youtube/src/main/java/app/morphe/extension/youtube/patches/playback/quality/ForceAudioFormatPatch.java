/*
 * Force AAC 140 — Morphe extension
 *
 * Put this file in:
 *   extensions/youtube/src/main/java/app/morphe/extension/youtube/patches/playback/quality/ForceAudioFormatPatch.java
 *
 * Morphe extensions are Java only (no Kotlin stdlib in the extensions APK).
 *
 * What it does:
 *   The adaptive-formats hook hands us every stream YouTube is willing to serve
 *   for this video (video + audio-only). If the setting is on, we keep all video
 *   streams untouched and throw away every audio-only stream except itag 140.
 *   YouTube then has no choice but to play 140.
 */

package app.morphe.extension.youtube.patches.playback.quality;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import app.morphe.extension.shared.Logger;
import app.morphe.extension.youtube.patches.VideoFormat.FormatInterface;
import app.morphe.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class ForceAudioFormatPatch {

    /**
     * itag 140 = AAC-LC, 128 kbps, 44.1 kHz stereo. Available to free accounts.
     * Change this constant if you want a different target, e.g. 251 for Opus ~130 kbps.
     */
    private static final int TARGET_ITAG = 140;

    private static final boolean FORCE_AUDIO_FORMAT_140 = Settings.FORCE_AUDIO_FORMAT_140.get();

    /**
     * Injection point. Called from VideoFormat.privateHookAdaptiveFormats().
     *
     * @return the formats list, with audio-only streams filtered down to {@link #TARGET_ITAG}
     *         when the setting is enabled and that stream exists.
     */
    public static List<FormatInterface> filterAdaptiveFormats(
            @NonNull String videoId,
            @NonNull List<FormatInterface> adaptiveFormats
    ) {
        if (!FORCE_AUDIO_FORMAT_140) {
            return adaptiveFormats;
        }

        try {
            FormatInterface target = null;

            for (FormatInterface format : adaptiveFormats) {
                if (format.patch_getItag() == TARGET_ITAG) {
                    target = format;
                    break;
                }
            }

            if (target == null) {
                Logger.printDebug(() -> "videoId: " + videoId
                        + ", itag " + TARGET_ITAG + " not offered, leaving formats unchanged");
                return adaptiveFormats;
            }

            final FormatInterface finalTarget = target;
            List<FormatInterface> filtered = new ArrayList<>(adaptiveFormats.size());
            for (FormatInterface format : adaptiveFormats) {
                String mimeType = format.patch_getMimeType();
                boolean isAudioOnly = mimeType != null && mimeType.startsWith("audio/");

                // Keep every video stream, and only the target audio stream.
                if (!isAudioOnly || format == finalTarget) {
                    filtered.add(format);
                }
            }

            Logger.printDebug(() -> "videoId: " + videoId
                    + ", forcing itag " + TARGET_ITAG
                    + ", formats " + adaptiveFormats.size() + " -> " + filtered.size());

            return filtered;
        } catch (Exception ex) {
            Logger.printException(() -> "Failed to force audio format", ex);
            return adaptiveFormats;
        }
    }
}
