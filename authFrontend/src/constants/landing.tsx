import {
    ShieldCheck,
    UserCog,
    KeyRound,
    Building2,
    Layers3,
    ShieldEllipsis,
} from "lucide-react";

export const features = [
    {
        icon: ShieldCheck,
        title: "JWT Authentication",
        description:
            "Secure authentication system using JWT access and refresh tokens.",
    },
    {
        icon: UserCog,
        title: "Role-Based Access",
        description:
            "Protected APIs with role-based authorization and secured routes.",
    },
    {
        icon: KeyRound,
        title: "OAuth2 Login",
        description:
            "Google and GitHub authentication integration using OAuth2.",
    },
    {
        icon: Building2,
        title: "Multi-Tenant Architecture",
        description:
            "Structured backend design supporting scalable tenant-based systems.",
    },
    {
        icon: Layers3,
        title: "REST API Optimization",
        description:
            "Pagination, filtering, and optimized API responses for better performance.",
    },
    {
        icon: ShieldEllipsis,
        title: "Secure Session Handling",
        description:
            "HTTP-only cookies and secure token management for safer sessions.",
    },
];

export const securityPoints = [
    "Role Based Access Control (RBAC)",
    "JWT + Refresh Token Strategy",
    "OAuth2 & Social Login Support",
    "Encrypted Credentials Handling",
];