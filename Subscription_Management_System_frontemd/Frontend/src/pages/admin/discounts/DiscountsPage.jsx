import React, { useState, useEffect } from 'react';
import { Percent, Plus, Tag, CheckCircle2, RefreshCw, AlertTriangle, Power } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { discountService } from '../../../services/discountService';

export default function DiscountsPage() {
  const [discountList, setDiscountList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const loadDiscounts = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await discountService.getAll();
      const list = Array.isArray(data) ? data : (data?.content || []);
      setDiscountList(list);
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to load discounts');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDiscounts();
  }, []);

  const handleToggleStatus = async (e, dsc) => {
    e.stopPropagation();
    try {
      const newStatus = dsc.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
      await discountService.updateStatus(dsc.id, newStatus);
      await loadDiscounts();
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to update discount status');
    }
  };

  return (
    <div className="space-y-5">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Promotions & Discount Governance</h1>
          <p className="text-xs text-on-surface-variant">
            Manage promotional rates, percentage rebates, and contractual flat discounts.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={loadDiscounts}
            className="p-2 rounded-lg border border-outline-variant/40 hover:bg-surface-container text-on-surface-variant"
            title="Refresh Discounts"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          </button>
          <button
            onClick={() => navigate('/admin/discounts/new')}
            className="px-3.5 py-2 rounded-lg bg-primary hover:bg-primary-container text-white text-xs font-semibold flex items-center gap-1.5 shadow-sm"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>Create Discount</span>
          </button>
        </div>
      </div>

      {error && (
        <div className="p-3 bg-error-container/20 border border-error/30 text-error rounded-xl text-xs flex justify-between">
          <span>{error}</span>
          <button onClick={() => setError('')} className="font-bold">×</button>
        </div>
      )}

      {loading ? (
        <div className="p-12 text-center text-on-surface-variant">
          <RefreshCw className="w-6 h-6 animate-spin mx-auto mb-2 text-primary" />
          <span className="text-xs">Loading promotional rules...</span>
        </div>
      ) : discountList.length === 0 ? (
        <div className="p-8 text-center bg-surface-container-lowest rounded-xl border border-outline-variant/40 text-on-surface-variant text-xs">
          No discounts configured yet. Click "Create Discount" to add one.
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {discountList.map((dsc) => (
            <div
              key={dsc.id}
              onClick={() => navigate(`/admin/discounts/${dsc.id}`)}
              className="bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-3 cursor-pointer hover:border-primary/40 transition-colors"
            >
              <div className="flex items-center justify-between">
                <span className="font-mono font-bold text-xs bg-primary/10 text-primary px-2.5 py-1 rounded-lg">
                  DSC-{dsc.id}
                </span>
                <div className="flex items-center gap-1.5">
                  <span className={`text-[10px] font-semibold px-2 py-0.5 rounded-full ${
                    dsc.status === 'ACTIVE' ? 'bg-emerald-50 text-emerald-700' : 'bg-surface-container text-on-surface-variant'
                  }`}>
                    {dsc.status}
                  </span>
                  <button
                    onClick={(e) => handleToggleStatus(e, dsc)}
                    className={`p-1 rounded hover:bg-surface-container ${
                      dsc.status === 'ACTIVE' ? 'text-amber-600' : 'text-emerald-600'
                    }`}
                    title={dsc.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
                  >
                    <Power className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>

              <div>
                <h3 className="font-bold text-sm text-on-surface">{dsc.name}</h3>
                <div className="text-xl font-extrabold text-on-surface font-mono mt-1">
                  {dsc.type === 'PERCENTAGE'
                    ? `${dsc.value}% OFF`
                    : `₹${Number(dsc.value).toLocaleString('en-IN')} FLAT`}
                </div>
              </div>

              <div className="pt-3 border-t border-surface-container flex items-center justify-between text-xs text-on-surface-variant">
                <span>Type: {dsc.type}</span>
                <span className="text-primary font-semibold hover:underline">View Details →</span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
