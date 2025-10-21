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
      await fetchEvents();
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

      showToast("Abandonaste el evento", "success");
      await fetchEvents();
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

  const handleUpdateUsers = async () => {
    await fetchEvents();
    const updated = events.find(e => e.id === selectedEvent.id);
    if (updated) setSelectedEvent(updated);
  };

  const handlePublishEvent = async (event) => {
    try {
      const token = localStorage.getItem("token");

      const response1 = await fetch(`${baseUrl}/events/${event.id}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({
          name: event.name,
          description: event.description,
          datetime: event.datetime,
          is_published: true
        })
      });

      if (!response1.ok) throw new Error("Error al modificar el evento");

      const response = await fetch(`${baseUrl}/events/publish`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({
          organization_id: 1, // id de organización fija por ahora
          event_id: event.id,
          name: event.name,
          description: event.description,
          datetime: event.datetime
        })
      });

      if (!response.ok) throw new Error("Error al publicar el evento");
      await fetchEvents();
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
            {events.map(event => {
              const eventDate = new Date(event.datetime);
              const isFuture = eventDate > today;
              const isAlreadyJoined = event.users?.includes(user.username);

              return (
                <React.Fragment key={event.id}>
                  <tr>
                    <td className="px-6 py-4">{event.name}</td>
                    <td className="px-6 py-4">{event.description}</td>
                    <td className="px-6 py-4">{eventDate.toLocaleString("es-AR")}</td>

                    {/* Miembros */}
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

                    {/* Acciones */}
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

                    {/* Donaciones */}
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

                  {/* Modal de acciones */}
                  {isActionsModalOpen && selectedEvent?.id === event.id && (
                    <ActionsModal
                      key={`actions-${event.id}`} // Unique key for ActionsModal
                      event={selectedEvent}
                      user={user}
                      isAlreadyJoined={isAlreadyJoined}
                      isFuture={isFuture}
                      onClose={() => setIsActionsModalOpen(false)}
                      onJoin={handleJoinEvent}
                      onLeave={handleLeaveEvent}
                      onEdit={handleEditEvent}
                      onDelete={handleDeleteEvent}
                      onPublish={handlePublishEvent}
                    />
                  )}
                </React.Fragment>
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