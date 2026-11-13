import { jsonRequest, request } from '../../services/apiClient'

const VOLUNTEERS_PATH = '/api/v1/volunteers'

export type Volunteer = {
  id: number
  nomeCompleto: string
  telefone: string | null
  email: string | null
  ativo: boolean
  disponibilidadeDias: string[]
  observacoes: string | null
}

export type VolunteerPayload = {
  nomeCompleto: string
  telefone: string | null
  email: string | null
  disponibilidadeDias: string[]
  observacoes: string | null
}

export const volunteerApi = {
  list: () => request<Volunteer[]>(VOLUNTEERS_PATH),

  create: (payload: VolunteerPayload) =>
    request<Volunteer>(VOLUNTEERS_PATH, jsonRequest('POST', payload)),

  update: (id: number, payload: VolunteerPayload) =>
    request<Volunteer>(`${VOLUNTEERS_PATH}/${id}`, jsonRequest('PUT', payload)),

  updateStatus: (id: number, ativo: boolean) =>
    request<Volunteer>(`${VOLUNTEERS_PATH}/${id}/status`, jsonRequest('PATCH', { ativo })),
}
