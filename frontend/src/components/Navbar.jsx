import React from "react";
import logo from "../assets/logo.png"; // Ajusta según tu logo

export default function Navbar() {
  return (
    <nav className="bg-empuje-navbar text-empuje-dark w-64 min-h-screen p-6 flex flex-col">
      {/* Logo */}
      <div className="flex items-center space-x-3 mb-10">
        <img src={logo} alt="Empuje Comunitario" className="w-100 h-100" />
      </div>

      {/* Links */}
      <a href="/users" className="px-3 py-2 rounded hover:bg-empuje-green transition">
        Gestión de usuarios
      </a>
      <a href="/donations" className="px-3 py-2 rounded hover:bg-empuje-blue transition mt-2">
        Inventario
      </a>
      <a href="/events" className="px-3 py-2 rounded hover:bg-empuje-orange transition mt-2">
        Gestión de eventos
      </a>
    </nav>
  );
}
