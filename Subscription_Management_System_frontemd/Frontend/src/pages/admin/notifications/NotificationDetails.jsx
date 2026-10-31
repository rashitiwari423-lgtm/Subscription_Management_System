import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Bell, Clock, CheckCircle2, ArrowUpRight } from 'lucide-react';
import { notificationsList } from '../../../data/mockData';

export default function NotificationDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const notif = notificationsList.find(n => n.id === id) || notificationsList[0];

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate('/admin/notifications')}
          className="p-1.5 rounded-lg border border-outline-variant/40 hover:bg-surface-container"
        >
          <ArrowLeft className="w-4 h-4" />
        </button>
        <div>
          <h1 className="text-xl font-bold text-on-surface">Notification Telemetry Detail</h1>
          <p className="text-xs text-on-surface-variant font-mono">{notif.id} • {notif.type}</p>
        </div>
      </div>

      <div className="bg-surface-container-lowest p-6 rounded-2xl border border-outline-variant/40 shadow-elevation-1 space-y-4">
        <div className="flex items-center justify-between border-b border-surface-container pb-4">
          <span className="text-[10px] uppercase font-mono font-bold bg-primary/10 text-primary px-2.5 py-1 rounded-full">
            {notif.type}
          </span>
          <span className="text-xs font-mono text-outline">{notif.time}</span>
        </div>

        <h2 className="text-base font-bold text-on-surface">{notif.title}</h2>
        <p className="text-xs text-on-surface-variant leading-relaxed">{notif.message}</p>

        <div className="pt-4 border-t border-surface-container flex justify-between items-center text-xs">
          <span className="text-emerald-700 font-semibold flex items-center gap-1">
            <CheckCircle2 className="w-3.5 h-3.5" />
            <span>Telemetry Dispatched</span>
          </span>
          <button
            onClick={() => navigate('/admin/ai-command-center')}
            className="px-3 py-1.5 bg-primary text-white rounded-lg font-semibold hover:bg-primary-container flex items-center gap-1 shadow-xs"
          >
            <span>Investigate in AI Console</span>
            <ArrowUpRight className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>
    </div>
  );
}
