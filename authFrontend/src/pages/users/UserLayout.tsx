import useAuth from "@/auth/store";
import { Navigate, Outlet, useLocation } from "react-router";

function UserLayout() {
  const accessToken = useAuth(state => state.accessToken);
  const authStatus = useAuth(state => state.authStatus);
  const location = useLocation();

  const isLoggedIn = !!(accessToken && authStatus);

  if (!isLoggedIn) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  return <Outlet />;
}

export default UserLayout;