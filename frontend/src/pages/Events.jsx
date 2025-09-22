import React, { useState, useEffect } from "react";
import EventModal from "../components/EventModal";
import MembersModal from "../components/MembersModal";
import { baseUrl } from "../constants/constants.js";
import { useAuth } from "../context/AuthContext";
import Toast from "../components/Toast";

export default function Events() {
  const [events, setEvents] = useState([]);
  const [isEventModalOpen, setIsEventModalOpen] = useState(false);
  const [eventToEdit, setEventToEdit] = useState(null);
  const [isMembersModalOpen, setIsMembersModalOpen] = useState(false);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [toast, setToast] = useState({ message: "", type: "success" });

  const today = new Date();
  const { user } = useAuth()

  const showToast = (message, type = "success") => {
    setToast({ message, type });
    setTimeout(() => setToast({ message: "", type: "success" }), 3000);
  };

  const fetchEvents = async () => {
    try {
      const token = localStorage.getItem("token");
      const response = await fetch(`${baseUrl}/events`, {
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
    }
  };

  useEffect(() => {
    fetchEvents();
  }, []);

  const handleAddEvent = () => {
    setEventToEdit(null);
    setIsEventModalOpen(true);
  };

  const handleEditEvent = (event) => {
    const eventDate = new Date(event.datetime);
    if (eventDate > today) {
      setEventToEdit(event);
      setIsEventModalOpen(true);
    } else {
      alert("Solo se pueden modificar eventos futuros.");
    }
  };
  
  const handleDeleteEvent = async (event) => {
    const eventDate = new Date(event.datetime);

    if (eventDate <= today) {
      alert("Solo se pueden eliminar eventos futuros.");
      return;
    }

    try {
      const token = localStorage.getItem("token");

      const response = await fetch(`${baseUrl}/events/${event.id}`, {
        method: "DELETE",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`
        }
      });

      if (!response.ok) throw new Error("Error al eliminar el evento");

      await fetchEvents();
      showToast("Evento dado de baja correctamente", "success");

    } catch (error) {
      console.error(error);
      alert("Hubo un problema al eliminar el evento.");
    }
  };


  const handleSubmitEvent = async (formData) => {
    try {
      const token = localStorage.getItem("token");

      const payload = {
        ...formData,
        ...(eventToEdit && { id: eventToEdit.id })
      };

      console.log("Payload enviado al backend:", payload); // Verificás qué se envía

      const response = await fetch(
        payload.id ? `${baseUrl}/events/${payload.id}` : `${baseUrl}/events`,
        {
          method: payload.id ? "PUT" : "POST",
          headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
          },
          body: JSON.stringify(payload)
        }
      );

      if (!response.ok) throw new Error("Error al guardar el evento");

      showToast(
        payload.id ? "Evento modificado con éxito" : "Evento registrado correctamente",
        "success"
      );

      await fetchEvents();
      setIsEventModalOpen(false);
      setEventToEdit(null);

    } catch (error) {
      console.error(error);
      showToast("Hubo un problema al procesar el evento", "error");
    }
  };


  const handleUpdateMembers = async (eventId, newMembers) => {
    try {
      const token = localStorage.getItem("token");
      const response = await fetch(`${baseUrl}/events/${eventId}/members`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({ members: newMembers })
      });

      if (!response.ok) throw new Error("Error al actualizar miembros");

      await fetchEvents();
    } catch (error) {
      console.error(error);
      alert("Hubo un problema al actualizar los miembros.");
    }
  };

  return (
    <div className="min-h-screen bg-empuje-bg p-6">
      {/* Título + agregar evento */}
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-empuje-green">Eventos Solidarios</h1>
        <button
          className="bg-empuje-green text-white px-4 py-2 rounded-lg hover:bg-green-700 transition"
          onClick={handleAddEvent}
        >
          Agregar Evento
        </button>
      </div>

      {/* Tabla */}
      <div className="bg-white shadow-md rounded-xl overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-empuje-green text-white">
            <tr>
              <th className="px-6 py-3 text-left text-sm font-medium">Nombre</th>
              <th className="px-6 py-3 text-left text-sm font-medium">Descripción</th>
              <th className="px-6 py-3 text-left text-sm font-medium">Fecha</th>
              <th className="px-6 py-3 text-center text-sm font-medium">Miembros</th>
              <th className="px-6 py-3 text-center text-sm font-medium">Acciones</th>
            </tr>
          </thead>

          <tbody className="divide-y divide-gray-200">
            {events.map(event => {
              const eventDate = new Date(event.datetime);
              const isFuture = eventDate > today;

              return (
                <tr key={event.id}>
                  <td className="px-6 py-4">{event.name}</td>
                  <td className="px-6 py-4">{event.description}</td>
                  <td className="px-6 py-4">{event.datetime}</td>

                  {/* Columna Miembros */}
                  <td className="px-6 py-4">
                    <div className="flex justify-center">
                      <button
                        className="bg-empuje-blue text-white px-3 py-1 rounded hover:bg-blue-700 transition"
                        onClick={() => {
                          setSelectedEvent(event);
                          setIsMembersModalOpen(true);
                        }}
                      >
                        Ver
                      </button>
                    </div>
                  </td>

                  {/* Columna Acciones */}
                  <td className="px-6 py-4 flex justify-center gap-2">
                    {/* Solo mostrar si el evento es futuro */}
                    {isFuture && (
                      <button
                        className="bg-empuje-green text-white px-3 py-1 rounded hover:bg-green-700 transition"
                        onClick={() => alert("Te agregaste al evento!")}
                      >
                        Agregarse
                      </button>
                    )}


                    {/* Solo PRESIDENTE y COORDINADOR pueden modificar/eliminar */}
                    {(user.role === "PRESIDENTE" || user.role === "COORDINADOR") && isFuture && (
                      <>
                        <button
                          className="bg-empuje-blue text-white px-3 py-1 rounded hover:bg-blue-700 transition"
                          onClick={() => handleEditEvent(event)}
                        >
                          Modificar
                        </button>
                        <button
                          className="bg-empuje-orange text-white px-3 py-1 rounded hover:bg-orange-700 transition"
                          onClick={() => handleDeleteEvent(event)}
                        >
                          Eliminar
                        </button>
                      </>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {/* Modal agregar/modificar evento */}
      <EventModal
        isOpen={isEventModalOpen}
        onClose={() => setIsEventModalOpen(false)}
        onSubmit={handleSubmitEvent}
        eventToEdit={eventToEdit}
      />

      {/* Modal miembros */}
      <MembersModal
        isOpen={isMembersModalOpen}
        onClose={() => setIsMembersModalOpen(false)}
        event={selectedEvent}
        onUpdateMembers={handleUpdateMembers}
      />

      {/* Toast visual */}
      {toast.message && (
        <Toast message={toast.message} type={toast.type} />
      )}
    </div>
  );
}
