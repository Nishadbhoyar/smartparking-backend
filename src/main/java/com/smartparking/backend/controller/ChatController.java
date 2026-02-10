// package com.smartparking.backend.controller;

// import com.smartparking.backend.dto.ChatRequest;
// import com.smartparking.backend.dto.ChatResponse; // Make sure typo is fixed in DTO folder
// import com.smartparking.backend.service.OpenAIService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.web.bind.annotation.*;

// @RestController
// @RequestMapping("/api/chat")
// @CrossOrigin(origins = "http://localhost:3000") // Adjust to your React port
// public class ChatController {

//     @Autowired
//     private OpenAIService openAIService;

//     @PostMapping
//     public ChatResponse chat(@RequestBody ChatRequest request) {
//         String aiResponse = openAIService.getChatResponse(request.getMessage());
//         return new ChatResponse(aiResponse);
//     }
// }