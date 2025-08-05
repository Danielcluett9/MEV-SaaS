package com.mevanalytics.platform.controller;

import com.mevanalytics.platform.dto.ScanRequest;
import com.mevanalytics.platform.dto.ScanResult;
import com.mevanalytics.platform.service.ContractScannerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/scanner")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class ContractScannerController {
    
    @Autowired
    private ContractScannerService scannerService;
    
    @PostMapping("/scan")
    public CompletableFuture<ResponseEntity<ScanResult>> scanContract(@Valid @RequestBody ScanRequest request) {
        System.out.println("🔍 API: Received scan request for contract: " + request.getContractAddress());
        
        return scannerService.scanContractAsync(request)
            .thenApply(result -> ResponseEntity.ok(result))
            .exceptionally(throwable -> {
                System.err.println("❌ API: Scan failed: " + throwable.getMessage());
                return ResponseEntity.internalServerError().build();
            });
    }
    
    @GetMapping("/scan/{scanId}")
    public ResponseEntity<ScanResult> getScanResult(@PathVariable Long scanId) {
        return scannerService.getScanResult(scanId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/history/{address}")
    public ResponseEntity<List<ScanResult>> getContractScanHistory(@PathVariable String address) {
        List<ScanResult> results = scannerService.getContractScanHistory(address);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getScanStats() {
        Map<String, Object> stats = scannerService.getScanStats();
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Contract Scanner API is running!");
    }
}
