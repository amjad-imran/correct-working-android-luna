package com.noisefit_cf2.dataconversions;

import java.lang.System;

@kotlin.Suppress(names = {"NAME_SHADOWING"})
@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\u0018\u0000 \u00032\u00020\u0001:\u0001\u0003B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0004"}, d2 = {"Lcom/noisefit_cf2/dataconversions/Colorfit2DataConverter;", "", "()V", "Companion", "noisefit_colorfit2_debug"})
public final class Colorfit2DataConverter {
    @org.jetbrains.annotations.NotNull
    public static final com.noisefit_cf2.dataconversions.Colorfit2DataConverter.Companion Companion = null;
    
    @javax.inject.Inject
    public Colorfit2DataConverter() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u00b6\u0003\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0018\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0015\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010!\n\u0002\b\b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\t\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u00042\u0006\u0010\u0006\u001a\u00020\u0007J\u001c\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u00042\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\n\u001a\u00020\u000bJ\u000e\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fJ\u000e\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0013J\u000e\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0017J\u000e\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u001bJ\u000e\u0010\u0018\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u0019J\u000e\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\u001c\u001a\u00020\u0019J\u000e\u0010\u001f\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u001eJ\u000e\u0010 \u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u001eJ\u000e\u0010 \u001a\u00020\u001e2\u0006\u0010\u001c\u001a\u00020\u0019J\u000e\u0010!\u001a\u00020\"2\u0006\u0010#\u001a\u00020$J\u000e\u0010%\u001a\u00020&2\u0006\u0010\'\u001a\u00020(J\u000e\u0010)\u001a\u00020*2\u0006\u0010+\u001a\u00020,J\u000e\u0010-\u001a\u00020.2\u0006\u0010+\u001a\u00020,J\u000e\u0010/\u001a\u0002002\u0006\u0010\u0016\u001a\u00020\u0017J\u000e\u00101\u001a\u0002022\u0006\u00103\u001a\u000204J\u0014\u00105\u001a\b\u0012\u0004\u0012\u0002060\u00042\u0006\u00103\u001a\u000204J\u000e\u00107\u001a\u0002082\u0006\u0010\u0016\u001a\u00020\u0017J\u000e\u00109\u001a\u00020:2\u0006\u0010;\u001a\u00020<J\u001a\u0010=\u001a\u0004\u0018\u00010>2\b\u0010?\u001a\u0004\u0018\u00010@2\u0006\u0010A\u001a\u00020BJ\u000e\u0010C\u001a\u00020>2\u0006\u0010D\u001a\u00020EJ\u000e\u0010F\u001a\u00020G2\u0006\u0010H\u001a\u00020IJ\u000e\u0010J\u001a\u00020K2\u0006\u0010L\u001a\u00020MJ\u000e\u0010N\u001a\u00020O2\u0006\u0010P\u001a\u00020QJ\u0018\u0010R\u001a\u00020S2\b\u0010?\u001a\u0004\u0018\u00010@2\u0006\u0010T\u001a\u00020UJ\u000e\u0010V\u001a\u00020W2\u0006\u0010X\u001a\u00020YJ\u000e\u0010Z\u001a\u00020[2\u0006\u0010\\\u001a\u00020]J\u000e\u0010^\u001a\u00020[2\u0006\u0010_\u001a\u00020`J\u000e\u0010a\u001a\u00020b2\u0006\u0010X\u001a\u00020YJ\u000e\u0010c\u001a\u00020`2\u0006\u0010d\u001a\u00020[J\u000e\u0010e\u001a\u00020]2\u0006\u0010d\u001a\u00020[J\u000e\u0010f\u001a\u00020g2\u0006\u0010X\u001a\u00020YJ\u0010\u0010h\u001a\u00020]2\b\u0010i\u001a\u0004\u0018\u00010jJ\u0014\u0010k\u001a\u00020\u00072\f\u0010l\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004J\u0016\u0010m\u001a\u00020\u00072\u000e\u0010l\u001a\n\u0012\u0004\u0012\u00020\t\u0018\u00010nJ\u000e\u0010o\u001a\u00020E2\u0006\u0010p\u001a\u00020>J\u0010\u0010q\u001a\u00020\u00132\b\u0010r\u001a\u0004\u0018\u00010\u0011J\u000e\u0010s\u001a\u00020\u00172\u0006\u0010t\u001a\u00020\u0015J\"\u0010u\u001a\u0004\u0018\u00010b2\b\u0010v\u001a\u0004\u0018\u00010w2\u000e\u0010x\u001a\n\u0012\u0004\u0012\u00020y\u0018\u00010nJ\"\u0010z\u001a\u0004\u0018\u00010b2\b\u0010v\u001a\u0004\u0018\u00010{2\u000e\u0010x\u001a\n\u0012\u0004\u0012\u00020]\u0018\u00010nJ\"\u0010|\u001a\u0004\u0018\u00010b2\b\u0010v\u001a\u0004\u0018\u00010{2\u000e\u0010x\u001a\n\u0012\u0004\u0012\u00020}\u0018\u00010\u0004J%\u0010~\u001a\b\u0012\u0004\u0012\u00020b0\u00042\f\u0010\u007f\u001a\b\u0012\u0004\u0012\u00020y0\u00042\t\u0010\u0080\u0001\u001a\u0004\u0018\u00010wJ\u0018\u0010\u0081\u0001\u001a\b\u0012\u0004\u0012\u00020b0\u00042\t\u0010\u0080\u0001\u001a\u0004\u0018\u00010{J\u0012\u0010\u0082\u0001\u001a\u00030\u0083\u00012\b\u0010p\u001a\u0004\u0018\u00010>J\u001c\u0010\u0084\u0001\u001a\u00020$2\b\u0010#\u001a\u0004\u0018\u00010\"2\t\u0010\u0085\u0001\u001a\u0004\u0018\u00010&J,\u0010\u0086\u0001\u001a\u000b\u0012\u0005\u0012\u00030\u0087\u0001\u0018\u00010\u00042\t\u0010v\u001a\u0005\u0018\u00010\u0088\u00012\u000f\u0010x\u001a\u000b\u0012\u0005\u0012\u00030\u0089\u0001\u0018\u00010nJ\u0010\u0010\u008a\u0001\u001a\u00020\u00172\u0007\u0010\u008b\u0001\u001a\u000200J&\u0010\u008c\u0001\u001a\u0005\u0018\u00010\u008d\u00012\t\u0010v\u001a\u0005\u0018\u00010\u008e\u00012\u000f\u0010x\u001a\u000b\u0012\u0005\u0012\u00030\u008f\u0001\u0018\u00010nJ.\u0010\u0090\u0001\u001a\u0005\u0018\u00010\u008d\u00012\u0006\u0010?\u001a\u00020@2\t\u0010v\u001a\u0005\u0018\u00010\u0091\u00012\u000f\u0010x\u001a\u000b\u0012\u0005\u0012\u00030\u0092\u0001\u0018\u00010nJ%\u0010\u0093\u0001\u001a\u0004\u0018\u00010g2\t\u0010v\u001a\u0005\u0018\u00010\u0094\u00012\u000f\u0010x\u001a\u000b\u0012\u0005\u0012\u00030\u0095\u0001\u0018\u00010nJ%\u0010\u0096\u0001\u001a\u0004\u0018\u00010g2\t\u0010v\u001a\u0005\u0018\u00010\u0097\u00012\u000f\u0010x\u001a\u000b\u0012\u0005\u0012\u00030\u0098\u0001\u0018\u00010nJ\u0010\u0010\u0099\u0001\u001a\u0002042\u0007\u0010\u009a\u0001\u001a\u000202J\u0013\u0010\u009b\u0001\u001a\u0002042\n\u0010\u009c\u0001\u001a\u0005\u0018\u00010\u009d\u0001J,\u0010\u009e\u0001\u001a\u000b\u0012\u0005\u0012\u00030\u009f\u0001\u0018\u00010\u00042\t\u0010v\u001a\u0005\u0018\u00010\u00a0\u00012\u000f\u0010x\u001a\u000b\u0012\u0005\u0012\u00030\u00a1\u0001\u0018\u00010nJ\u0010\u0010\u00a2\u0001\u001a\u00020\u00172\u0007\u0010\u00a3\u0001\u001a\u000208J\u000f\u0010\u00a4\u0001\u001a\u00020B2\u0006\u0010p\u001a\u00020>J\u0010\u0010\u00a5\u0001\u001a\u00020I2\u0007\u0010\u00a6\u0001\u001a\u00020GJ\u0010\u0010\u00a7\u0001\u001a\u00020M2\u0007\u0010\u008b\u0001\u001a\u00020KJ\u0012\u0010\u00a8\u0001\u001a\u00020U2\t\u0010\u00a9\u0001\u001a\u0004\u0018\u00010S\u00a8\u0006\u00aa\u0001"}, d2 = {"Lcom/noisefit_cf2/dataconversions/Colorfit2DataConverter$Companion;", "", "()V", "formatAlarms", "", "Lcom/ido/ble/protocol/model/Alarm;", "alarm", "Lcom/noisefit_commans/models/AlarmsList;", "formatAlarmsV3", "Lcom/ido/ble/protocol/model/AlarmV3;", "isDummy", "", "formatDialPlate", "Lcom/ido/ble/protocol/model/DialPlate;", "watchFace", "Lcom/noisefit_commans/models/WatchFace;", "formatDoNotDisturb", "Lcom/ido/ble/protocol/model/NotDisturbPara;", "doNotDisturb", "Lcom/noisefit_commans/models/DoNotDisturb;", "formatDrinkReminderData", "Lcom/ido/ble/protocol/model/DrinkWaterReminder;", "sedentaryData", "Lcom/noisefit_commans/models/SedentaryData;", "formatHeartRateInterval", "Lcom/noisefit_commans/models/HeartRateInterval;", "heartRateMeasureMode", "Lcom/ido/ble/protocol/model/HeartRateMeasureMode;", "heartRateInterval", "formatHeartRateIntervalActive", "Lcom/ido/ble/protocol/model/HeartRateMeasureModeV3;", "formatHeartRateIntervalActiveGet", "formatHeartRateIntervalV3", "formatMenstrualData", "Lcom/ido/ble/protocol/model/Menstrual;", "menstrualData", "Lcom/noisefit_commans/models/MenstrualData;", "formatMenstrualReminder", "Lcom/ido/ble/protocol/model/MenstrualRemind;", "menstrualReminder", "Lcom/noisefit_commans/models/MenstrualData$MenstrualReminder;", "formatMessageInfo", "Lcom/ido/ble/protocol/model/NewMessageInfo;", "appNotification", "Lcom/noisefit_commans/models/AppNotification;", "formatMessageInfoV3", "Lcom/ido/ble/protocol/model/V3MessageNotice;", "formatSedentaryData", "Lcom/ido/ble/protocol/model/LongSit;", "formatSportMode", "Lcom/ido/ble/protocol/model/QuickSportMode;", "data", "Lcom/noisefit_commans/models/SportsModeList;", "formatSportModeV3", "Lcom/ido/ble/protocol/model/SportModeSortV3$SportModeSortItemV3;", "formatStressData", "Lcom/ido/ble/protocol/model/PressureParam;", "formatSystemTime", "Lcom/ido/ble/protocol/model/SystemTime;", "calender", "Ljava/util/Calendar;", "formatTime", "Lcom/ido/ble/protocol/model/Units;", "colorFitDevice", "Lcom/noisefit_commans/models/ColorFitDevice;", "timeFormat", "Lcom/noisefit_commans/models/TimeFormat;", "formatUnits", "deviceUnits", "Lcom/noisefit_commans/models/DeviceUnits;", "formatUserInfo", "Lcom/ido/ble/protocol/model/UserInfo;", "userInfo", "Lcom/noisefit_commans/models/UserInfo;", "formatWalkReminderData", "Lcom/ido/ble/protocol/model/WalkReminder;", "walkReminderData", "Lcom/noisefit_commans/models/WalkReminderData;", "formatWashHandReminderData", "Lcom/ido/ble/protocol/model/WashHandReminder;", "handWashing", "Lcom/noisefit_commans/models/HandWashing;", "formatWristSenseData", "Lcom/ido/ble/protocol/model/UpHandGesture;", "wristLiftGesture", "Lcom/noisefit_commans/models/WristLiftGesture;", "getBloodPressure", "Lcom/noisefit_commans/models/BloodPressureData;", "liveData", "Lcom/ido/ble/protocol/model/LiveData;", "getBooleanFromInt", "", "cycle", "", "getBooleanFromIntArray", "intArray", "", "getHeartRate", "Lcom/noisefit_commans/models/HeartRate;", "getIntArrayFromBooleanArray", "bArray", "getIntFromBooleanArray", "getStepsData", "Lcom/noisefit_commans/models/StepsData;", "getWeatherTypeForWatch", "type", "", "parseAlarms", "alarms", "parseAlarmsV3", "", "parseDeviceUnits", "units", "parseDoNotDisturb", "notDisturbPara", "parseDrinkWaterData", "drinkWater", "parseHeartData", "p0", "Lcom/ido/ble/data/manage/database/HealthHeartRate;", "p1", "Lcom/ido/ble/data/manage/database/HealthHeartRateItem;", "parseHeartDataV3", "Lcom/ido/ble/data/manage/database/HealthHeartRateSecond;", "parseHeartDataV3ForLast5Min", "Lcom/ido/ble/data/manage/database/HealthHeartRateSecondItem;", "parseHeartHistory", "heartItems", "healthHeartRate", "parseHeartHistoryV3", "parseLanguage", "Lcom/noisefit_commans/models/Language;", "parseMenstrualData", "menstrualRemind", "parseOxygenData", "Lcom/noisefit_commans/models/BloodOxygenBreakup;", "Lcom/ido/ble/data/manage/database/HealthSpO2;", "Lcom/ido/ble/data/manage/database/HealthSpO2Item;", "parseSedentaryData", "longSit", "parseSleepData", "Lcom/noisefit_commans/models/SleepData;", "Lcom/ido/ble/data/manage/database/HealthSleep;", "Lcom/ido/ble/data/manage/database/HealthSleepItem;", "parseSleepDataV3", "Lcom/ido/ble/data/manage/database/HealthSleepV3;", "Lcom/ido/ble/data/manage/database/HealthSleepV3Item;", "parseSportsData", "Lcom/ido/ble/data/manage/database/HealthSport;", "Lcom/ido/ble/data/manage/database/HealthSportItem;", "parseSportsDataV3", "Lcom/ido/ble/data/manage/database/HealthSportV3;", "Lcom/ido/ble/data/manage/database/HealthSportV3Item;", "parseSportsModeInfo", "modes", "parseSportsModeInfoV3", "functionInfo", "Lcom/ido/ble/protocol/model/SportModeSortV3;", "parseStressData", "Lcom/noisefit_commans/models/StressDataBreakup;", "Lcom/ido/ble/data/manage/database/HealthPressure;", "Lcom/ido/ble/data/manage/database/HealthPressureItem;", "parseStressParam", "stress", "parseTimeFormat", "parseUserInfo", "info", "parseWalkReminderData", "parseWristSenseData", "gesture", "noisefit_colorfit2_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.noisefit_commans.models.BloodPressureData getBloodPressure(@org.jetbrains.annotations.NotNull
        com.ido.ble.protocol.model.LiveData liveData) {
            return null;
        }
        
        public final int getWeatherTypeForWatch(@org.jetbrains.annotations.Nullable
        java.lang.String type) {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.noisefit_commans.models.HeartRate getHeartRate(@org.jetbrains.annotations.NotNull
        com.ido.ble.protocol.model.LiveData liveData) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.noisefit_commans.models.StepsData getStepsData(@org.jetbrains.annotations.NotNull
        com.ido.ble.protocol.model.LiveData liveData) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.noisefit_commans.models.AlarmsList parseAlarms(@org.jetbrains.annotations.NotNull
        java.util.List<? extends com.ido.ble.protocol.model.Alarm> alarms) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.noisefit_commans.models.AlarmsList parseAlarmsV3(@org.jetbrains.annotations.Nullable
        java.util.List<com.ido.ble.protocol.model.AlarmV3> alarms) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.util.List<com.ido.ble.protocol.model.Alarm> formatAlarms(@org.jetbrains.annotations.NotNull
        com.noisefit_commans.models.AlarmsList alarm) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.util.List<com.ido.ble.protocol.model.AlarmV3> formatAlarmsV3(@org.jetbrains.annotations.NotNull
        com.noisefit_commans.models.AlarmsList alarm, boolean isDummy) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.ido.ble.protocol.model.Menstrual formatMenstrualData(@org.jetbrains.annotations.NotNull
        com.noisefit_commans.models.MenstrualData menstrualData) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.ido.ble.protocol.model.MenstrualRemind formatMenstrualReminder(@org.jetbrains.annotations.NotNull
        com.noisefit_commans.models.MenstrualData.MenstrualReminder menstrualReminder) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.noisefit_commans.models.MenstrualData parseMenstrualData(@org.jetbrains.annotations.Nullable
        com.ido.ble.protocol.model.Menstrual menstrualData, @org.jetbrains.annotations.Nullable
        com.ido.ble.protocol.model.MenstrualRemind menstrualRemind) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.ido.ble.protocol.model.HeartRateMeasureMode formatHeartRateInterval(@org.jetbrains.annotations.NotNull
        com.noisefit_commans.models.HeartRateInterval heartRateInterval) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.ido.ble.protocol.model.HeartRateMeasureModeV3 formatHeartRateIntervalV3(@org.jetbrains.annotations.NotNull
        com.noisefit_commans.models.HeartRateInterval heartRateInterval) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.ido.ble.protocol.model.HeartRateMeasureModeV3 formatHeartRateIntervalActive(@org.jetbrains.annotations.NotNull
        com.noisefit_commans.models.HeartRateInterval heartRateInterval) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.noisefit_commans.models.HeartRateInterval formatHeartRateInterval(@org.jetbrains.annotations.NotNull
        com.ido.ble.protocol.model.HeartRateMeasureMode heartRateMeasureMode) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.noisefit_commans.models.HeartRateInterval formatHeartRateIntervalV3(@org.jetbrains.annotations.NotNull
        com.ido.ble.protocol.model.HeartRateMeasureModeV3 heartRateMeasureMode) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.noisefit_commans.models.HeartRateInterval formatHeartRateIntervalActiveGet(@org.jetbrains.annotations.NotNull
        com.ido.ble.protocol.model.HeartRateMeasureModeV3 heartRateMeasureMode) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.noisefit_commans.models.DoNotDisturb parseDoNotDisturb(@org.jetbrains.annotations.Nullable
        com.ido.ble.protocol.model.NotDisturbPara notDisturbPara) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.ido.ble.protocol.model.NotDisturbPara formatDoNotDisturb(@org.jetbrains.annotations.NotNull
        com.noisefit_commans.models.DoNotDisturb doNotDisturb) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.noisefit_commans.models.SedentaryData parseSedentaryData(@org.jetbrains.annotations.NotNull
        com.ido.ble.protocol.model.LongSit longSit) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.noisefit_commans.models.WalkReminderData parseWalkReminderData(@org.jetbrains.annotations.NotNull
        com.ido.ble.protocol.model.WalkReminder longSit) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.noisefit_commans.models.SedentaryData parseDrinkWaterData(@org.jetbrains.annotations.NotNull
        com.ido.ble.protocol.model.DrinkWaterReminder drinkWater) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.noisefit_commans.models.SedentaryData parseStressParam(@org.jetbrains.annotations.NotNull
        com.ido.ble.protocol.model.PressureParam stress) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.ido.ble.protocol.model.LongSit formatSedentaryData(@org.jetbrains.annotations.NotNull
        com.noisefit_commans.models.SedentaryData sedentaryData) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.ido.ble.protocol.model.WalkReminder formatWalkReminderData(@org.jetbrains.annotations.NotNull
        com.noisefit_commans.models.WalkReminderData walkReminderData) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.ido.ble.protocol.model.DrinkWaterReminder formatDrinkReminderData(@org.jetbrains.annotations.NotNull
        com.noisefit_commans.models.SedentaryData sedentaryData) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.ido.ble.protocol.model.PressureParam formatStressData(@org.jetbrains.annotations.NotNull
        com.noisefit_commans.models.SedentaryData sedentaryData) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.ido.ble.protocol.model.WashHandReminder formatWashHandReminderData(@org.jetbrains.annotations.NotNull
        com.noisefit_commans.models.HandWashing handWashing) {
            return null;
        }
        
        public final int getIntFromBooleanArray(@org.jetbrains.annotations.NotNull
        boolean[] bArray) {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull
        public final int[] getIntArrayFromBooleanArray(@org.jetbrains.annotations.NotNull
        boolean[] bArray) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final boolean[] getBooleanFromInt(int cycle) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final boolean[] getBooleanFromIntArray(@org.jetbrains.annotations.NotNull
        int[] intArray) {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final com.noisefit_commans.models.StepsData parseSportsData(@org.jetbrains.annotations.Nullable
        com.ido.ble.data.manage.database.HealthSport p0, @org.jetbrains.annotations.Nullable
        java.util.List<com.ido.ble.data.manage.database.HealthSportItem> p1) {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final com.noisefit_commans.models.StepsData parseSportsDataV3(@org.jetbrains.annotations.Nullable
        com.ido.ble.data.manage.database.HealthSportV3 p0, @org.jetbrains.annotations.Nullable
        java.util.List<com.ido.ble.data.manage.database.HealthSportV3Item> p1) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.util.List<com.noisefit_commans.models.HeartRate> parseHeartHistory(@org.jetbrains.annotations.NotNull
        java.util.List<? extends com.ido.ble.data.manage.database.HealthHeartRateItem> heartItems, @org.jetbrains.annotations.Nullable
        com.ido.ble.data.manage.database.HealthHeartRate healthHeartRate) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.util.List<com.noisefit_commans.models.HeartRate> parseHeartHistoryV3(@org.jetbrains.annotations.Nullable
        com.ido.ble.data.manage.database.HealthHeartRateSecond healthHeartRate) {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final com.noisefit_commans.models.HeartRate parseHeartData(@org.jetbrains.annotations.Nullable
        com.ido.ble.data.manage.database.HealthHeartRate p0, @org.jetbrains.annotations.Nullable
        java.util.List<com.ido.ble.data.manage.database.HealthHeartRateItem> p1) {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final com.noisefit_commans.models.HeartRate parseHeartDataV3(@org.jetbrains.annotations.Nullable
        com.ido.ble.data.manage.database.HealthHeartRateSecond p0, @org.jetbrains.annotations.Nullable
        java.util.List<java.lang.Integer> p1) {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final com.noisefit_commans.models.HeartRate parseHeartDataV3ForLast5Min(@org.jetbrains.annotations.Nullable
        com.ido.ble.data.manage.database.HealthHeartRateSecond p0, @org.jetbrains.annotations.Nullable
        java.util.List<? extends com.ido.ble.data.manage.database.HealthHeartRateSecondItem> p1) {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final com.noisefit_commans.models.SleepData parseSleepData(@org.jetbrains.annotations.Nullable
        com.ido.ble.data.manage.database.HealthSleep p0, @org.jetbrains.annotations.Nullable
        java.util.List<com.ido.ble.data.manage.database.HealthSleepItem> p1) {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final com.noisefit_commans.models.SleepData parseSleepDataV3(@org.jetbrains.annotations.NotNull
        com.noisefit_commans.models.ColorFitDevice colorFitDevice, @org.jetbrains.annotations.Nullable
        com.ido.ble.data.manage.database.HealthSleepV3 p0, @org.jetbrains.annotations.Nullable
        java.util.List<com.ido.ble.data.manage.database.HealthSleepV3Item> p1) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.noisefit_commans.models.UserInfo parseUserInfo(@org.jetbrains.annotations.NotNull
        com.ido.ble.protocol.model.UserInfo info) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.ido.ble.protocol.model.UserInfo formatUserInfo(@org.jetbrains.annotations.NotNull
        com.noisefit_commans.models.UserInfo userInfo) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.noisefit_commans.models.Language parseLanguage(@org.jetbrains.annotations.Nullable
        com.ido.ble.protocol.model.Units units) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.noisefit_commans.models.DeviceUnits parseDeviceUnits(@org.jetbrains.annotations.NotNull
        com.ido.ble.protocol.model.Units units) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.ido.ble.protocol.model.Units formatUnits(@org.jetbrains.annotations.NotNull
        com.noisefit_commans.models.DeviceUnits deviceUnits) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.ido.ble.protocol.model.SystemTime formatSystemTime(@org.jetbrains.annotations.NotNull
        java.util.Calendar calender) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.ido.ble.protocol.model.DialPlate formatDialPlate(@org.jetbrains.annotations.NotNull
        com.noisefit_commans.models.WatchFace watchFace) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.ido.ble.protocol.model.NewMessageInfo formatMessageInfo(@org.jetbrains.annotations.NotNull
        com.noisefit_commans.models.AppNotification appNotification) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.ido.ble.protocol.model.V3MessageNotice formatMessageInfoV3(@org.jetbrains.annotations.NotNull
        com.noisefit_commans.models.AppNotification appNotification) {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final com.ido.ble.protocol.model.Units formatTime(@org.jetbrains.annotations.Nullable
        com.noisefit_commans.models.ColorFitDevice colorFitDevice, @org.jetbrains.annotations.NotNull
        com.noisefit_commans.models.TimeFormat timeFormat) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.noisefit_commans.models.TimeFormat parseTimeFormat(@org.jetbrains.annotations.NotNull
        com.ido.ble.protocol.model.Units units) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.noisefit_commans.models.WristLiftGesture parseWristSenseData(@org.jetbrains.annotations.Nullable
        com.ido.ble.protocol.model.UpHandGesture gesture) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.ido.ble.protocol.model.UpHandGesture formatWristSenseData(@org.jetbrains.annotations.Nullable
        com.noisefit_commans.models.ColorFitDevice colorFitDevice, @org.jetbrains.annotations.NotNull
        com.noisefit_commans.models.WristLiftGesture wristLiftGesture) {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.util.List<com.noisefit_commans.models.StressDataBreakup> parseStressData(@org.jetbrains.annotations.Nullable
        com.ido.ble.data.manage.database.HealthPressure p0, @org.jetbrains.annotations.Nullable
        java.util.List<com.ido.ble.data.manage.database.HealthPressureItem> p1) {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.util.List<com.noisefit_commans.models.BloodOxygenBreakup> parseOxygenData(@org.jetbrains.annotations.Nullable
        com.ido.ble.data.manage.database.HealthSpO2 p0, @org.jetbrains.annotations.Nullable
        java.util.List<com.ido.ble.data.manage.database.HealthSpO2Item> p1) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.noisefit_commans.models.SportsModeList parseSportsModeInfo(@org.jetbrains.annotations.NotNull
        com.ido.ble.protocol.model.QuickSportMode modes) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.noisefit_commans.models.SportsModeList parseSportsModeInfoV3(@org.jetbrains.annotations.Nullable
        com.ido.ble.protocol.model.SportModeSortV3 functionInfo) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.util.List<com.ido.ble.protocol.model.SportModeSortV3.SportModeSortItemV3> formatSportModeV3(@org.jetbrains.annotations.NotNull
        com.noisefit_commans.models.SportsModeList data) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.ido.ble.protocol.model.QuickSportMode formatSportMode(@org.jetbrains.annotations.NotNull
        com.noisefit_commans.models.SportsModeList data) {
            return null;
        }
    }
}