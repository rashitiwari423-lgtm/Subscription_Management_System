import React from 'react';
import { ShieldAlert, ArrowLeft, LogIn } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function UnauthorizedPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  // Send user to their own dashboard based on role
  const dashboardPath =
    user?.role === 'STAFF' ? '/staff/dashboard' :
    user?.role === 'CUSTOMER' ? '/customer/portal' :
    user?.role === 'ADMIN' ? '/admin/dashboard' :
    '/login';

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-surface flex items-center justify-center p-6">
      <div className="max-w-md w-full bg-surface-container-lowest p-8 rounded-2xl border border-outline-variant/40 shadow-elevation-2 text-center space-y-4">
        <div className="w-12 h-12 rounded-full bg-error-container text-error flex items-center justify-center mx-auto">
          <ShieldAlert className="w-6 h-6" />
        </div>
        <h2 className="text-xl font-bold text-on-surface">Access Denied</h2>
        <p className="text-xs text-on-surface-variant">
          Your current account <span className="font-mono font-semibold text-on-surface">{user?.email}</span> (role: <span className="font-semibold">{user?.role || 'unknown'}</span>) does not have permission to access this page.
        </p>
        <div className="flex flex-col sm:flex-row gap-2 justify-center pt-2">
          <Link
            to={dashboardPath}
            className="inline-flex items-center justify-center gap-2 px-4 py-2 bg-primary text-white text-xs font-semibold rounded-lg hover:bg-primary-container transition-all"
          >
            <ArrowLeft className="w-3.5 h-3.5" />
            <span>Go to My Dashboard</span>
          </Link>
          <button
            onClick={handleLogout}
            className="inline-flex items-center justify-center gap-2 px-4 py-2 border border-outline-variant/40 text-on-surface text-xs font-semibold rounded-lg hover:bg-surface-container transition-all"
          >
            <LogIn className="w-3.5 h-3.5" />
            <span>Switch Account</span>
          </button>
        </div>
      </div>
    </div>
  );
}

