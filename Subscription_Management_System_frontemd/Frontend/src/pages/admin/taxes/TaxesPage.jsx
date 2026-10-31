import React, { useState, useEffect } from 'react';
import { Landmark, Plus, ShieldCheck, CheckCircle2, RefreshCw, AlertTriangle, Edit, Power } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { taxService } from '../../../services/taxService';

export default function TaxesPage() {
  const navigate = useNavigate();
  const [taxList, setTaxList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadTaxes = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await taxService.getAll();
      const list = Array.isArray(data) ? data : (data?.content || []);
      setTaxList(list);
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to load tax records');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTaxes();
  }, []);

  const handleToggleStatus = async (tax) => {
    try {
      const newStatus = tax.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
      await taxService.updateStatus(tax.id, newStatus);
      await loadTaxes();
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to update tax status');
    }
  };

  return (
    <div className="space-y-5">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Tax Regimes & Indian GST</h1>
          <p className="text-xs text-on-surface-variant">
            Statutory tax schedules, GST rates (CGST/SGST/IGST), and enterprise compliance.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={loadTaxes}
            className="p-2 rounded-lg border border-outline-variant/40 hover:bg-surface-container text-on-surface-variant"
            title="Refresh Taxes"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          </button>
          <button
            onClick={() => navigate('/admin/taxes/new')}
            className="px-3.5 py-2 rounded-lg bg-primary hover:bg-primary-container text-white text-xs font-semibold flex items-center gap-1.5 shadow-sm"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>Create Tax Rate</span>
          </button>
        </div>
      </div>

      {error && (
        <div className="p-3 bg-error-container/20 border border-error/30 text-error rounded-xl text-xs flex justify-between">
          <span>{error}</span>
          <button onClick={() => setError('')} className="font-bold">×</button>
        </div>
      )}

      <div className="bg-surface-container-lowest border border-outline-variant/40 rounded-xl shadow-elevation-1 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-surface-container-low text-on-surface-variant uppercase text-[10px] tracking-wider border-b border-outline-variant/20 font-semibold">
              <tr>
                <th className="p-3.5">Tax Name</th>
                <th className="p-3.5">Percentage Rate</th>
                <th className="p-3.5">Tax ID</th>
                <th className="p-3.5">Status</th>
                <th className="p-3.5 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-container-low">
              {loading ? (
                <tr>
                  <td colSpan="5" className="p-8 text-center text-on-surface-variant">
                    <RefreshCw className="w-5 h-5 animate-spin mx-auto mb-2 text-primary" />
                    <span>Loading tax regimes...</span>
                  </td>
                </tr>
              ) : taxList.length === 0 ? (
                <tr>
                  <td colSpan="5" className="p-8 text-center text-on-surface-variant">
                    No tax rates configured yet.
                  </td>
                </tr>
              ) : (
                taxList.map((tax) => (
                  <tr key={tax.id} className="hover:bg-surface-container-low/60 transition-colors">
                    <td className="p-3.5 font-bold text-on-surface">{tax.name}</td>
                    <td className="p-3.5 font-mono font-bold text-base text-primary">
                      {tax.percentage}%
                    </td>
                    <td className="p-3.5 font-mono text-on-surface-variant">TAX-{tax.id}</td>
                    <td className="p-3.5">
                      <span className={`px-2 py-0.5 rounded-full text-[10px] font-semibold ${
                        tax.status === 'ACTIVE' ? 'bg-emerald-50 text-emerald-700 border border-emerald-200' : 'bg-surface-container text-on-surface-variant'
                      }`}>
                        {tax.status}
                      </span>
                    </td>
                    <td className="p-3.5 text-right">
                      <div className="flex items-center justify-end gap-1.5">
                        <button
                          onClick={() => navigate(`/admin/taxes/${tax.id}/edit`)}
                          className="p-1 rounded hover:bg-surface-container text-on-surface-variant hover:text-primary"
                          title="Edit Tax"
                        >
                          <Edit className="w-3.5 h-3.5" />
                        </button>
                        <button
                          onClick={() => handleToggleStatus(tax)}
                          className={`p-1 rounded hover:bg-surface-container ${
                            tax.status === 'ACTIVE' ? 'text-amber-600' : 'text-emerald-600'
                          }`}
                          title={tax.status === 'ACTIVE' ? 'Deactivate Tax' : 'Activate Tax'}
                        >
                          <Power className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
