import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react'
import { volunteerApi, type Volunteer, type VolunteerPayload } from './features/volunteers/volunteerApi'
import { MinistriesPage } from './features/ministries/MinistriesPage'
import { EventsPage } from './features/schedule/EventsPage'
import { SchedulesPage } from './features/schedule/SchedulesPage'
import { DashboardPage } from './features/dashboard/DashboardPage'
import './App.css'

const sections = [
  { id: 'inicio', label: 'Visão geral', symbol: '⌂' },
  { id: 'voluntarios', label: 'Voluntários', symbol: '♡' },
  { id: 'ministerios', label: 'Ministérios', symbol: '✳' },
  { id: 'agenda', label: 'Cultos e eventos', symbol: '▦' },
  { id: 'escalas', label: 'Escalas', symbol: '☷' },
] as const

type SectionId = (typeof sections)[number]['id']
type Theme = 'light' | 'dark'
type VolunteerForm = Omit<VolunteerPayload, 'telefone' | 'email' | 'observacoes'> & {
  telefone: string
  email: string
  observacoes: string
}

const suggestedAvailability = ['DOMINGO_MANHA', 'DOMINGO_NOITE', 'QUARTA_NOITE']
const availabilityLabels: Record<string, string> = {
  DOMINGO_MANHA: 'domingo de manhã',
  DOMINGO_NOITE: 'domingo à noite',
  QUARTA_NOITE: 'quarta à noite',
}

function displayAvailability(day: string) {
  return availabilityLabels[day] ?? day.replaceAll('_', ' ').toLocaleLowerCase('pt-BR')
}

function emptyForm(): VolunteerForm {
  return {
    nomeCompleto: '',
    telefone: '',
    email: '',
    disponibilidadeDias: [],
    observacoes: '',
  }
}

function formFromVolunteer(volunteer: Volunteer): VolunteerForm {
  return {
    nomeCompleto: volunteer.nomeCompleto,
    telefone: volunteer.telefone ?? '',
    email: volunteer.email ?? '',
    disponibilidadeDias: volunteer.disponibilidadeDias,
    observacoes: volunteer.observacoes ?? '',
  }
}

function toPayload(form: VolunteerForm): VolunteerPayload {
  return {
    nomeCompleto: form.nomeCompleto.trim(),
    telefone: form.telefone.trim() || null,
    email: form.email.trim() || null,
    disponibilidadeDias: [...new Set(form.disponibilidadeDias.map((day) => day.trim()).filter(Boolean))],
    observacoes: form.observacoes.trim() || null,
  }
}

