import { createRouter, createWebHistory, type RouteLocationNormalized } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  // Remonte en haut à chaque navigation (sauf retour arrière qui restaure la position)
  scrollBehavior(_to, _from, savedPosition) {
    return savedPosition || { top: 0 }
  },
  routes: [
    // Redirect racine vers table de démo
    {
      path: '/',
      redirect: '/t/table-1',
    },

    // === ROUTES CLIENT (publiques) ===

    // Accueil avec table
    {
      path: '/t/:qrSlug',
      name: 'AccueilTable',
      component: () => import('../views/client/AccueilTable.vue'),
    },

    // Carte des cocktails
    {
      path: '/carte',
      name: 'Carte',
      component: () => import('../views/client/Carte.vue'),
    },

    // Fiche cocktail
    {
      path: '/cocktail/:id',
      name: 'FicheCocktail',
      component: () => import('../views/client/FicheCocktail.vue'),
    },

    // Panier
    {
      path: '/panier',
      name: 'Panier',
      component: () => import('../views/client/Panier.vue'),
    },

    // Suivi commande
    {
      path: '/commande/:id',
      name: 'SuiviCommande',
      component: () => import('../views/client/SuiviCommande.vue'),
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
      path: '/barmaker/commandes',
      name: 'BarmakerCommandes',
      component: () => import('../views/barmaker/Commandes.vue'),
      meta: { requiresAuth: true },
    },

    // Traitement commande
    {
      path: '/barmaker/commandes/:id',
      name: 'BarmakerCommande',
      component: () => import('../views/barmaker/Commande.vue'),
      meta: { requiresAuth: true },
    },

    // Gestion carte
    {
      path: '/barmaker/carte',
      name: 'BarmakerCarte',
      component: () => import('../views/barmaker/Carte.vue'),
      meta: { requiresAuth: true },
    },

    // Nouveau cocktail
    {
      path: '/barmaker/carte/nouveau',
      name: 'BarmakerCocktailNouveau',
      component: () => import('../views/barmaker/CocktailForm.vue'),
      meta: { requiresAuth: true },
    },

    // Edit cocktail
    {
      path: '/barmaker/carte/:id/edit',
      name: 'BarmakerCocktailEdit',
      component: () => import('../views/barmaker/CocktailForm.vue'),
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
