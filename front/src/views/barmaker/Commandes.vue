<template>
  <BarmakerLayout>
    <div class="commandes-header">
      <h2>Commandes</h2>
      <div class="status-badge sb-new">
        <i></i>{{ newOrdersCount }} nouvelles
      </div>
    </div>

    <!-- Onglets de filtrage -->
    <div class="tabs">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        @click="selectedTab = tab.value"
        :class="['tab', { active: selectedTab === tab.value }]"
      >
        {{ tab.label }} ({{ getTabCount(tab.value) }})
      </button>
    </div>

    <!-- Grille de commandes -->
    <div v-if="filteredOrders.length > 0" class="ordergrid">
      <div v-for="order in filteredOrders" :key="order.id" class="ocard">
        <div class="otop">
          <div>
            <div class="onum">#B-{{ String(order.id).padStart(4, '0') }}</div>
            <div class="ometa">{{ formatRelativeTime(order.createdAt) }}</div>
          </div>
          <span class="status-badge" :class="statusClass(order.status)">
            <i></i>{{ statusLabel(order.status) }}
          </span>
        </div>
        <div class="oclient">
          {{ order.tableLabel }} • {{ order.items.length }}
          {{ order.items.length === 1 ? 'cocktail' : 'cocktails' }}
        </div>
        <button
          @click="goToOrder(order.id)"
          class="obtn"
        >
          Traiter
        </button>
      </div>
    </div>

    <!-- Vide -->
    <div v-else class="empty-state">
      <p>{{ emptyMessage }}</p>
    </div>
  </BarmakerLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import * as api from '@/api/client'
import type { Order, OrderStatus } from '@/types'
import { statusLabel, statusClass } from '@/utils/orderStatus'
import BarmakerLayout from '@/components/BarmakerLayout.vue'

const router = useRouter()
const orders = ref<Order[]>([])
const selectedTab = ref<OrderStatus>('COMMANDEE')
let pollInterval: number | null = null

interface TabConfig {
  value: OrderStatus
  label: string
}

const tabs: TabConfig[] = [
  { value: 'COMMANDEE', label: 'À traiter' },
  { value: 'EN_PREPARATION', label: 'En cours' },
]

const getTabCount = (status: OrderStatus): number => {
  return orders.value.filter((o) => o.status === status).length
}

const filteredOrders = computed(() => {
  return orders.value.filter((o) => o.status === selectedTab.value)
})

const emptyMessage = computed(() =>
  selectedTab.value === 'COMMANDEE'
    ? 'Aucune commande à traiter pour le moment.'
    : 'Aucune commande en préparation.'
)

const newOrdersCount = computed(
  () => orders.value.filter((o) => o.status === 'COMMANDEE').length
)

const formatRelativeTime = (isoDate?: string): string => {
  if (!isoDate) return '?'

  const date = new Date(isoDate)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMins = Math.floor(diffMs / 60000)

  if (diffMins < 1) return 'À l\'instant'
  if (diffMins === 1) return 'Il y a 1 min'
  if (diffMins < 60) return `Il y a ${diffMins} min`

  const diffHours = Math.floor(diffMins / 60)
  if (diffHours === 1) return 'Il y a 1 h'
  if (diffHours < 24) return `Il y a ${diffHours} h`

  const parts = isoDate.split('T')
  return parts[0] || '?'
}

const goToOrder = (orderId: number): void => {
  router.push(`/barmaker/commandes/${orderId}`)
}

const loadOrders = async () => {
  try {
    orders.value = await api.getQueue()
  } catch (error) {
    console.error('Erreur lors du chargement des commandes:', error)
  }
}

onMounted(() => {
  loadOrders()
  // Polling toutes les 5s
  pollInterval = window.setInterval(loadOrders, 5000)
})

onUnmounted(() => {
  if (pollInterval !== null) {
    clearInterval(pollInterval)
  }
})
</script>

<style scoped>
.commandes-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 22px;
}

.commandes-header h2 {
  font-family: var(--font-display);
  font-weight: 900;
  font-size: 32px;
  letter-spacing: -0.01em;
  margin: 0;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  border-radius: 999px;
  padding: 6px 13px;
  font-family: var(--font-body);
  font-weight: 600;
  font-size: 13px;
}

.status-badge i {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.sb-prep {
  background: rgba(255, 197, 61, 0.18);
  color: #9a6b00;
}

.sb-prep i {
  background: var(--agrume);
}

.sb-done {
  background: rgba(0, 194, 168, 0.16);
  color: #0a7a6a;
}

.sb-done i {
  background: var(--menthe);
}

.sb-new {
  background: rgba(255, 77, 46, 0.12);
  color: var(--spritz);
}

.sb-new i {
  background: var(--spritz);
}

.tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 22px;
}

.tab {
  font-size: 13px;
  font-weight: 600;
  padding: 8px 14px;
  border-radius: 999px;
  background: white;
  border: 1px solid rgba(22, 22, 29, 0.1);
  color: var(--encre);
  cursor: pointer;
  transition: all 0.2s;
}

.tab.active {
  background: var(--encre);
  color: white;
  border-color: var(--encre);
}

.tab:hover:not(.active) {
  background: rgba(22, 22, 29, 0.05);
}

.ordergrid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.ocard {
  background: white;
  border-radius: 16px;
  padding: 18px;
  box-shadow: 0 4px 14px rgba(22, 22, 29, 0.06);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.otop {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.onum {
  font-family: var(--font-display);
  font-weight: 800;
  font-size: 20px;
  color: var(--encre);
}

.ometa {
  font-family: var(--font-mono);
  font-size: 12px;
  color: rgba(22, 22, 29, 0.45);
  margin-top: 2px;
}

.oclient {
  font-size: 14px;
  color: rgba(22, 22, 29, 0.6);
}

.obtn {
  margin-top: 2px;
  background: var(--spritz);
  color: white;
  border: none;
  border-radius: 11px;
  padding: 11px;
  font-weight: 600;
  font-size: 14px;
  width: 100%;
  cursor: pointer;
  transition: background-color 0.2s;
}

.obtn:hover {
  background-color: #e63d1f;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: rgba(22, 22, 29, 0.6);
  font-size: 16px;
}

.empty-state p {
  margin: 0;
}

/* Tablette/mobile : la grille passe en 2 puis 1 colonne */
@media (max-width: 900px) {
  .ordergrid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 600px) {
  .ordergrid {
    grid-template-columns: 1fr;
  }

  .commandes-header h2 {
    font-size: 26px;
  }
}
</style>
