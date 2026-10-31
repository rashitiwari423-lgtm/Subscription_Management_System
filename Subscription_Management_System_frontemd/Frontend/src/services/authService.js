import api from './api';

export const authService = {
  login: async (email, password) => {
    const response = await api.post('/auth/login', {
      email: (email || '').trim(),
      password: password || '',
    });
    // Backend returns ApiResponse<LoginResponse>: { success, message, data: { accessToken, tokenType, expiresIn, user } }
    const { accessToken, user } = response.data.data;
    if (accessToken) {
      localStorage.setItem('subscriptiq_token', accessToken);
      localStorage.setItem('subscriptiq_user', JSON.stringify(user));
    }
    return { token: accessToken, user };
  },

  register: async (name, email, password) => {
    const response = await api.post('/auth/register', {
      name: (name || '').trim(),
      email: (email || '').trim().toLowerCase(),
      password: password || '',
    });
    return response.data.data;
  },

  forgotPassword: async (email) => {
    const response = await api.post('/auth/forgot-password', {
      email: (email || '').trim().toLowerCase(),
    });
    return response.data;
  },

  resetPassword: async (token, newPassword) => {
    const response = await api.post('/auth/reset-password', {
      token: (token || '').trim(),
      newPassword: newPassword || '',
    });
    return response.data;
  },

  getCurrentUser: async () => {
    try {
      const response = await api.get('/test/me');
      return response.data.data;
    } catch {
      const saved = localStorage.getItem('subscriptiq_user');
      return saved ? JSON.parse(saved) : null;
    }
  },

  logout: () => {
    localStorage.removeItem('subscriptiq_token');
    localStorage.removeItem('subscriptiq_user');
  },
};

export default authService;
