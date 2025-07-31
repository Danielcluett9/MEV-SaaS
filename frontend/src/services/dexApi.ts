// src/services/dexApi.ts
export const fetchDEXPrices = async () => {
  try {
    // CoinGecko API for token prices
    const response = await fetch(
      'https://api.coingecko.com/api/v3/simple/price?ids=ethereum,bitcoin,uniswap&vs_currencies=usd'
    );
    const data = await response.json();
    
    // 1inch API for DEX prices (you'll need to implement this)
    // const uniswapPrices = await fetch('https://api.1inch.io/v5.0/1/quote?...');
    
    return data;
  } catch (error) {
    console.error('Error fetching DEX prices:', error);
    return null;
  }
};

export const scanContractForMEV = async (contractAddress: string) => {
  try {
    // Call your backend MEV detection endpoint
    const response = await fetch(`http://localhost:8080/api/v1/mev/scan-contract`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ contractAddress })
    });
    
    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Error scanning contract:', error);
    return null;
  }
};
