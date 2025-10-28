import React, { useState, useEffect } from "react";
import { baseUrl } from "../constants/constants.js";
import Toast from "../components/Toast";
import { getOrganizationName } from "../constants/organizations.js";
import { useAuth } from "../context/AuthContext";

export default function ExternalEventsPage() {
  const [events, setEvents] = useState([]);
  const [toast, setToast] = useState({ message: "", type: "success" });

  const { user } = useAuth();

  const showToast = (message, type = "success") => {
    setToast({ message, type });
    setTimeout(() => setToast({ message: "", type: "success" }), 3000);
  };

  const fetchExternalEvents = async () => {
    try {
      const token = localStorage.getItem("token");
      const response = await fetch(`${baseUrl}/events/externals`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`
        }
      });
      const data = await response.json();
      setEvents(data);
    } catch (error) {
      console.error("Error al cargar eventos:", error);
      showToast("Error al cargar eventos externos", "error");
    }
  };

  const handleJoinEvent = async (eventId, organizationId) => {
    try {
      const token = localStorage.getItem("token");

      const response = await fetch(
        `${baseUrl}/events/${eventId}/organization/${organizationId}/user/${user.username}`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
          }
        }
      );

      if (!response.ok) throw new Error("Error en la adhesión");

      setEvents(prev =>
        prev.map(event =>
          event.remote_id === eventId
            ? {
                ...event,
                users: [...(event.users || []), user.username]
              }
            : event
        )
      );

      showToast(`Te agregaste al evento correctamente`, "success");
    } catch (error) {
      console.error("Error al agregarse al evento:", error);
      showToast("No se pudo agregar al evento", "error");
    }
  };

  useEffect(() => {
    fetchExternalEvents();
  }, []);

  return (
    <div className="min-h-screen bg-empuje-bg p-6">
      <h1 className="text-3xl font-bold text-empuje-green mb-6">Eventos Externos</h1>

      <div className="bg-white shadow-md rounded-xl overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-empuje-green text-white">
            <tr>
              <th className="px-6 py-3 text-left text-sm font-medium">Nombre</th>
              <th className="px-6 py-3 text-left text-sm font-medium">Descripción</th>
              <th className="px-6 py-3 text-left text-sm font-medium">Fecha</th>
              <th className="px-6 py-3 text-left text-sm font-medium">Organizador</th>
              <th className="px-6 py-3 text-left text-sm font-medium">Acciones</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {events.map(event => {
              const isAlreadyJoined = event.users?.includes(user.username);
              console.log("Event users:", event.users, "User:", user.username, "isAlreadyJoined:", isAlreadyJoined);

              return (
                <tr key={event.id}>
                  <td className="px-6 py-4">{event.name}</td>
                  <td className="px-6 py-4">{event.description}</td>
                  <td className="px-6 py-4">
                    {new Date(event.datetime).toLocaleString("es-AR")}
                  </td>
                  <td className="px-6 py-4">
                    {getOrganizationName(event.organization_id) || "—"}
                  </td>
                  <td className="px-6 py-4">
                    {isAlreadyJoined ? (
                      <div
                            className="flex items-center gap-1 text-purple-700 font-semibold"
                            title="Evento publicado"
                          >
                            ✅ <span className="text-sm">Agregado</span>
                          </div>
                    ) : (
                      <button
                        className="bg-empuje-green text-white px-3 py-1 rounded hover:bg-green-700 transition"
                        onClick={() =>
                          handleJoinEvent(event.remote_id, event.organization_id)
                        }
                      >
                        Agregarse
                      </button>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {toast.message && <Toast message={toast.message} type={toast.type} />}
    </div>
  );
}
