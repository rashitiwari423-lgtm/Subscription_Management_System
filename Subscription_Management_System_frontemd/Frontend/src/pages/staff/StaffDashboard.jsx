import React, { useState, useEffect } from 'react';
import {
  Users, FileSpreadsheet, RefreshCw, Receipt,
  AlertTriangle, BarChart2, Zap
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { customerService, quotationService, subscriptionService, invoiceService } from '../../services/appServices';

export default function StaffDashboard() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [counts, setCounts] = useState({ customers: 12, quotations: 7, subscriptions: 24, overdueInvoices: 3 });

  useEffect(() => {
    const loadCounts = async () => {
      try {
        const [custs, quots, subs, invs] = await Promise.all([
          customerService.getAll(),
          quotationService.getAll(),
          subscriptionService.getAll(),
          invoiceService.getAll()
        ]);
        const custList = Array.isArray(custs) ? custs : (custs?.content || []);
        const quotList = Array.isArray(quots) ? quots : (quots?.content || []);
        const subList = Array.isArray(subs) ? subs : (subs?.content || []);
        const invList = Array.isArray(invs) ? invs : (invs?.content || []);
        const overdue = invList.filter(inv => inv.status === 'OVERDUE' || inv.status === 'Overdue').length;
        setCounts({
          customers: custList.length,
          quotations: quotList.length,
          subscriptions: subList.filter(s => s.status === 'ACTIVE' || s.status === 'Active').length,
          overdueInvoices: overdue
        });
      } catch (err) {
        console.error('Failed to load staff counts:', err);
      }
    };
    loadCounts();
  }, []);

  const urgentActions = [
    {
      id: 1,
      title: 'Vertex Labs — Churn Warning',
      desc: 'Seat usage dropped 45%. Propose a downgrade or engagement plan.',
      severity: 'high',
      action: 'Draft Quote',
      route: '/staff/quotations'
    },
    {
      id: 2,
      title: 'Orion Fintech — Renewal in 14 Days',
      desc: '₹22,000/month commitment. Confirm renewal intent with account contact.',
      severity: 'medium',
      action: 'Review',
      route: '/staff/subscriptions'
    },
    {
      id: 3,
      title: 'DataPulse Analytics — Invoice Overdue',
      desc: 'INV-2024-009 overdue by 31 days. ₹15,000 outstanding.',
      severity: 'high',
      action: 'View Invoice',
      route: '/staff/invoices'
    }
  ];

  const quickNav = [
    { label: 'Customer Directory', desc: 'View all accounts', icon: Users, route: '/staff/customers' },
    { label: 'Quotations', desc: 'Deal pipeline & CPQ', icon: FileSpreadsheet, route: '/staff/quotations' },
    { label: 'Subscriptions', desc: 'Lifecycle states', icon: RefreshCw, route: '/staff/subscriptions' },
    { label: 'Invoices', desc: 'Billing & collections', icon: Receipt, route: '/staff/invoices' },
  ];

  const kpis = [
    { label: 'Assigned Accounts', value: `${counts.customers}`, sub: 'Active portfolios', color: 'text-on-surface', icon: Users },
    { label: 'Deal Desk Quotes', value: `${counts.quotations}`, sub: 'Pending proposals', color: 'text-primary', icon: FileSpreadsheet },
    { label: 'Active Subscriptions', value: `${counts.subscriptions}`, sub: 'Live contracts', color: 'text-emerald-700', icon: RefreshCw },
    { label: 'Overdue Invoices', value: `${counts.overdueInvoices}`, sub: 'Require follow-up', color: 'text-rose-700', icon: AlertTriangle },
  ];

  const greeting = () => {
    const h = new Date().getHours();
    if (h < 12) return 'morning';
    if (h < 17) return 'afternoon';
    return 'evening';
  };

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="bg-gradient-to-r from-surface-container-low via-surface to-primary/5 p-6 rounded-2xl border border-outline-variant/30 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div>
          <span className="text-[10px] font-mono uppercase bg-primary/10 text-primary font-bold px-2 py-0.5 rounded-full">
            STAFF OPERATIONS CONSOLE
          </span>
          <h1 className="text-xl font-bold text-on-surface mt-2">
            Good {greeting()}, {user?.name?.split(' ')[0] || 'Agent'} 👋
          </h1>
          <p className="text-xs text-on-surface-variant mt-0.5">
            {user?.title || 'Senior Account Executive'} · {user?.department || 'Customer Success & Ops'} · 99.2% SLA
          </p>
        </div>
        <div className="flex items-center gap-2">
          <span className="flex items-center gap-1.5 text-[11px] font-mono text-emerald-700 bg-emerald-50 px-3 py-1.5 rounded-lg border border-emerald-200">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-600 animate-pulse" />
            Shift: Active
          </span>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        {kpis.map((kpi) => {
          const Icon = kpi.icon;
          return (
            <div key={kpi.label} className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
              <div className="flex items-center justify-between mb-1">
                <span className="text-[10px] uppercase font-mono text-outline font-bold">{kpi.label}</span>
                <Icon className={`w-3.5 h-3.5 ${kpi.color} opacity-60`} />
              </div>
              <div className={`text-xl font-bold ${kpi.color} mt-0.5`}>{kpi.value}</div>
              <div className="text-[10px] text-on-surface-variant mt-0.5">{kpi.sub}</div>
            </div>
          );
        })}
      </div>

      {/* Main Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Urgent Actions */}
        <div className="bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-3">
          <div className="flex items-center justify-between border-b border-surface-container pb-3">
            <h2 className="text-xs font-bold uppercase tracking-wider text-on-surface flex items-center gap-1.5">
              <Zap className="w-3.5 h-3.5 text-amber-600" />
              Urgent Actions
            </h2>
            <span className="text-[10px] font-mono text-rose-700 font-bold bg-rose-50 px-2 py-0.5 rounded-full border border-rose-200">
              SLA WATCH
            </span>
          </div>
          <div className="space-y-2 text-xs">
            {urgentActions.map((item) => (
              <div
                key={item.id}
                className={`p-3 rounded-xl flex items-center justify-between gap-3 ${
                  item.severity === 'high'
                    ? 'bg-rose-50/60 border border-rose-100'
                    : 'bg-surface-container-low border border-surface-container'
                }`}
              >
                <div className="flex-1 min-w-0">
                  <div className="font-bold text-on-surface truncate">{item.title}</div>
                  <div className="text-[11px] text-on-surface-variant mt-0.5 line-clamp-1">{item.desc}</div>
                </div>
                <button
                  onClick={() => navigate(item.route)}
                  className="px-2.5 py-1 bg-primary text-white rounded text-[11px] font-semibold whitespace-nowrap flex-shrink-0"
                >
                  {item.action}
                </button>
              </div>
            ))}
          </div>
        </div>

        {/* Quick Navigation */}
        <div className="bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-3">
          <div className="flex items-center justify-between border-b border-surface-container pb-3">
            <h2 className="text-xs font-bold uppercase tracking-wider text-on-surface">Quick Navigation</h2>
          </div>
          <div className="grid grid-cols-2 gap-2 text-xs">
            {quickNav.map((item) => {
              const Icon = item.icon;
              return (
                <button
                  key={item.label}
                  onClick={() => navigate(item.route)}
                  className="p-3 rounded-xl border border-outline-variant/40 hover:bg-surface-container text-left space-y-1.5 transition-colors group"
                >
                  <Icon className="w-4 h-4 text-primary" />
                  <div className="font-bold text-on-surface">{item.label}</div>
                  <div className="text-[10px] text-on-surface-variant">{item.desc}</div>
                </button>
              );
            })}
          </div>
        </div>
      </div>

      {/* Today's Summary */}
      <div className="bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant/40 shadow-elevation-1">
        <h2 className="text-xs font-bold uppercase tracking-wider text-on-surface border-b border-surface-container pb-3 mb-3 flex items-center gap-1.5">
          <BarChart2 className="w-3.5 h-3.5 text-primary" />
          Today's Summary
        </h2>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 text-xs">
          <div className="p-3 rounded-xl bg-emerald-50 border border-emerald-100">
            <div className="font-bold text-emerald-700">3 Renewals This Week</div>
            <div className="text-[11px] text-emerald-600 mt-0.5">₹89,000 committed ARR at risk</div>
          </div>
          <div className="p-3 rounded-xl bg-primary/5 border border-primary/10">
            <div className="font-bold text-primary">2 Quotes Awaiting Approval</div>
            <div className="text-[11px] text-on-surface-variant mt-0.5">Orion Fintech, ByteForge</div>
          </div>
          <div className="p-3 rounded-xl bg-amber-50 border border-amber-100">
            <div className="font-bold text-amber-700">₹59,000 Outstanding</div>
            <div className="text-[11px] text-amber-600 mt-0.5">Across {counts.overdueInvoices} overdue accounts</div>
          </div>
        </div>
      </div>
    </div>
  );
}
