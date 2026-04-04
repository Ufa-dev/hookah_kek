import axios, { type AxiosError } from 'axios'
import type {
  AuthResponse, LoginRequest, RegisterRequest, TokenToRefresh,
  User, UserUpdateRequest,
  Tag, TagCreateRequest, TagUpdateRequest,
  TabacoBrand, BrandCreateRequest, BrandUpdateRequest, UpdateTagForBrandRequest,
  TabacoFlavor, FlavorCreateRequest, FlavorUpdateRequest, UpdateTagForFlavorRequest,
  MarketArcView, MarketCreateRequest, MarketUpdateRequest, MarketListParams, MarketUpdateCountRequest, MarketTotalWeightView, MarketAuditRecord,
  FlavorPack, PackCreateRequest, PackUpdateRequest,
  Slice, CursorParams, PackListParams,
  AuditEventType, BrandAuditRecord, FlavorAuditRecord, PackAuditRecord,
} from '@/types'

// ─── Token storage ─────────────────────────────────────────────────────────────

const KEYS = { access: 'kek_access', refresh: 'kek_refresh' } as const

export const tokenStorage = {
  getAccess:  () => localStorage.getItem(KEYS.access),
  getRefresh: () => localStorage.getItem(KEYS.refresh),
  set: (auth: AuthResponse) => {
    localStorage.setItem(KEYS.access,  auth.accessToken)
    localStorage.setItem(KEYS.refresh, auth.refreshToken)
  },
  clear: () => {
    localStorage.removeItem(KEYS.access)
    localStorage.removeItem(KEYS.refresh)
  },
}

// ─── Axios instance ────────────────────────────────────────────────────────────

const http = axios.create({
  baseURL: '/api/v1',
  headers: { 'Content-Type': 'application/json' },
})

