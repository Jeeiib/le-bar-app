<script setup lang="ts">
import { RouterView } from 'vue-router'
import ConfirmModal from '@/components/ConfirmModal.vue'
import ToastStack from '@/components/ToastStack.vue'
</script>

<template>
  <RouterView />
  <ConfirmModal />
  <ToastStack />
</template>
