import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth, DEMO_USERS } from '../../context/AuthContext';
import { ShieldCheck, ArrowRight, Lock, Mail, Key, CheckCircle, Shield, Award, UserCheck, Users, HelpCircle } from 'lucide-react';

export default function LoginPage() {
  const [selectedRole, setSelectedRole] = useState('ADMIN');
  const [email, setEmail] = useState(DEMO_USERS.ADMIN.email);
  const [password, setPassword] = useState('password123');
  const [rememberDevice, setRememberDevice] = useState(true);
  const [loading, setLoading] = useState(false);
  const [loginError, setLoginError] = useState('');
  const { loginWithCredentials, loginByRole } = useAuth();
  const navigate = useNavigate();

  const handleRoleSelect = (roleKey) => {
    setSelectedRole(roleKey);
    const demo = DEMO_USERS[roleKey];
    if (demo) {
      setEmail(demo.email);
      setPassword(demo.password);
    }
    setLoginError('');
  };

  const handleLogin = (e) => {
    e.preventDefault();
    setLoading(true);
    setLoginError('');

    setTimeout(() => {
      const result = loginWithCredentials(email, password);
      setLoading(false);

      if (result.success) {
        const role = result.user.role;
        if (role === 'STAFF') {
          navigate('/staff/dashboard');
        } else if (role === 'CUSTOMER') {
          navigate('/customer/dashboard');
        } else {
          navigate('/admin/dashboard');
        }
      } else {
        setLoginError(result.error || 'Invalid credentials. Please try again.');
      }
    }, 200);
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
      {/* Sign-in Form Card */}
      <div className="lg:col-span-7 bg-surface-container-lowest p-8 rounded-2xl border border-outline-variant/40 shadow-elevation-2">
        <div className="mb-6">
          <div className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-primary-fixed text-on-primary-fixed text-xs font-semibold uppercase tracking-wider mb-3">
            <ShieldCheck className="w-3.5 h-3.5" />
            <span>Multi-Role Authentication</span>
          </div>
          <h2 className="text-2xl font-bold text-on-surface tracking-tight">Enterprise Sign In</h2>
          <p className="text-xs text-on-surface-variant mt-1">
            Select a verified demo persona below or enter your corporate credentials.
          </p>
        </div>

        {/* Demo Fast Access Section */}
        <div className="mb-6 p-4 rounded-xl bg-surface-container-low border border-surface-container">
          <div className="flex items-center justify-between mb-2.5">
            <span className="text-xs font-bold text-on-surface flex items-center gap-1.5">
              <Key className="w-3.5 h-3.5 text-primary" />
              <span>Instant Demo Persona Selector:</span>
            </span>
            <span className="text-[10px] text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-full font-mono font-bold">
              ONE-CLICK PRELOAD
            </span>
          </div>
          <div className="grid grid-cols-3 gap-2.5">
            {[
              { id: 'ADMIN', label: 'Admin (CFO)', desc: 'Full System Access', icon: Shield, email: DEMO_USERS.ADMIN.email },
              { id: 'STAFF', label: 'Staff (Ops)', desc: 'Customer & Invoicing Queues', icon: UserCheck, email: DEMO_USERS.STAFF.email },
              { id: 'CUSTOMER', label: 'Customer', desc: 'Self-Serve Billing Portal', icon: Users, email: DEMO_USERS.CUSTOMER.email }
            ].map((p) => {
              const Icon = p.icon;
              return (
                <button
                  type="button"
                  key={p.id}
                  onClick={() => handleRoleSelect(p.id)}
                  className={`p-2.5 rounded-xl text-left border transition-all ${
                    selectedRole === p.id
                      ? 'border-primary bg-primary/10 text-primary ring-2 ring-primary/20 shadow-xs'
                      : 'border-outline-variant/40 bg-surface-container-lowest text-on-surface hover:bg-surface-container'
                  }`}
                >
                  <div className="flex items-center justify-between mb-1">
                    <span className="text-xs font-bold">{p.label}</span>
                    <Icon className="w-3.5 h-3.5 opacity-70" />
                  </div>
                  <div className="text-[10px] text-on-surface-variant line-clamp-1">{p.desc}</div>
                  <div className="text-[9px] font-mono text-outline mt-1 truncate">{p.email}</div>
                </button>
              );
            })}
          </div>
        </div>

        <form onSubmit={handleLogin} className="space-y-4">
          <div>
            <label className="block text-xs font-medium text-on-surface mb-1">Corporate Email Address</label>
            <div className="relative">
              <Mail className="w-4 h-4 absolute left-3 top-3 text-outline" />
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full h-10 pl-9 pr-3 rounded-lg border border-outline-variant/60 bg-surface-container-lowest text-sm text-on-surface focus:outline-none focus:border-primary-container focus:ring-1 focus:ring-primary-container font-mono"
                placeholder="name@subscriptiq.com"
              />
            </div>
          </div>

          <div>
            <div className="flex items-center justify-between mb-1">
              <label className="text-xs font-medium text-on-surface">Password</label>
              <a href="/forgot-password" className="text-xs text-primary hover:underline">Forgot password?</a>
            </div>
            <div className="relative">
              <Lock className="w-4 h-4 absolute left-3 top-3 text-outline" />
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full h-10 pl-9 pr-3 rounded-lg border border-outline-variant/60 bg-surface-container-lowest text-sm text-on-surface focus:outline-none focus:border-primary-container focus:ring-1 focus:ring-primary-container font-mono"
              />
            </div>
          </div>

          <div className="flex items-center justify-between text-xs text-on-surface-variant pt-1">
            <label className="flex items-center gap-2 cursor-pointer select-none">
              <input
                type="checkbox"
                checked={rememberDevice}
                onChange={(e) => setRememberDevice(e.target.checked)}
                className="rounded accent-primary w-3.5 h-3.5"
              />
              <span>Remember hardware credentials (30 days)</span>
            </label>
            <span className="flex items-center gap-1 font-mono text-[11px] text-tertiary">
              <Key className="w-3.5 h-3.5 text-primary" />
              <span>Password: password123</span>
            </span>
          </div>

          {loginError && (
            <div className="p-3 rounded-lg bg-rose-50 border border-rose-200 text-xs text-rose-700 font-medium flex items-center gap-2">
              <span>⚠</span>
              <span>{loginError}</span>
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full h-11 bg-primary hover:bg-primary-container text-white font-semibold text-sm rounded-lg flex items-center justify-center gap-2 shadow-elevation-1 transition-all active:scale-[0.99] mt-2 disabled:opacity-70"
          >
            <span>{loading ? 'Authenticating...' : `Sign In as ${selectedRole}`}</span>
            <ArrowRight className="w-4 h-4" />
          </button>
        </form>

        <div className="mt-6 pt-4 border-t border-surface-container flex items-center justify-between text-[11px] text-tertiary">
          <span className="flex items-center gap-1"><Shield className="w-3.5 h-3.5 text-primary" /> SOC2 Type II</span>
          <span className="flex items-center gap-1"><CheckCircle className="w-3.5 h-3.5 text-primary" /> 256-Bit TLS</span>
          <span className="flex items-center gap-1"><Award className="w-3.5 h-3.5 text-primary" /> Indian GST Certified</span>
        </div>
      </div>

      {/* Trust & Telemetry Panel (Obsidian Kinetic Design) */}
      <div className="lg:col-span-5 flex flex-col gap-4">
        <div className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1">
          <div className="flex items-center justify-between mb-4">
            <span className="text-[11px] uppercase font-bold tracking-wider text-primary">Role-Based Workspaces</span>
            <span className="flex items-center gap-1.5 text-[11px] font-mono text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-full border border-emerald-200">
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-600 animate-pulse"></span>
              ALL 3 ROLES LIVE
            </span>
          </div>

          <div className="space-y-3 text-xs">
            <div className="p-3 rounded-xl bg-surface-container-low border border-surface-container">
              <div className="font-bold text-on-surface">Admin Workspace (`/admin`)</div>
              <p className="text-[11px] text-on-surface-variant mt-0.5">
                Executive dashboard, staff provisioning, plan management, financial ledger, and AI Command Suite.
              </p>
            </div>
            <div className="p-3 rounded-xl bg-surface-container-low border border-surface-container">
              <div className="font-bold text-on-surface">Staff Operational Queue (`/staff`)</div>
              <p className="text-[11px] text-on-surface-variant mt-0.5">
                Day-to-day customer portfolio handling, deal desk CPQ proposals, renewal follow-ups, and invoice collections.
              </p>
            </div>
            <div className="p-3 rounded-xl bg-surface-container-low border border-surface-container">
              <div className="font-bold text-on-surface">Customer Self-Service Portal (`/customer`)</div>
              <p className="text-[11px] text-on-surface-variant mt-0.5">
                Seat utilization monitor, self-serve tier upgrade requests, auto-debit management, and GST tax invoice receipts.
              </p>
            </div>
          </div>
        </div>

        <div className="p-4 rounded-xl bg-gradient-to-br from-primary/10 via-secondary/5 to-surface border border-primary/20 text-xs">
          <div className="font-semibold text-primary flex items-center gap-1.5">
            <span>New Organization Deployment?</span>
          </div>
          <p className="text-on-surface-variant text-[11px] mt-1">
            Need to initialize a new multi-entity tenancy with custom GSTIN?
          </p>
          <a href="/onboarding" className="inline-flex items-center gap-1 text-primary font-bold mt-2 hover:underline">
            <span>Launch Workspace Onboarding Wizard</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </a>
        </div>
      </div>
    </div>
  );
}
