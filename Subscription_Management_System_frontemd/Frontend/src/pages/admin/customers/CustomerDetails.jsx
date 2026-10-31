import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Building, Mail, Phone, RefreshCw, Receipt, CreditCard, Sparkles, CheckCircle2, AlertTriangle } from 'lucide-react';
import { customerService } from '../../../services/customerService';

export default function CustomerDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [customer, setCustomer] = useState(null);
  const [activeTab, setActiveTab] = useState('subscriptions');

  const [subscriptionsList, setSubscriptionsList] = useState([]);
  const [invoicesList, setInvoicesList] = useState([]);
  const [paymentsList, setPaymentsList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [tabLoading, setTabLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    setLoading(true);
    customerService.getById(id)
      .then(c => {
        setCustomer(c);
      })
      .catch(err => {
        setError(err.userMessage || 'Customer profile not found');
      })
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(() => {
    if (!id) return;
    setTabLoading(true);
    if (activeTab === 'subscriptions') {
      customerService.getSubscriptions(id)
        .then(setSubscriptionsList)
        .catch(() => setSubscriptionsList([]))
        .finally(() => setTabLoading(false));
    } else if (activeTab === 'invoices') {
      customerService.getInvoices(id)
        .then(setInvoicesList)
        .catch(() => setInvoicesList([]))
        .finally(() => setTabLoading(false));
    } else if (activeTab === 'ledger') {
      customerService.getPayments(id)
        .then(setPaymentsList)
        .catch(() => setPaymentsList([]))
        .finally(() => setTabLoading(false));
    }
  }, [id, activeTab]);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[300px] text-xs text-on-surface-variant gap-2">
        <RefreshCw className="w-5 h-5 animate-spin text-primary" />
        <span>Loading customer profile...</span>
      </div>
    );
  }

  if (error || !customer) {
    return (
      <div className="p-8 text-center text-xs text-error space-y-3">
        <AlertTriangle className="w-8 h-8 mx-auto" />
        <p>{error || 'Customer not found'}</p>
        <button
          onClick={() => navigate('/admin/customers')}
          className="px-3 py-1.5 bg-primary text-white rounded text-xs font-semibold"
        >
          Back to Directory
        </button>
      </div>
    );
  }

  const formatCurr = (val) => `₹${Number(val || 0).toLocaleString('en-IN', { maximumFractionDigits: 2 })}`;

  return (
    <div className="space-y-6">
      {/* Header Profile Bar */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate('/admin/customers')}
            className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container text-on-surface-variant"
          >
            <ArrowLeft className="w-4 h-4" />
          </button>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-xl font-bold text-on-surface">{customer.companyName}</h1>
              <span className={`px-2 py-0.5 rounded-full text-[10px] font-semibold ${
                customer.status === 'ACTIVE' ? 'bg-emerald-50 text-emerald-700' : 'bg-surface-container text-on-surface-variant'
              }`}>
                {customer.status}
              </span>
            </div>
            <p className="text-xs text-on-surface-variant font-mono">
              Contact: {customer.contactName} • {customer.email} • ID: #{customer.id}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={() => navigate(`/admin/quotations/new?customerId=${customer.id}`)}
            className="px-3 py-1.5 rounded-lg bg-surface-container border border-outline-variant/40 text-xs font-semibold text-on-surface hover:bg-surface-container-high"
          >
            Generate Quote
          </button>
          <button
            onClick={() => navigate(`/admin/customers/${customer.id}/edit`)}
            className="px-3.5 py-1.5 rounded-lg bg-primary hover:bg-primary-container text-white text-xs font-semibold shadow-sm"
          >
            Edit Profile
          </button>
        </div>
      </div>

      {/* Profile Details Strip */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] font-mono uppercase text-outline">PRIMARY PHONE</div>
          <div className="text-sm font-bold text-on-surface mt-1">{customer.phone || 'Not recorded'}</div>
          <div className="text-[10px] text-on-surface-variant mt-0.5">Corporate telephone</div>
        </div>
        <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] font-mono uppercase text-outline">LOCATION</div>
          <div className="text-sm font-bold text-primary mt-1">{[customer.city, customer.country].filter(Boolean).join(', ') || 'India'}</div>
          <div className="text-[10px] text-on-surface-variant mt-0.5">{customer.address || 'Standard profile'}</div>
        </div>
        <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] font-mono uppercase text-outline">REGISTERED DATE</div>
          <div className="text-sm font-bold text-on-surface mt-1 font-mono">
            {customer.createdAt ? new Date(customer.createdAt).toLocaleDateString() : '—'}
          </div>
          <div className="text-[10px] text-on-surface-variant mt-0.5">Account creation timestamp</div>
        </div>
        <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] font-mono uppercase text-outline">STATUS</div>
          <div className="text-sm font-bold text-emerald-700 mt-1">{customer.status}</div>
          <div className="text-[10px] text-emerald-600 mt-0.5">Operational account</div>
        </div>
      </div>

      {/* Subscriptions / Invoices / Payments Tabs */}
      <div className="bg-surface-container-lowest border border-outline-variant/40 rounded-xl shadow-elevation-1 overflow-hidden">
        <div className="flex border-b border-surface-container px-4">
          {[
            { id: 'subscriptions', label: 'Subscriptions', icon: RefreshCw },
            { id: 'invoices', label: 'Invoices & Billing', icon: Receipt },
            { id: 'ledger', label: 'Payment Ledger', icon: CreditCard }
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

        <div className="p-4">
          {tabLoading ? (
            <div className="py-8 text-center text-xs text-on-surface-variant flex items-center justify-center gap-2">
              <RefreshCw className="w-4 h-4 animate-spin text-primary" />
              <span>Fetching {activeTab}...</span>
            </div>
          ) : (
            <>
              {activeTab === 'subscriptions' && (
                <div className="space-y-3">
                  {subscriptionsList.length > 0 ? (
                    subscriptionsList.map(sub => (
                      <div
                        key={sub.id}
                        onClick={() => navigate(`/admin/subscriptions/${sub.id}`)}
                        className="p-3.5 rounded-lg border border-outline-variant/30 bg-surface-container-low flex items-center justify-between cursor-pointer hover:bg-surface-container"
                      >
                        <div>
                          <div className="text-xs font-bold text-on-surface font-mono">{sub.subscriptionNumber}</div>
                          <div className="text-[11px] text-on-surface-variant">
                            Start: {sub.startDate} • Next Bill: {sub.nextBillingDate || '—'}
                          </div>
                        </div>
                        <div className="text-right">
                          <div className="text-xs font-mono font-bold text-primary">{formatCurr(sub.totalAmount)}</div>
                          <span className="text-[10px] text-emerald-700 font-semibold uppercase">{sub.status}</span>
                        </div>
                      </div>
                    ))
                  ) : (
                    <div className="py-8 text-center text-xs text-on-surface-variant">
                      No subscriptions found for this customer.
                    </div>
                  )}
                </div>
              )}

              {activeTab === 'invoices' && (
                <div className="space-y-2">
                  {invoicesList.length > 0 ? (
                    invoicesList.map(inv => (
                      <div
                        key={inv.id}
                        onClick={() => navigate(`/admin/invoices/${inv.id}`)}
                        className="p-3 rounded-lg border border-outline-variant/30 flex items-center justify-between text-xs cursor-pointer hover:bg-surface-container"
                      >
                        <div>
                          <span className="font-mono font-bold text-on-surface">{inv.invoiceNumber}</span>
                          <span className="text-on-surface-variant ml-3">Due: {inv.dueDate}</span>
                        </div>
                        <div className="flex items-center gap-3">
                          <span className="font-mono font-bold">{formatCurr(inv.totalAmount)}</span>
                          <span className={`px-2 py-0.5 rounded-full text-[10px] font-semibold ${
                            inv.status === 'PAID' ? 'bg-emerald-50 text-emerald-700' :
                            inv.status === 'OVERDUE' ? 'bg-rose-50 text-rose-700' : 'bg-surface-container text-on-surface-variant'
                          }`}>
                            {inv.status}
                          </span>
                        </div>
                      </div>
                    ))
                  ) : (
                    <div className="py-8 text-center text-xs text-on-surface-variant">
                      No invoices found for this customer.
                    </div>
                  )}
                </div>
              )}

              {activeTab === 'ledger' && (
                <div className="space-y-2">
                  {paymentsList.length > 0 ? (
                    paymentsList.map(pay => (
                      <div
                        key={pay.id}
                        onClick={() => navigate(`/admin/payments/${pay.id}`)}
                        className="p-3 rounded-lg border border-outline-variant/30 flex items-center justify-between text-xs cursor-pointer hover:bg-surface-container"
                      >
                        <div>
                          <span className="font-mono font-bold text-on-surface">{pay.paymentNumber}</span>
                          <span className="text-on-surface-variant ml-3 font-mono">{pay.paymentMethod}</span>
                        </div>
                        <div className="flex items-center gap-3">
                          <span className="font-mono font-bold text-emerald-700">{formatCurr(pay.amount)}</span>
                          <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-50 text-emerald-700">
                            {pay.status}
                          </span>
                        </div>
                      </div>
                    ))
                  ) : (
                    <div className="py-8 text-center text-xs text-on-surface-variant">
                      No payment settlements recorded for this customer yet.
                    </div>
                  )}
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
}
