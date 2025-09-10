import React from "react";
import { Link } from "react-router-dom";

const Forbidden = () => {
  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-empuje-bg text-center p-6">
      <h1 className="text-6xl font-bold text-red-600 mb-4">403</h1>
      <h2 className="text-3xl font-semibold mb-2">Acceso Prohibido</h2>
      <p className="text-gray-300 mb-6">
        No tienes permisos para ver esta página.
      </p>
      <Link
        to="/dashboard"
        className="bg-empuje-green text-white px-6 py-2 rounded-lg hover:bg-green-700 transition"
      >
        Volver al Dashboard
      </Link>
    </div>
  );
};

export default Forbidden;
