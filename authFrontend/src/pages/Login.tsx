import { FaGithub } from "react-icons/fa";
import { Button } from "../components/ui/button";
import { Card, CardContent } from "../components/ui/card";
import { Input } from "../components/ui/input";
import { Label } from "../components/ui/label";
import { NavLink, useNavigate } from "react-router";
import React, { useState } from "react";
import toast from "react-hot-toast";
import { loginUser } from "@/services/AuthService";
import type LoginData from "@/models/loginData";
import type ErrorResponse from "@/response/ErrorResponse";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { AlertCircleIcon } from "lucide-react";
import { Spinner } from "@/components/ui/spinner";
import useAuth from "@/auth/store";
import OAuth2Buttons from "@/components/OAuth2Buttons";

export default function LoginPage() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState(null);
  const login = useAuth(state => state.login);
  const [loginData, setLoginData] = useState<LoginData>({
    email: "",
    password: ""
  });
  const onHandleLoginInputChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setLoginData({
      ...loginData,
      [event.target.name]: event.target.value,
    });
  };

  const onHandleSubmitForm = async (event: React.FormEvent) => {
    event.preventDefault();
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(loginData.email)) {
      toast.error("Enter a valid email");
      return;
    }
    if (!loginData.password.trim()) {
      toast.error("Password is required");
      return;
    }
    try {
      setLoading(true);
      // const userInfo = await loginUser(loginData);
      const userInfo = await login(loginData);
      console.log(userInfo);
      toast.success("User Logged In Successfully");
      setLoginData({
        email: "",
        password: ""
      });
      navigate("/dashboard");
    } catch (error: any) {
        console.error(error);

        if (error.response?.data?.message) {
            setError(error.response.data.message);
        } else {
            setError("Unable to connect to the server.");
        }
    }finally{
      setLoading(false);
    }
  };

  return (
    <main className="flex min-h-screen items-center justify-center bg-background px-4">
      <Card className="w-full max-w-md border-border shadow-sm">
        <CardContent className="p-8">
          <div className="mb-8 text-center">
            <h1 className="text-3xl font-bold">Welcome Back</h1>
            <p className="mt-2 text-sm text-muted-foreground">Login to continue to your account</p>
          </div>
          {/* Form */}
          <form className="space-y-5" onSubmit={onHandleSubmitForm}>
              {error && (
                <Alert variant="destructive" className="max-w-md">
                <AlertCircleIcon />
                <AlertTitle>Login Failed</AlertTitle>
                <AlertDescription>
                  {error || "An error occurred while logging in."}
                </AlertDescription>
              </Alert>
              )}
            <div className="space-y-2">
              {/* Email */}
              <Label htmlFor="email">Email</Label>
              <Input id="email" type="email" placeholder="john@example.com" name="email" value={loginData.email} onChange={onHandleLoginInputChange} />
            </div>

            {/* Password */}
            <div className="space-y-2">
              <Label htmlFor="password">Password</Label>
              <Input id="password" type="password" placeholder="Enter your password" name="password" value={loginData.password} onChange={onHandleLoginInputChange} />
            </div>

            <Button className="w-full cursor-pointer"> {loading?<> <Spinner/> Please Wait...</>:"Login"}</Button>
          </form>

          <div className="my-6 flex items-center">
            <div className="h-px flex-1 bg-border" />
            <span className="px-3 text-xs text-muted-foreground">OR</span>
            <div className="h-px flex-1 bg-border" />
          </div>


          {/* OAuth */}
          <OAuth2Buttons/>
          <p className="mt-6 text-center text-sm text-muted-foreground"> Don't have an account?{" "}
            <NavLink to={"/SignUp"} className="font-medium text-foreground hover:underline">Sign Up</NavLink>
          </p>
        </CardContent>
      </Card>
    </main>
  );
}