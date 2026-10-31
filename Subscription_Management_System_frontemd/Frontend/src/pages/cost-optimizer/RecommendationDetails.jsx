import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, TrendingDown, CheckCircle2, ArrowUpRight, RefreshCw, AlertTriangle, XCircle, ShieldCheck } from 'lucide-react';
import { optimizationService } from '../../services/optimizationService';

export default function RecommendationDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [rec, setRec] = useState(null);
  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const loadRecommendation = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await optimizationService.getInsightById(id);
      setRec(data);
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to load recommendation');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (id) loadRecommendation();
  }, [id]);

  const handleApprove = async () => {
    try {
      setProcessing(true);
      setError('');
      const updated = await optimizationService.approve(rec.id);
      setRec(updated);
      setSuccessMsg('Cost recommendation approved successfully!');
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Approval failed');
    } finally {
      setProcessing(false);
    }
  };

  const handleReject = async () => {
    if (!window.confirm('Reject this cost recommendation?')) return;
    try {
      setProcessing(true);
      setError('');
      const updated = await optimizationService.reject(rec.id, 'Admin rejected recommendation');
      setRec(updated);
      setSuccessMsg('Cost recommendation rejected.');
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Rejection failed');
    } finally {
      setProcessing(false);
    }
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center p-12 text-center">
        <RefreshCw className="w-8 h-8 animate-spin text-primary mb-2" />
        <p className="text-xs text-on-surface-variant font-medium">Loading optimization telemetry...</p>
      </div>
    );
  }

  if (error && !rec) {
    return (
      <div className="p-8 text-center space-y-4">
        <div className="inline-flex p-3 rounded-full bg-error-container text-error">
          <AlertTriangle className="w-6 h-6" />
        </div>
        <p className="text-sm font-medium text-error">{error}</p>
        <button
          onClick={() => navigate('/admin/cost-optimizer')}
          className="px-4 py-2 bg-surface-container text-xs font-semibold rounded-lg text-on-surface"
        >
          Back to Optimizer
        </button>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate('/admin/cost-optimizer')}
          className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container"
        >
          <ArrowLeft className="w-4 h-4" />
        </button>
        <div>
          <h1 className="text-xl font-bold text-on-surface">Optimization Rationale & Contract Delta</h1>
          <p className="text-xs text-on-surface-variant font-mono">OPT-{rec.id} • {rec.recommendation}</p>
        </div>
      </div>

      {error && (
        <div className="p-3 bg-error-container/20 border border-error/30 text-error rounded-xl text-xs flex justify-between">
          <span>{error}</span>
          <button onClick={() => setError('')} className="font-bold">×</button>
        </div>
      )}

      {successMsg && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-xl text-xs flex items-center justify-between">
          <span>{successMsg}</span>
          <button onClick={() => setSuccessMsg('')} className="font-bold">×</button>
        </div>
      )}

      <div className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-5">
        <div className="flex items-center justify-between border-b border-surface-container pb-4">
          <span className="text-xs font-mono font-bold uppercase bg-primary/10 text-primary px-3 py-1 rounded-full">
            {rec.recommendation}
          </span>
          <span className="text-xs font-bold font-mono text-emerald-700 bg-emerald-50 px-2.5 py-1 rounded-full border border-emerald-200">
            Save ₹{Number(rec.potentialMonthlySavings || 0).toLocaleString('en-IN')}/mo
          </span>
        </div>

        <div>
          <h2 className="text-lg font-bold text-on-surface">{rec.resourceName}</h2>
          <p className="text-xs text-on-surface-variant leading-relaxed mt-1">
            {rec.proposedAction || `Downscale unused capacity on ${rec.customerCompanyName || 'subscription'}.`}
          </p>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs">
          <div className="p-3 bg-surface-container/30 rounded-xl">
            <div className="text-[10px] uppercase font-mono text-outline">Allocated</div>
            <div className="text-base font-bold text-on-surface mt-1">{rec.allocatedQuantity} Units</div>
          </div>
          <div className="p-3 bg-surface-container/30 rounded-xl">
            <div className="text-[10px] uppercase font-mono text-outline">Used</div>
            <div className="text-base font-bold text-primary mt-1">{rec.usedQuantity} Units</div>
          </div>
          <div className="p-3 bg-surface-container/30 rounded-xl">
            <div className="text-[10px] uppercase font-mono text-outline">Unused</div>
            <div className="text-base font-bold text-rose-700 mt-1">{rec.unusedQuantity} Units</div>
          </div>
          <div className="p-3 bg-surface-container/30 rounded-xl">
            <div className="text-[10px] uppercase font-mono text-outline">Utilization</div>
            <div className="text-base font-bold text-on-surface mt-1">{rec.utilizationPercentage}%</div>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4 p-4 rounded-xl bg-surface-container-low text-xs">
          <div>
            <div className="text-outline uppercase text-[10px]">Current Invoiced Spend</div>
            <div className="font-bold text-rose-700 font-mono text-base mt-0.5">
              ₹{Number(rec.currentMonthlyCost || 0).toLocaleString('en-IN')}/mo
            </div>
          </div>
          <div>
            <div className="text-outline uppercase text-[10px]">Potential Annualized Savings</div>
            <div className="font-bold text-emerald-700 font-mono text-base mt-0.5">
              ₹{Number(rec.potentialAnnualSavings || (rec.potentialMonthlySavings * 12) || 0).toLocaleString('en-IN')}
            </div>
          </div>
        </div>

        <div className="pt-4 border-t border-surface-container flex items-center justify-between">
          <span className="text-xs font-mono text-on-surface-variant">
            Status: <strong>{rec.status}</strong>
          </span>
          <div className="flex gap-2">
            {rec.status !== 'REJECTED' && (
              <button
                onClick={handleReject}
                disabled={processing}
                className="px-4 py-2 border border-rose-200 text-rose-700 bg-rose-50 rounded-lg text-xs font-semibold hover:bg-rose-100 flex items-center gap-1"
              >
                <XCircle className="w-3.5 h-3.5" />
                <span>Reject</span>
              </button>
            )}
            {rec.status !== 'APPROVED' && (
              <button
                onClick={handleApprove}
                disabled={processing}
                className="px-4 py-2 bg-primary hover:bg-primary-container text-white text-xs font-semibold rounded-lg flex items-center gap-1.5 shadow-sm"
              >
                <ShieldCheck className="w-3.5 h-3.5" />
                <span>{processing ? 'Approving...' : 'Approve Optimization'}</span>
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
