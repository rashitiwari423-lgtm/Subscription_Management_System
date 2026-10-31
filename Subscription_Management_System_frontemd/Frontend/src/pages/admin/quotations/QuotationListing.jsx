import React, { useState, useEffect } from 'react';
import { FileSpreadsheet, Plus, Search, ChevronRight, Eye, CheckCircle2, Clock, RefreshCw, AlertTriangle } from 'lucide-react';
import { quotationService } from '../../../services/quotationService';
import { useNavigate } from 'react-router-dom';

export default function QuotationListing() {
  const [quotationList, setQuotationList] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const loadQuotations = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await quotationService.getAll({
        search: searchTerm,
        status: statusFilter !== 'ALL' ? statusFilter : undefined
      });
      setQuotationList(data || []);
    } catch (err) {
      setError(err.userMessage || 'Failed to load quotations');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadQuotations();
  }, [statusFilter]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    loadQuotations();
  };

  const formatCurr = (val) => `₹${Number(val || 0).toLocaleString('en-IN', { maximumFractionDigits: 2 })}`;

  return (
    <div className="space-y-5">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Quotation Pipeline &amp; CPQ Deal Desk</h1>
          <p className="text-xs text-on-surface-variant">
            Track multi-tier proposals, lifecycle conversions, and client approvals.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={loadQuotations}
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
            <span>Create CPQ Quotation</span>
          </button>
        </div>
      </div>

      <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl flex flex-col sm:flex-row items-center justify-between gap-3">
        <form onSubmit={handleSearchSubmit} className="relative w-full sm:w-80">
          <Search className="w-3.5 h-3.5 absolute left-3 top-2.5 text-outline" />
          <input
            type="text"
            placeholder="Search quotations (Press Enter)..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full h-8 pl-9 pr-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest focus:outline-none focus:border-primary-container"
          />
        </form>

        <div className="flex items-center gap-2 w-full sm:w-auto">
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="h-8 px-2.5 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest text-on-surface"
          >
            <option value="ALL">All Statuses</option>
            <option value="DRAFT">DRAFT</option>
            <option value="SENT">SENT</option>
            <option value="ACCEPTED">ACCEPTED</option>
            <option value="REJECTED">REJECTED</option>
            <option value="CONVERTED">CONVERTED</option>
            <option value="EXPIRED">EXPIRED</option>
          </select>
        </div>
      </div>

      <div className="bg-surface-container-lowest border border-outline-variant/40 rounded-xl shadow-elevation-1 overflow-hidden">
        {loading ? (
          <div className="p-12 text-center text-xs text-on-surface-variant flex items-center justify-center gap-2">
            <RefreshCw className="w-4 h-4 animate-spin text-primary" />
            <span>Loading quotations...</span>
          </div>
        ) : error ? (
          <div className="p-6 text-center space-y-2 bg-error-container/20 rounded-xl border border-error/30">
            <AlertTriangle className="w-6 h-6 text-error mx-auto" />
            <p className="text-xs text-error font-medium">{error}</p>
            <button onClick={loadQuotations} className="px-3 py-1 bg-primary text-white text-xs rounded font-semibold">
              Retry
            </button>
          </div>
        ) : quotationList.length === 0 ? (
          <div className="p-12 text-center text-xs text-on-surface-variant">
            No quotations found. Click "Create CPQ Quotation" above to initiate a proposal.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-surface-container-low text-on-surface-variant uppercase text-[10px] tracking-wider border-b border-outline-variant/20 font-semibold">
                <tr>
                  <th className="p-3.5">Quote Reference</th>
                  <th className="p-3.5">Customer</th>
                  <th className="p-3.5">Total Amount</th>
                  <th className="p-3.5">Discount</th>
                  <th className="p-3.5">Tax</th>
                  <th className="p-3.5">Status</th>
                  <th className="p-3.5">Valid Until</th>
                  <th className="p-3.5 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-surface-container-low">
                {quotationList.map((qt) => (
                  <tr
                    key={qt.id}
                    onClick={() => navigate(`/admin/quotations/${qt.id}`)}
                    className="hover:bg-surface-container-low/60 cursor-pointer transition-colors"
                  >
                    <td className="p-3.5 font-mono font-bold text-primary">{qt.quotationNumber}</td>
                    <td className="p-3.5 font-bold text-on-surface">
                      {qt.customerCompanyName || `Customer #${qt.customerId}`}
                    </td>
                    <td className="p-3.5 font-mono font-bold text-on-surface">
                      {formatCurr(qt.totalAmount)}
                    </td>
                    <td className="p-3.5 font-mono text-emerald-700 font-semibold">
                      {formatCurr(qt.discountAmount)}
                    </td>
                    <td className="p-3.5 font-mono text-on-surface-variant">
                      {formatCurr(qt.taxAmount)}
                    </td>
                    <td className="p-3.5">
                      <span className={`px-2 py-0.5 rounded-full text-[10px] font-semibold ${
                        qt.status === 'CONVERTED' ? 'bg-primary/10 text-primary' :
                        qt.status === 'ACCEPTED' ? 'bg-emerald-50 text-emerald-700' :
                        qt.status === 'SENT' ? 'bg-amber-50 text-amber-700' :
                        qt.status === 'REJECTED' || qt.status === 'EXPIRED' ? 'bg-rose-50 text-rose-700' :
                        'bg-surface-container text-on-surface-variant'
                      }`}>
                        {qt.status}
                      </span>
                    </td>
                    <td className="p-3.5 font-mono text-on-surface-variant">{qt.validUntil}</td>
                    <td className="p-3.5 text-right">
                      <span className="text-primary font-semibold text-xs hover:underline">
                        View Details &rarr;
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
