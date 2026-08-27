import { Menu, X } from "lucide-react";
import { useState } from "react";
import { NavLink, useNavigate } from "react-router";
import ThemeToggle from "./ThemeToggle";
import { Button } from "./ui/button";
import useAuth from "@/auth/store";

function Navbar() {
  const accessToken = useAuth(state => state.accessToken);
  const authStatus = useAuth(state => state.authStatus);
  const user = useAuth(state => state.user);
  const logout = useAuth(state => state.logout);
  const navigate = useNavigate();
  

  const isLoggedIn = !!(accessToken && authStatus);
  const [isOpen, setIsOpen] = useState(false);
  return (
    <nav className="sticky top-0 z-50 border-b border-border bg-background/90 backdrop-blur">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-6">

        {/* Logo */}
        <div className="flex items-center gap-2">
          <span className="inline-block h-6 w-6 rounded-md bg-gradient-to-br from-slate-500 to-slate-700"></span>
          <NavLink to={"/"}>
            <span className="text-sm font-semibold tracking-tight md:text-base">Multitenant Authentication</span>
          </NavLink>
        </div>

        {/* Desktop Menu */}
        <div className="hidden items-center gap-5 md:flex">
          <ThemeToggle />

          <NavLink to="/" className="text-sm font-medium text-muted-foreground transition-colors hover:text-foreground"> Home
          </NavLink>

          {!isLoggedIn ? (
            <div className="flex items-center gap-2">
              <NavLink to="/login">
                <Button variant="outline" className="cursor-pointer px-4">Login</Button>
              </NavLink>

              <NavLink to="/signup">
                <Button className="cursor-pointer px-4">Sign Up</Button>
              </NavLink>
            </div>
          ) : (
            <div className="flex items-center gap-3">
              <NavLink to="/profile" className="flex items-center gap-2 rounded-lg px-3 py-2 transition-colors hover:bg-muted">
                <div className="flex h-8 w-8 items-center justify-center rounded-full bg-muted text-sm font-semibold">
                  {user?.name?.charAt(0).toUpperCase()}
                </div>

                <div className="flex flex-col">
                  <NavLink to={"/dashboard/profile"} className="text-sm font-medium" >{user?.name}</NavLink>
                </div>
              </NavLink>

              <Button variant="outline" size="sm" className="cursor-pointer"
                onClick={() => {
                  logout();
                  navigate("/");
                }}> Logout </Button>
            </div>
          )}
        </div>
        {/* Mobile Menu Button */}
        <button onClick={() => setIsOpen(!isOpen)} className="md:hidden">
          {isOpen ? (<X className="h-5 w-5" />) : (<Menu className="h-5 w-5" />)}
        </button>
      </div>
      {/* Mobile Menu */}
      {isOpen && (
        <div className="border-t border-border bg-background md:hidden">
          <div className="flex flex-col gap-4 px-6 py-5">
            <div className="flex items-center justify-between">
              <span className="text-sm text-muted-foreground">Theme</span>
              <ThemeToggle />
            </div>
            
            <NavLink to={"/"} className="text-sm text-muted-foreground transition-colors hover:text-foreground"
              onClick={() => setIsOpen(false)}>Home</NavLink>
            {!isLoggedIn ? (
              <>
                <NavLink to="/login" onClick={() => setIsOpen(false)}>
                  <Button variant="outline" className="w-full cursor-pointer">Login</Button>
                </NavLink>

                <NavLink to="/signup" onClick={() => setIsOpen(false)}>
                  <Button className="w-full cursor-pointer">Sign Up</Button>
                </NavLink>
              </>
            ) : (
              <>
                <NavLink to="/profile" onClick={() => setIsOpen(false)}>
                  <Button className="w-full cursor-pointer"> {user?.name} </Button>
                </NavLink>

                <Button variant="outline" className="w-full cursor-pointer" onClick={() => {
                    logout();
                    navigate("/");
                setIsOpen(false);}}>Logout</Button>
              </>
            )};
          </div>
        </div>
      )}
    </nav>
  );
}

export default Navbar;