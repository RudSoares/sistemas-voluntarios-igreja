import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { useAuth } from '../auth/AuthContext'
import { volunteerApi, type Volunteer } from '../volunteers/volunteerApi'
import { scheduleApi, type ConfirmationStatus, type ScheduleAssignment } from '../schedule/scheduleApi'

const suggestions = ['DOMINGO_MANHA', 'DOMINGO_NOITE', 'QUARTA_NOITE']
const statusLabels: Record<ConfirmationStatus, string> = { PENDENTE: 'Pendente', CONFIRMADO: 'Confirmado', RECUSADO: 'Recusado' }

export function VolunteerPortal({ theme, onToggleTheme }: { theme: 'light' | 'dark'; onToggleTheme: () => void }) {
  const { user, logout } = useAuth()
  const [profile, setProfile] = useState<Volunteer | null>(null)
  const [assignments, setAssignments] = useState<ScheduleAssignment[]>([])
  const [availability, setAvailability] = useState<string[]>([])
  const [custom, setCustom] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)
  const load = useCallback(async () => {
    setLoading(true); setError(null)
    try {
      const [me, schedule] = await Promise.all([volunteerApi.me(), scheduleApi.listMine()])
      setProfile(me); setAvailability(me.disponibilidadeDias); setAssignments(schedule)
    } catch (e) { setError(e instanceof Error ? e.message : 'Não foi possível carregar seu portal.') }
    finally { setLoading(false) }
  }, [])
  useEffect(() => { void load() }, [load])
  function toggle(day: string) {
    setAvailability(current => current.includes(day) ? current.filter(item => item !== day) : [...current, day])
  }
  function addCustom(event: FormEvent) {
    event.preventDefault()
    const normalized = custom.trim().toLocaleUpperCase('pt-BR').replace(/\s+/g, '_')
    if (normalized && !availability.includes(normalized)) setAvailability(current => [...current, normalized])
    setCustom('')
  }
  async function saveAvailability() {
    setSaving(true); setError(null); setNotice(null)
    try { const updated = await volunteerApi.updateMyAvailability(availability); setProfile(updated); setAvailability(updated.disponibilidadeDias); setNotice('Sua disponibilidade foi atualizada.') }
    catch (e) { setError(e instanceof Error ? e.message : 'Não foi possível salvar sua disponibilidade.') }
    finally { setSaving(false) }
  }
  async function respond(item: ScheduleAssignment, status: ConfirmationStatus) {
    setError(null)
    try { const updated = await scheduleApi.updateConfirmation(item.id, status); setAssignments(current => current.map(row => row.id === item.id ? updated : row)); setNotice('Resposta da escala registrada.') }
    catch (e) { setError(e instanceof Error ? e.message : 'Não foi possível responder à escala.') }
  }
  return <div className="portal-shell">
    <header className="portal-header"><a className="brand" href="#portal"><span className="brand-mark" aria-hidden="true">V</span><span className="brand-copy"><strong>Voluntários</strong><small>Portal do voluntário</small></span></a><div><span className="portal-greeting">Olá, {profile?.nomeCompleto ?? user?.nome}</span><button aria-label={theme === 'light' ? 'Ativar modo escuro' : 'Ativar modo claro'} className="theme-toggle" onClick={onToggleTheme} type="button"><span aria-hidden="true">{theme === 'light' ? '☾' : '☀'}</span>{theme === 'light' ? 'Modo escuro' : 'Modo claro'}</button><button className="secondary-button" onClick={logout} type="button">Sair (Logout)</button></div></header>
    <main className="portal-content">
      <div className="page-heading"><div><div className="eyebrow">MINHA ÁREA</div><h1>Minhas escalas</h1><p className="page-intro">Consulte seus próximos serviços e informe sua disponibilidade.</p></div></div>
      {error && <div className="alert alert-error" role="alert">{error}<button aria-label="Fechar mensagem" onClick={() => setError(null)} type="button">×</button></div>}{notice && <div className="alert alert-success" role="status">{notice}<button aria-label="Fechar mensagem" onClick={() => setNotice(null)} type="button">×</button></div>}
      <section className="portal-section"><div className="portal-section-heading"><div><div className="eyebrow">AGENDA PESSOAL</div><h2>Suas próximas escalas</h2></div></div>
        {loading ? <div className="list-message">Carregando escalas…</div> : assignments.length === 0 ? <div className="empty-state compact-empty"><h3>Você ainda não tem escalas</h3><p>Quando for escalado, o evento aparecerá aqui.</p></div> : <div className="portal-assignment-list">{assignments.map(item => <article className="portal-assignment" key={item.id}><div className="portal-event-date">{new Date(item.dataEvento + 'T12:00:00').toLocaleDateString('pt-BR', { day: '2-digit', month: 'short' }).replace('.', '')}</div><div className="portal-assignment-copy"><strong>{item.eventTitulo}</strong><span>{new Date(item.dataEvento + 'T12:00:00').toLocaleDateString('pt-BR', { weekday: 'long', day: 'numeric', month: 'long' })} · {item.horario.slice(0, 5)}</span><span>{item.ministryNome} · {item.ministryRoleNome}</span>{item.alertaDisponibilidade && <small className="availability-warning">{item.mensagemDisponibilidade}</small>}</div><div className="portal-confirm-actions"><span className={'status-badge ' + (item.statusConfirmacao === 'CONFIRMADO' ? 'status-active' : 'status-inactive')}>{statusLabels[item.statusConfirmacao]}</span><button className="text-button" disabled={item.statusConfirmacao === 'CONFIRMADO'} onClick={() => void respond(item, 'CONFIRMADO')} type="button">Confirmar</button><button className="text-button text-danger" disabled={item.statusConfirmacao === 'RECUSADO'} onClick={() => void respond(item, 'RECUSADO')} type="button">Recusar</button></div></article>)}</div>}
      </section>
      <section className="portal-section"><div className="portal-section-heading"><div><div className="eyebrow">PREFERÊNCIAS</div><h2>Minha disponibilidade</h2><p>Marque os períodos em que geralmente pode servir.</p></div></div>
        <div className="portal-availability">{suggestions.map(day => <button className={'availability-option ' + (availability.includes(day) ? 'is-selected' : '')} key={day} onClick={() => toggle(day)} type="button">{day.replaceAll('_', ' ').toLocaleLowerCase('pt-BR')}</button>)}{availability.filter(day => !suggestions.includes(day)).map(day => <span className="day-chip" key={day}>{day}<button aria-label={'Remover ' + day} onClick={() => toggle(day)} type="button">×</button></span>)}</div>
        <form className="custom-day-entry portal-custom-day" onSubmit={addCustom}><input aria-label="Adicionar outro período" maxLength={40} placeholder="Outro período, ex.: SEGUNDA_NOITE" value={custom} onChange={e => setCustom(e.target.value)} /><button className="secondary-button" type="submit">Adicionar</button></form>
        <div className="form-actions"><button className="primary-button" disabled={saving || !profile} onClick={() => void saveAvailability()} type="button">{saving ? 'Salvando…' : 'Salvar disponibilidade'}</button></div>
      </section>
    </main>
  </div>
}
