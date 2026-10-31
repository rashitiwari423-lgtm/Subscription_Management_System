import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Plus, Tag, CheckCircle2, Save } from 'lucide-react';
import { products } from '../../../data/mockData';

export default function ProductVariants() {
  const { id } = useParams();
  const navigate = useNavigate();
  const prod = products.find(p => p.id === id) || products[0];

  const [variants, setVariants] = useState([
    { id: 'VAR-01', name: 'Starter Tier Base', code: 'VAR-START', price: 4999, billingPeriod: 'Monthly', status: 'Active' },
    { id: 'VAR-02', name: 'Annual Growth Commitment', code: 'VAR-GROWTH-YR', price: 199990, billingPeriod: 'Annual', status: 'Active' },
    { id: 'VAR-03', name: 'Dedicated Multi-Tenant SLA', code: 'VAR-ENTERPRISE-SLA', price: 45000, billingPeriod: 'Monthly', status: 'Active' }
  ]);

  const [newVariant, setNewVariant] = useState({ name: '', code: '', price: 10000, billingPeriod: 'Monthly' });
  const [showAdd, setShowAdd] = useState(false);

  const handleAdd = (e) => {
    e.preventDefault();
    setVariants([...variants, { ...newVariant, id: `VAR-0${variants.length + 1}`, status: 'Active' }]);
    setShowAdd(false);
    setNewVariant({ name: '', code: '', price: 10000, billingPeriod: 'Monthly' });
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate(`/admin/products/${prod.id}`)}
            className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container"
          >
            <ArrowLeft className="w-4 h-4" />
          </button>
          <div>
            <h1 className="text-xl font-bold text-on-surface">Product Variants & Tier Pricing</h1>
            <p className="text-xs text-on-surface-variant font-mono">{prod.name} • {prod.code}</p>
          </div>
        </div>

        <button
          onClick={() => setShowAdd(!showAdd)}
          className="px-3 py-1.5 bg-primary text-white text-xs font-semibold rounded-lg hover:bg-primary-container flex items-center gap-1 shadow-sm"
        >
          <Plus className="w-3.5 h-3.5" />
          <span>Add Variant Tier</span>
        </button>
      </div>

      {showAdd && (
        <form onSubmit={handleAdd} className="p-4 bg-surface-container-lowest border border-primary/40 rounded-xl space-y-3">
          <h2 className="text-xs font-bold text-primary uppercase">New Variant Schedule</h2>
          <div className="grid grid-cols-1 sm:grid-cols-4 gap-3">
            <input
              type="text"
              required
              placeholder="Variant Name"
              value={newVariant.name}
              onChange={(e) => setNewVariant({ ...newVariant, name: e.target.value })}
              className="h-8 px-2.5 rounded-lg border border-outline-variant/60 text-xs"
            />
            <input
              type="text"
              required
              placeholder="SKU Code"
              value={newVariant.code}
              onChange={(e) => setNewVariant({ ...newVariant, code: e.target.value })}
              className="h-8 px-2.5 rounded-lg border border-outline-variant/60 text-xs font-mono"
            />
            <input
              type="number"
              required
              placeholder="Price (₹)"
              value={newVariant.price}
              onChange={(e) => setNewVariant({ ...newVariant, price: Number(e.target.value) })}
              className="h-8 px-2.5 rounded-lg border border-outline-variant/60 text-xs font-mono"
            />
            <select
              value={newVariant.billingPeriod}
              onChange={(e) => setNewVariant({ ...newVariant, billingPeriod: e.target.value })}
              className="h-8 px-2.5 rounded-lg border border-outline-variant/60 text-xs"
            >
              <option value="Monthly">Monthly</option>
              <option value="Quarterly">Quarterly</option>
              <option value="Annual">Annual</option>
            </select>
          </div>
          <div className="flex justify-end gap-2">
            <button
              type="button"
              onClick={() => setShowAdd(false)}
              className="px-3 py-1 text-xs border border-outline-variant/40 rounded-lg text-on-surface-variant"
            >
              Cancel
            </button>
            <button type="submit" className="px-3 py-1 bg-primary text-white text-xs font-semibold rounded-lg">
              Save Variant
            </button>
          </div>
        </form>
      )}

      <div className="bg-surface-container-lowest border border-outline-variant/40 rounded-xl shadow-elevation-1 overflow-hidden">
        <table className="w-full text-left text-xs">
          <thead className="bg-surface-container-low text-on-surface-variant uppercase text-[10px] font-semibold">
            <tr>
              <th className="p-3">Variant SKU</th>
              <th className="p-3">Display Name</th>
              <th className="p-3">Billing Cadence</th>
              <th className="p-3">Schedule Price</th>
              <th className="p-3">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-surface-container">
            {variants.map(v => (
              <tr key={v.id} className="hover:bg-surface-container-low/50">
                <td className="p-3 font-mono font-bold text-primary">{v.code}</td>
                <td className="p-3 font-bold text-on-surface">{v.name}</td>
                <td className="p-3 text-on-surface-variant">{v.billingPeriod}</td>
                <td className="p-3 font-mono font-bold text-on-surface">₹{v.price.toLocaleString('en-IN')}</td>
                <td className="p-3">
                  <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-50 text-emerald-700">
                    {v.status}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
