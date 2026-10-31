import React, { useState, useEffect } from 'react';
import { ArrowLeft, Save, Layers, AlertTriangle, RefreshCw } from 'lucide-react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { planService } from '../../../services/planService';
import { productService } from '../../../services/productService';

export default function CreatePlan() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const initialVariantId = searchParams.get('variantId') || '';

  const [variantsList, setVariantsList] = useState([]);
  const [variantId, setVariantId] = useState(initialVariantId);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    billingCycle: 'MONTHLY',
    price: 9999,
    currency: 'INR',
    trialDays: 0,
  });
  const [loadingVariants, setLoadingVariants] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    // Load products and variants so user can pick target variant
    const loadVariants = async () => {
      setLoadingVariants(true);
      try {
        const products = await productService.getAll();
        const allVariants = [];
        for (const p of products) {
          const vList = await productService.getVariantsByProductId(p.id);
          for (const v of vList) {
            allVariants.push({
              ...v,
              productName: p.name,
            });
          }
        }
        setVariantsList(allVariants);
        if (!variantId && allVariants.length > 0) {
          setVariantId(String(allVariants[0].id));
        }
      } catch (err) {
        console.warn('Could not load variant list:', err);
      } finally {
        setLoadingVariants(false);
      }
    };
    loadVariants();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!variantId) {
      setError('A product variant must be selected.');
      return;
    }
    setError('');
    setSaving(true);
    try {
      await planService.create(variantId, formData);
      navigate('/admin/plans');
    } catch (err) {
      setError(err.userMessage || 'Failed to create plan.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate('/admin/plans')}
          className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container"
        >
          <ArrowLeft className="w-4 h-4" />
        </button>
        <div>
          <h1 className="text-xl font-bold text-on-surface">Design Pricing Plan</h1>
          <p className="text-xs text-on-surface-variant">Configure recurring price, billing frequencies, and trial periods.</p>
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
          <label className="block text-xs font-medium text-on-surface mb-1">Target Product Variant (Parent SKU) *</label>
          {loadingVariants ? (
            <div className="text-xs text-on-surface-variant flex items-center gap-2">
              <RefreshCw className="w-3.5 h-3.5 animate-spin" />
              <span>Loading variants...</span>
            </div>
          ) : variantsList.length > 0 ? (
            <select
              value={variantId}
              onChange={(e) => setVariantId(e.target.value)}
              className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest"
              required
            >
              {variantsList.map((v) => (
                <option key={v.id} value={v.id}>
                  {v.productName} — {v.name} ({v.sku})
                </option>
              ))}
            </select>
          ) : (
            <div className="p-2.5 rounded-lg bg-surface-container text-xs text-on-surface-variant">
              No product variants found. Please create a product &amp; variant in the Catalog first.
            </div>
          )}
        </div>

        <div>
          <label className="block text-xs font-medium text-on-surface mb-1">Plan Display Name *</label>
          <input
            type="text"
            required
            value={formData.name}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest"
            placeholder="e.g. Enterprise Monthly Pro"
          />
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div>
            <label className="block text-xs font-medium text-on-surface mb-1">Billing Cycle *</label>
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
            <label className="block text-xs font-medium text-on-surface mb-1">Price (INR) *</label>
            <input
              type="number"
              min="0"
              step="0.01"
              required
              value={formData.price}
              onChange={(e) => setFormData({ ...formData, price: parseFloat(e.target.value) })}
              className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest font-mono"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-on-surface mb-1">Trial Grace Days</label>
            <input
              type="number"
              min="0"
              value={formData.trialDays}
              onChange={(e) => setFormData({ ...formData, trialDays: parseInt(e.target.value, 10) || 0 })}
              className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest font-mono"
            />
          </div>
        </div>

        <div>
          <label className="block text-xs font-medium text-on-surface mb-1">Plan Description</label>
          <textarea
            rows="3"
            value={formData.description}
            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            className="w-full p-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest"
            placeholder="Describe plan entitlements, license limits, and service terms..."
          />
        </div>

        <div className="pt-4 border-t border-surface-container flex items-center justify-end gap-2">
          <button
            type="button"
            onClick={() => navigate('/admin/plans')}
            className="px-4 py-2 border border-outline-variant/40 rounded-lg text-xs font-semibold text-on-surface-variant"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={saving || !variantId}
            className="px-4 py-2 bg-primary hover:bg-primary-container text-white rounded-lg text-xs font-semibold flex items-center gap-1.5 shadow-sm disabled:opacity-50"
          >
            <Save className="w-3.5 h-3.5" />
            <span>{saving ? 'Publishing...' : 'Publish Billing Plan'}</span>
          </button>
        </div>
      </form>
    </div>
  );
}
