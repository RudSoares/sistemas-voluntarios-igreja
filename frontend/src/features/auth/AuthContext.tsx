import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { jsonRequest, request } from '../../services/apiClient'

export type UserRole = 'SUPER_ADMIN' | 'CHURCH_ADMIN' | 'VOLUNTEER'
export type AuthUser = {
  nome: string
  email: string
  role: UserRole
  churchId: number | null
  branchId: number | null
}
type LoginResponse = AuthUser & { token: string }
type AuthContextValue = { user: AuthUser | null; login: (email: string, senha: string) => Promise<void>; logout: () => void }
const AuthContext = createContext<AuthContextValue | null>(null)

function loadSavedUser(): AuthUser | null {
  try {
    const raw = window.localStorage.getItem('voluntarios-user')
    const token = window.localStorage.getItem('voluntarios-token')
    return raw && token ? JSON.parse(raw) as AuthUser : null
  } catch { return null }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(loadSavedUser)
  const logout = useCallback(() => {
    setUser(null)
    try { window.localStorage.removeItem('voluntarios-token'); window.localStorage.removeItem('voluntarios-user') } catch { /* no-op */ }
  }, [])
  useEffect(() => {
    window.addEventListener('voluntarios:unauthorized', logout)
    return () => window.removeEventListener('voluntarios:unauthorized', logout)
  }, [logout])
  const login = useCallback(async (email: string, senha: string) => {
    const result = await request<LoginResponse>('/api/v1/auth/login', jsonRequest('POST', { email, senha }))
    const { token, ...profile } = result
    try {
      window.localStorage.setItem('voluntarios-token', token)
      window.localStorage.setItem('voluntarios-user', JSON.stringify(profile))
    } catch {
      try { window.localStorage.removeItem('voluntarios-token'); window.localStorage.removeItem('voluntarios-user') } catch { /* no-op */ }
      throw new Error('O navegador bloqueou o armazenamento da sessão. Habilite o armazenamento local e tente novamente.')
    }
    setUser(profile)
  }, [])
  const value = useMemo(() => ({ user, login, logout }), [user, login, logout])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const value = useContext(AuthContext)
  if (!value) throw new Error('useAuth deve ser usado dentro de AuthProvider.')
  return value
}
