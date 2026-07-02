<template>
  <Transition name="modal">
    <div v-if="ui.confirmOpen" class="overlay" @click.self="ui.resolveConfirm(false)">
      <div class="box">
        <p class="msg">{{ ui.confirmMessage }}</p>
        <div class="actions">
          <button class="btn-cancel" @click="ui.resolveConfirm(false)">Annuler</button>
          <button class="btn-ok" @click="ui.resolveConfirm(true)">{{ ui.confirmLabel }}</button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
// Boîte de confirmation globale, pilotée par le store ui (remplace le confirm() natif du navigateur).
import { useUiStore } from '@/stores/ui'

const ui = useUiStore()
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(22, 22, 29, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  z-index: 1000;
}

.box {
  background: white;
  border-radius: 20px;
  padding: 26px 24px 20px;
  width: 100%;
  max-width: 380px;
  box-shadow: 0 30px 80px rgba(22, 22, 29, 0.3);
}

.msg {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 18px;
  color: var(--encre);
  margin: 0 0 22px;
  line-height: 1.35;
}

.actions {
  display: flex;
  gap: 10px;
}

.btn-cancel,
.btn-ok {
  flex: 1;
  padding: 13px;
  border-radius: 12px;
  font-family: var(--font-body);
  font-weight: 600;
  font-size: 15px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-cancel {
  background: var(--glacon);
  color: var(--encre);
  border: 1px solid rgba(22, 22, 29, 0.1);
}

.btn-cancel:hover {
  background: rgba(22, 22, 29, 0.06);
}

.btn-ok {
  background: var(--spritz);
  color: white;
  border: none;
}

.btn-ok:hover {
  background: #e63d1f;
}

.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.2s;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
</style>
