package com.noisefit.colorfit_pro.dataConversion;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u00fe\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0012\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001e\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\t2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\nH\u0002J\u001a\u0010\u000e\u001a\u00020\u000f2\b\u0010\u0010\u001a\u0004\u0018\u00010\u00112\u0006\u0010\u0012\u001a\u00020\fH\u0002J\u0010\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0014H\u0002J\u000e\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u0019J\u000e\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001dJ\u0016\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020 0\u001f2\b\u0010!\u001a\u0004\u0018\u00010\"J\u001a\u0010#\u001a\b\u0012\u0004\u0012\u00020$0\u001f2\f\u0010%\u001a\b\u0012\u0004\u0012\u00020$0\tJ\u001c\u0010&\u001a\u00020\'2\f\u0010(\u001a\b\u0012\u0004\u0012\u00020)0\t2\u0006\u0010*\u001a\u00020\u000fJ\u0016\u0010+\u001a\u00020,2\u0006\u0010-\u001a\u00020)2\u0006\u0010*\u001a\u00020\u000fJ\u0016\u0010.\u001a\u00020/2\u000e\u00100\u001a\n\u0012\u0004\u0012\u000201\u0018\u00010\tJ\u000e\u00102\u001a\u0002032\u0006\u00104\u001a\u000205J\u000e\u00106\u001a\u0002072\u0006\u00108\u001a\u000209J\u0016\u0010:\u001a\u00020;2\u0006\u0010<\u001a\u00020\n2\u0006\u0010=\u001a\u00020>J\u0018\u0010?\u001a\u00020@2\u0006\u0010A\u001a\u00020B2\b\u0010C\u001a\u0004\u0018\u00010DJ\u0010\u0010E\u001a\u00020\u000f2\u0006\u0010F\u001a\u00020\fH\u0002J\u0010\u0010G\u001a\u00020\u000f2\u0006\u0010H\u001a\u00020\fH\u0002J\u0016\u0010I\u001a\u00020\f2\u000e\u0010J\u001a\n\u0012\u0004\u0012\u00020\n\u0018\u00010\tJ\u0016\u0010K\u001a\b\u0012\u0004\u0012\u00020L0\t2\b\u0010A\u001a\u0004\u0018\u00010MJ\u0014\u0010N\u001a\u00020O2\f\u0010P\u001a\b\u0012\u0004\u0012\u00020Q0\tJ\u0014\u0010R\u001a\b\u0012\u0004\u0012\u00020S0\t2\u0006\u0010A\u001a\u00020TJ\u0010\u0010U\u001a\u00020\u00192\b\u0010V\u001a\u0004\u0018\u00010\u0017J\u0014\u0010W\u001a\u00020O2\f\u0010P\u001a\b\u0012\u0004\u0012\u00020X0\tJ\u001c\u0010Y\u001a\b\u0012\u0004\u0012\u00020Z0\u001f2\u000e\u0010!\u001a\n\u0012\u0004\u0012\u00020\\\u0018\u00010[J\u000e\u0010]\u001a\u00020\u001d2\u0006\u0010\u001c\u001a\u00020\u001bJ\u000e\u0010^\u001a\u0002092\u0006\u0010_\u001a\u000207R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\u0004\u00a8\u0006`"}, d2 = {"Lcom/noisefit/colorfit_pro/dataConversion/DataConverter;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "getContext", "()Landroid/content/Context;", "setContext", "binStringToBooleanArray", "", "", "my_byte", "", "isEnable", "binary", "", "bytes", "", "radix", "celToFeh", "", "cels", "convertQRPayment", "Lcom/crrepa/ble/conn/bean/CRPElectronicCardInfo;", "qRPayment", "Lcom/noisefit_commans/models/UPIQRCode;", "convertVibration", "Lcom/crrepa/ble/conn/type/CRPVibrationStrength;", "data", "Lcom/noisefit_commans/models/VibrationIntensityEnum;", "convertWatchStoreToWatchFacesList", "Ljava/util/ArrayList;", "Lcom/noisefit_commans/models/WatchFace;", "p0", "Lcom/crrepa/ble/conn/bean/CRPWatchFaceStoreInfo;", "formatContactList", "Lcom/noisefit_commans/models/Contact;", "contactBeanList", "formatFutureWeatherData", "Lcom/crrepa/ble/conn/bean/CRPFutureWeatherInfo;", "weatherDataList", "Lcom/noisefit_commans/models/WeatherData;", "unit", "formatWeatherData", "Lcom/crrepa/ble/conn/bean/CRPTodayWeatherInfo;", "weatherData", "getAlarmList", "Lcom/noisefit_commans/models/AlarmsList;", "crpDeviceAlarmsList", "Lcom/crrepa/ble/conn/bean/CRPAlarmInfo;", "getCRPUserInfo", "Lcom/crrepa/ble/conn/bean/CRPUserInfo;", "userInfo", "Lcom/noisefit_commans/models/UserInfo;", "getMenstrualSettings", "Lcom/noisefit_commans/models/MenstrualData;", "cRPPhysiologcalPeriodInfo", "Lcom/crrepa/ble/conn/bean/CRPPhysiologcalPeriodInfo;", "getSedentaryReminderPeriod", "Lcom/noisefit_commans/models/WalkReminderData;", "status", "crpSedentaryReminderPeriodInfo", "Lcom/crrepa/ble/conn/bean/CRPSedentaryReminderPeriodInfo;", "getSleepData", "Lcom/noisefit_commans/models/SleepData;", "info", "Lcom/crrepa/ble/conn/bean/CRPSleepInfo;", "colorFitDevice", "Lcom/noisefit_commans/models/ColorFitDevice;", "getSportType", "sport", "getTimeFromMinutes", "minutes", "parseAlarmDaysToBytes", "alarmList", "parseBloodOxygenHistory", "Lcom/noisefit_commans/models/BloodOxygenBreakup;", "Lcom/crrepa/ble/conn/bean/CRPBloodOxygenInfo;", "parseCRPTrainingSportsMode", "Lcom/noisefit_commans/models/SportsModeListGPS;", "list", "Lcom/crrepa/ble/conn/bean/CRPTrainingInfo;", "parseHeartHistory", "Lcom/noisefit_commans/models/HeartRate;", "Lcom/crrepa/ble/conn/bean/CRPHeartRateInfo;", "parseQRPayment", "cRPElectronicCardInfo", "parseSportsMode", "Lcom/crrepa/ble/conn/bean/CRPMovementHeartRateInfo;", "parseStressData", "Lcom/noisefit_commans/models/StressDataBreakup;", "", "Lcom/crrepa/ble/conn/bean/CRPHistoryStressInfo;", "parseVibration", "setMenstrualData", "menstrualData", "noisefit_colorfit_pro_debug"})
public final class DataConverter {
    @org.jetbrains.annotations.NotNull
    private android.content.Context context;
    
    @javax.inject.Inject
    public DataConverter(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final android.content.Context getContext() {
        return null;
    }
    
    public final void setContext(@org.jetbrains.annotations.NotNull
    android.content.Context p0) {
    }
    
    private final double celToFeh(double cels) {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.VibrationIntensityEnum parseVibration(@org.jetbrains.annotations.NotNull
    com.crrepa.ble.conn.type.CRPVibrationStrength data) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.crrepa.ble.conn.type.CRPVibrationStrength convertVibration(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.VibrationIntensityEnum data) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.ArrayList<com.noisefit_commans.models.WatchFace> convertWatchStoreToWatchFacesList(@org.jetbrains.annotations.Nullable
    com.crrepa.ble.conn.bean.CRPWatchFaceStoreInfo p0) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.ArrayList<com.noisefit_commans.models.Contact> formatContactList(@org.jetbrains.annotations.NotNull
    java.util.List<com.noisefit_commans.models.Contact> contactBeanList) {
        return null;
    }
    
    private final java.lang.String getSportType(int sport) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.SportsModeListGPS parseSportsMode(@org.jetbrains.annotations.NotNull
    java.util.List<? extends com.crrepa.ble.conn.bean.CRPMovementHeartRateInfo> list) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.SportsModeListGPS parseCRPTrainingSportsMode(@org.jetbrains.annotations.NotNull
    java.util.List<? extends com.crrepa.ble.conn.bean.CRPTrainingInfo> list) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.noisefit_commans.models.HeartRate> parseHeartHistory(@org.jetbrains.annotations.NotNull
    com.crrepa.ble.conn.bean.CRPHeartRateInfo info) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.noisefit_commans.models.BloodOxygenBreakup> parseBloodOxygenHistory(@org.jetbrains.annotations.Nullable
    com.crrepa.ble.conn.bean.CRPBloodOxygenInfo info) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.SleepData getSleepData(@org.jetbrains.annotations.NotNull
    com.crrepa.ble.conn.bean.CRPSleepInfo info, @org.jetbrains.annotations.Nullable
    com.noisefit_commans.models.ColorFitDevice colorFitDevice) {
        return null;
    }
    
    private final java.lang.String getTimeFromMinutes(int minutes) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.crrepa.ble.conn.bean.CRPFutureWeatherInfo formatFutureWeatherData(@org.jetbrains.annotations.NotNull
    java.util.List<com.noisefit_commans.models.WeatherData> weatherDataList, @org.jetbrains.annotations.NotNull
    java.lang.String unit) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.crrepa.ble.conn.bean.CRPTodayWeatherInfo formatWeatherData(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.WeatherData weatherData, @org.jetbrains.annotations.NotNull
    java.lang.String unit) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.crrepa.ble.conn.bean.CRPPhysiologcalPeriodInfo setMenstrualData(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.MenstrualData menstrualData) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.crrepa.ble.conn.bean.CRPUserInfo getCRPUserInfo(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.UserInfo userInfo) {
        return null;
    }
    
    private final java.util.List<java.lang.Boolean> binStringToBooleanArray(int my_byte, boolean isEnable) {
        return null;
    }
    
    private final java.lang.String binary(byte[] bytes, int radix) {
        return null;
    }
    
    public final int parseAlarmDaysToBytes(@org.jetbrains.annotations.Nullable
    java.util.List<java.lang.Boolean> alarmList) {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.ArrayList<com.noisefit_commans.models.StressDataBreakup> parseStressData(@org.jetbrains.annotations.Nullable
    java.util.List<com.crrepa.ble.conn.bean.CRPHistoryStressInfo> p0) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.AlarmsList getAlarmList(@org.jetbrains.annotations.Nullable
    java.util.List<? extends com.crrepa.ble.conn.bean.CRPAlarmInfo> crpDeviceAlarmsList) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.UPIQRCode parseQRPayment(@org.jetbrains.annotations.Nullable
    com.crrepa.ble.conn.bean.CRPElectronicCardInfo cRPElectronicCardInfo) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.crrepa.ble.conn.bean.CRPElectronicCardInfo convertQRPayment(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.UPIQRCode qRPayment) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.WalkReminderData getSedentaryReminderPeriod(boolean status, @org.jetbrains.annotations.NotNull
    com.crrepa.ble.conn.bean.CRPSedentaryReminderPeriodInfo crpSedentaryReminderPeriodInfo) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.MenstrualData getMenstrualSettings(@org.jetbrains.annotations.NotNull
    com.crrepa.ble.conn.bean.CRPPhysiologcalPeriodInfo cRPPhysiologcalPeriodInfo) {
        return null;
    }
}