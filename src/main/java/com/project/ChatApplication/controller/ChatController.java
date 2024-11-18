package com.project.ChatApplication.controller;

import com.project.ChatApplication.model.ChatMessage;
import com.project.ChatApplication.service.FileProcessingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class ChatController {

    // private static final String UPLOAD_DIR = "uploads/";

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private FileProcessingService fileProcessingService;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, 
                               SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        return chatMessage;
    }

    @PostMapping("/chat/sendFile")
    public void sendFile(@RequestParam("file") MultipartFile file,
                         @RequestParam("sender") String sender) throws IOException {
        if (!file.isEmpty()) {
            // Create the upload directory if it doesn't exist
            // File uploadDir = new File(UPLOAD_DIR);
            // if (!uploadDir.exists()) {
            //     uploadDir.mkdir();
            // }

            // Save the file
            String fileName = file.getOriginalFilename();
            // Path path = Paths.get(UPLOAD_DIR + fileName);
            // Files.write(path, file.getBytes());
            String status = fileProcessingService.uploadFile(file);
            System.out.println("File sent by "+ sender+"; Status -->"+status);


            // Create a chat message for the file
            ChatMessage fileMessage = new ChatMessage();
            fileMessage.setType(ChatMessage.MessageType.FILE);
            fileMessage.setSender(sender);
            fileMessage.setFileName(fileName);
            fileMessage.setFileUrl("/chat/download/" + fileName);

            // Send the file message to all clients
            messagingTemplate.convertAndSend("/topic/public", fileMessage);
        }
    }

//     @PostMapping("/chat/upload")
//     public ResponseEntity<String> uploadChatFile(@RequestParam("file") MultipartFile file) {
//     String status = fileProcessingService.uploadFile(file);
//     switch (status) {
//         case "CREATED":
//             return new ResponseEntity<>("File uploaded successfully", HttpStatus.CREATED);
//         case "EXIST":
//             return new ResponseEntity<>("File already exists", HttpStatus.CONFLICT);
//         default:
//             return new ResponseEntity<>("File upload failed", HttpStatus.EXPECTATION_FAILED);
//     }
//    }

   @GetMapping("/chat/download/{filename}")
   public ResponseEntity<?> downloadChatFile(@PathVariable String filename) {
File resource = fileProcessingService.downloadFile(filename);
    if (resource != null && resource.exists()) {
        try {
            InputStreamResource resourceStream = new InputStreamResource(new FileInputStream(resource));
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getName() + "\"")
                .contentLength(resource.length())
                .body(resourceStream);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error reading file: " + e.getMessage());
        }
    } else {
        return ResponseEntity.notFound().build();
    }
}




}