package com.noisefit_commans.utils.bleUtils;



import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CRPScanRecordParser {
    private static final int SERVICE_DATA_TYPE = 0x16;
    private static final int SERVICE_DATA_LENGTH = 8;

    private static final int FIRMWARE_TYPE_POSITION = 2;
    private static final int FIRMWARE_TYPE_LENGTH = 3;
    private static final String FIRMWARE_TYPE_FILLING = "0";

    private static final int PLATFORM_POSITION = 5;
    private static final int PLATFORM_LENGTH = 1;

    private static final int CHIP_ID_POSITION = 6;
    private static final int CHIP_ID_LENGTH = 1;

    private static final int FUNCTION_POSITION = 7;
    private static final int FUNCTION_LENGTH = 2;

    /**
     * Parse band scan record
     *
     * @param scanRecord  the band scan record
     * @return CRPScanRecordInfo
     */
    public static CRPScanRecordInfo parseScanRecord(byte[] scanRecord) {
        if (scanRecord == null) {
            return null;
        }

        ByteBuffer buffer = ByteBuffer.wrap(scanRecord).order(ByteOrder.LITTLE_ENDIAN);
        while (buffer.remaining() > 2) {
            byte length = buffer.get();
            if (length == 0) {
                break;
            }

            byte type = buffer.get();
            length -= 1;
            switch (type) {
                case SERVICE_DATA_TYPE: // 从16 bit UUID Service解析出固件版本等信息
                    if (length < SERVICE_DATA_LENGTH) {
                        return null;
                    }

                    byte[] serviceDataBytes = new byte[length];
                    buffer.get(serviceDataBytes, 0, length);

                    byte[] firmwareTypeBytes = new byte[FIRMWARE_TYPE_LENGTH];
                    System.arraycopy(serviceDataBytes, FIRMWARE_TYPE_POSITION,
                            firmwareTypeBytes, 0, FIRMWARE_TYPE_LENGTH);
                    String firmwareType = new String(firmwareTypeBytes);
                    firmwareType.replace(FIRMWARE_TYPE_FILLING, "");

                    byte[] platformBytes = new byte[PLATFORM_LENGTH];
                    System.arraycopy(serviceDataBytes, PLATFORM_POSITION,
                            platformBytes, 0, PLATFORM_LENGTH);
                    int platform = platformBytes[0];

                    byte[] chipIdBytes = new byte[CHIP_ID_LENGTH];
                    System.arraycopy(serviceDataBytes, CHIP_ID_POSITION,
                            chipIdBytes, 0, CHIP_ID_LENGTH);
                    int chipId = chipIdBytes[0];

                    int functionLength = length - FUNCTION_POSITION;
                    byte[] functionBytes = new byte[functionLength];
                    System.arraycopy(serviceDataBytes, FUNCTION_POSITION,
                            functionBytes, 0, functionBytes.length);
                    int function;
                    if (1 < functionLength) {
                        function = functionBytes[1];
                    } else {
                        function = functionBytes[0];
                    }

                    CRPScanRecordInfo scanRecordInfo = new CRPScanRecordInfo();
                    scanRecordInfo.setFirmwareType(firmwareType);
                    scanRecordInfo.setPlatform(CRPScanRecordInfo.McuPlatform.getInstance(platform));
                    scanRecordInfo.setChipId(chipId);
                    scanRecordInfo.setFunction(CRPScanRecordInfo.BandFunction.getInstance(function));
                    return scanRecordInfo;
                default: // skip
                    break;
            }
            int newPosition = buffer.position() + length;
            if (length > 0 && newPosition < buffer.limit()) {
                buffer.position(newPosition);
            } else {
                return null;
            }
        }
        return null;
    }

    private static int twoBytes2int(byte byteHigh, byte byteLow) {
        return ((byteHigh & 0xFF) << 8) + (byteLow & 0xFF);
    }
}
