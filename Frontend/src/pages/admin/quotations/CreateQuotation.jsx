import React, { useState } from 'react';
import { ArrowLeft, Save, FileSpreadsheet, Plus, Trash2, Calculator } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { quotationService } from '../../../services/quotationService';

export default function CreateQuotation() {
  const navigate = useNavigate();
  const [customer, setCustomer] = useState('Nexus Dynamics');
  const [contact, setContact] = useState('Vikram Malhotra');
  const [items, setItems] = useState([
    { name: 'SubscriptIQ Enterprise Sentinel (Annual)', qty: 1, unitPrice: 540000 },
    { name: 'AI Churn Sentinel Add-on', qty: 1, unitPrice: 60000 }
  ]);
  const [discountPercent, setDiscountPercent] = useState(10);

  const subtotal = items.reduce((acc, it) => acc + (it.qty * it.unitPrice), 0);
  const discountAmount = (subtotal * discountPercent) / 100;
  const taxableAmount = subtotal - discountAmount;
  const gstAmount = taxableAmount * 0.18;
  const total = taxableAmount + gstAmount;

  const handleSubmit = async (e) => {
    e.preventDefault();
    await quotationService.create({
      customer,
      contact,
      value: total,
      discount: `${discountPercent}%`,
      itemsCount: items.length
    });
    navigate('/admin/quotations');
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate('/admin/quotations')}
          className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container"
        >
          <ArrowLeft className="w-4 h-4" />
        </button>
        <div>
          <h1 className="text-xl font-bold text-on-surface">CPQ Quotation & Deal Studio</h1>
          <p className="text-xs text-on-surface-variant">Configure line items, volume tiered discounts, and GST tax calculations.</p>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        <div className="lg:col-span-8 bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-5">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-medium text-on-surface mb-1">Target Client Organization</label>
              <input
                type="text"
                required
                value={customer}
                onChange={(e) => setCustomer(e.target.value)}
                className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest font-medium"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-on-surface mb-1">Primary Deal Contact</label>
              <input
                type="text"
                required
                value={contact}
                onChange={(e) => setContact(e.target.value)}
                className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest"
              />
            </div>
          </div>

          <div>
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-bold text-on-surface uppercase tracking-wider">Configured Line Items</span>
            </div>
            <div className="space-y-2">
              {items.map((it, idx) => (
                <div key={idx} className="p-3 rounded-lg border border-outline-variant/40 bg-surface-container-low flex items-center justify-between text-xs">
                  <div>
                    <div className="font-semibold text-on-surface">{it.name}</div>
                    <div className="text-[11px] text-on-surface-variant">Qty: {it.qty} × ₹{it.unitPrice.toLocaleString('en-IN')}</div>
                  </div>
                  <div className="font-mono font-bold text-on-surface">
                    ₹{(it.qty * it.unitPrice).toLocaleString('en-IN')}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Pricing Summary (4 Cols) */}
        <div className="lg:col-span-4 bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-4 flex flex-col justify-between">
          <div>
            <h2 className="text-sm font-bold text-on-surface flex items-center gap-2 mb-4">
              <Calculator className="w-4 h-4 text-primary" />
              <span>Financial Ledger Summary</span>
            </h2>

            <div className="space-y-2.5 text-xs">
              <div className="flex justify-between text-on-surface-variant">
                <span>Gross Subtotal:</span>
                <span className="font-mono font-semibold">₹{subtotal.toLocaleString('en-IN')}</span>
              </div>
              <div className="flex justify-between text-emerald-700">
                <span>Volume Discount ({discountPercent}%):</span>
                <span className="font-mono font-semibold">-₹{discountAmount.toLocaleString('en-IN')}</span>
              </div>
              <div className="flex justify-between text-on-surface-variant">
                <span>GST (18% IGST):</span>
                <span className="font-mono font-semibold">+₹{gstAmount.toLocaleString('en-IN')}</span>
              </div>
              <div className="pt-3 border-t border-surface-container flex justify-between text-sm font-bold text-on-surface">
                <span>Net Contract Total:</span>
                <span className="font-mono text-primary text-base">₹{total.toLocaleString('en-IN')}</span>
              </div>
            </div>
          </div>

          <button
            type="submit"
            className="w-full py-2.5 bg-primary hover:bg-primary-container text-white text-xs font-semibold rounded-lg shadow-elevation-1 flex items-center justify-center gap-2"
          >
            <Save className="w-3.5 h-3.5" />
            <span>Issue Official CPQ Quote</span>
          </button>
        </div>
      </form>
    </div>
  );
}

