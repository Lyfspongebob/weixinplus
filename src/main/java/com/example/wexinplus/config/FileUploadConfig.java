package com.example.wexinplus.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileUploadConfig {

    @PostConstruct
    public void init() {
        try {
            // 创建上传目录
            Path uploadDir = Paths.get(System.getProperty("user.dir") + "/uploads");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 创建图片子目录
            Path imageDir = uploadDir.resolve("images");
            if (!Files.exists(imageDir)) {
                Files.createDirectories(imageDir);
            }

            // 创建语音子目录
            Path voiceDir = uploadDir.resolve("voices");
            if (!Files.exists(voiceDir)) {
                Files.createDirectories(voiceDir);
            }

            // 创建文件子目录
            Path fileDir = uploadDir.resolve("files");
            if (!Files.exists(fileDir)) {
                Files.createDirectories(fileDir);
            }

            // 创建头像子目录
            Path avatarDir = uploadDir.resolve("avatars");
            if (!Files.exists(avatarDir)) {
                Files.createDirectories(avatarDir);
            }

            // 创建下载目录
            Path downloadDir = Paths.get(System.getProperty("user.dir") + "/downloads/chat_records");
            if (!Files.exists(downloadDir)) {
                Files.createDirectories(downloadDir);
            }

        } catch (IOException e) {
            throw new RuntimeException("初始化文件上传目录失败: " + e.getMessage());
        }
    }
}
