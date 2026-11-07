/*
 * Force AAC 140 — Morphe patch
 *
 * Put this file in:
 *   patches/src/main/kotlin/app/morphe/patches/youtube/video/quality/ForceAudioFormatPatch.kt
 *
 * It reuses the already-maintained adaptive-formats hook (videoFormatPatch), so
 * there is no new fingerprint to reverse engineer. That hook is the same one
 * PrioritizeVideoQualityPatch uses.
 */

package app.morphe.patches.youtube.video.quality

import app.morphe.patcher.patch.patch
import app.morphe.patches.shared.misc.settings.preference.SwitchPreference
import app.morphe.patches.youtube.misc.extension.sharedExtensionPatch
import app.morphe.patches.youtube.misc.settings.settingsPatch
import app.morphe.patches.youtube.video.format.hookAdaptiveFormat
import app.morphe.patches.youtube.video.format.videoFormatPatch

private const val EXTENSION_CLASS =
    "Lapp/morphe/extension/youtube/patches/playback/quality/ForceAudioFormatPatch;"

val forceAudioFormatPatch = patch(
    name = "Force audio format 140",
    description = "Adds an option to force the audio-only stream to itag 140 " +
            "(AAC-LC 128 kbps) instead of the codec and bitrate YouTube picks by itself."
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        videoFormatPatch,
    )

    execute {
        settingsMenuVideoQualityGroup.add(
            SwitchPreference("morphe_force_audio_format_140", summary = true)
        )

        hookAdaptiveFormat("$EXTENSION_CLASS->filterAdaptiveFormats")
    }
}
