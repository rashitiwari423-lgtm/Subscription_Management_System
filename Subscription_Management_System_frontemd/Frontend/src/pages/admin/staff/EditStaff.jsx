import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Save, RefreshCw, AlertTriangle } from 'lucide-react';
import { staffService } from '../../../services/staffService';

export default function EditStaff() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [member, setMember] = useState(null);
  const [status, setStatus] = useState('ACTIVE');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    async function loadUser() {
      try {
        setLoading(true);
        setError('');
        const data = await staffService.getById(id);
        setMember(data);
        setStatus(data.status || 'ACTIVE');
      } catch (err) {
        setError(err?.response?.data?.message || err?.message || 'Failed to load user');
      } finally {
        setLoading(false);
      }
    }
    if (id) loadUser();
  }, [id]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setSaving(true);
      setError('');
      await staffService.updateStatus(id, status);
      navigate(`/admin/staff/${id}`);
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to update user status');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center p-12 text-center">
        <RefreshCw className="w-8 h-8 animate-spin text-primary mb-2" />
        <p className="text-xs text-on-surface-variant font-medium">Loading user account...</p>
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center gap-3">
        <button onClick={() => navigate(`/admin/staff/${id}`)} className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container">
          <ArrowLeft className="w-4 h-4" />
        </button>
        <div>
          <h1 className="text-xl font-bold text-on-surface">Manage Staff Status</h1>
          <p className="text-xs text-on-surface-variant font-mono">{member?.name} • USR-{id}</p>
        </div>
      </div>

      {error && (
        <div className="p-3 bg-error-container/20 border border-error/30 text-error rounded-xl text-xs flex justify-between">
          <span>{error}</span>
          <button onClick={() => setError('')} className="font-bold">×</button>
        </div>
      )}

      <form onSubmit={handleSubmit} className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-4">
        <div>
          <label className="block text-xs font-medium text-on-surface mb-1">User Name</label>
          <input
            type="text"
            disabled
            value={member?.name || ''}
            className="w-full h-9 px-3 rounded-lg border border-outline-variant/40 text-xs bg-surface-container text-on-surface-variant cursor-not-allowed"
          />
        </div>

        <div>
          <label className="block text-xs font-medium text-on-surface mb-1">Email Address</label>
          <input
            type="email"
            disabled
            value={member?.email || ''}
            className="w-full h-9 px-3 rounded-lg border border-outline-variant/40 text-xs bg-surface-container text-on-surface-variant cursor-not-allowed font-mono"
          />
        </div>

        <div>
          <label className="block text-xs font-medium text-on-surface mb-1">Account Status *</label>
          <select
            value={status}
            onChange={(e) => setStatus(e.target.value)}
            className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest font-medium"
          >
            <option value="ACTIVE">ACTIVE</option>
            <option value="INACTIVE">INACTIVE</option>
          </select>
        </div>

        <div className="pt-4 border-t border-surface-container flex justify-end gap-2">
          <button
            type="button"
            onClick={() => navigate(`/admin/staff/${id}`)}
            className="px-4 py-2 border border-outline-variant/40 rounded-lg text-xs font-semibold text-on-surface-variant hover:bg-surface-container"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={saving}
            className="px-4 py-2 bg-primary hover:bg-primary-container text-white rounded-lg text-xs font-semibold flex items-center gap-1.5 shadow-sm"
          >
            <Save className="w-3.5 h-3.5" />
            <span>{saving ? 'Updating...' : 'Save Changes'}</span>
          </button>
        </div>
      </form>
    </div>
  );
}
