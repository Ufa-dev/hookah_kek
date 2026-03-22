import { useState, useEffect, type FormEvent } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { userApi } from '@/lib/api'
import { formatDate, getInitials } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { toast } from 'sonner'
import { Pencil, Check, X, Calendar, Mail, UserRound, Hash } from 'lucide-react'

export default function ProfilePage() {
  const qc = useQueryClient()
  const { data: user, isLoading } = useQuery({ queryKey: ['me'], queryFn: userApi.getMe })
  const [editing, setEditing] = useState(false)
  const [form, setForm] = useState({ name: '', email: '' })

  useEffect(() => { if (user) setForm({ name: user.name, email: user.email }) }, [user])

  const mutation = useMutation({
    mutationFn: userApi.updateMe,
    onSuccess: (updated) => { qc.setQueryData(['me'], updated); setEditing(false); toast.success('Профиль обновлён') },
    onError: () => toast.error('Не удалось обновить профиль'),
  })

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault()
    mutation.mutate({
      name:  form.name  !== user?.name  ? form.name  : undefined,
      email: form.email !== user?.email ? form.email : undefined,
    })
  }

  return (
    <div className="page-root">
      <div className="page-container page-enter">
        <div className="mb-8">
          <p className="text-xs text-ink-muted font-body uppercase tracking-widest mb-2">Аккаунт</p>
          <h1 className="font-display text-3xl sm:text-5xl font-bold text-red">
            Мой профиль
          </h1>
          <div className="red-line w-20 mt-4" />
        </div>

        {isLoading ? <ProfileSkeleton /> : user ? (
          <div className="space-y-4 max-w-lg">
            {/* Avatar card */}
            <div className="card p-5 sm:p-6">
              <div className="flex items-center gap-4">
                <div className="w-14 h-14 sm:w-16 sm:h-16 rounded-xl bg-red-pale border border-red-glow flex items-center justify-center flex-shrink-0">
                  <span className="font-display text-2xl sm:text-3xl text-red">{getInitials(user.name)}</span>
                </div>
                <div className="min-w-0 flex-1">
                  <p className="font-display text-xl sm:text-2xl text-ink truncate">{user.name}</p>
                  <p className="text-sm text-ink-muted truncate">{user.email}</p>
                </div>
                {!editing && (
                  <Button variant="outline" size="sm" onClick={() => setEditing(true)} className="flex-shrink-0">
                    <Pencil className="h-3.5 w-3.5" />
                    <span className="hidden xs:inline">Изменить</span>
                  </Button>
                )}
              </div>
            </div>

            {/* Edit form */}
            {editing && (
              <div className="card border-red-glow p-5 sm:p-6">
                <h2 className="font-display text-lg sm:text-xl text-ink mb-4">Редактировать профиль</h2>
                <form onSubmit={handleSubmit} className="space-y-4">
                  <div>
                    <Label htmlFor="p-name"><UserRound className="h-3 w-3 inline mr-1" /> Имя</Label>
                    <Input id="p-name" value={form.name} onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))} maxLength={100} required />
                  </div>
                  <div>
                    <Label htmlFor="p-email"><Mail className="h-3 w-3 inline mr-1" /> Email</Label>
                    <Input id="p-email" type="email" value={form.email} onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))} required />
                  </div>
                  <div className="flex gap-2 pt-1">
                    <Button type="submit" disabled={mutation.isPending}>
                      <Check className="h-3.5 w-3.5" />
                      {mutation.isPending ? 'Сохранение…' : 'Сохранить'}
                    </Button>
                    <Button type="button" variant="ghost" onClick={() => { setEditing(false); if (user) setForm({ name: user.name, email: user.email }) }}>
                      <X className="h-3.5 w-3.5" /> Отмена
                    </Button>
                  </div>
                </form>
              </div>
            )}

            {/* Meta */}
            <div className="card p-5 sm:p-6">
              <h2 className="font-display text-base sm:text-lg text-ink mb-4">Детали аккаунта</h2>
              <dl className="space-y-3 divide-y divide-border">
                <MetaRow icon={<Calendar className="h-3.5 w-3.5" />} label="Дата регистрации" value={formatDate(user.createdAt)} />
                <MetaRow icon={<Calendar className="h-3.5 w-3.5" />} label="Последнее обновление" value={formatDate(user.updatedAt)} pt />
                <MetaRow icon={<Hash className="h-3.5 w-3.5" />} label="User ID" value={typeof user.id === 'string' ? user.id : String(user.id)} mono pt />
              </dl>
            </div>
          </div>
        ) : null}
      </div>
    </div>
  )
}

function MetaRow({ icon, label, value, mono, pt }: { icon: React.ReactNode; label: string; value: string; mono?: boolean; pt?: boolean }) {
  return (
    <div className={`flex items-start justify-between gap-4 ${pt ? 'pt-3' : ''}`}>
      <dt className="flex items-center gap-1.5 text-xs text-ink-muted font-body uppercase tracking-wider flex-shrink-0">
        <span className="text-red-light">{icon}</span>{label}
      </dt>
      <dd className={`text-sm text-ink text-right break-all ${mono ? 'font-mono text-xs text-ink-dim' : ''}`}>{value}</dd>
    </div>
  )
}

function ProfileSkeleton() {
  return (
    <div className="space-y-4 max-w-lg">
      {[...Array(2)].map((_, i) => (
        <div key={i} className="card p-5 sm:p-6 space-y-4">
          <div className="flex items-center gap-4">
            <div className="skeleton w-14 h-14 rounded-xl" />
            <div className="flex-1 space-y-2">
              <div className="skeleton h-5 w-40" /><div className="skeleton h-4 w-32" />
            </div>
          </div>
        </div>
      ))}
    </div>
  )
}
