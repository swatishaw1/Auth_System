import { createRoot } from 'react-dom/client';
import { BrowserRouter, Route, Routes } from "react-router";
import './index.css';
import App from './App.tsx';
import Login from './pages/Login.tsx';
import SignUp from './pages/SignUp.tsx';
import About from './pages/About.tsx';
import Services from './pages/Services.tsx';
import RootLayout from './pages/RootLayout.tsx';
import { Toaster } from 'react-hot-toast';
import UserDashboard from './pages/users/UserDashboard.tsx';

createRoot(document.getElementById('root')!).render(
  <BrowserRouter>
    <Toaster toastOptions={{ duration: 3000, }} />
    <Routes>
      <Route path="/" element={<RootLayout />}>
        <Route index element={<App />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<SignUp />} />
        <Route path="/about" element={<About />} />
        <Route path="/services" element={<Services />} />
        <Route path="/dashboard" element={<UserDashboard />} />
      </Route>
    </Routes>
  </BrowserRouter>,
)
