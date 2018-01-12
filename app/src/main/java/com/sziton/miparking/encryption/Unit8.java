package com.sziton.miparking.encryption;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fwj on 2017/8/8.
 */

public class Unit8 {

    public static int[][] subKey;
    public static int[] BitIP = {25, 17, 54, 33, 9, 38, 34, 1, 11, 48, 29, 56,
            27, 50, 51, 40, 19, 58, 21, 5, 44, 31, 45, 7, 61, 47, 13, 57, 23,
            15, 53, 46, 24, 16, 8, 39, 0, 26, 18, 10, 2, 49, 12, 4, 36, 30, 14,
            6, 41, 59, 63, 22, 62, 32, 37, 42, 28, 20, 43, 52, 3, 35, 60, 55};
    public static int[] BitCP = {36, 7, 40, 60, 43, 19, 47, 23, 34, 4, 39, 8,
            42, 26, 46, 29, 33, 1, 38, 16, 57, 18, 51, 28, 32, 0, 37, 12, 56,
            10, 45, 21, 53, 3, 6, 61, 44, 54, 5, 35, 15, 48, 55, 58, 20, 22,
            31, 25, 9, 41, 13, 14, 59, 30, 2, 63, 11, 27, 17, 49, 62, 24, 52,
            50};
    public static int[][] BitPMC = {
            {56, 0, 53, 29, 17, 44, 24, 8, 20, 23, 43, 16, 7, 46, 36, 57, 2,
                    19, 42, 35, 32, 15, 31, 26, 54, 60, 33, 9, 38, 11, 61, 30,
                    10, 47, 40, 5, 52, 25, 41, 27, 62, 63, 6, 58, 13, 21, 3,
                    28, 18, 49, 55, 59, 39, 51, 12, 37, 14, 1, 4, 34, 22, 45,
                    48, 50},
            {63, 10, 47, 58, 39, 38, 51, 42, 23, 54, 3, 21, 14, 55, 49, 29,
                    37, 28, 56, 40, 61, 43, 60, 18, 16, 57, 26, 9, 30, 34, 11,
                    33, 1, 27, 53, 12, 36, 48, 52, 22, 46, 8, 45, 44, 59, 15,
                    5, 6, 13, 24, 35, 31, 2, 62, 41, 0, 4, 25, 50, 20, 7, 17,
                    32, 19},
            {8, 5, 46, 4, 39, 44, 63, 52, 2, 54, 56, 62, 21, 32, 50, 48, 20,
                    22, 47, 57, 60, 37, 12, 34, 9, 41, 27, 11, 6, 18, 33, 14,
                    24, 31, 28, 55, 36, 23, 16, 40, 51, 25, 61, 43, 17, 3, 35,
                    53, 0, 7, 10, 58, 15, 1, 13, 19, 38, 45, 29, 42, 49, 26,
                    59, 30},
            {26, 52, 25, 7, 48, 49, 56, 30, 27, 11, 22, 47, 8, 16, 40, 10, 9,
                    24, 50, 62, 57, 44, 34, 14, 4, 55, 59, 5, 39, 23, 17, 58,
                    12, 3, 63, 43, 6, 20, 51, 42, 45, 28, 31, 54, 53, 1, 41,
                    35, 13, 60, 21, 61, 19, 2, 46, 15, 36, 33, 18, 37, 0, 32,
                    38, 29},
            {42, 48, 16, 38, 41, 57, 53, 3, 52, 14, 61, 33, 26, 19, 32, 58,
                    10, 1, 9, 24, 43, 8, 15, 5, 56, 2, 40, 36, 7, 0, 17, 20,
                    45, 37, 6, 13, 25, 34, 11, 27, 30, 12, 63, 31, 28, 47, 4,
                    51, 62, 22, 55, 44, 29, 35, 59, 23, 46, 50, 39, 60, 49, 18,
                    21, 54},
            {51, 44, 45, 12, 10, 19, 9, 57, 53, 0, 49, 8, 29, 7, 22, 36, 13,
                    58, 35, 15, 50, 23, 59, 52, 63, 4, 30, 43, 26, 33, 42, 1,
                    14, 24, 55, 38, 5, 32, 48, 28, 21, 31, 17, 46, 41, 47, 60,
                    25, 20, 11, 61, 3, 6, 16, 2, 40, 39, 18, 62, 37, 34, 27,
                    56, 54},
            {19, 61, 20, 15, 0, 59, 60, 12, 10, 16, 35, 36, 34, 5, 27, 8, 43,
                    3, 54, 7, 57, 58, 26, 56, 13, 1, 23, 50, 11, 6, 25, 22, 31,
                    44, 62, 53, 14, 33, 39, 48, 52, 2, 40, 41, 29, 17, 18, 4,
                    47, 28, 63, 24, 9, 32, 21, 46, 49, 30, 38, 37, 51, 45, 42,
                    55},
            {33, 24, 29, 28, 30, 51, 20, 25, 0, 57, 22, 34, 13, 44, 31, 17,
                    49, 16, 18, 50, 4, 48, 5, 38, 41, 12, 63, 26, 55, 37, 52,
                    60, 27, 9, 21, 19, 45, 39, 54, 15, 53, 7, 43, 46, 62, 11,
                    14, 36, 56, 1, 10, 23, 42, 61, 8, 35, 40, 59, 47, 32, 2,
                    58, 6, 3},
            {21, 55, 35, 48, 60, 14, 12, 7, 15, 2, 5, 29, 27, 37, 13, 8, 34,
                    59, 10, 49, 33, 30, 51, 47, 57, 28, 63, 3, 11, 56, 53, 44,
                    54, 32, 61, 24, 6, 40, 43, 46, 20, 41, 31, 38, 62, 50, 16,
                    17, 58, 0, 22, 18, 42, 52, 23, 39, 1, 19, 25, 9, 26, 45,
                    36, 4},
            {21, 44, 43, 30, 22, 27, 1, 61, 26, 20, 53, 55, 13, 7, 46, 0, 48,
                    60, 2, 28, 17, 36, 12, 10, 25, 49, 34, 47, 14, 19, 45, 57,
                    6, 33, 56, 39, 18, 37, 52, 16, 58, 41, 3, 15, 51, 9, 62,
                    24, 23, 32, 4, 35, 31, 8, 11, 54, 38, 40, 63, 29, 5, 42,
                    59, 50},
            {59, 26, 54, 18, 31, 43, 23, 34, 13, 53, 19, 55, 36, 32, 38, 60,
                    37, 7, 2, 39, 20, 63, 8, 9, 28, 14, 50, 46, 21, 6, 47, 35,
                    22, 17, 41, 27, 16, 0, 5, 57, 45, 56, 29, 10, 49, 12, 42,
                    25, 24, 11, 40, 4, 30, 1, 52, 61, 58, 51, 3, 44, 62, 33,
                    15, 48},
            {48, 60, 32, 34, 55, 30, 46, 52, 56, 38, 6, 17, 5, 54, 15, 11, 44,
                    9, 10, 43, 53, 39, 19, 18, 33, 28, 41, 29, 12, 24, 14, 47,
                    23, 7, 31, 59, 62, 50, 13, 4, 61, 36, 3, 0, 49, 35, 2, 37,
                    42, 63, 21, 25, 8, 45, 16, 40, 22, 58, 1, 51, 20, 26, 27,
                    57},
            {11, 44, 10, 0, 57, 23, 52, 58, 15, 31, 24, 20, 25, 13, 30, 3, 2,
                    54, 5, 47, 36, 17, 8, 22, 38, 50, 7, 21, 40, 33, 37, 14,
                    43, 46, 27, 9, 60, 16, 61, 28, 59, 56, 63, 19, 32, 18, 48,
                    26, 62, 45, 51, 42, 49, 1, 35, 12, 39, 41, 55, 6, 29, 53,
                    34, 4},
            {11, 42, 45, 51, 32, 1, 9, 35, 7, 12, 53, 8, 20, 41, 54, 62, 55,
                    38, 15, 26, 5, 16, 39, 10, 19, 21, 33, 6, 61, 17, 63, 28,
                    58, 0, 48, 3, 23, 29, 37, 44, 13, 43, 27, 60, 36, 22, 30,
                    25, 4, 59, 34, 47, 2, 24, 46, 50, 52, 31, 18, 40, 57, 14,
                    49, 56},
            {47, 23, 35, 5, 6, 57, 12, 10, 31, 21, 51, 37, 33, 22, 1, 24, 17,
                    39, 58, 30, 54, 61, 63, 3, 60, 38, 40, 20, 59, 48, 2, 53,
                    27, 29, 19, 34, 16, 45, 26, 44, 4, 25, 32, 0, 15, 55, 36,
                    46, 43, 42, 7, 41, 11, 56, 18, 9, 14, 50, 13, 28, 8, 49,
                    62, 52},
            {32, 53, 46, 30, 42, 17, 44, 25, 12, 60, 10, 41, 27, 11, 57, 31,
                    9, 63, 34, 4, 47, 56, 48, 54, 1, 40, 45, 8, 5, 22, 35, 33,
                    7, 36, 26, 3, 58, 50, 49, 15, 37, 19, 61, 52, 0, 21, 23,
                    39, 62, 29, 51, 55, 13, 18, 24, 2, 38, 43, 6, 14, 16, 20,
                    28, 59}};

