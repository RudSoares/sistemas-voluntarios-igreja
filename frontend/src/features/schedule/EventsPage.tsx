import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { eventApi, type ServiceEvent, type ServiceEventPayload } from './scheduleApi'

const shifts = ['DOMINGO_MANHA', 'DOMINGO_NOITE', 'QUARTA_NOITE']
const emptyForm = (): ServiceEventPayload => ({ titulo: '', dataEvento: '', horario: '', turnoOuDiaSemana: shifts[0], observacoes: '' })

export function EventsPage() {
  const [events, setEvents] = useState<ServiceEvent[]>([])
  const [form, setForm] = useState<ServiceEventPayload>(emptyForm)
  const [editing, setEditing] = useState<ServiceEvent | null>(null)
  const [formOpen, setFormOpen] = useState(false)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(async () => {
    setLoading(true)
    try { setEvents(await eventApi.list()); setError(null) }
    catch (e) { setError(e instanceof Error ? e.message : 'Não foi possível carregar os eventos.') }
    finally { setLoading(false) }
  }, [])
  useEffect(() => { void load() }, [load])

  function openCreate() { setEditing(null); setForm(emptyForm()); setFormOpen(true); setError(null) }
  function openEdit(event: ServiceEvent) {
    setEditing(event)
    setForm({ titulo: event.titulo, dataEvento: event.dataEvento, horario: event.horario.slice(0, 5), turnoOuDiaSemana: event.turnoOuDiaSemana, observacoes: event.observacoes ?? '' })
    setFormOpen(true); setError(null)
  }
  async function save(event: FormEvent) {
    event.preventDefault(); setSaving(true); setError(null)
    try {
      const payload = { ...form, titulo: form.titulo.trim(), observacoes: form.observacoes.trim() || null }
      if (editing) await eventApi.update(editing.id, payload)
      else await eventApi.create(payload)
      setFormOpen(false); await load()
    } catch (e) { setError(e instanceof Error ? e.message : 'Não foi possível salvar o evento.') }
    finally { setSaving(false) }
  }
  async function cancel(event: ServiceEvent) {
    if (!window.confirm(`Cancelar “${event.titulo}”?`)) return
    try { await eventApi.updateStatus(event.id, 'CANCELADO'); await load() }
    catch (e) { setError(e instanceof Error ? e.message : 'Não foi possível cancelar o evento.') }
  }
  async function remove(event: ServiceEvent) {
    if (!window.confirm(`Excluir “${event.titulo}”?`)) return
    try { await eventApi.delete(event.id); await load() }
    catch (e) { setError(e instanceof Error ? e.message : 'Não foi possível excluir o evento.') }
  }

  return <>
    <div className="page-heading">
      <div><div className="eyebrow">PROGRAMAÇÃO</div><h1 id="page-title">Cultos e eventos</h1><p className="page-intro">Organize as datas e os horários da programação da igreja.</p></div>
      <button className="primary-button" onClick={openCreate} type="button"><span aria-hidden="true">＋</span> Novo evento</button>
    </div>
    {error && <div className="alert alert-error" role="alert">{error}<button aria-label="Fechar mensagem" onClick={() => setError(null)} type="button">×</button></div>}
    {formOpen && <form className="volunteer-form" onSubmit={save}>
      <div className="form-heading"><div><div className="eyebrow">{editing ? 'ATUALIZAÇÃO' : 'NOVO CADASTRO'}</div><h2>{editing ? 'Editar culto ou evento' : 'Cadastrar culto ou evento'}</h2></div><button className="icon-button" aria-label="Fechar formulário" onClick={() => setFormOpen(false)} type="button">×</button></div>
      <div className="form-grid">
        <label className="field field-full"><span>Título *</span><input required maxLength={150} value={form.titulo} onChange={e => setForm({ ...form, titulo: e.target.value })} placeholder="Ex.: Culto de Celebração" /></label>
        <label className="field"><span>Data *</span><input required type="date" value={form.dataEvento} onChange={e => setForm({ ...form, dataEvento: e.target.value })} /></label>
        <label className="field"><span>Horário *</span><input required type="time" value={form.horario} onChange={e => setForm({ ...form, horario: e.target.value })} /></label>
        <label className="field"><span>Turno ou dia da semana *</span><input required list="event-shifts" maxLength={40} value={form.turnoOuDiaSemana} onChange={e => setForm({ ...form, turnoOuDiaSemana: e.target.value.toUpperCase() })} placeholder="DOMINGO_MANHA" /><datalist id="event-shifts">{shifts.map(shift => <option key={shift} value={shift} />)}</datalist></label>
        <label className="field field-full"><span>Observações</span><textarea rows={3} value={form.observacoes ?? ''} onChange={e => setForm({ ...form, observacoes: e.target.value })} /></label>
      </div>
      <div className="form-actions"><button className="secondary-button" type="button" onClick={() => setFormOpen(false)}>Cancelar</button><button className="primary-button" disabled={saving} type="submit">{saving ? 'Salvando…' : editing ? 'Salvar alterações' : 'Cadastrar evento'}</button></div>
    </form>}
    <div aria-busy={loading} className="list-panel">
      {loading ? <div className="list-message">Carregando programação…</div> : events.length === 0 ? <div className="empty-state"><div className="empty-icon" aria-hidden="true">▦</div><h2>Nenhum culto ou evento cadastrado</h2><p>Adicione o primeiro evento para começar a montar as escalas.</p><button className="primary-button" onClick={openCreate} type="button">Cadastrar evento</button></div> :
        <div className="event-list">{events.map(event => <article className="event-card" key={event.id}>
          <div className="event-date"><strong>{new Date(`${event.dataEvento}T12:00:00`).toLocaleDateString('pt-BR', { day: '2-digit' })}</strong><span>{new Date(`${event.dataEvento}T12:00:00`).toLocaleDateString('pt-BR', { month: 'short' }).replace('.', '')}</span></div>
          <div className="event-main"><div className="event-card-heading"><h2>{event.titulo}</h2><span className={`status-badge ${event.status === 'AGENDADO' ? 'status-active' : 'status-inactive'}`}>{event.status === 'AGENDADO' ? 'Agendado' : event.status === 'CANCELADO' ? 'Cancelado' : 'Concluído'}</span></div><p>{new Date(`${event.dataEvento}T12:00:00`).toLocaleDateString('pt-BR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })} · {event.horario.slice(0, 5)} · {event.turnoOuDiaSemana.replaceAll('_', ' ').toLocaleLowerCase('pt-BR')}</p>{event.observacoes && <small>{event.observacoes}</small>}</div>
          <div className="row-actions">{event.status === 'AGENDADO' && <><button className="text-button" type="button" onClick={() => openEdit(event)}>Editar</button><button className="text-button" type="button" onClick={() => void cancel(event)}>Cancelar evento</button></>}<button className="text-button text-danger" type="button" onClick={() => void remove(event)}>Excluir</button></div>
        </article>)}</div>}
    </div>
  </>
}
