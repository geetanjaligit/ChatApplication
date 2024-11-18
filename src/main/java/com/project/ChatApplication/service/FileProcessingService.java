package com.project.ChatApplication.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

public interface FileProcessingService {
    // List<String> fileList();
    String uploadFile(MultipartFile file);
    File downloadFile(String fileName);
}
