<template>
  <div class="toast-stack">
    <TransitionGroup name="toast">
      <div v-for="t in ui.toasts" :key="t.id" class="toast" :class="t.type">
        {{ t.message }}
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup lang="ts">
import { useUiStore } from '@/stores/ui'

const ui = useUiStore()
</script>

<style scoped>
.toast-stack {
  position: fixed;
  bottom: 24px;
  left: 0;
  right: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  z-index: 1100;
  pointer-events: none;
}

.toast {
  pointer-events: auto;
  max-width: 90vw;
  padding: 13px 20px;
  border-radius: 12px;
  font-family: var(--font-body);
  font-weight: 600;
  font-size: 14px;
  color: white;
  box-shadow: 0 10px 30px rgba(22, 22, 29, 0.25);
}

.toast.error {
  background: var(--spritz);
}

.toast.success {
  background: var(--menthe);
}

.toast.info {
  background: var(--encre);
}

.toast-enter-active,
.toast-leave-active {
  transition: opacity 0.25s, transform 0.25s;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(12px);
}
</style>
