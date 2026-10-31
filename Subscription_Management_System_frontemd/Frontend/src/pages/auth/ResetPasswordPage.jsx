import React, { useState } from 'react';
import { useNavigate, Link, useSearchParams } from 'react-router-dom';
import { ShieldCheck, Lock, Key, ArrowRight, CheckCircle2 } from 'lucide-react';
import { authService } from '../../services/authService';

export default function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const urlToken = searchParams.get('token') || '';
  const [token, setToken] = useState(urlToken);
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!token.trim()) {
      setError('Password reset token is required.');
      return;
    }

    if (password.length < 8) {
      setError('Password must be at least 8 characters long.');
      return;
    }

    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    try {
      setLoading(true);
      await authService.resetPassword(token.trim(), password);
      setSuccess(true);
      setTimeout(() => {
        navigate('/login');
      }, 2000);
    } catch (err) {
      setError(err.userMessage || 'Failed to reset password. The token may be invalid or expired.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-md mx-auto bg-surface-container-lowest p-8 rounded-2xl border border-outline-variant/40 shadow-elevation-2">
      <div className="mb-6">
        <div className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-primary-fixed text-on-primary-fixed text-xs font-semibold uppercase tracking-wider mb-3">
          <ShieldCheck className="w-3.5 h-3.5" />
          <span>Credential Renewal</span>
        </div>
        <h2 className="text-xl font-bold text-on-surface">Set New Master Password</h2>
        <p className="text-xs text-on-surface-variant mt-1">
          Provide your verification token and specify replacement credentials.
        </p>
      </div>

      {success ? (
        <div className="p-4 rounded-xl bg-emerald-50 border border-emerald-200 text-xs text-emerald-800 flex items-center gap-2.5">
          <CheckCircle2 className="w-4 h-4 text-emerald-600 flex-shrink-0" />
          <span>Password renewed successfully! Redirecting to login...</span>
        </div>
      ) : (
        <form onSubmit={handleSubmit} className="space-y-4">
          {error && (
            <div className="p-2.5 rounded-lg bg-error-container/40 border border-error/30 text-xs text-error font-medium">
              {error}
            </div>
          )}

          <div>
            <label className="block text-xs font-medium text-on-surface mb-1">Reset Token *</label>
            <div className="relative">
              <Key className="w-4 h-4 absolute left-3 top-2.5 text-outline" />
              <input
                type="text"
                required
                value={token}
                onChange={(e) => setToken(e.target.value)}
                className="w-full h-9 pl-9 pr-3 rounded-lg border border-outline-variant/60 text-xs font-mono bg-surface-container-lowest text-on-surface focus:outline-none focus:border-primary-container"
                placeholder="Enter reset token"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-medium text-on-surface mb-1">New Password *</label>
            <div className="relative">
              <Lock className="w-4 h-4 absolute left-3 top-2.5 text-outline" />
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full h-9 pl-9 pr-3 rounded-lg border border-outline-variant/60 text-xs font-mono bg-surface-container-lowest text-on-surface focus:outline-none focus:border-primary-container"
                placeholder=">= 8 characters"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-medium text-on-surface mb-1">Confirm New Password *</label>
            <div className="relative">
              <Lock className="w-4 h-4 absolute left-3 top-2.5 text-outline" />
              <input
                type="password"
                required
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                className="w-full h-9 pl-9 pr-3 rounded-lg border border-outline-variant/60 text-xs font-mono bg-surface-container-lowest text-on-surface focus:outline-none focus:border-primary-container"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full h-10 bg-primary hover:bg-primary-container text-white text-xs font-semibold rounded-lg shadow-elevation-1 transition-all flex items-center justify-center gap-1.5 disabled:opacity-50"
          >
            <span>{loading ? 'Updating Password...' : 'Update Master Password'}</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </button>
        </form>
      )}

      <div className="mt-6 text-center text-xs text-on-surface-variant">
        <Link to="/login" className="text-primary hover:underline font-semibold">
          Cancel &amp; Back to Sign In
        </Link>
      </div>
    </div>
  );
}
