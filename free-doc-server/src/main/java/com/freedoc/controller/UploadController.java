package com.freedoc.controller;

import cn.hutool.core.io.FileUtil;
import com.freedoc.common.exception.BusinessException;
import com.freedoc.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    @PostMapping("/image")
    public R<Map<String, String>> uploadImage(@RequestParam("file[]") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("error.upload.fileEmpty");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = FileUtil.getSuffix(originalFilename);
        if (!isImage(extension)) {
            throw new BusinessException("error.upload.imageOnly");
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = UUID.randomUUID().toString() + "." + extension;
        String relativePath = datePath + "/" + fileName;

        File destFile = new File(uploadPath + "/images/" + relativePath);
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }

        try {
            file.transferTo(destFile);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException("error.upload.failed");
        }

        Map<String, String> result = new HashMap<>();
        result.put("url", "/api/upload/images/" + relativePath);
        result.put("originalName", originalFilename);

        return R.ok(result);
    }

    private boolean isImage(String extension) {
        if (extension == null) {
            return false;
        }
        extension = extension.toLowerCase();
        return extension.equals("jpg") || extension.equals("jpeg") ||
               extension.equals("png") || extension.equals("gif") ||
               extension.equals("webp") || extension.equals("bmp");
    }

}
