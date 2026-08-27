import ApiClient from "@/configuration/ApiClient";
import type LoginData from "@/models/loginData";
import type LoginResponseData from "@/models/LoginResponseData";
import type RegisterData from "@/models/RegisterData";
import type User from "@/models/User";

//Register User Function
export const registerUser = async (signUpData: RegisterData) => {
    //api call the server to save data
    const response = await ApiClient.post(`/auth/register`, signUpData);
    return response.data;
};


export const loginUser = async (loginData: LoginData) => {
    //api call the server to save data
    const response = await ApiClient.post(`/auth/login`, loginData);
    return response.data;
};

export const logoutUser = async () => {
  const response = await ApiClient.post(`/auth/logout`);
  return response.data;
};


export const getCurrentUser = async (emailId: string | undefined) => {
  const response = await ApiClient.get<User>(`/users/email/${emailId}`);
  return response.data;
};

//refresh token
export const refreshToken = async () => {
  const response = await ApiClient.post<LoginResponseData>(`/auth/refresh`);
  return response.data;
};