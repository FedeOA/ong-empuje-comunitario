import React, { useState, useEffect } from "react";
import { baseUrlWebServices } from "../constants/constants";
import { useAuth } from "../context/AuthContext";
import Toast from "../components/Toast";

const PARTICIPATION_EVENTS_QUERY = `
  query GetParticipationEvents($username: String!, $startDate: String, $endDate: String, $distribution: DonationDistributionFilter) {
    participationEvents(
      username: $username,
      startDate: $startDate,
      endDate: $endDate,
      distribution: $distribution
    ) {
      name
      datetime
      description
      donations {
        categoryId
        description
        quantity
      }
    }
  }
`;

const FiltersModal = ({ onClose, onApplyFilters, onSaveFilter }) => {
  const { user } = useAuth();
  const isPrivileged = user.role === "PRESIDENTE" || user.role === "COORDINADOR";

  const [filters, setFilters] = useState({
    username: user.username || "",
    searchUsername: isPrivileged ? "" : user.username,
    distribution: "BOTH",
    startDate: "",
    endDate: "",
    name: "",
  });

  const [savedFilters, setSavedFilters] = useState([]);
  const [selectedSavedFilter, setSelectedSavedFilter] = useState("");
  const [toast, setToast] = useState(null);

  useEffect(() => {
    const fetchSavedFilters = async () => {
      try {
        const token = localStorage.getItem("token");
        const response = await fetch(`${baseUrlWebServices}/api/events/filter/${user.username}`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        if (!response.ok) throw new Error("No se pudieron cargar los filtros");

        const data = await response.json();
        setSavedFilters(data);
      } catch (error) {
        console.error("Error fetching saved filters:", error);
        showToast("Error al cargar filtros guardados", "error");
      }
    };

    fetchSavedFilters();
  }, [user]);

  const showToast = (message, type = "success") => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3000);
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFilters((prev) => ({ ...prev, [name]: value }));
  };

  const handleSelectSavedFilter = (e) => {
    const selected = e.target.value;
    setSelectedSavedFilter(selected);
    const found = savedFilters.find((f) => f.name === selected);
    if (found) {
      setFilters({
        username: user.username,
        searchUsername: found.searchUsername ?? "",
        distribution: found.distribution ?? "BOTH",
        startDate: found.startDate ?? "",
        endDate: found.endDate ?? "",
        name: found.name ?? "",
      });
    }
  };

  const handleSaveFilter = async () => {
    if (!filters.name.trim()) {
      showToast("El nombre del filtro es obligatorio", "error");
      return;
    }

    try {
      const token = localStorage.getItem("token");
      const response = await fetch(`${baseUrlWebServices}/api/events/filter/save`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(filters),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "Error al guardar el filtro");
      }

      showToast("Filtro guardado correctamente", "success");
      onSaveFilter(filters);
    } catch (error) {
      console.error("Error saving filter:", error);
      showToast("Hubo un problema al guardar el filtro", "error");
    }
  };

  const executeGraphQLFilter = async () => {
    if (!filters.searchUsername.trim()) {
      showToast("El campo 'Usuario a filtrar' es obligatorio", "error");
      return;
    }

    try {
      const token = localStorage.getItem("token");

      const response = await fetch(`${baseUrlWebServices}/graphql`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          query: PARTICIPATION_EVENTS_QUERY,
          variables: {
            username: filters.searchUsername,
            startDate: filters.startDate || null,
            endDate: filters.endDate || null,
            distribution: filters.distribution.toUpperCase(),
          },
        }),
      });

      const result = await response.json();
      if (result.errors) throw new Error(result.errors[0].message);

      onApplyFilters(result.data.participationEvents);
    } catch (error) {
      console.error("GraphQL filter error:", error);
      showToast("Error al aplicar el filtro", "error");
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-lg p-6 w-full max-w-xl">
        <h2 className="text-2xl font-semibold mb-4 text-empuje-green">Filtros</h2>

        {toast && <Toast type={toast.type} message={toast.message} />}

        <div className="grid grid-cols-1 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700">Usuario a filtrar</label>
            <input
              type="text"
              name="searchUsername"
              value={filters.searchUsername}
              onChange={handleChange}
              disabled={!isPrivileged}
              className={`mt-1 block w-full border ${!isPrivileged ? "bg-gray-100" : ""} border-gray-300 rounded px-3 py-2`}
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">Donaciones</label>
            <select
              name="distribution"
              value={filters.distribution}
              onChange={handleChange}
              className="mt-1 block w-full border border-gray-300 rounded px-3 py-2"
            >
              <option value="YES">Sí</option>
              <option value="NO">No</option>
              <option value="BOTH">Ambos</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">Desde</label>
            <input
              type="date"
              name="startDate"
              value={filters.startDate}
              onChange={handleChange}
              className="mt-1 block w-full border border-gray-300 rounded px-3 py-2"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">Hasta</label>
            <input
              type="date"
              name="endDate"
              value={filters.endDate}
              onChange={handleChange}
              className="mt-1 block w-full border border-gray-300 rounded px-3 py-2"
            />
          </div>

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

          <div>
            <label className="block text-sm font-medium text-gray-700">Filtros guardados</label>
            <select
              value={selectedSavedFilter}
              onChange={handleSelectSavedFilter}
              className="mt-1 block w-full border border-gray-300 rounded px-3 py-2"
            >
              <option value="">Seleccionar...</option>
              {savedFilters.map((filtro, index) => (
                <option key={`${filtro.name || "sin-nombre"}-${index}`} value={filtro.name}>
                  {filtro.name || `Sin nombre (${index + 1})`}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="mt-6 flex justify-between">
          <button
            className="bg-empuje-green text-white px-4 py-2 rounded hover:bg-green-700 transition"
            onClick={executeGraphQLFilter}
          >
            Filtrar
          </button>
          <button
            className="bg-empuje-orange text-white px-4 py-2 rounded hover:bg-orange-600 transition"
            onClick={handleSaveFilter}
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
