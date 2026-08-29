import { ArrowRight, CheckCircle2 } from "lucide-react";
import { Button } from "../components/ui/button";
import { Card, CardContent } from "../components/ui/card";
import { features, securityPoints } from "../constants/landing";
import { useNavigate } from "react-router";

export default function AuthHomePage() {
    const navigate = useNavigate();
    const handleLearnMore = () => {
        document.getElementById("features")?.scrollIntoView({
            behavior: "smooth",
        });
    };

    return (
        <main className="min-h-screen overflow-hidden bg-background text-foreground">
            {/* Background Effects */}
            <div className="absolute inset-0 -z-10">
                <div className="absolute left-1/2 top-0 h-[50px] w-[50px] -translate-x-1/2 rounded-full bg-slate-500/10 blur-3xl" />
                <div className="absolute bottom-0 right-0 h-[40px] w-[40px] rounded-full bg-slate-400/10 blur-3xl" />
            </div>

            {/* Hero Section */}
            <section className="relative flex min-h-[80vh] items-center justify-center px-4 py-20 md:min-h-[90vh] md:px-6 md:py-24">
                <div className="mx-auto flex max-w-4xl flex-col items-center text-center">
                    <div className="mb-6 inline-flex items-center rounded-full border border-border bg-muted px-4 py-2 text-sm text-muted-foreground backdrop-blur"> Next Generation Authentication Platform </div>
                    <h1 className="text-5xl font-black leading-tight tracking-tight md:text-7xl"> Secure Access
                        <span className="block bg-gradient-to-r from-foreground to-muted-foreground bg-clip-text text-transparent"> For Modern Apps </span>
                    </h1>
                    <p className="mt-6 max-w-2xl text-lg leading-relaxed text-muted-foreground">A secure authentication system built using React.js, TypeScript, Spring Boot, JWT, and OAuth2 for modern web applications.
                    </p>
                    <div className="mt-10 flex flex-wrap justify-center gap-4">
                        <Button size="lg" className="bg-foreground font-semibold text-background hover:bg-foreground/90" onClick={() => navigate("/login")}>
                            Get Started<ArrowRight className="ml-2 h-4 w-4" />
                        </Button>
                        <Button variant="outline" size="lg" onClick={handleLearnMore}>Learn More</Button>
                    </div>
                    <div className="mt-10 flex flex-wrap justify-center gap-6 text-sm text-muted-foreground">
                        <div className="flex items-center gap-2">
                            <CheckCircle2 className="h-4 w-4 text-foreground" /> Secure JWT Flow </div>
                        <div className="flex items-center gap-2">
                            <CheckCircle2 className="h-4 w-4 text-foreground" /> OAuth2 Integration </div>
                    </div>
                </div>
            </section>

            {/* Features */}
            <section id="features" className="border-t border-border px-6 py-24">
                <div className="mx-auto max-w-7xl">
                    <div className="mx-auto flex flex-col items-center text-center">
                        <h2 className="text-4xl font-black md:text-5xl">Built For Modern Authentication</h2>
                        <p className="mt-4 text-lg text-muted-foreground">Designed with scalability, security and performance focused architecture principles.</p>
                    </div>
                    <div className="mt-16 grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                        {features.map((feature) => {
                            const Icon = feature.icon;
                            return (
                                <Card key={feature.title} className="group border-border bg-card transition-all duration-300 hover:border-muted-foreground/20" >
                                    <CardContent className="p-8">
                                        <div className="mb-5 inline-flex rounded-2xl bg-muted p-4 text-foreground"> <Icon className="h-7 w-7" /> </div>
                                        <h3 className="text-2xl font-bold"> {feature.title} </h3>
                                        <p className="mt-4 leading-relaxed text-muted-foreground"> {feature.description} </p>
                                    </CardContent>
                                </Card>
                            );
                        })}
                    </div>
                </div>
            </section>

            {/* Architecture */}
            <section className="border-t border-border px-6 py-24">
                <div className="mx-auto grid max-w-7xl items-center gap-16 lg:grid-cols-2">
                    <div>
                        <h2 className="text-4xl font-black md:text-5xl"> System Architecture</h2>
                        <p className="mt-6 text-lg leading-relaxed text-muted-foreground">
                            The application follows a scalable architecture
                            using React.js frontend, Spring Boot backend APIs
                            and JWT authentication flow.
                        </p>
                        <div className="mt-10 space-y-4">
                            {securityPoints.map((point) => (
                                <div key={point} className="flex items-center gap-3 rounded-xl border border-border bg-card p-4">
                                    <CheckCircle2 className="h-5 w-5 text-foreground" />
                                    <span>{point}</span>
                                </div>
                            ))}
                        </div>
                    </div>

                    <div className="rounded-3xl border border-border/60 bg-muted/20 p-8 shadow-sm">
                        <div className="space-y-5">
                            <div className="rounded-2xl border border-border/70 bg-background p-5 shadow-sm">
                                <h3 className="font-semibold"> Frontend Layer </h3>
                                <p className="mt-2 text-sm text-muted-foreground"> React.js + TypeScript + ShadCN UI </p>
                            </div>
                            <div className="flex justify-center">
                                <div className="h-10 w-px bg-muted-foreground/40" />
                            </div>
                            <div className="rounded-2xl border border-border/70 bg-background p-5 shadow-sm">
                                <h3 className="font-semibold"> API Gateway </h3>
                                <p className="mt-2 text-sm text-muted-foreground"> Authentication Middleware</p>
                            </div>
                            <div className="flex justify-center">
                                <div className="h-10 w-px bg-muted-foreground/40" />
                            </div>
                            <div className="rounded-2xl border border-border/70 bg-background p-5 shadow-sm">
                                <h3 className="font-semibold"> Backend Services </h3>
                                <p className="mt-2 text-sm text-muted-foreground"> Spring Boot Microservices + JWT </p>
                            </div>
                            <div className="flex justify-center">
                                <div className="h-10 w-px bg-muted-foreground/40" />
                            </div>
                            <div className="rounded-2xl border border-border/70 bg-background p-5 shadow-sm">
                                <h3 className="font-semibold"> Database Layer </h3>
                                <p className="mt-2 text-sm text-muted-foreground"> MySQL</p>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            {/* CTA */}
            <section className="px-6 py-28">
                <div className="mx-auto max-w-5xl rounded-[40px] border border-border bg-card p-12 text-center">
                    <h2 className="text-4xl font-black leading-tight md:text-6xl"> Ready To Build
                        <span className="block bg-gradient-to-r from-foreground to-muted-foreground bg-clip-text text-transparent"> Secure Authentication? </span>
                    </h2>
                    <p className="mx-auto mt-6 max-w-2xl text-lg text-muted-foreground">
                        Production-ready authentication infrastructure with
                        modern security standards and scalable architecture.
                    </p>
                    <div className="mt-10 flex flex-wrap justify-center gap-4">
                        <Button size="lg" className="bg-foreground font-semibold text-background hover:bg-foreground/90" onClick={() => navigate("/login")}>
                            Get Started
                        </Button>
                        <Button variant="outline" size="lg" onClick={handleLearnMore}> Learn More </Button>
                    </div>
                </div>
            </section>
        </main>
    );
}