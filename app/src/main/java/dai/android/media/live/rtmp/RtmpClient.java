package dai.android.media.live.rtmp;

public class RtmpClient {


    private long mPtr = 0L;
    private IRtmpCallback mCallback;

    private final String mUrl;

    public RtmpClient(String url) {
        this(url, null);
    }

    public RtmpClient(String url, IRtmpCallback cb) {
        mUrl = url;
        mCallback = cb;
    }

    public void connect(boolean publish) {
        if (mPtr <= 0) {
            mPtr = _nativeConnect(mUrl, publish);
        }
    }

    public void release() {
        _nativeRelease(mPtr);
        mPtr = 0L;
    }

    public boolean writeAACspec(byte[] data) {
        if (mPtr <= 0) {
            return false;
        }

        return _aac_spec_send(mPtr, data, data.length);
    }

    public boolean writeAACdata(byte[] data, long timestamp) {
        if (mPtr <= 0) {
            return false;
        }
        return _aac_data_send(mPtr, data, data.length, timestamp);
    }

    public boolean writeSPSandPPS(byte[] sps, byte[] pps, long timestamp) {
        if (mPtr <= 0) {
            return false;
        }
        return _sps_pps_send(mPtr, sps, sps.length, pps, pps.length, timestamp);
    }

    public boolean writeH264data(byte[] data, long timestamp) {
        if (mPtr <= 0) {
            return false;
        }
        return _h264_data_send(mPtr, data, data.length, timestamp);
    }


    private native boolean _aac_spec_send(long ptr, byte[] data, int length);

    private native boolean _aac_data_send(long ptr, byte[] data, int length, long timestamp);

    private native boolean _sps_pps_send(long ptr, byte[] sps, int spsLength, byte[] pps, int ppsLength, long timestamp);

    private native boolean _h264_data_send(long ptr, byte[] data, int length, long timestamp);

    private native long _nativeConnect(String url, boolean publish);

    private native void _nativeRelease(long ptr);

    private void nativeInfoCall(int what, String message) {
        if (mCallback != null) {
            mCallback.onInfo(what, message);
        }
    }


}
