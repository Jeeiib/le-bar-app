<template>
  <BarmakerLayout>
    <!-- Chargement -->
    <p v-if="order.id === 0" class="loading">Chargement de la commande…</p>

    <!-- En-tête -->
    <div v-else class="commande-header">
      <div class="header-left">
        <button @click="goBack" class="icon-btn">←</button>
        <div>
          <h2>Commande #B-{{ String(order.id).padStart(4, '0') }}</h2>
          <span class="ometa">
            {{ order.tableLabel }} • {{ order.items.length }}
            {{ order.items.length === 1 ? 'cocktail' : 'cocktails' }}
          </span>
        </div>
      </div>
      <span class="status-badge" :class="statusClass(order.status)">
        <i></i>{{ statusLabel(order.status) }}
      </span>
    </div>

    <!-- Items -->
    <div v-if="order.items.length > 0" class="items-list">
      <div v-for="item in order.items" :key="item.id" class="treatcard">
        <div class="th">
          <div>
            <span class="tname">{{ item.cocktailName }}</span>
            <span class="tsize">{{ item.size }} · {{ sizeCl(item.size) }}</span>
          </div>
          <button
            v-if="item.preparationStatus !== 'COMPLETED'"
            @click="advanceItemStep(item.id)"
            class="nextbtn"
            :disabled="isAdvancing"
          >
            Étape suivante →
          </button>
          <span v-else class="status-badge sb-done">
            <i></i>Terminé
          </span>
        </div>
        <PrepStepper :status="item.preparationStatus" />
      </div>
    </div>

    <!-- Message de complétude -->
    <div v-if="isOrderComplete" class="completion-message">
      <h3>Commande complète</h3>
      <p>Tous les cocktails sont préparés.</p>
    </div>
  </BarmakerLayout>
</template>

<script setup lang="ts">
// Traitement d'une commande par le barmaker : fait avancer chaque cocktail d'une étape de
// préparation (bouton « Étape suivante ») jusqu'à la commande complète.
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import * as api from '@/api/client'
import { sizeCl } from '@/utils/sizes'
import { useUiStore } from '@/stores/ui'
import type { Order } from '@/types'
import { statusLabel, statusClass } from '@/utils/orderStatus'
import BarmakerLayout from '@/components/BarmakerLayout.vue'
import PrepStepper from '@/components/PrepStepper.vue'

const router = useRouter()
const route = useRoute()
const ui = useUiStore()
const order = ref<Order>({
  id: 0,
  tableId: 0,
  tableLabel: '',
  customerName: null,
  status: 'ORDERED',
  createdAt: new Date().toISOString(),
  items: [],
  total: 0,
})
const isAdvancing = ref(false)

const orderId = computed(() => {
  const id = route.params.id
  if (typeof id === 'string') return parseInt(id)
  if (Array.isArray(id) && id[0]) return parseInt(id[0])
  return 0
})

const isOrderComplete = computed(() => {
  return order.value.items.length > 0 && order.value.items.every((i) => i.preparationStatus === 'COMPLETED')
})

const goBack = () => {
  router.push('/barmaker/orders')
}

const loadOrder = async () => {
  try {
    if (orderId.value > 0) {
      order.value = await api.getOrder(orderId.value)
    }
  } catch (error) {
    console.error('Erreur lors du chargement de la commande:', error)
    ui.toast('Erreur lors du chargement de la commande', 'error')
  }
}

const advanceItemStep = async (itemId: number) => {
  isAdvancing.value = true
  try {
    if (orderId.value > 0) {
      const updatedOrder = await api.advanceItem(orderId.value, itemId)
      order.value = updatedOrder
    }
  } catch (error) {
    console.error('Erreur lors de l\'avancement:', error)
  } finally {
    isAdvancing.value = false
  }
}

onMounted(() => {
  loadOrder()
})
</script>

<style scoped>
.commande-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 22px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.icon-btn {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: white;
  border: 1px solid rgba(22, 22, 29, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: var(--encre);
  cursor: pointer;
  transition: background-color 0.2s;
}

.icon-btn:hover {
  background: rgba(22, 22, 29, 0.05);
}

.commande-header h2 {
  font-family: var(--font-display);
  font-weight: 900;
  font-size: 28px;
  letter-spacing: -0.01em;
  margin: 0;
}

.ometa {
  font-family: var(--font-mono);
  font-size: 12px;
  color: rgba(22, 22, 29, 0.45);
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

.items-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.treatcard {
  background: white;
  border-radius: 16px;
  padding: 20px 22px;
  box-shadow: 0 4px 14px rgba(22, 22, 29, 0.06);
  margin-bottom: 14px;
}

.th {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.tname {
  font-family: var(--font-display);
  font-weight: 800;
  font-size: 19px;
  color: var(--encre);
}

.tsize {
  font-family: var(--font-mono);
  font-size: 12px;
  background: var(--glacon);
  border: 1px solid rgba(22, 22, 29, 0.1);
  border-radius: 8px;
  padding: 3px 8px;
  margin-left: 8px;
  color: var(--encre);
}

.nextbtn {
  background: var(--encre);
  color: white;
  border: none;
  border-radius: 11px;
  padding: 11px 18px;
  font-weight: 600;
  font-size: 14px;
  white-space: nowrap;
  cursor: pointer;
  transition: background-color 0.2s;
}

.nextbtn:hover:not(:disabled) {
  background-color: #0a0a10;
}

.nextbtn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.completion-message {
  background: rgba(0, 194, 168, 0.1);
  border: 1px solid var(--menthe);
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  margin-top: 20px;
}

.completion-message h3 {
  color: #0a7a6a;
  margin: 0 0 4px 0;
  font-size: 18px;
}

.completion-message p {
  color: #0a7a6a;
  margin: 0;
  font-size: 14px;
}

/* Mobile : en-tête et carte item se réorganisent */
@media (max-width: 640px) {
  .commande-header {
    flex-wrap: wrap;
    gap: 12px;
  }

  .commande-header h2 {
    font-size: 22px;
  }

  .th {
    flex-wrap: wrap;
    gap: 12px;
  }

  .nextbtn {
    width: 100%;
  }
}
</style>
