/*
 * Force audio format 140 — Morphe patch (FIXED)
 *
 * FIX: patch was "universal" (no compatiblePackages) but default = true, which
 * Morphe's loader rejects — checkUniversalPatchDefaults() throws PatchException
 * ("Universal patches must be declared with default false"), the whole bundle
 * fails to load, and Manager shows 0 patches. Declaring compatibleWith(YouTube)
 * makes it a non-universal patch, so default = true is allowed.
 *
 * Target path:
 *   patches/src/main/kotlin/app/morphe/patches/youtube/video/quality/ForceAudioFormatPatch.kt
 */

package app.morphe.patches.youtube.video.quality

import app.morphe.patcher.patch.patch
import app.morphe.patches.shared.misc.settings.preference.SwitchPreference
import app.morphe.patches.youtube.misc.extension.sharedExtensionPatch
import app.morphe.patches.youtube.misc.settings.settingsPatch
import app.morphe.patches.youtube.shared.Constants.COMPATIBILITY_YOUTUBE
import app.morphe.patches.youtube.video.format.hookAdaptiveFormat
import app.morphe.patches.youtube.video.format.videoFormatPatch

private const val EXTENSION_CLASS =
    "Lapp/morphe/extension/youtube/patches/playback/quality/ForceAudioFormatPatch;"

val forceAudioFormatPatch = patch(
    name = "Force audio format 140",
    description = "Adds an option to force the audio-only stream to itag 140 " +
            "(AAC-LC 128 kbps) instead of the codec and bitrate YouTube picks by itself."
) {
    // Required: a patch with no compatible packages and default = true fails to load.
    compatibleWith(COMPATIBILITY_YOUTUBE)

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