function App() {
  const [activeSection, setActiveSection] = useState<SectionId>('inicio')
  const [theme, setTheme] = useState<Theme>(() => {
    try {
      return window.localStorage.getItem('voluntarios-theme') === 'dark' ? 'dark' : 'light'
    } catch {
      return 'light'
    }
  })
  const [volunteers, setVolunteers] = useState<Volunteer[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [search, setSearch] = useState('')
  const [isFormOpen, setIsFormOpen] = useState(false)
  const [editingVolunteer, setEditingVolunteer] = useState<Volunteer | null>(null)
  const [form, setForm] = useState<VolunteerForm>(emptyForm)
  const [customAvailability, setCustomAvailability] = useState('')
  const [isSaving, setIsSaving] = useState(false)
  const [updatingStatusId, setUpdatingStatusId] = useState<number | null>(null)

  const loadVolunteers = useCallback(async () => {
    setIsLoading(true)
    setLoadError(null)
    try {
      setVolunteers(await volunteerApi.list())
    } catch (error) {
      setLoadError(error instanceof Error ? error.message : 'Não foi possível carregar os voluntários.')
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    void loadVolunteers()
  }, [loadVolunteers])

  useEffect(() => {
    document.documentElement.dataset.theme = theme
    document.querySelector('meta[name="theme-color"]')?.setAttribute(
      'content', theme === 'dark' ? '#101a17' : '#123b35',
    )
    try {
      window.localStorage.setItem('voluntarios-theme', theme)
    } catch {
      // A aplicação permanece utilizável mesmo se o navegador bloquear o armazenamento local.
    }
  }, [theme])

  const currentSection = sections.find((section) => section.id === activeSection)!
  const activeCount = volunteers.filter((volunteer) => volunteer.ativo).length
  const filteredVolunteers = useMemo(() => {
    const term = search.trim().toLocaleLowerCase('pt-BR')
    if (!term) return volunteers
    return volunteers.filter((volunteer) =>
      [volunteer.nomeCompleto, volunteer.telefone, volunteer.email]
        .some((value) => value?.toLocaleLowerCase('pt-BR').includes(term)),
    )
  }, [search, volunteers])

  function openCreateForm() {
    setEditingVolunteer(null)
    setForm(emptyForm())
    setCustomAvailability('')
    setActionError(null)
    setIsFormOpen(true)
  }

  function openEditForm(volunteer: Volunteer) {
    setEditingVolunteer(volunteer)
    setForm(formFromVolunteer(volunteer))
    setCustomAvailability('')
    setActionError(null)
    setIsFormOpen(true)
  }

  function closeForm() {
    setIsFormOpen(false)
    setEditingVolunteer(null)
    setActionError(null)
  }

  function toggleAvailability(day: string) {
    setForm((current) => ({
      ...current,
      disponibilidadeDias: current.disponibilidadeDias.includes(day)
        ? current.disponibilidadeDias.filter((value) => value !== day)
        : [...current.disponibilidadeDias, day],
    }))
  }

  function addCustomAvailability() {
    const value = customAvailability.trim().toLocaleUpperCase('pt-BR').replace(/\s+/g, '_')
    if (value && !form.disponibilidadeDias.includes(value)) {
      setForm((current) => ({ ...current, disponibilidadeDias: [...current.disponibilidadeDias, value] }))
    }
    setCustomAvailability('')
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setActionError(null)
    setIsSaving(true)
    try {
      const payload = toPayload(form)
      if (editingVolunteer) {
        await volunteerApi.update(editingVolunteer.id, payload)
      } else {
        await volunteerApi.create(payload)
      }
      closeForm()
      await loadVolunteers()
    } catch (error) {
      setActionError(error instanceof Error ? error.message : 'Não foi possível salvar o voluntário.')
    } finally {
      setIsSaving(false)
    }
  }

  async function toggleStatus(volunteer: Volunteer) {
    setActionError(null)
    setUpdatingStatusId(volunteer.id)
    try {
      const updated = await volunteerApi.updateStatus(volunteer.id, !volunteer.ativo)
      setVolunteers((current) => current.map((item) => item.id === updated.id ? updated : item))
    } catch (error) {
      setActionError(error instanceof Error ? error.message : 'Não foi possível alterar o status.')
    } finally {
      setUpdatingStatusId(null)
    }
  }

  return (
    <div className="app-shell" data-theme={theme}>
      <aside className="sidebar">
        <a className="brand" href="#voluntarios" onClick={() => setActiveSection('voluntarios')}>
          <span className="brand-mark" aria-hidden="true">V</span>
          <span className="brand-copy">
            <strong>Voluntários</strong>
            <small>Gestão da igreja</small>
          </span>
        </a>

        <div className="nav-caption">MENU</div>
        <nav aria-label="Navegação principal">
          {sections.map((section) => (
            <button
              aria-current={activeSection === section.id ? 'page' : undefined}
              className={`nav-item ${activeSection === section.id ? 'is-active' : ''}`}
              key={section.id}
              onClick={() => setActiveSection(section.id)}
              type="button"
            >
              <span aria-hidden="true" className="nav-symbol">{section.symbol}</span>
              <span>{section.label}</span>
              {section.id === 'voluntarios' && volunteers.length > 0 && (
                <span className="nav-count">{volunteers.length}</span>
              )}
            </button>
          ))}
        </nav>

        <div className="sidebar-note">
          <span className="note-dot" aria-hidden="true" />
          <span>Módulos em desenvolvimento</span>
        </div>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <div className="breadcrumb">Sistema <span>/</span> {currentSection.label}</div>
          <div className="topbar-actions">
            <button
              aria-label={theme === 'light' ? 'Ativar modo escuro' : 'Ativar modo claro'}
              aria-pressed={theme === 'dark'}
              className="theme-toggle"
              onClick={() => setTheme((current) => current === 'light' ? 'dark' : 'light')}
              type="button"
            >
              <span aria-hidden="true">{theme === 'light' ? '☾' : '☀'}</span>
              {theme === 'light' ? 'Modo escuro' : 'Modo claro'}
            </button>
            <div className="environment-label"><span /> Ambiente de desenvolvimento</div>
          </div>
        </header>

        <section className="page-content" aria-labelledby="page-title">
          {activeSection === 'inicio' ? (
            <DashboardPage navigate={(section) => setActiveSection(section)} />
          ) : activeSection === 'ministerios' ? (
            <MinistriesPage onGoToVolunteers={() => setActiveSection('voluntarios')} />
          ) : activeSection === 'agenda' ? (
            <EventsPage />
          ) : activeSection === 'escalas' ? (
            <SchedulesPage />
          ) : activeSection === 'voluntarios' ? (
            <>
              <div className="page-heading">
                <div>
                  <div className="eyebrow">CADASTRO E EQUIPE</div>
                  <h1 id="page-title">Voluntários</h1>
                  <p className="page-intro">Mantenha os contatos e a disponibilidade da equipe em um só lugar.</p>
                </div>
                <button className="primary-button" onClick={openCreateForm} type="button">
                  <span aria-hidden="true">＋</span> Novo voluntário
                </button>
              </div>

              {actionError && (
                <div className="alert alert-error" role="alert">
                  <span>{actionError}</span>
                  <button aria-label="Fechar mensagem" onClick={() => setActionError(null)} type="button">×</button>
                </div>
              )}

              {isFormOpen && (
                <form className="volunteer-form" onSubmit={handleSubmit}>
                  <div className="form-heading">
                    <div>
                      <div className="eyebrow">{editingVolunteer ? 'ATUALIZAÇÃO' : 'NOVO CADASTRO'}</div>
                      <h2>{editingVolunteer ? 'Editar voluntário' : 'Cadastrar voluntário'}</h2>
                    </div>
                    <button aria-label="Fechar formulário" className="icon-button" onClick={closeForm} type="button">×</button>
                  </div>

                  <div className="form-grid">
                    <label className="field field-full">
                      <span>Nome completo <b aria-hidden="true">*</b></span>
                      <input
                        autoFocus
                        autoComplete="name"
                        maxLength={150}
                        onChange={(event) => setForm((current) => ({ ...current, nomeCompleto: event.target.value }))}
                        placeholder="Nome e sobrenome"
                        required
                        value={form.nomeCompleto}
                      />
                    </label>
                    <label className="field">
                      <span>Telefone / WhatsApp</span>
                      <input
                        autoComplete="tel"
                        maxLength={40}
                        onChange={(event) => setForm((current) => ({ ...current, telefone: event.target.value }))}
                        placeholder="(00) 00000-0000"
                        type="tel"
                        value={form.telefone}
                      />
                    </label>
                    <label className="field">
                      <span>E-mail</span>
                      <input
                        autoComplete="email"
                        maxLength={254}
                        onChange={(event) => setForm((current) => ({ ...current, email: event.target.value }))}
                        placeholder="nome@exemplo.com"
                        type="email"
                        value={form.email}
                      />
                    </label>
                    <fieldset className="field field-full availability-field">
                      <legend>Disponibilidade</legend>
                      <div className="availability-options">
                        {suggestedAvailability.map((day) => (
                          <button
                            aria-pressed={form.disponibilidadeDias.includes(day)}
                            className={`availability-option ${form.disponibilidadeDias.includes(day) ? 'is-selected' : ''}`}
                            key={day}
                            onClick={() => toggleAvailability(day)}
                            type="button"
                          >
                            {displayAvailability(day)}
                          </button>
                        ))}
                      </div>
                      <div className="custom-day-entry">
                        <input
                          aria-label="Adicionar outro período de disponibilidade"
                          maxLength={40}
                          onChange={(event) => setCustomAvailability(event.target.value)}
                          onKeyDown={(event) => {
                            if (event.key === 'Enter') {
                              event.preventDefault()
                              addCustomAvailability()
                            }
                          }}
                          placeholder="Outro período, ex.: SEGUNDA_NOITE"
                          value={customAvailability}
                        />
                        <button className="secondary-button" onClick={addCustomAvailability} type="button">Adicionar</button>
                      </div>
                      {form.disponibilidadeDias.length > 0 && (
                        <div aria-label="Períodos selecionados" className="selected-days">
                          {form.disponibilidadeDias.map((day) => (
                            <span className="day-chip" key={day}>
                              {day}
                              <button aria-label={`Remover ${day}`} onClick={() => toggleAvailability(day)} type="button">×</button>
                            </span>
                          ))}
                        </div>
                      )}
                    </fieldset>
                    <label className="field field-full">
                      <span>Observações</span>
                      <textarea
                        onChange={(event) => setForm((current) => ({ ...current, observacoes: event.target.value }))}
                        placeholder="Informações úteis para a equipe"
                        rows={3}
                        value={form.observacoes}
                      />
                    </label>
                  </div>

                  <div className="form-actions">
                    <button className="secondary-button" onClick={closeForm} type="button">Cancelar</button>
                    <button className="primary-button" disabled={isSaving} type="submit">
                      {isSaving ? 'Salvando…' : editingVolunteer ? 'Salvar alterações' : 'Cadastrar voluntário'}
                    </button>
                  </div>
                </form>
              )}

              <div className="summary-row">
                <div className="summary-copy">
                  <strong>{isLoading || loadError ? '—' : volunteers.length}</strong> {volunteers.length === 1 ? 'voluntário cadastrado' : 'voluntários cadastrados'}
                  <span className="summary-separator">·</span>
                  <strong>{isLoading || loadError ? '—' : activeCount}</strong> ativos
                </div>
                <label className="search-field">
                  <span aria-hidden="true">⌕</span>
                  <input
                    aria-label="Buscar voluntários"
                    onChange={(event) => setSearch(event.target.value)}
                    placeholder="Buscar por nome ou contato"
                    type="search"
                    value={search}
                  />
                </label>
              </div>

              {loadError && (
                <div className="alert alert-error" role="alert">
                  <span>{loadError}</span>
                  <button className="retry-button" onClick={() => void loadVolunteers()} type="button">Tentar novamente</button>
                </div>
              )}

              <div aria-busy={isLoading} className="list-panel">
                {isLoading ? (
                  <div className="list-message">Carregando voluntários…</div>
                ) : loadError ? null : filteredVolunteers.length === 0 ? (
                  <div className="empty-state">
                    <div className="empty-icon" aria-hidden="true">♡</div>
                    <h2>{search ? 'Nenhum resultado encontrado' : 'Ainda não há voluntários cadastrados'}</h2>
                    <p>{search ? 'Tente buscar por outro nome ou contato.' : 'Cadastre a primeira pessoa para começar a organizar a equipe.'}</p>
                    {!search && <button className="primary-button" onClick={openCreateForm} type="button">Cadastrar primeiro voluntário</button>}
                  </div>
                ) : (
                  <>
                    <div className="desktop-table-wrap">
                      <table className="volunteers-table">
                        <thead>
                          <tr>
                            <th scope="col">Voluntário</th>
                            <th scope="col">Telefone / WhatsApp</th>
                            <th scope="col">Disponibilidade</th>
                            <th scope="col">Status</th>
                            <th scope="col"><span className="visually-hidden">Ações</span></th>
                          </tr>
                        </thead>
                        <tbody>
                          {filteredVolunteers.map((volunteer) => (
                            <tr key={volunteer.id}>
                              <td>
                                <div className="volunteer-name">{volunteer.nomeCompleto}</div>
                                {volunteer.email && <a className="volunteer-email" href={`mailto:${volunteer.email}`}>{volunteer.email}</a>}
                              </td>
                              <td>{volunteer.telefone || <span className="muted-value">Não informado</span>}</td>
                              <td><AvailabilityList days={volunteer.disponibilidadeDias} /></td>
                              <td><StatusBadge active={volunteer.ativo} /></td>
                              <td className="row-actions">
                                <button className="text-button" onClick={() => openEditForm(volunteer)} type="button">Editar</button>
                                <button
                                  className="text-button"
                                  disabled={updatingStatusId === volunteer.id}
                                  onClick={() => void toggleStatus(volunteer)}
                                  type="button"
                                >
                                  {volunteer.ativo ? 'Inativar' : 'Ativar'}
                                </button>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                    <div className="mobile-volunteer-list">
                      {filteredVolunteers.map((volunteer) => (
                        <article className="volunteer-card" key={volunteer.id}>
                          <div className="volunteer-card-heading">
                            <div>
                              <h2>{volunteer.nomeCompleto}</h2>
                              {volunteer.email && <a className="volunteer-email" href={`mailto:${volunteer.email}`}>{volunteer.email}</a>}
                            </div>
                            <StatusBadge active={volunteer.ativo} />
                          </div>
                          <div className="mobile-contact">{volunteer.telefone || <span className="muted-value">Telefone não informado</span>}</div>
                          <AvailabilityList days={volunteer.disponibilidadeDias} />
                          <div className="mobile-card-actions">
                            <button className="text-button" onClick={() => openEditForm(volunteer)} type="button">Editar</button>
                            <button
                              className="text-button"
                              disabled={updatingStatusId === volunteer.id}
                              onClick={() => void toggleStatus(volunteer)}
                              type="button"
                            >
                              {volunteer.ativo ? 'Inativar' : 'Ativar'}
                            </button>
                          </div>
                        </article>
                      ))}
                    </div>
                  </>
                )}
              </div>
            </>
          ) : (
            <div className="placeholder-page">
              <div className="eyebrow">MÓDULO PLANEJADO</div>
              <h1 id="page-title">{currentSection.label}</h1>
              <p className="page-intro">Esta área será implementada em uma próxima etapa.</p>
              <button className="secondary-button" onClick={() => setActiveSection('voluntarios')} type="button">
                Ir para voluntários
              </button>
            </div>
          )}

          <footer className="page-footer">Sistema de gestão de voluntários · MVP</footer>
        </section>
      </main>
    </div>
  )
}

function AvailabilityList({ days }: { days: string[] }) {
  if (days.length === 0) return <span className="muted-value">Não informada</span>
  return (
    <div className="availability-list">
      {days.map((day) => <span className="availability-tag" key={day}>{displayAvailability(day)}</span>)}
    </div>
  )
}

function StatusBadge({ active }: { active: boolean }) {
  return <span className={`status-badge ${active ? 'status-active' : 'status-inactive'}`}>{active ? 'Ativo' : 'Inativo'}</span>
}

export default App
