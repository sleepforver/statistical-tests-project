package com.org.statisticaltestsproject.service;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class WordService {

    public byte[] generateWord(String content) throws Exception {
        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XWPFParagraph title = doc.createParagraph();
            title.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText("实验数据统计检验报告");
            titleRun.setBold(true);
            titleRun.setFontSize(18);
            titleRun.addBreak();

            XWPFParagraph body = doc.createParagraph();
            XWPFRun bodyRun = body.createRun();
            bodyRun.setText(content.replace("\n", "\r\n"));
            bodyRun.setFontSize(12);

            doc.write(out);
            return out.toByteArray();
        }
    }
}