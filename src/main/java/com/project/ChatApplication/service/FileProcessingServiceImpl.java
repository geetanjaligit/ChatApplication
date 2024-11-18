package com.project.ChatApplication.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileProcessingServiceImpl implements FileProcessingService {

    // @Value("${filePath}")
    // private String basePath;

    // @Override
    // public List<String> fileList() {
    //     File dir = new File(basePath);
    //     File[] files = dir.listFiles();

    //     return files != null ? Arrays.stream(files).map(File::getName).collect(Collectors.toList()) : null;
    // }

    @Override
    public String uploadFile(MultipartFile multipartFile) {
    // File dir = new File(basePath + multipartFile.getOriginalFilename());

    // if (dir.exists()) {
    //     return "EXIST";
    // }

    // // Path path = Path.of(basePath + multipartFile.getOriginalFilename());
    // Path path = Path.of(basePath , multipartFile.getOriginalFilename());

    // try {
    //     Files.copy(multipartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
    //     return "CREATED";
    // } catch (Exception e) {
    //     System.out.println(e.getMessage());
    // }
    // return "FAILED";

    if (multipartFile.getSize() > 10_000_000) { // 10 MB size limit
        return "File size exceeds limit.";
    }
    try {
        // Convert MultipartFile to File
        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + multipartFile.getOriginalFilename());
        multipartFile.transferTo(convFile);

       
        // if (convFile.delete()) {
        //     System.out.println("Temporary file deleted successfully");
        // } else {
        //     System.err.println("Failed to delete temporary file");
        // }
        // return "file uploaded successfully";
        return "CREATED";

    } catch (Exception e) {
        return "File upload failed: " + e.getMessage();
    }

   }
  


    @Override
    public File downloadFile(String fileName) {
        File dir = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);
        // // File dir = new File("java.io.tmpdir" + "/" + fileName);
        // File dir = new File("tmp" + "/" + fileName);
        
        System.out.println("Attempting to download file from: " + dir.getPath());
        try {
            if (dir.exists()) {
                // return new UrlResource(dir.toURI());
                return dir;
            }
            else {
                System.out.println("File does not exist: " + dir.getPath()); 
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    

}
