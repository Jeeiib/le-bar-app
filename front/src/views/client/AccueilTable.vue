<template>
  <div class="screen">
    <!-- Hero avec couleur Spritz -->
    <div class="hero">
      <span class="pill">{{ tableStore.current?.label }}</span>
      <h1 class="brand">LE BAR'APP</h1>
    </div>

    <!-- Contenu principal -->
    <div class="container">
      <h2 class="title">Bienvenue !</h2>
      <p class="subtitle">
        Tu es à la <strong>{{ tableStore.current?.label }}</strong
        >. Commande directement d'ici, sans compte.
      </p>

      <div class="form-group">
        <label class="label-in">Ton prénom (optionnel)</label>
        <input
          v-model="tableStore.customerName"
          class="field"
          placeholder="Léa"
        />
      </div>

      <button class="btn" @click="goToCarte">Voir la carte</button>

      <p class="footnote">Sans compte · commande à ta table</p>
    </div>

    <!-- Message d'erreur si table non trouvée -->
    <div v-if="error" class="error-box">
      <p>{{ error }}</p>
      <p style="margin-top: 8px; font-size: 13px; color: rgba(22, 22, 29, 0.6)">
        Retente d'accéder via un QR code valide.
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useTableStore } from '@/stores/table'
import { useRouter, useRoute } from 'vue-router'

const tableStore = useTableStore()
const router = useRouter()
const route = useRoute()
const error = ref<string>('')

onMounted(async () => {
  const qrSlug = route.params.qrSlug as string
  try {
    await tableStore.load(qrSlug)
  } catch (err) {
    error.value =
      err instanceof Error ? err.message : 'Table introuvable'
  }
})

const goToCarte = () => {
  router.push('/carte')
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
  background-color: var(--spritz);
  padding: 22px;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: 10px;
  height: 170px;
}

.pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.92);
  color: var(--encre);
  border-radius: 999px;
  padding: 8px 13px;
  font-family: var(--font-mono);
  font-weight: 700;
  font-size: 12px;
  width: fit-content;
}

.brand {
  color: white;
  font-size: 26px;
  font-family: var(--font-display);
  font-weight: 900;
  margin: 0;
}

.container {
  padding: 28px 24px 30px;
  flex: 1;
}

.title {
  font-family: var(--font-display);
  font-weight: 900;
  font-size: 44px;
  line-height: 0.95;
  letter-spacing: -0.01em;
  margin: 0 0 8px;
}

.subtitle {
  font-size: 15px;
  color: rgba(22, 22, 29, 0.6);
  margin: 0 0 24px;
  line-height: 1.5;
}

.form-group {
  margin-bottom: 8px;
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
  background: white;
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
  margin-top: 8px;
  transition: background-color 0.2s;
}

.btn:hover {
  background-color: #e63d1f;
}

.footnote {
  text-align: center;
  margin-top: 18px;
  font-size: 13px;
  color: rgba(22, 22, 29, 0.45);
}

.error-box {
  background: rgba(255, 77, 46, 0.12);
  border-radius: 12px;
  padding: 16px;
  margin: 16px 24px;
  color: var(--spritz);
  font-size: 14px;
}
</style>
