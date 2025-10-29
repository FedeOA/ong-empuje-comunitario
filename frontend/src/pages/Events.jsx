import React, { useState, useEffect } from "react";
import EventModal from "../components/EventModal";
import MembersModal from "../components/MembersModal";
import { baseUrl } from "../constants/constants.js";
import { useAuth } from "../context/AuthContext";
import Toast from "../components/Toast";
import ActionsModal from "../components/ActionsModal.jsx";
import FiltersModal from "../components/FiltersModal.jsx";
import DonationEventModal from "../components/DonationEventModal";

export default function Events() {
  const [events, setEvents] = useState([]);
  const [isEventModalOpen, setIsEventModalOpen] = useState(false);
  const [eventToEdit, setEventToEdit] = useState(null);
  const [isMembersModalOpen, setIsMembersModalOpen] = useState(false);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [selectedEventId, setSelectedEventId] = useState(null);
  const [toast, setToast] = useState({ message: "", type: "success" });
  const [isActionsModalOpen, setIsActionsModalOpen] = useState(false);
  const [isFilterModalOpen, setIsFilterModalOpen] = useState(false);
  const [savedFilters, setSavedFilters] = useState([]);
  const [donationsModalOpen, setDonationsModalOpen] = useState(false);
  const [selectedDonations, setSelectedDonations] = useState([]);

  const handleOpenDonationsModal = (donations) => {
    const safeDonations = Array.isArray(donations) ? donations : [];
    setSelectedDonations(safeDonations);
    setDonationsModalOpen(true);
  };

  const today = new Date();
  const { user } = useAuth();

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
      const publishedCount = data.filter(event => event.is_published === true).length;
      const notPublishedCount = data.filter(event => event.is_published !== true).length;
      console.log(`Eventos cargados: ${publishedCount} publicados, ${notPublishedCount} no publicados.`);
      return data;
    } catch (error) {
      console.error("Error al cargar eventos:", error);
      return null;
    }
  };

  useEffect(() => {
    if (selectedEvent && events.length > 0) {
      const updated = events.find(e => e.id === selectedEvent.id);
      if (updated && JSON.stringify(updated) !== JSON.stringify(selectedEvent)) {
        setSelectedEvent(updated);
      }
    }
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
      setIsActionsModalOpen(false); 
    } else {
      alert("Solo se pueden modificar eventos futuros.");
    }
  };

  const handleJoinEvent = async (eventId) => {
    try {
      const token = localStorage.getItem("token");
      const response = await fetch(`${baseUrl}/events/${eventId}/users/${user.username}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`
        }
      });

      if (!response.ok) throw new Error("Error al agregarse al evento");

      showToast("¡Te agregaste al evento con éxito!", "success");
      const newEvents = await fetchEvents(); 
      if (newEvents) {
        const updatedEvent = newEvents.find(e => e.id === eventId);
        if (updatedEvent) {
          setSelectedEvent(updatedEvent); 
        }
      }
    } catch (error) {
      console.error(error);
      showToast("Hubo un problema al agregarte al evento", "error");
    }
  };

  const handleLeaveEvent = async (eventId) => {
    try {
      const token = localStorage.getItem("token");
      const response = await fetch(`${baseUrl}/events/${eventId}/users/${user.username}`, {
        method: "DELETE",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`
        }
      });

      if (!response.ok) throw new Error("Error al abandonar el evento");
      const newEvents = await fetchEvents();
      if (newEvents) {
        const updatedEvent = newEvents.find(e => e.id === eventId);
        if (updatedEvent) {
          setSelectedEvent(updatedEvent); 
        }
      }
    } catch (error) {
      console.error(error);
      showToast("Hubo un problema al abandonar el evento", "error");
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
      setIsActionsModalOpen(false);
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
        organization_id: formData.organization_id || user?.organization_id || 1,
        ...(eventToEdit && { id: eventToEdit.id })
      };
      console.log("Submitting event payload:", payload);
      const eventId = payload.id;

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

      const newEvents = await fetchEvents(); 
      if (eventId && newEvents) {
        const updatedEvent = newEvents.find(e => e.id === eventId);
        if (updatedEvent) {
          setSelectedEvent(updatedEvent); 
        }
      }
      setIsEventModalOpen(false);
      setEventToEdit(null);
    } catch (error) {
      console.error(error);
      showToast("Hubo un problema al procesar el evento", "error");
    }
  };

  const handleUpdateUsers = async () => {
    const newEvents = await fetchEvents();
    if (newEvents && selectedEvent) {
      const updated = newEvents.find(e => e.id === selectedEvent.id);
      if (updated) {
        setSelectedEvent(updated);
      }
    }
  };

  const handlePublishEvent = async (event) => {
   try {
      const token = localStorage.getItem("token");
      if (!token) {
        console.warn("No auth token found in localStorage");
        showToast("No autenticado. Inicia sesión e intenta de nuevo.", "error");
        return;
      }
      console.debug("Using token: ", `${token.substring(0,6)}...${token.slice(-6)}`);
      let existing = events.find(e => e.id === event.id);
      if (!existing) {
        const refreshed = await fetchEvents();
        existing = refreshed ? refreshed.find(e => e.id === event.id) : null;
      }
      if (!existing) throw new Error("Evento no encontrado");

      const toUpdate = { ...existing, is_published: true };

      const response1 = await fetch(`${baseUrl}/events/${event.id}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify(toUpdate)
      });
      if (!response1.ok) {
        let bodyText = "";
        try {
          bodyText = await response1.text();
        } catch (e) {
          console.warn("No se pudo leer el body de la respuesta", e);
        }

        if (response1.status === 401) {
          console.error("Publish returned 401, response body:", bodyText);
          showToast(bodyText || "No autorizado. Por favor inicia sesión.", "error");
        } else {
          console.error("Publish failed:", response1.status, bodyText);
          showToast(bodyText || "Error al modificar el evento", "error");
        }
        throw new Error(`Error al modificar el evento: ${response1.status} ${bodyText}`);
      }

      const newEvents = await fetchEvents();
      if (newEvents) {
        const updatedEvent = newEvents.find(e => e.id === event.id);
        if (updatedEvent) {
          setSelectedEvent(updatedEvent);
        }
      }
      showToast("Evento publicado correctamente", "success");
    } catch (error) {
      console.error(error);
      showToast("Hubo un problema al publicar el evento", "error");
    }
  };
