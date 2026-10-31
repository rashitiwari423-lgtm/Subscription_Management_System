import React, { useState, useEffect } from 'react';
import { ArrowLeft, PlusCircle, CheckCircle2, ArrowRight } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { quotationService } from '../../../services/quotationService';

export default function CreateSubscription() {
  const navigate = useNavigate();
  const [acceptedQuotations, setAcceptedQuotations] = useState([]);
  const [selectedQuotationId, setSelectedQuotationId] = useState('');
  const [loading, setLoading] = useState(true);
  const [converting, setConverting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    async function loadQuotations() {
      try {
        setLoading(true);
        const data = await quotationService.getAll({ status: 'ACCEPTED' });
        const list = Array.isArray(data) ? data : (data?.content || []);
        setAcceptedQuotations(list);
        if (list.length > 0) {
          setSelectedQuotationId(list[0].id.toString());
        }
      } catch (err) {
        console.error('Failed to load accepted quotations:', err);
      } finally {
        setLoading(false);
      }
    }
    loadQuotations();
  }, []);

  const handleConvert = async (e) => {
    e.preventDefault();
    if (!selectedQuotationId) {
      setError('Please select an accepted quotation to activate');
      return;
    }
    try {
      setConverting(true);
      setError('');
      const sub = await quotationService.convert(selectedQuotationId);
      navigate(`/admin/subscriptions/${sub.id}`);
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to convert quotation to subscription');
    } finally {
      setConverting(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate('/admin/subscriptions')}
          className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container"
        >
          <ArrowLeft className="w-4 h-4" />
        </button>
        <div>
          <h1 className="text-xl font-bold text-on-surface">Activate Client Subscription</h1>
          <p className="text-xs text-on-surface-variant">
            Convert an approved quotation into an active recurring billing contract.
          </p>
        </div>
      </div>

      {error && (
        <div className="p-3 bg-error-container/20 border border-error/30 text-error rounded-xl text-xs flex justify-between">
          <span>{error}</span>
          <button onClick={() => setError('')} className="font-bold">×</button>
        </div>
      )}

      <div className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-6">
        <div className="p-4 rounded-xl bg-primary/5 border border-primary/20 space-y-2">
          <div className="flex items-center gap-2 text-primary font-semibold text-xs">
            <CheckCircle2 className="w-4 h-4" />
            <span>Compliance & Lifecycle Rule</span>
          </div>
          <p className="text-xs text-on-surface-variant leading-relaxed">
            In SubscriptIQ, subscriptions must originate from an approved, legally-binding quotation to ensure full audit compliance, itemized pricing snapshots, and tax validation.
          </p>
        </div>

        {acceptedQuotations.length > 0 ? (
          <form onSubmit={handleConvert} className="space-y-4">
            <div>
              <label className="block text-xs font-medium text-on-surface mb-1">
                Select Approved Quotation *
              </label>
              <select
                value={selectedQuotationId}
                onChange={(e) => setSelectedQuotationId(e.target.value)}
                className="w-full h-10 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest font-medium"
              >
                {acceptedQuotations.map((q) => (
                  <option key={q.id} value={q.id}>
                    {q.quotationNumber || `QT-${q.id}`} — {q.customerCompanyName || `Customer #${q.customerId}`} (Total: ₹{Number(q.totalAmount || 0).toLocaleString('en-IN')})
                  </option>
                ))}
              </select>
            </div>

            <div className="pt-4 border-t border-surface-container flex items-center justify-end gap-2">
              <button
                type="button"
                onClick={() => navigate('/admin/quotations/new')}
                className="px-4 py-2 border border-outline-variant/40 rounded-lg text-xs font-semibold text-on-surface-variant hover:bg-surface-container"
              >
                Create New Quotation
              </button>
              <button
                type="submit"
                disabled={converting}
                className="px-4 py-2 bg-primary hover:bg-primary-container text-white rounded-lg text-xs font-semibold flex items-center gap-1.5 shadow-sm"
              >
                <span>{converting ? 'Activating...' : 'Convert & Launch Subscription'}</span>
                <ArrowRight className="w-3.5 h-3.5" />
              </button>
            </div>
          </form>
        ) : (
          <div className="text-center py-6 space-y-4">
            <p className="text-xs text-on-surface-variant">
              No accepted quotations are currently awaiting conversion.
            </p>
            <button
              onClick={() => navigate('/admin/quotations/new')}
              className="inline-flex items-center gap-1.5 px-4 py-2 bg-primary hover:bg-primary-container text-white text-xs font-semibold rounded-lg shadow-sm"
            >
              <PlusCircle className="w-4 h-4" />
              <span>Create New Quotation</span>
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
