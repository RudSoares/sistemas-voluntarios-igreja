import { useCallback, useEffect, useState } from 'react'
import { ministryApi } from '../ministries/ministryApi'
import { volunteerApi } from '../volunteers/volunteerApi'
import { eventApi, scheduleApi, type ServiceEvent } from '../schedule/scheduleApi'

type Props = { navigate: (section: 'voluntarios' | 'ministerios' | 'agenda' | 'escalas') => void }

export function DashboardPage({ navigate }: Props) {
  const [volunteerCount, setVolunteerCount] = useState(0)
  const [ministryCount, setMinistryCount] = useState(0)
  const [eventCount, setEventCount] = useState(0)
  const [scheduleCount, setScheduleCount] = useState(0)
  const [upcoming, setUpcoming] = useState<ServiceEvent[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(async () => {
    setLoading(true); setError(null)
    try {
      const [volunteers, ministries, events, schedules] = await Promise.all([
        volunteerApi.list(), ministryApi.list(), eventApi.upcoming(), scheduleApi.list(),
      ])
      setVolunteerCount(volunteers.filter(item => item.ativo).length)
      setMinistryCount(ministries.filter(item => item.ativo).length)
      setEventCount(events.length)
      setScheduleCount(schedules.length)
      setUpcoming(events.slice(0, 4))
    } catch (e) { setError(e instanceof Error ? e.message : 'Não foi possível carregar a visão geral.') }
    finally { setLoading(false) }
  }, [])
  useEffect(() => { void load() }, [load])

  return <>
    <div className="page-heading"><div><div className="eyebrow">RESUMO DA OPERAÇÃO</div><h1 id="page-title">Visão geral</h1><p className="page-intro">Acompanhe voluntários, ministérios, programação e escalas.</p></div><button className="secondary-button" onClick={() => void load()} type="button">Atualizar dados</button></div>
    {error && <div className="alert alert-error" role="alert">{error}<button className="retry-button" onClick={() => void load()} type="button">Tentar novamente</button></div>}
    <div className="dashboard-stats" aria-busy={loading}>
      <button className="dashboard-stat" onClick={() => navigate('voluntarios')} type="button"><span className="dashboard-stat-symbol">♡</span><span className="dashboard-stat-label">Voluntários ativos</span><strong>{loading ? '—' : volunteerCount}</strong></button>
      <button className="dashboard-stat" onClick={() => navigate('ministerios')} type="button"><span className="dashboard-stat-symbol">✳</span><span className="dashboard-stat-label">Ministérios ativos</span><strong>{loading ? '—' : ministryCount}</strong></button>
      <button className="dashboard-stat" onClick={() => navigate('agenda')} type="button"><span className="dashboard-stat-symbol">▦</span><span className="dashboard-stat-label">Próximos eventos</span><strong>{loading ? '—' : eventCount}</strong></button>
      <button className="dashboard-stat" onClick={() => navigate('escalas')} type="button"><span className="dashboard-stat-symbol">☷</span><span className="dashboard-stat-label">Escalas cadastradas</span><strong>{loading ? '—' : scheduleCount}</strong></button>
    </div>
    <section className="dashboard-upcoming list-panel">
      <div className="dashboard-section-heading"><div><div className="eyebrow">AGENDA</div><h2>Próximos cultos e eventos</h2></div><button className="text-button" onClick={() => navigate('agenda')} type="button">Ver programação</button></div>
      {loading ? <div className="list-message">Carregando próximos eventos…</div> : upcoming.length === 0 ? <div className="empty-state compact-empty"><h3>Nenhum evento próximo</h3><p>Cadastre um evento para vê-lo aqui.</p><button className="secondary-button" onClick={() => navigate('agenda')} type="button">Abrir programação</button></div> : <div className="dashboard-event-list">{upcoming.map(event => <article className="dashboard-event" key={event.id}><span className="dashboard-event-date">{new Date(`${event.dataEvento}T12:00:00`).toLocaleDateString('pt-BR', { day: '2-digit', month: 'short' }).replace('.', '')}</span><div><strong>{event.titulo}</strong><span>{new Date(`${event.dataEvento}T12:00:00`).toLocaleDateString('pt-BR', { weekday: 'long' })} · {event.horario.slice(0, 5)} · {event.turnoOuDiaSemana.replaceAll('_', ' ').toLocaleLowerCase('pt-BR')}</span></div><button className="text-button" onClick={() => navigate('escalas')} type="button">Ver escala</button></article>)}</div>}
    </section>
  </>
}
