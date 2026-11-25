import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { organizationApi, type Church, type ChurchPayload } from './organizationApi'

export function PlatformAdminPage() {
  const [churches, setChurches] = useState<Church[]>([])
  const [selectedChurch, setSelectedChurch] = useState<number | null>(null)
  const [editingChurch, setEditingChurch] = useState<Church | null>(null)
  const [churchForm, setChurchForm] = useState<ChurchPayload>({ name: '', cnpj: '', city: '', state: '' })
  const [adminForm, setAdminForm] = useState({ nome: '', email: '', senhaInicial: '' })
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const load = useCallback(async () => {
    setLoading(true)
    try { const list = await organizationApi.listChurches(); setChurches(list); setSelectedChurch(current => list.some(item => item.id === current) ? current : list[0]?.id ?? null); setError(null) }
    catch (e) { setError(e instanceof Error ? e.message : 'Não foi possível carregar as igrejas.') }
    finally { setLoading(false) }
  }, [])
  useEffect(() => { void load() }, [load])
  async function createChurch(event: FormEvent) {
    event.preventDefault(); setError(null); setNotice(null)
    try {
      const payload = { ...churchForm, name: churchForm.name.trim(), cnpj: churchForm.cnpj?.trim() || null, city: churchForm.city?.trim() || null, state: churchForm.state?.trim().toUpperCase() || null }
      if (editingChurch) {
        await organizationApi.updateChurch(editingChurch.id, payload)
        setNotice('Dados da igreja atualizados.')
        setEditingChurch(null)
      } else {
        const created = await organizationApi.createChurch(payload)
        setNotice('Igreja criada. Agora você pode cadastrar um administrador.')
        setSelectedChurch(created.id)
      }
      setChurchForm({ name: '', cnpj: '', city: '', state: '' })
      await load()
    }
    catch (e) { setError(e instanceof Error ? e.message : 'Não foi possível criar a igreja.') }
  }
  async function createAdmin(event: FormEvent) {
    event.preventDefault(); if (!selectedChurch) return
    setError(null); setNotice(null)
    try { await organizationApi.createAdmin(selectedChurch, adminForm); setAdminForm({ nome: '', email: '', senhaInicial: '' }); setNotice('Administrador da igreja criado com sucesso.') }
    catch (e) { setError(e instanceof Error ? e.message : 'Não foi possível criar o administrador.') }
  }
  async function editChurch(church: Church) {
    setEditingChurch(church)
    setChurchForm({ name: church.name, cnpj: church.cnpj, city: church.city, state: church.state })
  }
  async function toggleChurch(church: Church) {
    try { await organizationApi.updateChurchStatus(church.id, !church.active); await load() }
    catch (e) { setError(e instanceof Error ? e.message : 'Não foi possível alterar o status da igreja.') }
  }
  return <>
    <div className="page-heading"><div><div className="eyebrow">ADMINISTRAÇÃO DA PLATAFORMA</div><h1 id="page-title">Igrejas</h1><p className="page-intro">Cadastre igrejas e crie os primeiros administradores de cada organização.</p></div></div>
    {error && <div className="alert alert-error" role="alert">{error}</div>}{notice && <div className="alert alert-success" role="status">{notice}</div>}
    <div className="platform-grid">
      <form className="volunteer-form" onSubmit={createChurch}><div className="form-heading"><div><div className="eyebrow">CADASTRO</div><h2>{editingChurch ? 'Editar igreja' : 'Nova igreja'}</h2></div>{editingChurch && <button className="text-button" onClick={() => { setEditingChurch(null); setChurchForm({ name: '', cnpj: '', city: '', state: '' }) }} type="button">Cancelar edição</button>}</div><div className="form-grid">
        <label className="field field-full"><span>Nome *</span><input required maxLength={150} value={churchForm.name} onChange={e => setChurchForm({ ...churchForm, name: e.target.value })} /></label>
        <label className="field"><span>CNPJ (opcional)</span><input maxLength={18} value={churchForm.cnpj ?? ''} onChange={e => setChurchForm({ ...churchForm, cnpj: e.target.value })} /></label>
        <label className="field"><span>Cidade</span><input maxLength={100} value={churchForm.city ?? ''} onChange={e => setChurchForm({ ...churchForm, city: e.target.value })} /></label>
        <label className="field"><span>Estado</span><input maxLength={2} value={churchForm.state ?? ''} onChange={e => setChurchForm({ ...churchForm, state: e.target.value.toUpperCase() })} /></label>
      </div><div className="form-actions"><button className="primary-button" type="submit">{editingChurch ? 'Salvar alterações' : 'Criar igreja'}</button></div></form>
      <form className="volunteer-form" onSubmit={createAdmin}><div className="form-heading"><div><div className="eyebrow">ACESSO</div><h2>Novo administrador</h2></div></div><div className="form-grid">
        <label className="field field-full"><span>Igreja *</span><select required value={selectedChurch ?? ''} onChange={e => setSelectedChurch(Number(e.target.value))}><option value="">Selecione a igreja</option>{churches.map(church => <option key={church.id} value={church.id}>{church.name}</option>)}</select></label>
        <label className="field field-full"><span>Nome *</span><input required maxLength={150} value={adminForm.nome} onChange={e => setAdminForm({ ...adminForm, nome: e.target.value })} /></label>
        <label className="field field-full"><span>E-mail *</span><input required type="email" value={adminForm.email} onChange={e => setAdminForm({ ...adminForm, email: e.target.value })} /></label>
        <label className="field field-full"><span>Senha inicial * (mínimo 8 caracteres)</span><input required minLength={8} maxLength={72} type="password" value={adminForm.senhaInicial} onChange={e => setAdminForm({ ...adminForm, senhaInicial: e.target.value })} /></label>
      </div><div className="form-actions"><button className="primary-button" disabled={!selectedChurch} type="submit">Criar administrador</button></div></form>
    </div>
    <div className="list-panel organization-list" aria-busy={loading}>{loading ? <div className="list-message">Carregando igrejas…</div> : churches.map(church => <article className="organization-row" key={church.id}><div><strong>{church.name}</strong><span>{[church.city, church.state].filter(Boolean).join(' / ') || 'Localização não informada'}</span></div><span className={'status-badge ' + (church.active ? 'status-active' : 'status-inactive')}>{church.active ? 'Ativa' : 'Inativa'}</span><div className="organization-actions"><button className="text-button" onClick={() => void editChurch(church)} type="button">Editar</button><button className="text-button" onClick={() => void toggleChurch(church)} type="button">{church.active ? 'Inativar' : 'Ativar'}</button></div></article>)}</div>
  </>
}
