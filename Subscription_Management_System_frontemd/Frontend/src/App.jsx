import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './routes/ProtectedRoute';

// Public Homepage
import HomePage from './pages/home/HomePage';

// Layouts
import AdminLayout from './layouts/AdminLayout';
import AuthLayout from './layouts/AuthLayout';
import StaffLayout from './layouts/StaffLayout';
import CustomerLayout from './layouts/CustomerLayout';

// Auth Pages (4)
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import ForgotPasswordPage from './pages/auth/ForgotPasswordPage';
import ResetPasswordPage from './pages/auth/ResetPasswordPage';
import UnauthorizedPage from './pages/auth/UnauthorizedPage';
import OnboardingPage from './pages/onboarding/OnboardingPage';

// Admin Pages (34)
import AdminDashboard from './pages/admin/dashboard/AdminDashboard';
import CustomerDirectory from './pages/admin/customers/CustomerDirectory';
import AddCustomer from './pages/admin/customers/AddCustomer';
import CustomerDetails from './pages/admin/customers/CustomerDetails';
import EditCustomer from './pages/admin/customers/EditCustomer';

import StaffListing from './pages/admin/staff/StaffListing';
import AddStaff from './pages/admin/staff/AddStaff';
import StaffDetails from './pages/admin/staff/StaffDetails';
import EditStaff from './pages/admin/staff/EditStaff';

import ProductCatalog from './pages/admin/products/ProductCatalog';
import AddProduct from './pages/admin/products/AddProduct';
import ProductDetails from './pages/admin/products/ProductDetails';
import ProductVariants from './pages/admin/products/ProductVariants';

import PlanManagement from './pages/admin/plans/PlanManagement';
import CreatePlan from './pages/admin/plans/CreatePlan';
import PlanDetails from './pages/admin/plans/PlanDetails';
import EditPlan from './pages/admin/plans/EditPlan';

import QuotationListing from './pages/admin/quotations/QuotationListing';
import CreateQuotation from './pages/admin/quotations/CreateQuotation';
import QuotationDetails from './pages/admin/quotations/QuotationDetails';

import SubscriptionLifecycle from './pages/admin/subscriptions/SubscriptionLifecycle';
import CreateSubscription from './pages/admin/subscriptions/CreateSubscription';
import SubscriptionDetails from './pages/admin/subscriptions/SubscriptionDetails';

import InvoicesBillingRuns from './pages/admin/invoices/InvoicesBillingRuns';
import InvoiceDetails from './pages/admin/invoices/InvoiceDetails';

import PaymentsLedger from './pages/admin/payments/PaymentsLedger';
import RecordPayment from './pages/admin/payments/RecordPayment';
import PaymentDetails from './pages/admin/payments/PaymentDetails';

import DiscountsPage from './pages/admin/discounts/DiscountsPage';
import CreateDiscount from './pages/admin/discounts/CreateDiscount';
import DiscountDetails from './pages/admin/discounts/DiscountDetails';

import TaxesPage from './pages/admin/taxes/TaxesPage';
import CreateTax from './pages/admin/taxes/CreateTax';
import EditTax from './pages/admin/taxes/EditTax';

import NotificationsPage from './pages/admin/notifications/NotificationsPage';
import NotificationDetails from './pages/admin/notifications/NotificationDetails';
import OrgSettingsPage from './pages/admin/settings/OrgSettingsPage';

// System & Auxiliary Admin Pages
import BillingRunPage from './pages/admin/billing-runs/BillingRunPage';
import DunningPage from './pages/admin/dunning/DunningPage';
import AuditLogsPage from './pages/admin/audit-logs/AuditLogsPage';
import IntegrationsPage from './pages/admin/integrations/IntegrationsPage';
import DeveloperPage from './pages/admin/developer/DeveloperPage';

// Staff Operational Pages (6)
import StaffDashboard from './pages/staff/StaffDashboard';

