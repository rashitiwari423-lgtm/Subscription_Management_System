import api from './api';
import { DEMO_USERS } from '../context/AuthContext';

export const authService = {
  login: async (email, password) => {
    // Simulated network delay
    await new Promise(r => setTimeout(r, 300));

    const trimmed = (email || '').trim().toLowerCase();
    let matched = Object.values(DEMO_USERS).find(u => u.email.toLowerCase() === trimmed);

    if (!matched) {
      if (trimmed.includes('staff')) matched = DEMO_USERS.STAFF;
      else if (trimmed.includes('customer')) matched = DEMO_USERS.CUSTOMER;
      else matched = DEMO_USERS.ADMIN;
    }

    const token = `jwt_mock_${matched.role.toLowerCase()}_${Date.now()}`;
    return {
      token,
      user: matched
    };
  },

  getCurrentUser: () => {
    try {
      const saved = localStorage.getItem('subscriptiq_user');
      return saved ? JSON.parse(saved) : null;
    } catch {
      return null;
    }
  },

  logout: () => {
    localStorage.removeItem('subscriptiq_user');
    localStorage.removeItem('subscriptiq_token');
  }
};

export default authService;
