import React, { useState, useEffect } from "react";

export default function AddFilterModal({ isOpen, onClose, onSubmit, filterToEdit, categories }) {
  const [name, setName] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [deleted, setDeleted] = useState(null);

  useEffect(() => {
    if (filterToEdit) {
      setName(filterToEdit.name || "");
      setCategoryId(filterToEdit.categoryId ? filterToEdit.categoryId.toString() : "");
      setStartDate(filterToEdit.startDate || "");
      setEndDate(filterToEdit.endDate || "");
      setDeleted(filterToEdit.deleted);
    } else {
      setName("");
      setCategoryId("");
      setStartDate("");
      setEndDate("");
      setDeleted(null);
    }
  }, [filterToEdit, isOpen]);

  const handleSubmit = (e) => {
    e.preventDefault();
    const input = {
      name,
      categoryId: categoryId ? parseInt(categoryId) : null,
      startDate: startDate || null,
      endDate: endDate || null,
      deleted,
    };
    onSubmit(input);
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
          {filterToEdit ? "Modificar Filtro" : "Guardar Filtro"}
        </h2>
        <form className="space-y-4" onSubmit={handleSubmit}>
          <div>
            <label className="block text-sm font-medium text-gray-700">Nombre</label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg"
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700">Categoría</label>
            <select
              value={categoryId}
              onChange={(e) => setCategoryId(e.target.value)}
              className="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg"
            >
              <option value="">Todas</option>
              {categories.map((cat) => (
                <option key={cat.id} value={cat.id}>
                  {cat.name}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700">Fecha Inicio</label>
            <input
              type="datetime-local"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              className="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700">Fecha Fin</label>
            <input
              type="datetime-local"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              className="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700">Eliminado</label>
            <select
              value={deleted === null ? "" : deleted ? "YES" : "NO"}
              onChange={(e) => setDeleted(e.target.value === "YES" ? true : e.target.value === "NO" ? false : null)}
              className="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg"
            >
              <option value="">Ambos</option>
              <option value="YES">Sí</option>
              <option value="NO">No</option>
            </select>
          </div>
          <button
            type="submit"
            className="w-full bg-empuje-green text-white py-2 rounded-lg font-medium hover:bg-green-700 transition"
          >
            {filterToEdit ? "Actualizar" : "Guardar"}
          </button>
        </form>
      </div>
    </div>
  );
}