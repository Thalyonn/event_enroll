import React, { createContext, useContext, useEffect, useState } from "react";

interface AuthContextType {
    isAuthenticated: boolean;
    isAdmin: boolean;
    username: string | null;
    loading: boolean;
    refreshAuth: () => Promise<void>;
    login: (username: string, password: string) => Promise<void>;
    logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType>({
    isAuthenticated: false,
    isAdmin: false,
    username: null,
    loading: true,
    refreshAuth: async () => {},
    login: async () => {},
    logout: async () => {},
});

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [isAdmin, setIsAdmin] = useState(false);
    const [username, setUsername] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);

    const fetchAuth = async () => {
        try {
            console.log("attempting auth fetch");
            const res = await fetch("http://localhost:8080/api/auth/me", {
                credentials: "include", 
            });

            if (res.ok) {
                console.log("auth fetch success");
                const data = await res.json();
                setIsAuthenticated(true);
                setUsername(data.username);
                setIsAdmin(
                data.roles.some((role: any) => role.authority === "ROLE_ADMIN")
                );
            } else {
                console.log("auth fetch fail");
                setIsAuthenticated(false);
                setIsAdmin(false);
                setUsername(null);
            }
            } catch (err) {
                setIsAuthenticated(false);
                setIsAdmin(false);
                setUsername(null);
            } finally {
                setLoading(false);
            }
    };

    const login = async (username: string, password: string) => {
        const res = await fetch("http://localhost:8080/api/auth/login", {
            method: "POST",
            credentials: "include",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password }),
        });

        if (!res.ok) throw new Error("Login failed");

        await fetchAuth(); 
    };

    const logout = async () => {
        await fetch("http://localhost:8080/api/auth/logout", {
            method: "POST",
            credentials: "include",
        });

            
        setIsAuthenticated(false);
        setIsAdmin(false);
        setUsername(null);
    };

    

    useEffect(() => {
        fetchAuth();
    }, []);

    return (
        <AuthContext.Provider
        value={{
            isAuthenticated,
            isAdmin,
            username,
            loading,
            refreshAuth: fetchAuth,
            login,
            logout,
        }}
        >
        {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);
