import React, { useState, useEffect } from 'react';
import { ArrowLeft, Save, FileSpreadsheet, Plus, Trash2, Calculator, AlertTriangle, RefreshCw } from 'lucide-react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { quotationService } from '../../../services/quotationService';
import { customerService } from '../../../services/customerService';
import { planService } from '../../../services/planService';

export default function CreateQuotation() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const initialCustomerId = searchParams.get('customerId') || '';

  const [customers, setCustomers] = useState([]);
  const [plans, setPlans] = useState([]);
  const [loadingData, setLoadingData] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const [customerId, setCustomerId] = useState(initialCustomerId);
  const [quotationDate, setQuotationDate] = useState(new Date().toISOString().split('T')[0]);
  const [validUntil, setValidUntil] = useState(
    new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
  );
  const [notes, setNotes] = useState('Standard enterprise subscription terms and SLA agreement.');

  const [items, setItems] = useState([
    { planId: '', quantity: 1, discountAmount: 0, taxAmount: 0 }
  ]);

  useEffect(() => {
    const loadPrerequisites = async () => {
      setLoadingData(true);
      try {
        const [cList, pList] = await Promise.all([
          customerService.getAll(),
          planService.getAll(),
        ]);
        setCustomers(cList || []);
        setPlans(pList || []);
        if (!customerId && cList.length > 0) {
          setCustomerId(String(cList[0].id));
        }
        if (pList.length > 0) {
          setItems([{ planId: String(pList[0].id), quantity: 1, discountAmount: 0, taxAmount: 0 }]);
        }
      } catch (err) {
        setError('Failed to load customers or plans for quotation.');
      } finally {
        setLoadingData(false);
      }
    };
    loadPrerequisites();
  }, []);

  const handleAddItem = () => {
    const defaultPlanId = plans.length > 0 ? String(plans[0].id) : '';
    setItems([...items, { planId: defaultPlanId, quantity: 1, discountAmount: 0, taxAmount: 0 }]);
  };

  const handleRemoveItem = (index) => {
    if (items.length > 1) {
      setItems(items.filter((_, i) => i !== index));
    }
  };

  const handleItemChange = (index, field, value) => {
    const updated = [...items];
    updated[index][field] = value;
    setItems(updated);
  };

  // Calculations
  const calculatedItems = items.map((it) => {
    const selectedPlan = plans.find((p) => String(p.id) === String(it.planId));
    const unitPrice = selectedPlan ? Number(selectedPlan.price || 0) : 0;
    const qty = parseInt(it.quantity, 10) || 1;
    const itemSubtotal = unitPrice * qty;
    const disc = parseFloat(it.discountAmount) || 0;
    const tax = parseFloat(it.taxAmount) || 0;
    const itemTotal = Math.max(0, itemSubtotal - disc) + tax;
    return {
      ...it,
      unitPrice,
      itemSubtotal,
      itemTotal,
      planName: selectedPlan?.name || `Plan #${it.planId}`,
    };
  });

  const subtotal = calculatedItems.reduce((acc, it) => acc + it.itemSubtotal, 0);
  const totalDiscount = calculatedItems.reduce((acc, it) => acc + (parseFloat(it.discountAmount) || 0), 0);
  const totalTax = calculatedItems.reduce((acc, it) => acc + (parseFloat(it.taxAmount) || 0), 0);
  const grandTotal = Math.max(0, subtotal - totalDiscount) + totalTax;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!customerId) {
      setError('Please select a customer.');
      return;
    }
    if (items.some((it) => !it.planId)) {
      setError('All quotation items must have a plan selected.');
      return;
    }
    setError('');
    setSubmitting(true);
    try {
      await quotationService.create({
        customerId: parseInt(customerId, 10),
        quotationDate,
        validUntil,
        notes,
        items: items.map((it) => ({
          planId: parseInt(it.planId, 10),
          quantity: parseInt(it.quantity, 10) || 1,
          discountAmount: parseFloat(it.discountAmount) || 0,
          taxAmount: parseFloat(it.taxAmount) || 0,
        })),
      });
      navigate('/admin/quotations');
    } catch (err) {
      setError(err.userMessage || 'Failed to create quotation.');
    } finally {
      setSubmitting(false);
    }
  };

  const formatCurr = (val) => `₹${Number(val || 0).toLocaleString('en-IN', { maximumFractionDigits: 2 })}`;

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
          <h1 className="text-xl font-bold text-on-surface">CPQ Quotation &amp; Deal Studio</h1>
          <p className="text-xs text-on-surface-variant">Configure line items, plans, volume discounts, and validity schedules.</p>
        </div>
      </div>

      {error && (
        <div className="p-3 bg-error-container/30 border border-error/30 rounded-xl text-xs text-error flex items-center gap-2">
          <AlertTriangle className="w-4 h-4 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {loadingData ? (
        <div className="p-12 text-center text-xs text-on-surface-variant flex items-center justify-center gap-2">
          <RefreshCw className="w-4 h-4 animate-spin text-primary" />
          <span>Loading catalog &amp; customer profiles...</span>
        </div>
      ) : (
        <form onSubmit={handleSubmit} className="grid grid-cols-1 lg:grid-cols-12 gap-6">
          <div className="lg:col-span-8 bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-5">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-medium text-on-surface mb-1">Target Customer Account *</label>
                <select
                  value={customerId}
                  onChange={(e) => setCustomerId(e.target.value)}
                  className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest font-medium"
                  required
                >
                  {customers.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.companyName} ({c.contactName})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-medium text-on-surface mb-1">Quotation Date *</label>
                <input
                  type="date"
                  required
                  value={quotationDate}
                  onChange={(e) => setQuotationDate(e.target.value)}
                  className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest font-mono"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-on-surface mb-1">Valid Until Date *</label>
                <input
                  type="date"
                  required
                  value={validUntil}
                  onChange={(e) => setValidUntil(e.target.value)}
                  className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest font-mono"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-on-surface mb-1">Proposal Notes</label>
                <input
                  type="text"
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest"
                />
              </div>
            </div>

            <div className="pt-4 border-t border-surface-container">
              <div className="flex items-center justify-between mb-3">
                <h2 className="text-xs font-bold text-on-surface uppercase tracking-wider">Quotation Line Items</h2>
                <button
                  type="button"
                  onClick={handleAddItem}
                  className="text-xs text-primary font-semibold flex items-center gap-1 hover:underline"
                >
                  <Plus className="w-3.5 h-3.5" />
                  <span>Add Line Item</span>
                </button>
              </div>

              <div className="space-y-3">
                {items.map((it, idx) => (
                  <div key={idx} className="p-3 bg-surface-container-low rounded-xl border border-outline-variant/30 grid grid-cols-1 sm:grid-cols-12 gap-3 items-end">
                    <div className="sm:col-span-5">
                      <label className="block text-[10px] text-outline uppercase font-semibold mb-1">Target Plan *</label>
                      <select
                        value={it.planId}
                        onChange={(e) => handleItemChange(idx, 'planId', e.target.value)}
                        className="w-full h-8 px-2 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest"
                        required
                      >
                        {plans.map((p) => (
                          <option key={p.id} value={p.id}>
                            {p.name} ({formatCurr(p.price)}/{p.billingCycle?.toLowerCase()})
                          </option>
                        ))}
                      </select>
                    </div>

                    <div className="sm:col-span-2">
                      <label className="block text-[10px] text-outline uppercase font-semibold mb-1">Qty</label>
                      <input
                        type="number"
                        min="1"
                        value={it.quantity}
                        onChange={(e) => handleItemChange(idx, 'quantity', e.target.value)}
                        className="w-full h-8 px-2 rounded-lg border border-outline-variant/60 text-xs font-mono"
                      />
                    </div>

                    <div className="sm:col-span-2">
                      <label className="block text-[10px] text-outline uppercase font-semibold mb-1">Discount (₹)</label>
                      <input
                        type="number"
                        min="0"
                        step="0.01"
                        value={it.discountAmount}
                        onChange={(e) => handleItemChange(idx, 'discountAmount', e.target.value)}
                        className="w-full h-8 px-2 rounded-lg border border-outline-variant/60 text-xs font-mono"
                      />
                    </div>

                    <div className="sm:col-span-2">
                      <label className="block text-[10px] text-outline uppercase font-semibold mb-1">Tax (₹)</label>
                      <input
                        type="number"
                        min="0"
                        step="0.01"
                        value={it.taxAmount}
                        onChange={(e) => handleItemChange(idx, 'taxAmount', e.target.value)}
                        className="w-full h-8 px-2 rounded-lg border border-outline-variant/60 text-xs font-mono"
                      />
                    </div>

                    <div className="sm:col-span-1 flex justify-end">
                      <button
                        type="button"
                        onClick={() => handleRemoveItem(idx)}
                        disabled={items.length <= 1}
                        className="p-2 text-outline hover:text-error rounded-lg disabled:opacity-30"
                        title="Remove Item"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Pricing Summary Side Card */}
          <div className="lg:col-span-4 bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-4 h-fit">
            <h2 className="text-xs font-bold uppercase tracking-wider text-on-surface">Calculated Summary</h2>

            <div className="space-y-2 text-xs">
              <div className="flex justify-between text-on-surface-variant">
                <span>Subtotal:</span>
                <span className="font-mono">{formatCurr(subtotal)}</span>
              </div>
              <div className="flex justify-between text-emerald-700">
                <span>Discount Total:</span>
                <span className="font-mono">-{formatCurr(totalDiscount)}</span>
              </div>
              <div className="flex justify-between text-on-surface-variant">
                <span>Tax Total:</span>
                <span className="font-mono">+{formatCurr(totalTax)}</span>
              </div>
              <div className="pt-2 border-t border-surface-container flex justify-between font-bold text-sm text-on-surface">
                <span>Total Quotation:</span>
                <span className="font-mono text-primary text-base">{formatCurr(grandTotal)}</span>
              </div>
            </div>

            <button
              type="submit"
              disabled={submitting}
              className="w-full py-2.5 bg-primary hover:bg-primary-container text-white font-semibold text-xs rounded-lg flex items-center justify-center gap-1.5 shadow-sm disabled:opacity-50"
            >
              <Save className="w-3.5 h-3.5" />
              <span>{submitting ? 'Generating Quotation...' : 'Issue Draft Quotation'}</span>
            </button>
          </div>
        </form>
      )}
    </div>
  );
}
