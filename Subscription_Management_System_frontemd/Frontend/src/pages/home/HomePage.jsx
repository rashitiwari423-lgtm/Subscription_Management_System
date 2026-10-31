import React from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Shield,
  LayoutDashboard,
  Users,
  Briefcase,
  User,
  CreditCard,
  ArrowRight,
  Sparkles,
  CheckCircle2,
  TrendingUp,
  Receipt,
  Layers,
  Zap,
  Lock,
  ChevronRight,
  BarChart3,
  Bot
} from 'lucide-react';

export default function HomePage() {
  const navigate = useNavigate();

  const workspaces = [
    {
      id: 'admin',
      role: 'admin',
      name: 'Admin Portal',
      tagline: 'C-Suite & Operations Governance',
      description: 'Manage users, subscriptions, billing, analytics and business intelligence.',
      icon: Shield,
      badge: 'Full Governance',
      badgeColor: 'bg-primary/10 text-primary border-primary/20',
      actionText: 'Admin Login',
      link: '/login?role=admin',
      features: ['Financial Command Suite', 'Team RBAC & Staff Quotas', 'Audit Logs & GST Compliance'],
      gradient: 'from-primary/10 via-primary/5 to-transparent'
    },
    {
      id: 'staff',
      role: 'staff',
      name: 'Staff Portal',
      tagline: 'Tier-2 Operations & Sales Desk',
      description: 'Manage customers, quotations, subscriptions and daily operations.',
      icon: Briefcase,
      badge: 'Operations Desk',
      badgeColor: 'bg-emerald-50 text-emerald-700 border-emerald-200',
      actionText: 'Staff Login',
      link: '/login?role=staff',
      features: ['Quotation Deal Desk (CPQ)', 'Subscription Lifecycle Queue', 'Invoice Collections Watch'],
      gradient: 'from-emerald-500/10 via-emerald-500/5 to-transparent'
    },
    {
      id: 'customer',
      role: 'customer',
      name: 'Customer Portal',
      tagline: 'Self-Service Billing Hub',
      description: 'View subscriptions, invoices, payment history and account details.',
      icon: CreditCard,
      badge: 'Self-Serve Client',
      badgeColor: 'bg-indigo-50 text-indigo-700 border-indigo-200',
      actionText: 'Customer Login',
      link: '/login?role=customer',
      features: ['Live Seat Utilization', 'GST Tax Invoices & Receipts', '1-Click Tier Upgrades'],
      gradient: 'from-indigo-500/10 via-indigo-500/5 to-transparent'
    }
  ];

  const features = [
    {
      icon: Bot,
      title: 'AI Command Center',
      desc: 'Predict churn risks before they happen with continuous heuristic account monitoring and automated outreach sequences.'
    },
    {
      icon: Zap,
      title: 'Dynamic CPQ & Quotations',
      desc: 'Build customized multi-tier subscription plans, discount terms, and automated approval workflows in seconds.'
    },
    {
      icon: TrendingUp,
      title: 'Automated Dunning & Recovery',
      desc: 'Intelligent payment waterfall retries, promise-to-pay logging, and multi-stage dunning escalation sequences.'
    },
    {
      icon: Receipt,
      title: 'Indian GST & Multi-Rail Billing',
      desc: 'Compliant HSN/SAC code tracking, automated recurring invoice generation, and unified payment ledger.'
    }
  ];

  const stats = [
    { label: 'Annual Contract Value Tracked', value: '₹14.2 Cr+' },
    { label: 'Billing SLA On-Time Settlement', value: '99.8%' },
    { label: 'Average Dunning Recovery Rate', value: '88%' },
    { label: 'Security & Compliance Standard', value: 'SOC2 Type II' }
  ];

  return (
    <div className="min-h-screen bg-surface flex flex-col font-sans selection:bg-primary/20 selection:text-primary">
      {/* Top Header */}
      <header className="sticky top-0 z-40 bg-surface/80 backdrop-blur-md border-b border-outline-variant/30">
        <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
          {/* Logo & Brand */}
          <div className="flex items-center gap-3 cursor-pointer" onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}>
            <div className="w-9 h-9 rounded-xl bg-primary flex items-center justify-center text-white font-bold text-lg shadow-elevation-1">
              S
            </div>
            <div>
              <span className="font-extrabold text-lg text-on-surface tracking-tight">
                Subscript<span className="text-primary">IQ</span>
              </span>
              <span className="hidden sm:inline-block ml-2 text-[10px] uppercase font-mono px-2 py-0.5 rounded-full bg-primary/10 text-primary font-semibold">
                Intelligence Platform
              </span>
            </div>
          </div>

          {/* Navigation Links */}
          <nav className="hidden md:flex items-center gap-8 text-xs font-semibold text-on-surface-variant">
            <a href="#home" className="hover:text-primary transition-colors">Home</a>
            <a href="#workspaces" className="hover:text-primary transition-colors">Workspaces</a>
            <a href="#features" className="hover:text-primary transition-colors">Features</a>
            <a href="#about" className="hover:text-primary transition-colors">About</a>
            <a href="#contact" className="hover:text-primary transition-colors">Contact</a>
          </nav>

          {/* Action Button */}
          <div className="flex items-center gap-3">
            <button
              onClick={() => {
                const el = document.getElementById('workspaces');
                el ? el.scrollIntoView({ behavior: 'smooth' }) : navigate('/login');
              }}
              className="px-4 py-2 bg-primary hover:bg-primary-container text-white text-xs font-bold rounded-xl shadow-elevation-1 transition-all active:scale-95 flex items-center gap-1.5"
            >
              <span>Get Started</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </button>
          </div>
        </div>
      </header>

      {/* Main Body */}
      <main id="home" className="flex-1">
        {/* Hero Section */}
        <section className="relative overflow-hidden pt-16 pb-20 md:pt-24 md:pb-28 border-b border-outline-variant/20">
          {/* Subtle Ambient Background Gradient */}
          <div className="absolute inset-0 pointer-events-none -z-10">
            <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[700px] h-[350px] bg-gradient-to-tr from-primary/15 via-secondary/10 to-transparent blur-3xl rounded-full" />
          </div>

          <div className="max-w-5xl mx-auto px-6 text-center space-y-6">
            {/* Pill Tag */}
            <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-surface-container-lowest border border-outline-variant/40 shadow-xs text-xs font-medium text-on-surface">
              <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
              <span className="text-on-surface-variant">Subscription Intelligence, Simplified</span>
              <span className="text-primary font-bold">· SubscriptIQ 2.0</span>
            </div>

            {/* Main Heading */}
            <h1 className="text-3xl sm:text-5xl md:text-6xl font-extrabold text-on-surface tracking-tight leading-[1.12]">
              Manage Every Subscription.<br />
              <span className="bg-gradient-to-r from-primary via-secondary to-primary bg-clip-text text-transparent">
                Understand Every Opportunity.
              </span>
            </h1>

            {/* Subtitle */}
            <p className="max-w-2xl mx-auto text-sm sm:text-base text-on-surface-variant leading-relaxed font-normal">
              Take control of subscriptions, automate billing workflows, track payments and unlock smarter business decisions with AI-powered subscription intelligence.
            </p>

            {/* Action Buttons */}
            <div className="flex flex-wrap items-center justify-center gap-3 pt-4">
              <button
                onClick={() => {
                  const el = document.getElementById('workspaces');
                  el?.scrollIntoView({ behavior: 'smooth' });
                }}
                className="px-6 py-3 bg-primary hover:bg-primary-container text-white font-bold text-sm rounded-xl shadow-elevation-2 transition-all active:scale-95 flex items-center gap-2"
              >
                <span>Get Started</span>
                <ArrowRight className="w-4 h-4" />
              </button>
              <button
                onClick={() => {
                  const el = document.getElementById('features');
                  el?.scrollIntoView({ behavior: 'smooth' });
                }}
                className="px-6 py-3 bg-surface-container-lowest hover:bg-surface-container border border-outline-variant/60 text-on-surface font-semibold text-sm rounded-xl shadow-xs transition-colors"
              >
                Explore Features
              </button>
            </div>

            {/* Hero Stats Ribbon */}
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 pt-12 text-left max-w-4xl mx-auto">
              {stats.map((s, idx) => (
                <div key={idx} className="p-4 rounded-xl bg-surface-container-lowest/80 border border-outline-variant/30 backdrop-blur-xs">
                  <div className="text-xl sm:text-2xl font-extrabold text-on-surface font-mono">{s.value}</div>
                  <div className="text-[11px] text-on-surface-variant mt-1 font-medium">{s.label}</div>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* Workspace Selection Section */}
        <section id="workspaces" className="py-20 bg-surface-container-low/40 border-b border-outline-variant/20">
          <div className="max-w-7xl mx-auto px-6">
            <div className="text-center max-w-2xl mx-auto mb-14">
              <span className="text-[11px] font-mono uppercase font-bold text-primary tracking-wider bg-primary/10 px-3 py-1 rounded-full border border-primary/20">
                Persona Gateways
              </span>
              <h2 className="text-2xl sm:text-4xl font-extrabold text-on-surface tracking-tight mt-3">
                Choose Your Workspace
              </h2>
              <p className="text-xs sm:text-sm text-on-surface-variant mt-2">
                Dedicated interfaces customized for your specific operational responsibility, security clearance, and daily workflow.
              </p>
            </div>

            {/* 3 Premium Workspace Cards */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              {workspaces.map((ws) => {
                const Icon = ws.icon;
                return (
                  <div
                    key={ws.id}
                    className="relative rounded-2xl bg-surface-container-lowest border border-outline-variant/40 p-7 shadow-elevation-1 hover:shadow-elevation-3 transition-all duration-300 flex flex-col justify-between group hover:-translate-y-1"
                  >
                    <div>
                      {/* Top Bar with Icon & Badge */}
                      <div className="flex items-center justify-between mb-6">
                        <div className="w-12 h-12 rounded-xl bg-surface-container flex items-center justify-center text-primary group-hover:bg-primary group-hover:text-white transition-colors duration-300 shadow-xs">
                          <Icon className="w-6 h-6" />
                        </div>
                        <span className={`text-[10px] font-mono font-bold px-2.5 py-0.5 rounded-full border ${ws.badgeColor}`}>
                          {ws.badge}
                        </span>
                      </div>

                      {/* Card Titles */}
                      <h3 className="text-xl font-bold text-on-surface tracking-tight">{ws.name}</h3>
                      <div className="text-[11px] font-mono text-primary font-semibold mt-0.5">{ws.tagline}</div>
                      <p className="text-xs text-on-surface-variant mt-3 leading-relaxed">
                        {ws.description}
                      </p>

                      {/* Feature Checklist */}
                      <div className="mt-6 pt-5 border-t border-surface-container space-y-2">
                        {ws.features.map((feat, fIdx) => (
                          <div key={fIdx} className="flex items-center gap-2 text-xs text-on-surface-variant">
                            <CheckCircle2 className="w-3.5 h-3.5 text-primary flex-shrink-0" />
                            <span>{feat}</span>
                          </div>
                        ))}
                      </div>
                    </div>

                    {/* Bottom CTA Button */}
                    <div className="mt-8 pt-4">
                      <button
                        onClick={() => navigate(ws.link)}
                        className="w-full py-2.5 px-4 rounded-xl bg-primary hover:bg-primary-container text-white text-xs font-bold transition-all shadow-elevation-1 flex items-center justify-center gap-2 group-hover:shadow-elevation-2 active:scale-98"
                      >
                        <span>{ws.actionText}</span>
                        <ArrowRight className="w-3.5 h-3.5 group-hover:translate-x-1 transition-transform" />
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </section>

        {/* Feature Highlights Section */}
        <section id="features" className="py-20 border-b border-outline-variant/20">
          <div className="max-w-7xl mx-auto px-6">
            <div className="text-center max-w-2xl mx-auto mb-14">
              <span className="text-[11px] font-mono uppercase font-bold text-secondary tracking-wider bg-secondary/10 px-3 py-1 rounded-full border border-secondary/20">
                Core Capabilities
              </span>
              <h2 className="text-2xl sm:text-4xl font-extrabold text-on-surface tracking-tight mt-3">
                Engineered for High-Growth Recurring Revenue
              </h2>
              <p className="text-xs sm:text-sm text-on-surface-variant mt-2">
                Replace fragmented spreadsheets with a single pane of glass spanning contract lifecycle, invoicing pipelines, and client retention.
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              {features.map((f, i) => {
                const Icon = f.icon;
                return (
                  <div key={i} className="p-6 rounded-2xl bg-surface-container-lowest border border-outline-variant/40 shadow-xs hover:shadow-elevation-1 transition-all space-y-3">
                    <div className="w-10 h-10 rounded-xl bg-primary/10 text-primary flex items-center justify-center font-bold">
                      <Icon className="w-5 h-5" />
                    </div>
                    <h3 className="font-bold text-base text-on-surface">{f.title}</h3>
                    <p className="text-xs text-on-surface-variant leading-relaxed">
                      {f.desc}
                    </p>
                  </div>
                );
              })}
            </div>
          </div>
        </section>

        {/* About Section */}
        <section id="about" className="py-16 bg-surface-container-low/30 border-b border-outline-variant/20">
          <div className="max-w-4xl mx-auto px-6 text-center space-y-4">
            <span className="text-[11px] font-mono uppercase font-bold text-on-surface-variant">About SubscriptIQ</span>
            <h2 className="text-2xl sm:text-3xl font-extrabold text-on-surface">Built for Modern Financial & Operational Teams</h2>
            <p className="text-xs sm:text-sm text-on-surface-variant leading-relaxed">
              SubscriptIQ combines the reliability of traditional enterprise ERP systems with the predictive agility of modern machine intelligence. From deal desk CPQ creation to automated overdue dunning workflows, teams operate with zero guesswork.
            </p>
          </div>
        </section>

        {/* Contact & Support Section */}
        <section id="contact" className="py-16">
          <div className="max-w-4xl mx-auto px-6">
            <div className="p-8 rounded-3xl bg-gradient-to-br from-primary/10 via-surface to-secondary/10 border border-primary/20 flex flex-col sm:flex-row items-center justify-between gap-6 text-center sm:text-left">
              <div>
                <h3 className="text-xl font-bold text-on-surface">Ready to experience SubscriptIQ?</h3>
                <p className="text-xs text-on-surface-variant mt-1">Explore our instant verified demo personas or request an enterprise pilot.</p>
              </div>
              <button
                onClick={() => navigate('/login')}
                className="px-6 py-3 bg-primary hover:bg-primary-container text-white text-xs font-bold rounded-xl shadow-elevation-1 whitespace-nowrap active:scale-95"
              >
                Access Demo Consoles
              </button>
            </div>
          </div>
        </section>
      </main>

      {/* Footer */}
      <footer className="bg-surface-container-lowest border-t border-outline-variant/30 py-8 text-xs text-on-surface-variant">
        <div className="max-w-7xl mx-auto px-6 flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-2">
            <div className="w-6 h-6 rounded-lg bg-primary text-white font-bold flex items-center justify-center text-xs">S</div>
            <span className="font-bold text-on-surface">SubscriptIQ</span>
            <span className="text-[11px] text-outline">© {new Date().getFullYear()} SubscriptIQ Platform. All rights reserved.</span>
          </div>
          <div className="flex items-center gap-6 text-[11px]">
            <a href="#home" className="hover:text-primary">Home</a>
            <a href="#workspaces" className="hover:text-primary">Workspaces</a>
            <a href="#features" className="hover:text-primary">Features</a>
            <span className="text-outline">SOC2 Certified</span>
          </div>
        </div>
      </footer>
    </div>
  );
}

