import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Save, Landmark } from 'lucide-react';
import { taxService } from '../../../services/taxService';

export default function CreateTax() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    name: '',
    percentage: 18.00
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.name.trim()) {
      setError('Tax name is required');
      return;
    }
    if (formData.percentage < 0 || formData.percentage > 100) {
      setError('Percentage must be between 0 and 100');
      return;
    }

    try {
      setSaving(true);
      setError('');
      await taxService.create({
        name: formData.name.trim(),
        percentage: Number(formData.percentage)
      });
      navigate('/admin/taxes');
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to create tax rate');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center gap-3">
        <button onClick={() => navigate('/admin/taxes')} className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container">
          <ArrowLeft className="w-4 h-4" />
        </button>
        <div>
          <h1 className="text-xl font-bold text-on-surface">Create Statutory Tax Rule</h1>
          <p className="text-xs text-on-surface-variant">Add new tax rate and compliance percentage.</p>
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
          <label className="block text-xs font-medium text-on-surface mb-1">Tax Name *</label>
          <input
            type="text"
            required
            value={formData.name}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest"
            placeholder="e.g. IGST 18% or Export Zero-Rated"
          />
        </div>
        <div>
          <label className="block text-xs font-medium text-on-surface mb-1">Tax Percentage (%) *</label>
          <input
            type="number"
            step="0.01"
            min="0"
            max="100"
            required
            value={formData.percentage}
            onChange={(e) => setFormData({ ...formData, percentage: e.target.value })}
            className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs font-mono bg-surface-container-lowest"
          />
        </div>
        <div className="pt-4 border-t border-surface-container flex justify-end gap-2">
          <button type="button" onClick={() => navigate('/admin/taxes')} className="px-4 py-2 border rounded-lg text-xs font-semibold">
            Cancel
          </button>
          <button
            type="submit"
            disabled={saving}
            className="px-4 py-2 bg-primary text-white rounded-lg text-xs font-semibold flex items-center gap-1.5 shadow-sm"
          >
            <Save className="w-3.5 h-3.5" />
            <span>{saving ? 'Saving...' : 'Create Tax Rate'}</span>
          </button>
        </div>
      </form>
    </div>
  );
}
