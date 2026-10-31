import React, { useState } from 'react';
import { ArrowLeft, Save, Building, Mail, Phone, MapPin, AlertTriangle } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { customerService } from '../../../services/customerService';

export default function AddCustomer() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    companyName: '',
    contactName: '',
    email: '',
    phone: '',
    address: '',
    city: '',
    state: '',
    country: 'India',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await customerService.create(formData);
      navigate('/admin/customers');
    } catch (err) {
      setError(err.userMessage || 'Failed to create customer. Please check the inputs.');
    } finally {
      setLoading(false);
    }
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
          <p className="text-xs text-on-surface-variant">Create client entity in the database with billing address and contact profile.</p>
        </div>
      </div>

      {error && (
        <div className="p-3 bg-error-container/30 border border-error/30 rounded-xl text-xs text-error flex items-center gap-2">
          <AlertTriangle className="w-4 h-4 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      <form onSubmit={handleSubmit} className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-5">
        <div className="border-b border-surface-container pb-4">
          <h2 className="text-sm font-bold text-on-surface flex items-center gap-2 mb-3">
            <Building className="w-4 h-4 text-primary" />
            <span>Corporate Identity &amp; Contact</span>
          </h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-medium text-on-surface mb-1">Company / Entity Name *</label>
              <input
                type="text"
                required
                value={formData.companyName}
                onChange={(e) => setFormData({ ...formData, companyName: e.target.value })}
                className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest"
                placeholder="e.g. Acme Innovations Pvt Ltd"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-on-surface mb-1">Primary Contact Person *</label>
              <input
                type="text"
                required
                value={formData.contactName}
                onChange={(e) => setFormData({ ...formData, contactName: e.target.value })}
                className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest"
                placeholder="e.g. Rohan Sharma"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-on-surface mb-1">Billing Email Address *</label>
              <input
                type="email"
                required
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest font-mono"
                placeholder="billing@acme.com"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-on-surface mb-1">Contact Phone</label>
              <input
                type="text"
                value={formData.phone}
                onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest font-mono"
                placeholder="+91 9876543210"
              />
            </div>
          </div>
        </div>

        <div>
          <h2 className="text-sm font-bold text-on-surface flex items-center gap-2 mb-3">
            <MapPin className="w-4 h-4 text-primary" />
            <span>Billing Address</span>
          </h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="sm:col-span-2">
              <label className="block text-xs font-medium text-on-surface mb-1">Street Address</label>
              <input
                type="text"
                value={formData.address}
                onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest"
                placeholder="104, Tech Park, Outer Ring Road"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-on-surface mb-1">City</label>
              <input
                type="text"
                value={formData.city}
                onChange={(e) => setFormData({ ...formData, city: e.target.value })}
                className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest"
                placeholder="Bengaluru"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-on-surface mb-1">State / Province</label>
              <input
                type="text"
                value={formData.state}
                onChange={(e) => setFormData({ ...formData, state: e.target.value })}
                className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest"
                placeholder="Karnataka"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-on-surface mb-1">Country</label>
              <input
                type="text"
                value={formData.country}
                onChange={(e) => setFormData({ ...formData, country: e.target.value })}
                className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest"
                placeholder="India"
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
            disabled={loading}
            className="px-4 py-2 bg-primary hover:bg-primary-container text-white rounded-lg text-xs font-semibold flex items-center gap-1.5 shadow-sm disabled:opacity-50"
          >
            <Save className="w-3.5 h-3.5" />
            <span>{loading ? 'Creating...' : 'Create Customer Account'}</span>
          </button>
        </div>
      </form>
    </div>
  );
}
