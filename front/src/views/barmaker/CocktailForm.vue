<template>
  <div class="form-overlay">
    <div class="modal">
      <h3>{{ isEdit ? 'Éditer' : 'Nouveau' }} cocktail</h3>
      <p class="msub">Renseigne les infos, les ingrédients et un prix par taille.</p>

      <form @submit.prevent="handleSubmit">
        <!-- Nom et Catégorie -->
        <div class="frow">
          <div class="fgroup">
            <label class="label">Nom</label>
            <input v-model="form.name" type="text" class="field" placeholder="Mojito" />
          </div>
          <div class="fgroup">
            <label class="label">Catégorie</label>
            <select v-model.number="form.categoryId" class="field">
              <option
                v-for="cat in categories"
                :key="cat.id"
                :value="cat.id"
              >
                {{ cat.name }}
              </option>
            </select>
          </div>
        </div>

        <!-- Description -->
        <div class="fgroup">
          <label class="label">Description</label>
          <textarea
            v-model="form.description"
            class="field"
            rows="3"
            placeholder="Le grand classique cubain..."
          ></textarea>
        </div>

        <!-- Recherche externe -->
        <div class="fgroup">
          <label class="label">Ou rechercher sur TheCocktailDB</label>
          <div style="display: flex; gap: 8px">
            <input
              v-model="externalSearchQuery"
              type="text"
              class="field"
              placeholder="Ex: Mojito"
              @keyup.enter="searchExternal"
            />
            <button
              type="button"
              @click="searchExternal"
              class="btn-search"
              :disabled="isSearching || !externalSearchQuery"
            >
              {{ isSearching ? '...' : 'Chercher' }}
            </button>
          </div>

          <!-- Résultats recherche -->
          <div v-if="externalResults.length > 0" class="external-results">
            <p class="results-label">Sélectionne un résultat pour préremplir :</p>
            <div v-for="result in externalResults" :key="result.name" class="result-item">
              <button
                type="button"
                @click="selectExternalCocktail(result)"
                class="result-btn"
              >
                <strong>{{ result.name }}</strong>
                <br />
                <small>{{ result.instructions }}</small>
              </button>
            </div>
          </div>
        </div>

        <!-- Ingrédients -->
        <div class="fgroup">
          <label class="label">Ingrédients</label>
          <div class="ingredients-list">
            <div v-for="(ing, idx) in form.ingredients" :key="idx" class="ingredient-row">
              <input
                v-model="ing.name"
                type="text"
                class="field"
                placeholder="Nom"
              />
              <input
                v-model="ing.measure"
                type="text"
                class="field"
                placeholder="Mesure (opt.)"
              />
              <button
                type="button"
                @click="form.ingredients.splice(idx, 1)"
                class="btn-remove"
              >
                ×
              </button>
            </div>
          </div>
          <button
            type="button"
            @click="form.ingredients.push({ name: '', measure: null })"
            class="btn-add-ingredient"
          >
            + Ajouter ingrédient
          </button>
        </div>

        <!-- Tailles et prix -->
        <div class="fgroup">
          <label class="label">Tailles & prix</label>
          <div class="pricerows">
            <div v-for="size in form.sizes" :key="size.size" class="pricerow">
              <span class="psz">{{ size.size }} · {{ sizeCl(size.size) }}</span>
              <div class="pin">
                <input
                  v-model.number="size.price"
                  type="number"
                  step="0.01"
                  placeholder="0.00"
                />
                <span>€</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Boutons -->
        <div class="mfoot">
          <button type="button" @click="goBack" class="btn ghost">
            Annuler
          </button>
          <button type="submit" class="btn">
            {{ isLoading ? 'Enregistrement...' : 'Enregistrer' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
// Formulaire de création et d'édition d'un cocktail : saisie manuelle ou préremplissage via une
// recherche sur TheCocktailDB, avec les ingrédients et un prix par taille.
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import * as api from '@/api/client'
import { sizeCl } from '@/utils/sizes'
import { useMenuStore } from '@/stores/menu'
import { useUiStore } from '@/stores/ui'
import type { Category, CocktailRequest, ExternalCocktail } from '@/types'

const router = useRouter()
const route = useRoute()
const menu = useMenuStore()
const ui = useUiStore()

const categories = ref<Category[]>([])
const isLoading = ref(false)
const isSearching = ref(false)
const externalSearchQuery = ref('')
const externalResults = ref<ExternalCocktail[]>([])

const isEdit = computed(() => !!route.params.id)
const cocktailId = computed(() => {
  const id = route.params.id
  if (typeof id === 'string') return parseInt(id)
  if (Array.isArray(id) && id[0]) return parseInt(id[0])
  return undefined
})

const form = ref<CocktailRequest>({
  name: '',
  description: null,
  available: true,
  categoryId: 1,
  ingredients: [{ name: '', measure: null }],
  sizes: [
    { size: 'S', price: 0 },
    { size: 'M', price: 0 },
    { size: 'L', price: 0 },
  ],
  imageSourceUrl: undefined,
})

const loadCategories = async () => {
  try {
    const cats = await api.getCategories()
    categories.value = cats
    if (cats && cats.length > 0 && form.value.categoryId === 1) {
      form.value.categoryId = cats[0]?.id ?? 1
    }
  } catch (error) {
    console.error('Erreur chargement catégories:', error)
  }
}

const loadCocktail = async () => {
  if (!cocktailId.value) return
  try {
    const cocktail = await api.getCocktail(cocktailId.value)
    form.value = {
      name: cocktail.name,
      description: cocktail.description,
      available: cocktail.available,
      categoryId: cocktail.categoryId,
      ingredients: cocktail.ingredients,
      sizes: cocktail.sizes,
      imageSourceUrl: cocktail.imageUrl,
    }
  } catch (error) {
    console.error('Erreur chargement cocktail:', error)
  }
}

const searchExternal = async () => {
  if (!externalSearchQuery.value.trim()) return
  isSearching.value = true
  externalResults.value = []
  try {
    externalResults.value = await api.searchExternal(externalSearchQuery.value)
  } catch (error) {
    console.error('Erreur recherche:', error)
  } finally {
    isSearching.value = false
  }
}

const selectExternalCocktail = (cocktail: ExternalCocktail) => {
  form.value.name = cocktail.name
  form.value.description = cocktail.instructions ?? null
  form.value.ingredients = cocktail.ingredients
  form.value.imageSourceUrl = cocktail.imageUrl ?? undefined
  externalResults.value = []
  externalSearchQuery.value = ''
}

const handleSubmit = async () => {
  if (!form.value.name.trim()) {
    ui.toast('Le nom est requis', 'error')
    return
  }

  isLoading.value = true
  try {
    const saved =
      isEdit.value && cocktailId.value
        ? await api.updateCocktail(cocktailId.value, form.value)
        : await api.createCocktail(form.value)
    menu.upsertLocal(saved) // cache mis à jour instantanément (pas de rechargement)
    ui.toast(isEdit.value ? 'Cocktail modifié' : 'Cocktail ajouté', 'success')
    router.push('/barmaker/carte')
  } catch (error) {
    ui.toast(error instanceof Error ? error.message : 'Erreur lors de l\'enregistrement', 'error')
  } finally {
    isLoading.value = false
  }
}

const goBack = () => {
  router.push('/barmaker/carte')
}

onMounted(() => {
  loadCategories()
  if (isEdit.value) {
    loadCocktail()
  }
})
</script>

<style scoped>
.form-overlay {
  background: rgba(22, 22, 29, 0.5);
  padding: 40px;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
}

.modal {
  width: 560px;
  background: white;
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 30px 80px rgba(0, 0, 0, 0.3);
  max-height: 90vh;
  overflow-y: auto;
}

.modal h3 {
  font-family: var(--font-display);
  font-weight: 900;
  font-size: 26px;
  margin: 0 0 4px 0;
}

.msub {
  color: rgba(22, 22, 29, 0.6);
  font-size: 14px;
  margin: 0 0 22px 0;
}

.frow {
  display: flex;
  gap: 14px;
}

.frow > div {
  flex: 1;
}

.fgroup {
  margin-bottom: 16px;
}

.label {
  font-family: var(--font-mono);
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: rgba(22, 22, 29, 0.45);
  display: block;
  margin-bottom: 7px;
}

.field {
  display: block;
  width: 100%;
  background: white;
  border: 1px solid rgba(22, 22, 29, 0.1);
  border-radius: 12px;
  padding: 14px 16px;
  font-family: var(--font-body);
  font-size: 15px;
  color: var(--encre);
  box-sizing: border-box;
}

textarea.field {
  resize: vertical;
  font-family: var(--font-body);
}

.field::placeholder {
  color: rgba(22, 22, 29, 0.45);
}

.field:focus {
  outline: none;
  border-color: var(--spritz);
  box-shadow: 0 0 0 3px rgba(255, 77, 46, 0.1);
}

.btn-search {
  background: var(--spritz);
  color: white;
  border: none;
  border-radius: 12px;
  padding: 14px 16px;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  white-space: nowrap;
  transition: background-color 0.2s;
}

.btn-search:hover:not(:disabled) {
  background-color: #e63d1f;
}

.btn-search:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.external-results {
  margin-top: 12px;
  padding: 12px;
  background: var(--glacon);
  border-radius: 12px;
  max-height: 200px;
  overflow-y: auto;
}

.results-label {
  font-size: 12px;
  color: rgba(22, 22, 29, 0.6);
  margin: 0 0 8px 0;
}

.result-item {
  margin-bottom: 8px;
}

.result-btn {
  width: 100%;
  background: white;
  border: 1px solid rgba(22, 22, 29, 0.1);
  border-radius: 8px;
  padding: 8px 12px;
  text-align: left;
  cursor: pointer;
  font-family: var(--font-body);
  font-size: 13px;
  transition: all 0.2s;
}

.result-btn:hover {
  border-color: var(--spritz);
  background: rgba(255, 77, 46, 0.05);
}

.result-btn strong {
  display: block;
  color: var(--encre);
}

.result-btn small {
  display: block;
  color: rgba(22, 22, 29, 0.6);
  margin-top: 4px;
  line-height: 1.3;
}

.ingredients-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 12px;
}

.ingredient-row {
  display: flex;
  gap: 8px;
  align-items: center;
}

.ingredient-row .field {
  flex: 1;
}

.btn-remove {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: 1px solid rgba(22, 22, 29, 0.1);
  background: white;
  font-size: 18px;
  color: var(--spritz);
  cursor: pointer;
  transition: all 0.2s;
}

.btn-remove:hover {
  background: rgba(255, 77, 46, 0.1);
  border-color: var(--spritz);
}

.btn-add-ingredient {
  background: none;
  border: 1px dashed rgba(22, 22, 29, 0.3);
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 13px;
  color: rgba(22, 22, 29, 0.6);
  cursor: pointer;
  transition: all 0.2s;
}

.btn-add-ingredient:hover {
  border-color: var(--spritz);
  color: var(--spritz);
}

.pricerows {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.pricerow {
  display: flex;
  align-items: center;
  gap: 12px;
  background: var(--glacon);
  border: 1px solid rgba(22, 22, 29, 0.1);
  border-radius: 12px;
  padding: 10px 14px;
}

.psz {
  font-family: var(--font-display);
  font-weight: 800;
  font-size: 15px;
  white-space: nowrap;
  flex-shrink: 0;
  min-width: 68px;
  color: var(--encre);
}

.pin {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 6px;
  background: white;
  border: 1px solid rgba(22, 22, 29, 0.1);
  border-radius: 9px;
  padding: 8px 12px;
  font-family: var(--font-mono);
}

.pin input {
  flex: 1;
  border: none;
  background: transparent;
  font-family: var(--font-mono);
  font-size: 14px;
  padding: 0;
}

.pin input:focus {
  outline: none;
}

.pin span {
  font-family: var(--font-mono);
  font-size: 12px;
  color: rgba(22, 22, 29, 0.6);
}

.mfoot {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.btn {
  flex: 1;
  background: var(--spritz);
  color: white;
  border: none;
  border-radius: 14px;
  padding: 16px;
  font-family: var(--font-body);
  font-weight: 600;
  font-size: 16px;
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

.btn.ghost {
  background: white;
  color: var(--encre);
  border: 1px solid rgba(22, 22, 29, 0.1);
}

.btn.ghost:hover {
  background: rgba(22, 22, 29, 0.05);
}

/* Mobile : modale pleine largeur, champs nom/catégorie empilés */
@media (max-width: 640px) {
  .form-overlay {
    padding: 16px;
    align-items: flex-start;
  }

  .modal {
    width: 100%;
    padding: 22px 18px;
  }

  .frow {
    flex-direction: column;
    gap: 0;
  }
}
</style>
