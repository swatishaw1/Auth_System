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
import UserProfile from './pages/users/UserProfile.tsx';
import UserLayout from './pages/users/UserLayout.tsx';
import OAuthSuccess from './pages/OAuthSuccess.tsx';
import OAuthFailure from './pages/OAuthFailure.tsx';
import ForgotPassword from './pages/ForgotPassword.tsx';

createRoot(document.getElementById('root')!).render(
  <BrowserRouter>
    <Toaster toastOptions={{ duration: 3000, }} />
    <Routes>
      <Route path="/" element={<RootLayout />}>
        <Route index element={<App />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<SignUp />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/about" element={<About />} />
        <Route path="/services" element={<Services />} />
        <Route element={<UserLayout />}>
          <Route path="dashboard" element={<UserDashboard />} />
          <Route path="profile" element={<UserProfile />} />
          <Route path="dashboard/profile" element={<UserProfile />} />
        </Route>
        <Route path="/oauth/success" element={<OAuthSuccess />} />
        <Route path="/oauth/failure" element={<OAuthFailure />} />
      </Route>
    </Routes>
  </BrowserRouter>,
);
