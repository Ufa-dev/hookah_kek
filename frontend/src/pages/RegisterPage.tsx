import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '@/contexts/AuthContext'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { toast } from 'sonner'
import { Mail, Lock, UserRound } from 'lucide-react'

export default function RegisterPage() {
  const { register, isLoading } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ email: '', name: '', password: '' })
  const [error, setError] = useState('')

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError('')
    if (form.password.length < 6) { setError('Пароль должен быть не менее 6 символов'); return }
    try {
      await register(form)
      toast.success('Аккаунт создан')
      navigate('/dashboard')
    } catch {
      setError('Ошибка регистрации. Email уже занят или данные некорректны.')
    }
  }

  return (
    <div
      className="min-h-dvh flex flex-col items-center justify-center px-4 bg-deep"
      style={{ paddingTop: 'env(safe-area-inset-top, 0px)', paddingBottom: 'env(safe-area-inset-bottom, 0px)' }}
    >
      <div className="w-full max-w-sm page-enter">
        <div className="text-center mb-8">
          <img src="/logo.png" className="w-14 h-14 object-contain mb-4" alt="hookahPlace" />
          <h1 className="font-display text-3xl sm:text-4xl font-bold text-red">
            hookahPlace
          </h1>
          <p className="text-xs text-ink-muted font-body mt-1.5 uppercase tracking-widest">
            Управление брендами
          </p>
        </div>

        <div className="card p-6 sm:p-8">
          <h2 className="font-display text-xl sm:text-2xl text-ink mb-6">Создать аккаунт</h2>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <Label htmlFor="name">Имя</Label>
              <div className="relative">
                <UserRound className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-ink-muted pointer-events-none" />
                <Input id="name" type="text" autoComplete="name" placeholder="Ваше имя" className="pl-10"
                  value={form.name} onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
                  required maxLength={100} />
              </div>
            </div>
            <div>
              <Label htmlFor="email">Email</Label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-ink-muted pointer-events-none" />
                <Input id="email" type="email" autoComplete="email" placeholder="you@hookahplace.ru" className="pl-10"
                  value={form.email} onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))}
                  required />
              </div>
            </div>
            <div>
              <Label htmlFor="password">Пароль</Label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-ink-muted pointer-events-none" />
                <Input id="password" type="password" autoComplete="new-password" placeholder="6–32 символа" className="pl-10"
                  value={form.password} onChange={(e) => setForm((f) => ({ ...f, password: e.target.value }))}
                  required minLength={6} maxLength={32} />
              </div>
            </div>

            {error && (
              <div className="flex items-center gap-2 text-sm text-red-dim bg-red-pale border border-red-glow rounded px-3 py-2.5">
                <span className="flex-shrink-0 w-1.5 h-1.5 rounded-full bg-red" />
                {error}
              </div>
            )}

            <Button type="submit" className="w-full mt-1" disabled={isLoading}>
              {isLoading ? 'Создание…' : 'Создать аккаунт'}
            </Button>
          </form>

          <div className="border-t border-border my-5" />

          <p className="text-center text-sm text-ink-muted font-body">
            Уже есть аккаунт?{' '}
            <Link to="/login" className="text-red hover:text-red-dim transition-colors font-medium">
              Войти
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}
