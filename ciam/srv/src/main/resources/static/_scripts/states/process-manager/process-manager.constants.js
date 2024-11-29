export const PROCESS_STATUS = {
  initial: Object.freeze({
    isLoading: false,
    isSuccess: false,
    isError: false,
    error: null
  }),

  loading: Object.freeze({
    isLoading: true,
    isSuccess: false,
    isError: false,
    error: null
  }),

  success: Object.freeze({
    isLoading: false,
    isSuccess: true,
    isError: false,
    error: null
  }),

  error: (error) => Object.freeze({
    isLoading: false,
    isSuccess: false,
    isError: true,
    error
  })
};