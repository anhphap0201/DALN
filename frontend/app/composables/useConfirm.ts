import { reactive } from 'vue'

type ConfirmState = {
  visible: boolean
  title?: string
  message?: string
  confirmText?: string
  cancelText?: string
  _resolver?: (value: boolean) => void
}

const state = reactive<ConfirmState>({
  visible: false,
  title: undefined,
  message: undefined,
  confirmText: 'OK',
  cancelText: 'Hủy',
  _resolver: undefined,
})

export function useConfirm() {
  const confirm = (message: string, title?: string, options?: { confirmText?: string; cancelText?: string }) => {
    return new Promise<boolean>((resolve) => {
      state.message = message
      state.title = title
      state.confirmText = options?.confirmText || 'OK'
      state.cancelText = options?.cancelText || 'Hủy'
      state.visible = true
      state._resolver = (v: boolean) => {
        state.visible = false
        state._resolver = undefined
        resolve(v)
      }
    })
  }

  const resolveConfirm = (value: boolean) => {
    if (state._resolver) {
      state._resolver(value)
    }
    state.visible = false
  }

  return { state, confirm, resolveConfirm }
}

export { state as confirmState }
