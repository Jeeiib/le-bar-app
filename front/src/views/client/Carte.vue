<template>
  <div class="screen">
    <!-- Appbar -->
    <div class="appbar">
      <span class="brand">LE BAR'APP</span>
      <button class="cart-pill" @click="goToCart">
        Panier · <span class="count">{{ cartStore.count }}</span>
      </button>
    </div>

    <!-- Titre écran -->
    <div class="header">
      <h2 class="title">La carte</h2>
      <p class="subtitle">Choisis, compose, commande.</p>
    </div>

    <!-- Chips de filtrage -->
    <div class="chips">
      <button
        v-for="chip in filterChips"
        :key="chip"
        class="chip"
        :class="{ on: selectedCategory === chip }"
        @click="selectedCategory = chip"
      >
        {{ chip }}
      </button>
    </div>

    <div class="content">
      <!-- Skeleton au tout premier chargement -->
      <div v-if="!menu.loaded" class="grid">
        <div v-for="n in 6" :key="n" class="skel-card">
          <div class="skel-thumb"></div>
          <div class="skel-body">
            <div class="skel-line lg"></div>
            <div class="skel-line"></div>
            <div class="skel-line sm"></div>
          </div>
        </div>
      </div>

      <!-- Cocktails groupés par catégorie -->
      <template v-else>
        <template v-for="cat in filteredCategories" :key="cat.id">
          <div class="band" :style="bandColorStyle(cat.id)">
            {{ cat.name }}
            <span class="count">{{ getCocktailsByCategory(cat.id).length }}</span>
          </div>

          <div class="grid">
            <div
              v-for="cocktail in getCocktailsByCategory(cat.id)"
              :key="cocktail.id"
              class="card"
              @click="goToCocktail(cocktail.id)"
            >
              <img :src="cocktail.imageUrl" :alt="cocktail.name" class="thumb" />
              <div class="cbody">
                <div>
                  <div class="cname">{{ cocktail.name }}</div>
                  <div class="cdesc">{{ ingredientNames(cocktail) }}</div>
                </div>
                <div class="cprice">
                  <span class="p">dès {{ minPrice(cocktail) }} €</span>
                  <button
                    class="add"
                    aria-label="Voir le cocktail"
                    @click.stop="goToCocktail(cocktail.id)"
                  ></button>
                </div>
              </div>
            </div>
          </div>
        </template>
      </template>
    </div>

    <!-- Barre panier flottante -->
    <div class="cartbar" @click="goToCart">
      <span>Voir le panier</span>
      <span class="amt">{{ cartStore.total.toFixed(2) }} €</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { useMenuStore } from '@/stores/menu'
import type { Cocktail } from '@/types'

const router = useRouter()
const cartStore = useCartStore()
const menu = useMenuStore()

const selectedCategory = ref<string>('Tout')

onMounted(() => {
  menu.load()
})

const filterChips = computed(() => ['Tout', ...menu.categories.map((c) => c.name)])

const filteredCategories = computed(() => {
  if (selectedCategory.value === 'Tout') {
    return menu.categories
  }
  return menu.categories.filter((c) => c.name === selectedCategory.value)
})

const getCocktailsByCategory = (catId: number) => {
  return menu.cocktails.filter((c) => c.categoryId === catId)
}

const minPrice = (cocktail: Cocktail) => {
  const prices = cocktail.sizes.map((s) => s.price)
  return Math.min(...prices).toFixed(2)
}

const ingredientNames = (cocktail: Cocktail) =>
  cocktail.ingredients.map((i) => i.name).join(' · ')

const bandColorStyle = (catId: number) => {
  const catName = menu.categories.find((c) => c.id === catId)?.name || ''
  if (catName.includes('Signature')) {
    return { backgroundColor: 'var(--spritz)' }
  }
  if (catName.includes('Classique')) {
    return { backgroundColor: 'var(--menthe)' }
  }
  if (catName.includes('Sans alcool') || catName.includes('Softs')) {
    return { backgroundColor: 'var(--agrume)' }
  }
  return { backgroundColor: 'var(--encre)' }
}

const goToCocktail = (id: number) => {
  router.push(`/cocktail/${id}`)
}

const goToCart = () => {
  router.push('/panier')
}
</script>

