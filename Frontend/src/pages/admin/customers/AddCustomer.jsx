import React, { useState } from 'react';
import { ArrowLeft, Save, Building, Mail, Phone, MapPin } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { customerService } from '../../../services/customerService';

export default function AddCustomer() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    plan: 'Enterprise',
    seats: 25,
    mrr: 25000,
    industry: 'Technology',
    gstin: '27AABCA1234F1Z5',
    phone: '+91 98765 00000',
    city: 'Mumbai',
    state: 'Maharashtra'
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    await customerService.create(formData);
    navigate('/admin/customers');
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate('/admin/customers')}
          className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container text-on-surface-variant"
        >
          <ArrowLeft className="w-4 h-4" />
        </button>
        <div>
          <h1 className="text-xl font-bold text-on-surface">Add New Customer Account</h1>
          <p className="text-xs text-on-surface-variant">Create client entity with automated billing profile and GST reconciliation.</p>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-5">
        <div className="border-b border-surface-container pb-4">
          <h2 className="text-sm font-bold text-on-surface flex items-center gap-2 mb-3">
            <Building className="w-4 h-4 text-primary" />
            <span>Corporate Identity & Entity</span>
          </h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-medium text-on-surface mb-1">Company / Entity Name *</label>
              <input
                type="text"
                required
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest"
                placeholder="e.g. Reliance FinTech Ltd"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-on-surface mb-1">Billing Contact Email *</label>
              <input
                type="email"
                required
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest font-mono"
                placeholder="billing@company.com"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-on-surface mb-1">Industry Vertical</label>
              <select
                value={formData.industry}
                onChange={(e) => setFormData({ ...formData, industry: e.target.value })}
                className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest"
              >
                <option value="Technology">Technology & SaaS</option>
                <option value="Finance">Fintech & Banking</option>
                <option value="Healthcare">Healthcare & Bio</option>
                <option value="Retail">Retail & E-commerce</option>
                <option value="Consulting">Consulting & Ops</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-medium text-on-surface mb-1">GSTIN (India)</label>
              <input
                type="text"
                value={formData.gstin}
                onChange={(e) => setFormData({ ...formData, gstin: e.target.value })}
                className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest font-mono uppercase"
              />
            </div>
          </div>
        </div>

        <div>
          <h2 className="text-sm font-bold text-on-surface mb-3">Subscription Contract Baseline</h2>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div>
              <label className="block text-xs font-medium text-on-surface mb-1">Contract Tier Plan</label>
              <select
                value={formData.plan}
                onChange={(e) => setFormData({ ...formData, plan: e.target.value })}
                className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest"
              >
                <option value="Enterprise">Enterprise Sentinel</option>
                <option value="Business">Growth Scale</option>
                <option value="Starter">Starter Pro</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-medium text-on-surface mb-1">Provisioned Seats</label>
              <input
                type="number"
                value={formData.seats}
                onChange={(e) => setFormData({ ...formData, seats: Number(e.target.value) })}
                className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest font-mono"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-on-surface mb-1">Monthly MRR Target (₹)</label>
              <input
                type="number"
                value={formData.mrr}
                onChange={(e) => setFormData({ ...formData, mrr: Number(e.target.value) })}
                className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest font-mono"
              />
            </div>
          </div>
        </div>

        <div className="pt-4 border-t border-surface-container flex items-center justify-end gap-2">
          <button
            type="button"
            onClick={() => navigate('/admin/customers')}
            className="px-4 py-2 border border-outline-variant/40 rounded-lg text-xs font-semibold text-on-surface-variant hover:text-on-surface"
          >
            Cancel
          </button>
          <button
            type="submit"
            className="px-4 py-2 bg-primary hover:bg-primary-container text-white rounded-lg text-xs font-semibold flex items-center gap-1.5 shadow-sm"
          >
            <Save className="w-3.5 h-3.5" />
            <span>Create Customer Account</span>
          </button>
        </div>
      </form>
    </div>
  );
}

