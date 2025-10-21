import React, { useState, useEffect } from "react";
import UsersPage from "./Users";
import EventsPage from "./Events";
import DonationsPage from "./Donations";
import DonationRequestsPage from "./DonationRequests";
import ExternalEventsPage from "./ExternalEventsPage";
import DonationReportsPage from "./DonationReports";
import DonationReportExcelPage from "./DonationReportExcel";
import SoapDataPage from "./SoapData";
import { useAuth } from "../context/AuthContext";
import { hasPermission, getDefaultSection } from "../utils/permissions";
import UserProfile from "../components/UserProfile";

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
      case "donation-requests":
        return <DonationRequestsPage />;
      case "externalEvents":
        return <ExternalEventsPage />;
      case "donation-reports":
        return <DonationReportsPage/>;
      case "donation-report-excel":
        return <DonationReportExcelPage/>
      case "soap-data":
        return <SoapDataPage />;
      default:
        return null;
    }
  };

  return (
    <div className="flex min-h-screen bg-empuje-bg">
      {/* Sidebar */}
      <aside className="w-64 bg-white shadow-md p-6 flex flex-col">
        <UserProfile username={user?.username} role={user?.role} />
        <div className="h-6" />
        <div className="h-6" />

        {/* Navegación */}
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
          {hasPermission(user?.role, "donation-requests") && (
            <button
              className={`text-left px-3 py-2 rounded ${
                activeSection === "donation-requests"
                  ? "bg-empuje-green text-white"
                  : "text-gray-700"
              }`}
              onClick={() => setActiveSection("donation-requests")}
            >
              Solicitudes de Donación
            </button>
          )}
          {hasPermission(user?.role, "externalEvents") && (
            <button
              className={`text-left px-3 py-2 rounded ${
                activeSection === "externalEvents" ? "bg-empuje-green text-white" : "text-gray-700"
              }`}
              onClick={() => setActiveSection("externalEvents")}
            >
              Eventos Externos
            </button>
          )}
          {hasPermission(user?.role, "donation-reports") && (
            <button
              className={`text-left px-3 py-2 rounded ${
                activeSection === "donation-reports" ? "bg-empuje-green text-white" : "text-gray-700"
              }`}
              onClick={() => setActiveSection("donation-reports")}
            >
              Reporte de Donaciones
            </button>
          )}
          {hasPermission(user?.role, "donation-report-excel") && (
            <button
              className={`text-left px-3 py-2 rounded ${
                activeSection === "donation-report-excel" ? "bg-empuje-green text-white" : "text-gray-700"
              }`}
              onClick={() => setActiveSection("donation-report-excel")}
            >
              Exportar Reporte Donaciones
            </button>
          )}
          {hasPermission(user?.role, "soap-data") && (
            <button
              className={`text-left px-3 py-2 rounded ${
                activeSection === "soap-data" ? "bg-empuje-green text-white" : "text-gray-700"
              }`}
              onClick={() => setActiveSection("soap-data")}
            >
              Consulta ONGs
            </button>
          )}
        </nav>
      </aside>

      <main className="flex-1 p-6 overflow-auto">{renderSection()}</main>
    </div>
  );
}
