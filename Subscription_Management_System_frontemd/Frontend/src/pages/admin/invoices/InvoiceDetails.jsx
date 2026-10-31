import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Download, CheckCircle2, Receipt, AlertTriangle, RefreshCw, XCircle, CreditCard } from 'lucide-react';
import { invoiceService } from '../../../services/invoiceService';

export default function InvoiceDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [invoice, setInvoice] = useState(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const loadInvoice = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await invoiceService.getById(id);
      setInvoice(data);
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to load invoice details');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (id) {
      loadInvoice();
    }
  }, [id]);

  const handleCancelInvoice = async () => {
    if (!window.confirm('Are you sure you want to cancel/void this invoice?')) return;
    try {
      setActionLoading(true);
      setError('');
      await invoiceService.voidInvoice(invoice.id);
      setSuccessMsg('Invoice cancelled successfully');
      await loadInvoice();
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to cancel invoice');
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center p-12 text-center">
        <RefreshCw className="w-8 h-8 animate-spin text-primary mb-2" />
        <p className="text-xs text-on-surface-variant font-medium">Loading tax invoice...</p>
      </div>
    );
  }

  if (error && !invoice) {
    return (
      <div className="p-8 text-center space-y-4">
        <div className="inline-flex p-3 rounded-full bg-error-container text-error">
          <AlertTriangle className="w-6 h-6" />
        </div>
        <p className="text-sm font-medium text-error">{error}</p>
        <button
          onClick={() => navigate('/admin/invoices')}
          className="px-4 py-2 bg-surface-container text-xs font-semibold rounded-lg text-on-surface"
        >
          Back to Invoices
        </button>
      </div>
    );
  }

  const getStatusBadge = (status) => {
    switch (status) {
      case 'PAID':
        return 'bg-emerald-50 text-emerald-700 border-emerald-200';
      case 'OVERDUE':
        return 'bg-rose-50 text-rose-700 border-rose-200';
      case 'PENDING':
        return 'bg-amber-50 text-amber-700 border-amber-200';
      case 'CANCELLED':
        return 'bg-zinc-100 text-zinc-600 border-zinc-200';
      default:
        return 'bg-surface-container text-on-surface-variant';
    }
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate('/admin/invoices')}
            className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container"
          >
            <ArrowLeft className="w-4 h-4" />
          </button>
          <div>
            <h1 className="text-xl font-bold text-on-surface">Tax Invoice & Statutory Filing</h1>
            <p className="text-xs text-on-surface-variant font-mono">
              {invoice.invoiceNumber || `INV-${invoice.id}`} • {invoice.customerName || `Customer #${invoice.customerId}`}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {invoice.status !== 'PAID' && invoice.status !== 'CANCELLED' && (
            <button
              onClick={() => navigate(`/admin/payments/new?invoiceId=${invoice.id}&amount=${invoice.balanceAmount || invoice.totalAmount}`)}
              className="px-3 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold flex items-center gap-1.5 shadow-sm"
            >
              <CreditCard className="w-3.5 h-3.5" />
              <span>Record Payment</span>
            </button>
          )}

          {invoice.status !== 'CANCELLED' && invoice.status !== 'PAID' && (
            <button
              onClick={handleCancelInvoice}
              disabled={actionLoading}
              className="px-3 py-1.5 rounded-lg border border-rose-200 text-rose-700 bg-rose-50 text-xs font-semibold hover:bg-rose-100 flex items-center gap-1.5"
            >
              <XCircle className="w-3.5 h-3.5" />
              <span>Cancel / Void</span>
            </button>
          )}

          <button
            onClick={() => window.print()}
            className="px-3.5 py-1.5 rounded-lg border border-outline-variant/40 bg-surface-container-lowest text-xs font-semibold flex items-center gap-1.5 text-on-surface hover:bg-surface-container"
          >
            <Download className="w-3.5 h-3.5" />
            <span>Print / Export</span>
          </button>
        </div>
      </div>

      {successMsg && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 text-emerald-700 rounded-xl text-xs flex justify-between">
          <span>{successMsg}</span>
          <button onClick={() => setSuccessMsg('')} className="font-bold">×</button>
        </div>
      )}

      {error && (
        <div className="p-3 bg-error-container/20 border border-error/30 text-error rounded-xl text-xs flex justify-between">
          <span>{error}</span>
          <button onClick={() => setError('')} className="font-bold">×</button>
        </div>
      )}

      {/* Invoice Card */}
      <div className="bg-surface-container-lowest p-8 rounded-2xl border border-outline-variant/40 shadow-elevation-2 space-y-6">
        <div className="flex items-start justify-between border-b border-surface-container pb-6">
          <div>
            <div className="text-lg font-bold text-on-surface">SubscriptIQ Financial Systems Pvt Ltd</div>
            <div className="text-xs text-on-surface-variant mt-1">BKC Financial District, Mumbai 400051</div>
            <div className="text-xs font-mono text-outline mt-0.5">GSTIN: 27AABCS9912E1ZQ</div>
          </div>
          <div className="text-right">
            <div className="text-sm font-mono font-bold text-primary">{invoice.invoiceNumber || `INV-${invoice.id}`}</div>
            <div className="text-xs text-on-surface-variant mt-0.5">Issued: {invoice.invoiceDate || 'N/A'}</div>
            <div className="text-xs text-on-surface-variant">Due: {invoice.dueDate || 'N/A'}</div>
            <span className={`inline-block mt-2 px-2.5 py-0.5 rounded-full text-[10px] font-semibold border ${getStatusBadge(invoice.status)}`}>
              {invoice.status}
            </span>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4 text-xs">
          <div>
            <div className="text-[10px] uppercase font-bold text-outline">Billed To (Client Entity):</div>
            <div className="font-bold text-on-surface mt-1">{invoice.customerName || `Customer #${invoice.customerId}`}</div>
            <div className="text-on-surface-variant font-mono text-[11px]">ID: {invoice.customerId}</div>
            {invoice.subscriptionNumber && (
              <div className="text-on-surface-variant font-mono text-[11px] mt-0.5">
                Subscription: {invoice.subscriptionNumber}
              </div>
            )}
          </div>
          <div className="text-right">
            <div className="text-[10px] uppercase font-bold text-outline">Billing Period:</div>
            <div className="font-semibold text-on-surface mt-1 font-mono">
              {invoice.billingPeriodStart || 'N/A'} — {invoice.billingPeriodEnd || 'N/A'}
            </div>
            <div className="text-on-surface-variant text-[11px] mt-0.5">
              SAC: 998313 (IT SaaS & Cloud Architecture)
            </div>
          </div>
        </div>

        {/* Itemized Table */}
        <div className="border border-outline-variant/40 rounded-xl overflow-hidden">
          <table className="w-full text-left text-xs">
            <thead className="bg-surface-container-low text-on-surface-variant font-semibold">
              <tr>
                <th className="p-3">Description</th>
                <th className="p-3 text-center">Qty</th>
                <th className="p-3 text-right">Unit Price</th>
                <th className="p-3 text-right">Tax (GST)</th>
                <th className="p-3 text-right">Line Total</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-container">
              {invoice.items && invoice.items.length > 0 ? (
                invoice.items.map((item, idx) => (
                  <tr key={item.id || idx}>
                    <td className="p-3">
                      <div className="font-semibold text-on-surface">{item.description}</div>
                    </td>
                    <td className="p-3 text-center font-mono">{item.quantity}</td>
                    <td className="p-3 text-right font-mono">₹{Number(item.unitPrice || 0).toLocaleString('en-IN')}</td>
                    <td className="p-3 text-right font-mono">₹{Number(item.taxAmount || 0).toLocaleString('en-IN')}</td>
                    <td className="p-3 text-right font-mono font-bold text-on-surface">
                      ₹{Number(item.totalAmount || 0).toLocaleString('en-IN')}
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td className="p-3">
                    <div className="font-semibold text-on-surface">Subscription Recurring Charges</div>
                    <div className="text-[11px] text-on-surface-variant">Scheduled entitlement billing cycle</div>
                  </td>
                  <td className="p-3 text-center font-mono">1</td>
                  <td className="p-3 text-right font-mono">₹{Number(invoice.subtotal || 0).toLocaleString('en-IN')}</td>
                  <td className="p-3 text-right font-mono">₹{Number(invoice.taxAmount || 0).toLocaleString('en-IN')}</td>
                  <td className="p-3 text-right font-mono font-bold text-on-surface">
                    ₹{Number(invoice.totalAmount || 0).toLocaleString('en-IN')}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* Financial Totals */}
        <div className="flex justify-end pt-2">
          <div className="w-64 space-y-2 text-xs">
            <div className="flex justify-between text-on-surface-variant">
              <span>Subtotal:</span>
              <span className="font-mono text-on-surface">₹{Number(invoice.subtotal || 0).toLocaleString('en-IN')}</span>
            </div>
            {Number(invoice.discountAmount || 0) > 0 && (
              <div className="flex justify-between text-emerald-600">
                <span>Discount Applied:</span>
                <span className="font-mono">-₹{Number(invoice.discountAmount || 0).toLocaleString('en-IN')}</span>
              </div>
            )}
            <div className="flex justify-between text-on-surface-variant">
              <span>Tax (GST / Statutory):</span>
              <span className="font-mono text-on-surface">₹{Number(invoice.taxAmount || 0).toLocaleString('en-IN')}</span>
            </div>
            <div className="flex justify-between text-sm font-bold border-t border-surface-container pt-2">
              <span className="text-on-surface">Total Invoiced:</span>
              <span className="font-mono text-primary">₹{Number(invoice.totalAmount || 0).toLocaleString('en-IN')}</span>
            </div>
            <div className="flex justify-between text-xs text-on-surface-variant">
              <span>Paid Amount:</span>
              <span className="font-mono text-emerald-700">₹{Number(invoice.paidAmount || 0).toLocaleString('en-IN')}</span>
            </div>
            <div className="flex justify-between text-xs font-bold border-t border-dashed border-outline-variant/40 pt-2">
              <span>Balance Due:</span>
              <span className="font-mono text-rose-700">₹{Number(invoice.balanceAmount ?? invoice.totalAmount).toLocaleString('en-IN')}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
