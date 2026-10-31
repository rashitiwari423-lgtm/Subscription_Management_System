import React, { useState } from 'react';
import { Code2, Key, Copy, CheckCircle2, Webhook, Terminal, ExternalLink } from 'lucide-react';

export default function DeveloperPage() {
  const [copied, setCopied] = useState(false);
  const apiKey = 'siq_live_9f81a7b8c2d3e4f5061728394a5b6c7d';

  const handleCopy = () => {
    navigator.clipboard.writeText(apiKey);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div>
        <h1 className="text-xl font-bold text-on-surface">Developer APIs & Webhook Subscriptions</h1>
        <p className="text-xs text-on-surface-variant">
          Programmatic billing primitives, REST API keys, and event dispatch webhooks.
        </p>
      </div>

      {/* API Key Box */}
      <div className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Key className="w-4 h-4 text-primary" />
            <span className="text-xs font-bold text-on-surface uppercase tracking-wider">Production API Key</span>
          </div>
          <span className="text-[10px] font-mono bg-emerald-50 text-emerald-800 border border-emerald-200 px-2 py-0.5 rounded-full">
            READ / WRITE ENABLED
          </span>
        </div>

        <div className="flex items-center gap-2">
          <input
            type="text"
            readOnly
            value={apiKey}
            className="flex-1 h-9 px-3 rounded-lg border border-outline-variant/60 bg-surface-container-low font-mono text-xs text-on-surface"
          />
          <button
            onClick={handleCopy}
            className="px-3.5 py-2 rounded-lg bg-surface-container border border-outline-variant/40 text-xs font-semibold flex items-center gap-1.5 hover:bg-surface-container-high text-on-surface"
          >
            {copied ? <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" /> : <Copy className="w-3.5 h-3.5" />}
            <span>{copied ? 'Copied' : 'Copy Key'}</span>
          </button>
        </div>
      </div>

      {/* Sample cURL Code */}
      <div className="bg-inverse-surface text-inverse-on-surface p-6 rounded-2xl shadow-elevation-2 space-y-3 font-mono text-xs overflow-x-auto">
        <div className="flex items-center justify-between text-outline-variant text-[11px] pb-2 border-b border-white/10">
          <span>cURL • Create Subscription Entitlement</span>
          <span>HTTP 200 OK</span>
        </div>
        <pre className="text-emerald-400">
{`curl -X POST https://api.subscriptiq.io/v1/subscriptions \\
  -H "Authorization: Bearer siq_live_***" \\
  -H "Content-Type: application/json" \\
  -d '{
    "customerId": "C001",
    "planId": "PLAN-02",
    "currency": "INR",
    "billingCycle": "MONTHLY"
  }'`}
        </pre>
      </div>

      {/* Webhook Events */}
      <div className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-3">
        <h2 className="text-sm font-bold text-on-surface flex items-center gap-2">
          <Webhook className="w-4 h-4 text-primary" />
          <span>Registered Event Webhooks</span>
        </h2>
        <div className="space-y-2 text-xs">
          <div className="p-3 rounded-lg bg-surface-container-low flex items-center justify-between font-mono">
            <span>https://api.nexusdynamics.com/billing-hooks</span>
            <span className="text-emerald-700 font-bold">subscription.renewed</span>
          </div>
          <div className="p-3 rounded-lg bg-surface-container-low flex items-center justify-between font-mono">
            <span>https://api.apexglobal.com/webhooks/dunning</span>
            <span className="text-amber-700 font-bold">payment.failed</span>
          </div>
        </div>
      </div>
    </div>
  );
}

