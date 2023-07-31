package com.noisefit.hybrid.utils.watchface;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import cn.appscomm.bluetooth.BluetoothVar;
import cn.appscomm.bluetooth.implement.MBluetooth;
import cn.appscomm.bluetooth.interfaces.IBluetoothResultCallback;
import cn.appscomm.bluetooth.interfaces.IUpdateProgressCallBack;
import cn.appscomm.bluetoothsdk.app.BluetoothSDK;
import cn.appscomm.bluetoothsdk.interfaces.ResultCallBack;
import cn.appscomm.bluetoothsdk.utils.ImageUtil;
import cn.appscomm.bluetoothsdk.utils.LogUtil;
import cn.appscomm.bluetoothsdk.utils.ParseUtil;

public class CustomWatchfaceManager {
    private static final CustomWatchfaceManager shared = new CustomWatchfaceManager();

    private CustomWatchfaceManager() {}

    public static CustomWatchfaceManager getInstance() {
        return shared;
    }

    public void setCustomWatchface(CustomWatchface watchface, ResultCallBack callBack) {
        if (watchface.hasWidgets()) {
            ResultCallBack setWidgetsCallback = new ResultCallBack() {
                @Override
                public void onSuccess(int i, Object[] objects) {
                    // 2. ota watchface image
                    if (objects == null || objects.length == 0) {
                        return;
                    }
                    byte[] callbackBytes = (byte[])objects[0];
                    if (callbackBytes.length == 8 && callbackBytes[5] == 0x1e && callbackBytes[6] == 0x00) {
                        // clear callback
                        BluetoothSDK.set8002CallBack(null);
                        otaCustomWatchface(watchface, callBack);
                    } else {
                        if (callBack != null) {
                            callBack.onFail(190808081);
                        }
                    }
                }

                @Override
                public void onFail(int i) {
                    BluetoothSDK.set8002CallBack(null);
                    if (callBack != null) {
                        callBack.onFail(i);
                    }
                }
            };
            // 1. set widgets info and then ota watchface image
            BluetoothSDK.set8002CallBack(setWidgetsCallback);
            byte[] bytes = watchfaceToBytes(watchface);
            BluetoothSDK.sendCustomCommand(bytes, true);
        } else {
            // ota watchface image
            otaCustomWatchface(watchface, callBack);
        }
    }

    private void otaCustomWatchface(CustomWatchface watchface, ResultCallBack callback) {

        // get ota address
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

                byte[] addrBytes = ParseUtil.intToByteArray((int)address, 4);
                byte[] imageBytes = ImageUtil.getImageByteArrayEx(watchface.getImagePath(), watchface.getSize().getWidth(), watchface.getThumbnailSize().getHeight(), true);

                int zero = 0;
                byte[] zeroBytes = ParseUtil.intToByteArray(zero, 4);
                byte[] endBytes = new byte[8];
                System.arraycopy(zeroBytes, 0, endBytes, 0, 4);
                try {
                    byte[] strBytes = "FACE".getBytes("utf-8");
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
                MBluetooth.INSTANCE.updateImage(mac, addrBytes, imageBytes, endBytes, otaP);

            }

            @Override
            public void onFail(String s) {
                if (callback != null) {
                    callback.onFail(190808081);
                }
            }
        };
        String[] var9;
        (var9 = new String[1])[0] = cn.appscomm.bluetoothsdk.app.a.a;
        MBluetooth.INSTANCE.getCustomizeWatchFaceAddressEx(getOtaAddressCallback, ParseUtil.intToByteArray(watchface.getId(), 4), 2, var9);
    }

    private byte[] watchfaceToBytes(CustomWatchface watchface) {

        byte[] cmdHead = {0x6f, 0x1e, 0x71};

        byte[] idBytes = ParseUtil.intToByteArray(watchface.getId(), 4);

        List<Byte> cmdContent = new ArrayList<>();
        // update widgets operation
        cmdContent.add((byte) 0x02);
        // add id bytes
        cmdContent.add(idBytes[0]);
        cmdContent.add(idBytes[1]);
        cmdContent.add(idBytes[2]);
        cmdContent.add(idBytes[3]);

        // position bytes
        for (int i = 0; i < watchface.getWidgetList().size(); i++) {
            Widget widget = watchface.getWidgetList().get(i);
            if (widget.getPosition() != null) {
                cmdContent.add((byte) i);
                cmdContent.add((byte) widget.getType());
                // position
                cmdContent.add((byte) 0x00);
                // position data length
                cmdContent.add((byte) 0x04);

                byte[] xBytes = ParseUtil.intToByteArray(widget.getPosition().x, 2);
                byte[] yBytes = ParseUtil.intToByteArray(widget.getPosition().y, 2);

                cmdContent.add(xBytes[0]);
                cmdContent.add(xBytes[1]);

                cmdContent.add(yBytes[0]);
                cmdContent.add(yBytes[1]);
            }

            // size bytes
            if (widget.getSize() != null) {
                cmdContent.add((byte) i);
                cmdContent.add((byte) widget.getType());
                // size
                cmdContent.add((byte) 0x01);
                // size data length
                cmdContent.add((byte) 0x04);

                byte[] widthBytes = ParseUtil.intToByteArray(widget.getSize().getWidth(), 2);
                byte[] heightBytes = ParseUtil.intToByteArray(widget.getSize().getHeight(), 2);

                cmdContent.add(widthBytes[0]);
                cmdContent.add(widthBytes[1]);

                cmdContent.add(heightBytes[0]);
                cmdContent.add(heightBytes[1]);
            }

            // color
            // parse color like this
                /*
                int r = (widget.getColor() & 0x00FF0000);
                int g = (widget.getColor() & 0x0000FF00);
                int b = (widget.getColor() & 0x000000FF);
                */
            cmdContent.add((byte) i);
            cmdContent.add((byte) widget.getType());
            // color
            cmdContent.add((byte) 0x02);
            // color data length
            cmdContent.add((byte) 0x04);

            byte[] colorBytes = ParseUtil.intToByteArray(widget.getColor(), 4);
            cmdContent.add(colorBytes[3]);
            cmdContent.add(colorBytes[2]);
            cmdContent.add(colorBytes[1]);
            cmdContent.add(colorBytes[0]);

            if (widget.getStyle() >= 0) {
                cmdContent.add((byte) i);
                cmdContent.add((byte) widget.getType());
                // style
                cmdContent.add((byte) 0x03);
                // style data length
                cmdContent.add((byte) 0x01);
                cmdContent.add((byte) widget.getStyle());
            }
        }

        int length = cmdContent.size();
        byte[] lengthBytes = ParseUtil.intToByteArray(length, 2);

        byte[] cmd = new byte[cmdHead.length + 2 + length + cmdContent.size() + 1];

        int index = 0;
        System.arraycopy(cmdHead, 0, cmd, index, cmdHead.length);
        index = cmdHead.length;
        System.arraycopy(lengthBytes, 0, cmd, index, 2);
        index += 2;
        for (int i = 0; i < cmdContent.size(); i++) {
            cmd[index + i] = cmdContent.get(i);
        }
        index += cmdContent.size();

        // end
        cmd[index] = (byte) 0x8f;

        // debug
        String hexStr = ParseUtil.byteArrayToHexString(cmd);
        LogUtil.i("1", hexStr);
        return cmd;
    }
}
