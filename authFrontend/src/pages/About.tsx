import { ShieldCheck, Server, Database, Code2 } from "lucide-react"

function About() {
  return (
    <section className="w-full px-6 py-20 sm:py-24">
      <div className="mx-auto max-w-6xl">
        <div className="grid items-center gap-12 lg:grid-cols-2 lg:gap-20">
          {/* Left Content */}
          <div>
            <p className="mb-3 text-sm font-semibold uppercase tracking-wider text-primary">About</p>
            <h2 className="text-3xl font-bold tracking-tight sm:text-4xl lg:text-5xl">Authentication built for modern applications</h2>
            <p className="mt-6 text-base leading-7 text-muted-foreground sm:text-lg">
              This project is a full-stack authentication system designed
              to provide secure and reliable user authentication for
              modern web applications.
            </p>
            <p className="mt-4 text-base leading-7 text-muted-foreground sm:text-lg">
              It combines a modern frontend with a secure backend to handle
              authentication, protected resources, user sessions, and
              database operations in one complete system.
            </p>

            <div className="mt-8 flex flex-wrap gap-3">
              <span className="rounded-full border bg-muted/50 px-4 py-2 text-sm font-medium"> Full Stack </span>
              <span className="rounded-full border bg-muted/50 px-4 py-2 text-sm font-medium"> Secure Authentication </span>
              <span className="rounded-full border bg-muted/50 px-4 py-2 text-sm font-medium"> REST API </span>
            </div>
          </div>

          {/* Right - Highlights */}
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="rounded-2xl border bg-card p-6 shadow-sm">
              <div className="mb-4 flex h-11 w-11 items-center justify-center rounded-xl bg-primary/10">
                <ShieldCheck className="h-5 w-5 text-primary" />
              </div>
              <h3 className="font-semibold"> Security First </h3>

              <p className="mt-2 text-sm leading-6 text-muted-foreground">
                Secure authentication and session handling designed to
                protect user access.
              </p>
            </div>

            <div className="rounded-2xl border bg-card p-6 shadow-sm">
              <div className="mb-4 flex h-11 w-11 items-center justify-center rounded-xl bg-primary/10">
                <Server className="h-5 w-5 text-primary" />
              </div>

              <h3 className="font-semibold"> Full Stack Architecture </h3>

              <p className="mt-2 text-sm leading-6 text-muted-foreground"> Frontend and backend work together through secure APIs. </p>
            </div>

            <div className="rounded-2xl border bg-card p-6 shadow-sm">
              <div className="mb-4 flex h-11 w-11 items-center justify-center rounded-xl bg-primary/10">
                <Database className="h-5 w-5 text-primary" />
              </div>

              <h3 className="font-semibold"> Database Driven </h3>

              <p className="mt-2 text-sm leading-6 text-muted-foreground"> User and application data are persisted through a structured database layer. </p>
            </div>

            <div className="rounded-2xl border bg-card p-6 shadow-sm">
              <div className="mb-4 flex h-11 w-11 items-center justify-center rounded-xl bg-primary/10">
                <Code2 className="h-5 w-5 text-primary" />
              </div>

              <h3 className="font-semibold"> Developer Focused </h3>

              <p className="mt-2 text-sm leading-6 text-muted-foreground">
                Built with a clean architecture that can be extended for
                real-world applications.
              </p>
            </div>
          </div>

        </div>
      </div>
    </section>
  )
}

export default About;