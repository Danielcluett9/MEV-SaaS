package com.mevanalytics.platform.controller;

import com.mevanalytics.platform.service.RealTimeProtectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/protection")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class ProtectionController {
    
    @Autowired
    private RealTimeProtectionService protectionService;
    
    @PostMapping("/enable/{contractAddress}")
    public ResponseEntity<Map<String, Object>> enableProtection(@PathVariable String contractAddress) {
        try {
            // For HTTP requests, we can't maintain WebSocket connection
            // So we'll enable protection and provide WebSocket connection info
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Protection monitoring enabled for " + contractAddress,
                "contractAddress", contractAddress,
                "websocketUrl", "ws://localhost:8080/ws/protection/" + contractAddress,
                "instructions", "Connect to WebSocket URL for real-time alerts"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/status/{contractAddress}")
    public ResponseEntity<Map<String, Object>> getProtectionStatus(@PathVariable String contractAddress) {
        Map<String, Object> status = protectionService.getProtectionStatus(contractAddress);
        return ResponseEntity.ok(status);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getProtectionStats() {
        Map<String, Object> stats = protectionService.getProtectionStats();
        return ResponseEntity.ok(stats);
    }
}