    public static void initPermutation(int[] inData) {
        int[] newData = new int[8];
        int i = 0;
        // FillChar(newData, 8, 0);
        for (i = 0; i <= 63; i++) {
            if ((inData[BitIP[i] >> 3] & (1 << (7 - (BitIP[i] & 0x07)))) != 0) {
                // newData[i >> 3] = Convert.ToByte(newData[i >> 3] | (1 << (7 -
                // (i & 0x07))));
                newData[i >> 3] = (int) (newData[i >> 3] | (1 << (7 - (i & 0x07))));
            }
        }
        for (i = 0; i <= 7; i++) {
            inData[i] = newData[i];
        }
    }

    public static void conversePermutation(int[] inData) {
        int[] newData = new int[8];
        int i = 0;
        // FillChar(newData, 8, 0);
        for (i = 0; i <= 63; i++) {
            if ((inData[BitCP[i] >> 3] & (1 << (7 - (BitCP[i] & 0x07)))) != 0) {
                // newData[i >> 3] = Convert.ToByte(newData[i >> 3] | (1 << (7 -
                // (i & 0x07))));
                newData[i >> 3] = (int) (newData[i >> 3] | (1 << (7 - (i & 0x07))));
            }
        }
        for (i = 0; i <= 7; i++) {
            inData[i] = newData[i];
        }
    }

