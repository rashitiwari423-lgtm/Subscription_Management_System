import React, { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '../services/authService';

const AuthContext = createContext();

// Presets for UI convenience (e.g., Quick Fill on LoginPage)
export const DEMO_CREDENTIALS = {
  ADMIN: {
    email: 'admin@subscriptiq.com',
    password: 'Password@123',
    role: 'ADMIN',
    title: 'Platform Administrator',
    department: 'Executive Leadership'
  },
  STAFF: {
    email: 'staff@subscriptiq.com',
    password: 'Password@123',
    role: 'STAFF',
    title: 'Operations & Billing Executive',
    department: 'Customer Success & Ops'
  },
  CUSTOMER: {
    email: 'customer@subscriptiq.com',
    password: 'Password@123',
    role: 'CUSTOMER',
    title: 'Client Portal User',
    department: 'Client Representative'
  }
};

export const DEMO_USERS = DEMO_CREDENTIALS; // Alias for backward compatibility with UI components

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    try {
      const saved = localStorage.getItem('subscriptiq_user');
      return saved ? JSON.parse(saved) : null;
    } catch {
      return null;
    }
  });

  const [token, setToken] = useState(() => localStorage.getItem('subscriptiq_token') || null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (user) {
      localStorage.setItem('subscriptiq_user', JSON.stringify(user));
    } else {
      localStorage.removeItem('subscriptiq_user');
    }
  }, [user]);

  useEffect(() => {
    if (token) {
      localStorage.setItem('subscriptiq_token', token);
    } else {
      localStorage.removeItem('subscriptiq_token');
    }
  }, [token]);

  const loginWithCredentials = async (email, password) => {
    try {
      setLoading(true);
      const result = await authService.login(email, password);
      setUser(result.user);
      setToken(result.token);
      return { success: true, user: result.user };
    } catch (err) {
      const errorMsg = err.userMessage || err.response?.data?.message || 'Authentication failed. Please verify credentials.';
      return { success: false, error: errorMsg };
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    authService.logout();
    setUser(null);
    setToken(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        loginWithCredentials,
        logout,
        isAuthenticated: Boolean(user && token),
        isAdmin: user?.role === 'ADMIN',
        isStaff: user?.role === 'STAFF',
        isCustomer: user?.role === 'CUSTOMER'
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
