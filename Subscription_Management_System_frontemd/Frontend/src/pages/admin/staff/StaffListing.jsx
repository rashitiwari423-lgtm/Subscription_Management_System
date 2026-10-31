import React, { useState, useEffect } from 'react';
import { Search, Plus, UserCheck, Shield, Mail, Phone, MoreHorizontal, RefreshCw, AlertTriangle, Power } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { staffService } from '../../../services/staffService';

export default function StaffListing() {
  const [staffList, setStaffList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const navigate = useNavigate();

  const loadStaff = async () => {
    try {
      setLoading(true);
      setError('');
      // Pass role:'STAFF' so the service filters out ADMIN/CUSTOMER users
      const data = await staffService.getAll({ role: 'STAFF' });
      const list = Array.isArray(data) ? data : (data?.content || []);
      setStaffList(list);
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to load staff directory');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadStaff();
  }, []);

  const handleToggleStatus = async (e, member) => {
    e.stopPropagation();
    try {
      const newStatus = member.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
      await staffService.updateStatus(member.id, newStatus);
      await loadStaff();
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to update user status');
    }
  };

  const filtered = staffList.filter(m => {
    if (!search) return true;
    const term = search.toLowerCase();
    return (
      (m.name && m.name.toLowerCase().includes(term)) ||
      (m.email && m.email.toLowerCase().includes(term)) ||
      (m.role && m.role.toLowerCase().includes(term))
    );
  });

  return (
    <div className="space-y-5">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Staff Operations & User Directory</h1>
          <p className="text-xs text-on-surface-variant">
            Manage operational team accounts, customer assignments, and enterprise access roles.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={loadStaff}
            className="p-2 rounded-lg border border-outline-variant/40 hover:bg-surface-container text-on-surface-variant"
            title="Refresh Staff"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          </button>
          <button
            onClick={() => navigate('/admin/staff/new')}
            className="px-3.5 py-2 rounded-lg bg-primary hover:bg-primary-container text-white text-xs font-semibold flex items-center gap-1.5 shadow-sm"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>Add Staff Member</span>
          </button>
        </div>
      </div>

      {error && (
        <div className="p-3 bg-error-container/20 border border-error/30 text-error rounded-xl text-xs flex justify-between">
          <span>{error}</span>
          <button onClick={() => setError('')} className="font-bold">×</button>
        </div>
      )}

      <div className="relative">
        <Search className="w-3.5 h-3.5 text-on-surface-variant absolute left-3 top-1/2 -translate-y-1/2" />
        <input
          type="text"
          placeholder="Search staff by name, email, or role..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="w-full pl-9 pr-3 py-1.5 rounded-lg border border-outline-variant/40 bg-surface-container-lowest text-xs text-on-surface"
        />
      </div>

      {loading ? (
        <div className="p-12 text-center text-on-surface-variant">
          <RefreshCw className="w-6 h-6 animate-spin mx-auto mb-2 text-primary" />
          <span className="text-xs">Loading operational staff...</span>
        </div>
      ) : filtered.length === 0 ? (
        <div className="p-8 text-center bg-surface-container-lowest rounded-xl border border-outline-variant/40 text-on-surface-variant text-xs">
          No staff members found matching criteria.
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filtered.map((member) => (
            <div
              key={member.id}
              onClick={() => navigate(`/admin/staff/${member.id}`)}
              className="bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-3 cursor-pointer hover:border-primary/40 transition-colors"
            >
              <div className="flex items-start justify-between">
                <div className="w-10 h-10 rounded-full bg-primary/10 text-primary font-bold flex items-center justify-center text-sm">
                  {member.name ? member.name.split(' ').map(n => n[0]).join('') : 'U'}
                </div>
                <div className="flex items-center gap-1.5">
                  <span className={`px-2 py-0.5 rounded-full text-[10px] font-semibold ${
                    member.status === 'ACTIVE'
                      ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                      : 'bg-surface-container text-on-surface-variant'
                  }`}>
                    {member.status}
                  </span>
                  <button
                    onClick={(e) => handleToggleStatus(e, member)}
                    className={`p-1 rounded hover:bg-surface-container ${
                      member.status === 'ACTIVE' ? 'text-amber-600' : 'text-emerald-600'
                    }`}
                    title={member.status === 'ACTIVE' ? 'Deactivate account' : 'Activate account'}
                  >
                    <Power className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>

              <div>
                <h3 className="font-bold text-sm text-on-surface">{member.name}</h3>
                <p className="text-xs text-primary font-medium">{member.role}</p>
                <p className="text-[11px] text-on-surface-variant font-mono mt-0.5">{member.email}</p>
              </div>

              <div className="pt-3 border-t border-surface-container text-xs flex justify-between items-center text-on-surface-variant">
                <span>Account ID:</span>
                <span className="font-mono text-on-surface">USR-{member.id}</span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
