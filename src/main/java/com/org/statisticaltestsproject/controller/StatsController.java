package com.org.statisticaltestsproject.controller;


import com.org.statisticaltestsproject.service.StatsService;
import com.org.statisticaltestsproject.service.WordService;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class StatsController {

    private final StatsService statsService;
    private final WordService wordService;

    public StatsController(StatsService statsService, WordService wordService) {
        this.statsService = statsService;
        this.wordService = wordService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("error", "请上传一个 Excel 文件");
            return "index";
        }

        try {
            String report = statsService.performAllTests(file);
            model.addAttribute("report", report);
            model.addAttribute("filename", file.getOriginalFilename());

            // 将报告内容暂存到 session 或其他方式，这里直接再传一次
            model.addAttribute("rawReport", report);

            return "result";
        } catch (Exception e) {
            model.addAttribute("error", "处理文件时出错：" + e.getMessage());
            return "index";
        }
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> download(@RequestParam String report) throws Exception {
        byte[] wordBytes = wordService.generateWord(report);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("统计检验报告.docx", java.nio.charset.StandardCharsets.UTF_8)
                .build());

        return new ResponseEntity<>(wordBytes, headers, HttpStatus.OK);
    }
}