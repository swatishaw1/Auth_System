import { useEffect, useState } from "react";
import { AlertTriangle, ArrowLeft, CheckCircle2, Mail, Trash2, UserRound } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";

import useAuth from "@/auth/store";
import { NavLink, useNavigate } from "react-router";
import { deleteUser, updateUser } from "@/services/AuthService";

function UserProfile() {
  const user = useAuth((state) => state.user);
  const navigate = useNavigate();

  const [name, setName] = useState(user?.name || "");
  const [email, setEmail] = useState(user?.email || "");

  const [isEditing, setIsEditing] = useState(false);
  const [showDelete, setShowDelete] = useState(false);
  const [loading, setLoading] = useState(false);

  // Keep form synchronized with logged-in user
  useEffect(() => {
    setName(user?.name || "");
    setEmail(user?.email || "");
  }, [user]);

  // Save Profile
  const handleSave = async () => {
    if (!user?.id) {
      alert("User not found");
      return;
    }

    if (!name.trim()) {
      alert("Name is required");
      return;
    }

    if (!email.trim()) {
      alert("Email is required");
      return;
    }

    try {
      setLoading(true);

      const updatedUser = await updateUser(user.id, {
        name: name.trim(),
        email: email.trim(),
      });

      // Update Zustand user
      useAuth.setState({
        user: updatedUser,
      });

      setIsEditing(false);
    } catch (error) {
      console.error("Failed to update profile:", error);
      alert("Failed to update profile");
    } finally {
      setLoading(false);
    }
  };

  // Cancel Editing
  const handleCancel = () => {
    setName(user?.name || "");
    setEmail(user?.email || "");
    setIsEditing(false);
  };

  // Delete Account
  const handleDeleteAccount = async () => {
    if (!user?.id) {
      alert("User not found");
      return;
    }

    try {
      setLoading(true);

      await deleteUser(user.id);

      // Clear local user
      useAuth.setState({
        user: null,
      });

      // Logout / clear authentication state
      await useAuth.getState().logout();

      navigate("/login");
    } catch (error) {
      console.error("Failed to delete account:", error);
      alert("Failed to delete account");
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="min-h-[calc(100vh-4rem)] bg-background">
      <div className="mx-auto w-full max-w-5xl px-4 py-6 sm:px-6 sm:py-8 lg:px-8">
        {/* Header*/}
        <NavLink to="/dashboard">
          <Button variant="ghost" className="mb-3 cursor-pointer px-0 hover:bg-transparent" >
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back to Dashboard
          </Button>
        </NavLink>

        <section className="mb-8">
          <h1 className="text-2xl font-bold tracking-tight sm:text-3xl"> My Profile</h1>
          <p className="mt-2 text-sm text-muted-foreground sm:text-base"> Manage your personal information and account settings.</p>
        </section>

        {/* Profile Overview*/}
        <Card className="mb-6">
          <CardContent className="p-5 sm:p-6">
            <div className="flex flex-col gap-6 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <h2 className="text-lg font-semibold"> {user?.name || "User"}
                </h2>
                <p className="text-sm text-muted-foreground"> {user?.email || "No email available"}</p>
                <div className="mt-2 flex items-center gap-2">
                  <span className="flex items-center gap-1.5 text-xs font-medium">
                    <span className="h-2 w-2 rounded-full bg-foreground" /> Active
                  </span>
                  <span className="text-xs text-muted-foreground">•</span>
                  <span className="text-xs text-muted-foreground">Account</span>
                </div>
              </div>

              {!isEditing && (
                <Button variant="outline" className="w-full cursor-pointer sm:w-auto" onClick={() => setIsEditing(true)} >
                  <UserRound className="mr-2 h-4 w-4" />
                  Edit Profile
                </Button>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Personal Information */}
        <Card className="mb-6">
          <CardHeader>
            <CardTitle>Personal Information</CardTitle>
            <CardDescription>Update the information associated with your account.</CardDescription>
          </CardHeader>

          <CardContent className="space-y-6">
            {/* Name */}
            <div className="space-y-2">
              <Label htmlFor="name">Full Name</Label>
              <div className="relative">
                <UserRound className="absolute left-3 top-1/2 h-4 w-4-translate-y-1/2 text-muted-foreground" />
                <Input id="name" value={name} disabled={!isEditing || loading} onChange={(event) => setName(event.target.value)} className="pl-9" placeholder="Enter your name" />
              </div>
            </div>

            {/* Email */}
            <div className="space-y-2">
              <Label htmlFor="email">Email Address</Label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 h-4 w-4-translate-y-1/2 text-muted-foreground" />
                <Input id="email" type="email" value={email} disabled={!isEditing || loading} onChange={(event) => setEmail(event.target.value)} className="pl-9" placeholder="Enter your email" />
              </div>
              <p className="text-xs text-muted-foreground"> Changing your email may require verification.</p>
            </div>

            {/* Save / Cancel */}
            {isEditing && (
              <div className="flex flex-col-reverse gap-2 border-t pt-5 sm:flex-row sm:justify-end">
                <Button variant="outline" className="cursor-pointer" disabled={loading} onClick={handleCancel}> Cancel </Button>

                <Button className="cursor-pointer" disabled={loading} onClick={handleSave} >
                  {loading ? "Saving..." : "Save Changes"}
                </Button>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Account Status */}
        <Card className="mb-6">
          <CardHeader>
            <CardTitle>Account Status</CardTitle>
            <CardDescription> Information about the current state of your account.</CardDescription>
          </CardHeader>

          <CardContent>
            <div className="flex items-center gap-3 rounded-lg border p-4">
              <div className="flex h-10 w-10 items-center justify-center rounded-full bg-muted">
                <CheckCircle2 className="h-5 w-5" />
              </div>

              <div>
                <p className="text-sm font-medium">Account is active</p>
                <p className="text-xs text-muted-foreground">You currently have access to your account resources.</p>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Danger Zone */}
        <Card className="border-destructive/40">
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-destructive">
              <AlertTriangle className="h-5 w-5" />
              Danger Zone
            </CardTitle>

            <CardDescription>Permanent actions that cannot be easily undone.</CardDescription>
          </CardHeader>

          <CardContent>
            {!showDelete ? (
              <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <p className="text-sm font-medium">Delete your account</p>
                  <p className="mt-1 text-xs text-muted-foreground"> This permanently removes your account and associated data.</p>
                </div>

                <Button variant="destructive" className="w-full cursor-pointer sm:w-auto" disabled={loading} onClick={() => setShowDelete(true)} >
                  <Trash2 className="mr-2 h-4 w-4" />
                  Delete Account
                </Button>
              </div>
            ) : (
              <Alert variant="destructive">
                <AlertTriangle className="h-4 w-4" />
                <AlertTitle>Are you absolutely sure?</AlertTitle>
                <AlertDescription>
                  <div className="mt-3 space-y-4">
                    <p>Deleting your account is a permanent action. You will lose access to your account and its resources.</p>
                    <div className="flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
                      <Button variant="outline" className="cursor-pointer" disabled={loading} onClick={() => setShowDelete(false)}> Cancel </Button>

                      <Button variant="destructive" className="cursor-pointer" disabled={loading} onClick={handleDeleteAccount} >
                        <Trash2 className="mr-2 h-4 w-4" />
                        {loading ? "Deleting..." : "Permanently Delete"}
                      </Button>
                    </div>
                  </div>
                </AlertDescription>
              </Alert>
            )}
          </CardContent>
        </Card>
      </div>
    </main>
  );
}

export default UserProfile;
