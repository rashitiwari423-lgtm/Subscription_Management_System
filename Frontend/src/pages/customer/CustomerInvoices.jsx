import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Receipt,
  Search,
  Filter,
  ArrowLeft,
  ArrowUpRight,
  Download,
  Printer,
  CheckCircle2,
  AlertTriangle,
  Clock,
  X,
  CreditCard,
  Building,
  FileText
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { invoices } from '../../data/mockData';

export default function CustomerInvoices() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const customerCompany = user?.company || 'Nexus Dynamics';
  const customerId = user?.customerId || 'C001';

  // Filter invoices strictly for this authenticated customer
  const rawCustomerInvoices = invoices.filter(
    i => i.customer === customerCompany || i.customerId === customerId
  );

  // Fallback demo sample invoices if list is empty
  const myInvoices = rawCustomerInvoices.length > 0 ? rawCustomerInvoices : [
    {
      id: 'INV-2024-001',
      customer: customerCompany,
      customerId: customerId,
      date: '2024-01-15',
      dueDate: '2024-01-30',
      amount: 45000,
      tax: 8100,
      status: 'Paid',
      plan: 'Enterprise Pro — Jan 2024 Billing Run',
      sacCode: '998313'
    },
    {
      id: 'INV-2024-002',
      customer: customerCompany,
      customerId: customerId,
      date: '2024-02-15',
      dueDate: '2024-03-01',
      amount: 45000,
      tax: 8100,
      status: 'Paid',
      plan: 'Enterprise Pro — Feb 2024 Billing Run',
      sacCode: '998313'
    },
    {
      id: 'INV-2024-005',
      customer: customerCompany,
      customerId: customerId,
      date: '2024-03-15',
      dueDate: '2024-03-30',
      amount: 45000,
      tax: 8100,
      status: 'Pending',
      plan: 'Enterprise Pro — Mar 2024 Current Cycle',
      sacCode: '998313'
    }
  ];

  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [selectedInvoice, setSelectedInvoice] = useState(null);
  const [printSuccess, setPrintSuccess] = useState(false);

  // Filter logic
  const filteredInvoices = myInvoices.filter((inv) => {
    const matchesSearch =
      inv.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (inv.plan && inv.plan.toLowerCase().includes(searchTerm.toLowerCase()));

    const matchesStatus =
      statusFilter === 'ALL' || inv.status.toLowerCase() === statusFilter.toLowerCase();

    return matchesSearch && matchesStatus;
  });

  const handlePrint = (inv) => {
    setPrintSuccess(true);
    setTimeout(() => setPrintSuccess(false), 3000);
    // Standard browser print dialog
    window.print();
  };

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-bold text-on-surface">Tax Invoices & Billing Statements</h1>
            <span className="text-[10px] font-mono font-bold bg-emerald-50 text-emerald-800 border border-emerald-200 px-2 py-0.5 rounded-full">
              GST COMPLIANT
            </span>
          </div>
          <p className="text-xs text-on-surface-variant mt-0.5">
            Official Indian GST tax invoices and settlement receipts for {customerCompany}.
          </p>
        </div>

        <button
          onClick={() => navigate('/customer/dashboard')}
          className="px-3 py-1.5 border border-outline-variant/40 rounded-xl text-xs font-semibold hover:bg-surface-container flex items-center gap-1.5 transition-colors"
        >
          <ArrowLeft className="w-3.5 h-3.5" />
          <span>Back to Overview</span>
        </button>
      </div>

      {/* Print Success Toast */}
      {printSuccess && (
        <div className="p-3 rounded-xl bg-emerald-50 border border-emerald-200 text-xs text-emerald-800 flex items-center gap-2 font-medium">
          <CheckCircle2 className="w-4 h-4 text-emerald-600" />
          <span>Invoice dispatched to browser print engine / PDF generator.</span>
        </div>
      )}

      {/* Filter and Search Bar */}
      <div className="p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-2xl shadow-xs flex flex-col md:flex-row items-center justify-between gap-3">
        <div className="relative w-full md:w-80">
          <Search className="w-3.5 h-3.5 absolute left-3 top-2.5 text-outline" />
          <input
            type="text"
            placeholder="Search invoice # or description..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full h-8 pl-9 pr-3 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest text-on-surface focus:outline-none focus:border-primary-container"
          />
        </div>

        <div className="flex items-center gap-2 w-full md:w-auto">
          <div className="flex items-center gap-1.5 text-xs text-on-surface-variant">
            <Filter className="w-3.5 h-3.5 text-outline" />
            <span>Filter Status:</span>
          </div>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="h-8 px-2.5 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-lowest text-on-surface"
          >
            <option value="ALL">All Statuses</option>
            <option value="Paid">Paid</option>
            <option value="Pending">Pending</option>
            <option value="Overdue">Overdue</option>
            <option value="Partially Paid">Partially Paid</option>
          </select>
        </div>
      </div>

      {/* Invoice Table */}
      <div className="bg-surface-container-lowest border border-outline-variant/40 rounded-2xl shadow-elevation-1 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-surface-container-low text-on-surface-variant font-semibold uppercase text-[10px] tracking-wider border-b border-surface-container">
              <tr>
                <th className="p-3.5">Invoice #</th>
                <th className="p-3.5">Invoice Date</th>
                <th className="p-3.5">Due Date</th>
                <th className="p-3.5">Description</th>
                <th className="p-3.5">Base Amount</th>
                <th className="p-3.5">GST (18%)</th>
                <th className="p-3.5">Total Amount</th>
                <th className="p-3.5">Payment Status</th>
                <th className="p-3.5 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-container-low">
              {filteredInvoices.length === 0 ? (
                <tr>
                  <td colSpan="9" className="p-10 text-center text-on-surface-variant">
                    No invoice records found matching your query.
                  </td>
                </tr>
              ) : (
                filteredInvoices.map((inv) => {
                  const isPaid = inv.status === 'Paid';
                  const isOverdue = inv.status === 'Overdue';
                  const gst = inv.tax || Math.round(inv.amount * 0.18);
                  const total = inv.amount;
                  const base = total - gst;

                  return (
                    <tr key={inv.id} className="hover:bg-surface-container-low/50 transition-colors">
                      <td className="p-3.5 font-mono font-bold text-primary">{inv.id}</td>
                      <td className="p-3.5 font-mono text-on-surface-variant">{inv.date}</td>
                      <td className="p-3.5 font-mono text-on-surface-variant">{inv.dueDate || '30 Jan 2024'}</td>
                      <td className="p-3.5 font-medium text-on-surface">{inv.plan || 'Cloud Subscription Service'}</td>
                      <td className="p-3.5 font-mono text-on-surface-variant">₹{base.toLocaleString('en-IN')}</td>
                      <td className="p-3.5 font-mono text-on-surface-variant">₹{gst.toLocaleString('en-IN')}</td>
                      <td className="p-3.5 font-mono font-bold text-on-surface">₹{total.toLocaleString('en-IN')}</td>
                      <td className="p-3.5">
                        <span
                          className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-semibold ${
                            isPaid
                              ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                              : isOverdue
                              ? 'bg-rose-50 text-rose-700 border border-rose-200'
                              : 'bg-amber-50 text-amber-700 border border-amber-200'
                          }`}
                        >
                          {isPaid && <CheckCircle2 className="w-3 h-3" />}
                          {isOverdue && <AlertTriangle className="w-3 h-3" />}
                          {!isPaid && !isOverdue && <Clock className="w-3 h-3" />}
                          <span>{inv.status}</span>
                        </span>
                      </td>
                      <td className="p-3.5 text-right">
                        <div className="flex items-center justify-end gap-1.5">
                          <button
                            onClick={() => setSelectedInvoice(inv)}
                            className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container text-primary"
                            title="Inspect & Print GST Tax Invoice"
                          >
                            <FileText className="w-3.5 h-3.5" />
                          </button>
                          <button
                            onClick={() => handlePrint(inv)}
                            className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container text-on-surface-variant"
                            title="Print / Save PDF"
                          >
                            <Printer className="w-3.5 h-3.5" />
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

      {/* Invoice Details & GST Modal */}
      {selectedInvoice && (
        <div className="fixed inset-0 z-50 bg-black/40 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-surface-container-lowest max-w-2xl w-full p-6 sm:p-8 rounded-3xl border border-outline-variant/40 shadow-elevation-3 space-y-6">
            {/* Modal Header */}
            <div className="flex items-center justify-between border-b border-surface-container pb-4">
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 rounded-xl bg-primary text-white font-bold flex items-center justify-center text-sm">
                  S
                </div>
                <div>
                  <h3 className="font-bold text-base text-on-surface">SubscriptIQ Tax Invoice</h3>
                  <div className="text-[10px] font-mono text-outline">ORIGINAL FOR RECIPIENT · SAC: 998313</div>
                </div>
              </div>
              <button
                onClick={() => setSelectedInvoice(null)}
                className="p-1 rounded-lg text-outline hover:text-on-surface"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Invoice Meta Grid */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-xs bg-surface-container-low p-4 rounded-xl font-mono">
              <div>
                <span className="text-[10px] text-outline uppercase block">INVOICE NO</span>
                <strong className="text-primary">{selectedInvoice.id}</strong>
              </div>
              <div>
                <span className="text-[10px] text-outline uppercase block">ISSUE DATE</span>
                <span>{selectedInvoice.date}</span>
              </div>
              <div>
                <span className="text-[10px] text-outline uppercase block">DUE DATE</span>
                <span>{selectedInvoice.dueDate || '30 Jan 2024'}</span>
              </div>
              <div>
                <span className="text-[10px] text-outline uppercase block">STATUS</span>
                <span className={selectedInvoice.status === 'Paid' ? 'text-emerald-700 font-bold' : 'text-amber-700 font-bold'}>
                  {selectedInvoice.status}
                </span>
              </div>
            </div>

            {/* Bill To & Bill From */}
            <div className="grid grid-cols-2 gap-6 text-xs">
              <div className="space-y-1">
                <span className="text-[10px] uppercase font-mono text-outline font-bold">ISSUED TO (BUYER):</span>
                <div className="font-bold text-on-surface">{customerCompany}</div>
                <div className="text-on-surface-variant text-[11px]">Level 4, Cyber City Phase II, Gurugram, HR</div>
                <div className="text-[11px] font-mono text-primary">GSTIN: 06AABCU9603R1ZM</div>
              </div>
              <div className="space-y-1 text-right">
                <span className="text-[10px] uppercase font-mono text-outline font-bold">ISSUED BY (SELLER):</span>
                <div className="font-bold text-on-surface">SubscriptIQ Technologies Pvt Ltd</div>
                <div className="text-on-surface-variant text-[11px]">Bhartiya City, Thanisandra Main Rd, Bengaluru, KA</div>
                <div className="text-[11px] font-mono text-primary">GSTIN: 29AAGCS7392M1ZO</div>
              </div>
            </div>

            {/* Breakdown Table */}
            <div className="border border-surface-container rounded-xl overflow-hidden text-xs">
              <table className="w-full text-left">
                <thead className="bg-surface-container-low text-[10px] uppercase font-mono text-outline">
                  <tr>
                    <th className="p-3">Description</th>
                    <th className="p-3 text-center">SAC Code</th>
                    <th className="p-3 text-right">Amount (INR)</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-surface-container-low">
                  <tr>
                    <td className="p-3">
                      <div className="font-semibold text-on-surface">{selectedInvoice.plan || 'Enterprise Pro SaaS Plan'}</div>
                      <div className="text-[10px] text-on-surface-variant">Standard 1-Month Cloud Subscription Access</div>
                    </td>
                    <td className="p-3 font-mono text-center">998313</td>
                    <td className="p-3 font-mono text-right">
                      ₹{Math.round(selectedInvoice.amount * 0.82).toLocaleString('en-IN')}
                    </td>
                  </tr>
                  <tr>
                    <td colSpan="2" className="p-3 text-right font-medium text-on-surface-variant">
                      Integrated Goods &amp; Services Tax (IGST @ 18%):
                    </td>
                    <td className="p-3 font-mono text-right font-semibold">
                      ₹{Math.round(selectedInvoice.amount * 0.18).toLocaleString('en-IN')}
                    </td>
                  </tr>
                  <tr className="bg-surface-container-low/50 font-bold">
                    <td colSpan="2" className="p-3 text-right text-on-surface">
                      Grand Total Payable:
                    </td>
                    <td className="p-3 font-mono text-right text-primary text-sm">
                      ₹{selectedInvoice.amount.toLocaleString('en-IN')}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            {/* Modal Actions */}
            <div className="flex items-center justify-between pt-2">
              <span className="text-[10px] text-outline font-mono">Digitally signed via SubscriptIQ Billing Engine</span>
              <div className="flex items-center gap-2">
                <button
                  onClick={() => setSelectedInvoice(null)}
                  className="px-3 py-1.5 border border-outline-variant/40 rounded-lg text-xs font-semibold hover:bg-surface-container"
                >
                  Close
                </button>
                <button
                  onClick={() => handlePrint(selectedInvoice)}
                  className="px-4 py-1.5 bg-primary text-white text-xs font-bold rounded-lg hover:bg-primary-container flex items-center gap-1.5 shadow-xs"
                >
                  <Printer className="w-3.5 h-3.5" />
                  <span>Print Invoice</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
