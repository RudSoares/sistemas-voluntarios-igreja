import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react'
import { ministryApi, type VolunteerMinistry } from '../ministries/ministryApi'
import { eventApi, scheduleApi, type ConfirmationStatus, type ScheduleAssignment, type ServiceEvent } from './scheduleApi'

const confirmationLabel: Record<ConfirmationStatus, string> = { PENDENTE: 'Pendente', CONFIRMADO: 'Confirmado', RECUSADO: 'Recusado' }

export function SchedulesPage() {
  const [events, setEvents] = useState<ServiceEvent[]>([])
  const [memberships, setMemberships] = useState<VolunteerMinistry[]>([])
  const [assignments, setAssignments] = useState<ScheduleAssignment[]>([])
  const [eventId, setEventId] = useState<number | null>(null)
  const [membershipId, setMembershipId] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [busyId, setBusyId] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)

  const loadAssignments = useCallback(async (id: number) => setAssignments(await scheduleApi.list(id)), [])
  const load = useCallback(async () => {
    setLoading(true); setError(null)
    try {
      const [eventItems, ministryItems] = await Promise.all([eventApi.list(), ministryApi.list()])
      const activeEvents = eventItems.filter(event => event.status === 'AGENDADO')
      const activeMinistries = ministryItems.filter(ministry => ministry.ativo)
      const linkLists = await Promise.all(activeMinistries.map(ministry => ministryApi.listMemberships(ministry.id)))
      setEvents(activeEvents); setMemberships(linkLists.flat())
      setEventId(current => activeEvents.some(event => event.id === current) ? current : activeEvents[0]?.id ?? null)
    } catch (e) { setError(e instanceof Error ? e.message : 'Não foi possível carregar os dados da escala.') }
    finally { setLoading(false) }
  }, [])
  useEffect(() => { void load() }, [load])
  useEffect(() => {
    if (!eventId) { setAssignments([]); return }
    void loadAssignments(eventId).catch(e => setError(e instanceof Error ? e.message : 'Não foi possível carregar a escala.'))
  }, [eventId, loadAssignments])

  const selectedMembership = useMemo(() => memberships.find(item => String(item.id) === membershipId), [memberships, membershipId])
  const assignedVolunteerIds = useMemo(() => new Set(assignments.map(item => item.volunteerId)), [assignments])
  const availableMemberships = useMemo(() => memberships.filter(item => !assignedVolunteerIds.has(item.volunteerId)), [memberships, assignedVolunteerIds])

  async function addAssignment(event: FormEvent) {
    event.preventDefault(); if (!eventId || !selectedMembership) return
    setSaving(true); setError(null); setNotice(null)
    try {
      const added = await scheduleApi.create(eventId, selectedMembership.ministryId, selectedMembership.ministryRoleId, selectedMembership.volunteerId)
      setAssignments(current => [...current, added]); setMembershipId('')
      setNotice(added.alertaDisponibilidade ? added.mensagemDisponibilidade ?? 'A disponibilidade não coincide com este evento.' : 'Voluntário adicionado à escala.')
    } catch (e) { setError(e instanceof Error ? e.message : 'Não foi possível adicionar à escala.') }
    finally { setSaving(false) }
  }
  async function updateConfirmation(assignment: ScheduleAssignment, statusConfirmacao: ConfirmationStatus) {
    setBusyId(assignment.id); setError(null)
    try {
      const updated = await scheduleApi.updateConfirmation(assignment.id, statusConfirmacao)
      setAssignments(current => current.map(item => item.id === updated.id ? updated : item))
    } catch (e) { setError(e instanceof Error ? e.message : 'Não foi possível atualizar a confirmação.') }
    finally { setBusyId(null) }
  }
  async function removeAssignment(assignment: ScheduleAssignment) {
    if (!window.confirm(`Remover ${assignment.volunteerNomeCompleto} da escala?`)) return
    try { await scheduleApi.delete(assignment.id); setAssignments(current => current.filter(item => item.id !== assignment.id)); setNotice('Pessoa removida da escala.') }
    catch (e) { setError(e instanceof Error ? e.message : 'Não foi possível remover da escala.') }
  }

  const selectedEvent = events.find(event => event.id === eventId)
  return <>
    <div className="page-heading"><div><div className="eyebrow">EQUIPE E SERVIÇO</div><h1 id="page-title">Escalas</h1><p className="page-intro">Monte a equipe por ministério e acompanhe as confirmações.</p></div></div>
    {error && <div className="alert alert-error" role="alert">{error}<button aria-label="Fechar mensagem" onClick={() => setError(null)} type="button">×</button></div>}
    {notice && <div className="alert alert-success" role="status">{notice}<button aria-label="Fechar mensagem" onClick={() => setNotice(null)} type="button">×</button></div>}
    {loading ? <div className="list-panel list-message">Carregando eventos e vínculos…</div> : events.length === 0 ? <div className="list-panel empty-state"><div className="empty-icon" aria-hidden="true">☷</div><h2>Nenhum evento agendado</h2><p>Cadastre um culto ou evento para começar a montar escalas.</p></div> : <>
      <div className="schedule-toolbar"><label className="field"><span>Culto ou evento</span><select value={eventId ?? ''} onChange={e => setEventId(Number(e.target.value))}>{events.map(event => <option key={event.id} value={event.id}>{event.titulo} · {new Date(`${event.dataEvento}T12:00:00`).toLocaleDateString('pt-BR')} · {event.horario.slice(0, 5)}</option>)}</select></label>{selectedEvent && <span className="schedule-shift">{selectedEvent.turnoOuDiaSemana.replaceAll('_', ' ').toLocaleLowerCase('pt-BR')}</span>}</div>
      <form className="schedule-add-form" onSubmit={addAssignment}>
        <label className="field"><span>Voluntário, ministério e função</span><select required value={membershipId} onChange={e => setMembershipId(e.target.value)}><option value="">Selecione um vínculo cadastrado</option>{availableMemberships.map(item => <option key={item.id} value={item.id}>{item.volunteerNomeCompleto} · {item.ministryNome} / {item.ministryRoleNome}</option>)}</select></label>
        <button className="primary-button" disabled={saving || !membershipId} type="submit">{saving ? 'Adicionando…' : '＋ Adicionar à escala'}</button>
      </form>
      {availableMemberships.length === 0 && <p className="schedule-hint">Todos os vínculos ativos disponíveis já estão escalados neste evento. Para adicionar outra função à mesma pessoa, a regra atual exige uma única atribuição por evento.</p>}
      <div className="list-panel schedule-list" aria-busy={busyId !== null}>
        {assignments.length === 0 ? <div className="empty-state"><h2>Escala ainda vazia</h2><p>Selecione um vínculo acima para incluir a primeira pessoa.</p></div> : <div className="schedule-assignment-list">{assignments.map(item => <article className="schedule-assignment" key={item.id}>
          <div className="assignment-person"><strong>{item.volunteerNomeCompleto}</strong><span>{item.ministryNome} · {item.ministryRoleNome}</span>{item.alertaDisponibilidade && <small className="availability-warning">⚠ {item.mensagemDisponibilidade}</small>}</div>
          <label className="confirmation-field"><span>Confirmação</span><select aria-label={`Confirmação de ${item.volunteerNomeCompleto}`} disabled={busyId === item.id} value={item.statusConfirmacao} onChange={e => void updateConfirmation(item, e.target.value as ConfirmationStatus)}>{(Object.keys(confirmationLabel) as ConfirmationStatus[]).map(status => <option key={status} value={status}>{confirmationLabel[status]}</option>)}</select></label>
          <button className="text-button text-danger" type="button" onClick={() => void removeAssignment(item)}>Remover</button>
        </article>)}</div>}
      </div>
    </>}
  </>
}
