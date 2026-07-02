<template>
  <div class="screen">
    <!-- Appbar -->
    <div class="appbar">
      <span class="brand">LE BAR'APP</span>
      <span class="status-badge" :class="statusClass(order?.status)">
        <span class="status-dot"></span>
        {{ statusLabel(order?.status) }}
      </span>
    </div>

    <!-- Titre écran -->
    <div class="header">
      <h2 class="title">Commande envoyée !</h2>
      <p class="subtitle">
        {{ order?.tableLabel }} · N° <span class="order-id">{{ order?.id }}</span> ·
        {{ countReady }} cocktail<span v-if="countReady !== 1">s</span> sur
        {{ order?.items.length }} prêt<span v-if="countReady !== 1">s</span>
      </p>
    </div>

    <!-- Items avec steppers -->
    <div class="content">
      <div
        v-for="item in order?.items"
        :key="item.id"
        class="item-card"
      >
        <div class="item-header">
          <div class="item-name-row">
            <img
              :src="getCocktailImage(item.cocktailId)"
              :alt="item.cocktailName"
              class="thumb"
            />
            <span class="item-name">{{ item.cocktailName }}</span>
          </div>
          <span
            v-if="item.preparationStatus === 'TERMINEE'"
            class="item-badge ready"
          >
            <span class="badge-dot"></span>
            Prêt
          </span>
        </div>

        <!-- Stepper de préparation -->
        <PrepStepper :status="item.preparationStatus" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import * as api from '@/api/client'
import type { Order } from '@/types'
import { statusLabel, statusClass } from '@/utils/orderStatus'
import PrepStepper from '@/components/PrepStepper.vue'

const route = useRoute()
const order = ref<Order | null>(null)
const pollInterval = ref<number | null>(null)

onMounted(async () => {
  const orderId = parseInt(route.params.id as string)
  await loadOrder(orderId)

  // Démarrer le polling toutes les 3 secondes
  pollInterval.value = window.setInterval(() => {
    loadOrder(orderId)
  }, 3000)
})

onUnmounted(() => {
  if (pollInterval.value) {
    clearInterval(pollInterval.value)
  }
})

const loadOrder = async (orderId: number) => {
  try {
    order.value = await api.getOrder(orderId)

    // Arrêter le polling si la commande est terminée
    if (order.value.status === 'TERMINEE' && pollInterval.value) {
      clearInterval(pollInterval.value)
      pollInterval.value = null
    }
  } catch (error) {
    console.error('Erreur lors du chargement de la commande:', error)
  }
}

const countReady = computed(() => {
  if (!order.value) return 0
  return order.value.items.filter((item) => item.preparationStatus === 'TERMINEE').length
})

const getCocktailImage = (cocktailId: number) => {
  return `/api/cocktails/${cocktailId}/image`
}
</script>

<style scoped>
.screen {
  min-height: 100vh;
  background-color: var(--glacon);
  display: flex;
  flex-direction: column;
  width: 100%;
}

.appbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  background: white;
  box-shadow: 0 2px 4px rgba(22, 22, 29, 0.04);
}

.brand {
  font-family: var(--font-display);
  font-weight: 900;
  font-size: 19px;
  letter-spacing: -0.01em;
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

.status-badge .status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}

.status-badge.sb-new {
  background: rgba(255, 77, 46, 0.12);
  color: var(--spritz);
}

.status-badge.sb-new .status-dot {
  background: var(--spritz);
}

.status-badge.sb-prep {
  background: rgba(255, 197, 61, 0.18);
  color: #9a6b00;
}

.status-badge.sb-prep .status-dot {
  background: var(--agrume);
}

.status-badge.sb-done {
  background: rgba(0, 194, 168, 0.16);
  color: #0a7a6a;
}

.status-badge.sb-done .status-dot {
  background: var(--menthe);
}

.header {
  padding: 6px 20px 4px;
}

.title {
  font-family: var(--font-display);
  font-weight: 900;
  font-size: 34px;
  line-height: 0.95;
  letter-spacing: -0.01em;
  margin: 0 0 6px;
}

.subtitle {
  font-size: 15px;
  color: rgba(22, 22, 29, 0.6);
  margin: 0;
}

.order-id {
  font-family: var(--font-mono);
  color: var(--encre);
  font-weight: 700;
}

.content {
  flex: 1;
  padding: 14px 20px 26px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.item-card {
  background: white;
  border-radius: 16px;
  padding: 18px 16px;
  box-shadow: 0 4px 12px rgba(22, 22, 29, 0.06);
}

.item-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.item-name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.thumb {
  width: 34px;
  height: 34px;
  border-radius: 9px;
  flex-shrink: 0;
  object-fit: cover;
}

.item-name {
  font-family: var(--font-display);
  font-weight: 800;
  font-size: 16px;
}

.item-badge {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  border-radius: 999px;
  padding: 6px 13px;
  font-family: var(--font-body);
  font-weight: 600;
  font-size: 12px;
  background: rgba(0, 194, 168, 0.16);
  color: #0a7a6a;
}

.item-badge .badge-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--menthe);
  display: inline-block;
}

.item-badge.ready {
  background: rgba(0, 194, 168, 0.16);
  color: #0a7a6a;
}

</style>
