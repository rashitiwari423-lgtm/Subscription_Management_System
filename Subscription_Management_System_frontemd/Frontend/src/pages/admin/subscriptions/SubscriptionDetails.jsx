import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { 
  ArrowLeft, RefreshCw, AlertTriangle, Play, Pause, XCircle, 
  Receipt, Calendar, ShieldCheck, CheckCircle2, ChevronRight 
} from 'lucide-react';
import { subscriptionService } from '../../../services/subscriptionService';

export default function SubscriptionDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [sub, setSub] = useState(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const loadSubscription = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await subscriptionService.getById(id);
      setSub(data);
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to load subscription details');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (id) {
      loadSubscription();
    }
  }, [id]);

  const handleAction = async (actionFn, successText) => {
    try {
      setActionLoading(true);
      setError('');
      await actionFn();
      setSuccessMsg(successText);
      await loadSubscription();
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Action failed');
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center p-12 text-center">
        <RefreshCw className="w-8 h-8 animate-spin text-primary mb-2" />
        <p className="text-xs text-on-surface-variant font-medium">Loading subscription details...</p>
      </div>
    );
  }

  if (error && !sub) {
    return (
      <div className="p-8 text-center space-y-4">
        <div className="inline-flex p-3 rounded-full bg-error-container text-error">
          <AlertTriangle className="w-6 h-6" />
        </div>
        <p className="text-sm font-medium text-error">{error}</p>
        <button
          onClick={() => navigate('/admin/subscriptions')}
          className="px-4 py-2 bg-surface-container text-xs font-semibold rounded-lg text-on-surface"
        >
          Back to Subscriptions
        </button>
      </div>
    );
  }

  const getStatusBadge = (status) => {
    switch (status) {
      case 'ACTIVE':
        return 'bg-emerald-50 text-emerald-700 border-emerald-200';
      case 'PAUSED':
        return 'bg-amber-50 text-amber-700 border-amber-200';
      case 'CLOSED':
      case 'EXPIRED':
        return 'bg-rose-50 text-rose-700 border-rose-200';
      default:
        return 'bg-surface-container text-on-surface-variant border-outline-variant/30';
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate('/admin/subscriptions')}
            className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container"
          >
            <ArrowLeft className="w-4 h-4" />
          </button>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-xl font-bold text-on-surface">{sub.customerCompanyName || `Customer #${sub.customerId}`}</h1>
              <span className={`px-2.5 py-0.5 rounded-full text-[10px] font-semibold border ${getStatusBadge(sub.status)}`}>
                {sub.status}
              </span>
            </div>
            <p className="text-xs text-on-surface-variant font-mono">
              {sub.subscriptionNumber || `SUB-${sub.id}`} • {sub.planName}
            </p>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex flex-wrap items-center gap-2">
          {sub.status === 'ACTIVE' && (
            <button
              onClick={() => handleAction(() => subscriptionService.pause(sub.id), 'Subscription paused successfully')}
              disabled={actionLoading}
              className="px-3 py-1.5 rounded-lg border border-amber-300 bg-amber-50 text-amber-800 text-xs font-semibold hover:bg-amber-100 flex items-center gap-1.5"
            >
              <Pause className="w-3.5 h-3.5" />
              <span>Pause</span>
            </button>
          )}

          {sub.status === 'PAUSED' && (
            <button
              onClick={() => handleAction(() => subscriptionService.resume(sub.id), 'Subscription resumed successfully')}
              disabled={actionLoading}
              className="px-3 py-1.5 rounded-lg border border-emerald-300 bg-emerald-50 text-emerald-800 text-xs font-semibold hover:bg-emerald-100 flex items-center gap-1.5"
            >
              <Play className="w-3.5 h-3.5" />
              <span>Resume</span>
            </button>
          )}

          {sub.status === 'ACTIVE' && (
            <button
              onClick={() => handleAction(() => subscriptionService.renew(sub.id), 'Subscription renewed successfully')}
              disabled={actionLoading}
              className="px-3 py-1.5 rounded-lg border border-outline-variant/40 bg-surface-container-lowest text-xs font-semibold text-on-surface hover:bg-surface-container flex items-center gap-1.5"
            >
              <RefreshCw className={`w-3.5 h-3.5 ${actionLoading ? 'animate-spin' : ''}`} />
              <span>Renew Cycle</span>
            </button>
          )}

          {sub.status === 'ACTIVE' && (
            <button
              onClick={() => handleAction(() => subscriptionService.createInvoice(sub.id), 'Invoice generated successfully')}
              disabled={actionLoading}
              className="px-3 py-1.5 rounded-lg bg-secondary hover:bg-secondary/90 text-white text-xs font-semibold flex items-center gap-1.5 shadow-sm"
            >
              <Receipt className="w-3.5 h-3.5" />
              <span>Generate Invoice</span>
            </button>
          )}

          {sub.status !== 'CLOSED' && sub.status !== 'EXPIRED' && (
            <button
              onClick={() => {
                if (window.confirm('Are you sure you want to close this subscription? This action cannot be undone.')) {
                  handleAction(() => subscriptionService.close(sub.id), 'Subscription closed successfully');
                }
              }}
              disabled={actionLoading}
              className="px-3 py-1.5 rounded-lg border border-rose-200 text-rose-700 bg-rose-50 text-xs font-semibold hover:bg-rose-100 flex items-center gap-1.5"
            >
              <XCircle className="w-3.5 h-3.5" />
              <span>Close</span>
            </button>
          )}
        </div>
      </div>

      {successMsg && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 text-emerald-700 rounded-xl text-xs flex items-center justify-between">
          <span>{successMsg}</span>
          <button onClick={() => setSuccessMsg('')} className="text-emerald-900 font-bold">×</button>
        </div>
      )}

      {error && (
        <div className="p-3 bg-error-container/20 border border-error/30 text-error rounded-xl text-xs flex items-center justify-between">
          <span>{error}</span>
          <button onClick={() => setError('')} className="text-error font-bold">×</button>
        </div>
      )}

      {/* Metrics Row */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-outline">RECURRING AMOUNT</div>
          <div className="text-xl font-bold text-on-surface mt-1">₹{Number(sub.totalAmount || 0).toLocaleString('en-IN')}</div>
          <div className="text-[10px] text-on-surface-variant mt-0.5">{sub.billingCycle} billing cycle</div>
        </div>
        <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-outline">UNIT RATE</div>
          <div className="text-xl font-bold text-primary mt-1">₹{Number(sub.unitPrice || 0).toLocaleString('en-IN')}</div>
          <div className="text-[10px] text-on-surface-variant mt-0.5">Discount: ₹{Number(sub.discountAmount || 0).toLocaleString('en-IN')}</div>
        </div>
        <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-outline">SEATS / QUANTITY</div>
          <div className="text-xl font-bold text-on-surface mt-1">{sub.quantity || 1} Unit(s)</div>
          <div className="text-[10px] text-emerald-600 mt-0.5">Tax: ₹{Number(sub.taxAmount || 0).toLocaleString('en-IN')}</div>
        </div>
        <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-outline">NEXT BILLING</div>
          <div className="text-xl font-bold text-on-surface mt-1 font-mono text-sm">{sub.nextBillingDate || 'N/A'}</div>
          <div className="text-[10px] text-on-surface-variant mt-0.5">Started: {sub.startDate || 'N/A'}</div>
        </div>
      </div>

      {/* Information Details Card */}
      <div className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-4">
        <h2 className="text-sm font-bold text-on-surface">Subscription Parameters</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs">
          <div className="p-3 bg-surface-container/30 rounded-lg space-y-2">
            <div className="flex justify-between">
              <span className="text-on-surface-variant">Subscription Number:</span>
              <span className="font-mono font-medium text-on-surface">{sub.subscriptionNumber || `SUB-${sub.id}`}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-on-surface-variant">Customer ID:</span>
              <span className="font-mono text-on-surface">{sub.customerId}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-on-surface-variant">Plan ID:</span>
              <span className="font-mono text-on-surface">{sub.planId}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-on-surface-variant">Originating Quotation:</span>
              <span className="font-mono text-on-surface">{sub.quotationId ? `Q-${sub.quotationId}` : 'Direct'}</span>
            </div>
          </div>
          <div className="p-3 bg-surface-container/30 rounded-lg space-y-2">
            <div className="flex justify-between">
              <span className="text-on-surface-variant">Start Date:</span>
              <span className="font-mono text-on-surface">{sub.startDate || 'Immediate'}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-on-surface-variant">End Date:</span>
              <span className="font-mono text-on-surface">{sub.endDate || 'Ongoing / No Expiry'}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-on-surface-variant">Next Billing Date:</span>
              <span className="font-mono text-on-surface">{sub.nextBillingDate || 'N/A'}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-on-surface-variant">Created Timestamp:</span>
              <span className="font-mono text-on-surface">{sub.createdAt ? new Date(sub.createdAt).toLocaleString() : 'N/A'}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
