package com.mevanalytics.platform.config;

import com.mevanalytics.platform.service.RealTimeProtectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.*;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Autowired
    private RealTimeProtectionService protectionService;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new ProtectionWebSocketHandler(), "/ws/protection/*")
                .setAllowedOrigins("http://localhost:5173", "http://localhost:3000");
    }
    
    private class ProtectionWebSocketHandler implements WebSocketHandler {
        
        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            String contractAddress = extractContractAddress(session.getUri().getPath());
            if (contractAddress != null) {
                protectionService.enableProtection(contractAddress, session);
                System.out.println("🔗 WebSocket connected for contract: " + contractAddress);
                
                // Send welcome message
                session.sendMessage(new TextMessage("""
                    {
                        "type": "CONNECTION_ESTABLISHED",
                        "message": "Real-time protection enabled",
                        "contractAddress": "%s"
                    }
                    """.formatted(contractAddress)));
            }
        }
        
        @Override
        public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
            // Handle incoming messages from frontend if needed
            System.out.println("📨 Received WebSocket message: " + message.getPayload());
        }
        
        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
            String contractAddress = extractContractAddress(session.getUri().getPath());
            if (contractAddress != null) {
                protectionService.disableProtection(contractAddress, session);
                System.out.println("🔌 WebSocket disconnected for contract: " + contractAddress);
            }
        }
        
        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
            System.err.println("❌ WebSocket error: " + exception.getMessage());
        }
        
        @Override
        public boolean supportsPartialMessages() {
            return false;
        }
        
        private String extractContractAddress(String path) {
            // Extract contract address from path like "/ws/protection/0x123..."
            String[] parts = path.split("/");
            return parts.length >= 3 ? parts[parts.length - 1] : null;
        }
    }
}
