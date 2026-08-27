import type LoginData from "@/models/LoginData";
import type LoginResponseData from "@/models/LoginResponseData";
import type User from "@/models/User";
import { loginUser, logoutUser } from "@/services/AuthService";
import { create } from "zustand";
import { persist } from "zustand/middleware";

const LOCAL_KEY = "app_state";
//global authstate:

type AuthState = {
  accessToken: string | null;
  user: User | null;
  authStatus: boolean;
  authLoading: boolean;
  login: (loginData: LoginData) => Promise<LoginResponseData>;
  logout: (silent?: boolean) => Promise<void>;
  checkLogin: () => boolean | undefined;

  changeLocalLoginData: (
    accessToken: string,
    user: User,
    authStatus: boolean,
  ) => void;
};

//main logic for global state
const useAuth = create<AuthState>()(
  persist(
    (set, get) => ({
      accessToken: null,
      user: null,
      authStatus: false,
      authLoading: false,

      //For rotation of Refresh Token
      changeLocalLoginData: (accessToken, user, authStatus) => {
        set({ accessToken, user, authStatus });
      },
      login: async (loginData) => {
        console.log("started login...");
        set({ authLoading: true });
        try {
          const loginResponseData = await loginUser(loginData);
          console.log(loginResponseData);
          set({
            accessToken: loginResponseData.accessToken,
            user: loginResponseData.user,
            authStatus: true,
          });
          return loginResponseData;
        } catch (error) {
          console.log(error);
          return Promise.reject(error);
        } finally {
          set({ authLoading: false });
        }
      },
      logout: async (silent = false) => {
        try {
          //   if (!silent) {
          //     await logoutUser();
          //   }
          set({ authLoading: true });
          await logoutUser();
          // console.clear();
        } catch (error) {
          console.error(error);
          console.error("Logout failed:", error);
        } finally {
          set({
            authLoading: false,
          });
        }
        // await logoutUser();
        set({
          accessToken: null,
          user: null,
          authLoading: false,
          authStatus: false,
        });
      },
      checkLogin: () => {
        return !!(get().accessToken && get().authStatus);
      },
    }),
    { name: LOCAL_KEY },
  ),
);

export default useAuth;
