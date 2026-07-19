import { Menu, X } from "lucide-react";
import { useState } from "react";
import { NavLink } from "react-router";
import ThemeToggle from "./ThemeToggle";
import { Button } from "./ui/button";

function Navbar() {
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
        <div className="hidden items-center gap-6 md:flex">
          <ThemeToggle />
          <NavLink to={"/"} className="text-sm text-muted-foreground transition-colors hover:text-foreground">Home</NavLink>
          <NavLink to={"/login"}><Button variant={"outline"} className="cursor-pointer">Login</Button></NavLink>
          <NavLink to={"/signup"}><Button className="cursor-pointer">Sign Up</Button></NavLink>
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
              onClick={() => setIsOpen(false)}> Home </NavLink>
            <NavLink to={"/login"} onClick={() => setIsOpen(false)}>
              <Button variant={"outline"} className="w-full cursor-pointer">Login</Button>
            </NavLink>

            <NavLink to={"/signup"} onClick={() => setIsOpen(false)}>
              <Button className="w-full cursor-pointer"> Sign Up</Button>
            </NavLink>
          </div>
        </div>
      )}
    </nav>
  );
}

export default Navbar;