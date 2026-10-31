import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { ShieldCheck, Mail, Lock, Building, ArrowRight, User } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

export default function RegisterPage() {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    company: '',
    password: '',
    confirmPassword: '',
    agreeTerms: true
  });
  const [error, setError] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = (e) => {
    e.preventDefault();
    setError('');

    // Business rule: Password more than 8 chars, uppercase, lowercase, special character
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]).{9,}$/;
    if (!passwordRegex.test(formData.password)) {
      setError('Password must be at least 9 characters long and contain uppercase, lowercase, and a special character.');
      return;
    }

    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    login('CUSTOMER', formData.email);
    navigate('/customer/dashboard');
  };

  return (
    <div className="max-w-xl mx-auto bg-surface-container-lowest p-8 rounded-2xl border border-outline-variant/40 shadow-elevation-2">
      <div className="mb-6">
        <div className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-primary-fixed text-on-primary-fixed text-xs font-semibold uppercase tracking-wider mb-3">
          <ShieldCheck className="w-3.5 h-3.5" />
          <span>Tenant Registration</span>
        </div>
        <h2 className="text-2xl font-bold text-on-surface tracking-tight">Create Workspace Account</h2>
        <p className="text-xs text-on-surface-variant mt-1">
          Deploy an autonomous subscription management instance for your organization.
        </p>
      </div>

      {error && (
        <div className="mb-4 p-3 rounded-xl bg-error-container/40 border border-error/30 text-xs text-error font-medium">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-xs font-medium text-on-surface mb-1">Full Name *</label>
          <div className="relative">
            <User className="w-4 h-4 absolute left-3 top-2.5 text-outline" />
            <input
              type="text"
              required
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className="w-full h-9 pl-9 pr-3 rounded-lg border border-outline-variant/60 bg-surface-container-lowest text-xs text-on-surface focus:outline-none focus:border-primary-container"
              placeholder="e.g. Vikram Malhotra"
            />
          </div>
        </div>

        <div>
          <label className="block text-xs font-medium text-on-surface mb-1">Corporate Email Address (Unique ID) *</label>
          <div className="relative">
            <Mail className="w-4 h-4 absolute left-3 top-2.5 text-outline" />
            <input
              type="email"
              required
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              className="w-full h-9 pl-9 pr-3 rounded-lg border border-outline-variant/60 bg-surface-container-lowest text-xs font-mono text-on-surface focus:outline-none focus:border-primary-container"
              placeholder="name@company.com"
            />
          </div>
        </div>

        <div>
          <label className="block text-xs font-medium text-on-surface mb-1">Organization / Entity Name *</label>
          <div className="relative">
            <Building className="w-4 h-4 absolute left-3 top-2.5 text-outline" />
            <input
              type="text"
              required
              value={formData.company}
              onChange={(e) => setFormData({ ...formData, company: e.target.value })}
              className="w-full h-9 pl-9 pr-3 rounded-lg border border-outline-variant/60 bg-surface-container-lowest text-xs text-on-surface focus:outline-none focus:border-primary-container"
              placeholder="Acme Technologies Pvt Ltd"
            />
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-medium text-on-surface mb-1">Master Password *</label>
            <div className="relative">
              <Lock className="w-4 h-4 absolute left-3 top-2.5 text-outline" />
              <input
                type="password"
                required
                value={formData.password}
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                className="w-full h-9 pl-9 pr-3 rounded-lg border border-outline-variant/60 bg-surface-container-lowest text-xs font-mono text-on-surface focus:outline-none focus:border-primary-container"
                placeholder=">8 chars, Aa1@..."
              />
            </div>
            <p className="text-[10px] text-outline mt-1">&gt;8 chars, upper, lower &amp; special char</p>
          </div>
          <div>
            <label className="block text-xs font-medium text-on-surface mb-1">Confirm Password *</label>
            <div className="relative">
              <Lock className="w-4 h-4 absolute left-3 top-2.5 text-outline" />
              <input
                type="password"
                required
                value={formData.confirmPassword}
                onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
                className="w-full h-9 pl-9 pr-3 rounded-lg border border-outline-variant/60 bg-surface-container-lowest text-xs font-mono text-on-surface focus:outline-none focus:border-primary-container"
              />
            </div>
          </div>
        </div>

        <button
          type="submit"
          className="w-full h-10 bg-primary hover:bg-primary-container text-white font-semibold text-xs rounded-lg flex items-center justify-center gap-2 shadow-elevation-1 transition-all mt-4"
        >
          <span>Complete Registration</span>
          <ArrowRight className="w-4 h-4" />
        </button>
      </form>

      <div className="mt-6 text-center text-xs text-on-surface-variant">
        Already have a tenant login?{' '}
        <Link to="/login" className="text-primary font-semibold hover:underline">
          Sign In here
        </Link>
      </div>
    </div>
  );
}
