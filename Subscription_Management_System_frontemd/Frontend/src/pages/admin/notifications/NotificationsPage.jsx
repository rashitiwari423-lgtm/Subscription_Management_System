import React, { useState } from 'react';
import { Bell, CheckCircle2, Clock, Sparkles, Filter, Trash2 } from 'lucide-react';
import { notificationsList as initialNotifications } from '../../../data/mockData';

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState(initialNotifications || []);

  const handleMarkAll = () => {
    setNotifications(prev => prev.map(n => ({ ...n, unread: false })));
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Intelligence & Telemetry Alerts</h1>
          <p className="text-xs text-on-surface-variant">Real-time alerts for dunning failures, churn signals, and batch runs.</p>
        </div>
        <button
          onClick={handleMarkAll}
          className="px-3.5 py-1.5 border border-outline-variant/40 rounded-lg text-xs font-semibold text-on-surface hover:bg-surface-container"
        >
          Mark All as Read
        </button>
      </div>

      <div className="space-y-3">
        {notifications.map((item) => (
          <div
            key={item.id}
            className={`p-4 rounded-2xl border transition-all ${
              item.unread
                ? 'bg-surface-container-lowest border-primary/40 shadow-sm'
                : 'bg-surface-container-lowest/60 border-outline-variant/30'
            }`}
          >
            <div className="flex items-start justify-between">
              <div className="flex items-center gap-2">
                <span className="text-[10px] uppercase font-mono font-bold bg-primary/10 text-primary px-2 py-0.5 rounded-full">
                  {item.type}
                </span>
                <h3 className="text-xs font-bold text-on-surface">{item.title}</h3>
              </div>
              <span className="text-[10px] text-outline font-mono">{item.time}</span>
            </div>

            <p className="text-xs text-on-surface-variant mt-2">{item.message}</p>
          </div>
        ))}
      </div>
    </div>
  );
}

