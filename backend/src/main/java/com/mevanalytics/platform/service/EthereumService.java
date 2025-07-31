package com.mevanalytics.platform.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class EthereumService {
    
    @Value("${blockchain.ethereum.enabled:true}")
    private boolean ethereumEnabled;
    
    @Value("${blockchain.ethereum.rpc-url}")
    private String rpcUrl;
    
    @Value("${blockchain.ethereum.fallback-rpc}")
    private String fallbackRpcUrl;
    
    private Web3j web3j;
    private boolean isConnected = false;
    private String connectionStatus = "Not connected";
    private ScheduledExecutorService scheduler;
    
    @PostConstruct
    public void initialize() {
        if (!ethereumEnabled) {
            System.out.println("🚫 Ethereum connection disabled");
            return;
        }
        
        System.out.println("🔗 Initializing Ethereum connection...");
        connectToEthereum();
        
        // Start health check scheduler
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::checkConnection, 30, 30, TimeUnit.SECONDS);
    }
    
    private void connectToEthereum() {
        try {
            String currentRpc = rpcUrl;
            
            // Check if using placeholder API key
            if (currentRpc.contains("YOUR_API_KEY_HERE")) {
                System.out.println("⚠️ No Alchemy API key configured, using public RPC");
                currentRpc = fallbackRpcUrl;
                connectionStatus = "Using public RPC (limited)";
            }
            
            System.out.println("🌐 Connecting to: " + currentRpc);
            
            web3j = Web3j.build(new HttpService(currentRpc));
            
            // Test connection
            Web3ClientVersion version = web3j.web3ClientVersion().send();
            if (version.hasError()) {
                throw new RuntimeException("Connection test failed: " + version.getError().getMessage());
            }
            
            isConnected = true;
            connectionStatus = "Connected to " + (currentRpc.contains("alchemy") ? "Alchemy" : "Public RPC");
            
            System.out.println("✅ Ethereum connected successfully!");
            System.out.println("📡 Client: " + version.getWeb3ClientVersion());
            
            // Get current block to verify
            EthBlockNumber blockNumber = web3j.ethBlockNumber().send();
            System.out.println("🧱 Current block: " + blockNumber.getBlockNumber());
            
        } catch (Exception e) {
            isConnected = false;
            connectionStatus = "Connection failed: " + e.getMessage();
            System.err.println("❌ Failed to connect to Ethereum: " + e.getMessage());
            
            // Try fallback if primary failed
            if (!rpcUrl.equals(fallbackRpcUrl)) {
                System.out.println("🔄 Trying fallback RPC...");
                try {
                    web3j = Web3j.build(new HttpService(fallbackRpcUrl));
                    Web3ClientVersion version = web3j.web3ClientVersion().send();
                    if (!version.hasError()) {
                        isConnected = true;
                        connectionStatus = "Connected to fallback RPC";
                        System.out.println("✅ Fallback connection successful!");
                    }
                } catch (Exception fallbackError) {
                    System.err.println("❌ Fallback connection also failed: " + fallbackError.getMessage());
                }
            }
        }
    }
    
    private void checkConnection() {
        if (web3j == null) return;
        
        try {
            EthBlockNumber blockNumber = web3j.ethBlockNumber().send();
            if (blockNumber.hasError()) {
                isConnected = false;
                connectionStatus = "Connection lost";
            } else {
                isConnected = true;
                // Update status periodically
                if (connectionStatus.contains("failed") || connectionStatus.contains("lost")) {
                    connectionStatus = "Connection restored";
                }
            }
        } catch (Exception e) {
            isConnected = false;
            connectionStatus = "Health check failed: " + e.getMessage();
        }
    }
    
    /**
     * Get latest block number
     */
    public CompletableFuture<BigInteger> getLatestBlockNumber() {
        if (!isConnected || web3j == null) {
            return CompletableFuture.completedFuture(BigInteger.ZERO);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                EthBlockNumber result = web3j.ethBlockNumber().send();
                return result.getBlockNumber();
            } catch (Exception e) {
                System.err.println("❌ Error getting latest block: " + e.getMessage());
                return BigInteger.ZERO;
            }
        });
    }
    
    /**
     * Get block with full transaction details
     */
    public CompletableFuture<EthBlock.Block> getBlock(BigInteger blockNumber) {
        if (!isConnected || web3j == null) {
            return CompletableFuture.completedFuture(null);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                EthBlock result = web3j.ethGetBlockByNumber(
                    org.web3j.protocol.core.DefaultBlockParameter.valueOf(blockNumber), 
                    true  // Include full transaction objects
                ).send();
                
                return result.getBlock();
            } catch (Exception e) {
                System.err.println("❌ Error getting block " + blockNumber + ": " + e.getMessage());
                return null;
            }
        });
    }
    
    /**
     * Get current gas price
     */
    public CompletableFuture<BigInteger> getGasPrice() {
        if (!isConnected || web3j == null) {
            return CompletableFuture.completedFuture(BigInteger.ZERO);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                EthGasPrice result = web3j.ethGasPrice().send();
                return result.getGasPrice();
            } catch (Exception e) {
                System.err.println("❌ Error getting gas price: " + e.getMessage());
                return BigInteger.ZERO;
            }
        });
    }
    
    /**
     * Get transaction receipt
     */
    public CompletableFuture<TransactionReceipt> getTransactionReceipt(String txHash) {
        if (!isConnected || web3j == null) {
            return CompletableFuture.completedFuture(null);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                EthGetTransactionReceipt result = web3j.ethGetTransactionReceipt(txHash).send();
                return result.getTransactionReceipt().orElse(null);
            } catch (Exception e) {
                System.err.println("❌ Error getting transaction receipt: " + e.getMessage());
                return null;
            }
        });
    }
    
    // ===== STATUS METHODS =====
    
    public boolean isConnected() {
        return isConnected;
    }
    
    public String getConnectionStatus() {
        return connectionStatus;
    }
    
    public String getRpcUrl() {
        if (rpcUrl.contains("YOUR_API_KEY_HERE")) {
            return fallbackRpcUrl + " (fallback)";
        }
        return rpcUrl.contains("alchemy") ? "Alchemy RPC" : "Public RPC";
    }
    
    @PreDestroy
    public void cleanup() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
        if (web3j != null) {
            web3j.shutdown();
        }
    }
}
