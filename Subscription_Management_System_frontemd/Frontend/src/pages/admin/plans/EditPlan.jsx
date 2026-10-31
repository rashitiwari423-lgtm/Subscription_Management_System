import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Save, AlertTriangle, RefreshCw } from 'lucide-react';
import { planService } from '../../../services/planService';

export default function EditPlan() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [formData, setFormData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    setLoading(true);
    planService.getById(id)
      .then((p) => {
        setFormData({
          name: p.name || '',
          description: p.description || '',
          price: p.price || 0,
          billingCycle: p.billingCycle || 'MONTHLY',
          trialDays: p.trialDays || 0,
          currency: p.currency || 'INR',
        });
      })
      .catch((err) => setError(err.userMessage || 'Plan not found'))
      .finally(() => setLoading(false));
  }, [id]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSaving(true);
    try {
      await planService.update(id, formData);
      navigate(`/admin/plans/${id}`);
    } catch (err) {
      setError(err.userMessage || 'Failed to update plan.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center p-12 text-xs text-on-surface-variant gap-2">
        <RefreshCw className="w-4 h-4 animate-spin text-primary" />
        <span>Loading plan...</span>
      </div>
    );
  }

  if (error && !formData) {
    return (
      <div className="p-8 text-center text-xs text-error space-y-3">
        <AlertTriangle className="w-8 h-8 mx-auto" />
        <p>{error}</p>
        <button
          onClick={() => navigate('/admin/plans')}
          className="px-3 py-1.5 bg-primary text-white rounded text-xs font-semibold"
        >
          Back to Plans
        </button>
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate(`/admin/plans/${id}`)}
          className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container"
        >
          <ArrowLeft className="w-4 h-4" />
        </button>
        <div>
          <h1 className="text-xl font-bold text-on-surface">Edit Plan Configuration</h1>
          <p className="text-xs text-on-surface-variant font-mono">Plan ID: #{id}</p>
        </div>
      </div>

      {error && (
        <div className="p-3 bg-error-container/30 border border-error/30 rounded-xl text-xs text-error flex items-center gap-2">
          <AlertTriangle className="w-4 h-4 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      <form onSubmit={handleSubmit} className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-4">
        <div>
          <label className="block text-xs font-medium text-on-surface mb-1">Plan Display Name *</label>
          <input
            type="text"
            required
            value={formData.name}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest"
          />
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div>
            <label className="block text-xs font-medium text-on-surface mb-1">Billing Frequency</label>
            <select
              value={formData.billingCycle}
              onChange={(e) => setFormData({ ...formData, billingCycle: e.target.value })}
              className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest"
            >
              <option value="MONTHLY">MONTHLY</option>
              <option value="QUARTERLY">QUARTERLY</option>
              <option value="YEARLY">YEARLY</option>
            </select>
          </div>
          <div>
            <label className="block text-xs font-medium text-on-surface mb-1">Price (₹)</label>
            <input
              type="number"
              step="0.01"
              required
              value={formData.price}
              onChange={(e) => setFormData({ ...formData, price: parseFloat(e.target.value) })}
              className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs font-mono bg-surface-container-lowest"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-on-surface mb-1">Trial Days</label>
            <input
              type="number"
              min="0"
              value={formData.trialDays}
              onChange={(e) => setFormData({ ...formData, trialDays: parseInt(e.target.value, 10) || 0 })}
              className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs font-mono bg-surface-container-lowest"
            />
          </div>
        </div>

        <div>
          <label className="block text-xs font-medium text-on-surface mb-1">Description</label>
          <textarea
            rows="3"
            value={formData.description}
            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            className="w-full p-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest"
          />
        </div>

        <div className="pt-4 border-t border-surface-container flex justify-end gap-2">
          <button
            type="button"
            onClick={() => navigate(`/admin/plans/${id}`)}
            className="px-4 py-2 border rounded-lg text-xs font-semibold text-on-surface-variant hover:text-on-surface"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={saving}
            className="px-4 py-2 bg-primary hover:bg-primary-container text-white rounded-lg text-xs font-semibold flex items-center gap-1.5 disabled:opacity-50"
          >
            <Save className="w-3.5 h-3.5" />
            <span>{saving ? 'Updating...' : 'Update Plan'}</span>
          </button>
        </div>
      </form>
    </div>
  );
}
