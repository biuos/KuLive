package dai.android.media.live.rtmp;

public class RtmpClient {
    private long mHandler = 0L;

    private final String mUrl;
    private IRtmpCallback mCallback;


    /**
     * E.4.1 FLV Tag, page 75
     */
    // 8 = audio
    private static final int RTMP_TYPE_AUDIO = 8;

    // 9 = video
    private static final int RTMP_TYPE_VIDEO = 9;

    // 18 = script data
    private static final int RTMP_TYPE_SCRIPT = 18;


    public RtmpClient(String url) {
        this(url, null);
    }

    public RtmpClient(String url, IRtmpCallback cb) {
        mUrl = url;
        mCallback = cb;
    }

    public void connect(boolean publish) {
        if (mHandler <= 0) {
            mHandler = nativeConnect(mUrl, publish);
        }
    }

    public void release() {
        nativeRelease(mHandler);
        mHandler = 0L;
    }

    public void audioPacketWrite(byte[] data, long timestamp) {
        nativePacketWrite(mHandler, RTMP_TYPE_AUDIO, data, data.length, timestamp);
    }

    public void videoPacketWrite(byte[] data, long timestamp) {
        nativePacketWrite(mHandler, RTMP_TYPE_VIDEO, data, data.length, timestamp);
    }


    /**
     * write an audio raw frame to srs.
     * not similar to h.264 video, the audio never aggregated, always
     * encoded one frame by one, so this api is used to write a frame.
     *
     * @param sound_format Format of SoundData. The following values are defined:
     *                     0 = Linear PCM, platform endian
     *                     1 = ADPCM
     *                     2 = MP3
     *                     3 = Linear PCM, little endian
     *                     4 = Nellymoser 16 kHz mono
     *                     5 = Nellymoser 8 kHz mono
     *                     6 = Nellymoser
     *                     7 = G.711 A-law logarithmic PCM
     *                     8 = G.711 mu-law logarithmic PCM
     *                     9 = reserved
     *                     10 = AAC
     *                     11 = Speex
     *                     14 = MP3 8 kHz
     *                     15 = Device-specific sound
     *                     Formats 7, 8, 14, and 15 are reserved.
     *                     AAC is supported in Flash Player 9,0,115,0 and higher.
     *                     Speex is supported in Flash Player 10 and higher.
     * @param sound_rate   Sampling rate. The following values are defined:
     *                     0 = 5.5 kHz
     *                     1 = 11 kHz
     *                     2 = 22 kHz
     *                     3 = 44 kHz
     * @param sound_size   Size of each audio sample. This parameter only pertains to
     *                     uncompressed formats. Compressed formats always decode
     *                     to 16 bits internally.
     *                     0 = 8-bit samples
     *                     1 = 16-bit samples
     * @param sound_type   Mono or stereo sound
     *                     0 = Mono sound
     *                     1 = Stereo sound
     * @param timestamp    The timestamp of audio.
     * @return 0, success; otherswise, failed.
     * @example /trunk/research/librtmp/srs_aac_raw_publish.c
     * @example /trunk/research/librtmp/srs_audio_raw_publish.c
     * @remark for aac, the frame must be in ADTS format.
     * @remark for aac, only support profile 1-4, AAC main/LC/SSR/LTP,
     * @see aac-mp4a-format-ISO_IEC_14496-3+2001.pdf, page 75, 1.A.2.2 ADTS
     * @see aac-mp4a-format-ISO_IEC_14496-3+2001.pdf, page 23, 1.5.1.1 Audio object type
     * @see https://github.com/ossrs/srs/issues/212
     * @see E.4.2.1 AUDIODATA of video_file_format_spec_v10_1.pdf
     */
    public void audioWriteRawFrame(int sound_format, int sound_rate, int sound_size, int sound_type,
                                   int timestamp,
                                   byte[] raw, int length) {
        nativeAudioWriteRawFrame(mHandler, sound_format, sound_rate, sound_size, sound_type, timestamp, raw, length);
    }


    public void videoH264WriteRawFrame(byte[] rawData, long dts, long pts) {
        nativeVideoH264WriteRawFrame(mHandler, rawData, rawData.length, dts, pts);
    }


    private native long nativeConnect(String url, boolean publish);

    // see https://github.com/ossrs/srs/wiki/v3_CN_SrsLibrtmp#export-srs-librtmp
    // srs_audio_write_raw_frame
    private native void nativeAudioWriteRawFrame(long pointer,
                                                 int sound_format,
                                                 int sound_rate,
                                                 int sound_size,
                                                 int sound_type,
                                                 int timestamp,
                                                 byte[] raw,
                                                 int length);

    private native void nativeVideoH264WriteRawFrame(long pointer, byte[] raw, int length, long dts, long pts);

    private native void nativePacketWrite(long pointer, int type, byte[] data, int length, long timestamp);

    private native void nativeRelease(long pointer);

    private void nativeInfoCall(int what, String message) {
        if (mCallback != null) {
            mCallback.onInfo(what, message);
        }
    }


}
