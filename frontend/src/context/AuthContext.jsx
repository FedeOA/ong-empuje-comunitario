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
        setUser({ username: decoded.username, role: decoded.role });
      } catch (err) {
        console.error("Token inválido:", err);
        localStorage.removeItem("token");
      }
    } else {
      localStorage.removeItem("token"); // limpiar token mal formado
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


    const { token } = await res.json();
    localStorage.setItem("token", token);

    const decoded = jwtDecode(token);
    setUser({ username: decoded.username, role: decoded.role });
    setLoading(false);

    return decoded;
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
