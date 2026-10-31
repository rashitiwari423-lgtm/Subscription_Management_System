import React, { useState, useEffect } from 'react';
import { RefreshCw, Plus, Search, Filter, ShieldCheck, AlertCircle, ArrowUpRight, AlertTriangle } from 'lucide-react';
import { subscriptionService } from '../../../services/subscriptionService';
import { useNavigate } from 'react-router-dom';

export default function SubscriptionLifecycle() {
  const [subList, setSubList] = useState([]);
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const loadSubscriptions = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await subscriptionService.getAll({
        search: searchTerm,
        status: filterStatus !== 'ALL' ? filterStatus : undefined
      });
      setSubList(data || []);
    } catch (err) {
      setError(err.userMessage || 'Failed to load subscriptions');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSubscriptions();
  }, [filterStatus]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    loadSubscriptions();
  };

  const formatCurr = (val) => `₹${Number(val || 0).toLocaleString('en-IN', { maximumFractionDigits: 2 })}`;

  const totalAmount = subList.reduce((acc, s) => acc + Number(s.totalAmount || 0), 0);
  const activeCount = subList.filter(s => s.status === 'ACTIVE').length;
  const pausedCount = subList.filter(s => s.status === 'PAUSED').length;

  return (
    <div className="space-y-5">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Subscription Lifecycle Orchestration</h1>
          <p className="text-xs text-on-surface-variant">
            Monitor recurring subscriptions, status lifecycles, and automated renewal cadences.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={loadSubscriptions}
            className="p-2 border border-outline-variant/40 rounded-lg hover:bg-surface-container text-on-surface-variant"
            title="Refresh"
          >
            <RefreshCw className="w-3.5 h-3.5" />
          </button>
          <button
            onClick={() => navigate('/admin/quotations/new')}
            className="px-3.5 py-2 rounded-lg bg-primary hover:bg-primary-container text-white text-xs font-semibold flex items-center gap-1.5 shadow-sm"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>New Subscription via Quote</span>
          </button>
        </div>
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-outline">TOTAL CONTRACTS</div>
          <div className="text-xl font-bold text-on-surface mt-1">{subList.length}</div>
        </div>
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-emerald-700">ACTIVE SUBSCRIPTIONS</div>
          <div className="text-xl font-bold text-emerald-700 mt-1 font-tabular">
            {activeCount}
          </div>
        </div>
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-amber-700">PAUSED CONTRACTS</div>
          <div className="text-xl font-bold text-amber-700 mt-1 font-tabular">
            {pausedCount}
          </div>
        </div>
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-primary">AGGREGATE VALUE</div>
          <div className="text-xl font-bold text-primary mt-1 font-tabular">
            {formatCurr(totalAmount)}
          </div>
        </div>
      </div>

      <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl flex flex-col sm:flex-row items-center justify-between gap-3">
        <form onSubmit={handleSearchSubmit} className="relative w-full sm:w-80">
          <Search className="w-3.5 h-3.5 absolute left-3 top-2.5 text-outline" />
          <input
            type="text"
            placeholder="Search subscriptions (Press Enter)..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full h-8 pl-9 pr-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest focus:outline-none focus:border-primary-container"
          />
        </form>

        <div className="flex items-center gap-2 w-full sm:w-auto">
          <select
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value)}
            className="h-8 px-2.5 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest text-on-surface"
          >
            <option value="ALL">All Statuses</option>
            <option value="ACTIVE">ACTIVE</option>
            <option value="PAUSED">PAUSED</option>
            <option value="CLOSED">CLOSED</option>
            <option value="EXPIRED">EXPIRED</option>
          </select>
        </div>
      </div>

      <div className="bg-surface-container-lowest border border-outline-variant/40 rounded-xl shadow-elevation-1 overflow-hidden">
        {loading ? (
          <div className="p-12 text-center text-xs text-on-surface-variant flex items-center justify-center gap-2">
            <RefreshCw className="w-4 h-4 animate-spin text-primary" />
            <span>Loading subscriptions...</span>
          </div>
        ) : error ? (
          <div className="p-6 text-center space-y-2 bg-error-container/20 rounded-xl border border-error/30">
            <AlertTriangle className="w-6 h-6 text-error mx-auto" />
            <p className="text-xs text-error font-medium">{error}</p>
            <button onClick={loadSubscriptions} className="px-3 py-1 bg-primary text-white text-xs rounded font-semibold">
              Retry
            </button>
          </div>
        ) : subList.length === 0 ? (
          <div className="p-12 text-center text-xs text-on-surface-variant">
            No subscriptions found. Convert an accepted Quotation to create a subscription.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-surface-container-low text-on-surface-variant uppercase text-[10px] tracking-wider border-b border-outline-variant/20 font-semibold">
                <tr>
                  <th className="p-3.5">Subscription Ref</th>
                  <th className="p-3.5">Customer ID</th>
                  <th className="p-3.5">Plan ID</th>
                  <th className="p-3.5">Seats / Qty</th>
                  <th className="p-3.5">Amount</th>
                  <th className="p-3.5">Start Date</th>
                  <th className="p-3.5">Next Billing</th>
                  <th className="p-3.5">Status</th>
                  <th className="p-3.5 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-surface-container-low">
                {subList.map((sub) => (
                  <tr
                    key={sub.id}
                    onClick={() => navigate(`/admin/subscriptions/${sub.id}`)}
                    className="hover:bg-surface-container-low/60 cursor-pointer transition-colors"
                  >
                    <td className="p-3.5 font-mono font-bold text-primary">{sub.subscriptionNumber}</td>
                    <td className="p-3.5 font-semibold text-on-surface">Customer #{sub.customerId}</td>
                    <td className="p-3.5 font-mono">Plan #{sub.planId}</td>
                    <td className="p-3.5 font-mono">{sub.quantity}</td>
                    <td className="p-3.5 font-mono font-bold text-on-surface">{formatCurr(sub.totalAmount)}</td>
                    <td className="p-3.5 font-mono text-on-surface-variant">{sub.startDate}</td>
                    <td className="p-3.5 font-mono text-on-surface-variant">{sub.nextBillingDate || '—'}</td>
                    <td className="p-3.5">
                      <span className={`px-2 py-0.5 rounded-full text-[10px] font-semibold ${
                        sub.status === 'ACTIVE' ? 'bg-emerald-50 text-emerald-700' :
                        sub.status === 'PAUSED' ? 'bg-amber-50 text-amber-700' :
                        sub.status === 'CLOSED' ? 'bg-rose-50 text-rose-700' : 'bg-surface-container text-on-surface-variant'
                      }`}>
                        {sub.status}
                      </span>
                    </td>
                    <td className="p-3.5 text-right">
                      <span className="text-primary font-semibold text-xs hover:underline">
                        Manage &rarr;
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