    public static int[][] makeKey(int[] inKey) {
        int[] newData = new int[8];
        int i = 0;
        int j = 0;
        int k = 0;
        // FillChar(newData, 8, 0);
        // fillchar(outKey, 16, 0);
        int[][] outKey = new int[16][8];
        for (i = 0; i <= 7; i++) {
            for (j = 0; j <= 63; j++) {
                int bittemp1 = BitPMC[i][j];
                int bittemp2 = BitPMC[i][j] >> 3;
                int temp1 = inKey[BitPMC[i][j] >> 3];
                int temp2 = 1 << (7 - (BitPMC[i][j] & 0x07));
                if ((inKey[BitPMC[i][j] >> 3] & (1 << (7 - (BitPMC[i][j] & 0x07)))) != 0) {
                    int b = (int) (newData[j >> 3] | (1 << (7 - (j & 0x07))));
                    newData[j >> 3] = b;
                }
            }
            for (k = 0; k <= 7; k++) {
                outKey[i][k] = newData[k];
            }
            newData = new int[8];
            // FillChar(newData, 8, 0);
        }
        return outKey;
    }

    public static void desData(TDesMode desMode, int[] inData, int[] outData) {
        int i = 0;
        int j = 0;
        if (desMode == TDesMode.dmEncry) {
            for (i = 0; i <= 7; i++) {
                for (j = 0; j <= 7; j++) {
                    inData[j] = (int) (inData[j] ^ subKey[i][j]);
                }
            }
            for (j = 0; j <= 7; j++) {
                outData[j] = inData[j];
            }
        } else {
            if (desMode == TDesMode.dmDecry) {
                for (i = 7; i >= 0; i--) {
                    for (j = 0; j <= 7; j++) {
                        inData[j] = (int) (inData[j] ^ subKey[i][j]);
                    }
                }
                for (j = 0; j <= 7; j++) {
                    outData[j] = inData[j];
                }
            }
        }
    }

