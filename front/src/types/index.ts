// Size pour les cocktails
export type Size = 'S' | 'M' | 'L'

// Statuts de commande
export type OrderStatus = 'ORDERED' | 'IN_PREPARATION' | 'COMPLETED'

// Statuts de préparation des items
export type PrepStatus = 'INGREDIENTS' | 'ASSEMBLY' | 'GARNISH' | 'COMPLETED'

// Catégorie de cocktails
export interface Category {
  id: number
  name: string
  position: number
}

// Ingrédient d'un cocktail
export interface CocktailIngredient {
  name: string
  measure: string | null
}

// Prix par taille
export interface SizePrice {
  size: Size
  price: number
}

// Cocktail complet
export interface Cocktail {
  id: number
  name: string
  description: string | null
  available: boolean
  categoryId: number
  categoryName: string
  ingredients: CocktailIngredient[]
  sizes: SizePrice[]
  imageUrl: string
}

// Item dans une commande
export interface OrderItem {
  id: number
  cocktailId: number
  cocktailName: string
  size: Size
  unitPrice: number
  preparationStatus: PrepStatus
}

// Commande complète
export interface Order {
  id: number
  tableId: number
  tableLabel: string
  customerName: string | null
  status: OrderStatus
  createdAt: string
  items: OrderItem[]
  total: number
}

// Infos table (pour QR code)
export interface TableInfo {
  id: number
  label: string
  qrSlug: string
}

// Réponse d'authentification
export interface AuthResponse {
  token: string
  name: string
  role: string
}

// Cocktail externe (API TheCocktailDB)
export interface ExternalCocktail {
  name: string
  category: string | null
  instructions: string | null
  imageUrl: string | null
  ingredients: CocktailIngredient[]
}

// Requête pour créer/modifier un cocktail
export interface CocktailRequest {
  name: string
  description: string | null
  available: boolean
  categoryId: number
  ingredients: CocktailIngredient[]
  sizes: SizePrice[]
  imageSourceUrl?: string
}

// Requête pour créer une commande
export interface CreateOrderRequest {
  tableId: number
  customerName?: string
  items: {
    cocktailId: number
    size: Size
  }[]
}
