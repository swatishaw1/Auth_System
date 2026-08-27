import useAuth from "@/auth/store";
import { refreshToken } from "@/services/AuthService";
import { Loader2, ShieldCheck } from "lucide-react";
import { useEffect, useState } from "react";
import toast from "react-hot-toast";
import { useNavigate } from "react-router";

function OAuthSuccess() {
  const [isRefreshing, setIsRefreshing] = useState<boolean>(false);

  const changeLocalLoginData = useAuth((state) => state.changeLocalLoginData);
  const navigate = useNavigate();

  useEffect(() => {
    async function getAccessToken() {
      if (!isRefreshing) {
        //refresh token ki api call
        setIsRefreshing(true);
        try {
          const responseLoginData = await refreshToken();
          console.log(responseLoginData);
          //login:
          changeLocalLoginData(
            responseLoginData.accessToken,
            responseLoginData.user,
            true
          );

          toast.success("Login success !");
          navigate("/dashboard");
        } catch (error) {
          toast.error("Error while login!");
          console.log(error);
        } finally {
          setIsRefreshing(false);
        }
      }
    }

    getAccessToken();
  }, []);

  return (
    <main className="flex min-h-screen items-center justify-center bg-background px-4">

      <div className="w-full max-w-md text-center">

        {/* Icon */}
        <div className="mx-auto mb-6 flex h-16 w-16 items-center justify-center rounded-full bg-muted">
          <ShieldCheck className="h-8 w-8" />
        </div>

        {/* Message */}
        <h1 className="text-2xl font-bold tracking-tight">
          Authentication Successful
        </h1>

        <p className="mt-2 text-sm text-muted-foreground">
          Your account has been authenticated successfully.
        </p>

        {/* Loading */}
        <div className="mt-6 flex items-center justify-center gap-2 text-sm text-muted-foreground">
          <Loader2 className="h-4 w-4 animate-spin" />
          Redirecting to your dashboard...
        </div>

        {/* Small status */}
        <p className="mt-8 text-xs text-muted-foreground">
          Please wait while we finish setting up your session.
        </p>

      </div>

    </main>
  );
}

export default OAuthSuccess;