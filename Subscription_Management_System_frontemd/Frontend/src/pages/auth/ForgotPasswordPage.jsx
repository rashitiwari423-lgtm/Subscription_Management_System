import React, { useState } from 'react';
import { ArrowLeft, Mail, CheckCircle } from 'lucide-react';
import { Link } from 'react-router-dom';
import { authService } from '../../services/authService';

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [sent, setSent] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await authService.forgotPassword(email);
      setSent(true);
    } catch (err) {
      setError(err.userMessage || 'Failed to request password reset. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-md mx-auto bg-surface-container-lowest p-8 rounded-2xl border border-outline-variant/40 shadow-elevation-2">
      <Link to="/login" className="inline-flex items-center gap-1.5 text-xs text-primary font-medium mb-4 hover:underline">
        <ArrowLeft className="w-3.5 h-3.5" />
        <span>Back to Sign In</span>
      </Link>

      <h2 className="text-xl font-bold text-on-surface">Reset Hardware Credentials</h2>
      <p className="text-xs text-on-surface-variant mt-1 mb-6">
        Enter your verified corporate email to receive a password reset token.
      </p>

      {error && (
        <div className="mb-4 p-3 rounded-xl bg-error-container/40 border border-error/30 text-xs text-error font-medium">
          {error}
        </div>
      )}

      {sent ? (
        <div className="p-4 rounded-xl bg-emerald-50 border border-emerald-200 text-xs text-emerald-800 flex items-start gap-2.5">
          <CheckCircle className="w-4 h-4 text-emerald-600 mt-0.5 flex-shrink-0" />
          <div>
            <div className="font-semibold">Reset token generated</div>
            <div className="mt-0.5 text-emerald-700">If the account exists, instructions have been dispatched. Valid for 15 minutes.</div>
            <div className="mt-3">
              <Link to="/reset-password" className="text-primary font-semibold underline">
                Have a token? Proceed to Reset Password &rarr;
              </Link>
            </div>
          </div>
        </div>
      ) : (
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-medium text-on-surface mb-1">Corporate Email Address</label>
            <div className="relative">
              <Mail className="w-4 h-4 absolute left-3 top-3 text-outline" />
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full h-10 pl-9 pr-3 rounded-lg border border-outline-variant/60 bg-surface-container-lowest text-xs text-on-surface focus:outline-none focus:border-primary-container font-mono"
                placeholder="cfo@enterprise.com"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full h-10 bg-primary hover:bg-primary-container text-white text-xs font-semibold rounded-lg shadow-elevation-1 transition-all disabled:opacity-50"
          >
            {loading ? 'Sending request...' : 'Dispatch Challenge'}
          </button>
        </form>
      )}
    </div>
  );
}

