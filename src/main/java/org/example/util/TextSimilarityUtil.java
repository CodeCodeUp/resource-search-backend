package org.example.util;

import java.util.HashSet;
import java.util.Set;

/**
 * 文本相似度计算工具类
 */
public class TextSimilarityUtil {

    /**
     * 计算两个字符串的Jaccard相似度
     * @param str1 字符串1
     * @param str2 字符串2
     * @return 相似度（0-1之间）
     */
    public static double calculateJaccardSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return 0.0;
        }
        
        if (str1.equals(str2)) {
            return 1.0;
        }
        
        Set<String> set1 = getCharacterBigrams(str1);
        Set<String> set2 = getCharacterBigrams(str2);
        
        if (set1.isEmpty() && set2.isEmpty()) {
            return 1.0;
        }
        
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        return (double) intersection.size() / union.size();
    }

    /**
     * 计算编辑距离相似度
     * @param str1 字符串1
     * @param str2 字符串2
     * @return 相似度（0-1之间）
     */
    public static double calculateEditDistanceSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return 0.0;
        }
        
        if (str1.equals(str2)) {
            return 1.0;
        }
        
        int editDistance = calculateEditDistance(str1, str2);
        int maxLength = Math.max(str1.length(), str2.length());
        
        if (maxLength == 0) {
            return 1.0;
        }
        
        return 1.0 - (double) editDistance / maxLength;
    }

    /**
     * 计算两个字符串的综合相似度
     * 结合Jaccard相似度和编辑距离相似度
     * @param str1 字符串1
     * @param str2 字符串2
     * @return 相似度（0-1之间）
     */
    public static double calculateComprehensiveSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return 0.0;
        }
        
        // 预处理：去除空格和标点符号，转为小写
        String cleanStr1 = cleanText(str1);
        String cleanStr2 = cleanText(str2);
        
        double jaccardSim = calculateJaccardSimilarity(cleanStr1, cleanStr2);
        double editSim = calculateEditDistanceSimilarity(cleanStr1, cleanStr2);
        
        // 加权平均，Jaccard相似度权重更高
        return 0.7 * jaccardSim + 0.3 * editSim;
    }

    /**
     * 获取字符串的字符二元组集合
     */
    private static Set<String> getCharacterBigrams(String str) {
        Set<String> bigrams = new HashSet<>();
        
        if (str.length() < 2) {
            bigrams.add(str);
            return bigrams;
        }
        
        for (int i = 0; i < str.length() - 1; i++) {
            bigrams.add(str.substring(i, i + 2));
        }
        
        return bigrams;
    }

    /**
     * 计算编辑距离（Levenshtein距离）
     */
    private static int calculateEditDistance(String str1, String str2) {
        int len1 = str1.length();
        int len2 = str2.length();
        
        int[][] dp = new int[len1 + 1][len2 + 1];
        
        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + 1
                    );
                }
            }
        }
        
        return dp[len1][len2];
    }

    /**
     * 清理文本：去除标点符号、多余空格，转为小写
     */
    private static String cleanText(String text) {
        if (text == null) {
            return "";
        }
        
        return text.toLowerCase()
                   .replaceAll("[\\p{Punct}\\s]+", " ")
                   .trim();
    }

    /**
     * 判断两个字符串是否相似
     * @param str1 字符串1
     * @param str2 字符串2
     * @param threshold 相似度阈值
     * @return 是否相似
     */
    public static boolean isSimilar(String str1, String str2, double threshold) {
        return calculateComprehensiveSimilarity(str1, str2) >= threshold;
    }
}
