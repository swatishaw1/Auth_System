import { FaGithub } from "react-icons/fa";
import { Button } from "./ui/button";

function OAuth2Buttons() {

  const handleGoogleLogin = () => {
        const backendUrl =
        import.meta.env.VITE_API_BASE_BACKEND_URL || "http://localhost:8080";
        window.location.href = `${backendUrl}/oauth2/authorization/google`;
  };

  const handleGithubLogin = () => {
    const backendUrl = import.meta.env.VITE_API_BASE_BACKEND_URL || "http://localhost:8080";
    window.location.href = `${backendUrl}/oauth2/authorization/github`;
  };

  return (
    <div className="space-y-3">
        
      <Button variant="outline" className="w-full cursor-pointer" onClick={handleGoogleLogin} >
        <img src="https://www.svgrepo.com/show/475656/google-color.svg" alt="Google" className="mr-2 h-4 w-4" />
        Continue with Google
      </Button>

      <Button variant="outline" className="w-full cursor-pointer" onClick={handleGithubLogin} >
        <FaGithub className="mr-2 h-4 w-4" />
        Continue with GitHub
      </Button>
    </div>
  );
}

export default OAuth2Buttons;