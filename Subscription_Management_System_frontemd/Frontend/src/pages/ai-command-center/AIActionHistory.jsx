import React, { useState, useEffect } from 'react';
import { ArrowLeft, Sparkles, CheckCircle2, Clock, RefreshCw, AlertTriangle } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { aiService } from '../../services/aiService';

export default function AIActionHistory() {
  const navigate = useNavigate();
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadHistory = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await aiService.getActionHistory();
      const list = Array.isArray(data) ? data : (data?.content || []);
      setHistory(list);
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to load execution history');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadHistory();
  }, []);

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate('/admin/ai-command-center')}
            className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container"
          >
            <ArrowLeft className="w-4 h-4" />
          </button>
          <div>
            <h1 className="text-xl font-bold text-on-surface">Autonomous AI Execution Audit History</h1>
            <p className="text-xs text-on-surface-variant">Immutable chronological log of all autonomous and confirmed actions.</p>
          </div>
        </div>
        <button
          onClick={loadHistory}
          className="p-2 rounded-lg border border-outline-variant/40 hover:bg-surface-container text-on-surface-variant"
          title="Refresh History"
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

      <div className="bg-surface-container-lowest border border-outline-variant/40 rounded-xl shadow-elevation-1 overflow-hidden">
        <table className="w-full text-left text-xs">
          <thead className="bg-surface-container-low text-on-surface-variant uppercase text-[10px] font-semibold">
            <tr>
              <th className="p-3.5">Execution ID</th>
              <th className="p-3.5">Action Type</th>
              <th className="p-3.5">Summary / Outcome</th>
              <th className="p-3.5">Executed By</th>
              <th className="p-3.5">Timestamp</th>
              <th className="p-3.5">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-surface-container">
            {loading ? (
              <tr>
                <td colSpan="6" className="p-8 text-center text-on-surface-variant">
                  <RefreshCw className="w-5 h-5 animate-spin mx-auto mb-2 text-primary" />
                  <span>Loading audit log...</span>
                </td>
              </tr>
            ) : history.length === 0 ? (
              <tr>
                <td colSpan="6" className="p-8 text-center text-on-surface-variant">
                  No automated actions executed yet.
                </td>
              </tr>
            ) : (
              history.map((h) => (
                <tr key={h.id} className="hover:bg-surface-container-low/50">
                  <td className="p-3.5 font-mono font-bold text-primary">ACT-{h.id}</td>
                  <td className="p-3.5 font-mono text-[11px] font-semibold text-on-surface">{h.actionType}</td>
                  <td className="p-3.5 text-on-surface">{h.executionSummary || h.notes || 'Executed successfully'}</td>
                  <td className="p-3.5 font-mono text-on-surface-variant">{h.executedBy || 'SYSTEM'}</td>
                  <td className="p-3.5 font-mono text-on-surface-variant">
                    {h.executedAt ? new Date(h.executedAt).toLocaleString() : 'N/A'}
                  </td>
                  <td className="p-3.5">
                    <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
                      {h.status || 'COMPLETED'}
                    </span>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
