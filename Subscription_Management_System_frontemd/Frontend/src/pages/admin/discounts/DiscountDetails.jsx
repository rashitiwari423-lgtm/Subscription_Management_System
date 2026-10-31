import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Percent, Tag, Calendar, CheckCircle2, RefreshCw, AlertTriangle, Power } from 'lucide-react';
import { discountService } from '../../../services/discountService';

export default function DiscountDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [dsc, setDsc] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadDiscount = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await discountService.getById(id);
      setDsc(data);
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to load discount details');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (id) loadDiscount();
  }, [id]);

  const handleToggleStatus = async () => {
    try {
      const newStatus = dsc.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
      await discountService.updateStatus(dsc.id, newStatus);
      await loadDiscount();
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to update discount status');
    }
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center p-12 text-center">
        <RefreshCw className="w-8 h-8 animate-spin text-primary mb-2" />
        <p className="text-xs text-on-surface-variant font-medium">Loading discount rule...</p>
      </div>
    );
  }

  if (error && !dsc) {
    return (
      <div className="p-8 text-center space-y-4">
        <div className="inline-flex p-3 rounded-full bg-error-container text-error">
          <AlertTriangle className="w-6 h-6" />
        </div>
        <p className="text-sm font-medium text-error">{error}</p>
        <button
          onClick={() => navigate('/admin/discounts')}
          className="px-4 py-2 bg-surface-container text-xs font-semibold rounded-lg text-on-surface"
        >
          Back to Discounts
        </button>
      </div>
    );
  }

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
          <h1 className="text-xl font-bold text-on-surface">Discount Rule Audit</h1>
          <p className="text-xs text-on-surface-variant font-mono">DSC-{dsc.id} • {dsc.name}</p>
        </div>
      </div>

      {error && (
        <div className="p-3 bg-error-container/20 border border-error/30 text-error rounded-xl text-xs flex justify-between">
          <span>{error}</span>
          <button onClick={() => setError('')} className="font-bold">×</button>
        </div>
      )}

      <div className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-4">
        <div className="flex items-center justify-between border-b border-surface-container pb-4">
          <div>
            <span className="text-[10px] uppercase font-mono text-outline">DISCOUNT BENEFIT</span>
            <div className="text-2xl font-extrabold text-on-surface font-mono mt-0.5">
              {dsc.type === 'PERCENTAGE'
                ? `${dsc.value}% OFF`
                : `₹${Number(dsc.value).toLocaleString('en-IN')} DEDUCTION`}
            </div>
          </div>
          <div className="flex items-center gap-2">
            <span className={`px-2.5 py-1 rounded-full text-xs font-semibold ${
              dsc.status === 'ACTIVE' ? 'bg-emerald-50 text-emerald-700 border border-emerald-200' : 'bg-surface-container text-on-surface-variant'
            }`}>
              {dsc.status}
            </span>
            <button
              onClick={handleToggleStatus}
              className="px-3 py-1 bg-surface-container hover:bg-surface-container-high rounded text-xs font-semibold text-on-surface"
            >
              {dsc.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
            </button>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4 text-xs">
          <div>
            <span className="text-outline uppercase text-[10px]">Campaign Name</span>
            <div className="font-bold text-on-surface mt-0.5">{dsc.name}</div>
          </div>
          <div>
            <span className="text-outline uppercase text-[10px]">Rule Calculation Type</span>
            <div className="font-mono font-bold text-primary mt-0.5">{dsc.type}</div>
          </div>
          <div>
            <span className="text-outline uppercase text-[10px]">Discount ID</span>
            <div className="font-mono text-on-surface-variant mt-0.5">DSC-{dsc.id}</div>
          </div>
          <div>
            <span className="text-outline uppercase text-[10px]">Created At</span>
            <div className="font-mono text-on-surface-variant mt-0.5">
              {dsc.createdAt ? new Date(dsc.createdAt).toLocaleString() : 'N/A'}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
