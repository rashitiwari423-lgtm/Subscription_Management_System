import React, { useState } from 'react';
import { ArrowLeft, Mail, CheckCircle } from 'lucide-react';
import { Link } from 'react-router-dom';

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [sent, setSent] = useState(false);

  return (
    <div className="max-w-md mx-auto bg-surface-container-lowest p-8 rounded-2xl border border-outline-variant/40 shadow-elevation-2">
      <Link to="/login" className="inline-flex items-center gap-1.5 text-xs text-primary font-medium mb-4 hover:underline">
        <ArrowLeft className="w-3.5 h-3.5" />
        <span>Back to Sign In</span>
      </Link>

      <h2 className="text-xl font-bold text-on-surface">Reset Hardware Credentials</h2>
      <p className="text-xs text-on-surface-variant mt-1 mb-6">
        Enter your verified corporate email to receive a hardware token recovery sequence.
      </p>

      {sent ? (
        <div className="p-4 rounded-xl bg-emerald-50 border border-emerald-200 text-xs text-emerald-800 flex items-start gap-2.5">
          <CheckCircle className="w-4 h-4 text-emerald-600 mt-0.5 flex-shrink-0" />
          <div>
            <div className="font-semibold">Recovery challenge dispatched</div>
            <div className="mt-0.5 text-emerald-700">Check your inbox for cryptographic authorization link valid for 15 minutes.</div>
          </div>
        </div>
      ) : (
        <form onSubmit={(e) => { e.preventDefault(); setSent(true); }} className="space-y-4">
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
            className="w-full h-10 bg-primary hover:bg-primary-container text-white text-xs font-semibold rounded-lg shadow-elevation-1 transition-all"
          >
            Dispatch Challenge
          </button>
        </form>
      )}
    </div>
  );
}

