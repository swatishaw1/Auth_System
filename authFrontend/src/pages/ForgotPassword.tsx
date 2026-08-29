import { useEffect, useState } from "react";
import { ArrowLeft, KeyRound, Mail, ShieldCheck } from "lucide-react";
import { useNavigate } from "react-router";
import toast from "react-hot-toast";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Spinner } from "@/components/ui/spinner";

import {
  forgotPassword,
  verifyOtp,
  resetPassword,
} from "@/services/AuthService";

type Step = "email" | "otp" | "password";

export default function ForgotPassword() {
  const navigate = useNavigate();
  const [step, setStep] = useState<Step>("email");
  const [email, setEmail] = useState("");
  const [otp, setOtp] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [loading, setLoading] = useState(false);

  // OTP timer in seconds
  const [timeLeft, setTimeLeft] = useState(0);


  // OTP COUNTDOWN
  useEffect(() => {
    if (step !== "otp" || timeLeft <= 0) {
      return;
    }

    const timer = setInterval(() => {
      setTimeLeft((prev) => prev - 1);
    }, 1000);

    return () => clearInterval(timer);
  }, [step, timeLeft]);

  // OTP EXPIRED
  useEffect(() => {
    if (step === "otp" && timeLeft === 0) {
      setOtp("");
      setStep("email");
      toast.error("OTP expired. Please request a new OTP.");
    }
  }, [step, timeLeft]);

  // STEP 1: SEND OTP
  const handleSendOtp = async (event: React.FormEvent) => {
    event.preventDefault();
    const trimmedEmail = email.trim();
    if (!trimmedEmail) {
      toast.error("Email is required");
      return;
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(trimmedEmail)) {
      toast.error("Enter a valid email");
      return;
    }

    try {
      setLoading(true);
      await forgotPassword(trimmedEmail);
      // 5 minutes = 300 seconds
      setTimeLeft(5 * 60);
      setOtp("");
      setStep("otp");
      toast.success("OTP sent to your email");
    } catch (error: any) {
      console.error("Failed to send OTP:", error);
      const message = error.response?.data?.message || error.response?.data || "Failed to send OTP";
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  // STEP 2: VERIFY OTP
  const handleVerifyOtp = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!otp.trim()) {
      toast.error("OTP is required");
      return;
    }
    if (otp.length !== 6) {
      toast.error("OTP must be 6 digits");
      return;
    }
    if (timeLeft <= 0) {
      toast.error("OTP expired. Please request a new OTP.");
      setOtp("");
      setStep("email");
      return;
    }

    try {
      setLoading(true);
      await verifyOtp(email, otp);
      setStep("password");
      toast.success("OTP verified successfully");
    } catch (error: any) {
      console.error("OTP verification failed:", error);
      const message = error.response?.data?.message || error.response?.data || "Invalid OTP";
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  // CHANGE EMAIL
  const handleChangeEmail = () => {
    setOtp("");
    setTimeLeft(0);
    setStep("email");
  };

  // STEP 3: RESET PASSWORD
  const handleResetPassword = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!password) {
      toast.error("Password is required");
      return;
    }
    if (password.length < 8) {
      toast.error("Password must be at least 8 characters");
      return;
    }
    if (!confirmPassword) {
      toast.error("Confirm password is required");
      return;
    }
    if (password !== confirmPassword) {
      toast.error("Passwords do not match");
      return;
    }
    try {
      setLoading(true);
      await resetPassword(email, confirmPassword, password);
      toast.success("Password reset successfully");
      // Clear form
      setEmail("");
      setOtp("");
      setPassword("");
      setConfirmPassword("");
      setTimeLeft(0);

      // Go back to login
      navigate("/login");
    } catch (error: any) {
      console.error("Password reset failed:", error);
      const message = error.response?.data?.message || error.response?.data || "Failed to reset password";
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  // FORMAT TIMER
  const minutes = Math.floor(timeLeft / 60);
  const seconds = timeLeft % 60;
  const formattedTime = `${minutes}:${String(seconds).padStart(2, "0")}`;
  // UI
  return (
    <main className="flex min-h-screen items-center justify-center bg-background px-4 py-8">
      <Card className="w-full max-w-md border-border shadow-sm">
        {/* HEADER */}
        <CardHeader className="space-y-3 text-center">
          <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-muted">
            {step === "email" && <Mail className="h-6 w-6" />}
            {step === "otp" && <ShieldCheck className="h-6 w-6" />}
            {step === "password" && <KeyRound className="h-6 w-6" />}
          </div>

          <CardTitle className="text-2xl font-bold">
            {step === "email" && "Forgot Password?"}
            {step === "otp" && "Verify OTP"}
            {step === "password" && "Reset Password"}
          </CardTitle>

          <p className="text-sm text-muted-foreground">
            {step === "email" && "Enter your email address and we'll send you an OTP."}
            {step === "otp" && `Enter the 6-digit OTP sent to ${email}.`}
            {step === "password" && "Create a new password for your account."}
          </p>
        </CardHeader>

        <CardContent>
          {/* EMAIL */}
          {step === "email" && (
            <form onSubmit={handleSendOtp} className="space-y-5">
              <div className="space-y-2">
                <Label htmlFor="email">Email Address</Label>
                <Input id="email" type="email" placeholder="john@example.com" value={email} onChange={(event) => setEmail(event.target.value)} disabled={loading} />
              </div>

              <Button type="submit" className="w-full cursor-pointer" disabled={loading}>
                {loading ? (<> <Spinner /> Sending OTP... </>) : ( "Send OTP")}
              </Button>
            </form>
          )}

          {/* OTP */}
          {step === "otp" && (
            <form onSubmit={handleVerifyOtp} className="space-y-5">
              <div className="space-y-2">
                <Label htmlFor="otp">OTP</Label>
                <Input id="otp" type="text" inputMode="numeric" maxLength={6} placeholder="Enter 6-digit OTP" value={otp} onChange={(event) => setOtp(event.target.value.replace(/\D/g, ""))} disabled={loading} />
              </div>

              {/* TIMER */}
              <div className="text-center text-sm text-muted-foreground">OTP expires in{" "}
                <span className="font-semibold text-foreground">{formattedTime}</span>
              </div>

              <Button type="submit" className="w-full cursor-pointer" disabled={loading} >
                {loading ? (<> <Spinner /> Verifying... </>) : ( "Verify OTP")}
              </Button>

              <button type="button" className="w-full cursor-pointer text-sm text-muted-foreground hover:text-foreground hover:underline" onClick={handleChangeEmail} disabled={loading}> Change Email </button>
            </form>
          )}

          {/* RESET PASSWORD */}

          {step === "password" && (
            <form onSubmit={handleResetPassword} className="space-y-5">
              <div className="space-y-2">
                <Label htmlFor="password">New Password</Label>
                <Input id="password" type="password" placeholder="Enter new password" value={password} onChange={(event) => setPassword(event.target.value)} disabled={loading} />
              </div>

              <div className="space-y-2">
                <Label htmlFor="confirmPassword">Confirm Password</Label>
                <Input id="confirmPassword" type="password" placeholder="Confirm new password" value={confirmPassword} onChange={(event) => setConfirmPassword(event.target.value)} disabled={loading} />
              </div>

              <Button type="submit" className="w-full cursor-pointer" disabled={loading} >
                {loading ? ( <> <Spinner /> Resetting Password... </>) : ("Reset Password")}
              </Button>
            </form>
          )}

          {/*  BACK TO LOGIN */}

          <button type="button" onClick={() => navigate("/login")} className="mt-6 flex w-full cursor-pointer items-center justify-center gap-2 text-sm text-muted hover:text-foreground" disabled={loading} >
            <ArrowLeft className="h-4 w-4" />
            Back to Login
          </button>
        </CardContent>
      </Card>
    </main>
  );
}
