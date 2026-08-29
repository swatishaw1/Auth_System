import { AlertCircle, ArrowLeft, Home, RefreshCw } from "lucide-react";
import { useNavigate } from "react-router";
import { Button } from "@/components/ui/button";

function OAuthFailure() {

  const navigate = useNavigate();

  return (
    <main className="flex min-h-screen items-center justify-center bg-background px-4">

      <div className="w-full max-w-md text-center">
        {/* Icon */}
        <div className="mx-auto mb-6 flex h-16 w-16 items-center justify-center rounded-full bg-destructive/10">
          <AlertCircle className="h-8 w-8 text-destructive" />
        </div>
        {/* Message */}
        <h1 className="text-2xl font-bold tracking-tight"> Authentication Failed </h1>
        <p className="mt-2 text-sm leading-6 text-muted-foreground">
          We couldn't complete your authentication. Please try again or
          return to the home page.
        </p>

        {/* Actions */}
        <div className="mt-6 flex flex-col gap-3 sm:flex-row sm:justify-center">
          <Button variant="outline" className="cursor-pointer" onClick={() => navigate("/login")} >
            <RefreshCw className="mr-2 h-4 w-4" />
            Try Again
          </Button>

          <Button className="cursor-pointer" onClick={() => navigate("/")} >
            <Home className="mr-2 h-4 w-4" />
            Go Home
          </Button>
        </div>

        {/* Back link */}
        <button type="button" onClick={() => navigate(-1)} className="mt-8 inline-flex cursor-pointer items-center text-xs text-muted-foreground transition hover:text-foreground" >
          <ArrowLeft className="mr-1 h-3 w-3" /> Go back
        </button>
      </div>
    </main>
  );
}

export default OAuthFailure;