import React, { useState } from "react";
import UsersPage from "./Users";
import EventsPage from "./Events";
import DonationsPage from "./Donations";

export default function Home() {
  const [activeSection, setActiveSection] = useState("users");

  const renderSection = () => {
    switch (activeSection) {
      case "users":
        return <UsersPage />;
      case "events":
        return <EventsPage />;
      case "donations":
        return <DonationsPage />;
      default:
        return <UsersPage />;
    }
  };

  return (
    <div className="flex min-h-screen bg-empuje-bg">
      {/* Sidebar */}
      <aside className="w-64 bg-white shadow-md p-6 flex flex-col">
        <h2 className="text-2xl font-bold text-empuje-green mb-6">
          Dashboard
        </h2>
        <nav className="flex flex-col gap-3">
          <button
            className={`text-left px-3 py-2 rounded ${
              activeSection === "users" ? "bg-empuje-green text-white" : "text-gray-700"
            }`}
            onClick={() => setActiveSection("users")}
          >
            Gestión de Usuarios
          </button>
          <button
            className={`text-left px-3 py-2 rounded ${
              activeSection === "events" ? "bg-empuje-green text-white" : "text-gray-700"
            }`}
            onClick={() => setActiveSection("events")}
          >
            Gestión de Eventos
          </button>
          <button
            className={`text-left px-3 py-2 rounded ${
              activeSection === "donations" ? "bg-empuje-green text-white" : "text-gray-700"
            }`}
            onClick={() => setActiveSection("donations")}
          >
            Donaciones
          </button>
        </nav>
      </aside>

      {/* Contenido principal */}
      <main className="flex-1 p-6 overflow-auto">{renderSection()}</main>
    </div>
  );
}
