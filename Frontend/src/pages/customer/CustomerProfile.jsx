import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  User,
  Building,
  Mail,
  Shield,
  Key,
  MapPin,
  Landmark,
  Save,
  CheckCircle2,
  ArrowLeft
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

export default function CustomerProfile() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [companyName, setCompanyName] = useState(user?.company || 'Nexus Dynamics');
  const [contactName, setContactName] = useState(user?.name || 'Rahul Gupta');
  const [email, setEmail] = useState(user?.email || 'customer@subscriptiq.com');
  const [gstin, setGstin] = useState('06AABCU9603R1ZM');
  const [billingAddress, setBillingAddress] = useState('Level 4, Cyber City Phase II, Sector 24, Gurugram, Haryana - 122002');
  const [saved, setSaved] = useState(false);

  const handleSubmit = (e) => {
    e.preventDefault();
    setSaved(true);
    setTimeout(() => setSaved(false), 3000);
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Organization Profile & Tax Identity</h1>
          <p className="text-xs text-on-surface-variant mt-0.5">
            Manage your legal entity billing coordinates, registered GSTIN, and authorized administrative contacts.
          </p>
        </div>
        <button
          onClick={() => navigate('/customer/dashboard')}
          className="px-3 py-1.5 border border-outline-variant/40 rounded-xl text-xs font-semibold hover:bg-surface-container flex items-center gap-1.5"
        >
          <ArrowLeft className="w-3.5 h-3.5" />
          <span>Dashboard</span>
        </button>
      </div>

      {saved && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs rounded-xl flex items-center gap-2 font-medium">
          <CheckCircle2 className="w-4 h-4 text-emerald-600" />
          <span>Organization profile changes saved to billing profile.</span>
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Company Coordinates */}
        <div className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-4">
          <h2 className="text-sm font-bold text-on-surface flex items-center gap-2 border-b border-surface-container pb-3">
            <Building className="w-4 h-4 text-primary" />
            <span>Corporate Entity Details</span>
          </h2>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
            <div>
              <label className="block font-medium text-on-surface mb-1">Registered Company Name</label>
              <input
                type="text"
                value={companyName}
                onChange={(e) => setCompanyName(e.target.value)}
                className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 bg-surface-container-lowest text-on-surface"
              />
            </div>
            <div>
              <label className="block font-medium text-on-surface mb-1">State & Indian GSTIN</label>
              <input
                type="text"
                value={gstin}
                onChange={(e) => setGstin(e.target.value)}
                className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 bg-surface-container-lowest text-on-surface font-mono"
              />
            </div>
            <div className="sm:col-span-2">
              <label className="block font-medium text-on-surface mb-1">Official Tax Billing Address</label>
              <textarea
                rows="2"
                value={billingAddress}
                onChange={(e) => setBillingAddress(e.target.value)}
                className="w-full p-2.5 rounded-lg border border-outline-variant/60 bg-surface-container-lowest text-on-surface"
              />
            </div>
          </div>
        </div>

        {/* Contact Coordinates */}
        <div className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-4">
          <h2 className="text-sm font-bold text-on-surface flex items-center gap-2 border-b border-surface-container pb-3">
            <User className="w-4 h-4 text-primary" />
            <span>Authorized Account Administrator</span>
          </h2>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
            <div>
              <label className="block font-medium text-on-surface mb-1">Contact Officer Name</label>
              <input
                type="text"
                value={contactName}
                onChange={(e) => setContactName(e.target.value)}
                className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 bg-surface-container-lowest text-on-surface"
              />
            </div>
            <div>
              <label className="block font-medium text-on-surface mb-1">Corporate Email Address</label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 bg-surface-container-lowest text-on-surface font-mono"
              />
            </div>
          </div>
        </div>

        {/* Security / Compliance Badges */}
        <div className="p-4 bg-surface-container-low rounded-xl border border-surface-container flex items-center justify-between text-xs text-on-surface-variant">
          <div className="flex items-center gap-2">
            <Shield className="w-4 h-4 text-emerald-600" />
            <span>SOC2 Type II Certified Customer Vault</span>
          </div>
          <button
            type="submit"
            className="px-5 py-2 bg-primary hover:bg-primary-container text-white text-xs font-bold rounded-xl shadow-elevation-1 flex items-center gap-1.5 active:scale-95 transition-all"
          >
            <Save className="w-3.5 h-3.5" />
            <span>Save Profile Changes</span>
          </button>
        </div>
      </form>
    </div>
  );
}
