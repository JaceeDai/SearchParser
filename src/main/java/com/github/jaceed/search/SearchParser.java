package com.github.jaceed.search;

import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Created by Jacee.
 * Date: 2020.07.21
 */
public final class SearchParser {

    private static final String TAG = "SearchParser";
    private static final boolean DEBUG = false;
    private static final String SEPARATOR = "_";
    private static final String EN_TAG = "#";

    /*private static class Instance {
        private final static SearchParser sInstance = new SearchParser();
    }

    public static SearchParser getInstance() {
        return Instance.sInstance;
    }
*/
    private static int matchPattern(String expression, String key, MatchType type) {
        List<String> words = new ArrayList<>();
        List<Character> letters = new ArrayList<>();

        int pattern = 0;
        switch (type) {
            case CH:
                String[] chList = expression.split(SEPARATOR);
                for (String item : chList) {
                    words.add(item);
                    letters.add(item.charAt(0));
                }
                if (!letters.contains(key.charAt(0))) return 0;// Not match any word. Failed.
                pattern = calculateChPattern(words, 0, key, 0, 0);
                break;
            case EN:
                String[] enList = expression.split(" +");
                words.addAll(Arrays.asList(enList));
                pattern = calculateEnPattern(expression, words, 0, key, 0, 0);
                break;
            case CE:
                String[] ceList = expression.split(SEPARATOR);
                for (String item : ceList) {
                    words.add(item);
                    letters.add(item.startsWith(EN_TAG) ? item.charAt(1) : item.charAt(0));
                }
                if (!letters.contains(key.charAt(0))) return 0;// Not match any word. Failed.
                pattern = calculateChEnPattern(words, 0, key, 0, 0, 0);
            default:
        }
        return pattern;
    }

    private static int calculateChPattern(List<String> words, final int offsetWords, String key, final int offsetKey, int pattern) {
        if (offsetKey == key.length()) {
            return 0;
        }
        for (int i = offsetWords; i < words.size(); i++) {
            String word = words.get(i);
            int len = 0;
            do {
                len++;
                String k = key.substring(offsetKey, offsetKey + len);
                if (!word.startsWith(k)) {
                    len--;
                    break;
                }
            } while (len < key.length() - offsetKey);

            if (len > 0) {
                if (i == words.size() - 1 && len < key.length() - offsetKey) {
                    return -1;
                }
                pattern |= 1 << i;
                int value = calculateChPattern(words, i + 1, key, offsetKey + len, pattern);
                if (value == -1) {
                    return calculateChPattern(words, i + 1, key, 0, 0);
                } else {
                    pattern |= value;
                    return pattern;
                }
            }
        }
        return -1;
    }

    private static int calculateEnPattern(final String originalExp, List<String> words, final int offsetWords, String key, final int offsetKey, int pattern) {
        if (offsetKey == key.length()) {
            return 0;
        }

        for (int i = offsetWords; i < words.size(); i++) {
            String word = words.get(i);
            int len = 0;
            do {
                len++;
                String k = key.substring(offsetKey, offsetKey + len);
                if (!word.startsWith(k)) {
                    len--;
                    break;
                }
            } while (len < key.length() - offsetKey);

            if (len > 0) {
                if (i == words.size() - 1 && len < key.length() - offsetKey) {
                    return -1;
                }

                int offset = originalExp.indexOf(word);
                for (int pos = offset; pos < offset + len; pos++) {
                    pattern |= 1 << pos;
                }

                int value = calculateEnPattern(originalExp, words, i + 1, key, offsetKey + len, pattern);
                if (value == -1) {
                    return calculateEnPattern(originalExp, words, i + 1, key, 0, 0);
                } else {
                    pattern |= value;
                    return pattern;
                }
            }
        }
        return -1;
    }

