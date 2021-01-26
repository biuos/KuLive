package dai.android.media.live.rtmp

import android.media.MediaFormat

object Packet {

    fun intToByteArrayFull(dst: ByteArray, pos: Int, integer: Int) {
        dst[pos]     = (integer shr 24 and 0xFF).toByte()
        dst[pos + 1] = (integer shr 16 and 0xFF).toByte()
        dst[pos + 2] = (integer shr 8  and 0xFF).toByte()
        dst[pos + 3] = (integer        and 0xFF).toByte()
    }

    fun intToByteArrayTwoByte(dst: ByteArray, pos: Int, integer: Int) {
        dst[pos]     = (integer shr 8 and 0xFF).toByte()
        dst[pos + 1] = (integer       and 0xFF).toByte()
    }

    object H264Packet {
        fun generateAVCDecoderConfigurationRecord(mediaFormat: MediaFormat): ByteArray {
            val spsByteBuff = mediaFormat.getByteBuffer("csd-0")
            checkNotNull(spsByteBuff)

            val ppsByteBuff = mediaFormat.getByteBuffer("csd-1")
            checkNotNull(ppsByteBuff)

            spsByteBuff.position(4)


            ppsByteBuff.position(4)

            val spsLength = spsByteBuff.remaining()
            val ppsLength = ppsByteBuff.remaining()
            val length = 11 + spsLength + ppsLength
            val result = ByteArray(length)
            spsByteBuff[result, 8, spsLength]
            ppsByteBuff[result, 8 + spsLength + 3, ppsLength]


            //UB[8]configurationVersion
            //UB[8]AVCProfileIndication
            //UB[8]profile_compatibility
            //UB[8]AVCLevelIndication
            //UB[8]lengthSizeMinusOne
            result[0] = 0x01
            result[1] = result[9]
            result[2] = result[10]
            result[3] = result[11]
            result[4] = 0xFF.toByte()


            // UB[8]numOfSequenceParameterSets
            // UB[16]sequenceParameterSetLength
            result[5] = 0xE1.toByte()
            intToByteArrayTwoByte(result, 6, spsLength)


            // UB[8]numOfPictureParameterSets
            // UB[16]pictureParameterSetLength
            val pos = 8 + spsLength
            result[pos] = 0x01.toByte()
            intToByteArrayTwoByte(result, pos + 1, ppsLength)
            return result
        }
    }

    object FLVPacket {
        const val FLV_TAG_LENGTH = 11
        const val FLV_VIDEO_TAG_LENGTH = 5
        const val FLV_AUDIO_TAG_LENGTH = 2
        const val FLV_TAG_FOOTER_LENGTH = 4
        const val NALU_HEADER_LENGTH = 4

        fun fillFLVVideoTag(
            dst: ByteArray,
            pos: Int,
            isAVCSequenceHeader: Boolean,
            isIDR: Boolean,
            readDataLength: Int
        ) {
            //FrameType&CodecID
            dst[pos] = if (isIDR) 0x17.toByte() else 0x27.toByte()
            //AVCPacketType
            dst[pos + 1] = if (isAVCSequenceHeader) 0x00.toByte() else 0x01.toByte()
            //LAKETODO CompositionTime
            dst[pos + 2] = 0x00
            dst[pos + 3] = 0x00
            dst[pos + 4] = 0x00
            if (!isAVCSequenceHeader) {
                //NALU HEADER
                intToByteArrayFull(dst, pos + 5, readDataLength)
            }
        }

        fun fillFLVAudioTag(dst: ByteArray, pos: Int, isAACSequenceHeader: Boolean) {
            // UB[4] 10=AAC
            // UB[2] 3=44kHz
            // UB[1] 1=16-bit
            // UB[1] 0=MonoSound
            dst[pos] = 0xAE.toByte()
            dst[pos + 1] = if (isAACSequenceHeader) 0x00.toByte() else 0x01.toByte()
        }
    }
}
