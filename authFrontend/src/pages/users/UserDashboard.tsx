import { ArrowRight, CheckCircle2, Clock3, LockKeyhole, ShieldCheck, UserRound } from "lucide-react";
import { NavLink } from "react-router";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import useAuth from "@/auth/store";
import ApiClient from "@/configuration/ApiClient";
import { useState } from "react";

function UserDashboard() {
    const user = useAuth((state) => state.user);

    const firstName = user?.name?.split(" ")[0] || "User";
    const initial = user?.name?.charAt(0).toUpperCase() || "U";
    const [apiResponse, setApiResponse] = useState<any>(null);
    const handleTestAuth = async () => {
        try {
            const response = await ApiClient.get(`/users/byEmail/${user?.email}`);
            console.log("Protected API response:", response.data);
            setApiResponse(response.data);
        } catch (error) {
            console.error("Protected API failed:", error);
            setApiResponse(null);
        }
    };
  

  return (
    <main className="min-h-[calc(100vh-4rem)] bg-background">
      <div className="mx-auto w-full max-w-7xl px-4 py-6 sm:px-6 sm:py-8 lg:px-8">

        {/* Welcome Section */}
        <section className="mb-8">
          <div className="flex flex-col gap-5 sm:flex-row sm:items-center sm:justify-between">
            <div className="flex items-center gap-4">
              <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-primary text-lg font-semibold text-primary-foreground"> {initial} </div>
              <div> <p className="text-sm text-muted-foreground"> Welcome back </p>
                <h1 className="text-2xl font-bold tracking-tight sm:text-3xl"> Hello, {firstName} </h1>
              </div>
            </div>

            <NavLink to="/dashboard/profile">
              <Button variant="outline" className="w-full cursor-pointer sm:w-auto">
                <UserRound className="mr-2 h-4 w-4" />
                View Profile
              </Button>
            </NavLink>
          </div>

          <p className="mt-4 max-w-2xl text-sm leading-6 text-muted-foreground sm:text-base">
            Manage your account, review your security status, and access
            everything available to you from your dashboard.
          </p>
        </section>

        {/* Overview Cards */}
        <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {/* Account */}
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-3">
              <CardTitle className="text-sm font-medium"> Account </CardTitle>
              <UserRound className="h-4 w-4 text-muted-foreground" />
            </CardHeader>

            <CardContent>
              <div className="text-lg font-semibold">Active</div>

              <div className="mt-1 flex items-center gap-1.5 text-xs text-muted-foreground">
                <CheckCircle2 className="h-3.5 w-3.5" />
                Your account is active
              </div>
            </CardContent>
          </Card>

          {/* Security */}
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-3">
              <CardTitle className="text-sm font-medium"> Security </CardTitle>
              <ShieldCheck className="h-4 w-4 text-muted-foreground" />
            </CardHeader>

            <CardContent>
              <div className="text-lg font-semibold">Protected</div>

              <div className="mt-1 flex items-center gap-1.5 text-xs text-muted-foreground">
                <LockKeyhole className="h-3.5 w-3.5" />
                Authentication is enabled
              </div>
            </CardContent>
          </Card>

          {/* Session */}
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-3">
              <CardTitle className="text-sm font-medium">Session</CardTitle>
              <Clock3 className="h-4 w-4 text-muted-foreground" />
            </CardHeader>

            <CardContent>
              <div className="text-lg font-semibold">Active session</div>
              <div className="mt-1 text-xs text-muted-foreground"> You are currently signed in </div>
            </CardContent>
          </Card>
        </section>

        {/* Main Dashboard Content */}
        <section className="mt-6 grid gap-6 lg:grid-cols-3">
          {/* Quick Actions */}
          <Card className="lg:col-span-2">
            <CardHeader>
              <CardTitle>Quick Actions</CardTitle>
              <p className="text-sm text-muted-foreground"> Manage your account and access important features.</p>
            </CardHeader>
            <CardContent>
              <div className="grid gap-3 sm:grid-cols-2">
                <NavLink to="/dashboard/profile">
                  <div className="group flex cursor-pointer items-center justify-between rounded-lg border p-4 transition-colors hover:bg-muted/50">
                    <div className="flex items-center gap-3">
                      <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-muted">
                        <UserRound className="h-5 w-5" />
                      </div>

                      <div>
                        <p className="text-sm font-medium"> My Profile </p>
                        <p className="text-xs text-muted-foreground"> View and manage your profile </p>
                      </div>
                    </div>
                    <ArrowRight className="h-4 w-4 text-muted-foreground transition-transform group-hover:translate-x-1" />
                  </div>
                </NavLink>

                <NavLink to="/dashboard">
                  <div className="group flex cursor-pointer items-center justify-between rounded-lg border p-4 transition-colors hover:bg-muted/50">
                    <div className="flex items-center gap-3">
                      <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-muted">
                        <ShieldCheck className="h-5 w-5" />
                      </div>

                      <div>
                        <p className="text-sm font-medium"> Security </p>
                        <p className="text-xs text-muted-foreground"> Review your account security </p>
                      </div>
                    </div>
                    <ArrowRight className="h-4 w-4 text-muted-foreground transition-transform group-hover:translate-x-1" />
                  </div>
                </NavLink>
              </div>
            </CardContent>
          </Card>

          {/* Account Summary */}
          <Card>
            <CardHeader>
              <CardTitle>Account Summary</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div>
                <p className="text-xs text-muted-foreground">Name</p>
                <p className="mt-1 text-sm font-medium">{user?.name || "Not available"}</p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Email</p>
                <p className="mt-1 break-all text-sm font-medium"> {user?.email || "Not available"} </p>
              </div>

              <div>
                <p className="text-xs text-muted-foreground"> Account Status </p>
                <div className="mt-1 flex items-center gap-2">
                  <span className="h-2 w-2 rounded-full bg-foreground" />
                  <span className="text-sm font-medium">Active</span>
                </div>
              </div>
            </CardContent>
          </Card>
        </section>

        {/* Security Notice */}
        <section className="mt-6">
          <Card>
            <CardContent className="flex flex-col gap-4 p-5 sm:flex-row sm:items-center sm:justify-between sm:p-6">
              <div className="flex gap-3">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-muted">
                  <LockKeyhole className="h-5 w-5" />
                </div>

                <div>
                  <h2 className="text-sm font-semibold">Your account is protected</h2>
                  <p className="mt-1 text-xs leading-5 text-muted-foreground sm:text-sm">
                    Authentication and authorization controls protect access
                    to your account and tenant resources.
                  </p>
                </div>
              </div>

              <NavLink to="/dashboard/profile">
                <Button variant="outline" size="sm" className="w-full cursor-pointer sm:w-auto" >Manage Account</Button>
              </NavLink>
            </CardContent>
          </Card>
        </section>

      </div>
      <div className="mx-auto flex w-full max-w-5xl justify-center px-4 py-6 sm:px-6 lg:px-8">
        <Button variant="outline" className="cursor-pointer gap-2 rounded-lg border-primary/30 bg-primary/5 px-4 py-2 font-medium text-primary transition-all hover:border-primary hover:bg-primary hover:text-primary-foreground" onClick={handleTestAuth}>
          <ShieldCheck className="h-4 w-4" />
          Test Protected API
        </Button>
      </div>
      {apiResponse && (
        <div className="mt-6 rounded-lg border bg-muted/30 p-4">
          <p className="mb-3 text-sm font-medium">API Response</p>
          <pre className="max-h-64 overflow-auto rounded-md bg-background p-3 text-xs text-muted-foreground">
            {JSON.stringify(apiResponse, null, 2)}
          </pre>
        </div>
      )}
    </main>
  );
}

export default UserDashboard;