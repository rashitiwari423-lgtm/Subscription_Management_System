import React from 'react';
import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { LayoutDashboard, Receipt, RefreshCw, User, LogOut } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export default function CustomerLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-surface flex flex-col">
      {/* Customer Header */}
      <header className="h-16 bg-surface-container-lowest border-b border-outline-variant/30 px-8 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-lg bg-primary text-white font-bold flex items-center justify-center">
            S
          </div>
          <div>
            <div className="font-bold text-sm text-on-surface">SubscriptIQ Customer Portal</div>
            <div className="text-[10px] text-outline font-mono">
              TENANT: {user?.company || 'Nexus Dynamics'} · {user?.name || 'Rahul Gupta'}
            </div>
          </div>
        </div>

        <nav className="flex items-center gap-1.5 sm:gap-2">
          <NavLink
            to="/customer/dashboard"
            className={({ isActive }) =>
              `flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                isActive ? 'bg-primary text-white shadow-xs' : 'text-on-surface-variant hover:bg-surface-container'
              }`
            }
          >
            <LayoutDashboard className="w-4 h-4" />
            <span>Dashboard</span>
          </NavLink>

          <NavLink
            to="/customer/subscriptions"
            className={({ isActive }) =>
              `flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                isActive ? 'bg-primary text-white shadow-xs' : 'text-on-surface-variant hover:bg-surface-container'
              }`
            }
          >
            <RefreshCw className="w-4 h-4" />
            <span>My Subscriptions</span>
          </NavLink>

          <NavLink
            to="/customer/invoices"
            className={({ isActive }) =>
              `flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                isActive ? 'bg-primary text-white shadow-xs' : 'text-on-surface-variant hover:bg-surface-container'
              }`
            }
          >
            <Receipt className="w-4 h-4" />
            <span>Invoices & Payments</span>
          </NavLink>

          <NavLink
            to="/customer/profile"
            className={({ isActive }) =>
              `flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                isActive ? 'bg-primary text-white shadow-xs' : 'text-on-surface-variant hover:bg-surface-container'
              }`
            }
          >
            <User className="w-4 h-4" />
            <span>My Profile</span>
          </NavLink>
        </nav>

        <div className="flex items-center gap-3">
          <button
            onClick={() => { logout(); navigate('/'); }}
            className="flex items-center gap-1.5 text-xs text-on-surface-variant hover:text-error px-2.5 py-1.5 rounded-lg hover:bg-error-container/30 transition-colors"
          >
            <LogOut className="w-3.5 h-3.5" />
            <span>Exit Portal</span>
          </button>
        </div>
      </header>

      {/* Main portal body */}
      <main className="flex-1 max-w-7xl w-full mx-auto p-8">
        <Outlet />
      </main>
    </div>
  );
}

