import React, { useState } from 'react';
import { ArrowLeft, Save, Percent } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { discountService } from '../../../services/discountService';

export default function CreateDiscount() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    name: '',
    type: 'PERCENTAGE',
    value: 10
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.name.trim()) {
      setError('Discount name is required');
      return;
    }
    if (formData.value === '' || Number(formData.value) < 0) {
      setError('Discount value must be 0 or positive');
      return;
    }

    try {
      setSaving(true);
      setError('');
      await discountService.create({
        name: formData.name.trim(),
        type: formData.type,
        value: Number(formData.value)
      });
      navigate('/admin/discounts');
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to publish discount');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate('/admin/discounts')}
          className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container"
        >
          <ArrowLeft className="w-4 h-4" />
        </button>
        <div>
          <h1 className="text-xl font-bold text-on-surface">Publish Promotional Coupon</h1>
          <p className="text-xs text-on-surface-variant">Configure discount rules and rebate structures.</p>
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
          <label className="block text-xs font-medium text-on-surface mb-1">Discount Campaign Title *</label>
          <input
            type="text"
            required
            placeholder="e.g. Q1 Enterprise Expansion Discount"
            value={formData.name}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest"
          />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-medium text-on-surface mb-1">Discount Type *</label>
            <select
              value={formData.type}
              onChange={(e) => setFormData({ ...formData, type: e.target.value })}
              className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest"
            >
              <option value="PERCENTAGE">Percentage Discount (%)</option>
              <option value="FIXED">Flat Deduction (₹)</option>
            </select>
          </div>
          <div>
            <label className="block text-xs font-medium text-on-surface mb-1">
              {formData.type === 'PERCENTAGE' ? 'Percentage Value (%) *' : 'Flat Amount (₹) *'}
            </label>
            <input
              type="number"
              step="0.01"
              min="0"
              required
              value={formData.value}
              onChange={(e) => setFormData({ ...formData, value: e.target.value })}
              className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest font-mono"
            />
          </div>
        </div>

        <div className="pt-4 border-t border-surface-container flex items-center justify-end gap-2">
          <button
            type="button"
            onClick={() => navigate('/admin/discounts')}
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
            <span>{saving ? 'Publishing...' : 'Publish Discount'}</span>
          </button>
        </div>
      </form>
    </div>
  );
}
