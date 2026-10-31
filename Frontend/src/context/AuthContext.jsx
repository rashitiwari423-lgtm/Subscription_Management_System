import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext();

export const DEMO_USERS = {
  ADMIN: {
    id: 'USR-001',
    name: 'Elena Vance',
    email: 'admin@subscriptiq.com',
    password: 'password123',
    role: 'ADMIN',
    avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&h=100&fit=crop&crop=faces',
    title: 'VP of Finance & Operations',
    department: 'Executive Leadership'
  },
  STAFF: {
    id: 'USR-002',
    name: 'Aarav Patel',
    email: 'staff@subscriptiq.com',
    password: 'password123',
    role: 'STAFF',
    avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop&crop=faces',
    title: 'Senior Account Executive',
    department: 'Customer Success & Ops'
  },
  CUSTOMER: {
    id: 'USR-003',
    name: 'Rahul Gupta',
    email: 'customer@subscriptiq.com',
    password: 'password123',
    role: 'CUSTOMER',
    avatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100&h=100&fit=crop&crop=faces',
    title: 'CTO @ Nexus Dynamics',
    company: 'Nexus Dynamics',
    customerId: 'C001'
  }
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    try {
      const saved = localStorage.getItem('subscriptiq_user');
      return saved ? JSON.parse(saved) : DEMO_USERS.ADMIN;
    } catch {
      return DEMO_USERS.ADMIN;
    }
  });

  const [token, setToken] = useState(() => localStorage.getItem('subscriptiq_token') || 'mock-jwt-token-xyz');

  useEffect(() => {
    if (user) {
      localStorage.setItem('subscriptiq_user', JSON.stringify(user));
    } else {
      localStorage.removeItem('subscriptiq_user');
    }
  }, [user]);

  const loginWithCredentials = (email, password) => {
    const trimmedEmail = email.trim().toLowerCase();
    const trimmedPassword = password.trim();

    // Strict match against demo users — email AND password must both match
    const matched = Object.values(DEMO_USERS).find(
      u => u.email.toLowerCase() === trimmedEmail && u.password === trimmedPassword
    );

    if (matched) {
      // Strip password before storing in state/localStorage
      const { password: _pw, ...safeUser } = matched;
      setUser(safeUser);
      const mockJwt = `mock-token-${safeUser.role.toLowerCase()}-${Date.now()}`;
      setToken(mockJwt);
      localStorage.setItem('subscriptiq_token', mockJwt);
      return { success: true, user: safeUser };
    }

    // No match — return failure
    return { success: false, error: 'Invalid email or password. Please use the demo credentials shown on screen.' };
  };

  const loginByRole = (role) => {
    const profile = DEMO_USERS[role] || DEMO_USERS.ADMIN;
    setUser(profile);
    const mockJwt = `mock-token-${role.toLowerCase()}-${Date.now()}`;
    setToken(mockJwt);
    localStorage.setItem('subscriptiq_token', mockJwt);
    return profile;
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('subscriptiq_user');
    localStorage.removeItem('subscriptiq_token');
  };

  const switchRole = (newRole) => {
    return loginByRole(newRole);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        login: loginByRole,
        loginWithCredentials,
        loginByRole,
        logout,
        switchRole,
        isAuthenticated: !!user,
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
