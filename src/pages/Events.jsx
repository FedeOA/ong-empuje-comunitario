import React, { useState } from "react";
import EventModal from "../components/EventModal";
import MembersModal from "../components/MembersModal";

// Eventos iniciales de ejemplo
const initialEvents = [
  {
    id: 1,
    name: "Campaña de alimentos",
    description: "Recolección y distribución de alimentos.",
    date: "2025-10-15",
    members: ["John Doe", "Alice Presidente"]
  },
  {
    id: 2,
    name: "Limpieza del barrio",
    description: "Jornada de limpieza comunitaria.",
    date: "2024-12-01",
    members: ["John Doe"]
  }
];

export default function Events() {
  const [events, setEvents] = useState(initialEvents);
  const [isEventModalOpen, setIsEventModalOpen] = useState(false);
  const [eventToEdit, setEventToEdit] = useState(null);
  const [isMembersModalOpen, setIsMembersModalOpen] = useState(false);
  const [selectedEvent, setSelectedEvent] = useState(null);

  const today = new Date();

  // Agregar evento
  const handleAddEvent = () => {
    setEventToEdit(null);
    setIsEventModalOpen(true);
  };

  // Modificar evento
  const handleEditEvent = (event) => {
    const eventDate = new Date(event.date);
    if (eventDate > today) {
      setEventToEdit(event);
      setIsEventModalOpen(true);
    } else {
      alert("Solo se pueden modificar eventos futuros.");
    }
  };

  // Guardar evento agregado o modificado
  const handleSubmitEvent = (data) => {
    if (eventToEdit) {
      setEvents(events.map(e => e.id === eventToEdit.id ? { ...e, ...data } : e));
    } else {
      const id = events.length + 1;
      setEvents([...events, { ...data, id, members: [] }]);
    }
    setIsEventModalOpen(false);
  };

  // Eliminar evento futuro
  const handleDeleteEvent = (event) => {
    const eventDate = new Date(event.date);
    if (eventDate > today) {
      setEvents(events.filter(e => e.id !== event.id));
    } else {
      alert("Solo se pueden eliminar eventos futuros.");
    }
  };

  // Actualizar miembros de un evento
  const handleUpdateMembers = (eventId, newMembers) => {
    setEvents(events.map(e => e.id === eventId ? { ...e, members: newMembers } : e));
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
              const eventDate = new Date(event.date);
              const isFuture = eventDate > today;

              return (
                <tr key={event.id}>
                  <td className="px-6 py-4">{event.name}</td>
                  <td className="px-6 py-4">{event.description}</td>
                  <td className="px-6 py-4">{event.date}</td>

                  {/* Columna Miembros */}
                  <td className="px-6 py-4 text-center">
                    <button
                      className="bg-empuje-blue text-white px-3 py-1 rounded hover:bg-blue-700 transition"
                      onClick={() => {
                        setSelectedEvent(event);
                        setIsMembersModalOpen(true);
                      }}
                    >
                      Ver miembros
                    </button>
                  </td>

                  {/* Columna Acciones */}
                  <td className="px-6 py-4 flex justify-center gap-2">
                    <button
                      className="bg-empuje-green text-white px-3 py-1 rounded hover:bg-green-700 transition"
                      onClick={() => alert("Te agregaste al evento!")}
                    >
                      Agregarse
                    </button>

                    {isFuture && (
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
    </div>
  );
}
