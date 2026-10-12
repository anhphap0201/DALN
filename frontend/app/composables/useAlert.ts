import { reactive } from 'vue'

export type AlertType = 'info' | 'success' | 'warning' | 'error'

export type AlertState = {
  visible: boolean
  title?: string
  message: string
  type: AlertType
  buttonText?: string
  _resolver?: () => void
}

const state = reactive<AlertState>({
  visible: false,
  title: undefined,
  message: '',
  type: 'info',
  buttonText: 'Đồng ý',
  _resolver: undefined,
})

export function useAlert() {
  const alert = (
    message: string,
    title?: string,
    options?: { type?: AlertType; buttonText?: string }
  ) => {
    return new Promise<void>((resolve) => {
      state.message = message
      state.title = title
      state.type = options?.type || 'info'
      state.buttonText = options?.buttonText || 'Đồng ý'
      state.visible = true
      state._resolver = () => {
        state.visible = false
        state._resolver = undefined
        resolve()
      }
    })
  }

  const closeAlert = () => {
    if (state._resolver) {
      state._resolver()
    }
    state.visible = false
  }

  return { state, alert, closeAlert }
}

export { state as alertState }
