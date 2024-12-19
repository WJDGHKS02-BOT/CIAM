import { create } from 'zustand';

type State = {
  isLoggedIn: boolean;
  info: any;
};

type Action = {
  setIsLoggedIn: (isLoggedIn: boolean) => void;
  setInfo: (info: any) => void;
};

export type CurrentUserStore = State & Action;

export const useCurrentUserStore = create<CurrentUserStore>((set) => ({
  isLoggedIn: false,
  info: null,
  setIsLoggedIn: (isLoggedIn) => set({ isLoggedIn }),
  setInfo: (info) => set({ info }),
}));

export default useCurrentUserStore;
