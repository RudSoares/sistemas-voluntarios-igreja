import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { ministryApi, type Ministry, type MinistryPayload, type MinistryRole, type VolunteerMinistry } from './ministryApi'
import { volunteerApi, type Volunteer } from '../volunteers/volunteerApi'

type Props = {
  onGoToVolunteers: () => void
}

type MinistryForm = {
  nome: string
  descricao: string
}

const emptyMinistryForm = (): MinistryForm => ({ nome: '', descricao: '' })

export function MinistriesPage({ onGoToVolunteers }: Props) {
  const [ministries, setMinistries] = useState<Ministry[]>([])
  const [volunteers, setVolunteers] = useState<Volunteer[]>([])
  const [selectedMinistryId, setSelectedMinistryId] = useState<number | null>(null)
  const [roles, setRoles] = useState<MinistryRole[]>([])
  const [memberships, setMemberships] = useState<VolunteerMinistry[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [isDetailLoading, setIsDetailLoading] = useState(false)
  const [pageError, setPageError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)

  const [isMinistryFormOpen, setIsMinistryFormOpen] = useState(false)
  const [editingMinistry, setEditingMinistry] = useState<Ministry | null>(null)
  const [ministryForm, setMinistryForm] = useState<MinistryForm>(emptyMinistryForm)
  const [isSavingMinistry, setIsSavingMinistry] = useState(false)
  const [isUpdatingMinistryStatus, setIsUpdatingMinistryStatus] = useState(false)

  const [roleName, setRoleName] = useState('')
  const [editingRoleId, setEditingRoleId] = useState<number | null>(null)
  const [editingRoleName, setEditingRoleName] = useState('')
  const [isSavingRole, setIsSavingRole] = useState(false)
  const [busyRoleId, setBusyRoleId] = useState<number | null>(null)

  const [volunteerId, setVolunteerId] = useState('')
  const [ministryRoleId, setMinistryRoleId] = useState('')
  const [isLinkingVolunteer, setIsLinkingVolunteer] = useState(false)
  const [busyMembershipId, setBusyMembershipId] = useState<number | null>(null)

  const selectedMinistry = ministries.find((ministry) => ministry.id === selectedMinistryId) ?? null

  const loadPage = useCallback(async (preferredMinistryId?: number) => {
    setIsLoading(true)
    setPageError(null)
    try {
      const [ministryItems, volunteerItems] = await Promise.all([
        ministryApi.list(),
        volunteerApi.list(),
      ])
      setMinistries(ministryItems)
      setVolunteers(volunteerItems)
      setSelectedMinistryId((currentId) => {
        if (preferredMinistryId && ministryItems.some((item) => item.id === preferredMinistryId)) {
          return preferredMinistryId
        }
        if (currentId && ministryItems.some((item) => item.id === currentId)) return currentId
        return ministryItems[0]?.id ?? null
      })
    } catch (error) {
      setPageError(error instanceof Error ? error.message : 'Não foi possível carregar os ministérios.')
    } finally {
      setIsLoading(false)
    }
  }, [])

  const loadMinistryDetails = useCallback(async (id: number) => {
    setIsDetailLoading(true)
    setActionError(null)
    try {
      const [roleItems, membershipItems] = await Promise.all([
        ministryApi.listRoles(id),
        ministryApi.listMemberships(id),
      ])
      setRoles(roleItems)
      setMemberships(membershipItems)
      setMinistryRoleId((currentId) =>
        currentId && roleItems.some((role) => String(role.id) === currentId)
          ? currentId
          : roleItems[0] ? String(roleItems[0].id) : '',
      )
    } catch (error) {
      setActionError(error instanceof Error ? error.message : 'Não foi possível carregar os detalhes do ministério.')
      setRoles([])
      setMemberships([])
    } finally {
      setIsDetailLoading(false)
    }
  }, [])

  useEffect(() => {
    void loadPage()
  }, [loadPage])

  useEffect(() => {
    if (selectedMinistryId === null) {
      setRoles([])
      setMemberships([])
      return
    }
    void loadMinistryDetails(selectedMinistryId)
  }, [loadMinistryDetails, selectedMinistryId])

  function openCreateMinistry() {
    setEditingMinistry(null)
    setMinistryForm(emptyMinistryForm())
    setActionError(null)
    setIsMinistryFormOpen(true)
  }

  function openEditMinistry(ministry: Ministry) {
    setEditingMinistry(ministry)
    setMinistryForm({ nome: ministry.nome, descricao: ministry.descricao ?? '' })
    setActionError(null)
    setIsMinistryFormOpen(true)
  }

  function closeMinistryForm() {
    setIsMinistryFormOpen(false)
    setEditingMinistry(null)
  }

  async function handleMinistrySubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setActionError(null)
    setIsSavingMinistry(true)
    try {
      const payload: MinistryPayload = {
        nome: ministryForm.nome.trim(),
        descricao: ministryForm.descricao.trim() || null,
      }
      const saved = editingMinistry
        ? await ministryApi.update(editingMinistry.id, payload)
        : await ministryApi.create(payload)
      closeMinistryForm()
      await loadPage(saved.id)
    } catch (error) {
      setActionError(error instanceof Error ? error.message : 'Não foi possível salvar o ministério.')
    } finally {
      setIsSavingMinistry(false)
    }
  }

  async function toggleMinistryStatus(ministry: Ministry) {
    setActionError(null)
    setIsUpdatingMinistryStatus(true)
    try {
      const updated = await ministryApi.updateStatus(ministry.id, !ministry.ativo)
      setMinistries((current) => current.map((item) => item.id === updated.id ? updated : item))
    } catch (error) {
      setActionError(error instanceof Error ? error.message : 'Não foi possível alterar o status do ministério.')
    } finally {
      setIsUpdatingMinistryStatus(false)
    }
  }

  async function handleRoleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!selectedMinistry) return
    setActionError(null)
    setIsSavingRole(true)
    try {
      await ministryApi.createRole(selectedMinistry.id, roleName.trim())
      setRoleName('')
      await loadMinistryDetails(selectedMinistry.id)
    } catch (error) {
      setActionError(error instanceof Error ? error.message : 'Não foi possível adicionar a função.')
    } finally {
      setIsSavingRole(false)
    }
  }

  async function saveRole(role: MinistryRole) {
    if (!selectedMinistry) return
    setActionError(null)
    setBusyRoleId(role.id)
    try {
      await ministryApi.updateRole(selectedMinistry.id, role.id, editingRoleName.trim())
      setEditingRoleId(null)
      setEditingRoleName('')
      await loadMinistryDetails(selectedMinistry.id)
    } catch (error) {
      setActionError(error instanceof Error ? error.message : 'Não foi possível atualizar a função.')
    } finally {
      setBusyRoleId(null)
    }
  }

  async function deleteRole(role: MinistryRole) {
    if (!selectedMinistry) return
    setActionError(null)
    setBusyRoleId(role.id)
    try {
      await ministryApi.deleteRole(selectedMinistry.id, role.id)
      setRoles((current) => current.filter((item) => item.id !== role.id))
      if (ministryRoleId === String(role.id)) setMinistryRoleId('')
    } catch (error) {
      setActionError(error instanceof Error ? error.message : 'Não foi possível remover a função.')
    } finally {
      setBusyRoleId(null)
    }
  }

  async function handleLinkVolunteer(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!selectedMinistry || !volunteerId || !ministryRoleId) return
    setActionError(null)
    setIsLinkingVolunteer(true)
    try {
      await ministryApi.linkVolunteer(selectedMinistry.id, Number(volunteerId), Number(ministryRoleId))
      setVolunteerId('')
      await loadMinistryDetails(selectedMinistry.id)
    } catch (error) {
      setActionError(error instanceof Error ? error.message : 'Não foi possível vincular o voluntário.')
    } finally {
      setIsLinkingVolunteer(false)
    }
  }

  async function unlinkVolunteer(membership: VolunteerMinistry) {
    if (!selectedMinistry) return
    setActionError(null)
    setBusyMembershipId(membership.id)
    try {
      await ministryApi.unlinkVolunteer(selectedMinistry.id, membership.id)
      setMemberships((current) => current.filter((item) => item.id !== membership.id))
    } catch (error) {
      setActionError(error instanceof Error ? error.message : 'Não foi possível desvincular o voluntário.')
    } finally {
      setBusyMembershipId(null)
    }
  }

  return (
    <>
      <div className="page-heading">
        <div>
          <div className="eyebrow">EQUIPES E FUNÇÕES</div>
          <h1 id="page-title">Ministérios</h1>
          <p className="page-intro">Organize as equipes, funções e voluntários de cada ministério.</p>
        </div>
        <button className="primary-button" onClick={openCreateMinistry} type="button">
          <span aria-hidden="true">＋</span> Novo ministério
        </button>
      </div>

      {actionError && (
        <div className="alert alert-error" role="alert">
          <span>{actionError}</span>
          <button aria-label="Fechar mensagem" onClick={() => setActionError(null)} type="button">×</button>
        </div>
      )}
      {pageError && (
        <div className="alert alert-error" role="alert">
          <span>{pageError}</span>
          <button className="retry-button" onClick={() => void loadPage()} type="button">Tentar novamente</button>
        </div>
      )}

      {isMinistryFormOpen && (
        <form className="volunteer-form" onSubmit={handleMinistrySubmit}>
          <div className="form-heading">
            <div>
              <div className="eyebrow">{editingMinistry ? 'ATUALIZAÇÃO' : 'NOVO CADASTRO'}</div>
              <h2>{editingMinistry ? 'Editar ministério' : 'Cadastrar ministério'}</h2>
            </div>
            <button aria-label="Fechar formulário" className="icon-button" onClick={closeMinistryForm} type="button">×</button>
          </div>
          <div className="form-grid">
            <label className="field">
              <span>Nome <b aria-hidden="true">*</b></span>
              <input
                autoFocus
                maxLength={120}
                onChange={(event) => setMinistryForm((current) => ({ ...current, nome: event.target.value }))}
                placeholder="Ex.: Louvor"
                required
                value={ministryForm.nome}
              />
            </label>
            <label className="field">
              <span>Descrição</span>
              <input
                maxLength={2000}
                onChange={(event) => setMinistryForm((current) => ({ ...current, descricao: event.target.value }))}
                placeholder="Uma breve descrição (opcional)"
                value={ministryForm.descricao}
              />
            </label>
          </div>
          <div className="form-actions">
            <button className="secondary-button" onClick={closeMinistryForm} type="button">Cancelar</button>
            <button className="primary-button" disabled={isSavingMinistry} type="submit">
              {isSavingMinistry ? 'Salvando…' : editingMinistry ? 'Salvar alterações' : 'Cadastrar ministério'}
            </button>
          </div>
        </form>
      )}

      <div className="ministries-layout">
        <section aria-label="Lista de ministérios" className="ministries-list-panel">
          <div className="ministries-panel-heading">
            <div>
              <h2>Seus ministérios</h2>
              <span>{ministries.length} cadastrados</span>
            </div>
          </div>
          {isLoading ? (
            <div className="list-message">Carregando ministérios…</div>
          ) : ministries.length === 0 ? (
            <div className="ministries-list-empty">
              <p>Nenhum ministério cadastrado ainda.</p>
              <button className="text-button" onClick={openCreateMinistry} type="button">Cadastrar o primeiro</button>
            </div>
          ) : (
            <div className="ministries-list">
              {ministries.map((ministry) => (
                <button
                  aria-current={selectedMinistryId === ministry.id ? 'true' : undefined}
                  className={`ministry-list-item ${selectedMinistryId === ministry.id ? 'is-selected' : ''}`}
                  key={ministry.id}
                  onClick={() => setSelectedMinistryId(ministry.id)}
                  type="button"
                >
                  <span className="ministry-list-mark" aria-hidden="true">{ministry.nome.slice(0, 1).toLocaleUpperCase('pt-BR')}</span>
                  <span className="ministry-list-copy">
                    <strong>{ministry.nome}</strong>
                    <small>{ministry.ativo ? 'Ativo' : 'Inativo'}</small>
                  </span>
                  <span aria-hidden="true" className="ministry-list-arrow">›</span>
                </button>
              ))}
            </div>
          )}
        </section>

        <section aria-label="Detalhes do ministério" className="ministries-detail-panel">
          {selectedMinistry ? (
            <>
              <div className="ministry-detail-heading">
                <div>
                  <div className="eyebrow">MINISTÉRIO</div>
                  <h2>{selectedMinistry.nome}</h2>
                  <p>{selectedMinistry.descricao || 'Sem descrição cadastrada.'}</p>
                </div>
                <div className="ministry-detail-actions">
                  <span className={`status-badge ${selectedMinistry.ativo ? 'status-active' : 'status-inactive'}`}>
                    {selectedMinistry.ativo ? 'Ativo' : 'Inativo'}
                  </span>
                  <button className="text-button" onClick={() => openEditMinistry(selectedMinistry)} type="button">Editar</button>
                  <button
                    className="text-button"
                    disabled={isUpdatingMinistryStatus}
                    onClick={() => void toggleMinistryStatus(selectedMinistry)}
                    type="button"
                  >
                    {selectedMinistry.ativo ? 'Inativar' : 'Ativar'}
                  </button>
                </div>
              </div>

              {isDetailLoading ? (
                <div className="list-message">Carregando funções e vínculos…</div>
              ) : (
                <div className="ministry-sections-grid">
                  <section className="ministry-subsection">
                    <div className="ministry-subsection-heading">
                      <div>
                        <h3>Funções</h3>
                        <span>{roles.length} cadastradas</span>
                      </div>
                    </div>
                    <form className="compact-add-form" onSubmit={handleRoleSubmit}>
                      <input
                        aria-label="Nome da nova função"
                        maxLength={120}
                        onChange={(event) => setRoleName(event.target.value)}
                        placeholder="Ex.: Violão, Recepção"
                        required
                        value={roleName}
                      />
                      <button className="secondary-button" disabled={isSavingRole} type="submit">Adicionar</button>
                    </form>
                    {roles.length === 0 ? (
                      <div className="subsection-empty">Adicione as funções deste ministério.</div>
                    ) : (
                      <ul className="ministry-role-list">
                        {roles.map((role) => (
                          <li className="ministry-role-item" key={role.id}>
                            {editingRoleId === role.id ? (
                              <form
                                className="role-edit-form"
                                onSubmit={(event) => { event.preventDefault(); void saveRole(role) }}
                              >
                                <input
                                  aria-label={`Nome da função ${role.nome}`}
                                  autoFocus
                                  maxLength={120}
                                  onChange={(event) => setEditingRoleName(event.target.value)}
                                  required
                                  value={editingRoleName}
                                />
                                <button className="text-button" disabled={busyRoleId === role.id} type="submit">Salvar</button>
                                <button className="text-button" onClick={() => setEditingRoleId(null)} type="button">Cancelar</button>
                              </form>
                            ) : (
                              <>
                                <span className="role-mark" aria-hidden="true">✳</span>
                                <strong>{role.nome}</strong>
                                <span className="role-actions">
                                  <button
                                    aria-label={`Editar função ${role.nome}`}
                                    className="text-button"
                                    onClick={() => { setEditingRoleId(role.id); setEditingRoleName(role.nome) }}
                                    type="button"
                                  >Editar</button>
                                  <button
                                    aria-label={`Remover função ${role.nome}`}
                                    className="text-button"
                                    disabled={busyRoleId === role.id}
                                    onClick={() => void deleteRole(role)}
                                    type="button"
                                  >Remover</button>
                                </span>
                              </>
                            )}
                          </li>
                        ))}
                      </ul>
                    )}
                    <p className="section-hint">Uma função que possui voluntários vinculados só pode ser removida depois desses vínculos.</p>
                  </section>

                  <section className="ministry-subsection">
                    <div className="ministry-subsection-heading">
                      <div>
                        <h3>Voluntários vinculados</h3>
                        <span>{memberships.length} vínculos</span>
                      </div>
                    </div>
                    {roles.length === 0 ? (
                      <div className="subsection-empty">Cadastre uma função para poder vincular voluntários.</div>
                    ) : volunteers.length === 0 ? (
                      <div className="subsection-empty">
                        <p>Cadastre voluntários antes de vinculá-los.</p>
                        <button className="text-button" onClick={onGoToVolunteers} type="button">Ir para Voluntários</button>
                      </div>
                    ) : (
                      <form className="membership-add-form" onSubmit={handleLinkVolunteer}>
                        <label className="field">
                          <span>Voluntário</span>
                          <select onChange={(event) => setVolunteerId(event.target.value)} required value={volunteerId}>
                            <option value="">Selecione um voluntário</option>
                            {volunteers.map((volunteer) => (
                              <option key={volunteer.id} value={volunteer.id}>
                                {volunteer.nomeCompleto}{volunteer.ativo ? '' : ' (inativo)'}
                              </option>
                            ))}
                          </select>
                        </label>
                        <label className="field">
                          <span>Função</span>
                          <select onChange={(event) => setMinistryRoleId(event.target.value)} required value={ministryRoleId}>
                            <option value="">Selecione uma função</option>
                            {roles.map((role) => <option key={role.id} value={role.id}>{role.nome}</option>)}
                          </select>
                        </label>
                        <button className="secondary-button" disabled={isLinkingVolunteer} type="submit">
                          {isLinkingVolunteer ? 'Vinculando…' : 'Vincular voluntário'}
                        </button>
                      </form>
                    )}

                    {memberships.length === 0 ? (
                      <div className="subsection-empty membership-empty">Nenhum voluntário vinculado a este ministério.</div>
                    ) : (
                      <ul className="ministry-membership-list">
                        {memberships.map((membership) => (
                          <li className="ministry-membership-item" key={membership.id}>
                            <span className="volunteer-avatar" aria-hidden="true">
                              {membership.volunteerNomeCompleto.slice(0, 1).toLocaleUpperCase('pt-BR')}
                            </span>
                            <span className="membership-copy">
                              <strong>{membership.volunteerNomeCompleto}</strong>
                              <small>{membership.ministryRoleNome}</small>
                            </span>
                            <button
                              aria-label={`Desvincular ${membership.volunteerNomeCompleto} da função ${membership.ministryRoleNome}`}
                              className="text-button"
                              disabled={busyMembershipId === membership.id}
                              onClick={() => void unlinkVolunteer(membership)}
                              type="button"
                            >Desvincular</button>
                          </li>
                        ))}
                      </ul>
                    )}
                  </section>
                </div>
              )}
            </>
          ) : isLoading ? (
            <div className="list-message">Carregando…</div>
          ) : (
            <div className="ministries-detail-empty">
              <div className="empty-icon" aria-hidden="true">✳</div>
              <h2>Selecione um ministério</h2>
              <p>Ou cadastre um novo ministério para começar a adicionar funções e voluntários.</p>
              <button className="primary-button" onClick={openCreateMinistry} type="button">Cadastrar ministério</button>
            </div>
          )}
        </section>
      </div>
    </>
  )
}
