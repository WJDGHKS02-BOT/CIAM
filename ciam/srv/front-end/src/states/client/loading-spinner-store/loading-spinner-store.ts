import { create } from 'zustand';

const DEFAULT_MESSAGE = {
  title: 'Hang Tight!',
  description: 'This may take a few more minutes to process your request.',
} as const;

interface LoadingStore {
  isLoading: boolean;
  message?: {
    title?: string;
    description?: string;
  };
  showLoading: (message?: { title?: string; description?: string }) => void;
  showDefaultLoading: () => void;
  hideLoading: () => void;
}

export const useLoadingStore = create<LoadingStore>((set) => ({
  isLoading: false,
  message: undefined,
  showLoading: (message) => set({ isLoading: true, message }),
  showDefaultLoading: () => set({ isLoading: true, message: DEFAULT_MESSAGE }),
  hideLoading: () => set({ isLoading: false, message: undefined }),
}));
