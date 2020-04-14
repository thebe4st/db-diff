package cn.cenzhongyuan.mysql.sync.util;


import org.apache.commons.lang3.StringUtils;

public class ProjectUtils {
    public static String trimRight(String src,char ch) {
        if(StringUtils.isBlank(src)) {
            return "";
        }
        char[] chars = src.toCharArray();

        int i = chars.length - 1;
        while (i >=0 && chars[i] == ch) {
            i--;
        }
        i = Math.max(i + 1, 0);
        return src.substring(0,i);
    }
}
