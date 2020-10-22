# SearchParser

![searchParserVersion](https://maven-badges.herokuapp.com/maven-central/io.github.jaceed/searchparser/badge.svg)

## 版本

Gradle
```gradle
    implementation 'io.github.jaceed:searchparser:searchParserVersion'
```
或

Maven
```gradle
    <dependency>
       <groupId>io.github.jaceed</groupId>
       <artifactId>searchparser</artifactId>
       <version>searchParserVersion</version>
    </dependency>
```

> `searchParserVersion`替换为指定版本号

## 说明

这是一个字符串匹配（搜索）的工具类。当前只支持待匹配目标字符串为纯中文、纯英文字母或中英文混合的字符串。

首先，给待匹配的目标字符串生成一个“搜索表达式”**expression**，作为后续匹配依据。

```java
String expression = SearchParser.makeSearchExpressio(str);
```

对所有的目标字符串处理过后，得到一个搜索表达式集合。


### 通过关键词搜索匹配

给定一个关键词**key**，如果是纯中文，则可调用：

```java
int pattern = SearchParser.matchPatternSimple(source, key);
```

**pattern** 的值为非零，则匹配成功。

上述方法主要针对纯中文的完全子串匹配。

如果是纯英文，则调用首字母匹配的方法：

```java
int pattern = SearchParser.matchPattern(expression, key)
```

其中**expression**是搜索表达式库中的一个表达式，如果**pattern** 的值为非零，则匹配成功，此搜索表达式对应的目标字符串满足当前**key**。


### 搜索匹配结果排序

假如需要对某个**key**的搜索结果排序，将**pattern**由小到大或由大到小排序即可。

**pattern**值越小，则表明匹配的位置越靠前。


### 定位匹配索引

对于匹配成功的字符串，需要进一步知道该字符串的哪些位置满足关键词**key**的匹配，可调用如下方法：

```java
int[] indices = SearchParser.getMatchedIndex(pattern)
```

返回一个整型数组，是匹配到的目标字符串的字符位置索引数组。


## 鸣谢

[@qzw1210](https://github.com/qzw1210) 的jpinyin库
