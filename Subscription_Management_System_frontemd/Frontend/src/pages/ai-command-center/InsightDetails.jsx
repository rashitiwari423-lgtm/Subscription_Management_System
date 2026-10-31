import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Sparkles, CheckCircle2, ShieldAlert, ArrowUpRight, RefreshCw, AlertTriangle } from 'lucide-react';
import { aiService } from '../../services/aiService';

export default function InsightDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [insight, setInsight] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    async function loadInsight() {
      try {
        setLoading(true);
        setError('');
        const data = await aiService.getInsightById(id);
        setInsight(data);
      } catch (err) {
        setError(err?.response?.data?.message || err?.message || 'Failed to load insight details');
      } finally {
        setLoading(false);
      }
    }
    if (id) loadInsight();
  }, [id]);

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center p-12 text-center">
        <RefreshCw className="w-8 h-8 animate-spin text-primary mb-2" />
        <p className="text-xs text-on-surface-variant font-medium">Loading insight telemetry...</p>
      </div>
    );
  }

  if (error && !insight) {
    return (
      <div className="p-8 text-center space-y-4">
        <div className="inline-flex p-3 rounded-full bg-error-container text-error">
          <AlertTriangle className="w-6 h-6" />
        </div>
        <p className="text-sm font-medium text-error">{error}</p>
        <button
          onClick={() => navigate('/admin/ai-command-center')}
          className="px-4 py-2 bg-surface-container text-xs font-semibold rounded-lg text-on-surface"
        >
          Back to Command Center
        </button>
      </div>
    );
  }

  const getSeverityBadge = (severity) => {
    switch (severity) {
      case 'CRITICAL':
        return 'bg-rose-50 text-rose-700 border-rose-200';
      case 'HIGH':
        return 'bg-amber-50 text-amber-700 border-amber-200';
      case 'MEDIUM':
        return 'bg-blue-50 text-blue-700 border-blue-200';
      default:
        return 'bg-surface-container text-on-surface-variant';
    }
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate('/admin/ai-command-center')}
          className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container"
        >
          <ArrowLeft className="w-4 h-4" />
        </button>
        <div>
          <h1 className="text-xl font-bold text-on-surface">Insight Analysis & Telemetry</h1>
          <p className="text-xs text-on-surface-variant font-mono">INSIGHT-{insight.id} • {insight.type}</p>
        </div>
      </div>

      <div className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-5">
        <div className="flex items-center justify-between border-b border-surface-container pb-4">
          <span className={`px-2.5 py-0.5 rounded-full text-xs font-semibold border ${getSeverityBadge(insight.severity)}`}>
            {insight.severity} SEVERITY
          </span>
          {Number(insight.potentialRevenue || 0) > 0 && (
            <span className="text-xs font-bold text-emerald-700 bg-emerald-50 px-2.5 py-1 rounded-full border border-emerald-200">
              Potential Revenue: ₹{Number(insight.potentialRevenue).toLocaleString('en-IN')}
            </span>
          )}
        </div>

        <div>
          <h2 className="text-lg font-bold text-on-surface">{insight.title}</h2>
          <p className="text-xs text-on-surface-variant leading-relaxed mt-2">{insight.description}</p>
        </div>

        <div className="grid grid-cols-2 gap-4 text-xs">
          <div className="p-3 bg-surface-container/30 rounded-xl space-y-1">
            <span className="text-[10px] uppercase font-mono text-outline">AFFECTED CUSTOMERS</span>
            <div className="text-lg font-bold text-on-surface">{insight.affectedCustomers ?? 0} Accounts</div>
          </div>
          <div className="p-3 bg-surface-container/30 rounded-xl space-y-1">
            <span className="text-[10px] uppercase font-mono text-outline">RECOMMENDED ACTION</span>
            <div className="text-sm font-bold text-primary font-mono">{insight.recommendedAction || 'REVIEW_RESOURCES'}</div>
          </div>
        </div>

        <div className="pt-4 border-t border-surface-container flex justify-end gap-2">
          <button
            onClick={() => navigate(`/admin/ai-command-center/action-preview?insightId=${insight.id}&actionType=${insight.recommendedAction || 'SEND_RENEWAL_CAMPAIGN'}`)}
            className="px-4 py-2 bg-primary hover:bg-primary-container text-white text-xs font-semibold rounded-lg flex items-center gap-1.5 shadow-sm"
          >
            <span>Preview & Execute Action</span>
            <ArrowUpRight className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>
    </div>
  );
}
