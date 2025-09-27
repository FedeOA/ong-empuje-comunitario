import React, { useState, useEffect } from "react";

export default function EventModal({ isOpen, onClose, onSubmit, eventToEdit }) {
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    datetime: "",
  });

  const getLocalISOString = () => {
    const now = new Date();
    const local = new Date(now.getTime() - now.getTimezoneOffset() * 60000);
    return local.toISOString().slice(0, 16);
  };

  useEffect(() => {
    if (eventToEdit) {
      const raw = eventToEdit.datetime || "";
      const formatted = raw ? raw.slice(0, 16) : "";
      setFormData({
        name: eventToEdit.name || "",
        description: eventToEdit.description || "",
        datetime: formatted,
      });
    } else {
      setFormData({ name: "", description: "", datetime: "" });
    }
  }, [eventToEdit, isOpen]);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    const localNow = getLocalISOString();
    if (formData.datetime < localNow) {
      alert("La fecha y hora del evento no pueden ser anteriores al momento actual.");
      return;
    }

    onSubmit(formData);
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
      <div className="bg-white shadow-lg rounded-xl p-6 w-full max-w-lg relative">
        <button
          className="absolute top-3 right-3 text-gray-500 hover:text-gray-800 font-bold"
          onClick={onClose}
        >
          ×
        </button>

        <h2 className="text-2xl font-bold text-empuje-green mb-6 text-center">
          {eventToEdit ? "Modificar Evento" : "Agregar Evento"}
        </h2>

        <form className="space-y-4" onSubmit={handleSubmit}>
          <div>
            <label className="block text-sm font-medium text-gray-700">Nombre del evento</label>
            <input
              type="text"
              name="name"
              value={formData.name}
              onChange={handleChange}
              className="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-empuje-green focus:border-empuje-green"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">Descripción</label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              className="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-empuje-green focus:border-empuje-green"
              rows={3}
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">Fecha y hora del evento</label>
            <input
              type="datetime-local"
              name="datetime"
              value={formData.datetime}
              onChange={handleChange}
              min={getLocalISOString()}
              className="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-empuje-green focus:border-empuje-green"
              required
            />
          </div>

          <button
            type="submit"
            className="w-full bg-empuje-green text-white py-2 rounded-lg font-medium hover:bg-green-700 transition"
          >
            {eventToEdit ? "Confirmar" : "Agregar Evento"}
          </button>
        </form>
      </div>
    </div>
  );
}
