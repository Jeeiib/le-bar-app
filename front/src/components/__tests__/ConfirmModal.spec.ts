import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ConfirmModal from '@/components/ConfirmModal.vue'
import { useUiStore } from '@/stores/ui'

describe('ConfirmModal component', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('doit monter correctement', () => {
    const wrapper = mount(ConfirmModal, {
      global: {
        plugins: [createPinia()],
      },
    })
    expect(wrapper.exists()).toBe(true)
  })

  it('ne doit pas afficher la modale quand confirmOpen = false', () => {
    const wrapper = mount(ConfirmModal, {
      global: {
        plugins: [createPinia()],
      },
    })

    const overlay = wrapper.find('.overlay')
    expect(overlay.exists()).toBe(false)
  })

  it('doit afficher la modale quand confirmOpen = true', async () => {
    const wrapper = mount(ConfirmModal, {
      global: {
        plugins: [createPinia()],
      },
    })

    const uiStore = useUiStore()
    uiStore.confirm('Test message')
    await wrapper.vm.$nextTick()

    const overlay = wrapper.find('.overlay')
    expect(overlay.exists()).toBe(true)
  })

  it('doit afficher le message correct dans la modale', async () => {
    const wrapper = mount(ConfirmModal, {
      global: {
        plugins: [createPinia()],
      },
    })

    const uiStore = useUiStore()
    uiStore.confirm('Êtes-vous sûr de cette action ?')
    await wrapper.vm.$nextTick()

    const msg = wrapper.find('.msg')
    expect(msg.text()).toBe('Êtes-vous sûr de cette action ?')
  })

  it('doit afficher le label correct sur le bouton OK', async () => {
    const wrapper = mount(ConfirmModal, {
      global: {
        plugins: [createPinia()],
      },
    })

    const uiStore = useUiStore()
    uiStore.confirm('Supprimer ?', 'Oui, supprimer')
    await wrapper.vm.$nextTick()

    const btnOk = wrapper.find('.btn-ok')
    expect(btnOk.text()).toBe('Oui, supprimer')
  })

  it('doit utiliser le label par défaut si non fourni', async () => {
    const wrapper = mount(ConfirmModal, {
      global: {
        plugins: [createPinia()],
      },
    })

    const uiStore = useUiStore()
    uiStore.confirm('Message')
    await wrapper.vm.$nextTick()

    const btnOk = wrapper.find('.btn-ok')
    expect(btnOk.text()).toBe('Confirmer')
  })

  it('doit appeler resolveConfirm(true) quand on clique sur OK', async () => {
    const wrapper = mount(ConfirmModal, {
      global: {
        plugins: [createPinia()],
      },
    })

    const uiStore = useUiStore()
    const promise = uiStore.confirm('Message')
    await wrapper.vm.$nextTick()

    const btnOk = wrapper.find('.btn-ok')
    await btnOk.trigger('click')

    const result = await promise
    expect(result).toBe(true)
  })

  it('doit appeler resolveConfirm(false) quand on clique sur Annuler', async () => {
    const wrapper = mount(ConfirmModal, {
      global: {
        plugins: [createPinia()],
      },
    })

    const uiStore = useUiStore()
    const promise = uiStore.confirm('Message')
    await wrapper.vm.$nextTick()

    const btnCancel = wrapper.find('.btn-cancel')
    await btnCancel.trigger('click')

    const result = await promise
    expect(result).toBe(false)
  })

  it('doit appeler resolveConfirm(false) quand on clique sur l\'overlay', async () => {
    const wrapper = mount(ConfirmModal, {
      global: {
        plugins: [createPinia()],
      },
    })

    const uiStore = useUiStore()
    const promise = uiStore.confirm('Message')
    await wrapper.vm.$nextTick()

    const overlay = wrapper.find('.overlay')
    await overlay.trigger('click.self')

    const result = await promise
    expect(result).toBe(false)
  })

  it('doit avoir la classe overlay correctement', async () => {
    const wrapper = mount(ConfirmModal, {
      global: {
        plugins: [createPinia()],
      },
    })

    const uiStore = useUiStore()
    uiStore.confirm('Message')
    await wrapper.vm.$nextTick()

    const overlay = wrapper.find('.overlay')
    expect(overlay.classes()).toContain('overlay')
  })

  it('doit avoir la boîte modale avec les classes correctes', async () => {
    const wrapper = mount(ConfirmModal, {
      global: {
        plugins: [createPinia()],
      },
    })

    const uiStore = useUiStore()
    uiStore.confirm('Message')
    await wrapper.vm.$nextTick()

    const box = wrapper.find('.box')
    expect(box.classes()).toContain('box')
  })

  it('doit afficher les deux boutons dans les actions', async () => {
    const wrapper = mount(ConfirmModal, {
      global: {
        plugins: [createPinia()],
      },
    })

    const uiStore = useUiStore()
    uiStore.confirm('Message')
    await wrapper.vm.$nextTick()

    const actions = wrapper.find('.actions')
    const buttons = actions.findAll('button')
    expect(buttons).toHaveLength(2)
    expect(buttons[0]!.classes()).toContain('btn-cancel')
    expect(buttons[1]!.classes()).toContain('btn-ok')
  })

  it('doit fermer la modale après la confirmation', async () => {
    const wrapper = mount(ConfirmModal, {
      global: {
        plugins: [createPinia()],
      },
    })

    const uiStore = useUiStore()
    const promise = uiStore.confirm('Message')
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.overlay').exists()).toBe(true)

    const btnOk = wrapper.find('.btn-ok')
    await btnOk.trigger('click')
    await promise
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.overlay').exists()).toBe(false)
  })
})