    public static int HexToInt(String S) {
        return StrToIntDef('$' + S, 0);
    }

    public static int CharToInt(byte str) {
        int dData = 0;
        if (str <= '9') {
            dData = str;
            dData = dData - '0';
            return dData;
        } else {
            dData = str - 'A';
            dData = dData + 10;
            return dData;
        }
    }

    public static int HexToInt1(String str) {
        int dData0 = 0;
        int dData1 = 0;
        byte[] bt = str.getBytes();
        dData0 = CharToInt(bt[0]);
        dData1 = CharToInt(bt[1]);
        dData1 = dData0 * 16;
        return dData1;
    }

    public static boolean IsNumber(String str) {
        Pattern pat = Pattern.compile("^[0-9]*$");
        Matcher mat = pat.matcher(str);
        return mat.matches();

    }

    public static int StrToIntDef(String Str, int Def) {
        int result = Def;
        if (Str.indexOf("$", 0) > -1) {
            // TODO
            if (IsNumber(Str.substring(1))) {
                result = Integer.valueOf(Str.substring(1));
//			result = Integer.parseInt(Str.substring(1), 16);
            }
        } else {
            if (IsNumber(Str)) {
                result = Integer.valueOf(Str);
            }
        }
        return result;
    }

    public static void FillChar(int[][] data, int length, int values) {
        for (int i = 0; i < length; i++) {
            data[i][0] = (int) values;
        }
    }

    public static String EncryStr(String Str, String Key) {
        String result = null;
        int[] StrByte = new int[8];
        int[] OutByte = new int[8];
        int[] KeyByte = new int[8];
        String StrResult = "";
        String[] sss = Key.split("#");
        String[] yyy = Str.split("#");
        // if (Str.endsWith("#")) {
        // yyy[8]="";
        // }
        // if (Key.endsWith("#")) {
        // sss[8]="";
        // }
        for (int j = 0; j <= 7; j++) {
            KeyByte[j] = Integer.valueOf(sss[j]);
        }
        subKey = makeKey(KeyByte);
        for (int j = 0; j <= 7; j++) {
            StrByte[j] = Integer.valueOf(yyy[j]);
        }
        initPermutation(StrByte);
        desData(TDesMode.dmEncry, StrByte, OutByte);
        for (int j = 0; j <= 7; j++) {
            StrResult = StrResult + OutByte[j] + "&";
        }
        result = StrResult;
        return result;
    }

    public static String DecryStr1(String Str, String Key) {
        String result = null;
        int[] StrByte = new int[8];
        int[] OutByte = new int[8];
        int[] KeyByte = new int[8];
        String StrResult = "";
        String[] sss = Key.split("#");
        String[] yyy = Str.split("#");
        for (int j = 0; j <= 7; j++) {
            KeyByte[j] = Integer.valueOf(sss[j]);
        }
        subKey = makeKey(KeyByte);
        for (int j = 0; j <= 7; j++) {
            StrByte[j] = Integer.valueOf(yyy[j]);
        }
        // initPermutation(StrByte);
        desData(TDesMode.dmDecry, StrByte, OutByte);

        conversePermutation(OutByte);
        for (int j = 0; j <= 7; j++) {
            StrResult = StrResult + OutByte[j] + "&";
        }
        result = StrResult;

        return result;
    }

    public static String DecryStr(String Str, String Key) {
        String result = null;
        int[] StrByte = new int[8];
        int[] OutByte = new int[8];
        int[] KeyByte = new int[8];
        String StrResult = "";
        String[] sss = Key.split("#");
        String[] yyy = Str.split("#");
        String s = null;
        for (int j = 0; j <= 7; j++) {
            KeyByte[j] = Integer.valueOf(sss[j]);
        }
        subKey = makeKey(KeyByte);
        for (int j = 0; j <= 7; j++) {
            StrByte[j] = Integer.valueOf(yyy[j]);
        }
        desData(TDesMode.dmDecry, StrByte, OutByte);
        conversePermutation(OutByte);
        for (int j = 0; j <= 7; j++) {
            s = String.format("%x", new int[]{OutByte[j]});
            for (int I = 1; I <= 2 - s.length(); I++) {
                s = '0' + s;
            }
            StrResult = StrResult + s;
        }
        result = StrResult;
        return result;
    }

