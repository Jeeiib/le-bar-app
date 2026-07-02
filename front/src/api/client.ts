// Couche d'accès à l'API : tous les appels passent par ici, en chemins /api relatifs
// (le proxy nginx les route vers le back). Le jeton JWT du barmaker est ajouté au besoin.
import { useAuthStore } from '@/stores/auth'
import type {
  Category,
  Cocktail,
  CocktailRequest,
  CreateOrderRequest,
  Order,
  AuthResponse,
  ExternalCocktail,
  TableInfo,
} from '@/types'

// Base fetch wrapper avec gestion d'erreurs
async function fetchAPI<T>(
  path: string,
  options?: {
    method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
    body?: object
    useAuth?: boolean
  }
): Promise<T> {
  const url = `/api${path}`
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  }

  // Ajouter le token JWT si authentification requise
  if (options?.useAuth ?? false) {
    const authStore = useAuthStore()
    if (authStore.token) {
      headers.Authorization = `Bearer ${authStore.token}`
    }
  }

  const fetchOptions: RequestInit = {
    method: options?.method ?? 'GET',
    headers,
  }

  if (options?.body) {
    fetchOptions.body = JSON.stringify(options.body)
  }

  const response = await fetch(url, fetchOptions)

  if (!response.ok) {
    let errorMessage = `HTTP ${response.status}`
    try {
      const errorData = await response.json()
      if (errorData.message) {
        errorMessage = errorData.message
      }
    } catch {
      // Ignorer les erreurs de parsing JSON
    }
    throw new Error(errorMessage)
  }

  // Réponses sans corps (ex: DELETE 204) : ne pas tenter de parser du JSON
  if (response.status === 204) {
    return undefined as T
  }
  const text = await response.text()
  return (text ? JSON.parse(text) : undefined) as T
}

// === PUBLIC ENDPOINTS ===

export async function getTable(qrSlug: string): Promise<TableInfo> {
  return fetchAPI(`/tables/${qrSlug}`)
}

export async function getTables(): Promise<TableInfo[]> {
  return fetchAPI('/tables')
}

export async function getCategories(): Promise<Category[]> {
  return fetchAPI('/categories')
}

export async function getCocktails(categoryId?: number): Promise<Cocktail[]> {
  const query = categoryId ? `?categoryId=${categoryId}` : ''
  return fetchAPI(`/cocktails${query}`)
}

export async function getCocktail(id: number): Promise<Cocktail> {
  return fetchAPI(`/cocktails/${id}`)
}

export async function createOrder(body: CreateOrderRequest): Promise<Order> {
  return fetchAPI('/orders', {
    method: 'POST',
    body,
  })
}

export async function getOrder(id: number): Promise<Order> {
  return fetchAPI(`/orders/${id}`)
}

// === AUTH ENDPOINTS ===

export async function login(email: string, password: string): Promise<AuthResponse> {
  return fetchAPI('/auth/login', {
    method: 'POST',
    body: { email, password },
  })
}

export async function register(
  email: string,
  password: string,
  name: string
): Promise<AuthResponse> {
  return fetchAPI('/auth/register', {
    method: 'POST',
    body: { email, password, name },
  })
}

// === BARMAKER ENDPOINTS (require auth) ===

export async function getQueue(): Promise<Order[]> {
  return fetchAPI('/orders/queue', { useAuth: true })
}

export async function advanceItem(
  orderId: number,
  itemId: number
): Promise<Order> {
  return fetchAPI(`/orders/${orderId}/items/${itemId}/advance`, {
    method: 'PATCH',
    useAuth: true,
  })
}

export async function createCategory(name: string, position: number): Promise<Category> {
  return fetchAPI('/categories', {
    method: 'POST',
    body: { name, position },
    useAuth: true,
  })
}

export async function updateCategory(
  id: number,
  name: string,
  position: number
): Promise<Category> {
  return fetchAPI(`/categories/${id}`, {
    method: 'PUT',
    body: { name, position },
    useAuth: true,
  })
}

export async function deleteCategory(id: number): Promise<void> {
  return fetchAPI(`/categories/${id}`, {
    method: 'DELETE',
    useAuth: true,
  })
}

export async function createCocktail(body: CocktailRequest): Promise<Cocktail> {
  return fetchAPI('/cocktails', {
    method: 'POST',
    body,
    useAuth: true,
  })
}

export async function updateCocktail(id: number, body: CocktailRequest): Promise<Cocktail> {
  return fetchAPI(`/cocktails/${id}`, {
    method: 'PUT',
    body,
    useAuth: true,
  })
}

export async function deleteCocktail(id: number): Promise<void> {
  return fetchAPI(`/cocktails/${id}`, {
    method: 'DELETE',
    useAuth: true,
  })
}

export async function searchExternal(name: string): Promise<ExternalCocktail[]> {
  return fetchAPI(`/external/cocktails?name=${encodeURIComponent(name)}`, {
    useAuth: true,
  })
}

// Cree une table (barmaker) : le back deduit le slug du QR a partir du nom.
export async function createTable(label: string): Promise<TableInfo> {
  return fetchAPI('/tables', {
    method: 'POST',
    body: { label },
    useAuth: true,
  })
}

// Supprime une table (barmaker) ; refusee cote back si des commandes y sont rattachees.
export async function deleteTable(id: number): Promise<void> {
  return fetchAPI(`/tables/${id}`, {
    method: 'DELETE',
    useAuth: true,
  })
}
