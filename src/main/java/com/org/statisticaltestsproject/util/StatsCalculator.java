package com.org.statisticaltestsproject.util;

import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.apache.commons.math3.stat.inference.TTest;

import java.text.DecimalFormat;

public class StatsCalculator {

    private static final DecimalFormat DF = new DecimalFormat("#.####"); // 精确到小数点后4位

    // t 检验方法（假设无误，未修改，但添加了精度控制）
    public static String performTTestDetailed(double[] group1, double[] group2) {
        if (group1.length < 2 || group2.length < 2) {
            return "【独立样本 t 检验】样本量不足，无法计算。\n\n";
        }

        TTest tTest = new TTest();

        double mean1 = mean(group1);
        double mean2 = mean(group2);
        double var1 = variance(group1, mean1);
        double var2 = variance(group2, mean2);
        double std1 = Math.sqrt(var1);
        double std2 = Math.sqrt(var2);

        int n1 = group1.length;
        int n2 = group2.length;

        double t = tTest.t(group1, group2);
        double p = tTest.tTest(group1, group2);

        // Welch 自由度近似
        double se1 = std1 / Math.sqrt(n1);
        double se2 = std2 / Math.sqrt(n2);
        double dfApprox = Math.pow(se1 * se1 + se2 * se2, 2) /
                ((se1 * se1) * (se1 * se1) / (n1 - 1) + (se2 * se2) * (se2 * se2) / (n2 - 1));

        String conclusion = p < 0.05 ? "两组均值存在显著差异" : "两组均值无显著差异";

        return String.format("""
                【独立样本 t 检验（Welch's t-test，不假设方差相等）】

                样本信息：
                  A组 (n=%d)：  均值 = %s， 标准差 = %s
                  B组 (n=%d)：  均值 = %s， 标准差 = %s

                计算过程：
                  t = (μ₁ - μ₂) / √(s₁²/n₁ + s₂²/n₂)
                    = (%s - %s) / √(%s/%d + %s/%d)
                    = %s

                  自由度（近似）= %s
                  p 值 = %s

                结论（α = 0.05）：%s

                """,
                n1, DF.format(mean1), DF.format(std1),
                n2, DF.format(mean2), DF.format(std2),
                DF.format(mean1), DF.format(mean2), DF.format(var1), n1, DF.format(var2), n2, DF.format(t),
                DF.format(dfApprox), DF.format(p), conclusion);
    }

    // 修正后的卡方检验方法
    public static String performChiSquareDetailed(long[][] observed) {
        int rows = observed.length;
        int cols = observed[0].length;

        if (rows < 2 || cols < 2) {
            return "【卡方独立性检验】列联表必须至少为 2×2。\n\n";
        }

        // 手动计算期望频数
        long[] rowSums = new long[rows];
        long[] colSums = new long[cols];
        long total = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                rowSums[i] += observed[i][j];
                colSums[j] += observed[i][j];
                total += observed[i][j];
            }
        }

        double[][] expected = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                expected[i][j] = (double) rowSums[i] * colSums[j] / total;
            }
        }

        ChiSquareTest chiTest = new ChiSquareTest();
        double chi2 = chiTest.chiSquare(observed);
        double p = chiTest.chiSquareTest(observed);
        long df = (rows - 1L) * (cols - 1L);

        String conclusion = p < 0.05 ? "两个变量存在显著关联（不独立）" : "两个变量独立";

        StringBuilder sb = new StringBuilder();
        sb.append("【卡方独立性检验（Pearson χ² 检验）】\n\n");

        // 观察频数表
        sb.append("观察频数（O）:\n");
        appendTable(sb, observed);

        // 期望频数表
        sb.append("\n期望频数（E）:\n");
        appendTable(sb, expected);

        // 每个格子的贡献
        sb.append("\n每个格子的贡献值 (O-E)²/E :\n");
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double o = observed[i][j];
                double e = expected[i][j];
                double contrib = (o - e) * (o - e) / e;
                sb.append(String.format("  [%d,%d]: %s", i+1, j+1, DF.format(contrib)));
                if (j < cols - 1) sb.append("\t");
            }
            sb.append("\n");
        }

        sb.append(String.format("\nχ² = Σ(O-E)²/E = %s\n", DF.format(chi2)));
        sb.append(String.format("自由度 df = (r-1)(c-1) = %d\n", df));
        sb.append(String.format("p 值 = %s\n", DF.format(p)));
        sb.append(String.format("结论（α = 0.05）：%s\n\n", conclusion));

        return sb.toString();
    }

    private static void appendTable(StringBuilder sb, double[][] table) {
        for (double[] row : table) {
            for (int j = 0; j < row.length; j++) {
                sb.append(String.format("%s", DF.format(row[j])));
                if (j < row.length - 1) sb.append("\t");
            }
            sb.append("\n");
        }
    }

    private static void appendTable(StringBuilder sb, long[][] table) {
        for (long[] row : table) {
            for (int j = 0; j < row.length; j++) {
                sb.append(String.format("%d", row[j]));
                if (j < row.length - 1) sb.append("\t");
            }
            sb.append("\n");
        }
    }

    private static double mean(double[] data) {
        double sum = 0;
        for (double v : data) sum += v;
        return sum / data.length;
    }

    private static double variance(double[] data, double mean) {
        double sum = 0;
        for (double v : data) sum += (v - mean) * (v - mean);
        return sum / (data.length - 1); // 样本方差
    }
}
