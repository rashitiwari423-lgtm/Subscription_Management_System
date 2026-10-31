import React, { useState } from 'react';
import { ShieldCheck, Search, Filter, ShieldAlert, Key, UserCheck } from 'lucide-react';
import { auditLogs as initialLogs } from '../../../data/mockData';

export default function AuditLogsPage() {
  const [logs, setLogs] = useState(initialLogs || []);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Audit Logs & Security Telemetry</h1>
          <p className="text-xs text-on-surface-variant">
            Immutable SOC2 Type II cryptographic ledger of all sensitive financial and access mutations.
          </p>
        </div>
        <div className="flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-50 border border-emerald-200 text-xs font-mono text-emerald-800">
          <ShieldCheck className="w-4 h-4 text-emerald-600" />
          <span>WORM VAULT: ACTIVE</span>
        </div>
      </div>

      <div className="bg-surface-container-lowest border border-outline-variant/40 rounded-xl shadow-elevation-1 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-surface-container-low text-on-surface-variant uppercase text-[10px] tracking-wider border-b border-outline-variant/20 font-semibold">
              <tr>
                <th className="p-3.5">Timestamp (UTC)</th>
                <th className="p-3.5">Actor Identity</th>
                <th className="p-3.5">Security Event / Action</th>
                <th className="p-3.5">Affected Resource</th>
                <th className="p-3.5">Source IP</th>
                <th className="p-3.5">Audit Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-container-low font-mono">
              {logs.map((log) => (
                <tr key={log.id} className="hover:bg-surface-container-low/60">
                  <td className="p-3.5 text-on-surface-variant">{log.timestamp}</td>
                  <td className="p-3.5 font-bold font-sans text-on-surface">{log.actor}</td>
                  <td className="p-3.5 font-sans font-semibold text-primary">{log.action}</td>
                  <td className="p-3.5 text-on-surface-variant">{log.resource}</td>
                  <td className="p-3.5 text-outline">{log.ip}</td>
                  <td className="p-3.5">
                    <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-50 text-emerald-700 font-sans">
                      Verified (256-Bit)
                    </span>
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

