import React, { useState } from 'react';
import { ArrowLeft, CheckCircle2, Building, Users, CreditCard, Sparkles, ArrowRight } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export default function OnboardingPage() {
  const [step, setStep] = useState(1);
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-surface flex flex-col items-center justify-center p-6">
      <div className="max-w-2xl w-full bg-surface-container-lowest p-8 rounded-2xl border border-outline-variant/40 shadow-elevation-2">
        {/* Progress Bar */}
        <div className="flex items-center justify-between mb-8">
          {[
            { num: 1, label: 'Entity Profile', icon: Building },
            { num: 2, label: 'Team & RBAC', icon: Users },
            { num: 3, label: 'Gateway & Tax', icon: CreditCard },
            { num: 4, label: 'AI Sentinel', icon: Sparkles }
          ].map((s) => (
            <div key={s.num} className="flex items-center gap-2">
              <div className={`w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold ${
                step >= s.num ? 'bg-primary text-white' : 'bg-surface-container text-on-surface-variant'
              }`}>
                {s.num}
              </div>
              <span className={`text-xs font-medium hidden sm:inline ${step >= s.num ? 'text-on-surface' : 'text-outline'}`}>
                {s.label}
              </span>
            </div>
          ))}
        </div>

        {step === 1 && (
          <div className="space-y-4">
            <h2 className="text-xl font-bold text-on-surface">Organization Workspace Profile</h2>
            <p className="text-xs text-on-surface-variant">Configure your institutional identity and primary billing currency.</p>
            <div className="grid grid-cols-2 gap-4 pt-2">
              <div>
                <label className="block text-xs font-medium text-on-surface mb-1">Company Legal Name</label>
                <input defaultValue="Acme Global Technologies Pvt Ltd" className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs text-on-surface" />
              </div>
              <div>
                <label className="block text-xs font-medium text-on-surface mb-1">GSTIN Number (India)</label>
                <input defaultValue="27AADCB2230M1Z2" className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs font-mono text-on-surface" />
              </div>
            </div>
          </div>
        )}

        {step === 2 && (
          <div className="space-y-4">
            <h2 className="text-xl font-bold text-on-surface">Configure Governance & RBAC</h2>
            <p className="text-xs text-on-surface-variant">Grant granular access for CFOs, Account Executives, and Billing Ops.</p>
            <div className="p-4 rounded-xl bg-surface-container-low text-xs space-y-2 border border-surface-container">
              <div className="flex items-center justify-between">
                <span>Default Security Policy:</span>
                <span className="font-bold text-emerald-700">Strict Zero-Trust + SOC2 Matrix</span>
              </div>
              <div className="flex items-center justify-between">
                <span>Multi-Factor Enforcement:</span>
                <span className="font-bold text-primary">WebAuthn / FIDO2 Hardware Key</span>
              </div>
            </div>
          </div>
        )}

        {step === 3 && (
          <div className="space-y-4">
            <h2 className="text-xl font-bold text-on-surface">Payment Gateway Connectors</h2>
            <p className="text-xs text-on-surface-variant">Connect Razorpay, Stripe, or bank RTGS rails for instant reconciliation.</p>
            <div className="grid grid-cols-2 gap-3 pt-2">
              <div className="p-3 border-2 border-primary rounded-xl bg-primary/5 text-xs">
                <div className="font-bold text-primary">Razorpay Route (India)</div>
                <div className="text-[10px] text-on-surface-variant mt-0.5">UPI AutoPay, e-NACH, Net Banking & Cards</div>
              </div>
              <div className="p-3 border border-outline-variant/40 rounded-xl bg-surface-container-lowest text-xs">
                <div className="font-bold text-on-surface">Stripe Elements (Global)</div>
                <div className="text-[10px] text-on-surface-variant mt-0.5">International multi-currency ARR card collection</div>
              </div>
            </div>
          </div>
        )}

        {step === 4 && (
          <div className="space-y-4 text-center py-4">
            <div className="w-14 h-14 rounded-2xl bg-gradient-to-tr from-primary to-secondary text-white flex items-center justify-center mx-auto shadow-ai-glow">
              <Sparkles className="w-7 h-7" />
            </div>
            <h2 className="text-xl font-bold text-on-surface">Autonomous Intelligence Ready</h2>
            <p className="text-xs text-on-surface-variant max-w-md mx-auto">
              Your SubscriptIQ tenant is initialized with automated churn prediction, intelligent dunning sequences, and unified ARR analytics.
            </p>
          </div>
        )}

        {/* Wizard Footer Controls */}
        <div className="flex items-center justify-between mt-8 pt-4 border-t border-surface-container">
          {step > 1 ? (
            <button
              onClick={() => setStep(step - 1)}
              className="px-4 py-2 text-xs font-semibold text-on-surface-variant hover:text-on-surface border border-outline-variant/40 rounded-lg"
            >
              Previous Step
            </button>
          ) : <div></div>}

          {step < 4 ? (
            <button
              onClick={() => setStep(step + 1)}
              className="px-5 py-2 text-xs font-semibold bg-primary text-white rounded-lg hover:bg-primary-container flex items-center gap-1.5 shadow-elevation-1"
            >
              <span>Continue</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </button>
          ) : (
            <button
              onClick={() => navigate('/admin/dashboard')}
              className="px-6 py-2.5 text-xs font-semibold bg-primary hover:bg-primary-container text-white rounded-lg flex items-center gap-1.5 shadow-ai-glow"
            >
              <span>Launch Command Hub</span>
              <CheckCircle2 className="w-4 h-4" />
            </button>
          )}
        </div>
      </div>
    </div>
  );
}