return (
  <div className="min-h-screen bg-empuje-bg p-6">
    {/* Header */}
    <div className="flex justify-between items-center mb-6">
      <h1 className="text-3xl font-bold text-empuje-green">Eventos Solidarios</h1>
      <div className="flex gap-4">
        <button
          className="bg-empuje-green text-white px-4 py-2 rounded-lg hover:bg-green-700 transition"
          onClick={handleAddEvent}
        >
          Agregar Evento
        </button>
        <button
          className="bg-empuje-orange text-white px-4 py-2 rounded-lg hover:bg-orange-600 transition"
          onClick={() => setIsFilterModalOpen(true)}
        >
          Filtros
        </button>   
      </div>
    </div>

    {/* Table */}
    <div className="bg-white shadow-md rounded-xl overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-empuje-green text-white">
            <tr>
              <th className="px-6 py-3 text-left text-sm font-medium">Nombre</th>
              <th className="px-6 py-3 text-left text-sm font-medium">Descripción</th>
              <th className="px-6 py-3 text-left text-sm font-medium">Fecha</th>
              <th className="px-6 py-3 text-center text-sm font-medium">Miembros</th>
              <th className="px-6 py-3 text-center text-sm font-medium">Acciones</th>
              <th className="px-6 py-3 text-center text-sm font-medium">Donaciones</th>
            </tr>
          </thead>

          <tbody className="divide-y divide-gray-200">
            {events.map((event, index) => {
              const eventDate = new Date(event.datetime);
              const isFuture = eventDate > today;
              const isAlreadyJoined = event.users?.includes(user.username);

              return (
                <tr key={event.id ?? `event-${index}`}>
                  <td className="px-6 py-4">{event.name}</td>
                  <td className="px-6 py-4">{event.description}</td>
                  <td className="px-6 py-4">{eventDate.toLocaleString("es-AR")}</td>
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
                  <td className="px-6 py-4">
                    <div className="flex justify-center">
                      <button
                        className="bg-empuje-blue text-white px-3 py-1 rounded hover:bg-blue-700 transition"
                        onClick={() => {
                          setSelectedEvent(event);
                          setIsActionsModalOpen(true);
                        }}
                      >
                        Ver
                      </button>
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex justify-center">
                      <button
                        className="bg-empuje-blue text-white px-3 py-1 rounded hover:bg-blue-700 transition"
                        onClick={() => handleOpenDonationsModal(event.donations)}
                      >
                        Ver
                      </button>
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

    {/* Modals */}
    <EventModal
      isOpen={isEventModalOpen}
      onClose={() => setIsEventModalOpen(false)}
      onSubmit={handleSubmitEvent}
      eventToEdit={eventToEdit}
    />

    <MembersModal
      isOpen={isMembersModalOpen}
      onClose={() => setIsMembersModalOpen(false)}
      event={selectedEvent}
      onUpdateMembers={handleUpdateUsers}
      user={user}
    />

    {isActionsModalOpen && selectedEvent && (
      <ActionsModal
        event={selectedEvent}
        user={user}
        isAlreadyJoined={selectedEvent.users?.includes(user.username)}
        isFuture={new Date(selectedEvent.datetime) > today}
        onClose={() => setIsActionsModalOpen(false)}
        onJoin={handleJoinEvent}
        onLeave={handleLeaveEvent}
        onEdit={handleEditEvent}
        onDelete={handleDeleteEvent}
        onPublish={handlePublishEvent}
      />
    )}
    
    {donationsModalOpen && (
      <DonationEventModal
        donations={selectedDonations}
        onClose={() => setDonationsModalOpen(false)}
      />
    )}

    {isFilterModalOpen && (
      <FiltersModal
        onClose={() => setIsFilterModalOpen(false)}
        onApplyFilters={(filteredEvents) => {
          setEvents(filteredEvents); 
          setIsFilterModalOpen(false);
        }}
        onSaveFilter={(newFilter) => {
          setSavedFilters((prev) => [...prev, newFilter]);
        }}
        savedFilters={savedFilters}
      />
    )}


    {toast.message && (
      <Toast message={toast.message} type={toast.type} />
    )}
  </div>
);
}
