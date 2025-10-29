import React, { useState, useEffect } from "react";
import { baseUrl } from "../constants/constants.js";
import Toast from "../components/Toast";
import { getOrganizationName } from "../constants/organizations.js";
import { useAuth } from "../context/AuthContext";

export default function ExternalEventsPage() {
  const [events, setEvents] = useState([]);
  const [toast, setToast] = useState({ message: "", type: "success" });
  const [pendingJoins, setPendingJoins] = useState([]);

  const { user } = useAuth();
  const [currentUserProfile, setCurrentUserProfile] = useState(null);

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

  const fetchCurrentUserProfile = async () => {
    try {
      const token = localStorage.getItem("token");
      const res = await fetch(`${baseUrl}/users`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`,
        },
      });
      if (!res.ok) return;
      const list = await res.json();
      const me = list.find(u => u.username === user?.username);
      if (me) setCurrentUserProfile(me);
    } catch (e) {
      console.debug("Could not fetch full user profile:", e);
    }
  };

  const handleJoinEvent = async (eventId, organizationId) => {
    try {
      const token = localStorage.getItem("token");
      if (pendingJoins.includes(eventId)) return;
      const existing = events.find(e => e.remote_id === eventId);
      const myEmail = currentUserProfile?.email;
      const alreadyJoined = existing && Array.isArray(existing.users) && existing.users.some(u => {
        if (typeof u === 'string') {
          return u === user.username || (myEmail && u === myEmail);
        }
        return u?.username === user.username || (myEmail && u?.email === myEmail);
      });
      if (alreadyJoined) {
        showToast("Ya estás agregado a este evento", "info");
        return;
      }

      setPendingJoins(prev => [...prev, eventId]);

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
                users: [...(event.users || []), currentUserProfile?.email || user.username]
              }
            : event
        )
      );
      const pollForJoin = async (remoteId, username, attempts = 10, intervalMs = 1000) => {
        for (let i = 0; i < attempts; i++) {
          await new Promise(r => setTimeout(r, intervalMs));
          try {
            const refreshed = await fetch(`${baseUrl}/events/externals`, {
              method: "GET",
              headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
              }
            });
            if (!refreshed.ok) continue;
            const list = await refreshed.json();
            const found = list.find(e => e.remote_id === remoteId);
            const persisted = found && Array.isArray(found.users) && found.users.some(u => u === username || (currentUserProfile?.email && u === currentUserProfile.email));
            if (persisted) {
              setEvents(list);
              showToast("Te agregaste al evento correctamente", "success");
              return true;
            }
          } catch (e) {
          }
        }
        showToast("Adhesión enviada y pendiente de procesamiento", "success");
        return false;
      };

      pollForJoin(eventId, user.username).finally(() => {
        setPendingJoins(prev => prev.filter(id => id !== eventId));
      });

      showToast(`Te agregaste al evento correctamente`, "success");
    } catch (error) {
      console.error("Error al agregarse al evento:", error);
      showToast("No se pudo agregar al evento", "error");
      setPendingJoins(prev => prev.filter(id => id !== eventId));
    }
  };

  useEffect(() => {
    fetchExternalEvents();
    if (user && user.username) fetchCurrentUserProfile();
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
              const myEmail = currentUserProfile?.email;
              const isAlreadyJoined = Array.isArray(event.users) && event.users.some(u => {
                if (typeof u === 'string') return u === user.username || (myEmail && u === myEmail);
                return u?.username === user.username || (myEmail && u?.email === myEmail);
              });
              const isPending = pendingJoins.includes(event.remote_id);
              return (
                <tr key={event.remote_id ?? event.id}>
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
                        className="bg-empuje-green text-white px-3 py-1 rounded hover:bg-green-700 transition disabled:opacity-60"
                        onClick={() => handleJoinEvent(event.remote_id, event.organization_id)}
                        disabled={isPending}
                      >
                        {isPending ? 'Enviando...' : 'Agregarse'}
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
