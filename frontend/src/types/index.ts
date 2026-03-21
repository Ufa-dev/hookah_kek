// ─── Auth ─────────────────────────────────────────────────────────────────────

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
  refreshExpiresIn: number
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  email: string
  name: string
  password: string
}

export interface TokenToRefresh {
  refreshToken: string
}

// ─── User ─────────────────────────────────────────────────────────────────────

// Kotlin value class UserId is expected to serialize as a plain UUID string.
// If API returns { id: { value: "uuid" } }, update accordingly.
export interface User {
  id: string
  name: string
  email: string
  createdAt: string
  updatedAt: string
}

export interface UserUpdateRequest {
  name?: string
  email?: string
}

// ─── Tags ─────────────────────────────────────────────────────────────────────

export interface Tag {
  id: string
  name: string
  createdAt: string
  updatedAt: string
  updatedBy: string
}

export interface TagCreateRequest {
  /** 3–32 characters */
  name: string
}

export interface TagUpdateRequest {
  /** 3–32 characters */
  name: string
}

// ─── Brands ───────────────────────────────────────────────────────────────────

export interface TabacoBrand {
  id: string
  name: string
  description: string | null
  tags: Tag[]
  createdAt: string
  updatedAt: string
  updatedBy: string
}

export interface BrandCreateRequest {
  name: string
  description?: string
}

export interface BrandUpdateRequest {
  name: string
  description?: string
}

export interface UpdateTagForBrandRequest {
  brandId: string
  tagId: string
}

// ─── Flavors ──────────────────────────────────────────────────────────────────

export interface TabacoFlavor {
  id: string
  brandId: string
  name: string
  description?: string
  strength?: number
  tags: Tag[]
  createdAt: string
  updatedAt: string
  updatedBy: string
}

export interface FlavorCreateRequest {
  brandId: string
  name: string
  description?: string
  strength: number
}

export interface FlavorUpdateRequest {
  brandId: string
  name: string
  description?: string
  strength: number
}

export interface UpdateTagForFlavorRequest {
  flavorId: string
  tagId: string
}

// ─── Packs ────────────────────────────────────────────────────────────────────

export interface FlavorPack {
  id: string
  name: string
  flavorId: string | null
  currentWeightGrams: number
  totalWeightGrams: number
  createdAt: string
  updatedAt: string
  updatedBy: string
}

export interface PackCreateRequest {
  id: string
  name: string
  flavorId?: string
  currentWeightGrams: number
  totalWeightGrams: number
}

export interface PackUpdateRequest {
  name: string
  flavorId?: string
  currentWeightGrams: number
  totalWeightGrams: number
}

// ─── Market Arc ───────────────────────────────────────────────────────────────

export interface MarketArcView {
  id: string
  brandId: string
  brandName: string
  flavorId: string
  flavorName: string
  name: string
  weightGrams: number
  gtin?: string
  createdAt: string
  updatedAt: string
  updatedBy: string
}

export interface MarketCreateRequest {
  brandId: string
  flavorId: string
  name: string
  weightGrams: number
  gtin?: string
}

export interface MarketUpdateRequest {
  brandId: string
  flavorId: string
  name: string
  weightGrams: number
  gtin?: string
}

export interface MarketListParams {
  limit?: number
  after?: string
  brandName?: string
  flavorName?: string
  name?: string
  weightMin?: number
  weightMax?: number
  sortBy?: string
  sortDir?: 'asc' | 'desc'
}

// ─── Pagination ───────────────────────────────────────────────────────────────

export interface Slice<T> {
  items: T[]
  /** UUID of the last item — pass as `after` to fetch the next page. null = no more pages. */
  nextToken: string | null
}

export interface CursorParams {
  limit?: number
  after?: string
}

export interface PackListParams {
  limit?: number
  after?: string
  name?: string
  flavorId?: string
  brandId?: string
}
