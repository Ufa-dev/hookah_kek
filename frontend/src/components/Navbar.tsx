import { useState } from 'react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '@/contexts/AuthContext'
import { toast } from 'sonner'
import { LayoutDashboard, User, Package, Tag, LogOut, Menu, X, Flame, Archive, ShoppingBag } from 'lucide-react'

const NAV_ITEMS = [
  { to: '/dashboard',    label: 'Главная',    icon: LayoutDashboard },
  { to: '/admin/brands', label: 'Бренды',     icon: Package },
  { to: '/admin/flavors', label: 'Вкусы',     icon: Flame },
  { to: '/admin/market',  label: 'Склад',     icon: ShoppingBag },
  { to: '/admin/tags',   label: 'Теги',       icon: Tag },
  { to: '/admin/packs',  label: 'Контейнеры', icon: Archive },
  { to: '/profile',      label: 'Профиль',    icon: User },
] as const

export function Navbar() {
  const { logout } = useAuth()
  const navigate = useNavigate()
  const [drawerOpen, setDrawerOpen] = useState(false)

  const handleLogout = async () => {
    setDrawerOpen(false)
    await logout()
    toast.success('Вы вышли из системы')
    navigate('/login')
  }

  return (
    <>
      {/* ── Fixed top bar ─────────────────────────────────────────────── */}
      <header
        className="fixed top-0 left-0 right-0 z-40 h-[60px] bg-white border-b border-border shadow-card"
        style={{ paddingTop: 'env(safe-area-inset-top, 0px)' }}
      >
        <div className="max-w-5xl mx-auto px-4 sm:px-6 h-full flex items-center justify-between">
          {/* Logo */}
          <Link to="/dashboard" className="flex items-center gap-2.5 group">
            <img src="/logo.png" className="w-7 h-7 object-contain" alt="" />
            <span className="font-display text-base sm:text-lg font-bold text-ink whitespace-nowrap">
              Hookah Place
            </span>
          </Link>

          {/* Desktop nav */}
          <nav className="hidden md:flex items-center gap-1 ml-10">
            {NAV_ITEMS.map(({ to, label, icon: Icon }) => (
              <NavLink
                key={to}
                to={to}
                className={({ isActive }) =>
                  `flex items-center gap-1.5 px-3 py-2 rounded text-xs font-body font-medium uppercase tracking-wider transition-all duration-150 ${
                    isActive
                      ? 'text-red bg-red-pale border border-red-glow'
                      : 'text-ink-dim hover:text-ink hover:bg-elevated border border-transparent'
                  }`
                }
              >
                <Icon className="h-3.5 w-3.5" />
                {label}
              </NavLink>
            ))}
          </nav>

          {/* Desktop logout */}
          <button
            onClick={handleLogout}
            className="hidden md:flex items-center gap-1.5 px-3 py-2 rounded text-xs font-body font-medium text-ink-muted hover:text-red hover:bg-red-pale border border-transparent hover:border-red-glow transition-all duration-150 touch-target"
          >
            <LogOut className="h-3.5 w-3.5" />
            Выйти
          </button>

          {/* Mobile hamburger */}
          <button
            onClick={() => setDrawerOpen(true)}
            className="md:hidden p-2 rounded text-ink-dim hover:text-ink hover:bg-elevated transition-colors touch-target flex items-center justify-center"
            aria-label="Меню"
          >
            <Menu className="h-5 w-5" />
          </button>
        </div>
      </header>

      {/* ── Mobile drawer ─────────────────────────────────────────────── */}
      {drawerOpen && (
        <>
          <div
            className="fixed inset-0 z-50 bg-black/30 backdrop-blur-sm"
            onClick={() => setDrawerOpen(false)}
          />
          <aside
            className="fixed top-0 right-0 bottom-0 z-50 w-72 bg-white border-l border-border shadow-card-lg flex flex-col animate-in slide-in-from-right duration-300"
            style={{ paddingTop: 'env(safe-area-inset-top, 0px)' }}
          >
            <div className="flex items-center justify-between px-5 h-[60px] border-b border-border">
              <span className="font-display text-base font-bold text-ink">
                Hookah Place
              </span>
              <button
                onClick={() => setDrawerOpen(false)}
                className="p-2 rounded text-ink-muted hover:text-ink hover:bg-elevated transition-colors touch-target flex items-center justify-center"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            <nav className="flex-1 p-4 space-y-1 overflow-y-auto">
              {NAV_ITEMS.map(({ to, label, icon: Icon }) => (
                <NavLink
                  key={to}
                  to={to}
                  onClick={() => setDrawerOpen(false)}
                  className={({ isActive }) =>
                    `flex items-center gap-3 px-4 py-3 rounded-lg font-body font-medium text-sm transition-all duration-150 ${
                      isActive
                        ? 'text-red bg-red-pale border border-red-glow'
                        : 'text-ink-dim hover:text-ink hover:bg-elevated border border-transparent'
                    }`
                  }
                >
                  <Icon className="h-4 w-4 flex-shrink-0" />
                  {label}
                </NavLink>
              ))}
            </nav>

            <div
              className="p-4 border-t border-border"
              style={{ paddingBottom: 'calc(1rem + env(safe-area-inset-bottom, 0px))' }}
            >
              <button
                onClick={handleLogout}
                className="flex items-center gap-3 w-full px-4 py-3 rounded-lg text-sm font-body font-medium text-red hover:bg-red-pale transition-colors touch-target"
              >
                <LogOut className="h-4 w-4 flex-shrink-0" />
                Выйти из системы
              </button>
            </div>
          </aside>
        </>
      )}

      {/* ── Bottom tab bar (mobile) ──────────────────────────────────── */}
      <nav
        className="md:hidden fixed bottom-0 left-0 right-0 z-40 bg-white border-t border-border shadow-[0_-1px_4px_#0000000A]"
        style={{ paddingBottom: 'env(safe-area-inset-bottom, 0px)' }}
      >
        <div className="flex items-stretch h-16">
          {NAV_ITEMS.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                `flex-1 flex flex-col items-center justify-center gap-1 text-[10px] font-body font-medium uppercase tracking-wide transition-colors duration-150 ${
                  isActive ? 'text-red' : 'text-ink-muted'
                }`
              }
            >
              {({ isActive }) => (
                <>
                  <span className={`flex items-center justify-center w-8 h-8 rounded-lg transition-colors ${isActive ? 'bg-red-pale' : ''}`}>
                    <Icon className="h-[18px] w-[18px]" />
                  </span>
                  {label}
                </>
              )}
            </NavLink>
          ))}
        </div>
      </nav>
    </>
  )
}
