import React, { useState, useEffect } from 'react';
import { Package, Plus, Search, Tag, CheckCircle2, RefreshCw, AlertTriangle } from 'lucide-react';
import { productService } from '../../../services/productService';
import { useNavigate } from 'react-router-dom';

export default function ProductCatalog() {
  const [productList, setProductList] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const loadProducts = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await productService.getAll({ search: searchTerm });
      setProductList(data || []);
    } catch (err) {
      setError(err.userMessage || 'Failed to load products');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProducts();
  }, []);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    loadProducts();
  };

  return (
    <div className="space-y-5">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Product Catalog &amp; Variant Pricing</h1>
          <p className="text-xs text-on-surface-variant">
            Orchestrate catalog products, variants (SKUs), and pricing plans.
          </p>
        </div>
        <button
          onClick={() => navigate('/admin/products/new')}
          className="px-3.5 py-2 rounded-lg bg-primary hover:bg-primary-container text-white text-xs font-semibold flex items-center gap-1.5 shadow-sm"
        >
          <Plus className="w-3.5 h-3.5" />
          <span>Add Product</span>
        </button>
      </div>

      <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl flex items-center justify-between gap-3">
        <form onSubmit={handleSearchSubmit} className="relative w-full sm:w-80">
          <Search className="w-3.5 h-3.5 absolute left-3 top-2.5 text-outline" />
          <input
            type="text"
            placeholder="Search products (Press Enter)..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full h-8 pl-9 pr-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest focus:outline-none focus:border-primary-container"
          />
        </form>
        <button
          onClick={loadProducts}
          className="p-1.5 border border-outline-variant/60 rounded-lg hover:bg-surface-container text-outline"
          title="Refresh"
        >
          <RefreshCw className="w-3.5 h-3.5" />
        </button>
      </div>

      {loading ? (
        <div className="p-12 text-center text-xs text-on-surface-variant flex items-center justify-center gap-2">
          <RefreshCw className="w-4 h-4 animate-spin text-primary" />
          <span>Loading catalog...</span>
        </div>
      ) : error ? (
        <div className="p-6 text-center space-y-2 bg-error-container/20 rounded-xl border border-error/30">
          <AlertTriangle className="w-6 h-6 text-error mx-auto" />
          <p className="text-xs text-error font-medium">{error}</p>
          <button onClick={loadProducts} className="px-3 py-1 bg-primary text-white text-xs rounded">
            Retry
          </button>
        </div>
      ) : productList.length === 0 ? (
        <div className="p-12 bg-surface-container-lowest border border-outline-variant/40 rounded-xl text-center text-xs text-on-surface-variant">
          No products found in catalog. Click "Add Product" above to create one.
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {productList.map((prod) => (
            <div key={prod.id} className="bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-3">
              <div className="flex items-start justify-between">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-lg bg-primary/10 text-primary flex items-center justify-center font-bold">
                    <Package className="w-4 h-4" />
                  </div>
                  <div>
                    <h3 className="font-bold text-sm text-on-surface">{prod.name}</h3>
                    <span className="text-[10px] font-mono text-outline">{prod.category || 'Standard'}</span>
                  </div>
                </div>
                <span className={`px-2 py-0.5 rounded-full text-[10px] font-semibold ${
                  prod.status === 'ACTIVE' ? 'bg-emerald-50 text-emerald-700' : 'bg-surface-container text-on-surface-variant'
                }`}>
                  {prod.status}
                </span>
              </div>

              <p className="text-xs text-on-surface-variant line-clamp-2">{prod.description || 'No description provided.'}</p>

              <div className="pt-3 border-t border-surface-container flex items-center justify-between text-xs">
                <div>
                  <span className="text-[10px] text-outline uppercase">Product ID:</span>
                  <div className="font-bold font-mono text-on-surface">#{prod.id}</div>
                </div>
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => navigate(`/admin/products/${prod.id}/variants`)}
                    className="px-3 py-1 bg-primary/10 hover:bg-primary/20 rounded text-xs font-semibold text-primary"
                  >
                    View Variants
                  </button>
                  <button
                    onClick={() => navigate(`/admin/products/${prod.id}`)}
                    className="px-3 py-1 bg-surface-container hover:bg-surface-container-high rounded text-xs font-semibold text-on-surface"
                  >
                    Details
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