// Customer Self-Service Pages (5)
import CustomerPortal from './pages/customer/CustomerPortal';
import CustomerSubscriptions from './pages/customer/CustomerSubscriptions';
import CustomerSubscriptionDetails from './pages/customer/CustomerSubscriptionDetails';
import CustomerInvoices from './pages/customer/CustomerInvoices';
import CustomerProfile from './pages/customer/CustomerProfile';

// AI Command Center Pages (6)
import AICommandCenter from './pages/admin/ai-command-center/AICommandCenter';
import InsightDetails from './pages/ai-command-center/InsightDetails';
import ActionPreview from './pages/ai-command-center/ActionPreview';
import AIActionHistory from './pages/ai-command-center/AIActionHistory';

// AI Cost Optimizer Pages (5)
import CostOptimizer from './pages/admin/cost-optimizer/CostOptimizer';
import UsageAnalytics from './pages/cost-optimizer/UsageAnalytics';
import RecommendationDetails from './pages/cost-optimizer/RecommendationDetails';
import OptimizationHistory from './pages/cost-optimizer/OptimizationHistory';

// Analytics Dedicated Pages (4)
import FinancialAnalytics from './pages/admin/analytics/FinancialAnalytics';
import RevenueAnalytics from './pages/analytics/RevenueAnalytics';
import SubscriptionAnalytics from './pages/analytics/SubscriptionAnalytics';
import ChurnAnalytics from './pages/analytics/ChurnAnalytics';
import PaymentsAnalytics from './pages/analytics/PaymentsAnalytics';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public Landing Homepage */}
          <Route path="/" element={<HomePage />} />

          {/* Authentication — 4 screens */}
          <Route element={<AuthLayout />}>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/forgot-password" element={<ForgotPasswordPage />} />
            <Route path="/reset-password" element={<ResetPasswordPage />} />
          </Route>
          <Route path="/onboarding" element={<OnboardingPage />} />
          <Route path="/unauthorized" element={<UnauthorizedPage />} />

          {/* Admin — 34 core screens */}
          <Route
            path="/admin"
            element={
              <ProtectedRoute allowedRoles={['ADMIN']}>
                <AdminLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Navigate to="/admin/dashboard" replace />} />
            <Route path="dashboard" element={<AdminDashboard />} />

            {/* Customers */}
            <Route path="customers" element={<CustomerDirectory />} />
            <Route path="customers/new" element={<AddCustomer />} />
            <Route path="customers/:id" element={<CustomerDetails />} />
            <Route path="customers/:id/edit" element={<EditCustomer />} />

            {/* Staff */}
            <Route path="staff" element={<StaffListing />} />
            <Route path="staff/new" element={<AddStaff />} />
            <Route path="staff/:id" element={<StaffDetails />} />
            <Route path="staff/:id/edit" element={<EditStaff />} />

            {/* Products */}
            <Route path="products" element={<ProductCatalog />} />
            <Route path="products/new" element={<AddProduct />} />
            <Route path="products/:id" element={<ProductDetails />} />
            <Route path="products/:id/variants" element={<ProductVariants />} />

            {/* Plans */}
            <Route path="plans" element={<PlanManagement />} />
            <Route path="plans/new" element={<CreatePlan />} />
            <Route path="plans/:id" element={<PlanDetails />} />
            <Route path="plans/:id/edit" element={<EditPlan />} />

            {/* Quotations */}
            <Route path="quotations" element={<QuotationListing />} />
            <Route path="quotations/new" element={<CreateQuotation />} />
            <Route path="quotations/:id" element={<QuotationDetails />} />

            {/* Subscriptions */}
            <Route path="subscriptions" element={<SubscriptionLifecycle />} />
            <Route path="subscriptions/new" element={<CreateSubscription />} />
            <Route path="subscriptions/:id" element={<SubscriptionDetails />} />

            {/* Invoices */}
            <Route path="invoices" element={<InvoicesBillingRuns />} />
            <Route path="invoices/:id" element={<InvoiceDetails />} />

            {/* Payments */}
            <Route path="payments" element={<PaymentsLedger />} />
            <Route path="payments/new" element={<RecordPayment />} />
            <Route path="payments/:id" element={<PaymentDetails />} />

            {/* Discounts */}
            <Route path="discounts" element={<DiscountsPage />} />
            <Route path="discounts/new" element={<CreateDiscount />} />
            <Route path="discounts/:id" element={<DiscountDetails />} />

            {/* Taxes */}
            <Route path="taxes" element={<TaxesPage />} />
            <Route path="taxes/new" element={<CreateTax />} />
            <Route path="taxes/:id/edit" element={<EditTax />} />

            {/* Notifications */}
            <Route path="notifications" element={<NotificationsPage />} />
            <Route path="notifications/:id" element={<NotificationDetails />} />

            {/* Settings */}
            <Route path="settings" element={<OrgSettingsPage />} />

            {/* Financial Systems */}
            <Route path="billing-runs" element={<BillingRunPage />} />
            <Route path="dunning" element={<DunningPage />} />
            <Route path="audit-logs" element={<AuditLogsPage />} />
            <Route path="integrations" element={<IntegrationsPage />} />
            <Route path="developer" element={<DeveloperPage />} />

            {/* AI Command Center Suite (6 screens) */}
            <Route path="ai-command-center" element={<AICommandCenter />} />
            <Route path="ai-command-center/chat" element={<AICommandCenter />} />
            <Route path="ai-command-center/insights" element={<AICommandCenter />} />
            <Route path="ai-command-center/insights/:id" element={<InsightDetails />} />
            <Route path="ai-command-center/action-preview" element={<ActionPreview />} />
            <Route path="ai-command-center/history" element={<AIActionHistory />} />

            {/* AI Cost Optimizer Suite (5 screens) */}
            <Route path="cost-optimizer" element={<CostOptimizer />} />
            <Route path="cost-optimizer/usage" element={<UsageAnalytics />} />
            <Route path="cost-optimizer/recommendations" element={<CostOptimizer />} />
            <Route path="cost-optimizer/recommendations/:id" element={<RecommendationDetails />} />
            <Route path="cost-optimizer/history" element={<OptimizationHistory />} />

            {/* Analytics Suite (4 screens + summary) */}
            <Route path="analytics" element={<FinancialAnalytics />} />
            <Route path="analytics/revenue" element={<RevenueAnalytics />} />
            <Route path="analytics/subscriptions" element={<SubscriptionAnalytics />} />
            <Route path="analytics/churn" element={<ChurnAnalytics />} />
            <Route path="analytics/payments" element={<PaymentsAnalytics />} />
          </Route>

          {/* Staff Dedicated Workspace — 6 screens */}
          <Route
            path="/staff"
            element={
              <ProtectedRoute allowedRoles={['STAFF', 'ADMIN']}>
                <StaffLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Navigate to="/staff/dashboard" replace />} />
            <Route path="dashboard" element={<StaffDashboard />} />
            <Route path="customers" element={<CustomerDirectory />} />
            <Route path="customers/:id" element={<CustomerDetails />} />
            <Route path="quotations" element={<QuotationListing />} />
            <Route path="subscriptions" element={<SubscriptionLifecycle />} />
            <Route path="invoices" element={<InvoicesBillingRuns />} />
          </Route>

          {/* Customer Self-Service Portal — 5 screens */}
          <Route
            path="/customer"
            element={
              <ProtectedRoute allowedRoles={['CUSTOMER', 'ADMIN']}>
                <CustomerLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Navigate to="/customer/dashboard" replace />} />
            <Route path="dashboard" element={<CustomerPortal />} />
            <Route path="subscriptions" element={<CustomerSubscriptions />} />
            <Route path="subscriptions/:id" element={<CustomerSubscriptionDetails />} />
            <Route path="invoices" element={<CustomerInvoices />} />
            <Route path="profile" element={<CustomerProfile />} />
          </Route>

          {/* Fallbacks */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
