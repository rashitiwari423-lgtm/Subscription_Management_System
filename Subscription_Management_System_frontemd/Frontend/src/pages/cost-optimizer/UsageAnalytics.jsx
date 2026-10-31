import React, { useState, useEffect } from 'react';
import { ArrowLeft, TrendingDown, Users, Layers, RefreshCw, AlertTriangle } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { AreaChart, Area, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, BarChart, Bar } from 'recharts';
import { usageService } from '../../services/usageService';

export default function UsageAnalytics() {
  const navigate = useNavigate();
  const [usageRecords, setUsageRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadUsage = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await usageService.getAll();
      const list = Array.isArray(data) ? data : (data?.content || []);
      setUsageRecords(list);
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to load usage records');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUsage();
  }, []);

  const chartData = usageRecords.slice(0, 10).map(u => ({
    name: u.resourceName || `Res ${u.id}`,
    allocated: u.allocatedQuantity || 0,
    used: u.usedQuantity || 0,
    unused: u.unusedQuantity || 0,
    utilization: Number(u.utilizationPercentage) || 0
  }));

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate('/admin/cost-optimizer')}
            className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container"
          >
            <ArrowLeft className="w-4 h-4" />
          </button>
          <div>
            <h1 className="text-xl font-bold text-on-surface">Seat Utilization & Telemetry Analytics</h1>
            <p className="text-xs text-on-surface-variant">Real-time resource allocation, usage snapshots, and idle license telemetry.</p>
          </div>
        </div>
        <button
          onClick={loadUsage}
          className="p-2 rounded-lg border border-outline-variant/40 hover:bg-surface-container text-on-surface-variant"
          title="Refresh Usage"
        >
          <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
        </button>
      </div>

      {error && (
        <div className="p-3 bg-error-container/20 border border-error/30 text-error rounded-xl text-xs flex justify-between">
          <span>{error}</span>
          <button onClick={() => setError('')} className="font-bold">×</button>
        </div>
      )}

      {/* Chart */}
      <div className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1">
        <h2 className="text-sm font-bold text-on-surface mb-4">Resource Allocation vs Real Utilization</h2>
        <div className="h-64 w-full">
          {chartData.length > 0 ? (
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                <XAxis dataKey="name" tick={{ fontSize: 11 }} />
                <YAxis tick={{ fontSize: 11 }} />
                <Tooltip />
                <Bar dataKey="allocated" fill="#93c5fd" name="Allocated Units" />
                <Bar dataKey="used" fill="#4f46e5" name="Active / Used" />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex items-center justify-center h-full text-xs text-on-surface-variant">
              No usage data available to graph.
            </div>
          )}
        </div>
      </div>

      {/* Usage Table */}
      <div className="bg-surface-container-lowest border border-outline-variant/40 rounded-xl shadow-elevation-1 overflow-hidden">
        <div className="p-4 border-b border-surface-container-low font-bold text-xs text-on-surface">
          Resource Telemetry Snapshots
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-surface-container-low text-on-surface-variant uppercase text-[10px] font-semibold">
              <tr>
                <th className="p-3.5">Resource</th>
                <th className="p-3.5">Customer Org</th>
                <th className="p-3.5">Subscription</th>
                <th className="p-3.5 text-center">Allocated</th>
                <th className="p-3.5 text-center">Used</th>
                <th className="p-3.5 text-center">Unused</th>
                <th className="p-3.5 text-center">Utilization</th>
                <th className="p-3.5 text-right">Snapshot Date</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-container">
              {loading ? (
                <tr>
                  <td colSpan="8" className="p-8 text-center text-on-surface-variant">
                    <RefreshCw className="w-5 h-5 animate-spin mx-auto mb-2 text-primary" />
                    <span>Loading resource snapshots...</span>
                  </td>
                </tr>
              ) : usageRecords.length === 0 ? (
                <tr>
                  <td colSpan="8" className="p-8 text-center text-on-surface-variant">
                    No usage records recorded yet.
                  </td>
                </tr>
              ) : (
                usageRecords.map((u) => (
                  <tr key={u.id} className="hover:bg-surface-container-low/50">
                    <td className="p-3.5 font-bold text-on-surface">{u.resourceName || u.resourceType}</td>
                    <td className="p-3.5 text-on-surface">{u.customerCompanyName || `Customer #${u.customerId}`}</td>
                    <td className="p-3.5 font-mono text-primary">{u.subscriptionNumber || `SUB-${u.subscriptionId}`}</td>
                    <td className="p-3.5 text-center font-mono font-medium">{u.allocatedQuantity}</td>
                    <td className="p-3.5 text-center font-mono font-bold text-primary">{u.usedQuantity}</td>
                    <td className="p-3.5 text-center font-mono text-rose-700">{u.unusedQuantity}</td>
                    <td className="p-3.5 text-center">
                      <span className={`px-2 py-0.5 rounded-full text-[10px] font-semibold ${
                        Number(u.utilizationPercentage) > 75
                          ? 'bg-emerald-50 text-emerald-700'
                          : Number(u.utilizationPercentage) > 40
                          ? 'bg-amber-50 text-amber-700'
                          : 'bg-rose-50 text-rose-700'
                      }`}>
                        {u.utilizationPercentage}%
                      </span>
                    </td>
                    <td className="p-3.5 text-right font-mono text-on-surface-variant">{u.snapshotDate || 'N/A'}</td>
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
