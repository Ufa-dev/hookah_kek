import { createContext, useContext, useState, useCallback, useEffect, type ReactNode } from 'react'
import { authApi, tokenStorage, setAuthExpiredHandler } from '@/lib/api'
import type { AuthResponse, LoginRequest, RegisterRequest } from '@/types'
import { queryClient } from '@/lib/queryClient'

interface AuthState {
  isAuthenticated: boolean
  isLoading: boolean
}

interface AuthContextValue extends AuthState {
  login: (body: LoginRequest) => Promise<void>
  register: (body: RegisterRequest) => Promise<void>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>({
    isAuthenticated: !!tokenStorage.getAccess(),
    isLoading: false,
  })

  const handleAuth = useCallback((auth: AuthResponse) => {
    tokenStorage.set(auth)
    setState({ isAuthenticated: true, isLoading: false })
  }, [])

  const login = useCallback(
    async (body: LoginRequest) => {
      setState((s) => ({ ...s, isLoading: true }))
      try {
        const auth = await authApi.login(body)
        handleAuth(auth)
      } catch (e) {
        setState((s) => ({ ...s, isLoading: false }))
        throw e
      }
    },
    [handleAuth],
  )

  const register = useCallback(
    async (body: RegisterRequest) => {
      setState((s) => ({ ...s, isLoading: true }))
      try {
        const auth = await authApi.register(body)
        handleAuth(auth)
      } catch (e) {
        setState((s) => ({ ...s, isLoading: false }))
        throw e
      }
    },
    [handleAuth],
  )

  const logout = useCallback(async () => {
    const refreshToken = tokenStorage.getRefresh()
    if (refreshToken) {
      try {
        await authApi.logout({ refreshToken })
      } catch {
        // ignore – clear local state regardless
      }
    }
    tokenStorage.clear()
    queryClient.clear()
    setState({ isAuthenticated: false, isLoading: false })
  }, [])

  // Register the forced-logout handler so the axios interceptor can trigger
  // a proper React logout instead of a hard window.location redirect
  useEffect(() => {
    setAuthExpiredHandler(() => {
      queryClient.clear()
      setState({ isAuthenticated: false, isLoading: false })
    })
  }, [])

  return (
    <AuthContext.Provider value={{ ...state, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside <AuthProvider>')
  return ctx
}
