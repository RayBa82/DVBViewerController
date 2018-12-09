package org.dvbviewer.controller.player;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.audio.AudioCapabilities;
import com.google.android.exoplayer2.audio.AudioProcessor;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.ext.ffmpeg.FfmpegAudioRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecInfo;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.video.MediaCodecVideoRenderer;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.util.ArrayList;
import java.util.List;

public class DMSRenderersFactory extends DefaultRenderersFactory {

    private static final String TAG = DMSRenderersFactory.class.getName();

    public DMSRenderersFactory(Context context) {
        super(context, null, EXTENSION_RENDERER_MODE_ON, DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS);
    }

    /**
     * Builds video renderers for use by the player.
     *
     * @param context The {@link Context} associated with the player.
     * @param drmSessionManager An optional {@link DrmSessionManager}. May be null if the player
     *     will not be used for DRM protected playbacks.
     * @param allowedVideoJoiningTimeMs The maximum duration in milliseconds for which video
     *     renderers can attempt to seamlessly join an ongoing playback.
     * @param eventHandler A handler associated with the main thread's looper.
     * @param eventListener An event listener.
     * @param extensionRendererMode The extension renderer mode.
     * @param out An array to which the built renderers should be appended.
     */
    protected void buildVideoRenderers(Context context,
                                       DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, long allowedVideoJoiningTimeMs,
                                       Handler eventHandler, VideoRendererEventListener eventListener,
                                       @DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode, ArrayList<Renderer> out) {

            Log.d(TAG, "Adding MediaCodecVideoRenderer");
            out.add(new MediaCodecVideoRenderer(
                    context,
                    MediaCodecSelector.DEFAULT,
                    allowedVideoJoiningTimeMs,
                    drmSessionManager,
                    false,
                    eventHandler,
                    eventListener,
                    MAX_DROPPED_VIDEO_FRAME_COUNT_TO_NOTIFY));
    }

    /**
     * Builds audio renderers for use by the player.
     *
     * @param context The {@link Context} associated with the player.
     * @param drmSessionManager An optional {@link DrmSessionManager}. May be null if the player
     *     will not be used for DRM protected playbacks.
     * @param audioProcessors An array of {@link AudioProcessor}s that will process PCM audio
     *     buffers before output. May be empty.
     * @param eventHandler A handler to use when invoking event listeners and outputs.
     * @param eventListener An event listener.
     * @param extensionRendererMode The extension renderer mode.
     * @param out An array to which the built renderers should be appended.
     */
    protected void buildAudioRenderers(Context context,
                                       DrmSessionManager<FrameworkMediaCrypto> drmSessionManager,
                                       AudioProcessor[] audioProcessors, Handler eventHandler,
                                       AudioRendererEventListener eventListener, @ExtensionRendererMode int extensionRendererMode,
                                       ArrayList<Renderer> out) {
        AudioCapabilities audioCapabilities = AudioCapabilities.getCapabilities(context);

        // FFMpeg Audio Decoder
        final boolean enablePassthroughDecoder = false;

        // Native Audio Decoders
        Log.d(TAG, "Adding MediaCodecAudioRenderer");
        MediaCodecSelector mediaCodecSelector = buildMediaCodecSelector(enablePassthroughDecoder);
        out.add(new MediaCodecAudioRenderer(context, mediaCodecSelector, drmSessionManager,
                true, eventHandler, eventListener, audioCapabilities));

        // FFMpeg Audio Decoder
        final boolean enableFfmpegAudioRenderer = true;

        if (enableFfmpegAudioRenderer) {
            Log.d(TAG, "Adding FfmpegAudioRenderer");
            out.add(new FfmpegAudioRenderer(eventHandler, eventListener, audioProcessors));
        }
    }

    /**
     * Builds a MediaCodecSelector that can explicitly disable audio passthrough
     *
     * @param enablePassthroughDecoder
     * @return
     */
    private MediaCodecSelector buildMediaCodecSelector(final boolean enablePassthroughDecoder) {
        return new MediaCodecSelector() {

            @Override
            public List<MediaCodecInfo> getDecoderInfos(String mimeType, boolean requiresSecureDecoder) throws MediaCodecUtil.DecoderQueryException {
                return MediaCodecUtil.getDecoderInfos(mimeType, requiresSecureDecoder);
            }

            @Override
            public MediaCodecInfo getPassthroughDecoderInfo() throws MediaCodecUtil.DecoderQueryException {
                if (enablePassthroughDecoder) {
                    return MediaCodecUtil.getPassthroughDecoderInfo();
                }
                return null;
            }
        };
    }
}