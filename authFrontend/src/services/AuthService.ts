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

//Update User
export const updateUser = async (userId: string, userData: Partial<User>) => {
  const response = await ApiClient.put<User>(`/users/${userId}`, userData);
  return response.data;
};

//Delete User
export const deleteUser = async (userId: string) => {
  const response = await ApiClient.delete(`/users/${userId}`);
  return response.data;
};

// Forgot Password
export const forgotPassword = async (email: string) => {
  const response = await ApiClient.post("/auth/verifyEmail", {email,});
  return response.data;
};

// Verify OTP
export const verifyOtp = async (email: string, otp: string) => {
  const response = await ApiClient.post("/auth/verifyOtp", { email, otp, });
  return response.data;
};

// Reset Password
export const resetPassword = async ( email: string, rewrittenPassword: string, password: string ) => {
  const response = await ApiClient.post("/auth/resetPassword", { email, rewrittenPassword, password, });
  return response.data;
};
