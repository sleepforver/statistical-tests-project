package com.org.statisticaltestsproject.service;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelService {

    public double[] readTTestColumn(MultipartFile file, int colIndex) throws IOException {
        List<Double> values = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheet("TTest");
            if (sheet == null) return null;

            // 从第3行开始读取数据（跳过第1行说明、第2行表头）
            for (Row row : sheet) {
                if (row.getRowNum() < 2) continue; // rowNum从0开始，<2 即跳过第1、2行
                Cell cell = row.getCell(colIndex);
                if (cell != null && cell.getCellType() == CellType.NUMERIC) {
                    values.add(cell.getNumericCellValue());
                }
            }
        }
        return values.stream().mapToDouble(Double::doubleValue).toArray();
    }

    public long[][] readChiSquareTable(MultipartFile file) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheet("ChiSquare");
            if (sheet == null) return null;

            // 找到实际数据开始的行：从第3行起向下找第一行有数字的
            int dataStartRow = -1;
            int numCols = 0;

            for (Row row : sheet) {
                if (row.getRowNum() < 2) continue; // 肯定跳过前两行

                // 检查这一行是否有数字（至少两个数字）
                int numericCount = 0;
                for (Cell cell : row) {
                    if (cell != null && cell.getCellType() == CellType.NUMERIC) {
                        numericCount++;
                    }
                }
                if (numericCount >= 2) {
                    dataStartRow = row.getRowNum();
                    // 确定列数：从第2列（index 1）开始，到最后一个有值的列
                    numCols = row.getLastCellNum() - 1;
                    break;
                }
            }

            if (dataStartRow == -1 || numCols < 2) return null;

            // 收集所有数据行
            List<long[]> tableList = new ArrayList<>();
            for (int r = dataStartRow; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                long[] rowData = new long[numCols];
                boolean hasData = false;
                for (int c = 0; c < numCols; c++) {
                    Cell cell = row.getCell(c + 1); // 从第2列开始
                    if (cell != null && cell.getCellType() == CellType.NUMERIC) {
                        rowData[c] = (long) cell.getNumericCellValue();
                        hasData = true;
                    } else {
                        rowData[c] = 0; // 或抛异常，根据需求
                    }
                }
                if (hasData) {
                    tableList.add(rowData);
                }
            }

            if (tableList.size() < 2) return null;

            return tableList.toArray(new long[tableList.size()][]);
        }
    }
}
