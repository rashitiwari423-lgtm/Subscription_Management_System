import React, { useState } from 'react';
import {
  TrendingUp, Users, ArrowUpRight, BarChart3, Calendar, Download,
  Layers, RefreshCw, LineChart as ChartIcon
} from 'lucide-react';
import {
  AreaChart, Area, XAxis, YAxis, Tooltip, ResponsiveContainer,
  BarChart, Bar, CartesianGrid, Legend
} from 'recharts';

const COHORT_DATA = [
  { cohort: '2023 Q1', m0: 100, m3: 94, m6: 91, m9: 88, m12: 86 },
  { cohort: '2023 Q2', m0: 100, m3: 95, m6: 92, m9: 90, m12: 89 },
  { cohort: '2023 Q3', m0: 100, m3: 96, m6: 94, m9: 92, m12: 91 },
  { cohort: '2023 Q4', m0: 100, m3: 97, m6: 95, m9: 94, m12: 93 },
  { cohort: '2024 Q1', m0: 100, m3: 98, m6: 96, m9: 95, m12: null },
  { cohort: '2024 Q2', m0: 100, m3: 99, m6: 97, m9: null, m12: null },
];

const FORECAST_DATA = [
  { month: 'Oct (Act)', arr: 224 },
  { month: 'Nov (Fcst)', arr: 236 },
  { month: 'Dec (Fcst)', arr: 248 },
  { month: 'Jan 25', arr: 262 },
  { month: 'Feb 25', arr: 275 },
  { month: 'Mar 25', arr: 290 },
];

export default function FinancialAnalytics() {
  const [activeTab, setActiveTab] = useState('cohorts');

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Financial Analytics & ARR Cohort Forecasting</h1>
          <p className="text-xs text-on-surface-variant">
            Multi-year revenue retention matrices, LTV/CAC ratios, and predictive ARR extrapolation.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={() => alert('Cohort telemetry exported as CSV.')}
            className="px-3.5 py-1.5 rounded-lg border border-outline-variant/40 bg-surface-container-lowest text-xs font-semibold flex items-center gap-1.5 text-on-surface hover:bg-surface-container"
          >
            <Download className="w-3.5 h-3.5" />
            <span>Export Financial Ledger</span>
          </button>
        </div>
      </div>

      {/* KPI Tiles */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-outline">NET REVENUE RETENTION</div>
          <div className="text-2xl font-extrabold text-on-surface mt-1 font-tabular">118.4%</div>
          <div className="text-[10px] text-emerald-700 mt-0.5">+4.2% YoY expansion</div>
        </div>
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-outline">GROSS MARGIN PROFILE</div>
          <div className="text-2xl font-extrabold text-on-surface mt-1 font-tabular">82.6%</div>
          <div className="text-[10px] text-on-surface-variant mt-0.5">SaaS benchmark grade</div>
        </div>
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-outline">LTV TO CAC RATIO</div>
          <div className="text-2xl font-extrabold text-primary mt-1 font-tabular">4.8x</div>
          <div className="text-[10px] text-emerald-700 mt-0.5">Healthy unit economics</div>
        </div>
        <div className="p-3.5 bg-surface-container-lowest border border-outline-variant/40 rounded-xl">
          <div className="text-[10px] uppercase font-mono text-outline">PROJECTED Q1 2025 ARR</div>
          <div className="text-2xl font-extrabold text-on-surface mt-1 font-tabular">₹2.90 Cr</div>
          <div className="text-[10px] text-primary mt-0.5">Predictive run-rate</div>
        </div>
      </div>

      {/* Forecast Chart */}
      <div className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h2 className="text-sm font-bold text-on-surface">ARR Forecast Model (Monte Carlo 95% Confidence)</h2>
            <p className="text-xs text-on-surface-variant">Extrapolates contract renewals, upsells, and historical churn coefficients</p>
          </div>
          <span className="text-[10px] font-mono bg-primary/10 text-primary font-bold px-2 py-0.5 rounded-full">
            PREDICTIVE MODEL V2
          </span>
        </div>

        <div className="h-64 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={FORECAST_DATA} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
              <XAxis dataKey="month" tick={{ fontSize: 11, fill: '#64748b' }} axisLine={{ stroke: '#cbd5e1' }} />
              <YAxis tick={{ fontSize: 11, fill: '#64748b' }} tickFormatter={(v) => `₹${v}L`} axisLine={{ stroke: '#cbd5e1' }} />
              <Tooltip formatter={(v) => [`₹${v} Lakhs`, 'ARR Projection']} />
              <Bar dataKey="arr" fill="#4f46e5" radius={[6, 6, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Cohort Retention Heatmap */}
      <div className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1">
        <h2 className="text-sm font-bold text-on-surface mb-1">Subscriber Dollar Retention Matrix (Cohort Analysis)</h2>
        <p className="text-xs text-on-surface-variant mb-4">Percentage of original contract value retained across quarterly vintage cycles</p>

        <div className="overflow-x-auto">
          <table className="w-full text-xs text-left">
            <thead className="bg-surface-container-low text-on-surface-variant uppercase text-[10px] font-mono">
              <tr>
                <th className="p-3">Vintage Cohort</th>
                <th className="p-3 text-center">Month 0</th>
                <th className="p-3 text-center">Month 3</th>
                <th className="p-3 text-center">Month 6</th>
                <th className="p-3 text-center">Month 9</th>
                <th className="p-3 text-center">Month 12</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-container font-mono">
              {COHORT_DATA.map((row, i) => (
                <tr key={i} className="hover:bg-surface-container-low/50">
                  <td className="p-3 font-bold font-sans text-on-surface">{row.cohort}</td>
                  <td className="p-3 text-center bg-primary/20 text-primary font-bold">{row.m0}%</td>
                  <td className="p-3 text-center bg-primary/15 text-primary">{row.m3}%</td>
                  <td className="p-3 text-center bg-primary/10 text-primary">{row.m6}%</td>
                  <td className="p-3 text-center bg-primary/5 text-primary">{row.m9 ? `${row.m9}%` : '—'}</td>
                  <td className="p-3 text-center bg-primary/5 text-primary">{row.m12 ? `${row.m12}%` : '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

