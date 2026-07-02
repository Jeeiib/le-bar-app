<template>
  <div class="screen">
    <!-- Hero image -->
    <div class="hero" :style="heroStyle">
      <button class="icon-btn" @click="goBack">←</button>
      <div class="badge">
        <span class="badge-dot"></span>
        {{ cocktail?.categoryName || 'Catégorie' }}
      </div>
    </div>

    <!-- Contenu -->
    <div class="container">
      <h2 class="title">{{ cocktail?.name }}</h2>
      <p v-if="cocktail?.description" class="description">{{ cocktail.description }}</p>

      <!-- Ingrédients -->
      <label class="label-in">Ingrédients</label>
      <div class="ingredients">
        <span v-for="(ing, idx) in cocktail?.ingredients" :key="idx" class="ingredient-chip">
          {{ ing.name }} <span v-if="ing.measure" class="measure">{{ ing.measure }}</span>
        </span>
      </div>

      <!-- Sélecteur de taille -->
      <label class="label-in">Taille</label>
      <div class="size-selector">
        <button
          v-for="size in sortedSizes"
          :key="size.size"
          class="size-opt"
          :class="{ on: selectedSize === size.size }"
          @click="selectedSize = size.size"
        >
          <div class="sz">{{ size.size }}</div>
          <div class="cl">{{ sizeCl(size.size) }}</div>
          <div class="pr">{{ size.price.toFixed(2) }} €</div>
        </button>
      </div>

      <!-- Bouton ajouter -->
      <button class="btn" @click="addToCart">
        Ajouter au panier
        <span class="amt">· {{ selectedPrice.toFixed(2) }} €</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
// Fiche détaillée d'un cocktail : ingrédients, choix de la taille (avec prix) et ajout au panier.
import { onMounted, ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { useMenuStore } from '@/stores/menu'
import { sizeCl } from '@/utils/sizes'
import * as api from '@/api/client'
import type { Cocktail } from '@/types'

const router = useRouter()
const route = useRoute()
const cartStore = useCartStore()
const menu = useMenuStore()

const cocktail = ref<Cocktail | null>(null)
const selectedSize = ref<'S' | 'M' | 'L'>('M')

onMounted(async () => {
  const id = parseInt(route.params.id as string)

  // Cache du menu d'abord (affichage instantané), sinon requête directe
  const cached = menu.getById(id)
  if (cached) {
    cocktail.value = cached
  } else {
    try {
      cocktail.value = await api.getCocktail(id)
    } catch (error) {
      console.error('Erreur lors du chargement du cocktail:', error)
    }
  }

  if (cocktail.value && cocktail.value.sizes.length > 0) {
    const defaultSize = cocktail.value.sizes.find((s) => s.size === 'M')
    selectedSize.value = defaultSize ? 'M' : cocktail.value.sizes[0]?.size || 'M'
  }
})

const selectedPrice = computed(() => {
  return cocktail.value?.sizes.find((s) => s.size === selectedSize.value)?.price ?? 0
})

// Tailles triées dans l'ordre S, M, L
const sortedSizes = computed(() => {
  const order = ['S', 'M', 'L']
  return [...(cocktail.value?.sizes ?? [])].sort(
    (a, b) => order.indexOf(a.size) - order.indexOf(b.size)
  )
})

// Photo du cocktail en fond du hero
const heroStyle = computed(() => ({
  backgroundImage: cocktail.value ? `url(${cocktail.value.imageUrl})` : 'none',
}))

const goBack = () => {
  router.push('/carte')
}

const addToCart = () => {
  if (cocktail.value) {
    cartStore.add(cocktail.value, selectedSize.value, selectedPrice.value)
    router.push('/carte')
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

.hero {
  height: 230px;
  position: relative;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 18px;
  background-color: var(--encre);
  background-size: cover;
  background-position: center;
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
  background-color: rgba(255, 255, 255, 0.8);
}

.badge {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  border-radius: 999px;
  padding: 6px 13px;
  font-family: var(--font-body);
  font-weight: 600;
  font-size: 13px;
  background: rgba(255, 255, 255, 0.9);
  color: var(--spritz);
}

.badge-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--spritz);
  display: inline-block;
}

.container {
  padding: 22px 24px 24px;
  flex: 1;
}

.title {
  font-family: var(--font-display);
  font-weight: 900;
  font-size: 36px;
  line-height: 0.95;
  letter-spacing: -0.01em;
  margin: 0 0 8px;
}

.description {
  font-size: 15px;
  color: rgba(22, 22, 29, 0.6);
  margin: 0 0 20px;
  line-height: 1.5;
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

.ingredients {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 22px;
}

.ingredient-chip {
  font-size: 13px;
  background: white;
  border: 1px solid rgba(22, 22, 29, 0.1);
  border-radius: 999px;
  padding: 7px 12px;
  display: inline-block;
}

.ingredient-chip .measure {
  font-family: var(--font-mono);
  font-size: 11px;
  color: rgba(22, 22, 29, 0.45);
  margin-left: 4px;
}

.size-selector {
  display: flex;
  gap: 10px;
  margin-bottom: 24px;
}

.size-opt {
  flex: 1;
  border: 1.5px solid rgba(22, 22, 29, 0.1);
  border-radius: 14px;
  padding: 12px 8px;
  text-align: center;
  background: white;
  cursor: pointer;
  transition: all 0.2s;
}

.size-opt.on {
  border-color: var(--spritz);
  background: rgba(255, 77, 46, 0.06);
}

.size-opt .sz {
  font-family: var(--font-display);
  font-weight: 800;
  font-size: 18px;
}

.size-opt .cl {
  font-family: var(--font-mono);
  font-size: 10px;
  color: rgba(22, 22, 29, 0.4);
  margin-top: 2px;
}

.size-opt .pr {
  font-family: var(--font-mono);
  font-size: 12px;
  color: rgba(22, 22, 29, 0.6);
  margin-top: 3px;
}

.size-opt.on .cl,
.size-opt.on .pr {
  color: var(--spritz);
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

.btn:hover {
  background-color: #e63d1f;
}

.btn .amt {
  font-family: var(--font-mono);
  font-weight: 700;
}
</style>
