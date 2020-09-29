package com.github.jaceed.search;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jacee.
 * Date: 2020.09.29
 */
class StringUtils {

    public static boolean isChinese(String str) {
        String regex = "[\u4e00-\u9fa5]+";
        return str.matches(regex);
    }

    public static boolean isEnglish(String str) {
        String regex = "[a-zA-Z]+";
        return str.matches(regex);
    }

    public static boolean containsEnglish(String source) {
        Pattern pattern = Pattern.compile("[a-zA-Z]+");
        Matcher matcher = pattern.matcher(source);
        return matcher.find();
    }

    public static String filterSpaces(String source) {
        return source.replaceAll(" +", "");
    }

}
