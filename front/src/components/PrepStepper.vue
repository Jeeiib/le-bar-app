<template>
  <div class="stepper">
    <div
      class="step"
      :class="{
        done: currentStep > 0,
        cur: currentStep === 0,
      }"
    >
      <span class="dot">1</span>
      <span class="lab">Ingrédients</span>
    </div>
    <div
      class="step"
      :class="{
        done: currentStep > 1,
        cur: currentStep === 1,
      }"
    >
      <span class="dot">2</span>
      <span class="lab">Assemblage</span>
    </div>
    <div
      class="step"
      :class="{
        done: currentStep > 2,
        cur: currentStep === 2,
      }"
    >
      <span class="dot">3</span>
      <span class="lab">Dressage</span>
    </div>
    <div
      class="step"
      :class="{
        done: currentStep === 3,
        cur: currentStep === 3,
      }"
    >
      <span class="dot">4</span>
      <span class="lab">Terminé</span>
    </div>
  </div>
</template>

<script setup lang="ts">
// Frise des 4 étapes de préparation d'un cocktail (ingrédients, assemblage, dressage, terminé) ;
// met en avant l'étape en cours. Réutilisée côté client (suivi) et côté barmaker.
import { computed } from 'vue'
import type { PrepStatus } from '@/types'

interface Props {
  status: PrepStatus
}

const props = defineProps<Props>()

const stepIndexMap: Record<PrepStatus, number> = {
  PREPARATION_INGREDIENTS: 0,
  ASSEMBLAGE: 1,
  DRESSAGE: 2,
  TERMINEE: 3,
}

// computed pour rester reactif quand le statut de l'item change
const currentStep = computed(() => stepIndexMap[props.status] ?? 0)
</script>

<style scoped>
.stepper {
  display: flex;
}

.stepper .step {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 7px;
  position: relative;
}

.stepper .step::before {
  content: '';
  position: absolute;
  top: 16px;
  right: 50%;
  width: 100%;
  height: 3px;
  background: rgba(22, 22, 29, 0.1);
  z-index: 0;
}

.stepper .step:first-child::before {
  display: none;
}

.stepper .step.done::before,
.stepper .step.cur::before {
  background: var(--spritz);
}

.stepper .dot {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: white;
  border: 2px solid rgba(22, 22, 29, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-mono);
  font-weight: 700;
  font-size: 13px;
  color: rgba(22, 22, 29, 0.45);
  position: relative;
  z-index: 1;
  transition: all 0.2s;
}

.stepper .step.done .dot {
  background: var(--spritz);
  border-color: var(--spritz);
  color: white;
}

.stepper .step.cur .dot {
  border-color: var(--spritz);
  color: var(--spritz);
  box-shadow: 0 0 0 4px rgba(255, 77, 46, 0.16);
}

.stepper .lab {
  font-size: 10.5px;
  font-weight: 600;
  color: rgba(22, 22, 29, 0.45);
  text-align: center;
  max-width: 64px;
  line-height: 1.2;
}

.stepper .step.done .lab,
.stepper .step.cur .lab {
  color: var(--encre);
}
</style>
