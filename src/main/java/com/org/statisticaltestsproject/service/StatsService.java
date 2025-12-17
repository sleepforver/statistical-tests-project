package com.org.statisticaltestsproject.service;

import com.org.statisticaltestsproject.util.StatsCalculator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class StatsService {

    private final ExcelService excelService;

    public StatsService(ExcelService excelService) {
        this.excelService = excelService;
    }

    public String performAllTests(MultipartFile file) throws IOException {
        StringBuilder report = new StringBuilder();
        report.append("实验数据统计分析报告\n\n");

        // t 检验
        double[] group1 = excelService.readTTestColumn(file, 0);
        double[] group2 = excelService.readTTestColumn(file, 1);
        if (group1 != null && group2 != null && group1.length >= 2 && group2.length >= 2) {
            report.append(StatsCalculator.performTTestDetailed(group1, group2));
        } else if (file.getOriginalFilename().toLowerCase().contains("ttest")) {
            report.append("警告：TTest Sheet 数据不完整或缺失。\n\n");
        }

        // 卡方检验
        long[][] table = excelService.readChiSquareTable(file);
        if (table != null && table.length >= 2 && table[0].length >= 2) {
            report.append(StatsCalculator.performChiSquareDetailed(table));
        } else if (file.getOriginalFilename().toLowerCase().contains("chisquare")) {
            report.append("警告：ChiSquare Sheet 数据不完整或缺失。\n\n");
        }

        if (report.toString().trim().equals("实验数据统计分析报告")) {
            report.append("未检测到有效的 TTest 或 ChiSquare Sheet 数据。");
        }

        return report.toString();
    }
}