import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Save, RefreshCw, AlertTriangle } from 'lucide-react';
import { taxService } from '../../../services/taxService';

export default function EditTax() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    name: '',
    percentage: 18.00
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    async function loadTax() {
      try {
        setLoading(true);
        setError('');
        const tax = await taxService.getById(id);
        setFormData({
          name: tax.name || '',
          percentage: tax.percentage ?? 18.00
        });
      } catch (err) {
        setError(err?.response?.data?.message || err?.message || 'Failed to load tax rate');
      } finally {
        setLoading(false);
      }
    }
    if (id) loadTax();
  }, [id]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.name.trim()) {
      setError('Tax name is required');
      return;
    }
    try {
      setSaving(true);
      setError('');
      await taxService.update(id, {
        name: formData.name.trim(),
        percentage: Number(formData.percentage)
      });
      navigate('/admin/taxes');
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to update tax rate');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center p-12 text-center">
        <RefreshCw className="w-8 h-8 animate-spin text-primary mb-2" />
        <p className="text-xs text-on-surface-variant font-medium">Loading tax configuration...</p>
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate('/admin/taxes')}
          className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container"
        >
          <ArrowLeft className="w-4 h-4" />
        </button>
        <div>
          <h1 className="text-xl font-bold text-on-surface">Configure Tax Jurisdiction</h1>
          <p className="text-xs text-on-surface-variant font-mono">TAX-{id}</p>
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
          <label className="block text-xs font-medium text-on-surface mb-1">Tax Regime Name *</label>
          <input
            type="text"
            required
            value={formData.name}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest"
          />
        </div>

        <div>
          <label className="block text-xs font-medium text-on-surface mb-1">Statutory Rate (%) *</label>
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
          <button
            type="button"
            onClick={() => navigate('/admin/taxes')}
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
            <span>{saving ? 'Updating...' : 'Update Tax Rule'}</span>
          </button>
        </div>
      </form>
    </div>
  );
}
