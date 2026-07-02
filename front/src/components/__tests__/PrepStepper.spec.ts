import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import PrepStepper from '@/components/PrepStepper.vue'
import type { PrepStatus } from '@/types'

describe('PrepStepper component', () => {
  it('doit monter correctement avec un status valide', () => {
    const wrapper = mount(PrepStepper, {
      props: {
        status: 'PREPARATION_INGREDIENTS',
      },
    })
    expect(wrapper.exists()).toBe(true)
  })

  it('doit afficher 4 étapes', () => {
    const wrapper = mount(PrepStepper, {
      props: {
        status: 'PREPARATION_INGREDIENTS',
      },
    })

    const steps = wrapper.findAll('.step')
    expect(steps).toHaveLength(4)
  })

  it('doit afficher les labels des étapes', () => {
    const wrapper = mount(PrepStepper, {
      props: {
        status: 'PREPARATION_INGREDIENTS',
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
        status: 'PREPARATION_INGREDIENTS',
      },
    })

    const dots = wrapper.findAll('.dot')
    expect(dots[0]!.text()).toBe('1')
    expect(dots[1]!.text()).toBe('2')
    expect(dots[2]!.text()).toBe('3')
    expect(dots[3]!.text()).toBe('4')
  })

  it('doit avoir la classe cur sur l\'étape courante (PREPARATION_INGREDIENTS)', async () => {
    const wrapper = mount(PrepStepper, {
      props: {
        status: 'PREPARATION_INGREDIENTS',
      },
    })

    const steps = wrapper.findAll('.step')
    expect(steps[0]!.classes()).toContain('cur')
    expect(steps[1]!.classes()).not.toContain('cur')
    expect(steps[2]!.classes()).not.toContain('cur')
    expect(steps[3]!.classes()).not.toContain('cur')
  })

  it('doit avoir la classe cur sur l\'étape courante (ASSEMBLAGE)', async () => {
    const wrapper = mount(PrepStepper, {
      props: {
        status: 'ASSEMBLAGE',
      },
    })

    const steps = wrapper.findAll('.step')
    expect(steps[0]!.classes()).not.toContain('cur')
    expect(steps[1]!.classes()).toContain('cur')
    expect(steps[2]!.classes()).not.toContain('cur')
    expect(steps[3]!.classes()).not.toContain('cur')
  })

  it('doit avoir la classe cur sur l\'étape courante (DRESSAGE)', async () => {
    const wrapper = mount(PrepStepper, {
      props: {
        status: 'DRESSAGE',
      },
    })

    const steps = wrapper.findAll('.step')
    expect(steps[0]!.classes()).not.toContain('cur')
    expect(steps[1]!.classes()).not.toContain('cur')
    expect(steps[2]!.classes()).toContain('cur')
    expect(steps[3]!.classes()).not.toContain('cur')
  })

  it('doit avoir la classe cur sur l\'étape courante (TERMINEE)', async () => {
    const wrapper = mount(PrepStepper, {
      props: {
        status: 'TERMINEE',
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
        status: 'ASSEMBLAGE',
      },
    })

    const steps = wrapper.findAll('.step')
    expect(steps[0]!.classes()).toContain('done')
    expect(steps[1]!.classes()).not.toContain('done')
    expect(steps[2]!.classes()).not.toContain('done')
    expect(steps[3]!.classes()).not.toContain('done')
  })

  it('doit marquer toutes les étapes précédentes comme done quand TERMINEE', async () => {
    const wrapper = mount(PrepStepper, {
      props: {
        status: 'TERMINEE',
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
        status: 'PREPARATION_INGREDIENTS',
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
        status: 'PREPARATION_INGREDIENTS' as PrepStatus,
      },
    })

    let steps = wrapper.findAll('.step')
    expect(steps[0]!.classes()).toContain('cur')

    await wrapper.setProps({ status: 'ASSEMBLAGE' })

    steps = wrapper.findAll('.step')
    expect(steps[0]!.classes()).toContain('done')
    expect(steps[1]!.classes()).toContain('cur')
  })

  it('doit gérer les transitions multiples de statut', async () => {
    const wrapper = mount(PrepStepper, {
      props: {
        status: 'PREPARATION_INGREDIENTS' as PrepStatus,
      },
    })

    await wrapper.setProps({ status: 'ASSEMBLAGE' })
    let steps = wrapper.findAll('.step')
    expect(steps[1]!.classes()).toContain('cur')

    await wrapper.setProps({ status: 'DRESSAGE' })
    steps = wrapper.findAll('.step')
    expect(steps[0]!.classes()).toContain('done')
    expect(steps[1]!.classes()).toContain('done')
    expect(steps[2]!.classes()).toContain('cur')

    await wrapper.setProps({ status: 'TERMINEE' })
    steps = wrapper.findAll('.step')
    expect(steps[3]!.classes()).toContain('done')
    expect(steps[3]!.classes()).toContain('cur')
  })

  it('doit avoir la classe stepper sur le conteneur', () => {
    const wrapper = mount(PrepStepper, {
      props: {
        status: 'PREPARATION_INGREDIENTS',
      },
    })

    const stepper = wrapper.find('.stepper')
    expect(stepper.classes()).toContain('stepper')
  })

  it('doit afficher les points avec les classes correctes', () => {
    const wrapper = mount(PrepStepper, {
      props: {
        status: 'DRESSAGE',
      },
    })

    const dots = wrapper.findAll('.dot')
    expect(dots[0]!.classes()).toContain('dot')
    expect(dots[1]!.classes()).toContain('dot')
    expect(dots[2]!.classes()).toContain('dot')
    expect(dots[3]!.classes()).toContain('dot')
  })
})
