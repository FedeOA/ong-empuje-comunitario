import React, { useState, useEffect } from "react";
import UsersPage from "./Users";
import EventsPage from "./Events";
import DonationsPage from "./Donations";
import { useAuth } from "../context/AuthContext";
import { hasPermission, getDefaultSection } from "../utils/permissions";

export default function Home() {
  const { user } = useAuth();

  const [activeSection, setActiveSection] = useState(() =>
    getDefaultSection(user?.role)
  );

  useEffect(() => {
    setActiveSection(getDefaultSection(user?.role));
  }, [user]);

  const renderSection = () => {
    if (!hasPermission(user?.role, activeSection)) {
      return null;
    }

    switch (activeSection) {
      case "users":
        return <UsersPage />;
      case "events":
        return <EventsPage />;
      case "donations":
        return <DonationsPage />;
      default:
        return null;
    }
  };

  return (
    <div className="flex min-h-screen bg-empuje-bg">
      {/* Sidebar */}
      <aside className="w-64 bg-white shadow-md p-6 flex flex-col">
        <h2 className="text-2xl font-bold text-empuje-green mb-6">Dashboard</h2>
        <nav className="flex flex-col gap-3">
          {hasPermission(user?.role, "users") && (
            <button
              className={`text-left px-3 py-2 rounded ${
                activeSection === "users" ? "bg-empuje-green text-white" : "text-gray-700"
              }`}
              onClick={() => setActiveSection("users")}
            >
              Gestión de Usuarios
            </button>
          )}
          {hasPermission(user?.role, "events") && (
            <button
              className={`text-left px-3 py-2 rounded ${
                activeSection === "events" ? "bg-empuje-green text-white" : "text-gray-700"
              }`}
              onClick={() => setActiveSection("events")}
            >
              Gestión de Eventos
            </button>
          )}
          {hasPermission(user?.role, "donations") && (
            <button
              className={`text-left px-3 py-2 rounded ${
                activeSection === "donations" ? "bg-empuje-green text-white" : "text-gray-700"
              }`}
              onClick={() => setActiveSection("donations")}
            >
              Donaciones
            </button>
          )}
        </nav>
      </aside>

      {/* Contenido principal */}
      <main className="flex-1 p-6 overflow-auto">{renderSection()}</main>
    </div>
  );
}
