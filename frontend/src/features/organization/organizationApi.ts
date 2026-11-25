import { jsonRequest, request } from '../../services/apiClient'

const CHURCHES = '/api/v1/churches'
const BRANCHES = '/api/v1/branches'
export type Church = { id: number; name: string; cnpj: string | null; city: string | null; state: string | null; active: boolean }
export type Branch = { id: number; name: string; address: string | null; city: string | null; churchId: number; active: boolean }
export type ChurchPayload = Omit<Church, 'id' | 'active'>
export type BranchPayload = Omit<Branch, 'id' | 'churchId' | 'active'>

export const organizationApi = {
  listChurches: () => request<Church[]>(CHURCHES),
  createChurch: (payload: ChurchPayload) => request<Church>(CHURCHES, jsonRequest('POST', payload)),
  updateChurch: (id: number, payload: ChurchPayload) => request<Church>(CHURCHES + '/' + id, jsonRequest('PUT', payload)),
  updateChurchStatus: (id: number, active: boolean) => request<Church>(CHURCHES + '/' + id + '/status', jsonRequest('PATCH', { active })),
  createAdmin: (churchId: number, payload: { nome: string; email: string; senhaInicial: string }) =>
    request(CHURCHES + '/' + churchId + '/admins', jsonRequest('POST', payload)),
  listBranches: () => request<Branch[]>(BRANCHES),
  createBranch: (payload: BranchPayload) => request<Branch>(BRANCHES, jsonRequest('POST', payload)),
  updateBranch: (id: number, payload: BranchPayload) => request<Branch>(BRANCHES + '/' + id, jsonRequest('PUT', payload)),
  updateBranchStatus: (id: number, active: boolean) => request<Branch>(BRANCHES + '/' + id + '/status', jsonRequest('PATCH', { active })),
}
