import React, { useState, useEffect } from 'react';
import { ArrowLeft, Save, CreditCard, RefreshCw } from 'lucide-react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { paymentService } from '../../../services/paymentService';
import { invoiceService } from '../../../services/invoiceService';

export default function RecordPayment() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const initialInvoiceId = searchParams.get('invoiceId') || '';
  const initialAmount = searchParams.get('amount') || '';

  const [invoices, setInvoices] = useState([]);
  const [selectedInvoiceId, setSelectedInvoiceId] = useState(initialInvoiceId);
  const [amount, setAmount] = useState(initialAmount);
  const [paymentMethod, setPaymentMethod] = useState('BANK_TRANSFER');
  const [transactionReference, setTransactionReference] = useState('');
  const [notes, setNotes] = useState('');
  const [loadingInvoices, setLoadingInvoices] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    async function loadOpenInvoices() {
      try {
        setLoadingInvoices(true);
        const data = await invoiceService.getAll();
        const list = Array.isArray(data) ? data : (data?.content || []);
        // Prioritize unpaid or pending invoices
        const openList = list.filter(i => i.status !== 'PAID' && i.status !== 'CANCELLED');
        setInvoices(openList.length > 0 ? openList : list);
        if (!selectedInvoiceId && openList.length > 0) {
          setSelectedInvoiceId(openList[0].id.toString());
          if (!amount) {
            setAmount(openList[0].balanceAmount || openList[0].totalAmount || '');
          }
        }
      } catch (err) {
        console.error('Failed to load invoices:', err);
      } finally {
        setLoadingInvoices(false);
      }
    }
    loadOpenInvoices();
  }, []);

  const handleInvoiceChange = (invId) => {
    setSelectedInvoiceId(invId);
    const chosen = invoices.find(i => i.id.toString() === invId.toString());
    if (chosen) {
      setAmount(chosen.balanceAmount || chosen.totalAmount || '');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!selectedInvoiceId) {
      setError('Please select an invoice to apply the payment to');
      return;
    }
    if (!amount || Number(amount) <= 0) {
      setError('Payment amount must be greater than 0');
      return;
    }

    try {
      setSubmitting(true);
      setError('');
      await paymentService.record(selectedInvoiceId, {
        amount: Number(amount),
        paymentMethod,
        transactionReference: transactionReference.trim() || undefined,
        notes: notes.trim() || undefined,
        paymentDate: new Date().toISOString()
      });
      navigate('/admin/payments');
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to record payment');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate('/admin/payments')}
          className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container"
        >
          <ArrowLeft className="w-4 h-4" />
        </button>
        <div>
          <h1 className="text-xl font-bold text-on-surface">Record Payment Settlement</h1>
          <p className="text-xs text-on-surface-variant">
            Reconcile bank wire transfers, UPI transactions, cards, or cash against client invoices.
          </p>
        </div>
      </div>

      {error && (
        <div className="p-3 bg-error-container/20 border border-error/30 text-error rounded-xl text-xs flex justify-between">
          <span>{error}</span>
          <button onClick={() => setError('')} className="font-bold">×</button>
        </div>
      )}

      <form onSubmit={handleSubmit} className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-4">
        <div>
          <label className="block text-xs font-medium text-on-surface mb-1">
            Target Invoice *
          </label>
          <select
            value={selectedInvoiceId}
            onChange={(e) => handleInvoiceChange(e.target.value)}
            required
            className="w-full h-10 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest"
          >
            <option value="">-- Select Invoice --</option>
            {invoices.map((inv) => (
              <option key={inv.id} value={inv.id}>
                {inv.invoiceNumber || `INV-${inv.id}`} — {inv.customerName || `Customer #${inv.customerId}`} — Due: ₹{Number(inv.balanceAmount ?? inv.totalAmount).toLocaleString('en-IN')} ({inv.status})
              </option>
            ))}
          </select>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-medium text-on-surface mb-1">Settled Amount (₹) *</label>
            <input
              type="number"
              step="0.01"
              required
              min="0.01"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest font-mono"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-on-surface mb-1">Payment Method Rail *</label>
            <select
              value={paymentMethod}
              onChange={(e) => setPaymentMethod(e.target.value)}
              className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest"
            >
              <option value="BANK_TRANSFER">Bank Transfer (NEFT/RTGS/IMPS)</option>
              <option value="UPI">UPI AutoPay / VPA</option>
              <option value="CARD">Credit / Debit Card</option>
              <option value="CASH">Cash Settlement</option>
              <option value="OTHER">Other Gateway / Rail</option>
            </select>
          </div>
        </div>

        <div>
          <label className="block text-xs font-medium text-on-surface mb-1">
            Transaction / UTR Reference
          </label>
          <input
            type="text"
            placeholder="e.g. UTR20261031987654 or PAY_RZP_1029"
            value={transactionReference}
            onChange={(e) => setTransactionReference(e.target.value)}
            className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest font-mono"
          />
        </div>

        <div>
          <label className="block text-xs font-medium text-on-surface mb-1">Audit Notes</label>
          <textarea
            rows="2"
            placeholder="Optional audit notes or reconciliation memo"
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            className="w-full p-2.5 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest resize-none"
          />
        </div>

        <div className="pt-4 border-t border-surface-container flex items-center justify-end gap-2">
          <button
            type="button"
            onClick={() => navigate('/admin/payments')}
            className="px-4 py-2 border border-outline-variant/40 rounded-lg text-xs font-semibold text-on-surface-variant hover:bg-surface-container"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={submitting}
            className="px-4 py-2 bg-primary hover:bg-primary-container text-white rounded-lg text-xs font-semibold flex items-center gap-1.5 shadow-sm"
          >
            <Save className={`w-3.5 h-3.5 ${submitting ? 'animate-spin' : ''}`} />
            <span>{submitting ? 'Recording...' : 'Commit Payment'}</span>
          </button>
        </div>
      </form>
    </div>
  );
}
