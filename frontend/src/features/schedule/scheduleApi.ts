import { jsonRequest, request } from '../../services/apiClient'

const EVENTS_PATH = '/api/v1/events'
const SCHEDULES_PATH = '/api/v1/schedules'

export type EventStatus = 'AGENDADO' | 'CONCLUIDO' | 'CANCELADO'
export type ConfirmationStatus = 'PENDENTE' | 'CONFIRMADO' | 'RECUSADO'

export type ServiceEvent = {
  id: number
  titulo: string
  dataEvento: string
  horario: string
  turnoOuDiaSemana: string
  observacoes: string | null
  status: EventStatus
}

export type ServiceEventPayload = Omit<ServiceEvent, 'id' | 'status'> & { branchId?: number | null }

export type ScheduleAssignment = {
  id: number
  eventId: number
  eventTitulo: string
  dataEvento: string
  horario: string
  turnoOuDiaSemana: string
  ministryId: number
  ministryNome: string
  ministryRoleId: number
  ministryRoleNome: string
  volunteerId: number
  volunteerNomeCompleto: string
  statusConfirmacao: ConfirmationStatus
  alertaDisponibilidade: boolean
  mensagemDisponibilidade: string | null
}

export const eventApi = {
  list: () => request<ServiceEvent[]>(EVENTS_PATH),
  upcoming: () => request<ServiceEvent[]>(`${EVENTS_PATH}/upcoming`),
  create: (payload: ServiceEventPayload) => request<ServiceEvent>(EVENTS_PATH, jsonRequest('POST', payload)),
  update: (id: number, payload: ServiceEventPayload) => request<ServiceEvent>(`${EVENTS_PATH}/${id}`, jsonRequest('PUT', payload)),
  updateStatus: (id: number, status: EventStatus) => request<ServiceEvent>(`${EVENTS_PATH}/${id}/status`, jsonRequest('PATCH', { status })),
  delete: (id: number) => request<void>(`${EVENTS_PATH}/${id}`, { method: 'DELETE' }),
}

export const scheduleApi = {
  list: (eventId?: number) => request<ScheduleAssignment[]>(eventId ? `${SCHEDULES_PATH}?eventId=${eventId}` : SCHEDULES_PATH),
  listMine: () => request<ScheduleAssignment[]>(SCHEDULES_PATH + '/mine'),
  create: (eventId: number, ministryId: number, ministryRoleId: number, volunteerId: number) =>
    request<ScheduleAssignment>(`${SCHEDULES_PATH}/events/${eventId}/assignments`,
      jsonRequest('POST', { ministryId, ministryRoleId, volunteerId })),
  updateConfirmation: (id: number, statusConfirmacao: ConfirmationStatus) =>
    request<ScheduleAssignment>(`${SCHEDULES_PATH}/assignments/${id}/confirmation`, jsonRequest('PATCH', { statusConfirmacao })),
  delete: (id: number) => request<void>(`${SCHEDULES_PATH}/assignments/${id}`, { method: 'DELETE' }),
}