    private static int calculateChEnPattern(List<String> words, final int offsetWords, String key, final int offsetKey, int offsetExtra, int pattern) {
        if (offsetKey == key.length()) {
            return 0;
        }

        for (int i = offsetWords; i < words.size(); i++) {
            String word = words.get(i);
            if (word.startsWith(EN_TAG)) {
                String valueStr = word.substring(EN_TAG.length());
                int len = 0;
                do {
                    len++;
                    String k = key.substring(offsetKey, offsetKey + len);
                    if (!valueStr.startsWith(k)) {
                        len--;
                        break;
                    }
                } while (len < key.length() - offsetKey);

                if (len > 0) {
                    if (i == words.size() - 1 && len < key.length() - offsetKey) {
                        return -1;
                    }

                    int offset = i + offsetExtra;
                    for (int pos = offset; pos < offset + len; pos++) {
                        pattern |= 1 << pos;
                    }
                    // Update extra offset from English word
                    offsetExtra += valueStr.length() - 1;

                    int value = calculateChEnPattern(words, i + 1, key, offsetKey + len, offsetExtra, pattern);
                    if (value == -1) {
                        return calculateChEnPattern(words, i + 1, key, 0, offsetExtra, 0);
                    } else {
                        pattern |= value;
                        return pattern;
                    }
                }

                //No match, update extra offset anyway
                offsetExtra += valueStr.length() - 1;
            } else {
                int len = 0;
                do {
                    len++;
                    String k = key.substring(offsetKey, offsetKey + len);
                    if (!word.startsWith(k)) {
                        len--;
                        break;
                    }
                } while (len < key.length() - offsetKey);

                if (len > 0) {
                    if (i == words.size() - 1 && len < key.length() - offsetKey) {
                        return -1;
                    }

                    pattern |= 1 << (i + offsetExtra);
                    int value = calculateChEnPattern(words, i + 1, key, offsetKey + len, offsetExtra, pattern);
                    if (value == -1) {
                        return calculateChEnPattern(words, i + 1, key, 0, offsetExtra, 0);
                    } else {
                        pattern |= value;
                        return pattern;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Generate the matched position list from the pattern. The positions will be stored
     * in an Integer array, which starts from the index 0.
     *
     * @param pattern Pattern value, from method {@link #matchPattern(String, String)} in general.
     * @return Matched position array. Unmatched pattern will get an empty array.
     */
    public static int[] getMatchedIndex(int pattern) {
        if (DEBUG) System.out.println(TAG + " getMatchedIndex: pattern: " + Integer.toBinaryString(Integer.valueOf(pattern)));
        int[] res = new int[Integer.bitCount(pattern)];
        int mark = 1;
        int pos = 0;
        for (int i = 0; i < res.length; i++) {
            while ((pattern & (mark << pos)) == 0) {
                pos++;
            }
            res[i] = pos;
            // Move to the next
            pos++;
        }
        if (DEBUG) System.out.println(TAG + " getMatchedIndex: res: " + Arrays.toString(res));
        return res;
    }

    /**
     * Generate a searching expression according to the give string.
     *
     * @param str The string to be searched
     * @return The Generated expression. Words are separated by {@link #SEPARATOR} with a type tag
     * marked by {@link  MatchType} in the front.
     */
    public static String makeSearchExpression(String str) throws PinyinException, IllegalArgumentException {
        StringBuilder res = new StringBuilder();
        String pinyin = PinyinHelper.convertToPinyinString(str, SEPARATOR, PinyinFormat.WITHOUT_TONE);
        if (pinyin.equals(str)) {
            if (str.length() >= 32) {
                throw new IllegalArgumentException("English string's length should be smaller than 32!");
            }
            res.append(MatchType.EN);
            res.append(SEPARATOR);
            res.append(str);
        } else {
            boolean hasEnWord = false;
            String[] words = pinyin.split(SEPARATOR);
            if (words.length >= 32) {
                throw new IllegalArgumentException("Characters should be less than 32!");
            }
            for (String word : words) {
                res.append(SEPARATOR);
                if (str.contains(word)) {
                    hasEnWord = true;
                    res.append(EN_TAG);
                }
                res.append(word);
            }
            res.insert(0, hasEnWord ? MatchType.CE : MatchType.CH);
        }
        if (DEBUG) System.out.println(TAG + " makeSearchExpression: " + str + " --> " + res);
        return res.toString();
    }

    /**
     * Match the expression with the key.
     *
     * @param expression Expression string. Must be those generated by method {@link #makeSearchExpression(String)}
     * @param key        Key string to match the expression
     * @return The matched pattern value. -1 or 0 indicates match failed. Value greater than 0 indicates all the
     * matched indexes of the given expression string bit by bit from lower to higher, 1 means matched and 0 otherwise.
     */
    public static int matchPattern(String expression, String key) {
        MatchType type;
        String typeStr = expression.substring(0, expression.indexOf(SEPARATOR));
        switch (typeStr) {
            case "CH":
                type = MatchType.CH;
                break;
            case "EN":
                type = MatchType.EN;
                break;
            case "CE":
                type = MatchType.CE;
                break;
            default:
                throw new RuntimeException("Unknown Type: " + typeStr);
        }
        String expStr = expression.substring(expression.indexOf(SEPARATOR) + 1);
        return matchPattern(expStr.toLowerCase(Locale.ENGLISH), key.toLowerCase(Locale.ENGLISH), type);
    }

    /**
     * Match the source with the key in a simple way, assuming that the key is a substring of source.
     *
     * @param source
     * @param key
     * @return The matched pattern value. Value greater than 0 indicates all the matched indexes of
     * the given source string bit by bit from lower to higher, 1 means matched and 0 otherwise.
     */
    public static int matchPatternSimple(String source, String key) {
        if (!source.contains(key)) return 0;
        int pattern = 0;
        int pos = source.indexOf(key);
        for (int i = pos; i < pos + key.length(); i++) {
            pattern |= 1 << i;
        }
        return pattern;
    }

}
