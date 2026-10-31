import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, CheckCircle2, ShieldCheck, CreditCard, RefreshCw, AlertTriangle } from 'lucide-react';
import { paymentService } from '../../../services/paymentService';

export default function PaymentDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [pay, setPay] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    async function loadPayment() {
      try {
        setLoading(true);
        setError('');
        const data = await paymentService.getById(id);
        setPay(data);
      } catch (err) {
        setError(err?.response?.data?.message || err?.message || 'Failed to load payment record');
      } finally {
        setLoading(false);
      }
    }
    if (id) loadPayment();
  }, [id]);

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center p-12 text-center">
        <RefreshCw className="w-8 h-8 animate-spin text-primary mb-2" />
        <p className="text-xs text-on-surface-variant font-medium">Loading payment record...</p>
      </div>
    );
  }

  if (error && !pay) {
    return (
      <div className="p-8 text-center space-y-4">
        <div className="inline-flex p-3 rounded-full bg-error-container text-error">
          <AlertTriangle className="w-6 h-6" />
        </div>
        <p className="text-sm font-medium text-error">{error}</p>
        <button
          onClick={() => navigate('/admin/payments')}
          className="px-4 py-2 bg-surface-container text-xs font-semibold rounded-lg text-on-surface"
        >
          Back to Payments
        </button>
      </div>
    );
  }

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
          <h1 className="text-xl font-bold text-on-surface">Payment Audit Record</h1>
          <p className="text-xs text-on-surface-variant font-mono">
            {pay.paymentNumber || `PAY-${pay.id}`} • {pay.customerName || `Customer #${pay.customerId}`}
          </p>
        </div>
      </div>

      <div className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-4">
        <div className="flex items-center justify-between border-b border-surface-container pb-4">
          <div>
            <span className="text-[10px] uppercase font-mono text-outline">Settled Amount</span>
            <div className="text-2xl font-extrabold text-on-surface font-mono mt-0.5">
              ₹{Number(pay.amount || 0).toLocaleString('en-IN')}
            </div>
          </div>
          <span className="px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
            {pay.status}
          </span>
        </div>

        <div className="grid grid-cols-2 gap-4 text-xs">
          <div>
            <span className="text-outline uppercase text-[10px]">Payment Rail</span>
            <div className="font-bold text-on-surface mt-0.5">{pay.paymentMethod}</div>
          </div>
          <div>
            <span className="text-outline uppercase text-[10px]">Transaction Reference</span>
            <div className="font-bold text-on-surface font-mono mt-0.5">{pay.transactionReference || 'N/A'}</div>
          </div>
          <div>
            <span className="text-outline uppercase text-[10px]">Invoice Reference</span>
            <div className="font-bold text-primary font-mono mt-0.5">
              {pay.invoiceNumber || (pay.invoiceId ? `INV-${pay.invoiceId}` : 'N/A')}
            </div>
          </div>
          <div>
            <span className="text-outline uppercase text-[10px]">Settlement Date</span>
            <div className="font-mono text-on-surface-variant mt-0.5">
              {pay.paymentDate ? new Date(pay.paymentDate).toLocaleString() : 'N/A'}
            </div>
          </div>
        </div>

        {pay.notes && (
          <div className="pt-2 border-t border-surface-container text-xs">
            <span className="text-outline uppercase text-[10px]">Audit Notes</span>
            <p className="text-on-surface mt-0.5">{pay.notes}</p>
          </div>
        )}
      </div>
    </div>
  );
}
