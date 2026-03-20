import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '@/contexts/AuthContext'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { toast } from 'sonner'
import { Flame, Mail, Lock } from 'lucide-react'

export default function LoginPage() {
  const { login, isLoading } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError('')
    try {
      await login(form)
      toast.success('Добро пожаловать')
      navigate('/dashboard')
    } catch {
      setError('Неверный email или пароль')
    }
  }

  return (
    <div
      className="min-h-dvh flex flex-col items-center justify-center px-4 bg-deep"
      style={{ paddingTop: 'env(safe-area-inset-top, 0px)', paddingBottom: 'env(safe-area-inset-bottom, 0px)' }}
    >
      <div className="w-full max-w-sm page-enter">
        {/* Brand mark */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-14 h-14 rounded-xl bg-red-pale border border-red-glow shadow-red-sm mb-4">
            <Flame className="h-6 w-6 text-red" />
          </div>
          <h1 className="font-display text-3xl sm:text-4xl text-ink">
            hookah<span className="text-red">Place</span>
          </h1>
          <p className="text-xs text-ink-muted font-body mt-1.5 uppercase tracking-widest">
            Управление брендами
          </p>
        </div>

        <div className="card p-6 sm:p-8">
          <h2 className="font-display text-xl sm:text-2xl text-ink mb-6">Вход в систему</h2>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <Label htmlFor="email">Email</Label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-ink-muted pointer-events-none" />
                <Input
                  id="email" type="email" autoComplete="email"
                  placeholder="you@hookahplace.ru" className="pl-10"
                  value={form.email}
                  onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))}
                  required
                />
              </div>
            </div>

            <div>
              <Label htmlFor="password">Пароль</Label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-ink-muted pointer-events-none" />
                <Input
                  id="password" type="password" autoComplete="current-password"
                  placeholder="••••••••" className="pl-10"
                  value={form.password}
                  onChange={(e) => setForm((f) => ({ ...f, password: e.target.value }))}
                  required
                />
              </div>
            </div>

            {error && (
              <div className="flex items-center gap-2 text-sm text-red-dim bg-red-pale border border-red-glow rounded px-3 py-2.5">
                <span className="flex-shrink-0 w-1.5 h-1.5 rounded-full bg-red" />
                {error}
              </div>
            )}

            <Button type="submit" className="w-full mt-1" disabled={isLoading}>
              {isLoading ? 'Вход…' : 'Войти'}
            </Button>
          </form>

          <div className="border-t border-border my-5" />

          <p className="text-center text-sm text-ink-muted font-body">
            Нет аккаунта?{' '}
            <Link to="/register" className="text-red hover:text-red-dim transition-colors font-medium">
              Зарегистрироваться
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}
