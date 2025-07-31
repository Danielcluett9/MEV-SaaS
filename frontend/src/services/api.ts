import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/v1';

export interface DashboardData {
  totalExtracted: number;
  todayExtracted: number;
  sandwichAttacks: number;
  arbitrageOps: number;
  avgGasPrice: number;
  dailyData: Array<{
    date: string;
    extracted: number;
    attacks: number;
    arbitrage: number;
  }>;
  mevByStrategy: Array<{
    name: string;
    value: number;
    color: string;
  }>;
  topExtractors: Array<{
    rank: number;
    address: string;
    extracted: number;
    trades: number;
    winRate: number;
  }>;
  timestamp: string;
  version: string;
  status: string;
}

class APIService {
  private apiKey: string | null = null;
  
  constructor() {
    this.apiKey = localStorage.getItem('mev_api_key');
  }
  
  setAPIKey(key: string) {
    this.apiKey = key;
    localStorage.setItem('mev_api_key', key);
  }
  
  private getHeaders() {
    return {
      'Content-Type': 'application/json',
      ...(this.apiKey && { 'X-API-Key': this.apiKey })
    };
  }
  
  async getDashboardData(): Promise<DashboardData> {
    try {
      const response = await axios.get(`${API_BASE_URL}/analytics/dashboard`, {
        headers: this.getHeaders()
      });
      return response.data;
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
      throw new Error('Failed to fetch dashboard data');
    }
  }
  
  async generateAPIKey(email: string, tier: string) {
    try {
      const response = await axios.post(`${API_BASE_URL}/api-key/generate`, {
        email,
        tier
      }, {
        headers: this.getHeaders()
      });
      return response.data;
    } catch (error) {
      console.error('Error generating API key:', error);
      throw new Error('Failed to generate API key');
    }
  }
  
  async getHealthCheck() {
    try {
      const response = await axios.get(`${API_BASE_URL}/health`);
      return response.data;
    } catch (error) {
      console.error('Error checking health:', error);
      throw new Error('Backend health check failed');
    }
  }
  
  async getStatus() {
    try {
      const response = await axios.get(`${API_BASE_URL}/status`);
      return response.data;
    } catch (error) {
      console.error('Error fetching status:', error);
      throw new Error('Failed to fetch platform status');
    }
  }
}

export default new APIService();
