import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { ArrowLeft, CheckCircle2, ShieldAlert, Sparkles, Play, XCircle, RefreshCw, AlertTriangle, ShieldCheck } from 'lucide-react';
import { aiService } from '../../services/aiService';

export default function ActionPreview() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const insightId = searchParams.get('insightId') || '1';
  const actionType = searchParams.get('actionType') || 'SEND_RENEWAL_CAMPAIGN';

  const [preview, setPreview] = useState(null);
  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState('');
  const [statusMsg, setStatusMsg] = useState('');

  const loadPreview = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await aiService.previewAction(insightId, actionType);
      setPreview(data);
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to generate action preview');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPreview();
  }, [insightId, actionType]);

  const handleApprove = async () => {
    if (!preview?.actionId) return;
    try {
      setProcessing(true);
      setError('');
      const updated = await aiService.approveAction(preview.actionId, 'Admin approved via UI');
      setPreview(prev => ({ ...prev, status: updated.status || 'APPROVED' }));
      setStatusMsg('Action successfully approved! Ready for execution.');
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Approval failed');
    } finally {
      setProcessing(false);
    }
  };

  const handleReject = async () => {
    if (!preview?.actionId) return;
    if (!window.confirm('Are you sure you want to reject this action?')) return;
    try {
      setProcessing(true);
      setError('');
      await aiService.rejectAction(preview.actionId, 'Admin rejected via UI');
      setStatusMsg('Action rejected and archived.');
      setTimeout(() => navigate('/admin/ai-command-center'), 1500);
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Rejection failed');
    } finally {
      setProcessing(false);
    }
  };

  const handleExecute = async () => {
    if (!preview?.actionId) return;
    if (!window.confirm('Execute this automated business action now?')) return;
    try {
      setProcessing(true);
      setError('');
      await aiService.executeAction(preview.actionId, 'Executed by admin');
      setStatusMsg('Action successfully executed and committed to audit ledger.');
      setTimeout(() => navigate('/admin/ai-command-center/history'), 1500);
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Execution failed');
    } finally {
      setProcessing(false);
    }
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center p-12 text-center">
        <RefreshCw className="w-8 h-8 animate-spin text-primary mb-2" />
        <p className="text-xs text-on-surface-variant font-medium">Synthesizing zero-trust action preview...</p>
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate('/admin/ai-command-center')}
          className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container"
        >
          <ArrowLeft className="w-4 h-4" />
        </button>
        <div>
          <h1 className="text-xl font-bold text-on-surface">Action Preview & Executive Authorization</h1>
          <p className="text-xs text-on-surface-variant">Zero-Trust Guardrail: Bulk updates and communications require admin approval.</p>
        </div>
      </div>

      {error && (
        <div className="p-3 bg-error-container/20 border border-error/30 text-error rounded-xl text-xs flex justify-between">
          <span>{error}</span>
          <button onClick={() => setError('')} className="font-bold">×</button>
        </div>
      )}

      {statusMsg && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-xl text-xs flex items-center gap-2">
          <CheckCircle2 className="w-4 h-4 text-emerald-600 flex-shrink-0" />
          <span>{statusMsg}</span>
        </div>
      )}

      {preview && (
        <div className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-5">
          <div className="p-4 rounded-xl bg-primary/5 border border-primary/20 space-y-2">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Sparkles className="w-4 h-4 text-primary" />
                <span className="text-xs font-bold text-primary uppercase font-mono">{preview.actionType}</span>
              </div>
              <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-primary/10 text-primary">
                STATUS: {preview.status}
              </span>
            </div>
            <div className="text-sm font-bold text-on-surface">
              {preview.title || 'Automated Action Vector'}
            </div>
            <p className="text-xs text-on-surface-variant leading-relaxed">
              {preview.description}
            </p>
          </div>

          <div className="grid grid-cols-2 gap-3 text-xs">
            <div className="p-3 bg-surface-container/30 rounded-xl">
              <span className="text-[10px] uppercase font-mono text-outline">AFFECTED ACCOUNTS</span>
              <div className="text-base font-bold text-on-surface mt-0.5">{preview.affectedCustomers ?? 0} Customers</div>
              <div className="text-[10px] text-on-surface-variant mt-0.5">{preview.affectedSubscriptions ?? 0} Subscriptions</div>
            </div>
            <div className="p-3 bg-surface-container/30 rounded-xl">
              <span className="text-[10px] uppercase font-mono text-outline">FINANCIAL IMPACT</span>
              <div className="text-base font-bold font-mono text-emerald-700 mt-0.5">
                ₹{Number(preview.potentialRevenue || preview.potentialSavings || 0).toLocaleString('en-IN')}
              </div>
              <div className="text-[10px] text-on-surface-variant mt-0.5">Calculated Upside</div>
            </div>
          </div>

          {preview.warnings && preview.warnings.length > 0 && (
            <div className="p-3 rounded-xl bg-amber-50 border border-amber-200 text-xs space-y-1">
              <div className="flex items-center gap-1.5 font-bold text-amber-900">
                <AlertTriangle className="w-3.5 h-3.5 text-amber-700" />
                <span>Pre-Execution Warnings:</span>
              </div>
              <ul className="list-disc list-inside text-amber-800 text-[11px] space-y-0.5 pl-1">
                {preview.warnings.map((w, idx) => (
                  <li key={idx}>{w}</li>
                ))}
              </ul>
            </div>
          )}

          <div className="pt-4 border-t border-surface-container flex items-center justify-end gap-2">
            {preview.status !== 'REJECTED' && preview.status !== 'EXECUTED' && (
              <button
                onClick={handleReject}
                disabled={processing}
                className="px-4 py-2 border border-rose-200 text-rose-700 bg-rose-50 rounded-lg text-xs font-semibold hover:bg-rose-100 flex items-center gap-1"
              >
                <XCircle className="w-3.5 h-3.5" />
                <span>Reject</span>
              </button>
            )}

            {preview.status === 'PENDING_APPROVAL' && (
              <button
                onClick={handleApprove}
                disabled={processing}
                className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold rounded-lg flex items-center gap-1.5 shadow-sm"
              >
                <ShieldCheck className="w-3.5 h-3.5" />
                <span>{processing ? 'Approving...' : 'Approve Action'}</span>
              </button>
            )}

            {preview.status === 'APPROVED' && (
              <button
                onClick={handleExecute}
                disabled={processing}
                className="px-4 py-2 bg-primary hover:bg-primary-container text-white text-xs font-semibold rounded-lg flex items-center gap-1.5 shadow-sm"
              >
                <Play className="w-3.5 h-3.5 fill-current" />
                <span>{processing ? 'Executing...' : 'Execute Now'}</span>
              </button>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