    public static String EncryStrHex(String Str, String Key) {
        String result = null;
        String StrResult = "";
        String TempResult = null;
        String Temp = null;
        String s = "";
        String k = "";
        for (int i = 0; i <= Str.length() / 2 - 1; i++) {
            Temp = Str.substring(i * 2, i * 2 + 2);
            int b = Integer.valueOf(Temp, 16);
            s = s + b + "#";
        }
        for (int i = 0; i <= Key.length() / 2 - 1; i++) {
            Temp = Key.substring(i * 2, i * 2 + 2);
            // int asd =Integer.valueOf(HexToInt(Temp), 16);
            int asd = Integer.valueOf(String.valueOf(HexToInt(Temp)), 16);
            k = k + asd + "#";
        }
        TempResult = EncryStr(s, k);
        String[] temp = TempResult.split("&");
        for (int i = 0; i <= temp.length - 1; i++) {
            if (temp[i] == null || temp[i] == "") {
                continue;
            }
            // Temp = String.format("{0:X}", Integer.valueOf(temp[i]));
            // Temp = String.format("{0:X}", Integer.valueOf(temp[i]));
            Temp = Integer.toHexString(Integer.valueOf(temp[i]));

            if (Temp.length() == 1) {
                Temp = '0' + Temp;
            }
            StrResult = StrResult + Temp;
        }
        result = StrResult;
        return result.toUpperCase();
    }

    public static String IntToHex(int Value) {
        String result = "";
        char[] HexChars = new char[]{'1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        int iTemp = 0;
        int i = 0;
        i = 0;
        while (i < 4) {
            switch (i) {
                case 0:
                    iTemp = Value >> 24 & 0xFF;
                    break;
                case 1:
                    iTemp = Value >> 16 & 0xFF;
                    break;
                case 2:
                    iTemp = Value >> 8 & 0xFF;
                    break;
                case 3:
                    iTemp = Value & 0xFF;
                    break;
            }
            result = result + HexChars[iTemp / 16];
            result = result + HexChars[iTemp % 16];
            i++;
        }
        return result;
    }

    public static String DecryStrHex(String Str, String Key) {
        String result = null;
        String StrResult = "";
        String TempResult = null;
        String Temp = null;
        String s = "";
        String k = "";
        for (int i = 0; i <= Str.length() / 2 - 1; i++) {
            Temp = Str.substring(i * 2, i * 2 + 2);
            int b = Integer.valueOf(Temp, 16);
            s = s + b + "#";
        }
        for (int i = 0; i <= Key.length() / 2 - 1; i++) {
            Temp = Key.substring(i * 2, i * 2 + 2);
            // int asd =Integer.valueOf(HexToInt(Temp), 16);
            int asd = Integer.valueOf(String.valueOf(HexToInt(Temp)), 16);
            k = k + asd + "#";
        }
        TempResult = DecryStr1(s, k);
        String[] temp = TempResult.split("&");
        for (int i = 0; i <= temp.length - 1; i++) {
            if (temp[i] == null || temp[i] == "") {
                continue;
            }
            // Temp = String.format("{0:X}", Integer.valueOf(temp[i]));
            // Temp = String.format("{0:X}", Integer.valueOf(temp[i]));
            Temp = Integer.toHexString(Integer.valueOf(temp[i]));

            if (Temp.length() == 1) {
                Temp = '0' + Temp;
            }
            StrResult = StrResult + Temp;
        }
        result = StrResult;
        return result.toUpperCase();
    }

    public enum TDesMode {
        dmEncry, dmDecry,
    }

    public int getByte(int b) {
        if (b < 0) {
            return (b + 256);
        }
        return b;
    }
}
