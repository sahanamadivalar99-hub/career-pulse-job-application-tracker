package com.tracker.controller;

import com.tracker.dto.ChatMessageDto;
import com.tracker.model.ChatMessage;
import com.tracker.service.CareerAssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/career-assistant")
public class CareerAssistantController {

    @Autowired
    private CareerAssistantService careerAssistantService;

    @GetMapping
    public String viewChat(Model model) {
        List<ChatMessage> history = careerAssistantService.getChatHistory();
        model.addAttribute("history", history);
        model.addAttribute("activePage", "career-assistant");
        return "assistant/chat";
    }

    @PostMapping("/api/ask")
    @ResponseBody
    public ResponseEntity<ChatMessageDto> askQuestion(@RequestBody Map<String, String> payload) {
        String message = payload.get("message");
        ChatMessageDto response = careerAssistantService.processUserMessage(message);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/clear")
    public String clearHistory() {
        careerAssistantService.clearHistory();
        return "redirect:/career-assistant";
    }
}
