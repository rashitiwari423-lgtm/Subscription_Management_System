import React, { useState } from 'react';
import { Package, Plus, Search, Tag, CheckCircle2 } from 'lucide-react';
import { products as initialProducts } from '../../../data/mockData';
import { useNavigate } from 'react-router-dom';

export default function ProductCatalog() {
  const [productList, setProductList] = useState(initialProducts || []);
  const navigate = useNavigate();

  return (
    <div className="space-y-5">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Product Catalog & Variant Pricing</h1>
          <p className="text-xs text-on-surface-variant">
            Orchestrate SKUs, add-on usage modules, and variant price schedules.
          </p>
        </div>
        <button
          onClick={() => navigate('/admin/products/new')}
          className="px-3.5 py-2 rounded-lg bg-primary hover:bg-primary-container text-white text-xs font-semibold flex items-center gap-1.5 shadow-sm"
        >
          <Plus className="w-3.5 h-3.5" />
          <span>Add Product SKU</span>
        </button>
      </div>

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
                  <span className="text-[10px] font-mono text-outline">{prod.code}</span>
                </div>
              </div>
              <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-50 text-emerald-700">
                {prod.status}
              </span>
            </div>

            <p className="text-xs text-on-surface-variant">{prod.description}</p>

            <div className="pt-3 border-t border-surface-container flex items-center justify-between text-xs">
              <div>
                <span className="text-[10px] text-outline uppercase">Base Price:</span>
                <div className="font-bold font-mono text-on-surface">₹{prod.basePrice.toLocaleString('en-IN')}/mo</div>
              </div>
              <div>
                <span className="text-[10px] text-outline uppercase">Variants:</span>
                <div className="font-bold font-mono text-primary">{prod.variantsCount} Configured</div>
              </div>
              <button
                onClick={() => navigate(`/admin/products/${prod.id}`)}
                className="px-3 py-1 bg-surface-container hover:bg-surface-container-high rounded text-xs font-semibold text-on-surface"
              >
                Inspect
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

