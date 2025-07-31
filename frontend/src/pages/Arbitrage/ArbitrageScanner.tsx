import React, { useState, useEffect } from 'react';
import { TrendingUp, DollarSign, AlertTriangle, RefreshCw, Filter, Copy, ExternalLink } from 'lucide-react';

const ArbitrageScanner = () => {
  const [opportunities, setOpportunities] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [filters, setFilters] = useState({
    minProfit: 10,
    maxRisk: 'MEDIUM',
    showOnlyProfitable: true
  });
  const [autoRefresh, setAutoRefresh] = useState(true);
  const [lastUpdate, setLastUpdate] = useState(new Date());

  // Generate mock opportunities (later replace with real API calls)
  const generateMockOpportunities = () => {
    const tokens = [
      { pair: 'WETH/USDC', symbol: 'WETH' },
      { pair: 'WBTC/USDT', symbol: 'WBTC' },
      { pair: 'UNI/WETH', symbol: 'UNI' },
      { pair: 'LINK/USDC', symbol: 'LINK' },
      { pair: 'AAVE/WETH', symbol: 'AAVE' },
      { pair: 'MATIC/USDC', symbol: 'MATIC' }
    ];

    const dexes = ['Uniswap V2', 'Uniswap V3', 'SushiSwap', '1inch', 'Balancer'];

    return tokens.map((token, index) => {
      const dexA = dexes[Math.floor(Math.random() * dexes.length)];
      let dexB = dexes[Math.floor(Math.random() * dexes.length)];
      while (dexB === dexA) {
        dexB = dexes[Math.floor(Math.random() * dexes.length)];
      }

      const basePrice = 1000 + Math.random() * 2000;
      const priceDiff = (Math.random() * 0.05 + 0.001);
      const priceA = basePrice;
      const priceB = basePrice * (1 + priceDiff);
      
      const gasCost = 15 + Math.random() * 25;
      const tradingAmount = 1000 + Math.random() * 5000;
      const profit = (priceDiff * tradingAmount) - gasCost;
      const profitPercent = (profit / tradingAmount) * 100;
      
      const liquidity = 50000 + Math.random() * 500000;
      const riskLevel = profit > 50 ? 'LOW' : profit > 20 ? 'MEDIUM' : 'HIGH';

      return {
        id: index,
        tokenPair: token.pair,
        symbol: token.symbol,
        dexA: { name: dexA, price: priceA },
        dexB: { name: dexB, price: priceB },
        profitPercent: profitPercent,
        gasCost: gasCost,
        netProfit: profit,
        tradingAmount: tradingAmount,
        riskLevel: riskLevel,
        liquidity: liquidity,
        lastUpdated: new Date(),
        isPositive: profit > 0
      };
    }).filter(op => filters.showOnlyProfitable ? op.isPositive : true)
      .filter(op => op.netProfit >= filters.minProfit)
      .sort((a, b) => b.netProfit - a.netProfit);
  };

  useEffect(() => {
    setOpportunities(generateMockOpportunities());
    setIsLoading(false);
    setLastUpdate(new Date());

    let interval;
    if (autoRefresh) {
      interval = setInterval(() => {
        setOpportunities(generateMockOpportunities());
        setLastUpdate(new Date());
      }, 15000);
    }

    return () => {
      if (interval) clearInterval(interval);
    };
  }, [filters, autoRefresh]);

  const handleRefresh = () => {
    setIsLoading(true);
    setTimeout(() => {
      setOpportunities(generateMockOpportunities());
      setLastUpdate(new Date());
      setIsLoading(false);
    }, 1000);
  };

  const getRiskColor = (risk) => {
    switch (risk) {
      case 'LOW': return 'text-green-400 bg-green-400/20';
      case 'MEDIUM': return 'text-yellow-400 bg-yellow-400/20';
      case 'HIGH': return 'text-red-400 bg-red-400/20';
      default: return 'text-gray-400 bg-gray-400/20';
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(amount);
  };

  const formatPercentage = (percent) => {
    return `${percent >= 0 ? '+' : ''}${percent.toFixed(3)}%`;
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-blue-900 to-slate-800 text-white p-6">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h1 className="text-4xl font-bold mb-2 flex items-center">
                <TrendingUp className="mr-3 h-10 w-10 text-green-400" />
                Live Arbitrage Scanner
              </h1>
              <p className="text-slate-300 text-lg">
                Real-time profitable arbitrage opportunities across 15+ DEXes
              </p>
            </div>
            <div className="flex space-x-4">
              <button
                onClick={handleRefresh}
                disabled={isLoading}
                className="flex items-center px-4 py-2 bg-blue-500/20 border border-blue-400/30 rounded-lg hover:bg-blue-500/30 transition-all disabled:opacity-50"
              >
                <RefreshCw className={`mr-2 h-4 w-4 ${isLoading ? 'animate-spin' : ''}`} />
                Refresh
              </button>
              <button
                onClick={() => setAutoRefresh(!autoRefresh)}
                className={`flex items-center px-4 py-2 rounded-lg border transition-all ${
                  autoRefresh 
                    ? 'bg-green-500/20 border-green-400/30 text-green-400' 
                    : 'bg-gray-500/20 border-gray-400/30 text-gray-400'
                }`}
              >
                Auto Refresh {autoRefresh ? 'ON' : 'OFF'}
              </button>
            </div>
          </div>

          {/* Stats Row */}
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
            <div className="bg-white/5 backdrop-blur-sm rounded-lg p-4 border border-white/10">
              <div className="text-2xl font-bold text-green-400">
                {opportunities.filter(op => op.netProfit > 0).length}
              </div>
              <div className="text-slate-300 text-sm">Profitable Opportunities</div>
            </div>
            <div className="bg-white/5 backdrop-blur-sm rounded-lg p-4 border border-white/10">
              <div className="text-2xl font-bold text-blue-400">
                {formatCurrency(opportunities.reduce((sum, op) => sum + (op.netProfit > 0 ? op.netProfit : 0), 0))}
              </div>
              <div className="text-slate-300 text-sm">Total Potential Profit</div>
            </div>
            <div className="bg-white/5 backdrop-blur-sm rounded-lg p-4 border border-white/10">
              <div className="text-2xl font-bold text-yellow-400">
                {opportunities.length > 0 ? formatPercentage(Math.max(...opportunities.map(op => op.profitPercent))) : '0%'}
              </div>
              <div className="text-slate-300 text-sm">Highest Profit %</div>
            </div>
            <div className="bg-white/5 backdrop-blur-sm rounded-lg p-4 border border-white/10">
              <div className="text-2xl font-bold text-purple-400">
                {lastUpdate.toLocaleTimeString()}
              </div>
              <div className="text-slate-300 text-sm">Last Updated</div>
            </div>
          </div>

          {/* Filters */}
          <div className="bg-white/5 backdrop-blur-sm rounded-lg p-4 border border-white/10 mb-6">
            <div className="flex items-center mb-4">
              <Filter className="mr-2 h-5 w-5 text-blue-400" />
              <span className="font-semibold">Filters</span>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label className="block text-sm text-slate-300 mb-2">Min Profit (USD)</label>
                <input
                  type="number"
                  value={filters.minProfit}
                  onChange={(e) => setFilters({...filters, minProfit: parseFloat(e.target.value)})}
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-600 rounded-lg text-white"
                  min="0"
                  step="1"
                />
              </div>
              <div>
                <label className="block text-sm text-slate-300 mb-2">Max Risk Level</label>
                <select
                  value={filters.maxRisk}
                  onChange={(e) => setFilters({...filters, maxRisk: e.target.value})}
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-600 rounded-lg text-white"
                >
                  <option value="LOW">Low Risk Only</option>
                  <option value="MEDIUM">Low + Medium Risk</option>
                  <option value="HIGH">All Risk Levels</option>
                </select>
              </div>
              <div className="flex items-end">
                <label className="flex items-center">
                  <input
                    type="checkbox"
                    checked={filters.showOnlyProfitable}
                    onChange={(e) => setFilters({...filters, showOnlyProfitable: e.target.checked})}
                    className="mr-2"
                  />
                  <span className="text-sm text-slate-300">Show only profitable</span>
                </label>
              </div>
            </div>
          </div>
        </div>

        {/* Opportunities Table */}
        <div className="bg-white/5 backdrop-blur-sm rounded-lg border border-white/10 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-white/10">
                  <th className="text-left p-4 font-semibold">Token Pair</th>
                  <th className="text-left p-4 font-semibold">Buy From</th>
                  <th className="text-left p-4 font-semibold">Sell To</th>
                  <th className="text-left p-4 font-semibold">Profit %</th>
                  <th className="text-left p-4 font-semibold">Net Profit</th>
                  <th className="text-left p-4 font-semibold">Risk</th>
                  <th className="text-left p-4 font-semibold">Actions</th>
                </tr>
              </thead>
              <tbody>
                {isLoading ? (
                  <tr>
                    <td colSpan="7" className="text-center p-8">
                      <div className="flex items-center justify-center">
                        <RefreshCw className="animate-spin mr-2 h-5 w-5" />
                        Scanning for opportunities...
                      </div>
                    </td>
                  </tr>
                ) : opportunities.length === 0 ? (
                  <tr>
                    <td colSpan="7" className="text-center p-8 text-slate-400">
                      No opportunities found matching your filters
                    </td>
                  </tr>
                ) : (
                  opportunities.map((opportunity) => (
                    <tr key={opportunity.id} className="border-b border-white/5 hover:bg-white/5 transition-colors">
                      <td className="p-4">
                        <div className="font-semibold text-blue-400">{opportunity.tokenPair}</div>
                        <div className="text-sm text-slate-400">
                          Trading: {formatCurrency(opportunity.tradingAmount)}
                        </div>
                      </td>
                      <td className="p-4">
                        <div className="font-medium">{opportunity.dexA.name}</div>
                        <div className="text-sm text-slate-400">
                          {formatCurrency(opportunity.dexA.price)}
                        </div>
                      </td>
                      <td className="p-4">
                        <div className="font-medium">{opportunity.dexB.name}</div>
                        <div className="text-sm text-slate-400">
                          {formatCurrency(opportunity.dexB.price)}
                        </div>
                      </td>
                      <td className="p-4">
                        <div className={`font-semibold ${opportunity.profitPercent > 0 ? 'text-green-400' : 'text-red-400'}`}>
                          {formatPercentage(opportunity.profitPercent)}
                        </div>
                      </td>
                      <td className="p-4">
                        <div className={`font-semibold ${opportunity.netProfit > 0 ? 'text-green-400' : 'text-red-400'}`}>
                          {formatCurrency(opportunity.netProfit)}
                        </div>
                        <div className="text-sm text-slate-400">
                          Gas: {formatCurrency(opportunity.gasCost)}
                        </div>
                      </td>
                      <td className="p-4">
                        <span className={`px-2 py-1 rounded-full text-xs font-medium ${getRiskColor(opportunity.riskLevel)}`}>
                          {opportunity.riskLevel}
                        </span>
                        <div className="text-xs text-slate-400 mt-1">
                          Liquidity: {formatCurrency(opportunity.liquidity)}
                        </div>
                      </td>
                      <td className="p-4">
                        <div className="flex space-x-2">
                          <button
                            className="p-2 bg-blue-500/20 hover:bg-blue-500/30 rounded-lg transition-all"
                            title="Copy arbitrage code"
                          >
                            <Copy className="h-4 w-4" />
                          </button>
                          <button
                            className="p-2 bg-green-500/20 hover:bg-green-500/30 rounded-lg transition-all"
                            title="Execute arbitrage"
                          >
                            <ExternalLink className="h-4 w-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>

        {/* Warning Notice */}
        <div className="mt-6 bg-yellow-500/10 border border-yellow-400/30 rounded-lg p-4">
          <div className="flex items-start">
            <AlertTriangle className="h-5 w-5 text-yellow-400 mr-3 mt-0.5" />
            <div>
              <div className="font-semibold text-yellow-400 mb-1">Important Notice</div>
              <div className="text-sm text-slate-300">
                Arbitrage opportunities are highly time-sensitive and competitive. Profits shown are estimates 
                and actual results may vary due to slippage, gas price fluctuations, and market movements. 
                Always test with small amounts first and consider MEV protection for your transactions.
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ArbitrageScanner;
