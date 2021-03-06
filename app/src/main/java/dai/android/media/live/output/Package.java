package dai.android.media.live.output;

public abstract class Package {

    public static enum PacketType {
        Audio,
        Video
    }

    private byte[] buffer;

    public abstract PacketType getType();

    public final byte[] getBuffer() {
        return buffer;
    }

    public final void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }
}
