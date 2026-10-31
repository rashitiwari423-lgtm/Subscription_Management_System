import React, { useState } from 'react';
import { NavLink, useNavigate, useLocation } from 'react-router-dom';
import {
  LayoutDashboard,
  Users,
  UserCheck,
  Package,
  Layers,
  FileSpreadsheet,
  RefreshCw,
  Receipt,
  CreditCard,
  Percent,
  Landmark,
  Sparkles,
  TrendingDown,
  LineChart,
  Bell,
  Settings,
  ShieldCheck,
  Webhook,
  Code2,
  CalendarCheck,
  AlertTriangle,
  LogOut,
  ChevronDown,
  ChevronRight,
  Shield,
  Briefcase,
  FolderOpen
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

export default function Sidebar() {
  const { user, logout, switchRole } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  // Track expanded menu sections for a clean, non-overwhelming UI
  const [expandedSections, setExpandedSections] = useState({
    overview: true,
    customers: true,
    catalog: true,
    sales: true,
    finance: true,
    intelligence: true,
    system: false
  });

  const toggleSection = (key) => {
    setExpandedSections(prev => ({ ...prev, [key]: !prev[key] }));
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const navGroups = [
    {
      id: 'overview',
      title: 'Overview',
      items: [
        { label: 'Executive Dashboard', to: '/admin/dashboard', icon: LayoutDashboard }
      ]
    },
    {
      id: 'customers',
      title: 'Customers & Team',
      items: [
        { label: 'Customer Directory', to: '/admin/customers', icon: Users },
        { label: 'Staff & Team RBAC', to: '/admin/staff', icon: UserCheck, adminOnly: true },
      ]
    },
    {
      id: 'catalog',
      title: 'Catalog & Pricing',
      items: [
        { label: 'Products & SKUs', to: '/admin/products', icon: Package },
        { label: 'Plans & Cadence', to: '/admin/plans', icon: Layers },
        { label: 'Promotions & Discounts', to: '/admin/discounts', icon: Percent },
        { label: 'Taxes & Indian GST', to: '/admin/taxes', icon: Landmark },
      ]
    },
    {
      id: 'sales',
      title: 'Sales & Deal Desk',
      items: [
        { label: 'Quotations & CPQ', to: '/admin/quotations', icon: FileSpreadsheet },
        { label: 'Subscriptions Lifecycle', to: '/admin/subscriptions', icon: RefreshCw },
      ]
    },
    {
      id: 'finance',
      title: 'Finance & Invoicing',
      items: [
        { label: 'Invoices & Billing', to: '/admin/invoices', icon: Receipt },
        { label: 'Payments & Settlement', to: '/admin/payments', icon: CreditCard },
        { label: 'Dunning & Recovery', to: '/admin/dunning', icon: AlertTriangle, highlight: true },
        { label: 'Batch Billing Runs', to: '/admin/billing-runs', icon: CalendarCheck },
      ]
    },
    {
      id: 'intelligence',
      title: 'Intelligence Suite',
      items: [
        { label: 'AI Command Center', to: '/admin/ai-command-center', icon: Sparkles, badge: 'AI' },
        { label: 'AI Cost Optimizer', to: '/admin/cost-optimizer', icon: TrendingDown },
        { label: 'ARR Cohort Analytics', to: '/admin/analytics', icon: LineChart },
      ]
    },
    {
      id: 'system',
      title: 'System & Governance',
      items: [
        { label: 'Notifications', to: '/admin/notifications', icon: Bell },
        { label: 'Organization Settings', to: '/admin/settings', icon: Settings },
        { label: 'Audit Logs (SOC2)', to: '/admin/audit-logs', icon: ShieldCheck },
        { label: 'Payment Gateways', to: '/admin/integrations', icon: Webhook },
        { label: 'Developer Webhooks', to: '/admin/developer', icon: Code2 },
      ]
    }
  ];

  return (
    <aside className="fixed left-0 top-0 h-screen w-64 bg-surface-container-lowest border-r border-outline-variant/30 z-40 flex flex-col justify-between overflow-hidden shadow-elevation-1">
      <div className="flex flex-col h-full">
        {/* Brand Header */}
        <div className="p-4 border-b border-surface-container">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-xl bg-primary flex items-center justify-center shadow-ai-glow text-white font-bold text-base">
              S
            </div>
            <div>
              <div className="font-bold text-base text-on-surface tracking-tight flex items-center">
                Subscript<span className="text-primary">IQ</span>
              </div>
              <div className="text-[9px] uppercase font-mono tracking-wider text-on-surface-variant font-medium">
                FINANCIAL AI ENGINE
              </div>
            </div>
          </div>
          <div className="mt-2.5 flex items-center justify-between">
            <span className="text-[11px] font-semibold bg-primary/10 text-primary px-2.5 py-0.5 rounded-full border border-primary/20">
              {user?.role === 'ADMIN' ? 'Admin Ops Suite' : user?.role === 'STAFF' ? 'Staff Workspace' : 'Billing Console'}
            </span>
            <div className="flex items-center gap-1">
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse"></span>
              <span className="text-[10px] text-on-surface-variant font-mono">SOC2 LIVE</span>
            </div>
          </div>
        </div>

        {/* Simplified Collapsible Navigation Groups */}
        <div className="flex-1 overflow-y-auto px-3 py-3 space-y-3 text-sm">
          {navGroups.map((group) => {
            const visibleItems = group.items.filter(item => !item.adminOnly || user?.role === 'ADMIN');
            if (visibleItems.length === 0) return null;

            const isExpanded = expandedSections[group.id] !== false;

            return (
              <div key={group.id} className="space-y-0.5">
                <button
                  type="button"
                  onClick={() => toggleSection(group.id)}
                  className="w-full flex items-center justify-between px-2 py-1 text-[11px] font-bold uppercase tracking-wider text-outline hover:text-on-surface transition-colors"
                >
                  <span>{group.title}</span>
                  {isExpanded ? (
                    <ChevronDown className="w-3.5 h-3.5 opacity-60" />
                  ) : (
                    <ChevronRight className="w-3.5 h-3.5 opacity-60" />
                  )}
                </button>

                {isExpanded && (
                  <div className="space-y-0.5 pt-0.5">
                    {visibleItems.map((item, itemIdx) => {
                      const Icon = item.icon;
                      return (
                        <NavLink
                          key={itemIdx}
                          to={item.to}
                          className={({ isActive }) =>
                            `flex items-center justify-between px-2.5 py-1.5 rounded-lg transition-all duration-150 group ${
                              isActive
                                ? 'bg-primary text-white font-medium shadow-xs'
                                : item.highlight
                                ? 'text-rose-700 bg-rose-50/60 hover:bg-rose-100/70 font-medium'
                                : 'text-on-surface-variant hover:bg-surface-container hover:text-on-surface'
                            }`
                          }
                        >
                          {({ isActive }) => (
                            <>
                              <div className="flex items-center gap-2.5 truncate">
                                <Icon className={`w-4 h-4 flex-shrink-0 transition-colors ${
                                  isActive
                                    ? 'text-white'
                                    : item.highlight
                                    ? 'text-rose-600'
                                    : 'text-on-surface-variant group-hover:text-primary'
                                }`} />
                                <span className="text-xs truncate">{item.label}</span>
                              </div>
                              {item.badge && (
                                <span className={`text-[9px] font-bold px-1.5 py-0.2 rounded-full ${
                                  isActive ? 'bg-white text-primary' : 'bg-secondary-fixed text-on-secondary-fixed'
                                }`}>
                                  {item.badge}
                                </span>
                              )}
                            </>
                          )}
                        </NavLink>
                      );
                    })}
                  </div>
                )}
              </div>
            );
          })}
        </div>

        {/* User Profile & Interactive Demo Role Switcher */}
        <div className="p-3 border-t border-surface-container bg-surface-container-low/50">
          <div className="flex items-center justify-between mb-2">
            <div className="flex items-center gap-2 overflow-hidden">
              <img
                src={user?.avatar || "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&h=100&fit=crop"}
                alt={user?.name || "User"}
                className="w-7 h-7 rounded-full object-cover border border-outline-variant"
              />
              <div className="truncate">
                <div className="text-xs font-semibold text-on-surface truncate leading-tight">
                  {user?.name}
                </div>
                <div className="text-[10px] text-on-surface-variant truncate">
                  {user?.title || user?.role}
                </div>
              </div>
            </div>
            <button
              onClick={handleLogout}
              title="Sign Out"
              className="p-1 text-on-surface-variant hover:text-error hover:bg-error-container/40 rounded transition-colors"
            >
              <LogOut className="w-3.5 h-3.5" />
            </button>
          </div>

          {/* Quick Role Tester */}
          <div className="pt-2 border-t border-outline-variant/20 flex items-center justify-between text-[10px]">
            <span className="text-outline font-mono">ROLE:</span>
            <div className="flex gap-1">
              {[
                { r: 'ADMIN', path: '/admin/dashboard' },
                { r: 'STAFF', path: '/staff/dashboard' },
                { r: 'CUSTOMER', path: '/customer/dashboard' }
              ].map(({ r, path }) => (
                <button
                  key={r}
                  onClick={() => {
                    switchRole(r);
                    navigate(path);
                  }}
                  className={`px-1.5 py-0.5 rounded text-[9px] font-semibold transition-all ${
                    user?.role === r
                      ? 'bg-primary text-white shadow-xs'
                      : 'bg-surface-container text-on-surface-variant hover:bg-surface-container-high'
                  }`}
                >
                  {r}
                </button>
              ))}
            </div>
          </div>
        </div>
      </div>
    </aside>
  );
}
