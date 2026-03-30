import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { QueryClientProvider } from '@tanstack/react-query'
import { Toaster } from 'sonner'
import { queryClient } from '@/lib/queryClient'
import { AuthProvider } from '@/contexts/AuthContext'
import { ThemeProvider } from '@/contexts/ThemeContext'
import { useTheme } from '@/contexts/ThemeContext'
import { ProtectedRoute } from '@/components/ProtectedRoute'
import { Navbar } from '@/components/Navbar'
import LoginPage    from '@/pages/LoginPage'
import RegisterPage from '@/pages/RegisterPage'
import DashboardPage from '@/pages/DashboardPage'
import ProfilePage  from '@/pages/ProfilePage'
import BrandsPage   from '@/pages/admin/BrandsPage'
import TagsPage     from '@/pages/admin/TagsPage'
import PacksPage    from '@/pages/admin/PacksPage'
import FlavorsPage  from '@/pages/admin/FlavorsPage'
import MarketPage   from '@/pages/admin/MarketPage'
import AuditPage    from '@/pages/admin/AuditPage'

function AppLayout() {
  return (
    <>
      <Navbar />
      <Routes>
        <Route path="/dashboard"    element={<DashboardPage />} />
        <Route path="/profile"      element={<ProfilePage />} />
        <Route path="/admin/brands" element={<BrandsPage />} />
        <Route path="/admin/tags"   element={<TagsPage />} />
        <Route path="/admin/packs"   element={<PacksPage />} />
        <Route path="/admin/flavors" element={<FlavorsPage />} />
        <Route path="/admin/market"  element={<MarketPage />} />
        <Route path="/admin/audit"   element={<AuditPage />} />
        <Route path="*"              element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </>
  )
}

function AppInner() {
  const { theme } = useTheme()
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login"    element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route element={<ProtectedRoute />}>
            <Route path="/*" element={<AppLayout />} />
          </Route>
        </Routes>

        <Toaster
          position="bottom-right"
          theme={theme}
          toastOptions={{
            style: {
              fontFamily: '"Inter", system-ui, sans-serif',
              fontSize: '14px',
              borderRadius: '8px',
            },
          }}
        />
      </AuthProvider>
    </BrowserRouter>
  )
}

export default function App() {
  return (
    <ThemeProvider>
      <QueryClientProvider client={queryClient}>
        <AppInner />
      </QueryClientProvider>
    </ThemeProvider>
  )
}
