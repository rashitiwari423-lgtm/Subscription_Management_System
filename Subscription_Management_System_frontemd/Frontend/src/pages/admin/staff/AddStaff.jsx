import React, { useState } from 'react';
import { ArrowLeft, Save, UserCheck, Shield } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { staffService } from '../../../services/staffService';

export default function AddStaff() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: ''
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.name.trim() || !formData.email.trim() || !formData.password) {
      setError('Name, email, and password are required');
      return;
    }

    try {
      setSaving(true);
      setError('');
      await staffService.create({
        name: formData.name.trim(),
        email: formData.email.trim(),
        password: formData.password
      });
      navigate('/admin/staff');
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to create staff member');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate('/admin/staff')}
          className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container"
        >
          <ArrowLeft className="w-4 h-4" />
        </button>
        <div>
          <h1 className="text-xl font-bold text-on-surface">Provision Staff Member</h1>
          <p className="text-xs text-on-surface-variant">Add internal operations staff with backend STAFF role.</p>
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
          <label className="block text-xs font-medium text-on-surface mb-1">Full Legal Name *</label>
          <input
            type="text"
            required
            value={formData.name}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest"
            placeholder="e.g. Priya Sharma"
          />
        </div>

        <div>
          <label className="block text-xs font-medium text-on-surface mb-1">Corporate Email Address *</label>
          <input
            type="email"
            required
            value={formData.email}
            onChange={(e) => setFormData({ ...formData, email: e.target.value })}
            className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest font-mono"
            placeholder="priya.sharma@subscriptiq.com"
          />
        </div>

        <div>
          <label className="block text-xs font-medium text-on-surface mb-1">Temporary Initial Password *</label>
          <input
            type="password"
            required
            minLength={8}
            value={formData.password}
            onChange={(e) => setFormData({ ...formData, password: e.target.value })}
            className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest font-mono"
            placeholder="Minimum 8 characters (e.g. StaffPass@123)"
          />
        </div>

        <div className="pt-4 border-t border-surface-container flex items-center justify-end gap-2">
          <button
            type="button"
            onClick={() => navigate('/admin/staff')}
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
            <span>{saving ? 'Creating...' : 'Provision Staff Account'}</span>
          </button>
        </div>
      </form>
    </div>
  );
}
