import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Package, CheckCircle2, RefreshCw, AlertTriangle, Layers, Plus } from 'lucide-react';
import { productService } from '../../../services/productService';

export default function ProductDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [product, setProduct] = useState(null);
  const [variants, setVariants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    setLoading(true);
    Promise.all([
      productService.getById(id),
      productService.getVariantsByProductId(id),
    ])
      .then(([prodData, variantsData]) => {
        setProduct(prodData);
        setVariants(variantsData || []);
      })
      .catch((err) => {
        setError(err.userMessage || 'Product not found');
      })
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) {
    return (
      <div className="flex items-center justify-center p-12 text-xs text-on-surface-variant gap-2">
        <RefreshCw className="w-4 h-4 animate-spin text-primary" />
        <span>Loading product details...</span>
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
    <div className="max-w-3xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate('/admin/products')}
            className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container"
          >
            <ArrowLeft className="w-4 h-4" />
          </button>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-xl font-bold text-on-surface">{product.name}</h1>
              <span className={`px-2 py-0.5 rounded-full text-[10px] font-semibold ${
                product.status === 'ACTIVE' ? 'bg-emerald-50 text-emerald-700' : 'bg-surface-container text-on-surface-variant'
              }`}>
                {product.status}
              </span>
            </div>
            <p className="text-xs text-on-surface-variant font-mono">ID: #{product.id} • Category: {product.category || 'General'}</p>
          </div>
        </div>

        <button
          onClick={() => navigate(`/admin/products/${product.id}/variants`)}
          className="px-3.5 py-1.5 bg-primary hover:bg-primary-container text-white text-xs font-semibold rounded-lg shadow-sm flex items-center gap-1.5"
        >
          <Layers className="w-3.5 h-3.5" />
          <span>Manage Variants</span>
        </button>
      </div>

      <div className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-4">
        <div className="grid grid-cols-2 gap-4 text-xs">
          <div>
            <span className="text-outline uppercase text-[10px]">Product Category</span>
            <div className="font-bold text-on-surface text-sm mt-0.5">{product.category || 'Subscription Software'}</div>
          </div>
          <div>
            <span className="text-outline uppercase text-[10px]">Associated Variants (SKUs)</span>
            <div className="font-bold text-primary text-sm mt-0.5">{variants.length} Configured</div>
          </div>
        </div>
        <div className="pt-3 border-t border-surface-container">
          <span className="text-outline uppercase text-[10px]">Description</span>
          <p className="text-xs text-on-surface-variant mt-1">{product.description || 'No description entered.'}</p>
        </div>
      </div>

      <div className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-sm font-bold text-on-surface">Configured Product Variants (SKUs)</h2>
          <button
            onClick={() => navigate(`/admin/products/${product.id}/variants`)}
            className="text-primary text-xs font-semibold hover:underline"
          >
            Configure Variants &rarr;
          </button>
        </div>

        {variants.length > 0 ? (
          <div className="space-y-2">
            {variants.map(v => (
              <div key={v.id} className="p-3 rounded-lg border border-outline-variant/30 flex items-center justify-between text-xs">
                <div>
                  <div className="font-bold text-on-surface">{v.name}</div>
                  <div className="text-[10px] text-outline font-mono">SKU: {v.sku}</div>
                </div>
                <span className={`px-2 py-0.5 rounded-full text-[10px] font-semibold ${
                  v.status === 'ACTIVE' ? 'bg-emerald-50 text-emerald-700' : 'bg-surface-container text-on-surface-variant'
                }`}>
                  {v.status}
                </span>
              </div>
            ))}
          </div>
        ) : (
          <p className="text-xs text-on-surface-variant">No variants configured under this product yet.</p>
        )}
      </div>
    </div>
  );
}
