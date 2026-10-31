import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  RefreshCw,
  Search,
  Filter,
  ArrowLeft,
  ArrowRight,
  CheckCircle2,
  AlertCircle,
  Clock,
  ExternalLink,
  ShieldCheck,
  Calendar,
  Layers,
  Sparkles
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { subscriptions } from '../../data/mockData';

export default function CustomerSubscriptions() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const customerCompany = user?.company || 'Nexus Dynamics';
  const customerId = user?.customerId || 'C001';

  // Filter subscriptions strictly for the authenticated customer
  const rawCustomerSubs = subscriptions.filter(
    s => s.customer === customerCompany || s.customerId === customerId
  );

  // If customer has only 1 in general mock, provide clean contextual records so filters are testable
  const mySubscriptions = rawCustomerSubs.length > 0 ? rawCustomerSubs : [
    {
      id: 'SUB-NEX-01',
      customer: customerCompany,
      customerId: customerId,
      plan: 'Enterprise Pro',
      seats: 120,
      usagePercent: 78,
      mrr: 45000,
      status: 'Active',
      startDate: '2024-01-15',
      renewalDate: '2025-01-15',
      cycle: 'Monthly'
    },
    {
      id: 'SUB-NEX-02',
      customer: customerCompany,
      customerId: customerId,
      plan: 'AI Copilot Accelerator Addon',
      seats: 50,
      usagePercent: 62,
      mrr: 12000,
      status: 'Active',
      startDate: '2024-02-01',
      renewalDate: '2025-02-01',
      cycle: 'Monthly'
    }
  ];

  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');

  // Filter logic
  const filteredSubs = mySubscriptions.filter((sub) => {
    const matchesSearch =
      sub.plan.toLowerCase().includes(searchTerm.toLowerCase()) ||
      sub.id.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesStatus =
      statusFilter === 'ALL' || sub.status.toLowerCase() === statusFilter.toLowerCase();

    return matchesSearch && matchesStatus;
  });

  return (
    <div className="space-y-6">
      {/* Top Banner & Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-bold text-on-surface">My Subscription Portfolio</h1>
            <span className="text-[10px] font-mono font-bold bg-primary/10 text-primary border border-primary/20 px-2 py-0.5 rounded-full">
              {customerCompany}
            </span>
          </div>
          <p className="text-xs text-on-surface-variant mt-0.5">
            View active contracted subscription tiers, license provisions, renewal schedules, and terms.
          </p>
        </div>

        <button
          onClick={() => navigate('/customer/dashboard')}
          className="px-3 py-1.5 border border-outline-variant/40 rounded-xl text-xs font-semibold hover:bg-surface-container flex items-center gap-1.5 transition-colors"
        >
          <ArrowLeft className="w-3.5 h-3.5" />
          <span>Back to Overview</span>
        </button>
      </div>

      {/* Search & Filter Bar */}
      <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-2xl shadow-xs flex flex-col md:flex-row items-center justify-between gap-3">
        <div className="relative w-full md:w-80">
          <Search className="w-3.5 h-3.5 absolute left-3 top-2.5 text-outline" />
          <input
            type="text"
            placeholder="Search plan name or subscription ID..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full h-8 pl-9 pr-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest text-on-surface focus:outline-none focus:border-primary-container"
          />
        </div>

        <div className="flex items-center gap-2 w-full md:w-auto">
          <div className="flex items-center gap-1.5 text-xs text-on-surface-variant">
            <Filter className="w-3.5 h-3.5 text-outline" />
            <span>Filter Status:</span>
          </div>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="h-8 px-2.5 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest text-on-surface"
          >
            <option value="ALL">All Statuses</option>
            <option value="Active">Active</option>
            <option value="Paused">Paused</option>
            <option value="Expired">Expired</option>
            <option value="Cancelled">Cancelled</option>
          </select>
        </div>
      </div>

      {/* Subscriptions Grid / Cards */}
      {filteredSubs.length === 0 ? (
        <div className="p-12 bg-surface-container-lowest border border-outline-variant/40 rounded-2xl text-center space-y-3">
          <div className="w-12 h-12 rounded-full bg-surface-container mx-auto flex items-center justify-center text-outline">
            <RefreshCw className="w-6 h-6" />
          </div>
          <h3 className="text-base font-bold text-on-surface">No Subscriptions Found</h3>
          <p className="text-xs text-on-surface-variant max-w-sm mx-auto">
            No subscription records matched your search query or filter selection for {customerCompany}.
          </p>
          <button
            onClick={() => { setSearchTerm(''); setStatusFilter('ALL'); }}
            className="px-4 py-2 bg-primary text-white text-xs font-semibold rounded-lg hover:bg-primary-container"
          >
            Reset Filters
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {filteredSubs.map((sub) => {
            const isActive = sub.status === 'Active';
            return (
              <div
                key={sub.id}
                className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1 hover:shadow-elevation-2 transition-all flex flex-col justify-between space-y-4"
              >
                <div>
                  {/* Top Line */}
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <span className="text-[10px] font-mono text-outline uppercase">{sub.id}</span>
                      <h3 className="text-base font-bold text-on-surface tracking-tight mt-0.5">{sub.plan}</h3>
                    </div>
                    <span
                      className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-semibold ${
                        isActive
                          ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                          : sub.status === 'Paused'
                          ? 'bg-amber-50 text-amber-700 border border-amber-200'
                          : 'bg-surface-container text-on-surface-variant'
                      }`}
                    >
                      {isActive && <CheckCircle2 className="w-3 h-3" />}
                      <span>{sub.status}</span>
                    </span>
                  </div>

                  {/* Pricing & Cycle */}
                  <div className="mt-4 p-3 rounded-xl bg-surface-container-low/60 border border-surface-container flex items-baseline justify-between">
                    <div>
                      <span className="text-[10px] uppercase font-mono text-outline">COMMITTED RECURRING RATE</span>
                      <div className="text-xl font-bold font-mono text-primary mt-0.5">
                        ₹{sub.mrr.toLocaleString('en-IN')}
                        <span className="text-xs text-on-surface-variant font-normal"> / {sub.cycle || 'Monthly'}</span>
                      </div>
                    </div>
                    <div className="text-right">
                      <span className="text-[10px] uppercase font-mono text-outline">TAX RATE</span>
                      <div className="text-xs font-semibold text-on-surface mt-0.5">18% Indian IGST</div>
                    </div>
                  </div>

                  {/* Dates & Provisions */}
                  <div className="grid grid-cols-2 gap-3 mt-4 text-xs">
                    <div>
                      <span className="text-[10px] uppercase font-mono text-outline">CONTRACT START</span>
                      <div className="font-mono text-on-surface mt-0.5">{sub.startDate || '15 Jan 2024'}</div>
                    </div>
                    <div>
                      <span className="text-[10px] uppercase font-mono text-outline">NEXT RENEWAL DUE</span>
                      <div className="font-mono text-primary font-semibold mt-0.5">{sub.renewalDate}</div>
                    </div>
                  </div>

                  {/* Seat Utilization */}
                  {sub.seats && (
                    <div className="mt-4 pt-3 border-t border-surface-container space-y-1.5">
                      <div className="flex justify-between text-[11px] text-on-surface-variant">
                        <span>Seat Provisioning</span>
                        <span className="font-mono font-semibold text-on-surface">{sub.seats} Licenses ({sub.usagePercent || 75}% Used)</span>
                      </div>
                      <div className="w-full bg-surface-container h-1.5 rounded-full overflow-hidden">
                        <div
                          className="bg-primary h-full rounded-full"
                          style={{ width: `${sub.usagePercent || 75}%` }}
                        />
                      </div>
                    </div>
                  )}
                </div>

                {/* Bottom Navigation */}
                <div className="pt-3 border-t border-surface-container flex items-center justify-between">
                  <span className="text-[11px] text-emerald-700 font-semibold flex items-center gap-1">
                    <ShieldCheck className="w-3.5 h-3.5" />
                    <span>AutoPay Mandate Linked</span>
                  </span>
                  <button
                    onClick={() => navigate(`/customer/subscriptions/${sub.id}`)}
                    className="px-3 py-1.5 bg-surface-container hover:bg-surface-container-high text-on-surface text-xs font-bold rounded-lg transition-colors flex items-center gap-1.5"
                  >
                    <span>Agreement Details</span>
                    <ArrowRight className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
