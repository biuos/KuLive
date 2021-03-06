package dai.android.media.live.output;

public class AudioPackage extends Package {
    @Override
    public PacketType getType() {
        return PacketType.Audio;
    }
}
