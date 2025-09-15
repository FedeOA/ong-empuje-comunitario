import React, { useState, useEffect } from "react";

export default function EventModal({ isOpen, onClose, onSubmit, eventToEdit }) {
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    date: "",
  });

  // Rellenar formulario si estamos editando
  useEffect(() => {
    if (eventToEdit) {
      setFormData({
        name: eventToEdit.name || "",
        description: eventToEdit.description || "",
        date: eventToEdit.date || "",
      });
    } else {
      setFormData({ name: "", description: "", date: "" });
    }
  }, [eventToEdit, isOpen]);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
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
          {/* Nombre del evento */}
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

          {/* Descripción */}
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

          {/* Fecha */}
          <div>
            <label className="block text-sm font-medium text-gray-700">Fecha del evento</label>
            <input
              type="date"
              name="date"
              value={formData.date}
              onChange={handleChange}
              className="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-empuje-green focus:border-empuje-green"
              required
            />
          </div>

          {/* Botón */}
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
