export const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/+$/, '')

export async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response
  try {
    response = await fetch(`${API_BASE_URL}${path}`, init)
  } catch {
    throw new Error(`Não foi possível conectar à API em ${API_BASE_URL}. Confira se o backend está em execução.`)
  }

  if (!response.ok) {
    let message = `A API respondeu com status ${response.status}.`
    try {
      const body = (await response.json()) as { detail?: string; message?: string; error?: string }
      message = body.detail || body.message || body.error || message
    } catch {
      // A resposta de erro pode não ter corpo JSON.
    }
    throw new Error(message)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return (await response.json()) as T
}

export function jsonRequest(method: string, body: unknown): RequestInit {
  return {
    method,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  }
}
