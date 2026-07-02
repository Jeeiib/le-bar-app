import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ToastStack from '@/components/ToastStack.vue'
import { useUiStore } from '@/stores/ui'

describe('ToastStack component', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.runOnlyPendingTimers()
    vi.useRealTimers()
  })

  it('doit monter correctement', () => {
    const wrapper = mount(ToastStack, {
      global: {
        plugins: [createPinia()],
      },
    })
    expect(wrapper.exists()).toBe(true)
  })

  it('ne doit pas afficher de toast quand la liste est vide', () => {
    const wrapper = mount(ToastStack, {
      global: {
        plugins: [createPinia()],
      },
    })

    const toasts = wrapper.findAll('.toast')
    expect(toasts).toHaveLength(0)
  })

  it('doit afficher un toast success', async () => {
    const wrapper = mount(ToastStack, {
      global: {
        plugins: [createPinia()],
      },
    })

    const uiStore = useUiStore()
    uiStore.toast('Succès !', 'success')
    await wrapper.vm.$nextTick()

    const toasts = wrapper.findAll('.toast')
    expect(toasts).toHaveLength(1)
    expect(toasts[0]!.text()).toBe('Succès !')
    expect(toasts[0]!.classes()).toContain('success')
  })

  it('doit afficher un toast error', async () => {
    const wrapper = mount(ToastStack, {
      global: {
        plugins: [createPinia()],
      },
    })

    const uiStore = useUiStore()
    uiStore.toast('Erreur !', 'error')
    await wrapper.vm.$nextTick()

    const toasts = wrapper.findAll('.toast')
    expect(toasts).toHaveLength(1)
    expect(toasts[0]!.classes()).toContain('error')
  })

  it('doit afficher un toast info', async () => {
    const wrapper = mount(ToastStack, {
      global: {
        plugins: [createPinia()],
      },
    })

    const uiStore = useUiStore()
    uiStore.toast('Info', 'info')
    await wrapper.vm.$nextTick()

    const toasts = wrapper.findAll('.toast')
    expect(toasts).toHaveLength(1)
    expect(toasts[0]!.classes()).toContain('info')
  })

  it('doit afficher plusieurs toasts', async () => {
    const wrapper = mount(ToastStack, {
      global: {
        plugins: [createPinia()],
      },
    })

    const uiStore = useUiStore()
    uiStore.toast('Toast 1', 'success')
    uiStore.toast('Toast 2', 'error')
    uiStore.toast('Toast 3', 'info')
    await wrapper.vm.$nextTick()

    const toasts = wrapper.findAll('.toast')
    expect(toasts).toHaveLength(3)
    expect(toasts[0]!.text()).toBe('Toast 1')
    expect(toasts[1]!.text()).toBe('Toast 2')
    expect(toasts[2]!.text()).toBe('Toast 3')
  })

  it('doit avoir la structure DOM correcte', async () => {
    const wrapper = mount(ToastStack, {
      global: {
        plugins: [createPinia()],
      },
    })

    const stack = wrapper.find('.toast-stack')
    expect(stack.exists()).toBe(true)
    expect(stack.classes()).toContain('toast-stack')
  })

  it('doit afficher le message correct du toast', async () => {
    const wrapper = mount(ToastStack, {
      global: {
        plugins: [createPinia()],
      },
    })

    const uiStore = useUiStore()
    uiStore.toast('Message personnalisé', 'info')
    await wrapper.vm.$nextTick()

    const toast = wrapper.find('.toast')
    expect(toast.text()).toBe('Message personnalisé')
  })

  it('doit suppri les toasts quand ils expirent', async () => {
    const wrapper = mount(ToastStack, {
      global: {
        plugins: [createPinia()],
      },
    })

    const uiStore = useUiStore()
    uiStore.toast('Temporaire', 'info')
    await wrapper.vm.$nextTick()

    expect(wrapper.findAll('.toast')).toHaveLength(1)

    vi.advanceTimersByTime(3500)
    await wrapper.vm.$nextTick()

    expect(wrapper.findAll('.toast')).toHaveLength(0)
  })

  it('doit gérer l\'ajout et la suppression de plusieurs toasts', async () => {
    const wrapper = mount(ToastStack, {
      global: {
        plugins: [createPinia()],
      },
    })

    const uiStore = useUiStore()
    uiStore.toast('Toast 1', 'success')
    uiStore.toast('Toast 2', 'error')
    await wrapper.vm.$nextTick()

    expect(wrapper.findAll('.toast')).toHaveLength(2)

    vi.advanceTimersByTime(3500)
    await wrapper.vm.$nextTick()

    expect(wrapper.findAll('.toast')).toHaveLength(0)
  })

  it('doit assigner une clé unique à chaque toast', async () => {
    const wrapper = mount(ToastStack, {
      global: {
        plugins: [createPinia()],
      },
    })

    const uiStore = useUiStore()
    uiStore.toast('Toast 1')
    uiStore.toast('Toast 2')
    await wrapper.vm.$nextTick()

    const toasts = wrapper.findAll('.toast')
    expect(toasts).toHaveLength(2)
    expect(toasts[0]!.element).toBeTruthy()
    expect(toasts[1]!.element).toBeTruthy()
  })

  it('doit appliquer les classes de type aux toasts', async () => {
    const wrapper = mount(ToastStack, {
      global: {
        plugins: [createPinia()],
      },
    })

    const uiStore = useUiStore()
    uiStore.toast('Success', 'success')
    uiStore.toast('Error', 'error')
    uiStore.toast('Info', 'info')
    await wrapper.vm.$nextTick()

    const toasts = wrapper.findAll('.toast')
    expect(toasts[0]!.classes()).toContain('success')
    expect(toasts[1]!.classes()).toContain('error')
    expect(toasts[2]!.classes()).toContain('info')
  })

  it('doit gérer l\'affichage et la disparition progressive des toasts', async () => {
    const wrapper = mount(ToastStack, {
      global: {
        plugins: [createPinia()],
      },
    })

    const uiStore = useUiStore()
    uiStore.toast('Toast 1')

    vi.advanceTimersByTime(1000)

    uiStore.toast('Toast 2')
    await wrapper.vm.$nextTick()

    expect(wrapper.findAll('.toast')).toHaveLength(2)

    vi.advanceTimersByTime(2500)
    await wrapper.vm.$nextTick()

    expect(wrapper.findAll('.toast')).toHaveLength(1)

    vi.advanceTimersByTime(1000)
    await wrapper.vm.$nextTick()

    expect(wrapper.findAll('.toast')).toHaveLength(0)
  })
})
