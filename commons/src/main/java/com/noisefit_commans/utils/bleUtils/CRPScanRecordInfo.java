package com.noisefit_commans.utils.bleUtils;

public class CRPScanRecordInfo {

    public enum BandFunction {
        FUNC_NORMAL(0),
        FUNC_TALK(1 << 15),
        FUNC_GPS(1 << 14),
        FUNC_MUSIC(1 << 13),
        FUNC_LYRIC(1 << 12);

        private int value;

        BandFunction(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static BandFunction getInstance(int value) {
            switch (value) {
                case 1 << 15:
                    return FUNC_TALK;
                case 1 << 14:
                    return FUNC_GPS;
                case 1 << 13:
                    return FUNC_MUSIC;
                case 1 << 12:
                    return FUNC_LYRIC;
                default:
                    return FUNC_NORMAL;
            }
        }

    }

    public enum McuPlatform {
        PLATFORM_NONE(0),
        PLATFORM_NORDIC(1),
        PLATFORM_HUNTERSUN(2),
        PLATFORM_REALTEK(3),
        PLATFORM_GOODIX(4);

        private int value;

        McuPlatform(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static McuPlatform getInstance(int value) {
            switch (value) {
                case 1:
                    return PLATFORM_NORDIC;
                case 2:
                    return PLATFORM_HUNTERSUN;
                case 3:
                    return PLATFORM_REALTEK;
                case 4:
                    return PLATFORM_GOODIX;
                default:
                    return PLATFORM_NONE;
            }
        }
    }

    public enum NordicChip {
        NRF_NONE(0),
        NRF_51822(1),
        NRF_52832(2),
        NRF_52810(3),
        NRF_52840(4);

        private int value;

        NordicChip(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static NordicChip getInstance(int value) {
            switch (value) {
                case 1:
                    return NRF_51822;
                case 2:
                    return NRF_52832;
                case 3:
                    return NRF_52810;
                case 4:
                    return NRF_52840;
                default:
                    return NRF_NONE;
            }
        }
    }

    public enum HuntersunChip {
        HS_NONE(0),
        HS_6620D_A3(1),
        HS_6620D_A4(2),
        HS_6621(3);

        private int value;

        HuntersunChip(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static HuntersunChip getInstance(int value) {
            switch (value) {
                case 1:
                    return HS_6620D_A3;
                case 2:
                    return HS_6620D_A4;
                case 3:
                    return HS_6621;
                default:
                    return HS_NONE;
            }
        }
    }

    public enum RealtekChip {
        RTL_NONE(0),
        RTL_8762C(1),
        RTL_8762D(2);

        private int value;

        RealtekChip(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static RealtekChip getInstance(int value) {
            switch (value) {
                case 1:
                    return RTL_8762C;
                case 2:
                    return RTL_8762D;
                default:
                    return RTL_NONE;
            }
        }
    }

    public enum GoodixChip {
        GR_NONE(0),
        GR_5515(1);

        private int value;

        GoodixChip(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static GoodixChip getInstance(int value) {
            switch (value) {
                case 1:
                    return GR_5515;
                default:
                    return GR_NONE;
            }
        }
    }

    // 固件版本
    private String firmwareType;
    // 平台
    private McuPlatform platform;
    // MCU 型号
    private int chipId;
    // 手环功能
    private BandFunction function;

    public CRPScanRecordInfo() {
    }

    public String getFirmwareType() {
        return firmwareType;
    }

    public void setFirmwareType(String firmwareType) {
        this.firmwareType = firmwareType;
    }

    public McuPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(McuPlatform platform) {
        this.platform = platform;
    }

    public int getChipId() {
        return chipId;
    }

    public void setChipId(int chipId) {
        this.chipId = chipId;
    }

    public BandFunction getFunction() {
        return function;
    }

    public void setFunction(BandFunction function) {
        this.function = function;
    }
}
