import ApiClient from "@/configuration/ApiClient";
import type LoginData from "@/models/loginData";
import type RegisterData from "@/models/RegisterData";

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