package com.mevanalytics.platform.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ScanRequest {

    @NotBlank(message = "Contract address is required")
    @Pattern(regexp = "^0x[a-fA-F0-9]{40}$", message = "Invalid Ethereum address format")
    private String contractAddress;
    private String scanType;
    private Boolean includeMempoolAnalysis = true;
    private Boolean includeGasAnalysis;
    private Boolean useMachineLearning = false;
}
