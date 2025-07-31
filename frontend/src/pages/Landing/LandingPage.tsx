import React, { useState, useEffect } from 'react';
import { TrendingUp, Shield, Zap, BarChart3, Users, DollarSign, ArrowRight, CheckCircle } from 'lucide-react';
import { Link } from 'react-router-dom';

const LandingPage = () => {
  const [stats, setStats] = useState({
    mevPrevented: 2547832,
    usersProtected: 15420,
    profitGenerated: 892456
  });

  useEffect(() => {
    // Animate numbers on page load
    const interval = setInterval(() => {
      setStats(prev => ({
        mevPrevented: prev.mevPrevented + Math.floor(Math.random() * 1000),
        usersProtected: prev.usersProtected + Math.floor(Math.random() * 10),
        profitGenerated: prev.profitGenerated + Math.floor(Math.random() * 500)
      }));
    }, 3000);

    return () => clearInterval(interval);
  }, []);

  const formatNumber = (num) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(num);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-blue-900 to-slate-800 text-white">
      {/* Navigation */}
      <nav className="px-6 py-4 backdrop-blur-sm bg-white/5 border-b border-white/10">
        <div className="max-w-7xl mx-auto flex justify-between items-center">
          <div className="flex items-center space-x-2">
            <Shield className="h-8 w-8 text-blue-400" />
            <span className="text-2xl font-bold bg-gradient-to-r from-blue-400 to-cyan-400 bg-clip-text text-transparent">
              MEVProtect
            </span>
          </div>
          <div className="hidden md:flex space-x-8">
            <Link to="/arbitrage" className="hover:text-blue-400 transition-colors">Live Arbitrage</Link>
            <Link to="/protection" className="hover:text-blue-400 transition-colors">Contract Scanner</Link>
            <Link to="/dashboard" className="hover:text-blue-400 transition-colors">Dashboard</Link>
          </div>
          <div className="flex space-x-4">
            <Link to="/auth/login" className="px-4 py-2 text-blue-400 hover:text-white transition-colors">
              Login
            </Link>
            <Link to="/auth/register" className="px-6 py-2 bg-gradient-to-r from-blue-500 to-cyan-500 rounded-lg hover:from-blue-600 hover:to-cyan-600 transition-all transform hover:scale-105">
              Start Free Trial
            </Link>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="px-6 py-20">
        <div className="max-w-7xl mx-auto text-center">
          <div className="inline-flex items-center px-4 py-2 bg-blue-500/20 rounded-full border border-blue-400/30 mb-8">
            <TrendingUp className="h-4 w-4 mr-2 text-blue-400" />
            <span className="text-sm">Live: $42,847 in MEV attacks prevented today</span>
          </div>
          
          <h1 className="text-5xl md:text-7xl font-bold mb-6 leading-tight">
            Stop Losing Money to
            <span className="block bg-gradient-to-r from-red-400 via-orange-400 to-yellow-400 bg-clip-text text-transparent">
              MEV Attacks
            </span>
          </h1>
          
          <p className="text-xl md:text-2xl text-slate-300 mb-12 max-w-4xl mx-auto leading-relaxed">
            Real-time MEV protection + profitable arbitrage opportunities. 
            <br />
            <span className="text-blue-400 font-semibold">Join 15,000+ traders</span> who've protected their transactions.
          </p>

          <div className="flex flex-col md:flex-row justify-center space-y-4 md:space-y-0 md:space-x-6 mb-16">
            <Link to="/protection" className="px-8 py-4 bg-gradient-to-r from-blue-500 to-cyan-500 rounded-lg text-lg font-semibold hover:from-blue-600 hover:to-cyan-600 transition-all transform hover:scale-105 flex items-center justify-center">
              Scan My Contract
              <ArrowRight className="ml-2 h-5 w-5" />
            </Link>
            <Link to="/arbitrage" className="px-8 py-4 border border-white/20 rounded-lg text-lg font-semibold hover:bg-white/10 transition-all">
              View Live Opportunities
            </Link>
          </div>

          {/* Live Stats */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-4xl mx-auto">
            <div className="bg-white/5 backdrop-blur-sm rounded-2xl p-6 border border-white/10">
              <div className="text-3xl font-bold text-green-400 mb-2">
                {formatNumber(stats.mevPrevented)}
              </div>
              <div className="text-slate-300">MEV Attacks Prevented</div>
              <div className="text-sm text-green-400 mt-1">+$12,847 today</div>
            </div>
            <div className="bg-white/5 backdrop-blur-sm rounded-2xl p-6 border border-white/10">
              <div className="text-3xl font-bold text-blue-400 mb-2">
                {stats.usersProtected.toLocaleString()}
              </div>
              <div className="text-slate-300">Users Protected</div>
              <div className="text-sm text-blue-400 mt-1">+147 this week</div>
            </div>
            <div className="bg-white/5 backdrop-blur-sm rounded-2xl p-6 border border-white/10">
              <div className="text-3xl font-bold text-yellow-400 mb-2">
                {formatNumber(stats.profitGenerated)}
              </div>
              <div className="text-slate-300">Arbitrage Profits</div>
              <div className="text-sm text-yellow-400 mt-1">+$5,234 today</div>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="px-6 py-20 bg-white/5">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-4xl md:text-5xl font-bold mb-6">
              Why Choose MEVProtect?
            </h2>
            <p className="text-xl text-slate-300 max-w-3xl mx-auto">
              The only platform that protects you from MEV attacks while helping you discover profitable opportunities.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="bg-gradient-to-br from-blue-500/20 to-cyan-500/20 rounded-2xl p-8 border border-blue-400/30">
              <Shield className="h-12 w-12 text-blue-400 mb-6" />
              <h3 className="text-2xl font-bold mb-4">Real-time Protection</h3>
              <p className="text-slate-300 mb-6">
                Monitor your transactions and get alerted 30 seconds before potential sandwich attacks. 
                Automatically adjust slippage and gas prices for maximum protection.
              </p>
              <ul className="space-y-2">
                <li className="flex items-center text-sm">
                  <CheckCircle className="h-4 w-4 text-green-400 mr-2" />
                  Mempool monitoring
                </li>
                <li className="flex items-center text-sm">
                  <CheckCircle className="h-4 w-4 text-green-400 mr-2" />
                  Instant alerts
                </li>
                <li className="flex items-center text-sm">
                  <CheckCircle className="h-4 w-4 text-green-400 mr-2" />
                  MEV-safe routing
                </li>
              </ul>
            </div>

            <div className="bg-gradient-to-br from-green-500/20 to-emerald-500/20 rounded-2xl p-8 border border-green-400/30">
              <TrendingUp className="h-12 w-12 text-green-400 mb-6" />
              <h3 className="text-2xl font-bold mb-4">Live Arbitrage Scanner</h3>
              <p className="text-slate-300 mb-6">
                Discover profitable arbitrage opportunities across 15+ DEXes. 
                Real-time profit calculations including gas costs and slippage.
              </p>
              <ul className="space-y-2">
                <li className="flex items-center text-sm">
                  <CheckCircle className="h-4 w-4 text-green-400 mr-2" />
                  Cross-DEX price monitoring
                </li>
                <li className="flex items-center text-sm">
                  <CheckCircle className="h-4 w-4 text-green-400 mr-2" />
                  Profit calculator
                </li>
                <li className="flex items-center text-sm">
                  <CheckCircle className="h-4 w-4 text-green-400 mr-2" />
                  Risk assessment
                </li>
              </ul>
            </div>

            <div className="bg-gradient-to-br from-purple-500/20 to-pink-500/20 rounded-2xl p-8 border border-purple-400/30">
              <BarChart3 className="h-12 w-12 text-purple-400 mb-6" />
              <h3 className="text-2xl font-bold mb-4">Contract Vulnerability Scan</h3>
              <p className="text-slate-300 mb-6">
                Upload any contract address to get a detailed vulnerability report. 
                Understand your MEV risk exposure and get protection recommendations.
              </p>
              <ul className="space-y-2">
                <li className="flex items-center text-sm">
                  <CheckCircle className="h-4 w-4 text-green-400 mr-2" />
                  Vulnerability scoring
                </li>
                <li className="flex items-center text-sm">
                  <CheckCircle className="h-4 w-4 text-green-400 mr-2" />
                  Protection recommendations
                </li>
                <li className="flex items-center text-sm">
                  <CheckCircle className="h-4 w-4 text-green-400 mr-2" />
                  Historical attack analysis
                </li>
              </ul>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="px-6 py-20 bg-gradient-to-r from-blue-600/20 to-cyan-600/20">
        <div className="max-w-4xl mx-auto text-center">
          <h2 className="text-4xl md:text-5xl font-bold mb-6">
            Ready to Protect Your Transactions?
          </h2>
          <p className="text-xl text-slate-300 mb-8">
            Join thousands of traders who've eliminated MEV losses and discovered profitable opportunities.
          </p>
          <div className="flex flex-col md:flex-row justify-center space-y-4 md:space-y-0 md:space-x-6">
            <Link to="/auth/register" className="px-8 py-4 bg-gradient-to-r from-blue-500 to-cyan-500 rounded-lg text-lg font-semibold hover:from-blue-600 hover:to-cyan-600 transition-all transform hover:scale-105 flex items-center justify-center">
              Start 7-Day Free Trial
              <ArrowRight className="ml-2 h-5 w-5" />
            </Link>
            <Link to="/dashboard" className="px-8 py-4 border border-white/20 rounded-lg text-lg font-semibold hover:bg-white/10 transition-all">
              View Demo Dashboard
            </Link>
          </div>
          <p className="text-sm text-slate-400 mt-4">
            No credit card required • 7-day free trial • Cancel anytime
          </p>
        </div>
      </section>
    </div>
  );
};

export default LandingPage;