<style scoped>
.screen {
  min-height: 100vh;
  background-color: var(--glacon);
  display: flex;
  flex-direction: column;
  width: 100%;
  padding-bottom: 90px;
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

.cart-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: var(--encre);
  color: white;
  border-radius: 999px;
  padding: 8px 13px;
  font-family: var(--font-mono);
  font-weight: 700;
  font-size: 12px;
  border: none;
  cursor: pointer;
  transition: background-color 0.2s;
}

.cart-pill:hover {
  background-color: rgba(22, 22, 29, 0.85);
}

.cart-pill .count {
  color: var(--agrume);
}

.header {
  padding: 4px 20px 0;
}

.title {
  font-family: var(--font-display);
  font-weight: 900;
  font-size: 44px;
  line-height: 0.95;
  letter-spacing: -0.01em;
  margin: 0 0 6px;
}

.subtitle {
  font-size: 15px;
  color: rgba(22, 22, 29, 0.6);
  margin: 0;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 8px 20px 12px;
}

.chip {
  font-family: var(--font-body);
  font-weight: 600;
  font-size: 13px;
  padding: 8px 14px;
  border-radius: 999px;
  background: white;
  border: 1px solid rgba(22, 22, 29, 0.1);
  color: var(--encre);
  cursor: pointer;
  transition: all 0.2s;
}

.chip.on {
  background: var(--spritz);
  border-color: var(--spritz);
  color: white;
}

.chip:hover:not(.on) {
  border-color: rgba(22, 22, 29, 0.2);
}

.content {
  flex: 1;
}

.band {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
  color: white;
  font-family: var(--font-display);
  font-weight: 900;
  font-size: 18px;
  letter-spacing: 0.01em;
}

.band .count {
  font-family: var(--font-mono);
  font-weight: 700;
  font-size: 12px;
  opacity: 0.85;
  margin-left: auto;
}

.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  padding: 14px 20px;
}

.card {
  background: white;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 6px 16px rgba(22, 22, 29, 0.08);
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 24px rgba(22, 22, 29, 0.12);
}

.thumb {
  width: 100%;
  height: 118px;
  object-fit: cover;
  display: block;
}

.cbody {
  padding: 14px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  flex: 1;
  gap: 10px;
}

.cname {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 16px;
}

.cdesc {
  font-size: 12px;
  color: rgba(22, 22, 29, 0.6);
  line-height: 1.35;
  margin-top: 5px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.cprice {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cprice .p {
  font-family: var(--font-mono);
  font-weight: 700;
  font-size: 14px;
}

/* Bouton + : croix dessinée en CSS pour un centrage parfait */
.add {
  flex: none;
  position: relative;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--spritz);
  border: none;
  padding: 0;
  cursor: pointer;
  transition: background-color 0.2s;
}

.add::before,
.add::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  background: white;
  border-radius: 1px;
}

.add::before {
  width: 13px;
  height: 2.5px;
  transform: translate(-50%, -50%);
}

.add::after {
  width: 2.5px;
  height: 13px;
  transform: translate(-50%, -50%);
}

.add:hover {
  background-color: #e63d1f;
}

/* Skeleton de chargement (calibré sur la vraie carte) */
.skel-card {
  background: white;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 6px 16px rgba(22, 22, 29, 0.08);
}

.skel-thumb {
  width: 100%;
  height: 118px;
}

.skel-body {
  padding: 14px;
}

.skel-line {
  height: 11px;
  border-radius: 6px;
  margin-bottom: 8px;
}

.skel-line.lg {
  width: 60%;
  height: 14px;
}

.skel-line.sm {
  width: 35%;
  margin-top: 18px;
  margin-bottom: 0;
}

.skel-thumb,
.skel-line {
  background: linear-gradient(90deg, #eef0f3 25%, #e3e6ea 37%, #eef0f3 63%);
  background-size: 400% 100%;
  animation: shimmer 1.4s ease infinite;
}

@keyframes shimmer {
  0% {
    background-position: 100% 0;
  }
  100% {
    background-position: 0 0;
  }
}

.cartbar {
  position: fixed;
  bottom: 20px;
  left: 20px;
  right: 20px;
  margin: 0 auto;
  max-width: 350px;
  background: var(--encre);
  color: white;
  border-radius: 16px;
  padding: 16px 22px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-family: var(--font-body);
  font-weight: 600;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.cartbar:hover {
  background-color: rgba(22, 22, 29, 0.85);
}

.cartbar .amt {
  font-family: var(--font-mono);
  font-weight: 700;
  font-size: 15px;
  color: var(--agrume);
}
</style>
