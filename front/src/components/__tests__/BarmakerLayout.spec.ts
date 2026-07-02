import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import BarmakerLayout from '@/components/BarmakerLayout.vue'
import { useAuthStore } from '@/stores/auth'

describe('BarmakerLayout component', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('doit monter correctement avec le contenu du slot', () => {
    const wrapper = mount(BarmakerLayout, {
      global: {
        plugins: [createPinia()],
        stubs: {
          RouterLink: {
            template: '<a :href="to"><slot /></a>',
            props: ['to'],
          },
        },
      },
      slots: {
        default: '<div class="test-content">Mon contenu</div>',
      },
    })
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.test-content').text()).toBe('Mon contenu')
  })

  it('doit afficher le titre LE BAR\'APP', () => {
    const wrapper = mount(BarmakerLayout, {
      global: {
        plugins: [createPinia()],
        stubs: {
          RouterLink: {
            template: '<a :href="to"><slot /></a>',
            props: ['to'],
          },
        },
      },
    })

    const title = wrapper.find('.b')
    expect(title.text()).toBe('LE BAR\'APP')
  })

  it('doit utiliser le role fourni en prop', () => {
    const wrapper = mount(BarmakerLayout, {
      props: {
        role: 'Manager',
      },
      global: {
        plugins: [createPinia()],
        stubs: {
          RouterLink: {
            template: '<a :href="to"><slot /></a>',
            props: ['to'],
          },
        },
      },
    })

    const roleText = wrapper.find('.role')
    expect(roleText.text()).toContain('Manager')
  })

  it('doit utiliser le role par défaut si non fourni', () => {
    const wrapper = mount(BarmakerLayout, {
      global: {
        plugins: [createPinia()],
        stubs: {
          RouterLink: {
            template: '<a :href="to"><slot /></a>',
            props: ['to'],
          },
        },
      },
    })

    const roleText = wrapper.find('.role')
    expect(roleText.text()).toContain('Barmaker')
  })

  it('doit utiliser la location fournie en prop', () => {
    const wrapper = mount(BarmakerLayout, {
      props: {
        location: 'Terrasse',
      },
      global: {
        plugins: [createPinia()],
        stubs: {
          RouterLink: {
            template: '<a :href="to"><slot /></a>',
            props: ['to'],
          },
        },
      },
    })

    const roleText = wrapper.find('.role')
    expect(roleText.text()).toContain('Terrasse')
  })

  it('doit utiliser la location par défaut si non fournie', () => {
    const wrapper = mount(BarmakerLayout, {
      global: {
        plugins: [createPinia()],
        stubs: {
          RouterLink: {
            template: '<a :href="to"><slot /></a>',
            props: ['to'],
          },
        },
      },
    })

    const roleText = wrapper.find('.role')
    expect(roleText.text()).toContain('Comptoir')
  })

  it('doit avoir une barre latérale avec les classes correctes', () => {
    const wrapper = mount(BarmakerLayout, {
      global: {
        plugins: [createPinia()],
        stubs: {
          RouterLink: {
            template: '<a :href="to"><slot /></a>',
            props: ['to'],
          },
        },
      },
    })

    const sidebar = wrapper.find('.side')
    expect(sidebar.classes()).toContain('side')
  })

  it('doit avoir une zone principale avec les classes correctes', () => {
    const wrapper = mount(BarmakerLayout, {
      global: {
        plugins: [createPinia()],
        stubs: {
          RouterLink: {
            template: '<a :href="to"><slot /></a>',
            props: ['to'],
          },
        },
      },
    })

    const main = wrapper.find('.main')
    expect(main.classes()).toContain('main')
  })

  it('doit afficher un bouton de déconnexion', () => {
    const wrapper = mount(BarmakerLayout, {
      global: {
        plugins: [createPinia()],
        stubs: {
          RouterLink: {
            template: '<a :href="to"><slot /></a>',
            props: ['to'],
          },
        },
      },
    })

    const logoutBtn = wrapper.find('.logout-btn')
    expect(logoutBtn.exists()).toBe(true)
    expect(logoutBtn.text()).toBe('Déconnexion')
  })

  it('doit appeler logout quand on clique sur déconnexion', async () => {
    const wrapper = mount(BarmakerLayout, {
      global: {
        plugins: [createPinia()],
        stubs: {
          RouterLink: {
            template: '<a :href="to"><slot /></a>',
            props: ['to'],
          },
        },
      },
    })

    const authStore = useAuthStore()
    authStore.token = 'some-token'

    const logoutBtn = wrapper.find('.logout-btn')
    await logoutBtn.trigger('click')

    expect(authStore.token).toBe('')
  })

  it('doit afficher des liens de navigation', () => {
    const wrapper = mount(BarmakerLayout, {
      global: {
        plugins: [createPinia()],
        stubs: {
          RouterLink: {
            template: '<a :href="to"><slot /></a>',
            props: ['to'],
          },
        },
      },
    })

    const links = wrapper.findAll('.nav-link')
    expect(links.length).toBeGreaterThanOrEqual(2)
  })

  it('doit afficher le conteneur nav-links', () => {
    const wrapper = mount(BarmakerLayout, {
      global: {
        plugins: [createPinia()],
        stubs: {
          RouterLink: {
            template: '<a :href="to"><slot /></a>',
            props: ['to'],
          },
        },
      },
    })

    const navLinks = wrapper.find('.nav-links')
    expect(navLinks.exists()).toBe(true)
  })

  it('doit avoir la structure bm correcte', () => {
    const wrapper = mount(BarmakerLayout, {
      global: {
        plugins: [createPinia()],
        stubs: {
          RouterLink: {
            template: '<a :href="to"><slot /></a>',
            props: ['to'],
          },
        },
      },
    })

    const bm = wrapper.find('.bm')
    expect(bm.exists()).toBe(true)
    expect(bm.classes()).toContain('bm')
  })

  it('doit afficher les infos de role et location séparées par un bullet', () => {
    const wrapper = mount(BarmakerLayout, {
      props: {
        role: 'Barista',
        location: 'Bar',
      },
      global: {
        plugins: [createPinia()],
        stubs: {
          RouterLink: {
            template: '<a :href="to"><slot /></a>',
            props: ['to'],
          },
        },
      },
    })

    const roleText = wrapper.find('.role')
    expect(roleText.text()).toBe('Barista • Bar')
  })
})
