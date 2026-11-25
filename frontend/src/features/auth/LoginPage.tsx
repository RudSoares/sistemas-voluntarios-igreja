import { useState, type FormEvent } from 'react'
import { useAuth } from './AuthContext'

export function LoginPage({ theme, onToggleTheme }: { theme: 'light' | 'dark'; onToggleTheme: () => void }) {
  const { login } = useAuth()
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  async function submit(event: FormEvent) {
    event.preventDefault(); setError(null); setLoading(true)
    try { await login(email.trim(), senha) }
    catch (e) { setError(e instanceof Error ? e.message : 'Não foi possível entrar. Confira seus dados.') }
    finally { setLoading(false) }
  }
  return <main className="login-page">
    <button aria-label={theme === 'light' ? 'Ativar modo escuro' : 'Ativar modo claro'} className="theme-toggle login-theme-toggle" onClick={onToggleTheme} type="button"><span aria-hidden="true">{theme === 'light' ? '☾' : '☀'}</span>{theme === 'light' ? 'Modo escuro' : 'Modo claro'}</button>
    <section className="login-card">
      <div className="login-brand-mark" aria-hidden="true">V</div>
      <div className="eyebrow">GESTÃO DA IGREJA</div>
      <h1>Acesse sua conta</h1>
      <p>Entre para acompanhar voluntários, programação e escalas.</p>
      {error && <div className="alert alert-error" role="alert">{error}</div>}
      <form onSubmit={submit}>
        <label className="field"><span>E-mail</span><input autoComplete="username" autoFocus required type="email" value={email} onChange={e => setEmail(e.target.value)} /></label>
        <label className="field"><span>Senha</span><input autoComplete="current-password" required type="password" value={senha} onChange={e => setSenha(e.target.value)} /></label>
        <button className="primary-button" disabled={loading} type="submit">{loading ? 'Entrando…' : 'Entrar'}</button>
      </form>
      <small className="login-footnote">Acesso protegido para sua igreja.</small>
    </section>
  </main>
}
