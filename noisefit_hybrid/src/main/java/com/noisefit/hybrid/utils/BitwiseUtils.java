package com.noisefit.hybrid.utils;

import javax.inject.Inject;

import cn.appscomm.bluetoothsdk.app.BluetoothSDK;
import cn.appscomm.bluetoothsdk.interfaces.ResultCallBack;

public class BitwiseUtils {

    @Inject
    public BitwiseUtils() {

    }


    public String byteArrayToHexString(byte[] bytes) {
        String hexStr = "0123456789ABCDEF";
        StringBuilder result = new StringBuilder();
        String hex;
        for (byte aByte : bytes) {
            hex = String.valueOf(hexStr.charAt((aByte & 0xF0) >> 4));                            // 字节高4位
            hex += String.valueOf(hexStr.charAt(aByte & 0x0F));                                  // 字节低4位
            result.append(hex).append(" ");
        }
        return result.toString().trim();
    }

    public String decToHex(Integer value) {
        String val = Integer.toHexString(value);
        if (val.length() % 2 != 0) {
            val = "0" + val;
        }
        return val.toUpperCase();
    }

    public int hexToDec(String hex) {
        return Integer.parseInt(hex, 16);
    }

    public byte[] hexStrToBytes(String str) {
        if (str == null || str.trim().length() == 0) {
            return new byte[0];
        }

        String[] cmdStrs = str.split(",");
        byte[] bytes = new byte[cmdStrs.length];
        for (int i = 0; i < cmdStrs.length; i++) {
            String cmd = cmdStrs[i].trim();
            if (cmd.length() > 2) {
                cmd = cmd.substring(cmd.length() - 2);
            }
            bytes[i] = (byte) Integer.parseInt(cmd, 16);
        }
        return bytes;
    }


    public String asciiToHex(String asciiValue) {
        char[] chars = asciiValue.toCharArray();
        StringBuilder hex = new StringBuilder();
        for (char aChar : chars) {
            hex.append(", 0x").append(Integer.toHexString(aChar));
        }
        return hex.toString().trim();
    }


    public String hexToASCII(String hexValue) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < hexValue.length(); i += 2) {
            String str = hexValue.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }
}