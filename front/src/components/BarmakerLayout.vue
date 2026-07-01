<template>
  <div class="bm">
    <!-- Sidebar sombre -->
    <div class="side">
      <div class="b">LE BAR'APP</div>
      <nav class="nav-links">
        <router-link
          to="/barmaker/commandes"
          class="nav-link"
          :class="{ on: isActive('/barmaker/commandes') }"
        >
          Commandes
        </router-link>
        <router-link
          to="/barmaker/carte"
          class="nav-link"
          :class="{ on: isActive('/barmaker/carte') }"
        >
          Ma carte
        </router-link>
      </nav>
      <div class="role">{{ role }} • {{ location }}</div>
      <button @click="handleLogout" class="logout-btn">
        Déconnexion
      </button>
    </div>

    <!-- Zone principale -->
    <div class="main">
      <slot></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

interface Props {
  role?: string
  location?: string
}

withDefaults(defineProps<Props>(), {
  role: 'Barmaker',
  location: 'Comptoir',
})

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const isActive = (path: string) => {
  return route.path.startsWith(path)
}

const handleLogout = () => {
  authStore.logout()
  router.push('/barmaker/login')
}
</script>

<style scoped>
.bm {
  display: flex;
  min-height: 100vh;
}

.side {
  width: 218px;
  background: var(--encre);
  color: white;
  padding: 24px 18px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-shrink: 0;
  /* Sticky : la fenêtre défile, la sidebar reste visible (scroll géré au niveau window) */
  position: sticky;
  top: 0;
  height: 100vh;
  align-self: flex-start;
}

.side .b {
  font-family: var(--font-display);
  font-weight: 900;
  font-size: 20px;
  margin-bottom: 18px;
  color: white;
}

/* Conteneur transparent en desktop (les liens s'empilent comme des enfants directs) */
.nav-links {
  display: contents;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 12px 14px;
  border-radius: 12px;
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  font-weight: 500;
  font-size: 15px;
  transition: background-color 0.2s, color 0.2s;
}

.nav-link:hover {
  background-color: rgba(255, 255, 255, 0.08);
}

.nav-link.on {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.role {
  margin-top: auto;
  font-family: var(--font-mono);
  font-size: 11px;
  color: rgba(255, 255, 255, 0.45);
  padding: 0 14px;
}

.logout-btn {
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  font-weight: 500;
  padding: 12px 14px;
  margin-top: 8px;
  cursor: pointer;
  border-radius: 12px;
  text-align: left;
  transition: background-color 0.2s, color 0.2s;
}

.logout-btn:hover {
  background-color: rgba(255, 255, 255, 0.08);
  color: white;
}

.main {
  flex: 1;
  padding: 28px 30px;
  min-width: 0;
}

/* Mobile : barre du haut. Ligne 1 = logo + déconnexion, ligne 2 = onglets segmentés */
@media (max-width: 768px) {
  .bm {
    flex-direction: column;
  }

  .side {
    width: 100%;
    height: auto;
    flex-direction: row;
    flex-wrap: wrap;
    align-items: center;
    column-gap: 8px;
    row-gap: 10px;
    padding: 12px 16px;
    position: sticky;
    top: 0;
    z-index: 20;
  }

  .side .b {
    margin-bottom: 0;
  }

  .logout-btn {
    order: 1;
    margin: 0 0 0 auto;
    width: auto;
    padding: 7px 12px;
    font-size: 13px;
  }

  /* Ligne 2 : les onglets en segmenté pleine largeur */
  .nav-links {
    display: flex;
    order: 2;
    flex-basis: 100%;
    gap: 8px;
  }

  .nav-link {
    flex: 1;
    justify-content: center;
    padding: 10px 12px;
    font-size: 14px;
    background: rgba(255, 255, 255, 0.06);
  }

  .nav-link.on {
    background: rgba(255, 255, 255, 0.16);
  }

  .role {
    display: none;
  }

  .main {
    padding: 18px 16px;
  }
}
</style>
