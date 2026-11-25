import { jsonRequest, request } from '../../services/apiClient'

const MINISTRIES_PATH = '/api/v1/ministries'

export type Ministry = {
  id: number
  nome: string
  descricao: string | null
  ativo: boolean
}

export type MinistryRole = {
  id: number
  ministryId: number
  nome: string
}

export type VolunteerMinistry = {
  id: number
  volunteerId: number
  volunteerNomeCompleto: string
  ministryId: number
  ministryRoleId: number
  ministryRoleNome: string
}

export type MinistryPayload = {
  nome: string
  descricao: string | null
  branchId?: number | null
}

export const ministryApi = {
  list: () => request<Ministry[]>(MINISTRIES_PATH),

  create: (payload: MinistryPayload) =>
    request<Ministry>(MINISTRIES_PATH, jsonRequest('POST', payload)),

  update: (id: number, payload: MinistryPayload) =>
    request<Ministry>(`${MINISTRIES_PATH}/${id}`, jsonRequest('PUT', payload)),

  updateStatus: (id: number, ativo: boolean) =>
    request<Ministry>(`${MINISTRIES_PATH}/${id}/status`, jsonRequest('PATCH', { ativo })),

  listRoles: (ministryId: number) =>
    request<MinistryRole[]>(`${MINISTRIES_PATH}/${ministryId}/roles`),

  createRole: (ministryId: number, nome: string) =>
    request<MinistryRole>(`${MINISTRIES_PATH}/${ministryId}/roles`, jsonRequest('POST', { nome })),

  updateRole: (ministryId: number, roleId: number, nome: string) =>
    request<MinistryRole>(`${MINISTRIES_PATH}/${ministryId}/roles/${roleId}`, jsonRequest('PUT', { nome })),

  deleteRole: (ministryId: number, roleId: number) =>
    request<void>(`${MINISTRIES_PATH}/${ministryId}/roles/${roleId}`, { method: 'DELETE' }),

  listMemberships: (ministryId: number) =>
    request<VolunteerMinistry[]>(`${MINISTRIES_PATH}/${ministryId}/memberships`),

  linkVolunteer: (ministryId: number, volunteerId: number, ministryRoleId: number) =>
    request<VolunteerMinistry>(
      `${MINISTRIES_PATH}/${ministryId}/memberships`,
      jsonRequest('POST', { volunteerId, ministryRoleId }),
    ),

  unlinkVolunteer: (ministryId: number, membershipId: number) =>
    request<void>(`${MINISTRIES_PATH}/${ministryId}/memberships/${membershipId}`, { method: 'DELETE' }),
}
