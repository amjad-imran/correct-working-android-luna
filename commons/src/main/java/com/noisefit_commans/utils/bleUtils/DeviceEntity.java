package com.noisefit_commans.utils.bleUtils;


import android.os.ParcelUuid;
import android.util.Log;

import java.util.Locale;
import java.util.UUID;

public class DeviceEntity implements Comparable<DeviceEntity> {
    public String address;
    public String name;
    public int rssi;
    public ScanRecord scanRecord;
    public DeviceScanQrCodeBean.DeviceRadioBroadcastBean mDeviceRadioBroadcastBean;

    public CRPScanRecordInfo.McuPlatform mcuPlatform;
    @Override
    public int compareTo(DeviceEntity o) {
        int num1 = this.rssi;
        int num2 = o.rssi;
        int aa = 0;
        if (num2 > num1) {
            aa = (num2 - num1);
        }
        if (num2 < num1) {
            aa = (num2 - num1);
        }
        return aa;
    }

    public static final UUID SCAN_RECORD = UUID.fromString("0000fe78-0000-1000-8000-00805f9b34fb");


    public static DeviceScanQrCodeBean.DeviceRadioBroadcastBean getScanRecordModel(android.bluetooth.le.ScanRecord scanRecord) {
        DeviceScanQrCodeBean.DeviceRadioBroadcastBean mDeviceScanQrCodeBean = null;
        if (scanRecord != null) {
            if (scanRecord.getServiceData() != null) {
                ParcelUuid parcelUuid = new ParcelUuid(SCAN_RECORD);
                byte[] serviceData = scanRecord.getServiceData().get(parcelUuid);
                if (serviceData != null) {
                    final StringBuilder stringBuilder = new StringBuilder(serviceData.length);
                    for (byte byteChar : serviceData) {
                        stringBuilder.append(String.format(Locale.ENGLISH, "%02X", byteChar));
                    }
                    Log.i("DeviceEntity", "stringBuilder.toString() = " + stringBuilder);
                    mDeviceScanQrCodeBean = new DeviceScanQrCodeBean.DeviceRadioBroadcastBean(stringBuilder.toString());
                }
            }
        }
        return mDeviceScanQrCodeBean;
    }

}
