<template>
  <div class="screen">
    <!-- Appbar -->
    <div class="appbar">
      <button class="icon-btn" @click="goBack">←</button>
      <span class="brand">Mon panier</span>
      <span class="pill">{{ tableStore.current?.label }}</span>
    </div>

    <!-- Liste panier ou vide -->
    <div class="content">
      <div v-if="cartStore.count === 0" class="empty">
        <p class="empty-title">Ton verre est vide.</p>
        <button class="btn-link" @click="goBack">Retour à la carte</button>
      </div>

      <div v-else class="items">
        <div
          v-for="(item, idx) in cartStore.items"
          :key="idx"
          class="cart-item"
        >
          <img
            :src="item.cocktail.imageUrl"
            :alt="item.cocktail.name"
            class="thumb"
          />
          <div class="item-info">
            <div class="item-name">
              {{ item.cocktail.name }}
              <span class="size-badge">{{ item.size }} · {{ sizeCl(item.size) }}</span>
            </div>
            <div class="item-price">{{ (item.unitPrice * item.quantity).toFixed(2) }} €</div>
          </div>
          <div class="item-actions">
            <button class="qty-btn" @click="cartStore.decrement(idx)">−</button>
            <span class="qty">{{ item.quantity }}</span>
            <button class="qty-add" aria-label="Ajouter" @click="cartStore.increment(idx)"></button>
          </div>
        </div>
      </div>
    </div>

    <!-- Pied de page -->
    <div v-if="cartStore.count > 0" class="footer">
      <div class="form-group">
        <label class="label-in">Prénom pour la commande (optionnel)</label>
        <input
          v-model="tableStore.customerName"
          class="field"
          placeholder="Léa"
        />
      </div>

      <div class="total-line">
        <span class="total-label">Total</span>
        <span class="total-amount">{{ cartStore.total.toFixed(2) }} €</span>
      </div>

      <button class="btn" :disabled="isCommanding" @click="placeOrder">
        <span v-if="!isCommanding">
          Commander
          <span class="amt">· {{ cartStore.total.toFixed(2) }} €</span>
        </span>
        <span v-else>Commande en cours...</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
// Panier client : récapitulatif des verres, ajustement des quantités et envoi de la commande
// (une ligne par unité pour que le barmaker suive chaque verre séparément).
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { useTableStore } from '@/stores/table'
import { sizeCl } from '@/utils/sizes'
import { useUiStore } from '@/stores/ui'
import * as api from '@/api/client'

const router = useRouter()
const cartStore = useCartStore()
const tableStore = useTableStore()
const ui = useUiStore()
const isCommanding = ref(false)

const goBack = () => {
  router.push('/carte')
}

const placeOrder = async () => {
  if (!tableStore.current || cartStore.count === 0) {
    return
  }

  isCommanding.value = true
  try {
    const order = await api.createOrder({
      tableId: tableStore.current.id,
      customerName: tableStore.customerName || undefined,
      items: cartStore.items.flatMap((item) =>
        Array.from({ length: item.quantity }, () => ({
          cocktailId: item.cocktail.id,
          size: item.size,
        }))
      ),
    })

    cartStore.clear()
    router.push(`/commande/${order.id}`)
  } catch (error) {
    ui.toast(error instanceof Error ? error.message : 'Erreur lors de la commande', 'error')
  } finally {
    isCommanding.value = false
  }
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
  background-color: rgba(22, 22, 29, 0.04);
}

.brand {
  font-family: var(--font-display);
  font-weight: 900;
  font-size: 17px;
  letter-spacing: -0.01em;
  flex: 1;
  text-align: center;
}

.pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: var(--encre);
  color: white;
  border-radius: 999px;
  padding: 6px 11px;
  font-family: var(--font-mono);
  font-weight: 700;
  font-size: 11px;
}

.content {
  padding: 10px 20px 0;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  gap: 20px;
}

.empty-title {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 18px;
  color: rgba(22, 22, 29, 0.6);
}

.btn-link {
  background: transparent;
  color: var(--spritz);
  font-size: 14px;
  cursor: pointer;
  text-decoration: underline;
  border: none;
  font-family: var(--font-body);
  padding: 0;
}

.btn-link:hover {
  color: var(--menthe);
}

.items {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 12px;
  background: white;
  border: 1px solid rgba(22, 22, 29, 0.1);
  border-radius: 0;
  padding: 10px 16px;
  margin-bottom: 1px;
}

.cart-item:first-child {
  border-top-left-radius: 12px;
  border-top-right-radius: 12px;
}

.cart-item:last-child {
  border-bottom-left-radius: 12px;
  border-bottom-right-radius: 12px;
}

.thumb {
  width: 54px;
  height: 54px;
  border-radius: 12px;
  flex-shrink: 0;
  object-fit: cover;
}

.item-info {
  flex: 1;
}

.item-name {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.size-badge {
  font-family: var(--font-mono);
  font-size: 11px;
  background: var(--glacon);
  border: 1px solid rgba(22, 22, 29, 0.1);
  border-radius: 7px;
  padding: 2px 7px;
  font-weight: 400;
  white-space: nowrap;
  flex-shrink: 0;
}

.item-price {
  font-family: var(--font-mono);
  color: var(--encre);
  font-size: 13px;
  margin-top: 3px;
}

.item-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.qty-btn {
  width: auto;
  min-width: 24px;
  height: 24px;
  background: transparent;
  border: none;
  color: rgba(22, 22, 29, 0.45);
  cursor: pointer;
  font-size: 16px;
}

.qty {
  font-family: var(--font-mono);
  font-weight: 700;
  font-size: 13px;
  min-width: 20px;
  text-align: center;
}

.qty-add {
  position: relative;
  flex: none;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: var(--spritz);
  border: none;
  cursor: pointer;
  transition: background-color 0.2s;
}

.qty-add::before,
.qty-add::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  background: white;
  border-radius: 1px;
}

.qty-add::before {
  width: 11px;
  height: 2.5px;
  transform: translate(-50%, -50%);
}

.qty-add::after {
  width: 2.5px;
  height: 11px;
  transform: translate(-50%, -50%);
}

.qty-add:hover {
  background-color: #e63d1f;
}

.footer {
  padding: 16px 20px 26px;
  border-top: 1px solid rgba(22, 22, 29, 0.1);
  background: white;
}

.form-group {
  margin-bottom: 16px;
}

.label-in {
  font-family: var(--font-mono);
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: rgba(22, 22, 29, 0.45);
  margin-bottom: 7px;
  display: block;
}

.field {
  display: block;
  width: 100%;
  background: var(--glacon);
  border: 1px solid rgba(22, 22, 29, 0.1);
  border-radius: 12px;
  padding: 14px 16px;
  font-family: var(--font-body);
  font-size: 15px;
  color: var(--encre);
}

.field::placeholder {
  color: rgba(22, 22, 29, 0.45);
}

.total-line {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 18px;
}

.total-label {
  color: rgba(22, 22, 29, 0.6);
}

.total-amount {
  font-family: var(--font-mono);
  font-weight: 700;
  font-size: 26px;
}

.btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background: var(--spritz);
  color: white;
  border: none;
  border-radius: 14px;
  padding: 16px;
  font-family: var(--font-body);
  font-weight: 600;
  font-size: 16px;
  width: 100%;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn:hover:not(:disabled) {
  background-color: #e63d1f;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn .amt {
  font-family: var(--font-mono);
  font-weight: 700;
}
</style>
