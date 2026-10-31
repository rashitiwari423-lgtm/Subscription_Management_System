import React, { useState } from 'react';
import { Settings, Shield, Building, Key, Users, Save, CheckCircle2 } from 'lucide-react';

export default function OrgSettingsPage() {
  const [activeTab, setActiveTab] = useState('general');
  const [saved, setSaved] = useState(false);

  const handleSave = (e) => {
    e.preventDefault();
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Organization Settings & RBAC Permissions</h1>
          <p className="text-xs text-on-surface-variant">Configure enterprise entity legal parameters, API keys, and security baselines.</p>
        </div>
        {saved && (
          <span className="flex items-center gap-1.5 text-xs text-emerald-700 bg-emerald-50 px-3 py-1 rounded-full border border-emerald-200">
            <CheckCircle2 className="w-3.5 h-3.5" />
            <span>Policy updated</span>
          </span>
        )}
      </div>

      <div className="bg-surface-container-lowest rounded-2xl border border-outline-variant/40 shadow-elevation-1 overflow-hidden">
        <div className="flex border-b border-surface-container px-4">
          {[
            { id: 'general', label: 'General & Tax Profile', icon: Building },
            { id: 'rbac', label: 'RBAC Access Matrix', icon: Shield },
            { id: 'security', label: 'Security & Zero-Trust', icon: Key }
          ].map(t => {
            const Icon = t.icon;
            return (
              <button
                key={t.id}
                onClick={() => setActiveTab(t.id)}
                className={`flex items-center gap-2 py-3 px-4 text-xs font-semibold border-b-2 transition-all ${
                  activeTab === t.id
                    ? 'border-primary text-primary'
                    : 'border-transparent text-on-surface-variant hover:text-on-surface'
                }`}
              >
                <Icon className="w-3.5 h-3.5" />
                <span>{t.label}</span>
              </button>
            );
          })}
        </div>

        <form onSubmit={handleSave} className="p-6 space-y-5">
          {activeTab === 'general' && (
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-medium text-on-surface mb-1">Entity Registered Name</label>
                  <input defaultValue="SubscriptIQ Technologies India Pvt Ltd" className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest" />
                </div>
                <div>
                  <label className="block text-xs font-medium text-on-surface mb-1">Primary Operating Currency</label>
                  <input defaultValue="INR (Indian Rupee - ₹)" disabled className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container font-mono" />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-medium text-on-surface mb-1">Corporate GSTIN</label>
                  <input defaultValue="27AABCS9912E1ZQ" className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest font-mono uppercase" />
                </div>
                <div>
                  <label className="block text-xs font-medium text-on-surface mb-1">HSN / SAC Master Code</label>
                  <input defaultValue="998313 (Software as a Service)" className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest font-mono" />
                </div>
              </div>
            </div>
          )}

          {activeTab === 'rbac' && (
            <div className="space-y-3">
              <h3 className="text-xs font-bold text-on-surface uppercase tracking-wider">Role Access Boundary</h3>
              <div className="border border-outline-variant/40 rounded-xl overflow-hidden text-xs">
                <table className="w-full text-left">
                  <thead className="bg-surface-container-low text-on-surface-variant">
                    <tr>
                      <th className="p-3">Role Level</th>
                      <th className="p-3">Permissions Scope</th>
                      <th className="p-3">Dunning / Write</th>
                      <th className="p-3">Tax Master</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-surface-container">
                    <tr>
                      <td className="p-3 font-bold text-primary">ADMIN (CFO)</td>
                      <td className="p-3">Global Tenancy Access</td>
                      <td className="p-3 text-emerald-700 font-bold">YES</td>
                      <td className="p-3 text-emerald-700 font-bold">YES</td>
                    </tr>
                    <tr>
                      <td className="p-3 font-bold text-on-surface">STAFF (Ops)</td>
                      <td className="p-3">Customer Queues & Quotations</td>
                      <td className="p-3 text-emerald-700 font-bold">YES</td>
                      <td className="p-3 text-rose-700 font-bold">NO</td>
                    </tr>
                    <tr>
                      <td className="p-3 font-bold text-outline">CUSTOMER</td>
                      <td className="p-3">Self-Service Billing Portal</td>
                      <td className="p-3 text-rose-700 font-bold">NO</td>
                      <td className="p-3 text-rose-700 font-bold">NO</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {activeTab === 'security' && (
            <div className="space-y-4 text-xs">
              <div className="p-4 rounded-xl bg-surface-container-low border border-surface-container flex items-center justify-between">
                <div>
                  <div className="font-bold text-on-surface">Hardware Token Enforcement (FIDO2)</div>
                  <div className="text-[11px] text-on-surface-variant">Mandate YubiKey or Touch ID for all administrative write actions.</div>
                </div>
                <input type="checkbox" defaultChecked className="w-4 h-4 rounded accent-primary" />
              </div>
              <div className="p-4 rounded-xl bg-surface-container-low border border-surface-container flex items-center justify-between">
                <div>
                  <div className="font-bold text-on-surface">SOC2 Telemetry Stream</div>
                  <div className="text-[11px] text-on-surface-variant">Export immutable financial audit logs to S3/CloudWatch vault.</div>
                </div>
                <input type="checkbox" defaultChecked className="w-4 h-4 rounded accent-primary" />
              </div>
            </div>
          )}

          <div className="pt-4 border-t border-surface-container flex items-center justify-end">
            <button
              type="submit"
              className="px-4 py-2 bg-primary hover:bg-primary-container text-white text-xs font-semibold rounded-lg flex items-center gap-1.5 shadow-sm"
            >
              <Save className="w-3.5 h-3.5" />
              <span>Save System Settings</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

