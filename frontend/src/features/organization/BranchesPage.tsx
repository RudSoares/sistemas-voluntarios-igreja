import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { organizationApi, type Branch, type BranchPayload } from './organizationApi'

export function BranchesPage() {
  const [branches, setBranches] = useState<Branch[]>([])
  const [form, setForm] = useState<BranchPayload>({ name: '', address: '', city: '' })
  const [editing, setEditing] = useState<Branch | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const load = useCallback(async () => {
    setLoading(true)
    try { setBranches(await organizationApi.listBranches()); setError(null) }
    catch (e) { setError(e instanceof Error ? e.message : 'Não foi possível carregar as filiais.') }
    finally { setLoading(false) }
  }, [])
  useEffect(() => { void load() }, [load])
  async function submit(event: FormEvent) {
    event.preventDefault(); setSaving(true); setError(null)
    try {
      const payload = { ...form, name: form.name.trim(), address: form.address?.trim() || null, city: form.city?.trim() || null }
      if (editing) { await organizationApi.updateBranch(editing.id, payload); setEditing(null) }
      else await organizationApi.createBranch(payload)
      setForm({ name: '', address: '', city: '' }); await load()
    }
    catch (e) { setError(e instanceof Error ? e.message : 'Não foi possível criar a filial.') }
    finally { setSaving(false) }
  }
  async function toggle(branch: Branch) {
    try { await organizationApi.updateBranchStatus(branch.id, !branch.active); await load() }
    catch (e) { setError(e instanceof Error ? e.message : 'Não foi possível alterar a filial.') }
  }
  function edit(branch: Branch) {
    setEditing(branch)
    setForm({ name: branch.name, address: branch.address, city: branch.city })
  }
  return <>
    <div className="page-heading"><div><div className="eyebrow">ORGANIZAÇÃO</div><h1 id="page-title">Filiais</h1><p className="page-intro">Gerencie as congregações vinculadas à sua igreja.</p></div></div>
    {error && <div className="alert alert-error" role="alert">{error}</div>}
    <form className="volunteer-form" onSubmit={submit}>
      <div className="form-heading"><div><div className="eyebrow">{editing ? 'ATUALIZAÇÃO' : 'NOVA FILIAL'}</div><h2>{editing ? 'Editar congregação' : 'Adicionar congregação'}</h2></div>{editing && <button className="text-button" onClick={() => { setEditing(null); setForm({ name: '', address: '', city: '' }) }} type="button">Cancelar edição</button>}</div>
      <div className="form-grid">
        <label className="field"><span>Nome *</span><input required maxLength={150} value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} placeholder="Ex.: Sede" /></label>
        <label className="field"><span>Cidade</span><input maxLength={100} value={form.city ?? ''} onChange={e => setForm({ ...form, city: e.target.value })} /></label>
        <label className="field field-full"><span>Endereço</span><input maxLength={255} value={form.address ?? ''} onChange={e => setForm({ ...form, address: e.target.value })} /></label>
      </div>
      <div className="form-actions"><button className="primary-button" disabled={saving} type="submit">{saving ? 'Salvando…' : editing ? 'Salvar alterações' : 'Adicionar filial'}</button></div>
    </form>
    <div className="list-panel organization-list" aria-busy={loading}>{loading ? <div className="list-message">Carregando filiais…</div> : branches.length === 0 ? <div className="empty-state"><h2>Nenhuma filial cadastrada</h2></div> : branches.map(branch => <article className="organization-row" key={branch.id}><div><strong>{branch.name}</strong><span>{[branch.address, branch.city].filter(Boolean).join(' · ') || 'Endereço não informado'}</span></div><span className={'status-badge ' + (branch.active ? 'status-active' : 'status-inactive')}>{branch.active ? 'Ativa' : 'Inativa'}</span><div className="organization-actions"><button className="text-button" onClick={() => edit(branch)} type="button">Editar</button><button className="text-button" onClick={() => void toggle(branch)} type="button">{branch.active ? 'Inativar' : 'Ativar'}</button></div></article>)}</div>
  </>
}
