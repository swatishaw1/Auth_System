import { Moon, Sun } from "lucide-react";
import { useEffect, useState } from "react";
import { ToggleGroup, ToggleGroupItem } from "./ui/toggle-group";

export default function ThemeToggle() {
  const [theme, setTheme] = useState(localStorage.getItem("theme") || "light");
  useEffect(() => {
    localStorage.setItem("theme", theme);
    if (theme === "dark") {
      document.documentElement.classList.add("dark");
    } else {
      document.documentElement.classList.remove("dark");
    }
  }, [theme]);

  return (
    <ToggleGroup
      type="single"
      value={theme}
      onValueChange={(value) => {
        if (value) setTheme(value);
      }}
      className="bg-muted rounded-full p-2"
    >
      <ToggleGroupItem
        value="light"
        className=" rounded-full w-6 h-6 data-[state=on]:bg-background data-[state=on]:text-foreground hover:bg-gray-700"
      >
        <Sun className="h-0.01 w-0.01" />
      </ToggleGroupItem>
      <ToggleGroupItem
        value="dark"
        className=" rounded-full w-6 h-6 data-[state=on]:bg-background data-[state=on]:text-foreground hover:bg-gray-400 "
      >
        <Moon className="h-0.01 w-0.01" />
      </ToggleGroupItem>
    </ToggleGroup>
  );
}
