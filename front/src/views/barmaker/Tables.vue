<template>
  <BarmakerLayout>
    <!-- En-tête -->
    <div class="tables-header">
      <h2>QR Codes des tables</h2>
      <button @click="handlePrint" class="btn-print">
        Imprimer
      </button>
    </div>

    <!-- Skeleton au chargement -->
    <div v-if="loading" class="tables-grid">
      <div v-for="n in 6" :key="n" class="table-card skeleton">
        <div class="skel-title"></div>
        <div class="skel-qr"></div>
        <div class="skel-url"></div>
      </div>
    </div>

    <!-- Grille des tables -->
    <div v-else-if="tables.length > 0" class="tables-grid">
      <div v-for="table in tables" :key="table.id" class="table-card">
        <div class="table-label">{{ table.label }}</div>
        <div class="qr-container">
          <QrcodeVue :value="qrUrl(table.qrSlug)" :size="160" level="M" />
        </div>
        <div class="table-url">{{ qrUrl(table.qrSlug) }}</div>
      </div>
    </div>

    <!-- Vide -->
    <div v-else class="empty-state">
      <p>Aucune table disponible.</p>
    </div>
  </BarmakerLayout>
</template>

<script setup lang="ts">
// Génère les QR codes des tables à imprimer. Chaque QR encode l'URL du menu de la table à partir
// de l'adresse d'accès courante (window.location.origin), pour rester valable quelle que soit
// l'adresse utilisée (LAN, nom de domaine…) plutôt qu'un localhost figé.
import { ref, onMounted } from 'vue'
import * as api from '@/api/client'
import { useUiStore } from '@/stores/ui'
import BarmakerLayout from '@/components/BarmakerLayout.vue'
import QrcodeVue from 'qrcode.vue'
import type { TableInfo } from '@/types'

const ui = useUiStore()
const tables = ref<TableInfo[]>([])
const loading = ref(true)

// Construit l'URL du QR code basée sur l'origin courant
const qrUrl = (slug: string): string => {
  return `${window.location.origin}/t/${slug}`
}

// Lance l'impression
const handlePrint = () => {
  window.print()
}

// Charge les tables au montage du composant
onMounted(async () => {
  loading.value = true
  try {
    tables.value = await api.getTables()
  } catch (error) {
    ui.toast(
      error instanceof Error ? error.message : 'Erreur lors du chargement des tables',
      'error'
    )
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.tables-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
}

.tables-header h2 {
  font-family: var(--font-display);
  font-weight: 900;
  font-size: 32px;
  letter-spacing: -0.01em;
  margin: 0;
}

.btn-print {
  background: var(--spritz);
  color: white;
  border: none;
  border-radius: 14px;
  padding: 12px 18px;
  font-family: var(--font-body);
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-print:hover {
  background-color: #e63d1f;
}

.tables-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.table-card {
  background: white;
  border-radius: 14px;
  padding: 18px;
  box-shadow: 0 3px 10px rgba(22, 22, 29, 0.05);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
  break-inside: avoid;
}

.table-label {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 16px;
  color: var(--encre);
  text-align: center;
}

.qr-container {
  display: flex;
  justify-content: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 10px;
}

.table-url {
  font-family: var(--font-mono);
  font-size: 11px;
  color: rgba(22, 22, 29, 0.6);
  text-align: center;
  word-break: break-all;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: rgba(22, 22, 29, 0.6);
  font-size: 16px;
}

.empty-state p {
  margin: 0;
}

/* Skeleton de chargement */
.table-card.skeleton {
  opacity: 0.7;
}

.skel-title,
.skel-qr,
.skel-url {
  border-radius: 6px;
  background-image: linear-gradient(90deg, #eef0f3 25%, #e3e6ea 37%, #eef0f3 63%);
  background-size: 400% 100%;
  animation: shimmer 1.4s ease infinite;
}

.skel-title {
  width: 80%;
  height: 14px;
}

.skel-qr {
  width: 160px;
  height: 160px;
}

.skel-url {
  width: 100%;
  height: 10px;
}

@keyframes shimmer {
  0% {
    background-position: 100% 0;
  }
  100% {
    background-position: 0 0;
  }
}

/* Impression */
@media print {
  /* Masquer la nav et les boutons */
  .tables-header {
    display: none;
  }

  .btn-print {
    display: none;
  }

  /* Adapter la grille pour l'impression */
  .tables-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
  }

  .table-card {
    page-break-inside: avoid;
    break-inside: avoid;
    box-shadow: 0 1px 3px rgba(22, 22, 29, 0.1);
  }

  /* Fond blanc pour l'impression */
  .table-card {
    background: white;
  }
}

/* Mobile */
@media (max-width: 640px) {
  .tables-header {
    flex-direction: column;
    gap: 14px;
    margin-bottom: 20px;
  }

  .tables-header h2 {
    font-size: 26px;
  }

  .btn-print {
    width: 100%;
  }

  .tables-grid {
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
    gap: 14px;
  }

  @media print {
    .tables-grid {
      grid-template-columns: repeat(2, 1fr);
    }
  }
}
</style>