http.interceptors.request.use((config) => {
  const token = tokenStorage.getAccess()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Logout callback — set by AuthContext on mount so interceptor can trigger React logout
let onAuthExpired: (() => void) | null = null
export function setAuthExpiredHandler(handler: () => void) {
  onAuthExpired = handler
}

function forceLogout() {
  tokenStorage.clear()
  if (onAuthExpired) {
    onAuthExpired()
  } else {
    // Fallback before AuthContext mounts (e.g. during SSR or early load)
    window.location.href = '/login'
  }
}

// Auto-refresh on 401 — single in-flight promise to avoid race conditions
let refreshing: Promise<AuthResponse> | null = null

http.interceptors.response.use(
  (res) => res,
  async (err: AxiosError) => {
    const original = err.config
    if (err.response?.status !== 401 || !original) return Promise.reject(err)

    // Never retry the refresh endpoint itself — would cause infinite loop
    if (original.url?.includes('/auth/refresh')) {
      forceLogout()
      return Promise.reject(err)
    }

    // Prevent retrying the same request more than once
    if ((original as any)._retry) {
      forceLogout()
      return Promise.reject(err)
    }
    ;(original as any)._retry = true

    const refreshToken = tokenStorage.getRefresh()
    if (!refreshToken) {
      forceLogout()
      return Promise.reject(err)
    }

    try {
      if (!refreshing) {
        refreshing = authApi.refresh({ refreshToken }).finally(() => { refreshing = null })
      }
      const fresh = await refreshing
      tokenStorage.set(fresh)
      original.headers!['Authorization'] = `Bearer ${fresh.accessToken}`
      return http(original)
    } catch {
      forceLogout()
      return Promise.reject(err)
    }
  },
)

// ─── Auth ─────────────────────────────────────────────────────────────────────

export const authApi = {
  register: (body: RegisterRequest) =>
    http.post<AuthResponse>('/auth/register', body).then((r) => r.data),
  login: (body: LoginRequest) =>
    http.post<AuthResponse>('/auth/login', body).then((r) => r.data),
  refresh: (body: TokenToRefresh) =>
    http.post<AuthResponse>('/auth/refresh', body).then((r) => r.data),
  logout: (body: TokenToRefresh) =>
    http.post<void>('/auth/logout', body),
}

// ─── User ─────────────────────────────────────────────────────────────────────

export const userApi = {
  getMe:    () => http.get<User>('/users/me').then((r) => r.data),
  updateMe: (body: UserUpdateRequest) => http.patch<User>('/users/me', body).then((r) => r.data),
}

// ─── Tags ─────────────────────────────────────────────────────────────────────

export const tagApi = {
  create:     (body: TagCreateRequest)              => http.post<Tag>('/tag', body).then((r) => r.data),
  updateName: (id: string, body: TagUpdateRequest)  => http.patch<Tag>(`/tag/${id}/name`, body).then((r) => r.data),
  findById:   (id: string)                          => http.get<Tag>(`/tag/id/${id}`).then((r) => r.data),
  findByName: (name: string)                        => http.get<Tag>(`/tag/name/${encodeURIComponent(name)}`).then((r) => r.data),
  list:       (params: CursorParams = {})           => http.get<Slice<Tag>>('/tag', { params }).then((r) => r.data),
}

// ─── Brands ───────────────────────────────────────────────────────────────────

export const brandApi = {
  create:     (body: BrandCreateRequest)                        => http.post<TabacoBrand>('/brand', body).then((r) => r.data),
  update:     (id: string, body: BrandUpdateRequest)            => http.put<TabacoBrand>(`/brand/${id}`, body).then((r) => r.data),
  findById:   (id: string)                                      => http.get<TabacoBrand>(`/brand/id/${id}`).then((r) => r.data),
  findByName: (name: string)                                    => http.get<TabacoBrand[]>(`/brand/name/${encodeURIComponent(name)}`).then((r) => r.data),
  findByTags: (tagIds: string[])                                => http.get<TabacoBrand[]>('/brand/brands', { params: { tags: tagIds.join(',') } }).then((r) => r.data),
  list: (params: CursorParams & { name?: string; tagIds?: string[] } = {}) =>
    http.get<Slice<TabacoBrand>>('/brand', { params }).then((r) => r.data),
  addTag:     (body: UpdateTagForBrandRequest)                  => http.patch<TabacoBrand>('/brand/add-tag', body).then((r) => r.data),
  removeTag:  (body: UpdateTagForBrandRequest)                  => http.patch<TabacoBrand>('/brand/remove-tag', body).then((r) => r.data),
  delete:     (id: string)                                      => http.delete(`/brand/${id}`),
}

// ─── Flavors ──────────────────────────────────────────────────────────────────

export const flavorApi = {
  list: (params: { cursor?: string; limit?: number } = {}) =>
    http.get<TabacoFlavor[]>('/flavor', { params: { cursor: params.cursor, limit: params.limit ?? 20 } }),

  search: (params: { brandId?: string; name?: string; tagIds?: string[]; cursor?: string; limit?: number }) =>
    http.get<TabacoFlavor[]>('/flavor/search', { params: { ...params, limit: params.limit ?? 20 } }),

  findByBrandId: (brandId: string, params: { cursor?: string; limit?: number } = {}) =>
    http.get<TabacoFlavor[]>(`/flavor/brand/${brandId}`, { params: { cursor: params.cursor, limit: params.limit ?? 20 } }),

  findById: (id: string) =>
    http.get<TabacoFlavor>(`/flavor/id/${id}`),

  create: (body: FlavorCreateRequest) =>
    http.post<TabacoFlavor>('/flavor', body),

  update: (id: string, body: FlavorUpdateRequest) =>
    http.put<TabacoFlavor>(`/flavor/${id}`, body),

  addTag: (body: UpdateTagForFlavorRequest) =>
    http.patch<TabacoFlavor>('/flavor/add-tag', body),

  removeTag: (body: UpdateTagForFlavorRequest) =>
    http.patch<TabacoFlavor>('/flavor/remove-tag', body),

  delete: (id: string) =>
    http.delete(`/flavor/${id}`),
}

// ─── Market ───────────────────────────────────────────────────────────────────

export const marketApi = {
  list: (params: MarketListParams = {}) =>
    http.get<MarketArcView[]>('/market', { params }),

  findById: (id: string) =>
    http.get<MarketArcView>(`/market/${id}`),

  create: (body: MarketCreateRequest) =>
    http.post<MarketArcView>('/market', body),

  update: (id: string, body: MarketUpdateRequest) =>
    http.put<MarketArcView>(`/market/${id}`, body),

  delete: (id: string) =>
    http.delete(`/market/${id}`),

  updateCount: (id: string, body: MarketUpdateCountRequest) =>
    http.patch<MarketArcView>(`/market/${id}/count`, body),

  totalWeightByFlavor: (flavorId: string) =>
    http.get<MarketTotalWeightView>(`/market/total-weight/${flavorId}`).then(r => r.data),
}

// ─── Audit ────────────────────────────────────────────────────────────────────

export const auditApi = {
  listBrand:  (params: CursorParams & { eventType?: AuditEventType; entityId?: string } = {}) =>
    http.get<Slice<BrandAuditRecord>>('/audit/brand', { params }).then(r => r.data),
  listFlavor: (params: CursorParams & { eventType?: AuditEventType; entityId?: string } = {}) =>
    http.get<Slice<FlavorAuditRecord>>('/audit/flavor', { params }).then(r => r.data),
  listPack:   (params: CursorParams & { eventType?: AuditEventType; entityId?: string } = {}) =>
    http.get<Slice<PackAuditRecord>>('/audit/pack', { params }).then(r => r.data),
  listMarket: (params: CursorParams & { eventType?: AuditEventType; entityId?: string } = {}) =>
    http.get<Slice<MarketAuditRecord>>('/audit/market', { params }).then(r => r.data),
}

// ─── Reports ──────────────────────────────────────────────────────────────────

export const reportsApi = {
  downloadStock: () =>
    http.get('/reports/stock', { responseType: 'blob' }),
}

// ─── Packs ────────────────────────────────────────────────────────────────────

export const packApi = {
  list:     (params: PackListParams = {})                 => http.get<Slice<FlavorPack>>('/pack', { params }).then((r) => r.data),
  findById: (id: string)                                 => http.get<FlavorPack>(`/pack/${encodeURIComponent(id)}`).then((r) => r.data),
  create:   (body: PackCreateRequest)                    => http.post<FlavorPack>('/pack', body).then((r) => r.data),
  update:   (id: string, body: PackUpdateRequest)        => http.put<FlavorPack>(`/pack/${encodeURIComponent(id)}`, body).then((r) => r.data),
  delete:   (id: string)                                 => http.delete<void>(`/pack/${encodeURIComponent(id)}`),
}
