import React, { useState, useEffect } from 'react';
import { Receipt, Search, Filter, RefreshCw, AlertTriangle, ArrowUpRight, CheckCircle2, Clock } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { invoiceService } from '../../../services/invoiceService';
import { billingService } from '../../../services/billingService';

export default function InvoicesBillingRuns() {
  const [invoiceList, setInvoiceList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [runningBilling, setRunningBilling] = useState(false);
  const [billingResult, setBillingResult] = useState(null);
  const navigate = useNavigate();

  const loadInvoices = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await invoiceService.getAll();
      const list = Array.isArray(data) ? data : (data?.content || []);
      setInvoiceList(list);
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to load invoices');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadInvoices();
  }, []);

  const handleExecuteRecurringBilling = async () => {
    if (!window.confirm('Are you sure you want to run recurring billing? This will process due subscriptions and mark overdue invoices.')) {
      return;
    }
    try {
      setRunningBilling(true);
      setError('');
      setBillingResult(null);
      const summary = await billingService.triggerRecurringBilling();
      setBillingResult(summary);
      await loadInvoices();
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Recurring billing execution failed');
    } finally {
      setRunningBilling(false);
    }
  };

  const filtered = invoiceList.filter(i => {
    const matchesStatus = statusFilter === 'ALL' || i.status === statusFilter;
    const matchesSearch = !search ||
      (i.invoiceNumber && i.invoiceNumber.toLowerCase().includes(search.toLowerCase())) ||
      (i.customerName && i.customerName.toLowerCase().includes(search.toLowerCase())) ||
      (i.subscriptionNumber && i.subscriptionNumber.toLowerCase().includes(search.toLowerCase()));
    return matchesStatus && matchesSearch;
  });

  const totalInvoiced = invoiceList.reduce((acc, i) => acc + (Number(i.totalAmount) || 0), 0);
  const totalPaid = invoiceList
    .filter(i => i.status === 'PAID')
    .reduce((acc, i) => acc + (Number(i.totalAmount) || 0), 0);
  const totalOverdue = invoiceList
    .filter(i => i.status === 'OVERDUE')
    .reduce((acc, i) => acc + (Number(i.balanceAmount ?? i.totalAmount) || 0), 0);
  const totalPending = invoiceList
    .filter(i => i.status === 'PENDING' || i.status === 'DRAFT')
    .reduce((acc, i) => acc + (Number(i.balanceAmount ?? i.totalAmount) || 0), 0);

  const getStatusBadge = (status) => {
    switch (status) {
      case 'PAID':
        return 'bg-emerald-50 text-emerald-700 border border-emerald-200';
      case 'OVERDUE':
        return 'bg-rose-50 text-rose-700 border border-rose-200';
      case 'PENDING':
        return 'bg-amber-50 text-amber-700 border border-amber-200';
      case 'DRAFT':
        return 'bg-surface-container text-on-surface-variant border border-outline-variant/30';
      case 'CANCELLED':
        return 'bg-zinc-100 text-zinc-600 border border-zinc-200';
      default:
        return 'bg-surface-container text-on-surface-variant';
    }
  };

  return (
    <div className="space-y-5">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Invoices & Billing Execution</h1>
          <p className="text-xs text-on-surface-variant">
            Automated invoice generation, GST tax breakdown, and payment status tracking.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={loadInvoices}
            className="p-2 rounded-lg border border-outline-variant/40 hover:bg-surface-container text-on-surface-variant"
            title="Refresh Invoices"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          </button>
          <button
            onClick={handleExecuteRecurringBilling}
            disabled={runningBilling}
            className="px-3.5 py-2 rounded-lg bg-primary hover:bg-primary-container text-white text-xs font-semibold flex items-center gap-1.5 shadow-sm"
          >
            <ArrowUpRight className={`w-3.5 h-3.5 ${runningBilling ? 'animate-spin' : ''}`} />
            <span>{runningBilling ? 'Running Billing...' : 'Execute Recurring Billing'}</span>
          </button>
        </div>
      </div>

      {billingResult && (
        <div className="p-4 bg-emerald-50 border border-emerald-200 rounded-xl text-xs space-y-1">
          <div className="flex items-center justify-between">
            <span className="font-bold text-emerald-800">Recurring Billing Completed Successfully</span>
            <button onClick={() => setBillingResult(null)} className="font-bold text-emerald-900">×</button>
          </div>
          <div className="flex flex-wrap gap-4 text-emerald-700 pt-1">
            <span>Subscriptions Checked: <strong>{billingResult.subscriptionsChecked}</strong></span>
            <span>Invoices Created: <strong>{billingResult.invoicesCreated}</strong></span>
            <span>Overdue Marked: <strong>{billingResult.overdueInvoicesUpdated}</strong></span>
            <span>Already Processed: <strong>{billingResult.alreadyProcessed}</strong></span>
            <span>Failures: <strong>{billingResult.failed}</strong></span>
          </div>
        </div>
      )}

      {error && (
        <div className="p-3 bg-error-container/20 border border-error/30 text-error rounded-xl text-xs flex justify-between">
          <span>{error}</span>
          <button onClick={() => setError('')} className="font-bold">×</button>
        </div>
      )}

      {/* Metrics Row */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-outline">TOTAL INVOICED</div>
          <div className="text-xl font-bold text-on-surface mt-1">₹{totalInvoiced.toLocaleString('en-IN')}</div>
        </div>
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-emerald-700">COLLECTED (PAID)</div>
          <div className="text-xl font-bold text-emerald-700 mt-1">
            ₹{totalPaid.toLocaleString('en-IN')}
          </div>
        </div>
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-rose-700">OVERDUE ARREARS</div>
          <div className="text-xl font-bold text-rose-700 mt-1">
            ₹{totalOverdue.toLocaleString('en-IN')}
          </div>
        </div>
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-amber-700">PENDING / DRAFT</div>
          <div className="text-xl font-bold text-amber-700 mt-1">
            ₹{totalPending.toLocaleString('en-IN')}
          </div>
        </div>
      </div>

      {/* Filters */}
      <div className="flex flex-col sm:flex-row items-center gap-3">
        <div className="relative flex-1 w-full">
          <Search className="w-3.5 h-3.5 text-on-surface-variant absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Search invoice number, customer name, subscription..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-9 pr-3 py-1.5 rounded-lg border border-outline-variant/40 bg-surface-container-lowest text-xs text-on-surface"
          />
        </div>
        <div className="flex items-center gap-1.5 self-start sm:self-auto overflow-x-auto">
          {['ALL', 'DRAFT', 'PENDING', 'PAID', 'OVERDUE', 'CANCELLED'].map((status) => (
            <button
              key={status}
              onClick={() => setStatusFilter(status)}
              className={`px-2.5 py-1 text-[11px] font-semibold rounded-lg transition-colors ${
                statusFilter === status
                  ? 'bg-primary text-white'
                  : 'bg-surface-container-low text-on-surface-variant hover:bg-surface-container'
              }`}
            >
              {status}
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
                <th className="p-3.5">Invoice #</th>
                <th className="p-3.5">Billed Customer</th>
                <th className="p-3.5">Invoice Amount</th>
                <th className="p-3.5">Tax (GST)</th>
                <th className="p-3.5">Invoice Date</th>
                <th className="p-3.5">Due Date</th>
                <th className="p-3.5">Status</th>
                <th className="p-3.5 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-container-low">
              {loading ? (
                <tr>
                  <td colSpan="8" className="p-8 text-center text-on-surface-variant">
                    <RefreshCw className="w-5 h-5 animate-spin mx-auto mb-2 text-primary" />
                    <span>Loading invoices...</span>
                  </td>
                </tr>
              ) : filtered.length === 0 ? (
                <tr>
                  <td colSpan="8" className="p-8 text-center text-on-surface-variant">
                    No invoices matching current filter.
                  </td>
                </tr>
              ) : (
                filtered.map((inv) => (
                  <tr
                    key={inv.id}
                    onClick={() => navigate(`/admin/invoices/${inv.id}`)}
                    className="hover:bg-surface-container-low/60 cursor-pointer transition-colors"
                  >
                    <td className="p-3.5 font-mono font-bold text-primary">{inv.invoiceNumber || `INV-${inv.id}`}</td>
                    <td className="p-3.5 font-bold text-on-surface">{inv.customerName || `Customer #${inv.customerId}`}</td>
                    <td className="p-3.5 font-mono font-bold text-on-surface">
                      ₹{Number(inv.totalAmount || 0).toLocaleString('en-IN')}
                    </td>
                    <td className="p-3.5 font-mono text-on-surface-variant">
                      ₹{Number(inv.taxAmount || 0).toLocaleString('en-IN')}
                    </td>
                    <td className="p-3.5 font-mono text-on-surface-variant">{inv.invoiceDate || 'N/A'}</td>
                    <td className="p-3.5 font-mono text-on-surface-variant">{inv.dueDate || 'N/A'}</td>
                    <td className="p-3.5">
                      <span className={`px-2 py-0.5 rounded-full text-[10px] font-semibold ${getStatusBadge(inv.status)}`}>
                        {inv.status}
                      </span>
                    </td>
                    <td className="p-3.5 text-right" onClick={(e) => e.stopPropagation()}>
                      <button
                        onClick={() => navigate(`/admin/invoices/${inv.id}`)}
                        className="px-2.5 py-1 bg-surface-container hover:bg-surface-container-high rounded text-xs font-semibold text-primary"
                      >
                        Details
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
