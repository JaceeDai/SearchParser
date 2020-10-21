package com.github.jaceed.search;

import com.github.stuxuhai.jpinyin.PinyinException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class SearchParserTest {

    private static HashMap<String, String> libraries = new HashMap<>();

    @BeforeClass
    public static void init() throws PinyinException {
        libraries.put("My name is test!", SearchParser.makeSearchExpression("My name is test!"));
        libraries.put("Mine is not test!", SearchParser.makeSearchExpression("Mine is not test!"));
        libraries.put("My test is this.", SearchParser.makeSearchExpression("My test is this."));
        libraries.put("这是一个搜索测试", SearchParser.makeSearchExpression("这是一个搜索测试"));
        libraries.put("这不是个测试", SearchParser.makeSearchExpression("这不是个测试"));
        libraries.put("这不是个测试这不是个测试这不是个测试这不是个测试这不是个测试结束吧", SearchParser.makeSearchExpression("这不是个测试这不是个测试这不是个测试这不是个测试这不是个测试结束吧"));
        libraries.put("测试Hello混合", SearchParser.makeSearchExpression("测试Hello混合"));

        for (Map.Entry<String, String> i : libraries.entrySet()) {
            System.out.println(i.getKey() + " -> " + i.getValue());
        }
    }

    @Test
    public void getMatchedIndex() {
        Assert.assertArrayEquals(SearchParser.getMatchedIndex(SearchParser.matchPattern(libraries.get("这是一个搜索测试"), "zhecs")), new int[] {0, 6, 7});
        Assert.assertArrayEquals(SearchParser.getMatchedIndex(SearchParser.matchPattern(libraries.get("这不是个测试这不是个测试这不是个测试这不是个测试这不是个测试结束吧"), "zbsjsb")), new int[] {0, 1, 2, 30, 31, 32});
        Assert.assertArrayEquals(SearchParser.getMatchedIndex(SearchParser.matchPattern(libraries.get("测试Hello混合"), "cesl")), new int[]{0, 1, 4});
    }

    @Test
    public void makeSearchExpression() {
    }

    @Test
    public void matchPattern() {
        Assert.assertNotEquals(0, SearchParser.matchPattern(libraries.get("这是一个搜索测试"), "zhe"));
        Assert.assertTrue( SearchParser.matchPattern(libraries.get("这是一个搜索测试"), "zhe") > 0);
        Assert.assertTrue( SearchParser.matchPattern(libraries.get("这是一个搜索测试"), "zhecs") > 0);
        Assert.assertTrue( SearchParser.matchPattern(libraries.get("这是一个搜索测试"), "soucs") > 0);
        Assert.assertTrue( SearchParser.matchPattern(libraries.get("这是一个搜索测试"), "zbs") <= 0);
        Assert.assertTrue( SearchParser.matchPattern(libraries.get("My name is test!"), "my") > 0);
        Assert.assertTrue( SearchParser.matchPattern(libraries.get("My name is test!"), "mna") > 0);
        Assert.assertTrue( SearchParser.matchPattern(libraries.get("My name is test!"), "mnas") > 0);
        Assert.assertTrue( SearchParser.matchPattern(libraries.get("测试Hello混合"), "ces") > 0);
        Assert.assertTrue( SearchParser.matchPattern(libraries.get("测试Hello混合"), "ceshel") > 0);
        Assert.assertTrue( SearchParser.matchPattern(libraries.get("测试Hello混合"), "cesl") > 0);
    }

    @Test
    public void matchPatternSimple() {
    }
}