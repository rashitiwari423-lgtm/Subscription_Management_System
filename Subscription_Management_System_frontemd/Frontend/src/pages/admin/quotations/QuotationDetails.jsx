import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Download, Send, CheckCircle2, FileSpreadsheet, RefreshCw, AlertTriangle, ArrowRight, XCircle } from 'lucide-react';
import { quotationService } from '../../../services/quotationService';

export default function QuotationDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [quotation, setQuotation] = useState(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState('');
  const [convertedSub, setConvertedSub] = useState(null);

  const loadQuotation = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await quotationService.getById(id);
      setQuotation(data);
    } catch (err) {
      setError(err.userMessage || 'Quotation not found');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadQuotation();
  }, [id]);

  const handleStatusTransition = async (newStatus) => {
    setActionLoading(true);
    setSuccessMsg('');
    setError(null);
    try {
      const updated = await quotationService.updateStatus(id, newStatus);
      setQuotation(updated);
      setSuccessMsg(`Quotation transitioned to ${newStatus} successfully.`);
    } catch (err) {
      setError(err.userMessage || `Failed to transition quotation to ${newStatus}.`);
    } finally {
      setActionLoading(false);
    }
  };

  const handleConvert = async () => {
    if (!window.confirm('Convert this accepted quotation into an active recurring subscription?')) {
      return;
    }
    setActionLoading(true);
    setSuccessMsg('');
    setError(null);
    try {
      const sub = await quotationService.convert(id);
      setConvertedSub(sub);
      setSuccessMsg(`Quotation converted to subscription successfully! Created Subscription ${sub.subscriptionNumber}.`);
      loadQuotation();
    } catch (err) {
      setError(err.userMessage || 'Failed to convert quotation to subscription.');
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center p-12 text-xs text-on-surface-variant gap-2">
        <RefreshCw className="w-4 h-4 animate-spin text-primary" />
        <span>Loading proposal document...</span>
      </div>
    );
  }

  if (error && !quotation) {
    return (
      <div className="p-8 text-center text-xs text-error space-y-3">
        <AlertTriangle className="w-8 h-8 mx-auto" />
        <p>{error}</p>
        <button
          onClick={() => navigate('/admin/quotations')}
          className="px-3 py-1.5 bg-primary text-white rounded text-xs font-semibold"
        >
          Back to Quotations
        </button>
      </div>
    );
  }

  const formatCurr = (val) => `₹${Number(val || 0).toLocaleString('en-IN', { maximumFractionDigits: 2 })}`;

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate('/admin/quotations')}
            className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container"
          >
            <ArrowLeft className="w-4 h-4" />
          </button>
          <div>
            <h1 className="text-xl font-bold text-on-surface">Contract Proposal Document</h1>
            <p className="text-xs text-on-surface-variant font-mono">
              Ref: {quotation.quotationNumber} • Customer #{quotation.customerId}
            </p>
          </div>
        </div>

        {/* Lifecycle Actions */}
        <div className="flex items-center gap-2">
          {quotation.status === 'DRAFT' && (
            <button
              onClick={() => handleStatusTransition('SENT')}
              disabled={actionLoading}
              className="px-3.5 py-1.5 rounded-lg bg-primary hover:bg-primary-container text-white text-xs font-semibold flex items-center gap-1.5 shadow-sm disabled:opacity-50"
            >
              <Send className="w-3.5 h-3.5" />
              <span>Send to Client</span>
            </button>
          )}

          {quotation.status === 'SENT' && (
            <>
              <button
                onClick={() => handleStatusTransition('ACCEPTED')}
                disabled={actionLoading}
                className="px-3.5 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold flex items-center gap-1.5 shadow-sm disabled:opacity-50"
              >
                <CheckCircle2 className="w-3.5 h-3.5" />
                <span>Mark Accepted</span>
              </button>
              <button
                onClick={() => handleStatusTransition('REJECTED')}
                disabled={actionLoading}
                className="px-3.5 py-1.5 rounded-lg border border-rose-300 text-rose-700 hover:bg-rose-50 text-xs font-semibold flex items-center gap-1.5 disabled:opacity-50"
              >
                <XCircle className="w-3.5 h-3.5" />
                <span>Mark Rejected</span>
              </button>
            </>
          )}

          {quotation.status === 'ACCEPTED' && (
            <button
              onClick={handleConvert}
              disabled={actionLoading}
              className="px-4 py-2 rounded-lg bg-emerald-700 hover:bg-emerald-800 text-white text-xs font-bold flex items-center gap-2 shadow-md disabled:opacity-50"
            >
              <CheckCircle2 className="w-4 h-4" />
              <span>Convert to Subscription</span>
            </button>
          )}

          {quotation.status === 'CONVERTED' && (
            <span className="px-3 py-1 rounded-lg bg-primary/10 text-primary font-bold text-xs flex items-center gap-1.5">
              <CheckCircle2 className="w-4 h-4" />
              <span>Converted to Subscription</span>
            </span>
          )}
        </div>
      </div>

      {successMsg && (
        <div className="p-4 rounded-xl bg-emerald-50 border border-emerald-200 text-xs text-emerald-800 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4 text-emerald-600 flex-shrink-0" />
            <span>{successMsg}</span>
          </div>
          {convertedSub && (
            <button
              onClick={() => navigate(`/admin/subscriptions/${convertedSub.id}`)}
              className="font-bold underline text-emerald-900"
            >
              View Subscription &rarr;
            </button>
          )}
        </div>
      )}

      {error && (
        <div className="p-3 bg-error-container/30 border border-error/30 rounded-xl text-xs text-error flex items-center gap-2">
          <AlertTriangle className="w-4 h-4 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      <div className="bg-surface-container-lowest p-8 rounded-2xl border border-outline-variant/40 shadow-elevation-2 space-y-6">
        <div className="flex items-start justify-between border-b border-surface-container pb-6">
          <div>
            <div className="text-lg font-bold text-on-surface">SubscriptIQ Financial Systems Pvt Ltd</div>
            <div className="text-xs text-on-surface-variant mt-1">BKC Financial District, Mumbai 400051</div>
            <div className="text-xs font-mono text-outline mt-0.5">GSTIN: 27AABCS9912E1ZQ</div>
          </div>
          <div className="text-right">
            <div className="text-xs font-mono font-bold text-primary">{quotation.quotationNumber}</div>
            <div className="text-xs text-on-surface-variant mt-0.5">Valid Until: {quotation.validUntil}</div>
            <span className={`inline-block mt-2 px-2.5 py-0.5 rounded-full text-[10px] font-semibold ${
              quotation.status === 'CONVERTED' ? 'bg-primary/10 text-primary' :
              quotation.status === 'ACCEPTED' ? 'bg-emerald-50 text-emerald-700' :
              quotation.status === 'SENT' ? 'bg-amber-50 text-amber-700' :
              'bg-surface-container text-on-surface-variant'
            }`}>
              {quotation.status}
            </span>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4 text-xs">
          <div>
            <div className="text-[10px] uppercase font-bold text-outline">Billed To Customer:</div>
            <div className="font-bold text-on-surface mt-1">{quotation.customerCompanyName || `Customer #${quotation.customerId}`}</div>
            <div className="text-on-surface-variant font-mono">Date: {quotation.quotationDate}</div>
          </div>
          <div className="text-right">
            <div className="text-[10px] uppercase font-bold text-outline">Proposal Terms &amp; Notes:</div>
            <div className="text-on-surface mt-1">{quotation.notes || 'Standard terms apply.'}</div>
          </div>
        </div>

        <div className="border border-outline-variant/40 rounded-xl overflow-hidden">
          <table className="w-full text-left text-xs">
            <thead className="bg-surface-container-low text-on-surface-variant font-semibold">
              <tr>
                <th className="p-3">Plan / Item</th>
                <th className="p-3 text-center">Qty</th>
                <th className="p-3 text-right">Unit Price</th>
                <th className="p-3 text-right">Discount</th>
                <th className="p-3 text-right">Tax</th>
                <th className="p-3 text-right">Total</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-container">
              {(quotation.items || []).map((it, idx) => (
                <tr key={idx}>
                  <td className="p-3">
                    <div className="font-semibold text-on-surface">{it.planName || `Plan #${it.planId}`}</div>
                  </td>
                  <td className="p-3 text-center font-mono">{it.quantity}</td>
                  <td className="p-3 text-right font-mono">{formatCurr(it.unitPrice)}</td>
                  <td className="p-3 text-right font-mono text-emerald-700">-{formatCurr(it.discountAmount)}</td>
                  <td className="p-3 text-right font-mono text-on-surface-variant">+{formatCurr(it.taxAmount)}</td>
                  <td className="p-3 text-right font-mono font-bold text-on-surface">{formatCurr(it.totalAmount)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="pt-4 border-t border-surface-container flex justify-between items-center">
          <div className="text-xs text-on-surface-variant">
            Authorized Signatory • SubscriptIQ Automated Revenue Vault
          </div>
          <div className="text-right space-y-1">
            <div className="text-xs text-outline uppercase font-mono">
              Subtotal: {formatCurr(quotation.subtotal)}
            </div>
            <div className="text-xs text-emerald-700 font-mono">
              Discount: -{formatCurr(quotation.discountAmount)}
            </div>
            <div className="text-xs text-on-surface-variant font-mono">
              Tax: +{formatCurr(quotation.taxAmount)}
            </div>
            <div className="text-xl font-extrabold text-primary font-mono pt-1">
              Net Total: {formatCurr(quotation.totalAmount)}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
