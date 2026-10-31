import React from 'react';
import { Outlet } from 'react-router-dom';

export default function AuthLayout() {
  return (
    <div className="min-h-screen bg-surface flex flex-col justify-between selection:bg-primary-fixed selection:text-on-primary-fixed">
      <header className="w-full bg-surface/80 backdrop-blur-xl border-b border-outline-variant/30">
        <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-primary text-white font-bold flex items-center justify-center shadow-ai-glow">
              S
            </div>
            <div className="flex flex-col">
              <span className="font-bold text-base text-on-surface tracking-tight leading-none">
                Subscript<span className="text-primary">IQ</span>
              </span>
              <span className="text-[10px] font-mono tracking-widest text-on-surface-variant leading-none mt-1">
                FINANCIAL INTELLIGENCE
              </span>
            </div>
          </div>
          <div className="flex items-center gap-4 text-xs font-mono text-tertiary">
            <span className="flex items-center gap-1">
              <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
              CLUSTER ACTIVE (MUMBAI)
            </span>
          </div>
        </div>
      </header>

      <main className="flex-1 flex items-center justify-center p-6">
        <div className="w-full max-w-5xl">
          <Outlet />
        </div>
      </main>

      <footer className="w-full py-4 text-center text-xs text-on-surface-variant border-t border-outline-variant/20">
        SubscriptIQ Enterprise Subscription Intelligence Platform &copy; 2024. All financial calculations certified under Indian GST & IndAS standards.
      </footer>
    </div>
  );
}

