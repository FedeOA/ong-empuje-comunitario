import React, { useState } from "react";

const FiltersModal = ({
  onClose,
  onApplyFilters,
  onSaveFilter,
  savedFilters = [],
}) => {
  const [filters, setFilters] = useState({
    username: "",
    donations: "ambos",
    fromDate: "",
    toDate: "",
    name: "",
  });

  const [selectedSavedFilter, setSelectedSavedFilter] = useState("");

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFilters((prev) => ({ ...prev, [name]: value }));
  };

  const handleSelectSavedFilter = (e) => {
    const selected = e.target.value;
    setSelectedSavedFilter(selected);
    const found = savedFilters.find((f) => f.name === selected);
    if (found) setFilters(found);
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-lg p-6 w-full max-w-xl">
        <h2 className="text-2xl font-semibold mb-4 text-empuje-green">Filtros</h2>

        <div className="grid grid-cols-1 gap-4">
          {/* Usuario */}
          <div>
            <label className="block text-sm font-medium text-gray-700">Usuario</label>
            <input
              type="text"
              name="username"
              value={filters.username}
              onChange={handleChange}
              className="mt-1 block w-full border border-gray-300 rounded px-3 py-2"
            />
          </div>

          {/* Donaciones */}
          <div>
            <label className="block text-sm font-medium text-gray-700">Donaciones</label>
            <select
              name="donations"
              value={filters.donations}
              onChange={handleChange}
              className="mt-1 block w-full border border-gray-300 rounded px-3 py-2"
            >
              <option value="si">Sí</option>
              <option value="no">No</option>
              <option value="ambos">Ambos</option>
            </select>
          </div>

          {/* Desde */}
          <div>
            <label className="block text-sm font-medium text-gray-700">Desde</label>
            <input
              type="date"
              name="fromDate"
              value={filters.fromDate}
              onChange={handleChange}
              className="mt-1 block w-full border border-gray-300 rounded px-3 py-2"
            />
          </div>

          {/* Hasta */}
          <div>
            <label className="block text-sm font-medium text-gray-700">Hasta</label>
            <input
              type="date"
              name="toDate"
              value={filters.toDate}
              onChange={handleChange}
              className="mt-1 block w-full border border-gray-300 rounded px-3 py-2"
            />
          </div>

          {/* Nombre del filtro */}
          <div>
            <label className="block text-sm font-medium text-gray-700">Nombre del filtro</label>
            <input
              type="text"
              name="name"
              value={filters.name}
              onChange={handleChange}
              className="mt-1 block w-full border border-gray-300 rounded px-3 py-2"
            />
          </div>

          {/* Filtros guardados */}
          <div>
            <label className="block text-sm font-medium text-gray-700">Filtros guardados</label>
            <select
              value={selectedSavedFilter}
              onChange={handleSelectSavedFilter}
              className="mt-1 block w-full border border-gray-300 rounded px-3 py-2"
            >
              <option value="">Seleccionar...</option>
              {savedFilters.map((filtro) => (
                <option key={filtro.name} value={filtro.name}>
                  {filtro.name}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Botones */}
        <div className="mt-6 flex justify-between">
          <button
            className="bg-empuje-green text-white px-4 py-2 rounded hover:bg-green-700 transition"
            onClick={() => onApplyFilters(filters)}
          >
            Filtrar
          </button>
          <button
            className="bg-empuje-orange text-white px-4 py-2 rounded hover:bg-orange-600 transition"
            onClick={() => onSaveFilter(filters)}
          >
            Guardar Filtro
          </button>
          <button
            className="text-gray-600 hover:text-gray-800"
            onClick={onClose}
          >
            Cerrar
          </button>
        </div>
      </div>
    </div>
  );
};

export default FiltersModal;
