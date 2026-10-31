import React, { useState, useEffect } from 'react';
import {
  Sparkles, Send, Bot, User, ArrowUpRight, CheckCircle2, AlertTriangle,
  TrendingUp, RefreshCw, Layers, ShieldCheck, Zap
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { aiService } from '../../../services/aiService';

export default function AICommandCenter() {
  const navigate = useNavigate();
  const [insights, setInsights] = useState([]);
  const [loadingInsights, setLoadingInsights] = useState(true);
  const [messages, setMessages] = useState([
    {
      id: 1,
      sender: 'ai',
      text: 'Good day. I am the SubscriptIQ AI Copilot. Ask me any analytical or operational question about subscriptions, overdue payments, churn probability, or revenue optimization.'
    }
  ]);
  const [inputMessage, setInputMessage] = useState('');
  const [asking, setAsking] = useState(false);
  const [error, setError] = useState('');

  const loadInsights = async () => {
    try {
      setLoadingInsights(true);
      setError('');
      const data = await aiService.getInsights();
      const list = Array.isArray(data) ? data : (data?.content || []);
      setInsights(list);
    } catch (err) {
      console.error('Failed to load AI insights:', err);
    } finally {
      setLoadingInsights(false);
    }
  };

  useEffect(() => {
    loadInsights();
  }, []);

  const handleSendMessage = async (e) => {
    e.preventDefault();
    const query = inputMessage.trim();
    if (!query) return;

    const userMsg = { id: Date.now(), sender: 'user', text: query };
    setMessages(prev => [...prev, userMsg]);
    setInputMessage('');
    setAsking(true);

    try {
      const result = await aiService.ask(query);
      let replyText = result?.answer || result?.response || 'Analysis complete.';
      if (result?.recommendations && result.recommendations.length > 0) {
        replyText += '\n\nRecommendations:\n• ' + result.recommendations.join('\n• ');
      }
      setMessages(prev => [
        ...prev,
        {
          id: Date.now() + 1,
          sender: 'ai',
          text: replyText,
          metrics: result?.keyMetrics || null
        }
      ]);
    } catch (err) {
      setMessages(prev => [
        ...prev,
        {
          id: Date.now() + 1,
          sender: 'ai',
          text: `Error processing query: ${err?.response?.data?.message || err?.message || 'Server error'}`
        }
      ]);
    } finally {
      setAsking(false);
    }
  };

  const handleQuickQuestion = (q) => {
    setInputMessage(q);
  };

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
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="bg-gradient-to-r from-primary-container via-secondary-container to-primary p-6 rounded-2xl text-white shadow-ai-glow flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div>
          <div className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-white/20 text-white text-[11px] font-semibold backdrop-blur-md mb-2">
            <Sparkles className="w-3.5 h-3.5 text-yellow-300" />
            <span>Autonomous Intelligence Suite</span>
          </div>
          <h1 className="text-xl font-bold tracking-tight">AI Subscription Command Sentinel</h1>
          <p className="text-xs text-white/80 mt-1 max-w-xl">
            Real-time business insights, churn probability analysis, and controlled autonomous action workflows.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={() => navigate('/admin/ai-command-center/history')}
            className="px-3.5 py-2 rounded-xl bg-white/10 hover:bg-white/20 backdrop-blur-md border border-white/20 text-white text-xs font-semibold flex items-center gap-1.5"
          >
            <span>Action Audit History</span>
            <ArrowUpRight className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>

      {/* Main Grid: Chat Sentinel (5 Cols) + Insights Feed (7 Cols) */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left Column: Conversational AI Command Desk */}
        <div className="lg:col-span-5 bg-surface-container-lowest rounded-2xl border border-outline-variant/40 shadow-elevation-1 flex flex-col h-[600px] overflow-hidden">
          <div className="p-3.5 border-b border-surface-container bg-surface-container-low/50 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div className="w-7 h-7 rounded-lg bg-primary text-white flex items-center justify-center font-bold text-xs shadow-xs">
                <Bot className="w-4 h-4" />
              </div>
              <div>
                <div className="text-xs font-bold text-on-surface">SubscriptIQ Neural Copilot</div>
                <div className="text-[10px] text-emerald-700 flex items-center gap-1 font-mono">
                  <span className="w-1.5 h-1.5 rounded-full bg-emerald-600 animate-pulse"></span>
                  CONNECTED
                </div>
              </div>
            </div>
          </div>

          {/* Quick Prompts */}
          <div className="p-2.5 bg-surface-container-low/30 border-b border-surface-container flex gap-1.5 overflow-x-auto text-[11px]">
            <button
              onClick={() => handleQuickQuestion('Show overdue payments')}
              className="px-2 py-1 rounded bg-surface-container hover:bg-surface-container-high text-on-surface whitespace-nowrap"
            >
              Overdue Payments
            </button>
            <button
              onClick={() => handleQuickQuestion('Which subscriptions are expiring?')}
              className="px-2 py-1 rounded bg-surface-container hover:bg-surface-container-high text-on-surface whitespace-nowrap"
            >
              Expiring Subscriptions
            </button>
            <button
              onClick={() => handleQuickQuestion('What is my revenue trend?')}
              className="px-2 py-1 rounded bg-surface-container hover:bg-surface-container-high text-on-surface whitespace-nowrap"
            >
              Revenue Trend
            </button>
          </div>

          {/* Chat Stream */}
          <div className="flex-1 overflow-y-auto p-4 space-y-3 text-xs">
            {messages.map((m) => (
              <div
                key={m.id}
                className={`flex gap-2.5 ${m.sender === 'user' ? 'justify-end' : 'justify-start'}`}
              >
                {m.sender === 'ai' && (
                  <div className="w-6 h-6 rounded-full bg-primary text-white flex items-center justify-center flex-shrink-0 text-[10px] font-bold">
                    AI
                  </div>
                )}
                <div
                  className={`max-w-[85%] p-3 rounded-2xl leading-relaxed whitespace-pre-wrap ${
                    m.sender === 'user'
                      ? 'bg-primary text-white rounded-br-none shadow-sm'
                      : 'bg-surface-container text-on-surface rounded-bl-none border border-outline-variant/30'
                  }`}
                >
                  {m.text}
                </div>
              </div>
            ))}
            {asking && (
              <div className="flex gap-2.5 justify-start items-center text-xs text-on-surface-variant">
                <RefreshCw className="w-4 h-4 animate-spin text-primary" />
                <span>Analyzing subscription telemetry...</span>
              </div>
            )}
          </div>

          {/* Input Box */}
          <form onSubmit={handleSendMessage} className="p-3 border-t border-surface-container bg-surface-container-lowest flex gap-2">
            <input
              type="text"
              placeholder="Ask an analytical question..."
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              disabled={asking}
              className="flex-1 px-3 py-2 rounded-lg border border-outline-variant/60 text-xs bg-surface-container-low"
            />
            <button
              type="submit"
              disabled={asking || !inputMessage.trim()}
              className="px-3.5 py-2 bg-primary text-white rounded-lg text-xs font-semibold hover:bg-primary-container disabled:opacity-50 flex items-center justify-center"
            >
              <Send className="w-3.5 h-3.5" />
            </button>
          </form>
        </div>

        {/* Right Column: Dynamic Heuristic Insights Feed */}
        <div className="lg:col-span-7 space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-sm font-bold text-on-surface flex items-center gap-2">
              <Zap className="w-4 h-4 text-amber-500" />
              <span>Real-Time AI Business Insights</span>
            </h2>
            <button
              onClick={loadInsights}
              className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container text-on-surface-variant"
              title="Refresh Insights"
            >
              <RefreshCw className={`w-3.5 h-3.5 ${loadingInsights ? 'animate-spin' : ''}`} />
            </button>
          </div>

          {loadingInsights ? (
            <div className="p-12 text-center text-on-surface-variant">
              <RefreshCw className="w-6 h-6 animate-spin mx-auto mb-2 text-primary" />
              <span className="text-xs">Fetching business insights...</span>
            </div>
          ) : insights.length === 0 ? (
            <div className="p-8 text-center bg-surface-container-lowest rounded-2xl border border-outline-variant/40 text-xs text-on-surface-variant">
              No active insight anomalies detected across current subscriptions.
            </div>
          ) : (
            <div className="space-y-3">
              {insights.map((ins) => (
                <div
                  key={ins.id}
                  className="bg-surface-container-lowest p-5 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-3 hover:border-primary/40 transition-colors"
                >
                  <div className="flex items-start justify-between">
                    <div>
                      <div className="flex items-center gap-2">
                        <span className={`px-2 py-0.5 rounded-full text-[10px] font-semibold border ${getSeverityBadge(ins.severity)}`}>
                          {ins.severity}
                        </span>
                        <span className="text-[11px] font-mono text-outline">{ins.type}</span>
                      </div>
                      <h3 className="text-sm font-bold text-on-surface mt-1.5">{ins.title}</h3>
                    </div>
                    {Number(ins.potentialRevenue || 0) > 0 && (
                      <div className="text-right">
                        <div className="text-[10px] uppercase font-mono text-outline">UPSIDE REVENUE</div>
                        <div className="text-sm font-bold font-mono text-emerald-700">
                          ₹{Number(ins.potentialRevenue).toLocaleString('en-IN')}
                        </div>
                      </div>
                    )}
                  </div>

                  <p className="text-xs text-on-surface-variant leading-relaxed">
                    {ins.description}
                  </p>

                  <div className="pt-3 border-t border-surface-container flex flex-wrap items-center justify-between gap-2">
                    <div className="text-xs text-on-surface-variant">
                      {ins.affectedCustomers ? (
                        <span><strong>{ins.affectedCustomers}</strong> customer(s) affected</span>
                      ) : (
                        <span>Status: <strong>{ins.status}</strong></span>
                      )}
                    </div>
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => navigate(`/admin/ai-command-center/insights/${ins.id}`)}
                        className="px-3 py-1.5 bg-surface-container hover:bg-surface-container-high rounded-lg text-xs font-semibold text-on-surface"
                      >
                        Details
                      </button>
                      <button
                        onClick={() => navigate(`/admin/ai-command-center/action-preview?insightId=${ins.id}&actionType=${ins.recommendedAction || 'SEND_RENEWAL_CAMPAIGN'}`)}
                        className="px-3 py-1.5 bg-primary hover:bg-primary-container text-white rounded-lg text-xs font-semibold flex items-center gap-1 shadow-xs"
                      >
                        <span>Take Action</span>
                        <ArrowUpRight className="w-3 h-3" />
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
