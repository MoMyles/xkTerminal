package com.cetcme.xkterminal.port;

public class Utils {
    /**
     * 字符串转换成十六进制字符串
     * @param str 待转换的ASCII字符串
     * @return String 每个Byte之间空格分隔，如: [61 6C 6B]
     */
    public static String str2HexStr(String str)
    {

        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;

        for (int i = 0; i < bs.length; i++)
        {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    /**
     * 十六进制转换字符串
     * @param hexStr str Byte字符串(Byte之间无分隔符 如:[616C6B])
     * @return String 对应的字符串
     */
    public static String hexStr2Str(String hexStr)
    {
        String str = "0123456789ABCDEF";
        hexStr = hexStr.replace(" ", "").toUpperCase();
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;

        for (int i = 0; i < bytes.length; i++)
        {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
//        String ss = "";
//        if (bytes[0] == 36 && bytes[1] == 48 && bytes[2] == 52) {
//            // $04
//            byte[] head = new byte[6];
//            for (int i=0;i<6;i++){
//                head[i] = bytes[i];
//                System.out.print(byte2HexStr(new byte[]{bytes[i]})+" ");
//            }
//            System.out.println();
//            byte[] bcd = new byte[8];
//            for (int i=0;i<8;i++){
//                bcd[i] = bytes[i + 6];
//                System.out.print(byte2HexStr(new byte[]{bytes[i+6]})+" ");
//            }
//            System.out.println();
//            byte[] tail = new byte[bytes.length - 13];
//            for (int i=0;i<bytes.length - 13;i++){
//                tail[i] = bytes[i + 13];
//                System.out.print(byte2HexStr(new byte[]{bytes[i+13]})+" ");
//            }
//            System.out.println();
//            String s = new String(tail);
//            ss = new String(head) + bcd2Str(bcd) + s;
//        } else {
        String ss = new String(bytes);
//        }
        return ss;
    }

    public static String bcd2Str(byte[] bytes) {
        char temp[] = new char[bytes.length * 2], val;

        for (int i = 0; i < bytes.length; i++) {
            val = (char) (((bytes[i] & 0xf0) >> 4) & 0x0f);
            temp[i * 2] = (char) (val > 9 ? val + 'A' - 10 : val + '0');

            val = (char) (bytes[i] & 0x0f);
            temp[i * 2 + 1] = (char) (val > 9 ? val + 'A' - 10 : val + '0');
        }
        return new String(temp);
    }

    /**
     * bytes转换成十六进制字符串
     * @param b byte数组
     * @return String 每个Byte值之间空格分隔
     */
    public static String byte2HexStr(byte[] b)
    {
        String stmp="";
        StringBuilder sb = new StringBuilder("");
        for (int n=0;n<b.length;n++)
        {
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length()==1)? "0"+stmp : stmp);
            sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }

    /**
     * bytes字符串转换为Byte值
     * @param  hexString Byte字符串，每个Byte之间没有分隔符
     * @return byte[]
     */
    public static byte[] hexStr2Bytes(String hexString)
    {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }
    /**
     * Convert char to byte
     * @param c char
     * @return byte
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
    /**
     * String的字符串转换成unicode的String
     * @param strText 全角字符串
     * @return String 每个unicode之间无分隔符
     * @throws Exception
     */
    public static String strToUnicode(String strText)
            throws Exception
    {
        char c;
        StringBuilder str = new StringBuilder();
        int intAsc;
        String strHex;
        for (int i = 0; i < strText.length(); i++)
        {
            c = strText.charAt(i);
            intAsc = (int) c;
            strHex = Integer.toHexString(intAsc);
            if (intAsc > 128)
                str.append("\\u" + strHex);
            else // 低位在前面补00
                str.append("\\u00" + strHex);
        }
        return str.toString();
    }

    /**
     * unicode的String转换成String的字符串
     * @param hex 16进制值字符串 （一个unicode为2byte）
     * @return String 全角字符串
     */
    public static String unicodeToString(String hex)
    {
        int t = hex.length() / 6;
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < t; i++)
        {
            String s = hex.substring(i * 6, (i + 1) * 6);
            // 高位需要补上00再转
            String s1 = s.substring(2, 4) + "00";
            // 低位直接转
            String s2 = s.substring(4);
            // 将16进制的string转为int
            int n = Integer.valueOf(s1, 16) + Integer.valueOf(s2, 16);
            // 将int转换为字符
            char[] chars = Character.toChars(n);
            str.append(new String(chars));
        }
        return str.toString();
    }
}
