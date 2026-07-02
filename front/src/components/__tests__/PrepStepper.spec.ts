import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import PrepStepper from '@/components/PrepStepper.vue'
import type { PrepStatus } from '@/types'

describe('PrepStepper component', () => {
  it('doit monter correctement avec un status valide', () => {
    const wrapper = mount(PrepStepper, {
      props: {
        status: 'INGREDIENTS',
      },
    })
    expect(wrapper.exists()).toBe(true)
  })

  it('doit afficher 4 étapes', () => {
    const wrapper = mount(PrepStepper, {
      props: {
        status: 'INGREDIENTS',
      },
    })

    const steps = wrapper.findAll('.step')
    expect(steps).toHaveLength(4)
  })

  it('doit afficher les labels des étapes', () => {
    const wrapper = mount(PrepStepper, {
      props: {
        status: 'INGREDIENTS',
      },
    })

    const labels = wrapper.findAll('.lab')
    expect(labels[0]!.text()).toBe('Ingrédients')
    expect(labels[1]!.text()).toBe('Assemblage')
    expect(labels[2]!.text()).toBe('Dressage')
    expect(labels[3]!.text()).toBe('Terminé')
  })

  it('doit afficher les numéros des étapes', () => {
    const wrapper = mount(PrepStepper, {
      props: {
        status: 'INGREDIENTS',
      },
    })

    const dots = wrapper.findAll('.dot')
    expect(dots[0]!.text()).toBe('1')
    expect(dots[1]!.text()).toBe('2')
    expect(dots[2]!.text()).toBe('3')
    expect(dots[3]!.text()).toBe('4')
  })

  it('doit avoir la classe cur sur l\'étape courante (INGREDIENTS)', async () => {
    const wrapper = mount(PrepStepper, {
      props: {
        status: 'INGREDIENTS',
      },
    })

    const steps = wrapper.findAll('.step')
    expect(steps[0]!.classes()).toContain('cur')
    expect(steps[1]!.classes()).not.toContain('cur')
    expect(steps[2]!.classes()).not.toContain('cur')
    expect(steps[3]!.classes()).not.toContain('cur')
  })

  it('doit avoir la classe cur sur l\'étape courante (ASSEMBLY)', async () => {
    const wrapper = mount(PrepStepper, {
      props: {
        status: 'ASSEMBLY',
      },
    })

    const steps = wrapper.findAll('.step')
    expect(steps[0]!.classes()).not.toContain('cur')
    expect(steps[1]!.classes()).toContain('cur')
    expect(steps[2]!.classes()).not.toContain('cur')
    expect(steps[3]!.classes()).not.toContain('cur')
  })

  it('doit avoir la classe cur sur l\'étape courante (GARNISH)', async () => {
    const wrapper = mount(PrepStepper, {
      props: {
        status: 'GARNISH',
      },
    })

    const steps = wrapper.findAll('.step')
    expect(steps[0]!.classes()).not.toContain('cur')
    expect(steps[1]!.classes()).not.toContain('cur')
    expect(steps[2]!.classes()).toContain('cur')
    expect(steps[3]!.classes()).not.toContain('cur')
  })

  it('doit avoir la classe cur sur l\'étape courante (COMPLETED)', async () => {
    const wrapper = mount(PrepStepper, {
      props: {
        status: 'COMPLETED',
      },
    })

    const steps = wrapper.findAll('.step')
    expect(steps[0]!.classes()).not.toContain('cur')
    expect(steps[1]!.classes()).not.toContain('cur')
    expect(steps[2]!.classes()).not.toContain('cur')
    expect(steps[3]!.classes()).toContain('cur')
  })

  it('doit marquer les étapes précédentes comme done', async () => {
    const wrapper = mount(PrepStepper, {
      props: {
        status: 'ASSEMBLY',
      },
    })

    const steps = wrapper.findAll('.step')
    expect(steps[0]!.classes()).toContain('done')
    expect(steps[1]!.classes()).not.toContain('done')
    expect(steps[2]!.classes()).not.toContain('done')
    expect(steps[3]!.classes()).not.toContain('done')
  })

  it('doit marquer toutes les étapes précédentes comme done quand COMPLETED', async () => {
    const wrapper = mount(PrepStepper, {
      props: {
        status: 'COMPLETED',
      },
    })

    const steps = wrapper.findAll('.step')
    expect(steps[0]!.classes()).toContain('done')
    expect(steps[1]!.classes()).toContain('done')
    expect(steps[2]!.classes()).toContain('done')
    expect(steps[3]!.classes()).toContain('done')
  })

  it('ne doit pas marquer d\'étapes comme done au début', async () => {
    const wrapper = mount(PrepStepper, {
      props: {
        status: 'INGREDIENTS',
      },
    })

    const steps = wrapper.findAll('.step')
    expect(steps[0]!.classes()).not.toContain('done')
    expect(steps[1]!.classes()).not.toContain('done')
    expect(steps[2]!.classes()).not.toContain('done')
    expect(steps[3]!.classes()).not.toContain('done')
  })

  it('doit réagir aux changements de statut', async () => {
    const wrapper = mount(PrepStepper, {
      props: {
        status: 'INGREDIENTS' as PrepStatus,
      },
    })

    let steps = wrapper.findAll('.step')
    expect(steps[0]!.classes()).toContain('cur')

    await wrapper.setProps({ status: 'ASSEMBLY' })

    steps = wrapper.findAll('.step')
    expect(steps[0]!.classes()).toContain('done')
    expect(steps[1]!.classes()).toContain('cur')
  })

  it('doit gérer les transitions multiples de statut', async () => {
    const wrapper = mount(PrepStepper, {
      props: {
        status: 'INGREDIENTS' as PrepStatus,
      },
    })

    await wrapper.setProps({ status: 'ASSEMBLY' })
    let steps = wrapper.findAll('.step')
    expect(steps[1]!.classes()).toContain('cur')

    await wrapper.setProps({ status: 'GARNISH' })
    steps = wrapper.findAll('.step')
    expect(steps[0]!.classes()).toContain('done')
    expect(steps[1]!.classes()).toContain('done')
    expect(steps[2]!.classes()).toContain('cur')

    await wrapper.setProps({ status: 'COMPLETED' })
    steps = wrapper.findAll('.step')
    expect(steps[3]!.classes()).toContain('done')
    expect(steps[3]!.classes()).toContain('cur')
  })

  it('doit avoir la classe stepper sur le conteneur', () => {
    const wrapper = mount(PrepStepper, {
      props: {
        status: 'INGREDIENTS',
      },
    })

    const stepper = wrapper.find('.stepper')
    expect(stepper.classes()).toContain('stepper')
  })

  it('doit afficher les points avec les classes correctes', () => {
    const wrapper = mount(PrepStepper, {
      props: {
        status: 'GARNISH',
      },
    })

    const dots = wrapper.findAll('.dot')
    expect(dots[0]!.classes()).toContain('dot')
    expect(dots[1]!.classes()).toContain('dot')
    expect(dots[2]!.classes()).toContain('dot')
    expect(dots[3]!.classes()).toContain('dot')
  })
})
