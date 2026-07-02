// Routes de l'application (espace client public + espace barmaker) et garde d'accès qui protège
// les pages barmaker (meta.requiresAuth).
import { createRouter, createWebHistory, type RouteLocationNormalized } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  // Remonte en haut à chaque navigation (sauf retour arrière qui restaure la position)
  scrollBehavior(_to, _from, savedPosition) {
    return savedPosition || { top: 0 }
  },
  routes: [
    // Redirection racine vers la table de démo
    {
      path: '/',
      redirect: '/t/table-1',
    },

    // === ROUTES CLIENT (publiques) ===

    // Accueil avec table
    {
      path: '/t/:qrSlug',
      name: 'TableWelcome',
      component: () => import('../views/client/TableWelcome.vue'),
    },

    // Carte des cocktails
    {
      path: '/menu',
      name: 'Menu',
      component: () => import('../views/client/Menu.vue'),
    },

    // Fiche cocktail
    {
      path: '/cocktail/:id',
      name: 'CocktailDetail',
      component: () => import('../views/client/CocktailDetail.vue'),
    },

    // Panier
    {
      path: '/cart',
      name: 'Cart',
      component: () => import('../views/client/Cart.vue'),
    },

    // Suivi commande
    {
      path: '/order/:id',
      name: 'OrderTracking',
      component: () => import('../views/client/OrderTracking.vue'),
    },

    // === ROUTES BARMAKER ===

    // Login barmaker
    {
      path: '/barmaker/login',
      name: 'BarmakerLogin',
      component: () => import('../views/barmaker/Login.vue'),
    },

    // File de commandes
    {
      path: '/barmaker/orders',
      name: 'BarmakerOrders',
      component: () => import('../views/barmaker/Orders.vue'),
      meta: { requiresAuth: true },
    },

    // Traitement commande
    {
      path: '/barmaker/orders/:id',
      name: 'BarmakerOrderDetail',
      component: () => import('../views/barmaker/OrderDetail.vue'),
      meta: { requiresAuth: true },
    },

    // Gestion carte
    {
      path: '/barmaker/menu',
      name: 'BarmakerMenu',
      component: () => import('../views/barmaker/Menu.vue'),
      meta: { requiresAuth: true },
    },

    // Nouveau cocktail
    {
      path: '/barmaker/menu/new',
      name: 'BarmakerCocktailNew',
      component: () => import('../views/barmaker/CocktailForm.vue'),
      meta: { requiresAuth: true },
    },

    // Edit cocktail
    {
      path: '/barmaker/menu/:id/edit',
      name: 'BarmakerCocktailEdit',
      component: () => import('../views/barmaker/CocktailForm.vue'),
      meta: { requiresAuth: true },
    },

    // QR codes des tables
    {
      path: '/barmaker/tables',
      name: 'BarmakerTables',
      component: () => import('../views/barmaker/Tables.vue'),
      meta: { requiresAuth: true },
    },
  ],
})

// Garde de navigation pour les routes protégées
router.beforeEach((to: RouteLocationNormalized) => {
  const authStore = useAuthStore()
  const requiresAuth = to.meta.requiresAuth as boolean | undefined

  if (requiresAuth && !authStore.isAuthenticated) {
    return { name: 'BarmakerLogin' }
  }

  return true
})

export default router
