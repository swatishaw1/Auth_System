import { FaGithub } from "react-icons/fa";
import { Button } from "../components/ui/button";
import { Card, CardContent } from "../components/ui/card";
import { Input } from "../components/ui/input";
import { Label } from "../components/ui/label";
import { NavLink, useNavigate } from "react-router";
import React, { useState, type FormEvent } from "react";
import toast from "react-hot-toast";
import { registerUser } from "@/services/AuthService";
import { Spinner } from "@/components/ui/spinner";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { AlertCircleIcon } from "lucide-react";
import OAuth2Buttons from "@/components/OAuth2Buttons";

export default function SignupPage() {
  const [data, setData] = useState({
    name: "",
    email: "",
    password: ""
  });
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState(null);
  const navigate = useNavigate();//For navigating user directly to another page

  //text input, email, password, number, textarea
  //Handling Form change 
  const onHandleInputChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setData((value) => ({
      ...value,
      [event.target.name]: event.target.value,
    }));
  };

  const handleFormSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    console.log(data);
    if (!data.name.trim()) {
      toast.error("Name is required");
      return;
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(data.email)) {
      toast.error("Enter a valid email");
      return;
    }
    if (!data.password.trim()) {
      toast.error("Password is required");
      return;
    }
    if (data.password.length < 3) {
      toast.error("Password must be at least 8 characters");
      return;
    }

    //Form Submit Error Success
    try {
      setLoading(true);
      const signUpInfo = await registerUser(data);
      console.log(signUpInfo);
      toast.success("User Register Successfully");
      setData({
        name: "",
        email: "",
        password: ""
      });
      navigate("/login");//navigate to login
    } catch (error:any) {
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
            <h1 className="text-3xl font-bold">Create Account</h1>
            <p className="mt-2 text-sm text-muted-foreground">Create your account to get started</p>
          </div>

          <form className="space-y-5" onSubmit={handleFormSubmit}>
            {error && (
                <Alert variant="destructive" className="max-w-md">
                <AlertCircleIcon />
                <AlertTitle>SignUp Failed</AlertTitle>
                <AlertDescription>
                  {error || "An error occurred while creating Account in."}
                </AlertDescription>
              </Alert>
              )}
            <div className="space-y-2">
              <Label htmlFor="name">Full Name</Label>
              <Input id="name" type="text" placeholder="John Doe" name="name" value={data.name} onChange={onHandleInputChange} />
            </div>

            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input id="email" type="email" placeholder="john@example.com" name="email" value={data.email} onChange={onHandleInputChange} />
            </div>

            <div className="space-y-2">
              <Label htmlFor="password">Password</Label>
              <Input id="password" type="password" placeholder="Create a password" name="password" value={data.password} onChange={onHandleInputChange} />
            </div>

            <Button className="w-full cursor-pointer"> {loading?<> <Spinner/> Please Wait...</>:"Create Account"}</Button>
          </form>

          <div className="my-6 flex items-center">
            <div className="h-px flex-1 bg-border" />
            <span className="px-3 text-xs text-muted-foreground">OR</span>
            <div className="h-px flex-1 bg-border" />
          </div>

          {/* OAuth2 Buttons */}
          <OAuth2Buttons/>

          <p className="mt-6 text-center text-sm text-muted-foreground">
            Already have an account?{" "}
            < NavLink to={"/login"} type="button" className="font-medium text-foreground hover:underline">Sign In</NavLink>
          </p>
        </CardContent>
      </Card>
    </main >
  );
}