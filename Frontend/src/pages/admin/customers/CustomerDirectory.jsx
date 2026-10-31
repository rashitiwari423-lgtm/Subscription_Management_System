import React, { useState, useEffect } from 'react';
import { Search, Plus, Filter, ArrowUpDown, MoreHorizontal, ShieldCheck, Download, Trash2, Edit3, Eye } from 'lucide-react';
import { customerService } from '../../../services/customerService';
import { useNavigate } from 'react-router-dom';

export default function CustomerDirectory() {
  const [customerList, setCustomerList] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const navigate = useNavigate();

  useEffect(() => {
    customerService.getAll().then(setCustomerList);
  }, []);

  const handleDelete = async (id, e) => {
    e.stopPropagation();
    if (window.confirm('Are you sure you want to delete this customer account?')) {
      await customerService.delete(id);
      setCustomerList(prev => prev.filter(c => c.id !== id));
    }
  };

  const filtered = customerList.filter(c => {
    const matchesSearch = c.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                          c.email.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = statusFilter === 'ALL' || c.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  return (
    <div className="space-y-5">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Customer Directory</h1>
          <p className="text-xs text-on-surface-variant">
            Manage institutional client portfolios, billing contacts, and subscription telemetry.
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
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-outline">TOTAL ACCOUNTS</div>
          <div className="text-xl font-bold text-on-surface mt-1">{customerList.length}</div>
        </div>
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-emerald-700">ACTIVE CONTRACTS</div>
          <div className="text-xl font-bold text-emerald-700 mt-1">
            {customerList.filter(c => c.status === 'Active').length}
          </div>
        </div>
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-amber-700">AT RISK CHURN</div>
          <div className="text-xl font-bold text-amber-700 mt-1">
            {customerList.filter(c => c.status === 'At Risk').length}
          </div>
        </div>
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-outline">AGGREGATE MRR</div>
          <div className="text-xl font-bold text-primary mt-1 font-tabular">
            ₹{customerList.reduce((acc, c) => acc + (c.mrr || 0), 0).toLocaleString('en-IN')}
          </div>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl flex flex-col sm:flex-row items-center justify-between gap-3">
        <div className="relative w-full sm:w-80">
          <Search className="w-3.5 h-3.5 absolute left-3 top-2.5 text-outline" />
          <input
            type="text"
            placeholder="Search customers by name, domain, email..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full h-8 pl-9 pr-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest focus:outline-none focus:border-primary-container"
          />
        </div>

        <div className="flex items-center gap-2 w-full sm:w-auto">
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="h-8 px-2.5 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest text-on-surface"
          >
            <option value="ALL">All Statuses</option>
            <option value="Active">Active</option>
            <option value="At Risk">At Risk</option>
            <option value="Churned">Churned</option>
          </select>
        </div>
      </div>

      {/* Customers Data Table */}
      <div className="bg-surface-container-lowest border border-outline-variant/40 rounded-xl shadow-elevation-1 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-surface-container-low text-on-surface-variant uppercase text-[10px] tracking-wider border-b border-outline-variant/20 font-semibold">
              <tr>
                <th className="p-3.5">Customer / Company</th>
                <th className="p-3.5">Industry</th>
                <th className="p-3.5">Tier Plan</th>
                <th className="p-3.5">Seats</th>
                <th className="p-3.5">MRR Contribution</th>
                <th className="p-3.5">Health Status</th>
                <th className="p-3.5 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-container-low">
              {filtered.map((cust) => (
                <tr
                  key={cust.id}
                  onClick={() => navigate(`/admin/customers/${cust.id}`)}
                  className="hover:bg-surface-container-low/60 cursor-pointer transition-colors"
                >
                  <td className="p-3.5">
                    <div className="font-bold text-on-surface">{cust.name}</div>
                    <div className="text-[11px] text-on-surface-variant font-mono">{cust.email}</div>
                  </td>
                  <td className="p-3.5">{cust.industry || 'Technology'}</td>
                  <td className="p-3.5 font-medium">{cust.plan}</td>
                  <td className="p-3.5 font-mono">{cust.seats} seats</td>
                  <td className="p-3.5 font-bold font-mono text-on-surface">
                    ₹{(cust.mrr || 0).toLocaleString('en-IN')}
                  </td>
                  <td className="p-3.5">
                    <span className={`px-2 py-0.5 rounded-full text-[10px] font-semibold ${
                      cust.status === 'Active' ? 'bg-emerald-50 text-emerald-700' :
                      cust.status === 'At Risk' ? 'bg-amber-50 text-amber-700' : 'bg-rose-50 text-rose-700'
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
                        onClick={(e) => handleDelete(cust.id, e)}
                        className="p-1 hover:bg-error-container/40 rounded text-outline hover:text-error"
                        title="Delete Account"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

