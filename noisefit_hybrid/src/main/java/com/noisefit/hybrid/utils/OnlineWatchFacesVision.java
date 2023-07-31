package com.noisefit.hybrid.utils;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;

import javax.inject.Inject;

import cn.appscomm.bluetooth.BluetoothVar;
import cn.appscomm.bluetooth.implement.MBluetooth;
import cn.appscomm.bluetooth.interfaces.IBluetoothResultCallback;
import cn.appscomm.bluetooth.interfaces.IUpdateProgressCallBack;
import cn.appscomm.bluetoothsdk.interfaces.ResultCallBack;
import cn.appscomm.bluetoothsdk.utils.FileUtil;
import cn.appscomm.bluetoothsdk.utils.ParseUtil;

public class OnlineWatchFacesVision {

    @Inject
    public OnlineWatchFacesVision() {
    }


    public void otaOnlineWatchface(int id, String watchfaceBinLocalPath, ResultCallBack callback) {

        if (watchfaceBinLocalPath == null) {
            if (callback != null) {
                callback.onFail(2000);
            }

            return;
        }

        byte[] binBytes = FileUtil.fileToBytes(watchfaceBinLocalPath);
        if (binBytes == null) {
            if (callback != null) {
                callback.onFail(2008);
            }

            return;
        }

        IBluetoothResultCallback getOtaAddressCallback = new IBluetoothResultCallback() {
            @Override
            public void onSuccess(String s) {
                BluetoothVar var = MBluetooth.INSTANCE.getBluetoothVarByMAC(s);
                if (var == null) {
                    // error callback
                    if (callback != null) {
                        callback.onFail(2001);
                    }

                }

                long address = var.watchFaceOTAAddress;
                if (address <= 0) {
                    if (callback != null) {
                        callback.onFail(2004);
                    }

                    return;
                }

                byte[] addrBytes = ParseUtil.intToByteArray((int) address, 4);

                byte[] finalBytes = new byte[addrBytes.length + binBytes.length];
                System.arraycopy(addrBytes, 0, finalBytes, 0, addrBytes.length);
                System.arraycopy(binBytes, 0, finalBytes, addrBytes.length, binBytes.length);

                int zero = 0;
                byte[] zeroBytes = ParseUtil.intToByteArray(zero, 4);
                byte[] endBytes = new byte[8];
                System.arraycopy(zeroBytes, 0, endBytes, 0, 4);
                try {
                    byte[] strBytes = "OLWF".getBytes("utf-8");
                    System.arraycopy(strBytes, 0, endBytes, 4, 4);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                IUpdateProgressCallBack otaP = new IUpdateProgressCallBack() {
                    @Override
                    public void curUpdateProgress(int i) {
                        if (callback != null) {
                            Object[] param;
                            (param = new Object[1])[0] = i;
                            callback.onSuccess(190808082, param);

                        }
                    }

                    @Override
                    public void curUpdateMax(int i) {
                        if (callback != null) {
                            Object[] param;
                            (param = new Object[1])[0] = i;
                            callback.onSuccess(190808083, param);

                        }
                    }

                    @Override
                    public void updateResult(boolean b) {
                        if (callback != null) {
                            Object[] param;
                            (param = new Object[1])[0] = b;
                            callback.onSuccess(190808084, param);
                        }
                    }
                };

                String mac = cn.appscomm.bluetoothsdk.app.a.a;
                if (TextUtils.isEmpty(mac)) {
                    if (callback != null) {
                        callback.onFail(190808084);
                    }
                    return;
                }
                MBluetooth.INSTANCE.updateImage(mac, addrBytes, binBytes, endBytes, otaP);
            }

            @Override
            public void onFail(String s) {
                if (callback != null) {
                    callback.onFail(2005);
                }
            }
        };
        String[] var9;
        (var9 = new String[1])[0] = cn.appscomm.bluetoothsdk.app.a.a;
        MBluetooth.INSTANCE.getCustomizeWatchFaceAddressEx(getOtaAddressCallback, ParseUtil.intToByteArray(id, 4), 2, var9);
    }


}
