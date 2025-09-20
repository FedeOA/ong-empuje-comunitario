import React from "react";
import { useAuth } from "../hooks/useAuth";
import LoadingSpinner from "../components/LoadingSpinner";

const Dashboard = () => {
  const { user, loading, logout } = useAuth();

  if (loading) return <LoadingSpinner />;
  if (!user) return <p>No hay usuario autenticado</p>;

  return (
    <div>
      <h1>Bienvenido, {user.username}</h1>
      <p>Rol: {user.role}</p>
      <button
        className="bg-red-500 text-white px-3 py-1 rounded"
        onClick={logout}
      >
        Cerrar sesión
      </button>
    </div>
  );
};

export default Dashboard;
