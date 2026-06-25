package com.ryanheise.just_audio;

import androidx.media3.common.C;
import androidx.media3.common.audio.AudioProcessor.AudioFormat;
import androidx.media3.common.audio.BaseAudioProcessor;
import java.nio.ByteBuffer;

/**
 * An {@link androidx.media3.common.audio.AudioProcessor} that applies stereo balance (pan).
 *
 * <p>Pan range: -1.0 (full left) to 0.0 (centre) to 1.0 (full right). Only active for stereo
 * 16-bit PCM or float PCM audio; other formats are passed through unchanged.
 */
public class BalanceAudioProcessor extends BaseAudioProcessor {

    private volatile float pan = 0f;

    public void setPan(float pan) {
        this.pan = Math.max(-1f, Math.min(1f, pan));
    }

    @Override
    protected AudioFormat onConfigure(AudioFormat inputFormat)
            throws UnhandledAudioFormatException {
        if (inputFormat.channelCount != 2
                || (inputFormat.encoding != C.ENCODING_PCM_16BIT
                        && inputFormat.encoding != C.ENCODING_PCM_FLOAT)) {
            return AudioFormat.NOT_SET;
        }
        return inputFormat;
    }

    @Override
    public void queueInput(ByteBuffer inputBuffer) {
        int remaining = inputBuffer.remaining();
        if (remaining == 0) {
            return;
        }

        float currentPan = pan;
        float leftGain = Math.min(1f, 1f - currentPan);
        float rightGain = Math.min(1f, 1f + currentPan);

        ByteBuffer outputBuffer = replaceOutputBuffer(remaining);

        if (inputAudioFormat.encoding == C.ENCODING_PCM_16BIT) {
            while (inputBuffer.hasRemaining()) {
                short left = inputBuffer.getShort();
                short right = inputBuffer.getShort();
                outputBuffer.putShort((short) (left * leftGain));
                outputBuffer.putShort((short) (right * rightGain));
            }
        } else {
            while (inputBuffer.hasRemaining()) {
                float left = inputBuffer.getFloat();
                float right = inputBuffer.getFloat();
                outputBuffer.putFloat(left * leftGain);
                outputBuffer.putFloat(right * rightGain);
            }
        }

        outputBuffer.flip();
    }
}
