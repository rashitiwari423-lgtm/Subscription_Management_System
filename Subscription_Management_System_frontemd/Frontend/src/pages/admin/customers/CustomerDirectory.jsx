import React, { useState, useEffect } from 'react';
import { Search, Plus, Filter, ArrowUpDown, MoreHorizontal, ShieldCheck, Download, Trash2, Edit3, Eye, RefreshCw, AlertTriangle, UserCheck } from 'lucide-react';
import { customerService } from '../../../services/customerService';
import { useNavigate } from 'react-router-dom';

export default function CustomerDirectory() {
  const [customerList, setCustomerList] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const loadCustomers = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await customerService.getAll({
        search: searchTerm,
        status: statusFilter !== 'ALL' ? statusFilter : undefined
      });
      setCustomerList(data || []);
    } catch (err) {
      setError(err.userMessage || 'Failed to load customers');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCustomers();
  }, [statusFilter]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    loadCustomers();
  };

  const handleToggleStatus = async (cust, e) => {
    e.stopPropagation();
    const newStatus = cust.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    if (window.confirm(`Are you sure you want to change status of ${cust.companyName} to ${newStatus}?`)) {
      try {
        await customerService.updateStatus(cust.id, newStatus);
        loadCustomers();
      } catch (err) {
        alert(err.userMessage || 'Failed to update customer status');
      }
    }
  };

  return (
    <div className="space-y-5">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Customer Directory</h1>
          <p className="text-xs text-on-surface-variant">
            Manage client profiles, organization accounts, and billing portfolios.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={() => navigate('/admin/customers/new')}
            className="px-3.5 py-2 rounded-lg bg-primary hover:bg-primary-container text-white text-xs font-semibold flex items-center gap-1.5 shadow-sm transition-all"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>Add Customer</span>
          </button>
        </div>
      </div>

      {/* KPI strip */}
      <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-outline">TOTAL ACCOUNTS</div>
          <div className="text-xl font-bold text-on-surface mt-1">{customerList.length}</div>
        </div>
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-emerald-700">ACTIVE ACCOUNTS</div>
          <div className="text-xl font-bold text-emerald-700 mt-1">
            {customerList.filter(c => c.status === 'ACTIVE').length}
          </div>
        </div>
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-amber-700">INACTIVE / SUSPENDED</div>
          <div className="text-xl font-bold text-amber-700 mt-1">
            {customerList.filter(c => c.status !== 'ACTIVE').length}
          </div>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl flex flex-col sm:flex-row items-center justify-between gap-3">
        <form onSubmit={handleSearchSubmit} className="relative w-full sm:w-80">
          <Search className="w-3.5 h-3.5 absolute left-3 top-2.5 text-outline" />
          <input
            type="text"
            placeholder="Search by company, contact, or email (Enter to search)..."
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
            <option value="ACTIVE">ACTIVE</option>
            <option value="INACTIVE">INACTIVE</option>
            <option value="SUSPENDED">SUSPENDED</option>
          </select>
          <button
            onClick={loadCustomers}
            className="p-1.5 border border-outline-variant/60 rounded-lg hover:bg-surface-container text-outline"
            title="Refresh"
          >
            <RefreshCw className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>

      {/* Customers Data Table */}
      <div className="bg-surface-container-lowest border border-outline-variant/40 rounded-xl shadow-elevation-1 overflow-hidden">
        {loading ? (
          <div className="p-8 text-center text-xs text-on-surface-variant flex items-center justify-center gap-2">
            <RefreshCw className="w-4 h-4 animate-spin text-primary" />
            <span>Loading customers...</span>
          </div>
        ) : error ? (
          <div className="p-6 text-center space-y-2">
            <AlertTriangle className="w-6 h-6 text-error mx-auto" />
            <p className="text-xs text-error font-medium">{error}</p>
            <button
              onClick={loadCustomers}
              className="px-3 py-1 bg-primary text-white rounded text-xs font-semibold"
            >
              Retry
            </button>
          </div>
        ) : customerList.length === 0 ? (
          <div className="p-8 text-center text-xs text-on-surface-variant">
            No customer accounts found matching criteria.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-surface-container-low text-on-surface-variant uppercase text-[10px] tracking-wider border-b border-outline-variant/20 font-semibold">
                <tr>
                  <th className="p-3.5">Company Name</th>
                  <th className="p-3.5">Primary Contact</th>
                  <th className="p-3.5">Email</th>
                  <th className="p-3.5">Phone</th>
                  <th className="p-3.5">Location</th>
                  <th className="p-3.5">Status</th>
                  <th className="p-3.5 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-surface-container-low">
                {customerList.map((cust) => (
                  <tr
                    key={cust.id}
                    onClick={() => navigate(`/admin/customers/${cust.id}`)}
                    className="hover:bg-surface-container-low/60 cursor-pointer transition-colors"
                  >
                    <td className="p-3.5">
                      <div className="font-bold text-on-surface">{cust.companyName}</div>
                      <div className="text-[10px] text-outline font-mono">ID #{cust.id}</div>
                    </td>
                    <td className="p-3.5 font-medium text-on-surface">{cust.contactName}</td>
                    <td className="p-3.5 font-mono text-on-surface-variant">{cust.email}</td>
                    <td className="p-3.5 font-mono">{cust.phone || '—'}</td>
                    <td className="p-3.5 text-on-surface-variant">{[cust.city, cust.country].filter(Boolean).join(', ') || '—'}</td>
                    <td className="p-3.5">
                      <span className={`px-2 py-0.5 rounded-full text-[10px] font-semibold ${
                        cust.status === 'ACTIVE' ? 'bg-emerald-50 text-emerald-700' :
                        cust.status === 'SUSPENDED' ? 'bg-rose-50 text-rose-700' : 'bg-surface-container text-on-surface-variant'
                      }`}>
                        {cust.status}
                      </span>
                    </td>
                    <td className="p-3.5 text-right" onClick={(e) => e.stopPropagation()}>
                      <div className="flex items-center justify-end gap-1">
                        <button
                          onClick={() => navigate(`/admin/customers/${cust.id}`)}
                          className="p-1 hover:bg-surface-container rounded text-outline hover:text-primary"
                          title="View Profile"
                        >
                          <Eye className="w-3.5 h-3.5" />
                        </button>
                        <button
                          onClick={() => navigate(`/admin/customers/${cust.id}/edit`)}
                          className="p-1 hover:bg-surface-container rounded text-outline hover:text-primary"
                          title="Edit Customer"
                        >
                          <Edit3 className="w-3.5 h-3.5" />
                        </button>
                        <button
                          onClick={(e) => handleToggleStatus(cust, e)}
                          className="p-1 hover:bg-surface-container rounded text-outline hover:text-primary"
                          title={cust.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
                        >
                          <UserCheck className="w-3.5 h-3.5" />
                        </button>
                      </div>
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
