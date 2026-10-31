import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Plus, Tag, CheckCircle2, Save, RefreshCw, AlertTriangle, Layers } from 'lucide-react';
import { productService } from '../../../services/productService';

export default function ProductVariants() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [product, setProduct] = useState(null);
  const [variants, setVariants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [showAdd, setShowAdd] = useState(false);
  const [newVariant, setNewVariant] = useState({ name: '', sku: '', description: '' });
  const [saving, setSaving] = useState(false);
  const [actionError, setActionError] = useState('');

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [p, vList] = await Promise.all([
        productService.getById(id),
        productService.getVariantsByProductId(id),
      ]);
      setProduct(p);
      setVariants(vList || []);
    } catch (err) {
      setError(err.userMessage || 'Failed to load product variants');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [id]);

  const handleAdd = async (e) => {
    e.preventDefault();
    setActionError('');
    setSaving(true);
    try {
      await productService.createVariant(id, {
        name: newVariant.name,
        sku: newVariant.sku,
        description: newVariant.description || '',
      });
      setShowAdd(false);
      setNewVariant({ name: '', sku: '', description: '' });
      loadData();
    } catch (err) {
      setActionError(err.userMessage || 'Failed to create variant');
    } finally {
      setSaving(false);
    }
  };

  const handleToggleStatus = async (v) => {
    const nextStatus = v.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    try {
      await productService.updateVariantStatus(v.id, nextStatus);
      loadData();
    } catch (err) {
      alert(err.userMessage || 'Failed to update variant status');
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center p-12 text-xs text-on-surface-variant gap-2">
        <RefreshCw className="w-4 h-4 animate-spin text-primary" />
        <span>Loading product variants...</span>
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="p-8 text-center text-xs text-error space-y-3">
        <AlertTriangle className="w-8 h-8 mx-auto" />
        <p>{error || 'Product not found'}</p>
        <button
          onClick={() => navigate('/admin/products')}
          className="px-3 py-1.5 bg-primary text-white rounded text-xs font-semibold"
        >
          Back to Catalog
        </button>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate(`/admin/products/${product.id}`)}
            className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container"
          >
            <ArrowLeft className="w-4 h-4" />
          </button>
          <div>
            <h1 className="text-xl font-bold text-on-surface">Product Variants (SKUs)</h1>
            <p className="text-xs text-on-surface-variant font-mono">
              Parent Product: {product.name} (ID: #{product.id})
            </p>
          </div>
        </div>

        <button
          onClick={() => setShowAdd(!showAdd)}
          className="px-3 py-1.5 bg-primary text-white text-xs font-semibold rounded-lg hover:bg-primary-container flex items-center gap-1 shadow-sm"
        >
          <Plus className="w-3.5 h-3.5" />
          <span>Add Variant SKU</span>
        </button>
      </div>

      {actionError && (
        <div className="p-3 bg-error-container/30 border border-error/30 rounded-xl text-xs text-error flex items-center gap-2">
          <AlertTriangle className="w-4 h-4 flex-shrink-0" />
          <span>{actionError}</span>
        </div>
      )}

      {showAdd && (
        <form onSubmit={handleAdd} className="p-5 bg-surface-container-lowest border border-primary/40 rounded-2xl shadow-elevation-1 space-y-3">
          <h2 className="text-xs font-bold text-primary uppercase">New Variant SKU Definition</h2>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <div>
              <label className="block text-[11px] font-medium text-on-surface mb-1">Variant Name *</label>
              <input
                type="text"
                required
                placeholder="e.g. Enterprise Dedicated"
                value={newVariant.name}
                onChange={(e) => setNewVariant({ ...newVariant, name: e.target.value })}
                className="w-full h-8 px-2.5 rounded-lg border border-outline-variant/60 text-xs"
              />
            </div>
            <div>
              <label className="block text-[11px] font-medium text-on-surface mb-1">SKU Code *</label>
              <input
                type="text"
                required
                placeholder="e.g. SKU-ENT-01"
                value={newVariant.sku}
                onChange={(e) => setNewVariant({ ...newVariant, sku: e.target.value })}
                className="w-full h-8 px-2.5 rounded-lg border border-outline-variant/60 text-xs font-mono uppercase"
              />
            </div>
            <div>
              <label className="block text-[11px] font-medium text-on-surface mb-1">Description</label>
              <input
                type="text"
                placeholder="Short description"
                value={newVariant.description}
                onChange={(e) => setNewVariant({ ...newVariant, description: e.target.value })}
                className="w-full h-8 px-2.5 rounded-lg border border-outline-variant/60 text-xs"
              />
            </div>
          </div>
          <div className="flex justify-end gap-2 pt-2">
            <button
              type="button"
              onClick={() => setShowAdd(false)}
              className="px-3 py-1 text-xs border border-outline-variant/40 rounded-lg text-on-surface-variant"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={saving}
              className="px-3 py-1 bg-primary text-white text-xs font-semibold rounded-lg disabled:opacity-50"
            >
              {saving ? 'Saving...' : 'Save Variant'}
            </button>
          </div>
        </form>
      )}

      <div className="bg-surface-container-lowest border border-outline-variant/40 rounded-xl shadow-elevation-1 overflow-hidden">
        {variants.length === 0 ? (
          <div className="p-8 text-center text-xs text-on-surface-variant">
            No variants created under this product yet. Click "Add Variant SKU" above to create one.
          </div>
        ) : (
          <table className="w-full text-left text-xs">
            <thead className="bg-surface-container-low text-on-surface-variant uppercase text-[10px] font-semibold border-b border-outline-variant/20">
              <tr>
                <th className="p-3.5">SKU Code</th>
                <th className="p-3.5">Variant Name</th>
                <th className="p-3.5">Description</th>
                <th className="p-3.5">Status</th>
                <th className="p-3.5 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-container">
              {variants.map(v => (
                <tr key={v.id} className="hover:bg-surface-container-low/50">
                  <td className="p-3.5 font-mono font-bold text-primary">{v.sku}</td>
                  <td className="p-3.5 font-bold text-on-surface">{v.name}</td>
                  <td className="p-3.5 text-on-surface-variant">{v.description || '—'}</td>
                  <td className="p-3.5">
                    <span className={`px-2 py-0.5 rounded-full text-[10px] font-semibold ${
                      v.status === 'ACTIVE' ? 'bg-emerald-50 text-emerald-700' : 'bg-surface-container text-on-surface-variant'
                    }`}>
                      {v.status}
                    </span>
                  </td>
                  <td className="p-3.5 text-right">
                    <div className="flex items-center justify-end gap-2">
                      <button
                        onClick={() => navigate(`/admin/plans/new?variantId=${v.id}`)}
                        className="px-2 py-1 bg-primary/10 hover:bg-primary/20 text-primary rounded text-xs font-semibold"
                      >
                        Add Plan
                      </button>
                      <button
                        onClick={() => handleToggleStatus(v)}
                        className="px-2 py-1 bg-surface-container hover:bg-surface-container-high text-on-surface-variant rounded text-xs font-semibold"
                      >
                        {v.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
