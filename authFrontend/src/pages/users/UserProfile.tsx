import { useEffect, useRef, useState } from "react";
import { AlertTriangle, ArrowLeft, CheckCircle2, ImagePlus, Mail, Shield, Trash2, Upload, UserRound, X,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Alert, AlertDescription, AlertTitle,
} from "@/components/ui/alert";
import useAuth from "@/auth/store";
import { NavLink } from "react-router";

function UserProfile() {
  const user = useAuth(state => state.user);

  const fileInputRef = useRef<HTMLInputElement>(null);

  const [name, setName] = useState(user?.name || "");
  const [email, setEmail] = useState(user?.email || "");

  const [profileImage, setProfileImage] = useState<string | null>(
    user?.profileImageUrl || null
  );

  const [selectedImage, setSelectedImage] = useState<File | null>(null);

  const [isEditing, setIsEditing] = useState(false);
  const [showDelete, setShowDelete] = useState(false);

  useEffect(() => {
    setName(user?.name || "");
    setEmail(user?.email || "");
    setProfileImage(user?.profileImageUrl || null);
  }, [user]);

  const initial = user?.name?.charAt(0).toUpperCase() || "U";

  const handleImageSelect = (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    const file = event.target.files?.[0];

    if (!file) return;

    if (!file.type.startsWith("image/")) {
      alert("Please select an image file.");
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      alert("Image size must be less than 5MB.");
      return;
    }

    setSelectedImage(file);

    const imageUrl = URL.createObjectURL(file);
    setProfileImage(imageUrl);

    setIsEditing(true);
  };

  const handleRemoveImage = () => {
    setProfileImage(null);
    setSelectedImage(null);

    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }

    setIsEditing(true);
  };

  const handleSave = () => {
    console.log("Updated profile:", {
      name,
      email,
      profileImage: selectedImage,
    });

    // API call will be added later.

    setIsEditing(false);
  };

  const handleCancel = () => {
    setName(user?.name || "");
    setEmail(user?.email || "");
    setProfileImage(user?.profileImageUrl || null);
    setSelectedImage(null);

    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }

    setIsEditing(false);
  };

  const handleDeleteAccount = () => {
    // Delete account API will be added later.
    console.log("Delete account");
  };

  return (
    <main className="min-h-[calc(100vh-4rem)] bg-background">

      <div className="mx-auto w-full max-w-5xl px-4 py-6 sm:px-6 sm:py-8 lg:px-8">

        {/* Header */}
        <NavLink to="/dashboard">
            <Button variant="ghost" className="mb-3 cursor-pointer px-0 hover:bg-transparent" >
                <ArrowLeft className="mr-2 h-4 w-4" />
                Back to Dashboard
            </Button>
        </NavLink>
        <section className="mb-8">
          <h1 className="text-2xl font-bold tracking-tight sm:text-3xl"> My Profile </h1>
          <p className="mt-2 text-sm text-muted-foreground sm:text-base"> Manage your personal information and account settings. </p>
        </section>


        {/* Profile Overview */}
        <Card className="mb-6">
          <CardContent className="p-5 sm:p-6">
            <div className="flex flex-col gap-6 sm:flex-row sm:items-center sm:justify-between">
              <div className="flex items-center gap-4">

                {/* Profile Image */}
                <div className="relative shrink-0">
                  {profileImage ? (
                    <img src={profileImage} alt="Profile" className="h-20 w-20 rounded-full object-cover ring-2 ring-border"/>
                  ) : (
                    <div className="flex h-20 w-20 items-center justify-center rounded-full bg-primary text-2xl font-semibold text-primary-foreground"> {initial} </div>
                  )}
                  {isEditing && (
                    <button type="button" onClick={() => fileInputRef.current?.click()} className="absolute bottom-0 right-0 flex h-7 w-7 cursor-pointer items-center justify-center rounded-full border bg-background shadow-sm transition-colors hover:bg-muted" title="Change profile picture" >
                      <ImagePlus className="h-4 w-4" />
                    </button>
                  )}

                </div>


                {/* User Info */}
                <div>
                  <h2 className="text-lg font-semibold"> {user?.name || "User"}</h2>
                  <p className="text-sm text-muted-foreground"> {user?.email || "No email available"}</p>
                  <div className="mt-2 flex flex-wrap items-center gap-2">
                    <span className="flex items-center gap-1.5 text-xs font-medium">
                      <span className="h-2 w-2 rounded-full bg-foreground" /> Active
                    </span>
                    <span className="text-xs text-muted-foreground">
                      •
                    </span>
                    {/* <span className="text-xs text-muted-foreground">
                      {user?.role || "USER"}
                    </span> */}
                  </div>
                </div>
              </div>


              {/* Profile Image Actions */}
              {isEditing && (
                <div className="flex flex-col gap-2 sm:items-end">
                  <input ref={fileInputRef} type="file" accept="image/*" onChange={handleImageSelect} className="hidden"/>

                  <Button type="button" variant="outline" className="w-full cursor-pointer sm:w-auto" onClick={() => fileInputRef.current?.click()}>
                    <Upload className="mr-2 h-4 w-4" />
                    Change Photo
                  </Button>

                  {profileImage && (
                    <Button type="button" variant="ghost" className="w-full cursor-pointer text-destructive hover:text-destructive sm:w-auto" onClick={handleRemoveImage} >
                      <X className="mr-2 h-4 w-4" />
                      Remove Photo
                    </Button>
                  )}
                </div>
              )}

              {!isEditing && (
                <Button variant="outline" className="w-full cursor-pointer sm:w-auto" onClick={() => setIsEditing(true)}>
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
            <CardTitle> Personal Information </CardTitle>
            <CardDescription> Update the information associated with your account.</CardDescription>
          </CardHeader>


          <CardContent className="space-y-6">
            {/* Name */}
            <div className="space-y-2">
              <Label htmlFor="name"> Full Name </Label>
              <div className="relative">
                <UserRound className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input id="name" value={name} disabled={!isEditing} onChange={event => setName(event.target.value)} className="pl-9" placeholder="Enter your name"/>
              </div>
            </div>


            {/* Email */}
            <div className="space-y-2">
              <Label htmlFor="email"> Email Address</Label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input id="email" type="email" value={email} disabled={!isEditing} onChange={event => setEmail(event.target.value)} className="pl-9" placeholder="Enter your email" />
              </div>
              <p className="text-xs text-muted-foreground"> Changing your email may require verification.</p>

            </div>


            {/* Role */}
            {/* <div className="space-y-2">
              <Label>Role</Label>
              <div className="flex items-center gap-3 rounded-lg border bg-muted/30 p-3">
                <Shield className="h-4 w-4 text-muted-foreground" />
                <div>
                  <p className="text-sm font-medium">{user?.role || "USER"}</p>
                  <p className="text-xs text-muted-foreground">Your role is managed by the system administrator.</p>
                </div>
              </div>
            </div> */}

            {/* Tenant */}
            {/* <div className="space-y-2">
              <Label>Tenant</Label>
              <div className="flex items-center gap-3 rounded-lg border bg-muted/30 p-3">
                <div className="flex h-8 w-8 items-center justify-center rounded-md bg-background text-xs font-semibold">T</div>
                <div> <p className="text-sm font-medium"> {user?.tenantId || "Current Tenant"} </p>
                  <p className="text-xs text-muted-foreground"> Your tenant is managed by the system.</p>
                </div>
              </div>
            </div> */}


            {/* Save / Cancel */}
            {isEditing && (
              <div className="flex flex-col-reverse gap-2 border-t pt-5 sm:flex-row sm:justify-end">
                <Button variant="outline" className="cursor-pointer" onClick={handleCancel}> Cancel</Button>
                <Button className="cursor-pointer" onClick={handleSave} > Save Changes</Button>
              </div>
            )}

          </CardContent>

        </Card>


        {/* Account Status */}
        <Card className="mb-6">
          <CardHeader>
            <CardTitle> Account Status </CardTitle>
            <CardDescription> Information about the current state of your account. </CardDescription>
          </CardHeader>

          <CardContent>
            <div className="flex items-center gap-3 rounded-lg border p-4">
              <div className="flex h-10 w-10 items-center justify-center rounded-full bg-muted">
                <CheckCircle2 className="h-5 w-5" />
              </div>

              <div>
                <p className="text-sm font-medium"> Account is active </p>
                <p className="text-xs text-muted-foreground"> You currently have access to your account and tenant resources.
                </p>
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
            <CardDescription> Permanent actions that cannot be easily undone. </CardDescription>
          </CardHeader>


          <CardContent>
            {!showDelete ? (
              <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <p className="text-sm font-medium"> Delete your account </p>
                  <p className="mt-1 text-xs text-muted-foreground"> This permanently removes your account and associated data according to the application's retention policy. </p>
                </div>

                <Button variant="destructive" className="w-full cursor-pointer sm:w-auto" onClick={() => setShowDelete(true)} >
                  <Trash2 className="mr-2 h-4 w-4" />
                  Delete Account
                </Button>
              </div>

            ) : (

              <Alert variant="destructive">
                <AlertTriangle className="h-4 w-4" />
                <AlertTitle>
                  Are you absolutely sure?
                </AlertTitle>
                <AlertDescription>
                  <div className="mt-3 space-y-4">
                    <p> Deleting your account is a permanent action. You will lose access to your account and its resources. </p>
                    <div className="flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
                      <Button variant="outline" className="cursor-pointer" onClick={() => setShowDelete(false)}> Cancel
                      </Button>
                      <Button variant="destructive" className="cursor-pointer" onClick={handleDeleteAccount} >
                        <Trash2 className="mr-2 h-4 w-4" />
                        Permanently Delete
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