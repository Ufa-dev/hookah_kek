import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { userApi, brandApi } from '@/lib/api'
import { formatDate, getInitials } from '@/lib/utils'
import { ArrowRight, Package, Tag, UserRound, Calendar } from 'lucide-react'

export default function DashboardPage() {
  const { data: user, isLoading: loadingUser } = useQuery({ queryKey: ['me'], queryFn: userApi.getMe })
  const { data: brandsSlice } = useQuery({
    queryKey: ['brands-count'],
    queryFn: () => brandApi.list({ limit: 1 }),
  })
  const { data: tagsSlice } = useQuery({
    queryKey: ['tags-count'],
    queryFn: () => tagApi_list(),
  })

  // Use the infinite query cache to derive counts — or show from first page
  return (
    <div className="page-root">
      <div className="page-container page-enter">

        {/* Greeting */}
        <div className="mb-8 sm:mb-10">
          {loadingUser ? (
            <div className="skeleton h-10 w-56 mb-2" />
          ) : (
            <h1 className="font-display text-3xl sm:text-5xl text-ink leading-tight">
              Привет,{' '}
              <span className="text-red">{user?.name ?? 'гость'}</span>
            </h1>
          )}
          <p className="text-sm text-ink-dim font-body mt-2">Панель управления hookahPlace Studio</p>
          <div className="red-line w-20 mt-4" />
        </div>

        {/* Quick nav cards */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4 mb-6">
          <QuickLink to="/admin/brands" label="Управлять брендами" description="Создавать, редактировать, тегировать" icon={<Package className="h-5 w-5 text-red" />} />
          <QuickLink to="/admin/tags"   label="Управлять тегами"   description="Создавать и переименовывать теги"    icon={<Tag className="h-5 w-5 text-red" />} />
        </div>

        {/* User card */}
        {user && (
          <div className="card p-5 sm:p-6">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 sm:w-14 sm:h-14 rounded-xl bg-red-pale border border-red-glow flex items-center justify-center flex-shrink-0">
                <span className="font-display text-xl sm:text-2xl text-red">
                  {getInitials(user.name)}
                </span>
              </div>
              <div className="min-w-0 flex-1">
                <p className="font-display text-lg sm:text-xl text-ink truncate">{user.name}</p>
                <p className="text-xs sm:text-sm text-ink-muted truncate">{user.email}</p>
                <div className="flex items-center gap-1 mt-1">
                  <Calendar className="h-3 w-3 text-ink-muted flex-shrink-0" />
                  <span className="text-xs text-ink-muted">С {formatDate(user.createdAt)}</span>
                </div>
              </div>
              <Link to="/profile" className="ml-auto flex-shrink-0 flex items-center gap-1.5 text-xs text-red hover:text-red-dim font-body font-medium transition-colors">
                Изменить <ArrowRight className="h-3.5 w-3.5" />
              </Link>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

// Stub — avoids importing tagApi directly (already in lib/api)
function tagApi_list() {
  return import('@/lib/api').then((m) => m.tagApi.list({ limit: 1 }))
}

function QuickLink({ to, label, description, icon }: { to: string; label: string; description: string; icon: React.ReactNode }) {
  return (
    <Link to={to} className="card-hover p-4 sm:p-5 flex items-center gap-4 group">
      <div className="p-2.5 rounded-xl bg-red-pale border border-red-glow flex-shrink-0">{icon}</div>
      <div className="min-w-0">
        <p className="font-body font-medium text-ink text-sm group-hover:text-red transition-colors truncate">{label}</p>
        <p className="text-xs text-ink-muted truncate">{description}</p>
      </div>
      <ArrowRight className="h-4 w-4 text-ink-muted group-hover:text-red ml-auto flex-shrink-0 group-hover:translate-x-0.5 transition-all" />
    </Link>
  )
}
