import React, { useState } from 'react';
import { Search, Bell, Shield, Terminal, ArrowUpRight, CheckCircle2, ChevronDown } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';

export default function TopBar({ title, subtitle }) {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [showNotifications, setShowNotifications] = useState(false);

  return (
    <header className="sticky top-0 z-30 h-14 bg-surface/85 backdrop-blur-md border-b border-outline-variant/30 px-6 flex items-center justify-between">
      {/* Title & Breadcrumbs */}
      <div className="flex items-center gap-3">
        <div>
          <h1 className="text-sm font-bold text-on-surface tracking-tight flex items-center gap-2">
            <span>{title || 'Subscription Intelligence Hub'}</span>
            <span className="text-outline-variant font-normal">/</span>
            <span className="text-xs text-primary font-mono font-medium">{subtitle || 'Live Production Cluster'}</span>
          </h1>
        </div>
      </div>

      {/* Global Actions */}
      <div className="flex items-center gap-3">
        {/* Search */}
        <div className="relative hidden md:block">
          <Search className="w-3.5 h-3.5 absolute left-2.5 top-2.5 text-outline" />
          <input
            type="text"
            placeholder="Search accounts, invoices, tax records (⌘K)..."
            className="h-8 pl-8 pr-12 rounded-lg bg-surface-container-lowest border border-outline-variant/40 text-xs text-on-surface placeholder:text-outline w-64 focus:outline-none focus:border-primary-container focus:ring-1 focus:ring-primary-container"
          />
          <kbd className="absolute right-2 top-2 text-[10px] bg-surface-container px-1 py-0.2 rounded text-outline font-mono">⌘K</kbd>
        </div>

        {/* Currency & Telemetry badge */}
        <div className="hidden lg:flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-emerald-50 border border-emerald-200/60 text-emerald-700 text-xs font-medium">
          <span className="w-1.5 h-1.5 rounded-full bg-emerald-600"></span>
          <span>INR Gateway: Connected</span>
        </div>

        {/* Notification Bell */}
        <div className="relative">
          <button
            onClick={() => setShowNotifications(!showNotifications)}
            className="w-8 h-8 rounded-lg border border-outline-variant/30 flex items-center justify-center text-on-surface-variant hover:bg-surface-container relative"
          >
            <Bell className="w-4 h-4" />
            <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-primary ring-2 ring-white"></span>
          </button>

          {showNotifications && (
            <div className="absolute right-0 mt-2 w-80 bg-surface-container-lowest border border-outline-variant/40 rounded-xl shadow-elevation-3 p-3 z-50">
              <div className="flex items-center justify-between pb-2 border-b border-surface-container">
                <span className="text-xs font-bold text-on-surface">Intelligence Alerts</span>
                <span className="text-[10px] text-primary cursor-pointer hover:underline" onClick={() => navigate('/admin/notifications')}>View All</span>
              </div>
              <div className="space-y-2 mt-2">
                <div className="p-2 rounded-lg bg-surface-container-low text-xs">
                  <div className="font-semibold text-primary">High Churn Risk: Vertex Labs</div>
                  <div className="text-[11px] text-on-surface-variant">Seat usage dropped 45%. Automated engagement sequence recommended.</div>
                </div>
                <div className="p-2 rounded-lg bg-surface-container-low text-xs">
                  <div className="font-semibold text-emerald-700">Dunning Recovery: ₹32,000</div>
                  <div className="text-[11px] text-on-surface-variant">Orion Fintech invoice #INV-2024-006 successfully collected.</div>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Quick Help / Docs */}
        <button
          onClick={() => navigate('/admin/developer')}
          className="hidden sm:flex items-center gap-1 text-xs text-on-surface-variant border border-outline-variant/40 px-2.5 py-1.5 rounded-lg hover:bg-surface-container"
        >
          <Code2 className="w-3.5 h-3.5" />
          <span>API Docs</span>
        </button>
      </div>
    </header>
  );
}

function Code2(props) {
  return (
    <svg {...props} xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="m18 16 4-4-4-4"/><path d="m6 8-4 4 4 4"/><path d="m14.5 4-5 16"/>
    </svg>
  );
}

