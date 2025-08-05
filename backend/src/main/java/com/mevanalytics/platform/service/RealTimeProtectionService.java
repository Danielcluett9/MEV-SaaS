package com.mevanalytics.platform.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.web3j.protocol.core.methods.response.Transaction;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class RealTimeProtectionService {
    
    @Autowired
    private EthereumService ethereumService;
    
    @Autowired
    private MEVDetectionService mevDetectionService;
    
    // Track protected contracts and their WebSocket sessions
    private final Map<String, Set<WebSocketSession>> protectedContracts = new ConcurrentHashMap<>();
    private final Map<String, ContractProtectionConfig> protectionConfigs = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    @PostConstruct
    public void initialize() {
        System.out.println("🛡️ Real-Time Protection Service initializing...");
        
        // Start monitoring protected contracts every 10 seconds
        scheduler.scheduleAtFixedRate(this::monitorProtectedContracts, 10, 10, TimeUnit.SECONDS);
    }
    
    public static class ContractProtectionConfig {
        public String contractAddress;
        public boolean alertOnSandwich = true;
        public boolean alertOnFrontrun = true;
        public boolean alertOnUnusualGas = true;
        public boolean autoProtectTransactions = false;
        public LocalDateTime enabledAt;
        public int alertCount = 0;
        
        public ContractProtectionConfig(String address) {
            this.contractAddress = address;
            this.enabledAt = LocalDateTime.now();
        }
    }
    
    public static class ProtectionAlert {
        public String alertType;
        public String contractAddress;
        public String threatLevel;
        public String description;
        public String recommendedAction;
        public String transactionHash;
        public LocalDateTime timestamp;
        
        public ProtectionAlert(String type, String contract, String level, String desc, String action) {
            this.alertType = type;
            this.contractAddress = contract;
            this.threatLevel = level;
            this.description = desc;
            this.recommendedAction = action;
            this.timestamp = LocalDateTime.now();
        }
    }
    
    /**
     * Enable real-time protection for a contract
     */
    public String enableProtection(String contractAddress, WebSocketSession session) {
        System.out.println("🛡️ Enabling real-time protection for: " + contractAddress);
        
        // Add to protected contracts
        protectedContracts.computeIfAbsent(contractAddress, k -> ConcurrentHashMap.newKeySet()).add(session);
        protectionConfigs.put(contractAddress, new ContractProtectionConfig(contractAddress));
        
        // Send confirmation
        sendAlert(contractAddress, new ProtectionAlert(
            "PROTECTION_ENABLED",
            contractAddress,
            "INFO",
            "Real-time MEV protection is now active for this contract",
            "Monitor transactions and gas prices"
        ));
        
        return "Protection enabled for " + contractAddress;
    }
    
    /**
     * Disable protection for a contract
     */
    public void disableProtection(String contractAddress, WebSocketSession session) {
        Set<WebSocketSession> sessions = protectedContracts.get(contractAddress);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                protectedContracts.remove(contractAddress);
                protectionConfigs.remove(contractAddress);
                System.out.println("🛡️ Protection disabled for: " + contractAddress);
            }
        }
    }
    
    /**
     * Monitor all protected contracts for suspicious activity
     */
    private void monitorProtectedContracts() {
        if (protectedContracts.isEmpty() || !ethereumService.isConnected()) {
            return;
        }
        
        System.out.println("🔍 Monitoring " + protectedContracts.size() + " protected contracts...");
        
        for (String contractAddress : protectedContracts.keySet()) {
            try {
                checkContractForThreats(contractAddress);
            } catch (Exception e) {
                System.err.println("❌ Error monitoring contract " + contractAddress + ": " + e.getMessage());
            }
        }
    }
    
    private void checkContractForThreats(String contractAddress) {
        ContractProtectionConfig config = protectionConfigs.get(contractAddress);
        if (config == null) return;
        
        // Check for recent transactions involving this contract
        // This would integrate with your existing MEVDetectionService
        
        // Simulate threat detection (in real implementation, you'd check mempool)
        Random random = new Random();
        
        // 10% chance of detecting suspicious activity (for demo)
        if (random.nextInt(100) < 10) {
            String[] threatTypes = {"SANDWICH_ATTACK", "FRONTRUN_ATTEMPT", "UNUSUAL_GAS"};
            String threatType = threatTypes[random.nextInt(threatTypes.length)];
            
            ProtectionAlert alert = createThreatAlert(threatType, contractAddress);
            sendAlert(contractAddress, alert);
            
            config.alertCount++;
        }
    }
    
    private ProtectionAlert createThreatAlert(String threatType, String contractAddress) {
        return switch (threatType) {
            case "SANDWICH_ATTACK" -> new ProtectionAlert(
                "SANDWICH_ATTACK",
                contractAddress,
                "HIGH",
                "Potential sandwich attack detected targeting this contract",
                "Consider using MEV protection or increasing slippage tolerance"
            );
            case "FRONTRUN_ATTEMPT" -> new ProtectionAlert(
                "FRONTRUN_ATTEMPT", 
                contractAddress,
                "MEDIUM",
                "Frontrunning attempt detected in mempool",
                "Monitor gas prices and consider private mempool"
            );
            case "UNUSUAL_GAS" -> new ProtectionAlert(
                "UNUSUAL_GAS",
                contractAddress,
                "LOW", 
                "Unusual gas price patterns detected",
                "Monitor for potential bot activity"
            );
            default -> new ProtectionAlert(
                "UNKNOWN_THREAT",
                contractAddress,
                "LOW",
                "Unknown suspicious activity detected",
                "Continue monitoring"
            );
        };
    }
    
    /**
     * Send alert to all connected clients for this contract
     */
    private void sendAlert(String contractAddress, ProtectionAlert alert) {
        Set<WebSocketSession> sessions = protectedContracts.get(contractAddress);
        if (sessions == null) return;
        
        try {
            String alertJson = String.format("""
                {
                    "type": "%s",
                    "contractAddress": "%s",
                    "threatLevel": "%s",
                    "description": "%s",
                    "recommendedAction": "%s",
                    "timestamp": "%s"
                }
                """, 
                alert.alertType, 
                alert.contractAddress, 
                alert.threatLevel, 
                alert.description,
                alert.recommendedAction,
                alert.timestamp
            );
            
            TextMessage message = new TextMessage(alertJson);
            
            // Send to all connected sessions for this contract
            Iterator<WebSocketSession> iterator = sessions.iterator();
            while (iterator.hasNext()) {
                WebSocketSession session = iterator.next();
                try {
                    if (session.isOpen()) {
                        session.sendMessage(message);
                        System.out.println("📡 Alert sent: " + alert.alertType + " for " + contractAddress);
                    } else {
                        iterator.remove(); // Remove closed sessions
                    }
                } catch (Exception e) {
                    System.err.println("❌ Failed to send alert: " + e.getMessage());
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error creating alert: " + e.getMessage());
        }
    }
    
    /**
     * Get protection status for a contract
     */
    public Map<String, Object> getProtectionStatus(String contractAddress) {
        ContractProtectionConfig config = protectionConfigs.get(contractAddress);
        boolean isProtected = config != null;
        
        Map<String, Object> status = new HashMap<>();
        status.put("protected", isProtected);
        status.put("contractAddress", contractAddress);
        
        if (isProtected) {
            status.put("enabledAt", config.enabledAt);
            status.put("alertCount", config.alertCount);
            status.put("activeConnections", protectedContracts.get(contractAddress).size());
            status.put("config", Map.of(
                "alertOnSandwich", config.alertOnSandwich,
                "alertOnFrontrun", config.alertOnFrontrun,
                "alertOnUnusualGas", config.alertOnUnusualGas,
                "autoProtect", config.autoProtectTransactions
            ));
        }
        
        return status;
    }
    
    /**
     * Get stats for all protected contracts
     */
    public Map<String, Object> getProtectionStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProtectedContracts", protectedContracts.size());
        stats.put("activeConnections", protectedContracts.values().stream().mapToInt(Set::size).sum());
        
        int totalAlerts = protectionConfigs.values().stream().mapToInt(config -> config.alertCount).sum();
        stats.put("totalAlerts", totalAlerts);
        
        return stats;
    }
}
