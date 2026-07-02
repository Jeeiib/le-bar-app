<script setup lang="ts">
// Composant racine : affiche la page courante (RouterView) et les surcouches globales
// (dialogue de confirmation et notifications).
import { RouterView } from 'vue-router'
import ConfirmModal from '@/components/ConfirmModal.vue'
import ToastStack from '@/components/ToastStack.vue'
</script>

<template>
  <RouterView />
  <ConfirmModal />
  <ToastStack />
</template>
