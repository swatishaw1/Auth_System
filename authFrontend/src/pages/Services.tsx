import { services } from "@/constants/landing"

function Services() {

  return (
    <section className="w-full px-6 py-20">
      <div className="mx-auto max-w-6xl">
        <div className="mb-12 text-center">
          <p className="mb-2 text-sm font-semibold uppercase tracking-wider text-primary">Services</p>
          <h2 className="text-3xl font-bold tracking-tight sm:text-4xl">Everything you need for secure authentication</h2>
          <p className="mx-auto mt-4 max-w-2xl text-muted-foreground">
            A full-stack authentication system designed to provide secure,
            reliable, and seamless user access.
          </p>
        </div>

        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {services.map((service) => {const Icon = service.icon
            return (
              <div key={service.title} className="rounded-2xl border bg-card p-6 shadow-sm transition-all duration-300 hover:-translate-y-1 hover:shadow-lg" >
                <div className="mb-5 flex h-12 w-12 items-center justify-center rounded-xl bg-primary/10">
                  <Icon className="h-6 w-6 text-primary" />
                </div>
                <h3 className="mb-2 text-xl font-semibold"> {service.title}</h3>
                <p className="leading-relaxed text-muted-foreground"> {service.description}</p>
              </div>
            )
          })}
        </div>
      </div>
    </section>
  )
}

export default Services