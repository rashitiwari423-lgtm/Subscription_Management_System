import React, { useState, useEffect } from 'react';
import { CreditCard, Search, Filter, ShieldCheck, CheckCircle2, XCircle, ArrowUpRight, RefreshCw, AlertTriangle } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { paymentService } from '../../../services/paymentService';

export default function PaymentsLedger() {
  const [paymentList, setPaymentList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [filterMethod, setFilterMethod] = useState('ALL');
  const navigate = useNavigate();

  const loadPayments = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await paymentService.getAll();
      const list = Array.isArray(data) ? data : (data?.content || []);
      setPaymentList(list);
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to load payments');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPayments();
  }, []);

  const filtered = paymentList.filter(p => {
    const matchesMethod = filterMethod === 'ALL' || p.paymentMethod === filterMethod;
    const matchesSearch = !search ||
      (p.paymentNumber && p.paymentNumber.toLowerCase().includes(search.toLowerCase())) ||
      (p.customerName && p.customerName.toLowerCase().includes(search.toLowerCase())) ||
      (p.invoiceNumber && p.invoiceNumber.toLowerCase().includes(search.toLowerCase())) ||
      (p.transactionReference && p.transactionReference.toLowerCase().includes(search.toLowerCase()));
    return matchesMethod && matchesSearch;
  });

  const totalSettled = paymentList
    .filter(p => p.status === 'SUCCESS' || p.status === 'COMPLETED' || p.status === 'PAID')
    .reduce((acc, p) => acc + (Number(p.amount) || 0), 0);

  const getStatusBadge = (status) => {
    switch (status) {
      case 'SUCCESS':
      case 'COMPLETED':
      case 'PAID':
        return 'bg-emerald-50 text-emerald-700 border border-emerald-200';
      case 'FAILED':
        return 'bg-rose-50 text-rose-700 border border-rose-200';
      case 'PENDING':
        return 'bg-amber-50 text-amber-700 border border-amber-200';
      default:
        return 'bg-surface-container text-on-surface-variant';
    }
  };

  return (
    <div className="space-y-5">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Payments & Settlement Ledger</h1>
          <p className="text-xs text-on-surface-variant">
            Multi-rail payment settlement telemetry across UPI, Cards, Bank Transfers, and Cash.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={loadPayments}
            className="p-2 rounded-lg border border-outline-variant/40 hover:bg-surface-container text-on-surface-variant"
            title="Refresh Payments"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          </button>
          <button
            onClick={() => navigate('/admin/payments/new')}
            className="px-3.5 py-2 rounded-lg bg-primary hover:bg-primary-container text-white text-xs font-semibold flex items-center gap-1.5 shadow-sm"
          >
            <CreditCard className="w-3.5 h-3.5" />
            <span>Record Settlement</span>
          </button>
        </div>
      </div>

      {error && (
        <div className="p-3 bg-error-container/20 border border-error/30 text-error rounded-xl text-xs flex justify-between">
          <span>{error}</span>
          <button onClick={() => setError('')} className="font-bold">×</button>
        </div>
      )}

      {/* Metrics Row */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-outline">TOTAL TRANSACTIONS</div>
          <div className="text-xl font-bold text-on-surface mt-1">{paymentList.length}</div>
        </div>
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-emerald-700">GROSS SETTLED</div>
          <div className="text-xl font-bold text-emerald-700 mt-1">₹{totalSettled.toLocaleString('en-IN')}</div>
        </div>
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-outline">BANK TRANSFERS</div>
          <div className="text-xl font-bold text-on-surface mt-1">
            {paymentList.filter(p => p.paymentMethod === 'BANK_TRANSFER').length}
          </div>
        </div>
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-outline">UPI / DIGITAL</div>
          <div className="text-xl font-bold text-on-surface mt-1">
            {paymentList.filter(p => p.paymentMethod === 'UPI' || p.paymentMethod === 'CARD').length}
          </div>
        </div>
      </div>

      {/* Search and Filters */}
      <div className="flex flex-col sm:flex-row items-center gap-3">
        <div className="relative flex-1 w-full">
          <Search className="w-3.5 h-3.5 text-on-surface-variant absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Search payment number, transaction ref, customer, invoice..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-9 pr-3 py-1.5 rounded-lg border border-outline-variant/40 bg-surface-container-lowest text-xs text-on-surface"
          />
        </div>
        <div className="flex items-center gap-1.5 self-start sm:self-auto overflow-x-auto">
          {['ALL', 'BANK_TRANSFER', 'UPI', 'CARD', 'CASH', 'OTHER'].map((method) => (
            <button
              key={method}
              onClick={() => setFilterMethod(method)}
              className={`px-2.5 py-1 text-[11px] font-semibold rounded-lg transition-colors ${
                filterMethod === method
                  ? 'bg-primary text-white'
                  : 'bg-surface-container-low text-on-surface-variant hover:bg-surface-container'
              }`}
            >
              {method}
            </button>
          ))}
        </div>
      </div>

      {/* Table */}
      <div className="bg-surface-container-lowest border border-outline-variant/40 rounded-xl shadow-elevation-1 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-surface-container-low text-on-surface-variant uppercase text-[10px] tracking-wider border-b border-outline-variant/20 font-semibold">
              <tr>
                <th className="p-3.5">Payment #</th>
                <th className="p-3.5">Invoice Ref</th>
                <th className="p-3.5">Customer Name</th>
                <th className="p-3.5">Amount Settled</th>
                <th className="p-3.5">Payment Rail</th>
                <th className="p-3.5">Reference / Txn ID</th>
                <th className="p-3.5">Settlement Date</th>
                <th className="p-3.5">Status</th>
                <th className="p-3.5 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-container-low">
              {loading ? (
                <tr>
                  <td colSpan="9" className="p-8 text-center text-on-surface-variant">
                    <RefreshCw className="w-5 h-5 animate-spin mx-auto mb-2 text-primary" />
                    <span>Loading payment transactions...</span>
                  </td>
                </tr>
              ) : filtered.length === 0 ? (
                <tr>
                  <td colSpan="9" className="p-8 text-center text-on-surface-variant">
                    No payment records found.
                  </td>
                </tr>
              ) : (
                filtered.map((pay) => (
                  <tr
                    key={pay.id}
                    onClick={() => navigate(`/admin/payments/${pay.id}`)}
                    className="hover:bg-surface-container-low/60 cursor-pointer transition-colors"
                  >
                    <td className="p-3.5 font-mono font-bold text-primary">{pay.paymentNumber || `PAY-${pay.id}`}</td>
                    <td className="p-3.5 font-mono text-on-surface-variant">{pay.invoiceNumber || (pay.invoiceId ? `INV-${pay.invoiceId}` : 'N/A')}</td>
                    <td className="p-3.5 font-bold text-on-surface">{pay.customerName || `Customer #${pay.customerId}`}</td>
                    <td className="p-3.5 font-mono font-bold text-on-surface">
                      ₹{Number(pay.amount || 0).toLocaleString('en-IN')}
                    </td>
                    <td className="p-3.5 font-medium">{pay.paymentMethod}</td>
                    <td className="p-3.5 font-mono text-[11px] text-on-surface-variant">{pay.transactionReference || 'N/A'}</td>
                    <td className="p-3.5 font-mono text-on-surface-variant">
                      {pay.paymentDate ? new Date(pay.paymentDate).toLocaleDateString() : 'N/A'}
                    </td>
                    <td className="p-3.5">
                      <span className={`px-2 py-0.5 rounded-full text-[10px] font-semibold ${getStatusBadge(pay.status)}`}>
                        {pay.status}
                      </span>
                    </td>
                    <td className="p-3.5 text-right" onClick={(e) => e.stopPropagation()}>
                      <button
                        onClick={() => navigate(`/admin/payments/${pay.id}`)}
                        className="px-2.5 py-1 bg-surface-container hover:bg-surface-container-high rounded text-xs font-semibold text-primary"
                      >
                        Audit
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
