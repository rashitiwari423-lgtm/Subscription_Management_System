import React, { useState } from 'react';
import { ArrowLeft, Save, Package, AlertTriangle } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { productService } from '../../../services/productService';

export default function AddProduct() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    name: '',
    category: 'Subscription Software',
    description: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await productService.create(formData);
      navigate('/admin/products');
    } catch (err) {
      setError(err.userMessage || 'Failed to create product.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate('/admin/products')}
          className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container"
        >
          <ArrowLeft className="w-4 h-4" />
        </button>
        <div>
          <h1 className="text-xl font-bold text-on-surface">Define New Product SKU</h1>
          <p className="text-xs text-on-surface-variant">Create subscription catalog product entry.</p>
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
          <label className="block text-xs font-medium text-on-surface mb-1">Product Name *</label>
          <input
            type="text"
            required
            value={formData.name}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest"
            placeholder="e.g. SubscriptIQ Core Platform"
          />
        </div>

        <div>
          <label className="block text-xs font-medium text-on-surface mb-1">Category</label>
          <input
            type="text"
            value={formData.category}
            onChange={(e) => setFormData({ ...formData, category: e.target.value })}
            className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest"
            placeholder="e.g. Subscription Software, AI Add-on"
          />
        </div>

        <div>
          <label className="block text-xs font-medium text-on-surface mb-1">Product Description</label>
          <textarea
            rows="3"
            value={formData.description}
            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            className="w-full p-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest"
            placeholder="Detail product capabilities and target tier..."
          />
        </div>

        <div className="pt-4 border-t border-surface-container flex items-center justify-end gap-2">
          <button
            type="button"
            onClick={() => navigate('/admin/products')}
            className="px-4 py-2 border border-outline-variant/40 rounded-lg text-xs font-semibold text-on-surface-variant"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={loading}
            className="px-4 py-2 bg-primary hover:bg-primary-container text-white rounded-lg text-xs font-semibold flex items-center gap-1.5 shadow-sm disabled:opacity-50"
          >
            <Save className="w-3.5 h-3.5" />
            <span>{loading ? 'Publishing...' : 'Publish Product'}</span>
          </button>
        </div>
      </form>
    </div>
  );
}
