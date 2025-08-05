package com.mevanalytics.platform.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanResult {
    
    private Long scanId;
    private String contractAddress;
    private String contractName;
    private String status; // PENDING, RUNNING, COMPLETED, FAILED
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    private Integer riskScore;
    
    private VulnerabilitySummary vulnerabilities;
    private GasAnalysisResult gasAnalysis;
    private MempoolAnalysisResult mempoolAnalysis;
    private ProtectionRecommendations protectionRecommendations;
    
    private BigDecimal mlConfidenceScore;
    private Integer scanDurationMs;
    private Integer attackCountLast30d;
    
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VulnerabilitySummary {
        private Integer total;
        private Integer critical;
        private Integer high;
        private Integer medium;
        private Integer low;
        private List<VulnerabilityDetail> details;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VulnerabilityDetail {
        private String type;
        private String severity;
        private String title;
        private String description;
        private String location;
        private BigDecimal confidenceScore;
        private Boolean hasRemediation;
        private String recommendedFix;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GasAnalysisResult {
        private String currentGasEfficiency;
        private String optimizationPotential;
        private List<String> recommendations;
        private Integer estimatedSavingsPercent;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MempoolAnalysisResult {
        private Integer recentTransactions;
        private String averageGasPrice;
        private Boolean hasUnusualActivity;
        private List<String> suspiciousPatterns;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProtectionRecommendations {
        private List<String> immediateActions;
        private List<String> longTermImprovements;
        private List<CodeSuggestion> codeFixes;
        private String overallStrategy;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CodeSuggestion {
        private String title;
        private String description;
        private String beforeCode;
        private String afterCode;
        private String difficulty;
        private String estimatedTime;
    }
}
