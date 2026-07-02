<template>
  <div class="login-container">
    <div class="login-wrapper">
      <!-- Panneau sombre gauche -->
      <div class="login-dark">
        <div class="brand">LE BAR'APP</div>
        <div class="content">
          <h1>Espace<br />barmaker.</h1>
          <p>Gère la carte et traite les commandes du comptoir.</p>
        </div>
        <span class="role">Réservé au staff</span>
      </div>

      <!-- Formulaire droite -->
      <div class="login-form">
        <h2>Connexion</h2>

        <!-- Message d'erreur -->
        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>

        <!-- Email -->
        <div class="form-group">
          <label class="label">Email</label>
          <input
            v-model="email"
            type="email"
            class="field"
            placeholder="barmaker@lebarapp.fr"
            @keyup.enter="handleLogin"
          />
        </div>

        <!-- Mot de passe -->
        <div class="form-group">
          <label class="label">Mot de passe</label>
          <input
            v-model="password"
            type="password"
            class="field"
            placeholder="••••••••"
            @keyup.enter="handleLogin"
          />
        </div>

        <!-- Bouton -->
        <button
          @click="handleLogin"
          class="btn-login"
          :disabled="isLoading"
        >
          {{ isLoading ? 'Connexion...' : 'Se connecter' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// Page de connexion du barmaker : envoie les identifiants et, en cas de succès, redirige vers
// la file de commandes.
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const email = ref('')
const password = ref('')
const errorMessage = ref('')
const isLoading = ref(false)

const handleLogin = async () => {
  if (!email.value || !password.value) {
    errorMessage.value = 'Veuillez remplir tous les champs'
    return
  }

  isLoading.value = true
  errorMessage.value = ''

  try {
    await authStore.login(email.value, password.value)
    router.push('/barmaker/commandes')
  } catch (error) {
    const msg = error instanceof Error ? error.message : 'Erreur de connexion'
    errorMessage.value = msg
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--glacon);
}

.login-wrapper {
  display: flex;
  width: 100%;
  max-width: 920px;
  background: white;
  border-radius: 26px;
  box-shadow: 0 30px 80px rgba(22, 22, 29, 0.18);
  overflow: hidden;
}

.login-dark {
  width: 46%;
  background: var(--encre);
  color: white;
  padding: 46px 38px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.brand {
  font-family: var(--font-display);
  font-weight: 900;
  font-size: 23px;
  margin-bottom: 0;
}

.login-dark .content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-dark h1 {
  font-family: var(--font-display);
  font-weight: 900;
  font-size: 38px;
  line-height: 1.02;
  margin: 0 0 14px 0;
  color: white;
}

.login-dark p {
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
  font-size: 15px;
}

.role {
  font-family: var(--font-mono);
  font-size: 11px;
  color: rgba(255, 255, 255, 0.45);
}

.login-form {
  flex: 1;
  padding: 46px 42px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-form h2 {
  font-family: var(--font-display);
  font-weight: 900;
  font-size: 30px;
  margin: 0 0 20px 0;
}

.form-group {
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

.field::placeholder {
  color: rgba(22, 22, 29, 0.45);
}

.field:focus {
  outline: none;
  border-color: var(--spritz);
  box-shadow: 0 0 0 3px rgba(255, 77, 46, 0.1);
}

.btn-login {
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

.btn-login:hover:not(:disabled) {
  background-color: #e63d1f;
}

.btn-login:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.error-message {
  background: rgba(255, 77, 46, 0.1);
  border: 1px solid var(--spritz);
  border-radius: 12px;
  padding: 12px 14px;
  color: var(--spritz);
  font-size: 14px;
  margin-bottom: 16px;
}

/* Mobile : les deux panneaux s'empilent */
@media (max-width: 768px) {
  .login-container {
    padding: 16px;
  }

  .login-wrapper {
    flex-direction: column;
    max-width: 460px;
  }

  .login-dark {
    width: auto;
    padding: 32px 28px;
  }

  .login-dark h1 {
    font-size: 30px;
  }

  .login-form {
    padding: 32px 28px;
  }
}
</style>
