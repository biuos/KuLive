package dai.android.media.live.output;

import java.util.concurrent.LinkedBlockingQueue;

import dai.android.media.live.Package;
import dai.android.media.live.rtmp.RtmpClient;

public abstract class AbstractLive {

    static {
        System.loadLibrary("KuLive");
    }

    protected final LinkedBlockingQueue<Package> pktQueue = new LinkedBlockingQueue<>();
    protected final RtmpClient rtmp;
    protected volatile boolean started = false;

    protected abstract void work();

    private final Thread workThread = new Thread(() -> {
        started = true;
        work();
    });

    public AbstractLive(String url) {
        rtmp = new RtmpClient(url);
    }

    protected abstract void subStart();

    public final void start() {
        workThread.start();
        subStart();
    }


    protected abstract void subStop();

    public final void stop() {
        started = false;
        subStop();
    }


    protected abstract void subRelease();

    public void release() {
        rtmp.release();
        subRelease();
    }

    public final void addPackage(Package pkg) {
        if (!started) return;
        pktQueue.add(pkg);
    }


    protected void writeAudioPackage(OutAPackage pkt) {
    }

    protected void writeVideoPackage(OutVPackage pkt) {
        if (null == pkt)
            return;

        rtmp.writeH264data(pkt.getBuffer(), pkt.getTimestamp());
    }
}
