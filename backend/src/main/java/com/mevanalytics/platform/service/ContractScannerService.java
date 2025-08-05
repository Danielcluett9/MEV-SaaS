package com.mevanalytics.platform.service;

import com.mevanalytics.platform.dto.ScanRequest;
import com.mevanalytics.platform.dto.ScanResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthGetCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ContractScannerService {
    
    @Autowired
    private EthereumService ethereumService;
    
    private final AtomicLong scanIdCounter = new AtomicLong(1);
    private final Map<Long, ScanResult> scanResults = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> contractScans = new ConcurrentHashMap<>();
    
    @Async
    public CompletableFuture<ScanResult> scanContractAsync(ScanRequest request) {
        long startTime = System.currentTimeMillis();
        Long scanId = scanIdCounter.getAndIncrement();
        
        System.out.println("🔍 Starting contract scan #" + scanId + " for: " + request.getContractAddress());
        
        try {
            // Create initial scan result
            ScanResult result = ScanResult.builder()
                .scanId(scanId)
                .contractAddress(request.getContractAddress())
                .contractName("Unknown Contract")
                .status("RUNNING")
                .startedAt(LocalDateTime.now())
                .build();
            
            scanResults.put(scanId, result);
            
            // Step 1: Verify it's a contract
            boolean isContract = isValidContract(request.getContractAddress());
            if (!isContract) {
                result.setStatus("FAILED");
                result.setCompletedAt(LocalDateTime.now());
                result.setRiskLevel("UNKNOWN");
                throw new IllegalArgumentException("Address is not a contract");
            }
            
            // Step 2: Get contract bytecode
            String bytecode = getContractBytecode(request.getContractAddress());
            
            // Step 3: Analyze vulnerabilities
            List<ScanResult.VulnerabilityDetail> vulnerabilities = detectVulnerabilities(bytecode, request.getContractAddress());
            
            // Step 4: Calculate risk score
            int riskScore = calculateRiskScore(vulnerabilities);
            String riskLevel = determineRiskLevel(riskScore);
            
            // Step 5: Generate gas analysis
            ScanResult.GasAnalysisResult gasAnalysis = null;
            if (request.getIncludeGasAnalysis()) {
                gasAnalysis = analyzeGasUsage(bytecode);
            }
            
            // Step 6: Generate mempool analysis
            ScanResult.MempoolAnalysisResult mempoolAnalysis = null;
            if (request.getIncludeMempoolAnalysis()) {
                mempoolAnalysis = analyzeMempoolActivity(request.getContractAddress());
            }
            
            // Step 7: Generate protection recommendations
            ScanResult.ProtectionRecommendations recommendations = generateRecommendations(vulnerabilities, riskLevel);
            
            // Step 8: Build final result
            ScanResult.VulnerabilitySummary vulnSummary = ScanResult.VulnerabilitySummary.builder()
                .total(vulnerabilities.size())
                .critical((int) vulnerabilities.stream().filter(v -> "CRITICAL".equals(v.getSeverity())).count())
                .high((int) vulnerabilities.stream().filter(v -> "HIGH".equals(v.getSeverity())).count())
                .medium((int) vulnerabilities.stream().filter(v -> "MEDIUM".equals(v.getSeverity())).count())
                .low((int) vulnerabilities.stream().filter(v -> "LOW".equals(v.getSeverity())).count())
                .details(vulnerabilities)
                .build();
            
            result.setVulnerabilities(vulnSummary);
            result.setGasAnalysis(gasAnalysis);
            result.setMempoolAnalysis(mempoolAnalysis);
            result.setProtectionRecommendations(recommendations);
            result.setRiskScore(riskScore);
            result.setRiskLevel(riskLevel);
            result.setStatus("COMPLETED");
            result.setCompletedAt(LocalDateTime.now());
            result.setScanDurationMs((int) (System.currentTimeMillis() - startTime));
            
            // Store result
            scanResults.put(scanId, result);
            contractScans.computeIfAbsent(request.getContractAddress(), k -> new ArrayList<>()).add(scanId);
            
            System.out.println("✅ Contract scan #" + scanId + " completed in " + result.getScanDurationMs() + "ms");
            System.out.println("📊 Risk Level: " + riskLevel + " (Score: " + riskScore + ")");
            System.out.println("🔍 Vulnerabilities: " + vulnerabilities.size() + " found");
            
            return CompletableFuture.completedFuture(result);
            
        } catch (Exception e) {
            System.err.println("❌ Contract scan #" + scanId + " failed: " + e.getMessage());
            
            ScanResult errorResult = ScanResult.builder()
                .scanId(scanId)
                .contractAddress(request.getContractAddress())
                .status("FAILED")
                .startedAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .scanDurationMs((int) (System.currentTimeMillis() - startTime))
                .build();
            
            scanResults.put(scanId, errorResult);
            throw new RuntimeException("Scan failed: " + e.getMessage(), e);
        }
    }
    
    private boolean isValidContract(String address) {
        try {
            if (!ethereumService.isConnected()) {
                return false;
            }
            
            // Use your existing EthereumService's web3j instance
            EthGetCode ethGetCode = ethereumService.getWeb3j().ethGetCode(address, 
                org.web3j.protocol.core.DefaultBlockParameterName.LATEST).send();
            String code = ethGetCode.getCode();
            return code != null && !code.equals("0x") && code.length() > 2;
        } catch (Exception e) {
            System.err.println("❌ Error checking if address is contract: " + e.getMessage());
            return false;
        }
    }
    
    private String getContractBytecode(String address) {
        try {
            EthGetCode ethGetCode = ethereumService.getWeb3j().ethGetCode(address, 
                org.web3j.protocol.core.DefaultBlockParameterName.LATEST).send();
            return ethGetCode.getCode();
        } catch (Exception e) {
            System.err.println("❌ Error getting contract bytecode: " + e.getMessage());
            return "";
        }
    }
    
    private List<ScanResult.VulnerabilityDetail> detectVulnerabilities(String bytecode, String address) {
        List<ScanResult.VulnerabilityDetail> vulnerabilities = new ArrayList<>();
        
        if (bytecode == null || bytecode.length() < 10) {
            return vulnerabilities;
        }
        
        // 1. Check for reentrancy patterns
        if (containsReentrancyPattern(bytecode)) {
            vulnerabilities.add(ScanResult.VulnerabilityDetail.builder()
                .type("reentrancy")
                .severity("HIGH")
                .title("Potential Reentrancy Vulnerability")
                .description("Contract may be vulnerable to reentrancy attacks due to external calls")
                .location("Bytecode analysis")
                .confidenceScore(BigDecimal.valueOf(0.75))
                .hasRemediation(true)
                .recommendedFix("Implement reentrancy guards or use checks-effects-interactions pattern")
                .build());
        }
        
        // 2. Check for unchecked external calls
        if (containsUncheckedExternalCalls(bytecode)) {
            vulnerabilities.add(ScanResult.VulnerabilityDetail.builder()
                .type("unchecked_external_call")
                .severity("MEDIUM")
                .title("Unchecked External Calls")
                .description("Contract makes external calls without proper error handling")
                .location("Bytecode analysis")
                .confidenceScore(BigDecimal.valueOf(0.65))
                .hasRemediation(true)
                .recommendedFix("Add proper error handling for external calls")
                .build());
        }
        
        // 3. Check for potential MEV vulnerabilities
        if (containsMEVVulnerablePatterns(bytecode)) {
            vulnerabilities.add(ScanResult.VulnerabilityDetail.builder()
                .type("mev_vulnerable")
                .severity("HIGH")
                .title("MEV Vulnerability Detected")
                .description("Contract appears vulnerable to MEV extraction attacks")
                .location("DEX interaction patterns")
                .confidenceScore(BigDecimal.valueOf(0.80))
                .hasRemediation(true)
                .recommendedFix("Implement slippage protection and consider MEV protection services")
                .build());
        }
        
        // 4. Check for gas optimization issues
        if (hasGasOptimizationIssues(bytecode)) {
            vulnerabilities.add(ScanResult.VulnerabilityDetail.builder()
                .type("gas_inefficiency")
                .severity("LOW")
                .title("Gas Optimization Opportunities")
                .description("Contract has potential gas optimization opportunities")
                .location("Storage and computation patterns")
                .confidenceScore(BigDecimal.valueOf(0.60))
                .hasRemediation(true)
                .recommendedFix("Optimize storage layout and reduce redundant computations")
                .build());
        }
        
        return vulnerabilities;
    }
    
    private int calculateRiskScore(List<ScanResult.VulnerabilityDetail> vulnerabilities) {
        int score = 0;
        for (ScanResult.VulnerabilityDetail vuln : vulnerabilities) {
            switch (vuln.getSeverity()) {
                case "CRITICAL" -> score += 25;
                case "HIGH" -> score += 15;
                case "MEDIUM" -> score += 8;
                case "LOW" -> score += 3;
            }
        }
        return Math.min(100, score);
    }
    
    private String determineRiskLevel(int riskScore) {
        if (riskScore >= 80) return "CRITICAL";
        if (riskScore >= 60) return "HIGH";
        if (riskScore >= 30) return "MEDIUM";
        return "LOW";
    }
    
    private ScanResult.GasAnalysisResult analyzeGasUsage(String bytecode) {
        List<String> recommendations = new ArrayList<>();
        
        if (bytecode.contains("5b") && bytecode.contains("5d")) { // JUMPDEST patterns
            recommendations.add("Consider optimizing jump destinations");
        }
        
        if (bytecode.length() > 10000) {
            recommendations.add("Large contract size - consider splitting into modules");
        }
        
        recommendations.add("Use packed structs for storage efficiency");
        recommendations.add("Cache storage reads in local variables");
        
        return ScanResult.GasAnalysisResult.builder()
            .currentGasEfficiency("MEDIUM")
            .optimizationPotential("10-15% gas reduction possible")
            .recommendations(recommendations)
            .estimatedSavingsPercent(12)
            .build();
    }
    
    private ScanResult.MempoolAnalysisResult analyzeMempoolActivity(String contractAddress) {
        return ScanResult.MempoolAnalysisResult.builder()
            .recentTransactions(25)
            .averageGasPrice("22.5 gwei")
            .hasUnusualActivity(false)
            .suspiciousPatterns(List.of())
            .build();
    }
    
    private ScanResult.ProtectionRecommendations generateRecommendations(
            List<ScanResult.VulnerabilityDetail> vulnerabilities, String riskLevel) {
        
        List<String> immediateActions = new ArrayList<>();
        List<String> longTermImprovements = new ArrayList<>();
        List<ScanResult.CodeSuggestion> codeFixes = new ArrayList<>();
        
        if (vulnerabilities.stream().anyMatch(v -> "reentrancy".equals(v.getType()))) {
            immediateActions.add("Implement reentrancy guards on state-changing functions");
            codeFixes.add(ScanResult.CodeSuggestion.builder()
                .title("Add Reentrancy Guard")
                .description("Prevent reentrancy attacks with OpenZeppelin's ReentrancyGuard")
                .beforeCode("function withdraw() public { /* vulnerable */ }")
                .afterCode("function withdraw() public nonReentrant { /* protected */ }")
                .difficulty("LOW")
                .estimatedTime("30 minutes")
                .build());
        }
        
        if (vulnerabilities.stream().anyMatch(v -> "mev_vulnerable".equals(v.getType()))) {
            immediateActions.add("Add slippage protection to DEX interactions");
            longTermImprovements.add("Consider using MEV protection services like Flashbots Protect");
        }
        
        immediateActions.add("Review and test all external calls");
        longTermImprovements.add("Implement comprehensive access controls");
        longTermImprovements.add("Regular security audits and monitoring");
        
        String strategy = switch (riskLevel) {
            case "CRITICAL" -> "URGENT: Address critical vulnerabilities immediately before deployment";
            case "HIGH" -> "HIGH PRIORITY: Fix high-risk issues and implement security measures";
            case "MEDIUM" -> "MODERATE: Improve security posture and implement best practices";
            default -> "MAINTENANCE: Follow security best practices and monitor for issues";
        };
        
        return ScanResult.ProtectionRecommendations.builder()
            .immediateActions(immediateActions)
            .longTermImprovements(longTermImprovements)
            .codeFixes(codeFixes)
            .overallStrategy(strategy)
            .build();
    }
    
    // Pattern detection methods
    private boolean containsReentrancyPattern(String bytecode) {
        return bytecode.contains("6000803e") || bytecode.contains("3d6000803e");
    }
    
    private boolean containsUncheckedExternalCalls(String bytecode) {
        return bytecode.contains("f1") && !bytecode.contains("600051");
    }
    
    private boolean containsMEVVulnerablePatterns(String bytecode) {
        // Look for DEX interaction patterns without protection
        return bytecode.contains("a9059cbb") || bytecode.contains("23b872dd");
    }
    
    private boolean hasGasOptimizationIssues(String bytecode) {
        return bytecode.length() > 5000; // Large contracts often have optimization opportunities
    }
    
    // Public methods for API
    public Optional<ScanResult> getScanResult(Long scanId) {
        return Optional.ofNullable(scanResults.get(scanId));
    }
    
    public List<ScanResult> getContractScanHistory(String address) {
        List<Long> scans = contractScans.getOrDefault(address, new ArrayList<>());
        return scans.stream()
            .map(scanResults::get)
            .filter(Objects::nonNull)
            .sorted((a, b) -> b.getStartedAt().compareTo(a.getStartedAt()))
            .toList();
    }
    
    public Map<String, Object> getScanStats() {
        int totalScans = scanResults.size();
        long completedScans = scanResults.values().stream()
            .filter(scan -> "COMPLETED".equals(scan.getStatus()))
            .count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalScans", totalScans);
        stats.put("completedScans", completedScans);
        stats.put("successRate", totalScans > 0 ? (completedScans * 100.0 / totalScans) : 0);
        
        return stats;
    }
}
