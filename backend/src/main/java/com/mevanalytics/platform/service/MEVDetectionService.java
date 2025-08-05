package com.mevanalytics.platform.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;

import jakarta.annotation.PostConstruct;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MEVDetectionService {
    
    @Autowired
    private EthereumService ethereumService;
    
    @Value("${mev.detection.enabled:true}")
    private boolean detectionEnabled;
    
    @Value("${mev.detection.scan-latest-blocks:50}")
    private int scanLatestBlocks;
    
    @Value("${mev.detection.min-profit-usd:0.1}")
    private double minProfitUsd;
    
    @Value("${dex.uniswap-v2}")
    private String uniswapV2Router;
    
    @Value("${dex.sushiswap}")
    private String sushiswapRouter;
    
    private BigInteger lastProcessedBlock = BigInteger.ZERO;
    private Set<String> knownDexRouters = new HashSet<>();
    private AtomicInteger totalMEVDetected = new AtomicInteger(0);
    private AtomicInteger sandwichAttacks = new AtomicInteger(0);
    private AtomicInteger arbitrageOps = new AtomicInteger(0);
    private BigDecimal totalExtractedValue = BigDecimal.ZERO;
    
    @PostConstruct
    public void initialize() {
        if (!detectionEnabled) {
            System.out.println("🚫 MEV detection disabled");
            return;
        }
        
        System.out.println("🔍 MEV Detection Service initializing...");
        
        // Add known DEX router addresses
        knownDexRouters.add(uniswapV2Router.toLowerCase());
        knownDexRouters.add(sushiswapRouter.toLowerCase());
        knownDexRouters.add("0x10ed43c718714eb63d5aa57b78b54704e256024e"); // PancakeSwap
        knownDexRouters.add("0xe592427a0aece92de3edee1f18e0157c05861564"); // Uniswap V3
        
        System.out.println("🎯 Monitoring " + knownDexRouters.size() + " DEX routers");
        System.out.println("💰 Minimum profit threshold: $" + minProfitUsd);
        
        // Initialize starting block
        initializeStartingBlock();
    }
    
    private void initializeStartingBlock() {
        ethereumService.getLatestBlockNumber().thenAccept(latestBlock -> {
            if (latestBlock.compareTo(BigInteger.ZERO) > 0) {
                lastProcessedBlock = latestBlock.subtract(BigInteger.valueOf(scanLatestBlocks));
                System.out.println("🎯 Starting MEV detection from block: " + lastProcessedBlock);
            }
        });
    }
    
    /**
     * Scheduled MEV detection - runs every 30 seconds
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 10000)
    public void scanForMEVTransactions() {
        if (!detectionEnabled || !ethereumService.isConnected()) {
            return;
        }
        
        System.out.println("🔍 Scanning for MEV transactions...");
        
        ethereumService.getLatestBlockNumber().thenAccept(latestBlock -> {
            if (lastProcessedBlock.equals(BigInteger.ZERO)) {
                lastProcessedBlock = latestBlock.subtract(BigInteger.valueOf(10));
            }
            
            // Process up to 5 blocks at a time to avoid overwhelming
            BigInteger endBlock = lastProcessedBlock.add(BigInteger.valueOf(5));
            if (endBlock.compareTo(latestBlock) > 0) {
                endBlock = latestBlock;
            }
            
            if (endBlock.compareTo(lastProcessedBlock) > 0) {
                System.out.println("📊 Processing blocks " + lastProcessedBlock + " to " + endBlock);
                
                for (BigInteger blockNum = lastProcessedBlock; blockNum.compareTo(endBlock) <= 0; blockNum = blockNum.add(BigInteger.ONE)) {
                    processBlockForMEV(blockNum);
                }
                
                lastProcessedBlock = endBlock;
            }
        });
    }
    
    private void processBlockForMEV(BigInteger blockNumber) {
        ethereumService.getBlock(blockNumber).thenAccept(block -> {
            if (block == null || block.getTransactions() == null) {
                return;
            }
            
            try {
                List<Transaction> transactions = new ArrayList<>();
                for (EthBlock.TransactionResult result : block.getTransactions()) {
                    transactions.add((Transaction) result.get());
                }
                
                if (transactions.size() > 1) {
                    // Detect different types of MEV
                    detectSandwichAttacks(block, transactions);
                    detectArbitrageOpportunities(block, transactions);
                    
                    if (totalMEVDetected.get() % 10 == 0 && totalMEVDetected.get() > 0) {
                        System.out.println("📈 MEV Summary: " + totalMEVDetected.get() + " total, " + 
                                         sandwichAttacks.get() + " sandwich, " + 
                                         arbitrageOps.get() + " arbitrage, $" + 
                                         totalExtractedValue.setScale(2, RoundingMode.HALF_UP) + " extracted");
                    }
                }
            } catch (Exception e) {
                System.err.println("❌ Error processing block " + blockNumber + ": " + e.getMessage());
            }
        });
    }
    
    /**
     * Detect sandwich attacks
     */
    private void detectSandwichAttacks(EthBlock.Block block, List<Transaction> transactions) {
        for (int i = 1; i < transactions.size() - 1; i++) {
            Transaction prevTx = transactions.get(i - 1);
            Transaction victimTx = transactions.get(i);
            Transaction nextTx = transactions.get(i + 1);
            
            if (isSandwichPattern(prevTx, victimTx, nextTx)) {
                BigDecimal profit = calculateSandwichProfit(prevTx, victimTx, nextTx);
                
                if (profit.doubleValue() >= minProfitUsd) {
                    sandwichAttacks.incrementAndGet();
                    totalMEVDetected.incrementAndGet();
                    totalExtractedValue = totalExtractedValue.add(profit);
                    
                    System.out.println("🥪 Sandwich attack detected in block " + block.getNumber() + 
                        " - Attacker: " + shortenAddress(prevTx.getFrom()) + 
                        " - Profit: $" + profit.setScale(2, RoundingMode.HALF_UP));
                }
            }
        }
    }
    
    /**
     * Detect arbitrage opportunities
     */
    private void detectArbitrageOpportunities(EthBlock.Block block, List<Transaction> transactions) {
        for (Transaction tx : transactions) {
            if (isArbitrageTransaction(tx)) {
                BigDecimal profit = calculateArbitrageProfit(tx);
                
                if (profit.doubleValue() >= minProfitUsd) {
                    arbitrageOps.incrementAndGet();
                    totalMEVDetected.incrementAndGet();
                    totalExtractedValue = totalExtractedValue.add(profit);
                    
                    System.out.println("⚖️ Arbitrage detected in block " + block.getNumber() + 
                        " - Trader: " + shortenAddress(tx.getFrom()) + 
                        " - Profit: $" + profit.setScale(2, RoundingMode.HALF_UP));
                }
            }
        }
    }
    
    // ===== DETECTION LOGIC =====
    
    private boolean isSandwichPattern(Transaction prevTx, Transaction victimTx, Transaction nextTx) {
        // Same attacker for front and back run
        boolean sameAttacker = prevTx.getFrom().equalsIgnoreCase(nextTx.getFrom());
        
        // All targeting DEX routers
        boolean allToDex = isValidDexTransaction(prevTx) && 
                          isValidDexTransaction(victimTx) && 
                          isValidDexTransaction(nextTx);
        
        // Higher gas price for attacker transactions (typical sandwich pattern)
        boolean higherGasPrice = prevTx.getGasPrice().compareTo(victimTx.getGasPrice()) >= 0 &&
                               nextTx.getGasPrice().compareTo(victimTx.getGasPrice()) >= 0;
        
        return sameAttacker && allToDex && higherGasPrice;
    }
    
    private boolean isArbitrageTransaction(Transaction tx) {
        if (!isValidDexTransaction(tx)) return false;
        
        // High gas price (willing to pay premium for speed)
        boolean highGas = tx.getGasPrice().compareTo(BigInteger.valueOf(30_000_000_000L)) > 0; // > 30 gwei
        
        // Has significant value
        boolean hasValue = tx.getValue().compareTo(BigInteger.valueOf(1000000000000000000L)) > 0; // > 1 ETH
        
        return highGas && hasValue;
    }
    
    private boolean isValidDexTransaction(Transaction tx) {
        return tx.getTo() != null && knownDexRouters.contains(tx.getTo().toLowerCase());
    }
    
    // ===== PROFIT CALCULATIONS =====
    
    private BigDecimal calculateSandwichProfit(Transaction frontRun, Transaction victim, Transaction backRun) {
        // Simplified calculation - estimate based on victim transaction value
        BigDecimal victimValueEth = weiToEth(victim.getValue());
        BigDecimal estimatedProfit = victimValueEth.multiply(BigDecimal.valueOf(0.002)); // 0.2% of victim value
        
        // Convert to USD (assuming $3000 ETH)
        return estimatedProfit.multiply(BigDecimal.valueOf(3000));
    }
    
    private BigDecimal calculateArbitrageProfit(Transaction tx) {
        // Estimate based on transaction value and gas price
        BigDecimal txValueEth = weiToEth(tx.getValue());
        BigDecimal estimatedProfitPercent = BigDecimal.valueOf(0.005); // 0.5%
        
        // Convert to USD
        return txValueEth.multiply(estimatedProfitPercent).multiply(BigDecimal.valueOf(3000));
    }
    
    private BigDecimal weiToEth(BigInteger wei) {
        return new BigDecimal(wei).divide(new BigDecimal("1000000000000000000"), 18, RoundingMode.HALF_UP);
    }
    
    private String shortenAddress(String address) {
        if (address == null || address.length() < 10) return address;
        return address.substring(0, 6) + "..." + address.substring(address.length() - 4);
    }
    
    // ===== PUBLIC GETTERS FOR DASHBOARD =====
    
    public int getTotalMEVDetected() {
        return totalMEVDetected.get();
    }
    
    public int getSandwichAttacks() {
        return sandwichAttacks.get();
    }
    
    public int getArbitrageOps() {
        return arbitrageOps.get();
    }
    
    public BigDecimal getTotalExtractedValue() {
        return totalExtractedValue;
    }
    
    public BigInteger getLastProcessedBlock() {
        return lastProcessedBlock;
    }
}
