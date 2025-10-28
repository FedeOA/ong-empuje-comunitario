import React, { createContext, useContext, useState, useEffect } from "react";
import { jwtDecode } from "jwt-decode";
import { baseUrl } from "../constants/constants.js";

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (token && typeof token === "string" && token.split(".").length === 3) {
      try {
        const decoded = jwtDecode(token);
        setUser({ username: decoded.sub, role: decoded.role });
      } catch (err) {
        console.error("Token inválido:", err);
        localStorage.removeItem("token");
      }
    } else {
      localStorage.removeItem("token");
    }
    setLoading(false);
  }, []);


  const login = async (usernameOrEmail, password) => {
    setLoading(true);
    const res = await fetch(`${baseUrl}/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ usernameOrEmail, password }),
    });
    if (!res.ok) {
      const errorData = await res.json();
      setLoading(false);
      throw new Error(errorData.message || "Credenciales inválidas");
    }
    const data = await res.json();
    if (!data.token || typeof data.token !== 'string' || data.token === 'Empty') {
      setLoading(false);
      throw new Error(data.message || 'Token inválido del servidor');
    }
    localStorage.setItem("token", data.token);
    try {
      const decoded = jwtDecode(data.token);
      setUser({
        username: decoded.sub,
        role: decoded.role,
        organizationId: decoded.orgId || 0
      });
      console.log("username: ", decoded.sub);
      console.log("role: ", decoded.role);
      console.log("organization id: ", decoded.orgId);
    } catch (err) {
      console.error("Token decode error:", err);
      localStorage.removeItem("token");
      throw new Error("Token inválido");
    }
    setLoading(false);
    return data;
  };

  const logout = () => {
    localStorage.removeItem("token");
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
