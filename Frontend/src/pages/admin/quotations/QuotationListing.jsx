import React, { useState } from 'react';
import { FileSpreadsheet, Plus, Search, ChevronRight, Eye, CheckCircle2, Clock } from 'lucide-react';
import { quotations as initialQuotations } from '../../../data/mockData';
import { useNavigate } from 'react-router-dom';

export default function QuotationListing() {
  const [quotationList, setQuotationList] = useState(initialQuotations || []);
  const navigate = useNavigate();

  return (
    <div className="space-y-5">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Quotation Pipeline & CPQ Deal Desk</h1>
          <p className="text-xs text-on-surface-variant">
            Track multi-tier proposals, customized contract terms, and client approvals.
          </p>
        </div>
        <button
          onClick={() => navigate('/admin/quotations/new')}
          className="px-3.5 py-2 rounded-lg bg-primary hover:bg-primary-container text-white text-xs font-semibold flex items-center gap-1.5 shadow-sm"
        >
          <Plus className="w-3.5 h-3.5" />
          <span>Create CPQ Quotation</span>
        </button>
      </div>

      <div className="bg-surface-container-lowest border border-outline-variant/40 rounded-xl shadow-elevation-1 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-surface-container-low text-on-surface-variant uppercase text-[10px] tracking-wider border-b border-outline-variant/20 font-semibold">
              <tr>
                <th className="p-3.5">Quote Reference</th>
                <th className="p-3.5">Target Client</th>
                <th className="p-3.5">Deal Contact</th>
                <th className="p-3.5">Contract Value</th>
                <th className="p-3.5">Discount</th>
                <th className="p-3.5">Status</th>
                <th className="p-3.5">Valid Until</th>
                <th className="p-3.5 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-container-low">
              {quotationList.map((qt) => (
                <tr
                  key={qt.id}
                  onClick={() => navigate(`/admin/quotations/${qt.id}`)}
                  className="hover:bg-surface-container-low/60 cursor-pointer transition-colors"
                >
                  <td className="p-3.5 font-mono font-bold text-primary">{qt.id}</td>
                  <td className="p-3.5 font-bold text-on-surface">{qt.customer}</td>
                  <td className="p-3.5 text-on-surface-variant">{qt.contact}</td>
                  <td className="p-3.5 font-mono font-bold text-on-surface">
                    ₹{qt.value.toLocaleString('en-IN')}
                  </td>
                  <td className="p-3.5 font-mono text-emerald-700 font-semibold">{qt.discount}</td>
                  <td className="p-3.5">
                    <span className={`px-2 py-0.5 rounded-full text-[10px] font-semibold ${
                      qt.status === 'Accepted' ? 'bg-emerald-50 text-emerald-700' :
                      qt.status === 'Negotiation' ? 'bg-primary/10 text-primary' :
                      qt.status === 'Sent' ? 'bg-amber-50 text-amber-700' : 'bg-surface-container text-on-surface-variant'
                    }`}>
                      {qt.status}
                    </span>
                  </td>
                  <td className="p-3.5 text-on-surface-variant font-mono">{qt.validUntil}</td>
                  <td className="p-3.5 text-right" onClick={(e) => e.stopPropagation()}>
                    <button
                      onClick={() => navigate(`/admin/quotations/${qt.id}`)}
                      className="px-2.5 py-1 bg-surface-container hover:bg-surface-container-high rounded text-xs font-semibold text-primary"
                    >
                      Preview PDF
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

