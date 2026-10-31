import React, { useState, useEffect } from 'react';
import {
  AlertTriangle,
  RefreshCw,
  CheckCircle2,
  Send,
  Calendar,
  DollarSign,
  ShieldAlert,
  Search,
  Filter,
  Sliders,
  History,
  FileText,
  User,
  Clock,
  Check,
  X,
  CreditCard,
  ArrowUpRight,
  ChevronRight
} from 'lucide-react';
import dunningService from '../../../services/dunningService';
import { useNavigate } from 'react-router-dom';

export default function DunningPage() {
  const navigate = useNavigate();
  const [cases, setCases] = useState([]);
  const [stats, setStats] = useState({
    totalOverdueInvoices: 0,
    totalOutstandingAmount: 0,
    uniqueOverdueCustomers: 0,
    amountRecovered: 0,
    recoveryRate: 88,
    failedPaymentCount: 0,
    upcomingReminders: 0
  });

  const [activeTab, setActiveTab] = useState('cases'); // 'cases' | 'rules' | 'history'
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [agingFilter, setAgingFilter] = useState('ALL');

  // Modals state
  const [selectedCase, setSelectedCase] = useState(null);
  const [modalType, setModalType] = useState(null); // 'payment' | 'reminder' | 'promise' | 'escalate' | 'details'
  const [actionAmount, setActionAmount] = useState('');
  const [actionMethod, setActionMethod] = useState('Bank Transfer');
  const [actionPromiseDate, setActionPromiseDate] = useState('');
  const [actionNotes, setActionNotes] = useState('');
  const [toastMessage, setToastMessage] = useState('');

  // Rules state
  const [rules, setRules] = useState({
    firstReminderDays: 3,
    secondReminderDays: 7,
    finalReminderDays: 14,
    escalationDays: 21,
    autoRetryEnabled: true
  });

  const loadData = async () => {
    const data = await dunningService.getCases();
    const st = await dunningService.getStats();
    setCases(data);
    setStats(st);
    setRules(dunningService.getRules());
  };

  useEffect(() => {
    loadData();
  }, []);

  const showToast = (msg) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(''), 3500);
  };

  const handleSendReminder = async (item, type = 'Email') => {
    await dunningService.sendSimulatedReminder(item.id, type);
    await loadData();
    showToast(`Simulated ${type} reminder dispatched to ${item.customer} (${item.invoiceId}).`);
    setModalType(null);
  };

  const handleRecordPayment = async (e) => {
    e.preventDefault();
    if (!selectedCase || !actionAmount) return;
    const num = Number(actionAmount);
    if (isNaN(num) || num <= 0) {
      alert('Please enter a valid payment amount.');
      return;
    }
    await dunningService.recordPartialOrFullPayment(selectedCase.id, num, actionMethod);
    await loadData();
    showToast(`Payment of ₹${num.toLocaleString('en-IN')} recorded for ${selectedCase.invoiceId}.`);
    setModalType(null);
    setActionAmount('');
  };

  const handleSetPromise = async (e) => {
    e.preventDefault();
    if (!selectedCase || !actionPromiseDate) return;
    await dunningService.setPaymentPromise(selectedCase.id, actionPromiseDate, actionNotes);
    await loadData();
    showToast(`Payment promise recorded for ${selectedCase.customer} on ${actionPromiseDate}.`);
    setModalType(null);
    setActionPromiseDate('');
    setActionNotes('');
  };

  const handleEscalate = async (e) => {
    e.preventDefault();
    if (!selectedCase) return;
    await dunningService.escalateCase(selectedCase.id, actionNotes);
    await loadData();
    showToast(`Account ${selectedCase.customer} flagged as ESCALATED with credit hold.`);
    setModalType(null);
    setActionNotes('');
  };

  const handleSaveRules = (e) => {
    e.preventDefault();
    dunningService.saveRules(rules);
    showToast('Dunning automated reminder rules updated in system.');
  };

  // Filtered list
  const filteredCases = cases.filter((c) => {
    const matchesSearch =
      c.customer.toLowerCase().includes(searchTerm.toLowerCase()) ||
      c.invoiceId.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesStatus = statusFilter === 'ALL' || c.recoveryStatus === statusFilter;

    let matchesAging = true;
    if (agingFilter === '0-30') matchesAging = c.daysOverdue <= 30;
    else if (agingFilter === '31-60') matchesAging = c.daysOverdue > 30 && c.daysOverdue <= 60;
    else if (agingFilter === '60+') matchesAging = c.daysOverdue > 60;

    return matchesSearch && matchesStatus && matchesAging;
  });

  return (
    <div className="space-y-6">
      {/* Toast Notification */}
      {toastMessage && (
        <div className="fixed bottom-6 right-6 z-50 bg-on-surface text-surface px-4 py-3 rounded-xl shadow-elevation-3 flex items-center gap-2.5 text-xs font-medium animate-bounce">
          <CheckCircle2 className="w-4 h-4 text-emerald-400" />
          <span>{toastMessage}</span>
        </div>
      )}

      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-bold text-on-surface">Dunning & Revenue Recovery</h1>
            <span className="text-[10px] font-mono font-bold bg-amber-50 text-amber-800 border border-amber-200 px-2 py-0.5 rounded-full">
              FINANCIAL OPS
            </span>
          </div>
          <p className="text-xs text-on-surface-variant mt-0.5">
            Automated retry sequences, payment reminders, promise-to-pay tracking, and collection escalations.
          </p>
        </div>

        {/* Tab Switcher */}
        <div className="flex items-center bg-surface-container-low p-1 rounded-xl text-xs border border-surface-container">
          <button
            onClick={() => setActiveTab('cases')}
            className={`px-3 py-1.5 rounded-lg font-semibold transition-all ${
              activeTab === 'cases'
                ? 'bg-primary text-white shadow-xs'
                : 'text-on-surface-variant hover:text-on-surface'
            }`}
          >
            Overdue Cases ({cases.length})
          </button>
          <button
            onClick={() => setActiveTab('rules')}
            className={`px-3 py-1.5 rounded-lg font-semibold transition-all ${
              activeTab === 'rules'
                ? 'bg-primary text-white shadow-xs'
                : 'text-on-surface-variant hover:text-on-surface'
            }`}
          >
            Dunning Rules
          </button>
        </div>
      </div>

      {/* Part A: Dunning Dashboard KPI Strip */}
      <div className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-7 gap-3">
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-rose-700 font-bold">OVERDUE INVOICES</div>
          <div className="text-xl font-extrabold text-rose-700 mt-1 font-tabular">
            {stats.totalOverdueInvoices}
          </div>
          <div className="text-[10px] text-on-surface-variant mt-0.5">Active recovery cases</div>
        </div>

        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-outline font-bold">OUTSTANDING ARREARS</div>
          <div className="text-xl font-extrabold text-on-surface mt-1 font-tabular">
            ₹{stats.totalOutstandingAmount.toLocaleString('en-IN')}
          </div>
          <div className="text-[10px] text-rose-600 mt-0.5">Unsettled balance</div>
        </div>

        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-outline font-bold">OVERDUE CLIENTS</div>
          <div className="text-xl font-extrabold text-on-surface mt-1">
            {stats.uniqueOverdueCustomers} Orgs
          </div>
          <div className="text-[10px] text-on-surface-variant mt-0.5">Under risk watch</div>
        </div>

        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-emerald-700 font-bold">RECOVERY RATE</div>
          <div className="text-xl font-extrabold text-emerald-700 mt-1 font-tabular">
            {stats.recoveryRate}%
          </div>
          <div className="text-[10px] text-emerald-600 mt-0.5">High recovery speed</div>
        </div>

        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-emerald-700 font-bold">AMOUNT RECOVERED</div>
          <div className="text-xl font-extrabold text-emerald-700 mt-1 font-tabular">
            ₹{stats.amountRecovered.toLocaleString('en-IN')}
          </div>
          <div className="text-[10px] text-emerald-600 mt-0.5">This billing cycle</div>
        </div>

        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-outline font-bold">FAILED ATTEMPTS</div>
          <div className="text-xl font-extrabold text-on-surface mt-1 font-tabular">
            {stats.failedPaymentCount}
          </div>
          <div className="text-[10px] text-on-surface-variant mt-0.5">Gateway retries</div>
        </div>

        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-primary font-bold">DUE REMINDERS</div>
          <div className="text-xl font-extrabold text-primary mt-1 font-tabular">
            {stats.upcomingReminders}
          </div>
          <div className="text-[10px] text-primary mt-0.5">Scheduled actions</div>
        </div>
      </div>

      {/* Main Content Area */}
      {activeTab === 'cases' && (
        <div className="space-y-4">
          {/* Part B: Filters & Search */}
          <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl flex flex-col md:flex-row items-center justify-between gap-3">
            <div className="relative w-full md:w-80">
              <Search className="w-3.5 h-3.5 absolute left-3 top-2.5 text-outline" />
              <input
                type="text"
                placeholder="Search by customer name or invoice #..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full h-8 pl-9 pr-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest text-on-surface focus:outline-none focus:border-primary-container"
              />
            </div>

            <div className="flex flex-wrap items-center gap-2 w-full md:w-auto">
              <div className="flex items-center gap-1.5 text-xs text-on-surface-variant">
                <Filter className="w-3.5 h-3.5 text-outline" />
                <span>Status:</span>
              </div>
              <select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                className="h-8 px-2.5 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest text-on-surface"
              >
                <option value="ALL">All Recovery Statuses</option>
                <option value="Upcoming">Upcoming</option>
                <option value="Reminder Sent">Reminder Sent</option>
                <option value="Payment Promise">Payment Promise</option>
                <option value="Partially Recovered">Partially Recovered</option>
                <option value="Recovered">Recovered (Paid)</option>
                <option value="Escalated">Escalated</option>
              </select>

              <select
                value={agingFilter}
                onChange={(e) => setAgingFilter(e.target.value)}
                className="h-8 px-2.5 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest text-on-surface"
              >
                <option value="ALL">All Aging Windows</option>
                <option value="0-30">1 - 30 Days Overdue</option>
                <option value="31-60">31 - 60 Days Overdue</option>
                <option value="60+">&gt; 60 Days Overdue</option>
              </select>
            </div>
          </div>

          {/* Part B & C: Overdue Invoices Management Table */}
          <div className="bg-surface-container-lowest border border-outline-variant/40 rounded-xl shadow-elevation-1 overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead className="bg-surface-container-low text-on-surface-variant uppercase text-[10px] tracking-wider border-b border-outline-variant/20 font-semibold">
                  <tr>
                    <th className="p-3.5">Invoice #</th>
                    <th className="p-3.5">Customer Name</th>
                    <th className="p-3.5">Invoice Amount</th>
                    <th className="p-3.5">Paid Amount</th>
                    <th className="p-3.5">Outstanding Amount</th>
                    <th className="p-3.5">Due Date</th>
                    <th className="p-3.5">Days Overdue</th>
                    <th className="p-3.5">Recovery Status</th>
                    <th className="p-3.5 text-right">Available Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-surface-container-low">
                  {filteredCases.length === 0 ? (
                    <tr>
                      <td colSpan="9" className="p-8 text-center text-on-surface-variant">
                        No overdue recovery cases matched your search filter criteria.
                      </td>
                    </tr>
                  ) : (
                    filteredCases.map((item) => {
                      const isRecovered = item.recoveryStatus === 'Recovered';
                      const isEscalated = item.recoveryStatus === 'Escalated';
                      const isPromise = item.recoveryStatus === 'Payment Promise';

                      return (
                        <tr key={item.id} className="hover:bg-surface-container-low/60 transition-colors">
                          <td className="p-3.5 font-mono font-bold text-primary">
                            <button
                              onClick={() => navigate(`/admin/invoices/${item.invoiceId}`)}
                              className="hover:underline flex items-center gap-1"
                            >
                              <span>{item.invoiceId}</span>
                              <ArrowUpRight className="w-3 h-3 opacity-60" />
                            </button>
                          </td>
                          <td className="p-3.5">
                            <div className="font-bold text-on-surface">{item.customer}</div>
                            <div className="text-[10px] text-on-surface-variant font-mono">{item.failureReason}</div>
                          </td>
                          <td className="p-3.5 font-mono font-semibold">
                            ₹{item.invoiceAmount.toLocaleString('en-IN')}
                          </td>
                          <td className="p-3.5 font-mono text-emerald-700">
                            ₹{(item.paidAmount || 0).toLocaleString('en-IN')}
                          </td>
                          <td className="p-3.5 font-mono font-bold text-rose-700">
                            ₹{item.outstandingAmount.toLocaleString('en-IN')}
                          </td>
                          <td className="p-3.5 font-mono text-on-surface-variant">{item.dueDate}</td>
                          <td className="p-3.5">
                            <span className={`px-2 py-0.5 rounded-full text-[10px] font-mono font-bold ${
                              item.daysOverdue > 60
                                ? 'bg-rose-100 text-rose-800'
                                : item.daysOverdue > 30
                                ? 'bg-amber-100 text-amber-800'
                                : 'bg-surface-container text-on-surface-variant'
                            }`}>
                              {item.daysOverdue} Days
                            </span>
                          </td>
                          <td className="p-3.5">
                            <span
                              className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-semibold ${
                                isRecovered
                                  ? 'bg-emerald-50 text-emerald-700'
                                  : isEscalated
                                  ? 'bg-rose-50 text-rose-700'
                                  : isPromise
                                  ? 'bg-primary/10 text-primary'
                                  : 'bg-amber-50 text-amber-700'
                              }`}
                            >
                              {isRecovered && <Check className="w-3 h-3" />}
                              {isEscalated && <AlertTriangle className="w-3 h-3" />}
                              <span>{item.recoveryStatus}</span>
                            </span>
                          </td>
                          {/* Part E: Recovery Actions */}
                          <td className="p-3.5 text-right">
                            <div className="flex items-center justify-end gap-1.5">
                              {/* Send Reminder */}
                              <button
                                title="Send Simulated Email Reminder"
                                onClick={() => handleSendReminder(item, 'Email')}
                                className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container text-primary"
                              >
                                <Send className="w-3.5 h-3.5" />
                              </button>

                              {/* Record Payment */}
                              <button
                                title="Record Settlement or Partial Payment"
                                onClick={() => {
                                  setSelectedCase(item);
                                  setActionAmount(item.outstandingAmount);
                                  setModalType('payment');
                                }}
                                className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-emerald-50 text-emerald-700"
                              >
                                <DollarSign className="w-3.5 h-3.5" />
                              </button>

                              {/* Mark Payment Promise */}
                              <button
                                title="Set Promised Settlement Date"
                                onClick={() => {
                                  setSelectedCase(item);
                                  setActionPromiseDate(item.paymentPromiseDate || '2024-03-20');
                                  setModalType('promise');
                                }}
                                className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container text-on-surface-variant"
                              >
                                <Calendar className="w-3.5 h-3.5" />
                              </button>

                              {/* Escalate */}
                              <button
                                title="Escalate to Legal & Credit Hold"
                                onClick={() => {
                                  setSelectedCase(item);
                                  setModalType('escalate');
                                }}
                                className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-rose-50 text-rose-700"
                              >
                                <ShieldAlert className="w-3.5 h-3.5" />
                              </button>

                              {/* Audit Case Details */}
                              <button
                                title="Inspect Dunning History"
                                onClick={() => {
                                  setSelectedCase(item);
                                  setModalType('details');
                                }}
                                className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container text-outline"
                              >
                                <History className="w-3.5 h-3.5" />
                              </button>
                            </div>
                          </td>
                        </tr>
                      );
                    })
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* Part F: Dunning Rules Interface */}
      {activeTab === 'rules' && (
        <div className="max-w-2xl bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-5">
          <div>
            <h2 className="text-base font-bold text-on-surface">Automated Dunning & Reminder Cadence</h2>
            <p className="text-xs text-on-surface-variant mt-0.5">
              Specify schedule thresholds for gentle reminders, critical warnings, and operations escalation.
            </p>
          </div>

          <form onSubmit={handleSaveRules} className="space-y-4 text-xs">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block font-medium text-on-surface mb-1">First Reminder Trigger</label>
                <div className="flex items-center gap-2">
                  <input
                    type="number"
                    value={rules.firstReminderDays}
                    onChange={(e) => setRules({ ...rules, firstReminderDays: Number(e.target.value) })}
                    className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 font-mono"
                  />
                  <span className="text-on-surface-variant whitespace-nowrap">days after due date</span>
                </div>
              </div>

              <div>
                <label className="block font-medium text-on-surface mb-1">Second Warning Trigger</label>
                <div className="flex items-center gap-2">
                  <input
                    type="number"
                    value={rules.secondReminderDays}
                    onChange={(e) => setRules({ ...rules, secondReminderDays: Number(e.target.value) })}
                    className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 font-mono"
                  />
                  <span className="text-on-surface-variant whitespace-nowrap">days after due date</span>
                </div>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block font-medium text-on-surface mb-1">Final Notice Trigger</label>
                <div className="flex items-center gap-2">
                  <input
                    type="number"
                    value={rules.finalReminderDays}
                    onChange={(e) => setRules({ ...rules, finalReminderDays: Number(e.target.value) })}
                    className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 font-mono"
                  />
                  <span className="text-on-surface-variant whitespace-nowrap">days after due date</span>
                </div>
              </div>

              <div>
                <label className="block font-medium text-on-surface mb-1">Collections Escalation</label>
                <div className="flex items-center gap-2">
                  <input
                    type="number"
                    value={rules.escalationDays}
                    onChange={(e) => setRules({ ...rules, escalationDays: Number(e.target.value) })}
                    className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 font-mono"
                  />
                  <span className="text-on-surface-variant whitespace-nowrap">days after due date</span>
                </div>
              </div>
            </div>

            <div className="p-3 rounded-xl bg-surface-container-low border border-surface-container flex items-center justify-between">
              <div>
                <div className="font-bold text-on-surface">Automated UPI / Card AutoPay Retry Waterfall</div>
                <div className="text-[11px] text-on-surface-variant">Attempt intelligent background retries every 72 hours.</div>
              </div>
              <input
                type="checkbox"
                checked={rules.autoRetryEnabled}
                onChange={(e) => setRules({ ...rules, autoRetryEnabled: e.target.checked })}
                className="w-4 h-4 rounded accent-primary cursor-pointer"
              />
            </div>

            <div className="pt-3 border-t border-surface-container flex justify-end">
              <button
                type="submit"
                className="px-4 py-2 bg-primary hover:bg-primary-container text-white text-xs font-semibold rounded-lg shadow-sm"
              >
                Save Dunning Rules
              </button>
            </div>
          </form>
        </div>
      )}

      {/* MODAL 1: Record Payment */}
      {modalType === 'payment' && selectedCase && (
        <div className="fixed inset-0 z-50 bg-black/40 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-surface-container-lowest max-w-md w-full p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-3 space-y-4">
            <div className="flex items-center justify-between border-b border-surface-container pb-3">
              <div className="flex items-center gap-2">
                <DollarSign className="w-4 h-4 text-emerald-700" />
                <h3 className="font-bold text-sm text-on-surface">Record Settlement / Partial Payment</h3>
              </div>
              <button onClick={() => setModalType(null)} className="text-outline hover:text-on-surface">
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="text-xs bg-surface-container-low p-3 rounded-xl space-y-1">
              <div>Customer: <strong className="text-on-surface">{selectedCase.customer}</strong></div>
              <div>Invoice: <strong className="text-on-surface font-mono">{selectedCase.invoiceId}</strong></div>
              <div>Outstanding Amount: <strong className="text-rose-700 font-mono">₹{selectedCase.outstandingAmount.toLocaleString('en-IN')}</strong></div>
            </div>

            <form onSubmit={handleRecordPayment} className="space-y-3 text-xs">
              <div>
                <label className="block font-medium text-on-surface mb-1">Amount to Settle (₹) *</label>
                <input
                  type="number"
                  required
                  max={selectedCase.outstandingAmount}
                  value={actionAmount}
                  onChange={(e) => setActionAmount(e.target.value)}
                  className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 font-mono text-sm font-bold text-on-surface"
                />
              </div>

              <div>
                <label className="block font-medium text-on-surface mb-1">Payment Method Rail</label>
                <select
                  value={actionMethod}
                  onChange={(e) => setActionMethod(e.target.value)}
                  className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest"
                >
                  <option value="Bank Transfer">Bank Transfer (RTGS / NEFT)</option>
                  <option value="UPI AutoPay">UPI AutoPay / e-NACH</option>
                  <option value="Corporate Card">Corporate Card</option>
                  <option value="Cheque">Bank Demand Draft / Cheque</option>
                </select>
              </div>

              <div className="pt-3 border-t border-surface-container flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setModalType(null)}
                  className="px-3 py-1.5 border border-outline-variant/40 rounded-lg text-on-surface-variant font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-1.5 bg-emerald-700 hover:bg-emerald-800 text-white rounded-lg font-semibold"
                >
                  Confirm Settlement
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL 2: Payment Promise */}
      {modalType === 'promise' && selectedCase && (
        <div className="fixed inset-0 z-50 bg-black/40 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-surface-container-lowest max-w-md w-full p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-3 space-y-4">
            <div className="flex items-center justify-between border-b border-surface-container pb-3">
              <div className="flex items-center gap-2">
                <Calendar className="w-4 h-4 text-primary" />
                <h3 className="font-bold text-sm text-on-surface">Record Customer Payment Promise</h3>
              </div>
              <button onClick={() => setModalType(null)} className="text-outline hover:text-on-surface">
                <X className="w-4 h-4" />
              </button>
            </div>

            <form onSubmit={handleSetPromise} className="space-y-3 text-xs">
              <div>
                <label className="block font-medium text-on-surface mb-1">Promised Settlement Date *</label>
                <input
                  type="date"
                  required
                  value={actionPromiseDate}
                  onChange={(e) => setActionPromiseDate(e.target.value)}
                  className="w-full h-9 px-3 rounded-lg border border-outline-variant/60 font-mono"
                />
              </div>

              <div>
                <label className="block font-medium text-on-surface mb-1">Agreement Notes / Memo</label>
                <textarea
                  rows="3"
                  value={actionNotes}
                  onChange={(e) => setActionNotes(e.target.value)}
                  placeholder="e.g. Finance director confirmed wire scheduled for mid-month."
                  className="w-full p-2.5 rounded-lg border border-outline-variant/60"
                />
              </div>

              <div className="pt-3 border-t border-surface-container flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setModalType(null)}
                  className="px-3 py-1.5 border border-outline-variant/40 rounded-lg text-on-surface-variant font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-1.5 bg-primary text-white rounded-lg font-semibold"
                >
                  Save Promise
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL 3: Escalation */}
      {modalType === 'escalate' && selectedCase && (
        <div className="fixed inset-0 z-50 bg-black/40 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-surface-container-lowest max-w-md w-full p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-3 space-y-4">
            <div className="flex items-center justify-between border-b border-surface-container pb-3">
              <div className="flex items-center gap-2 text-rose-700">
                <ShieldAlert className="w-4 h-4" />
                <h3 className="font-bold text-sm">Escalate Overdue Case</h3>
              </div>
              <button onClick={() => setModalType(null)} className="text-outline hover:text-on-surface">
                <X className="w-4 h-4" />
              </button>
            </div>

            <p className="text-xs text-on-surface-variant">
              Escalating will flag <strong>{selectedCase.customer}</strong> on credit hold and trigger a high-priority review by legal operations.
            </p>

            <form onSubmit={handleEscalate} className="space-y-3 text-xs">
              <div>
                <label className="block font-medium text-on-surface mb-1">Escalation Reason *</label>
                <textarea
                  required
                  rows="3"
                  value={actionNotes}
                  onChange={(e) => setActionNotes(e.target.value)}
                  placeholder="Detail failure response, unresponsiveness or breached payment promises..."
                  className="w-full p-2.5 rounded-lg border border-outline-variant/60"
                />
              </div>

              <div className="pt-3 border-t border-surface-container flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setModalType(null)}
                  className="px-3 py-1.5 border border-outline-variant/40 rounded-lg text-on-surface-variant font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-1.5 bg-rose-700 hover:bg-rose-800 text-white rounded-lg font-semibold"
                >
                  Confirm Escalation
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL 4: Case History & Audit */}
      {modalType === 'details' && selectedCase && (
        <div className="fixed inset-0 z-50 bg-black/40 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-surface-container-lowest max-w-lg w-full p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-3 space-y-4">
            <div className="flex items-center justify-between border-b border-surface-container pb-3">
              <div>
                <h3 className="font-bold text-sm text-on-surface">{selectedCase.customer} • Recovery Telemetry</h3>
                <span className="text-[10px] font-mono text-outline">{selectedCase.invoiceId}</span>
              </div>
              <button onClick={() => setModalType(null)} className="text-outline hover:text-on-surface">
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="text-xs space-y-3">
              <div className="p-3 rounded-xl bg-surface-container-low space-y-1">
                <div className="flex justify-between">
                  <span>Gross Invoice:</span>
                  <span className="font-mono font-bold">₹{selectedCase.invoiceAmount.toLocaleString('en-IN')}</span>
                </div>
                <div className="flex justify-between text-emerald-700">
                  <span>Settled Amount:</span>
                  <span className="font-mono font-bold">₹{(selectedCase.paidAmount || 0).toLocaleString('en-IN')}</span>
                </div>
                <div className="flex justify-between text-rose-700 font-bold pt-1 border-t border-surface-container">
                  <span>Balance Due:</span>
                  <span className="font-mono">₹{selectedCase.outstandingAmount.toLocaleString('en-IN')}</span>
                </div>
              </div>

              <div>
                <h4 className="font-bold text-on-surface uppercase text-[10px] tracking-wider mb-2">Simulated Reminders History</h4>
                <div className="space-y-1.5 max-h-44 overflow-y-auto">
                  {(selectedCase.remindersHistory || []).map((rem) => (
                    <div key={rem.id} className="p-2 rounded-lg border border-surface-container bg-surface-container-low/40 flex items-center justify-between text-[11px]">
                      <div>
                        <div className="font-semibold text-on-surface">{rem.label}</div>
                        <div className="text-[10px] text-on-surface-variant font-mono">{rem.type} • {rem.date}</div>
                      </div>
                      <span className="text-[9px] font-bold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-full">
                        {rem.status}
                      </span>
                    </div>
                  ))}
                </div>
              </div>

              {selectedCase.notes && (
                <div className="p-2.5 rounded-lg bg-surface-container-low text-[11px]">
                  <strong className="text-on-surface">Operational Notes:</strong>
                  <p className="text-on-surface-variant mt-0.5">{selectedCase.notes}</p>
                </div>
              )}
            </div>

            <div className="pt-3 border-t border-surface-container flex justify-end">
              <button
                onClick={() => setModalType(null)}
                className="px-4 py-1.5 bg-surface-container hover:bg-surface-container-high rounded-lg font-semibold text-xs text-on-surface"
              >
                Close Audit Record
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
