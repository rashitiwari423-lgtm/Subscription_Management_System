import api from './api';

export const staffService = {
  /**
   * Fetch all users with STAFF role.
   * The backend /api/admin/users returns all roles — we fetch up to 200 and filter.
   */
  getAll: async (params = {}) => {
    const size = params.size ?? 200;
    const page = params.page ?? 0;
    const response = await api.get(`/admin/users?page=${page}&size=${size}`);
    const allUsers = response.data.data?.content || response.data.data || [];
    // Filter to STAFF role only unless caller explicitly asks for all
    return params.role === 'ALL' ? allUsers : allUsers.filter(u => u.role === 'STAFF');
  },

  getById: async (id) => {
    const response = await api.get(`/admin/users/${id}`);
    return response.data.data;
  },

  create: async (data) => {
    const response = await api.post('/admin/staff', {
      name: data.name,
      email: data.email,
      password: data.password || 'StaffPass@123',
    });
    return response.data.data;
  },

  updateStatus: async (id, status) => {
    const response = await api.patch(`/admin/users/${id}/status`, { status });
    return response.data.data;
  },
};

export default staffService;

