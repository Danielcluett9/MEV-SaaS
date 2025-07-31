import React, { useState } from 'react';
import { Shield, AlertTriangle, CheckCircle, Search } from 'lucide-react';

const MEVProtectionScanner = () => {
  const [contractAddress, setContractAddress] = useState('');
  const [isScanning, setIsScanning] = useState(false);
  const [scanResult, setScanResult] = useState(null);

  const sampleContracts = [
    { address: '0x1f9840a85d5aF5bf1D1762F925BDADdC4201F984', name: 'Uniswap Token (UNI)' },
    { address: '0xA0b86a33E6441c8e9dd0c4a1CD9eeAc7a5b9a5d7', name: 'Vulnerable DeFi Contract' },
    { address: '0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2', name: 'Wrapped Ether (WETH)' },
    { address: '0x6B175474E89094C44Da98b954EedeAC495271d0F', name: 'Dai Stablecoin (DAI)' }
  ];

  const scanContract = async () => {
    if (!contractAddress || !contractAddress.startsWith('0x')) {
      alert('Please enter a valid contract address starting with 0x');
      return;
    }

    setIsScanning(true);
    await new Promise(resolve => setTimeout(resolve, 3000));
    const mockResult = generateMockScanResult(contractAddress);
    setScanResult(mockResult);
    setIsScanning(false);
  };

  const generateMockScanResult = (address) => {
    const isHighRisk = Math.random() > 0.6;
    const riskScore = isHighRisk ? 60 + Math.random() * 40 : Math.random() * 50;
    
    const vulnerabilities = [
      {
        type: 'Sandwich Attack Vulnerability',
        severity: isHighRisk ? 'HIGH' : 'MEDIUM',
        description: 'Contract uses AMM swaps without slippage protection',
        recommendation: 'Implement dynamic slippage calculation and MEV-resistant routing',
        detected: isHighRisk || Math.random() > 0.5
      },
      {
        type: 'Front-running Risk',
        severity: 'MEDIUM',
        description: 'Transaction ordering dependency detected in token swaps',
        recommendation: 'Use commit-reveal schemes or private mempools',
        detected: Math.random() > 0.4
      }
    ].filter(v => v.detected);

    return {
      address,
      riskScore: Math.round(riskScore),
      riskLevel: riskScore > 70 ? 'HIGH' : riskScore > 40 ? 'MEDIUM' : 'LOW',
      vulnerabilities,
      totalLossUSD: isHighRisk ? 8900.50 : 2340.50,
      attacksLastMonth: isHighRisk ? 12 : 3,
      protectionRecommendations: [
        'Enable real-time MEV monitoring',
        'Use private mempool for sensitive transactions',
        'Implement dynamic slippage protection',
        'Monitor gas prices and use optimal timing'
      ],
      scanDate: new Date()
    };
  };

  const getRiskColor = (risk) => {
    switch (risk) {
      case 'LOW': return 'text-green-400 bg-green-400/20 border-green-400/30';
      case 'MEDIUM': return 'text-yellow-400 bg-yellow-400/20 border-yellow-400/30';
      case 'HIGH': return 'text-red-400 bg-red-400/20 border-red-400/30';
      default: return 'text-gray-400 bg-gray-400/20 border-gray-400/30';
    }
  };

  const getSeverityColor = (severity) => {
    switch (severity) {
      case 'LOW': return 'text-green-400';
      case 'MEDIUM': return 'text-yellow-400';
      case 'HIGH': return 'text-red-400';
      default: return 'text-gray-400';
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
    }).format(amount);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-blue-900 to-slate-800 text-white p-6">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-4xl font-bold mb-2 flex items-center">
            <Shield className="mr-3 h-10 w-10 text-blue-400" />
            MEV Protection Center
          </h1>
          <p className="text-slate-300 text-lg">
            Scan contracts for MEV vulnerabilities and get real-time protection recommendations
          </p>
        </div>

        {/* Scanner Section */}
        <div className="bg-white/5 backdrop-blur-sm rounded-lg border border-white/10 p-6 mb-8">
          <h2 className="text-2xl font-bold mb-4 flex items-center">
            <Search className="mr-2 h-6 w-6 text-blue-400" />
            Contract Vulnerability Scanner
          </h2>
          
          <div className="flex flex-col md:flex-row gap-4 mb-6">
            <div className="flex-1">
              <label className="block text-sm text-slate-300 mb-2">Contract Address</label>
              <input
                type="text"
                value={contractAddress}
                onChange={(e) => setContractAddress(e.target.value)}
                placeholder="0x1f9840a85d5aF5bf1D1762F925BDADdC4201F984"
                className="w-full px-4 py-3 bg-slate-800 border border-slate-600 rounded-lg text-white focus:border-blue-400 focus:outline-none"
              />
            </div>
            <div className="flex items-end">
              <button
                onClick={scanContract}
                disabled={isScanning}
                className="px-6 py-3 bg-gradient-to-r from-blue-500 to-cyan-500 rounded-lg font-semibold hover:from-blue-600 hover:to-cyan-600 transition-all disabled:opacity-50 flex items-center"
              >
                {isScanning ? (
                  <>
                    <div className="animate-spin mr-2 h-4 w-4 border-2 border-white border-t-transparent rounded-full"></div>
                    Scanning...
                  </>
                ) : (
                  <>
                    <Search className="mr-2 h-4 w-4" />
                    Scan Contract
                  </>
                )}
              </button>
            </div>
          </div>

          {/* Quick Test Contracts */}
          <div className="mb-4">
            <p className="text-sm text-slate-300 mb-2">Try these sample contracts:</p>
            <div className="flex flex-wrap gap-2">
              {sampleContracts.map((contract, index) => (
                <button
                  key={index}
                  onClick={() => setContractAddress(contract.address)}
                  className="px-3 py-1 bg-slate-700 hover:bg-slate-600 rounded-md text-sm transition-colors"
                >
                  {contract.name}
                </button>
              ))}
            </div>
          </div>
        </div>

        {/* Scan Results */}
        {scanResult && (
          <div className="space-y-6 mb-8">
            {/* Risk Overview */}
            <div className="bg-white/5 backdrop-blur-sm rounded-lg border border-white/10 p-6">
              <h3 className="text-xl font-bold mb-4">Risk Assessment</h3>
              <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                <div className="text-center">
                  <div className={`inline-flex items-center px-4 py-2 rounded-full border font-semibold ${getRiskColor(scanResult.riskLevel)}`}>
                    <Shield className="mr-2 h-4 w-4" />
                    {scanResult.riskLevel} RISK
                  </div>
                  <div className="text-3xl font-bold mt-2">{scanResult.riskScore}/100</div>
                  <div className="text-sm text-slate-400">Risk Score</div>
                </div>
                <div className="text-center">
                  <div className="text-3xl font-bold text-red-400">{formatCurrency(scanResult.totalLossUSD)}</div>
                  <div className="text-sm text-slate-400">Total MEV Losses</div>
                </div>
                <div className="text-center">
                  <div className="text-3xl font-bold text-yellow-400">{scanResult.attacksLastMonth}</div>
                  <div className="text-sm text-slate-400">Attacks This Month</div>
                </div>
                <div className="text-center">
                  <div className="text-3xl font-bold text-blue-400">{scanResult.vulnerabilities.length}</div>
                  <div className="text-sm text-slate-400">Vulnerabilities Found</div>
                </div>
              </div>
            </div>

            {/* Vulnerabilities */}
            <div className="bg-white/5 backdrop-blur-sm rounded-lg border border-white/10 p-6">
              <h3 className="text-xl font-bold mb-4">Detected Vulnerabilities</h3>
              {scanResult.vulnerabilities.length === 0 ? (
                <div className="text-center py-8">
                  <CheckCircle className="h-12 w-12 text-green-400 mx-auto mb-3" />
                  <p className="text-green-400 font-semibold">No major vulnerabilities detected!</p>
                </div>
              ) : (
                <div className="space-y-4">
                  {scanResult.vulnerabilities.map((vuln, index) => (
                    <div key={index} className="border border-slate-600 rounded-lg p-4">
                      <div className="flex items-start justify-between mb-2">
                        <div className="flex items-center">
                          <AlertTriangle className={`h-5 w-5 mr-2 ${getSeverityColor(vuln.severity)}`} />
                          <h4 className="font-semibold">{vuln.type}</h4>
                        </div>
                        <span className={`px-2 py-1 rounded text-xs font-medium ${getSeverityColor(vuln.severity)}`}>
                          {vuln.severity}
                        </span>
                      </div>
                      <p className="text-slate-300 text-sm mb-3">{vuln.description}</p>
                      <div className="bg-blue-500/10 border border-blue-400/30 rounded-lg p-3">
                        <p className="text-blue-400 text-sm font-medium">💡 Recommendation:</p>
                        <p className="text-slate-300 text-sm mt-1">{vuln.recommendation}</p>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Protection Recommendations */}
            <div className="bg-gradient-to-r from-green-500/10 to-blue-500/10 border border-green-400/30 rounded-lg p-6">
              <h3 className="text-xl font-bold mb-4 flex items-center">
                <Shield className="mr-2 h-6 w-6 text-green-400" />
                Protection Recommendations
              </h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {scanResult.protectionRecommendations.map((rec, index) => (
                  <div key={index} className="flex items-start">
                    <CheckCircle className="h-5 w-5 text-green-400 mr-3 mt-0.5 flex-shrink-0" />
                    <span className="text-slate-300">{rec}</span>
                  </div>
                ))}
              </div>
              <div className="mt-6 pt-6 border-t border-green-400/30">
                <button className="px-6 py-3 bg-gradient-to-r from-green-500 to-blue-500 rounded-lg font-semibold hover:from-green-600 hover:to-blue-600 transition-all">
                  Enable Real-time Protection
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default MEVProtectionScanner;
