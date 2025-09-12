import React, { createContext, useContext, useState, useEffect } from "react";

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);       // Usuario logueado
  const [loading, setLoading] = useState(true); // Mientras “carga” la sesión

  // Simula la carga de un usuario al montar el provider
  const fetchUser = async () => {
    setLoading(true);
    // Simulación de delay
    setTimeout(() => {
      // Usuario simulado
      setUser({ id: 1, nombre: "Federico", role: "presidente" });
      setLoading(false);
    }, 500); // medio segundo de “loading”
  };

  useEffect(() => {
    fetchUser();
  }, []);

  // Login simulado
  const login = async (email, password) => {
    setLoading(true);
    return new Promise((resolve) => {
      setTimeout(() => {
        const fakeUser = { id: 1, nombre: "Federico", role: "vocal" };
        setUser(fakeUser);
        setLoading(false);
        resolve(fakeUser);
      }, 500);
    });
  };

  // Logout
  const logout = () => {
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, fetchUser }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
