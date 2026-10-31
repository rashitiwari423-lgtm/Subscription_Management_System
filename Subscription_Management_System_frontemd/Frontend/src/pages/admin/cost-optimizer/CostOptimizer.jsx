import React, { useState, useEffect } from 'react';
import {
  TrendingDown, CheckCircle2, AlertCircle, ArrowUpRight, DollarSign,
  Layers, Users, ShieldAlert, Sparkles, Filter, RefreshCw, Play
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { optimizationService } from '../../../services/optimizationService';

export default function CostOptimizer() {
  const navigate = useNavigate();
  const [insights, setInsights] = useState([]);
  const [loading, setLoading] = useState(true);
  const [analyzing, setAnalyzing] = useState(false);
  const [error, setError] = useState('');
  const [filterType, setFilterType] = useState('ALL');

  const loadInsights = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await optimizationService.getInsights();
      const list = Array.isArray(data) ? data : (data?.content || []);
      setInsights(list);
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to load optimization insights');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadInsights();
  }, []);

  const handleRunAnalysis = async () => {
    try {
      setAnalyzing(true);
      setError('');
      await optimizationService.analyze();
      await loadInsights();
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to trigger optimization analysis');
    } finally {
      setAnalyzing(false);
    }
  };

  const filtered = insights.filter(i => {
    if (filterType === 'ALL') return true;
    return i.recommendation === filterType || i.status === filterType;
  });

  const totalMonthlySavings = insights.reduce(
    (acc, i) => acc + (Number(i.potentialMonthlySavings) || 0), 0
  );
  const totalAnnualSavings = insights.reduce(
    (acc, i) => acc + (Number(i.potentialAnnualSavings) || (Number(i.potentialMonthlySavings) || 0) * 12), 0
  );
  const totalUnusedQuantity = insights.reduce(
    (acc, i) => acc + (Number(i.unusedQuantity) || 0), 0
  );

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-on-surface">AI Subscription Cost Optimizer</h1>
          <p className="text-xs text-on-surface-variant">
            Identify redundant SaaS licenses, reclaim unallocated seats, and downgrade underutilized tiers.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={() => navigate('/admin/cost-optimizer/usage')}
            className="px-3 py-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container text-xs font-semibold text-on-surface"
          >
            Usage Analytics
          </button>
          <button
            onClick={() => navigate('/admin/cost-optimizer/history')}
            className="px-3 py-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container text-xs font-semibold text-on-surface"
          >
            History
          </button>
          <button
            onClick={handleRunAnalysis}
            disabled={analyzing}
            className="px-3.5 py-1.5 bg-primary hover:bg-primary-container text-white text-xs font-semibold rounded-lg flex items-center gap-1.5 shadow-sm"
          >
            <Play className={`w-3.5 h-3.5 fill-current ${analyzing ? 'animate-spin' : ''}`} />
            <span>{analyzing ? 'Analyzing Telemetry...' : 'Run Optimization Scan'}</span>
          </button>
        </div>
      </div>

      {error && (
        <div className="p-3 bg-error-container/20 border border-error/30 text-error rounded-xl text-xs flex justify-between">
          <span>{error}</span>
          <button onClick={() => setError('')} className="font-bold">×</button>
        </div>
      )}

      {/* KPI Tiles */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-outline">UNUSED SEATS / UNITS</div>
          <div className="text-xl font-bold text-on-surface mt-1">{totalUnusedQuantity} Units</div>
          <div className="text-[10px] text-amber-700 mt-0.5">Identified idle capacity</div>
        </div>
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-outline">OPTIMIZATION OPPORTUNITIES</div>
          <div className="text-xl font-bold text-on-surface mt-1">{insights.length} Contracts</div>
          <div className="text-[10px] text-primary mt-0.5">Scanned subscriptions</div>
        </div>
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-outline">MONTHLY RECLAIMABLE</div>
          <div className="text-xl font-bold text-emerald-700 mt-1 font-mono">
            ₹{totalMonthlySavings.toLocaleString('en-IN')}
          </div>
          <div className="text-[10px] text-on-surface-variant mt-0.5">Per-cycle savings</div>
        </div>
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-outline">ANNUALIZED RUN-RATE</div>
          <div className="text-xl font-bold text-emerald-700 mt-1 font-mono">
            ₹{totalAnnualSavings.toLocaleString('en-IN')}
          </div>
          <div className="text-[10px] text-emerald-600 mt-0.5">12-month projection</div>
        </div>
      </div>

      {/* Recommendations Feed */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-sm font-bold text-on-surface flex items-center gap-2">
            <Sparkles className="w-4 h-4 text-primary" />
            <span>Identified Cost Optimization Opportunities</span>
          </h2>
          <button
            onClick={loadInsights}
            className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container text-on-surface-variant"
            title="Refresh Insights"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          </button>
        </div>

        {loading ? (
          <div className="p-12 text-center text-on-surface-variant">
            <RefreshCw className="w-6 h-6 animate-spin mx-auto mb-2 text-primary" />
            <span className="text-xs">Evaluating cost recommendations...</span>
          </div>
        ) : filtered.length === 0 ? (
          <div className="p-8 text-center bg-surface-container-lowest rounded-2xl border border-outline-variant/40 text-xs text-on-surface-variant">
            No cost optimization anomalies detected. Subscriptions are running at optimal utilization.
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {filtered.map((rec) => (
              <div
                key={rec.id}
                className="bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-4 flex flex-col justify-between hover:border-primary/40 transition-colors"
              >
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-[10px] uppercase font-mono font-bold text-primary px-2 py-0.5 rounded-full bg-primary/10">
                      {rec.recommendation}
                    </span>
                    <span className="text-xs font-mono font-bold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-full border border-emerald-200">
                      Save ₹{Number(rec.potentialMonthlySavings || 0).toLocaleString('en-IN')}/mo
                    </span>
                  </div>

                  <h3 className="text-sm font-bold text-on-surface">{rec.resourceName}</h3>
                  <p className="text-xs text-on-surface-variant leading-relaxed">
                    {rec.proposedAction || `Reduce ${rec.unusedQuantity || 0} unallocated seats for ${rec.customerCompanyName || 'client'}.`}
                  </p>

                  <div className="grid grid-cols-3 gap-2 pt-2 text-xs">
                    <div className="p-2 bg-surface-container/30 rounded-lg">
                      <div className="text-[10px] uppercase text-outline">Allocated</div>
                      <div className="font-bold font-mono text-on-surface">{rec.allocatedQuantity}</div>
                    </div>
                    <div className="p-2 bg-surface-container/30 rounded-lg">
                      <div className="text-[10px] uppercase text-outline">Used</div>
                      <div className="font-bold font-mono text-primary">{rec.usedQuantity}</div>
                    </div>
                    <div className="p-2 bg-surface-container/30 rounded-lg">
                      <div className="text-[10px] uppercase text-outline">Unused</div>
                      <div className="font-bold font-mono text-rose-700">{rec.unusedQuantity}</div>
                    </div>
                  </div>
                </div>

                <div className="pt-3 border-t border-surface-container flex items-center justify-between">
                  <span className="text-[11px] font-mono text-on-surface-variant">
                    Util: {rec.utilizationPercentage}%
                  </span>
                  <button
                    onClick={() => navigate(`/admin/cost-optimizer/recommendations/${rec.id}`)}
                    className="px-3 py-1.5 bg-primary hover:bg-primary-container text-white text-xs font-semibold rounded-lg flex items-center gap-1 shadow-xs"
                  >
                    <span>Inspect Proposal</span>
                    <ArrowUpRight className="w-3 h-3" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
