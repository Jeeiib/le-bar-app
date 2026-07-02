<template>
  <BarmakerLayout>
    <!-- En-tête -->
    <div class="carte-header">
      <h2>Ma carte</h2>
      <router-link to="/barmaker/carte/nouveau" class="btn-new">
        + Nouveau cocktail
      </router-link>
    </div>

    <!-- Filtres catégories -->
    <div v-if="categories.length > 0" class="catbar">
      <button
        v-for="cat in categories"
        :key="cat.id"
        @click="selectedCategoryId = cat.id"
        :class="['chip', { on: selectedCategoryId === cat.id }]"
      >
        {{ cat.name }}
      </button>
      <button class="chip" style="border-style: dashed; color: rgba(22, 22, 29, 0.45)">
        + Catégorie
      </button>
    </div>

    <!-- Skeleton au tout premier chargement -->
    <div v-if="!menu.loaded" class="cocktails-list">
      <div v-for="n in 5" :key="n" class="mrow">
        <div class="mthumb skel"></div>
        <div class="minfo">
          <div class="skel-line lg"></div>
          <div class="skel-line"></div>
        </div>
      </div>
    </div>

    <!-- Liste des cocktails -->
    <div v-else-if="filteredCocktails.length > 0" class="cocktails-list">
      <div v-for="cocktail in filteredCocktails" :key="cocktail.id" class="mrow">
        <!-- Vignette -->
        <div
          class="mthumb"
          :style="{ backgroundImage: cocktail.imageUrl ? `url(${cocktail.imageUrl})` : 'none' }"
        ></div>

        <!-- Infos -->
        <div class="minfo">
          <div class="mname">{{ cocktail.name }}</div>
          <div class="ming">
            {{ cocktail.ingredients.map((i) => i.name).join(', ') }}
          </div>
        </div>

        <!-- Prix -->
        <div class="mprices">
          <b v-for="size in cocktail.sizes ?? []" :key="size.size">
            {{ size.size }} · {{ sizeCl(size.size) }} · {{ formatPrice(size.price) }}
          </b>
        </div>

        <!-- Actions -->
        <div class="macts">
          <router-link :to="`/barmaker/carte/${cocktail.id}/edit`" class="btn-act" title="Éditer">
            <Pencil :size="16" />
          </router-link>
          <button @click="deleteCocktail(cocktail.id)" class="btn-act" title="Supprimer">
            <Trash2 :size="16" />
          </button>
        </div>
      </div>
    </div>

    <!-- Vide -->
    <div v-else class="empty-state">
      <p>Aucun cocktail dans cette catégorie.</p>
    </div>
  </BarmakerLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import * as api from '@/api/client'
import { useMenuStore } from '@/stores/menu'
import { useUiStore } from '@/stores/ui'
import BarmakerLayout from '@/components/BarmakerLayout.vue'
import { Pencil, Trash2 } from 'lucide-vue-next'
import { sizeCl } from '@/utils/sizes'

const menu = useMenuStore()
const ui = useUiStore()
const selectedCategoryId = ref<number | null>(null)

const categories = computed(() => menu.categories)

const filteredCocktails = computed(() => {
  if (selectedCategoryId.value === null) {
    return menu.cocktails
  }
  return menu.cocktails.filter((c) => c.categoryId === selectedCategoryId.value)
})

const formatPrice = (price: number | undefined): string => {
  if (price === undefined) return 'N/A'
  return `${price.toFixed(2).replace('.', ',')} €`
}

const deleteCocktail = async (id: number) => {
  const ok = await ui.confirm('Supprimer ce cocktail ?', 'Supprimer')
  if (!ok) {
    return
  }
  // Retrait instantané du cache, synchro BDD derrière (rollback si échec)
  const snapshot = menu.removeLocal(id)
  ui.toast('Cocktail supprimé', 'success')
  try {
    await api.deleteCocktail(id)
  } catch (error) {
    menu.restoreLocal(snapshot)
    ui.toast(error instanceof Error ? error.message : 'Suppression impossible', 'error')
  }
}

onMounted(async () => {
  // Cache partagé : instantané si déjà chargé (retour depuis une fiche/commande)
  await menu.load()
  const first = menu.categories[0]
  if (selectedCategoryId.value === null && first) {
    selectedCategoryId.value = first.id
  }
})
</script>

<style scoped>
.carte-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 22px;
}

.carte-header h2 {
  font-family: var(--font-display);
  font-weight: 900;
  font-size: 32px;
  letter-spacing: -0.01em;
  margin: 0;
}

.btn-new {
  background: var(--spritz);
  color: white;
  border: none;
  border-radius: 14px;
  padding: 12px 18px;
  font-family: var(--font-body);
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  text-decoration: none;
  display: inline-block;
  transition: background-color 0.2s;
}

.btn-new:hover {
  background-color: #e63d1f;
}

.catbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
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

.chip:hover {
  background: rgba(22, 22, 29, 0.05);
}

.chip.on {
  background: var(--spritz);
  border-color: var(--spritz);
  color: white;
}

.cocktails-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.mrow {
  display: flex;
  align-items: center;
  gap: 16px;
  background: white;
  border-radius: 14px;
  padding: 14px 16px;
  box-shadow: 0 3px 10px rgba(22, 22, 29, 0.05);
}

.mthumb {
  width: 54px;
  height: 54px;
  border-radius: 12px;
  flex-shrink: 0;
  background-size: cover;
  background-position: center;
  background-color: var(--glacon);
}

.minfo {
  flex: 1;
}

.mname {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 16px;
  color: var(--encre);
}

.ming {
  font-size: 12.5px;
  color: rgba(22, 22, 29, 0.6);
  margin-top: 3px;
}

.mprices {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

.mprices b {
  font-family: var(--font-mono);
  font-weight: 700;
  font-size: 12px;
  background: var(--glacon);
  border: 1px solid rgba(22, 22, 29, 0.1);
  border-radius: 8px;
  padding: 5px 8px;
  color: var(--encre);
}

.macts {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.btn-act {
  width: 34px;
  height: 34px;
  border-radius: 9px;
  border: 1px solid rgba(22, 22, 29, 0.1);
  background: white;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  color: var(--encre);
}

.btn-act:hover {
  background: rgba(22, 22, 29, 0.05);
  border-color: var(--spritz);
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

/* Skeleton de chargement */
.skel-line {
  height: 12px;
  border-radius: 6px;
  margin-bottom: 8px;
}

.skel-line.lg {
  width: 45%;
  height: 15px;
}

.mthumb.skel,
.skel-line {
  background-image: linear-gradient(90deg, #eef0f3 25%, #e3e6ea 37%, #eef0f3 63%);
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

/* Mobile : la ligne se réorganise (prix sur une 2e ligne) */
@media (max-width: 640px) {
  .carte-header h2 {
    font-size: 26px;
  }

  .mrow {
    flex-wrap: wrap;
    row-gap: 12px;
  }

  .minfo {
    flex: 1 1 140px;
  }

  .macts {
    order: 2;
  }

  .mprices {
    order: 3;
    flex-basis: 100%;
  }
}
</style>
