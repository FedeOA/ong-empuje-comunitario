// frontend/src/pages/DonationReports.jsx
import React, { useState, useEffect } from "react";
import DonationReportFilterModal from "../components/DonationReportFilterModal";
import { baseUrlGraphQL } from "../constants/constants.js";
import { categoriesIndexes } from "../constants/Categories.js";

export default function DonationReport() {
  const [reports, setReports] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [filterToEdit, setFilterToEdit] = useState(null);
  const [toast, setToast] = useState({ message: "", type: "success" });
  const [loading, setLoading] = useState(false);
  const [savedFilters, setSavedFilters] = useState([]);

  const showToast = (message, type = "success") => {
    setToast({ message, type });
    setTimeout(() => setToast({ message: "", type: "success" }), 5000); // Extended timeout for better visibility
  };

  const fetchDonationReport = async (filters = {}) => {
    setLoading(true);
    try {
      const query = `
        query DonationReport($categoryId: Int, $startDate: String, $endDate: String, $deleted: Boolean) {
          donationReport(categoryId: $categoryId, startDate: $startDate, endDate: $endDate, deleted: $deleted) {
            categoryId
            categoryName
            deleted
            totalQuantity
            donations {
              id
              categoryId
              description
              quantity
              createdAt
              createdByUsername
              deleted
            }
          }
          categories {
            id
            name
          }
        }
      `;

      const variables = {
        categoryId: filters.categoryId ? parseInt(filters.categoryId) : null,
        startDate: filters.startDate ? `${filters.startDate}T00:00:00` : null,
        endDate: filters.endDate ? `${filters.endDate}T23:59:59` : null,
        deleted: filters.deleted === "YES" ? true : filters.deleted === "NO" ? false : null,
      };

      // Validate dates
      if (variables.startDate && variables.endDate && variables.startDate > variables.endDate) {
        throw new Error("La fecha de fin no puede ser anterior a la fecha de inicio");
      }

      const response = await fetch(`${baseUrlGraphQL}/graphql`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          query,
          variables,
        }),
      });

      if (!response.ok) {
        throw new Error(`Error de red: ${response.status} ${response.statusText}`);
      }

      const data = await response.json();
      console.log("Fetched donation report:", JSON.stringify(data, null, 2));

      if (data.errors) {
        const errorMessage = data.errors[0]?.message || "Error desconocido en el servidor";
        throw new Error(errorMessage);
      }

      setReports(data.data.donationReport || []);
    } catch (error) {
      console.error("Error al cargar informe de donaciones:", error);
      showToast(`Error al cargar informe de donaciones: ${error.message}`, "error");
    } finally {
      setLoading(false);
    }
  };

  const fetchSavedFilters = async () => {
    try {
      const query = `
        query {
          savedFilters {
            id
            name
            categoryId
            categoryName
            startDate
            endDate
            deleted
          }
        }
      `;

      const response = await fetch(`${baseUrlGraphQL}/graphql`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ query }),
      });

      if (!response.ok) {
        throw new Error(`Error de red: ${response.status} ${response.statusText}`);
      }

      const data = await response.json();
      if (data.errors) {
        throw new Error(data.errors[0]?.message || "Error desconocido en el servidor");
      }

      return data.data.savedFilters || [];
    } catch (error) {
      console.error("Error al cargar filtros:", error);
      showToast(`Error al cargar filtros guardados: ${error.message}`, "error");
      return [];
    }
  };

  useEffect(() => {
    fetchDonationReport();
    fetchSavedFilters().then(setSavedFilters);
  }, []);

  const openFilterModal = (filter = null) => {
    setFilterToEdit(filter);
    setIsModalOpen(true);
  };

  const handleSubmitFilter = async (data) => {
    try {
      const mutation = filterToEdit
        ? `
          mutation UpdateFilter($id: ID!, $input: FilterInput!) {
            updateFilter(id: $id, input: $input) {
              id
              name
            }
          }
        `
        : `
          mutation SaveFilter($input: FilterInput!) {
            saveFilter(input: $input) {
              id
              name
            }
          }
        `;

      const variables = filterToEdit
        ? {
            id: filterToEdit.id,
            input: {
              name: data.name,
              categoryId: data.categoryId ? parseInt(data.categoryId) : null,
              startDate: data.startDate ? `${data.startDate}T00:00:00` : null,
              endDate: data.endDate ? `${data.endDate}T23:59:59` : null,
              deleted: data.deleted === "YES" ? true : data.deleted === "NO" ? false : null,
            },
          }
        : {
            input: {
              name: data.name,
              categoryId: data.categoryId ? parseInt(data.categoryId) : null,
              startDate: data.startDate ? `${data.startDate}T00:00:00` : null,
              endDate: data.endDate ? `${data.endDate}T23:59:59` : null,
              deleted: data.deleted === "YES" ? true : data.deleted === "NO" ? false : null,
            },
          };

      const response = await fetch(`${baseUrlGraphQL}/graphql`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ query: mutation, variables }),
      });

      if (!response.ok) {
        throw new Error(`Error de red: ${response.status} ${response.statusText}`);
      }

      const result = await response.json();
      if (result.errors) {
        throw new Error(result.errors[0]?.message || "Error desconocido en el servidor");
      }

      showToast(
        filterToEdit ? "Filtro actualizado correctamente" : "Filtro guardado correctamente"
      );

      setTimeout(() => {
        fetchDonationReport(data);
        fetchSavedFilters().then(setSavedFilters);
      }, 1000);

      setIsModalOpen(false);
      setFilterToEdit(null);
    } catch (error) {
      console.error("Error al procesar el filtro:", error);
      showToast(`Error al procesar el filtro: ${error.message}`, "error");
    }
  };

  const handleDeleteFilter = async (filterId) => {
    if (!confirm("¿Está seguro de eliminar este filtro?")) return;

    try {
      const mutation = `
        mutation DeleteFilter($id: ID!) {
          deleteFilter(id: $id)
        }
      `;

      const response = await fetch(`${baseUrlGraphQL}/graphql`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          query: mutation,
          variables: { id: filterId },
        }),
      });

      if (!response.ok) {
        throw new Error(`Error de red: ${response.status} ${response.statusText}`);
      }

      const data = await response.json();
      if (data.errors) {
        throw new Error(data.errors[0]?.message || "Error desconocido en el servidor");
      }

      showToast("Filtro eliminado correctamente");
      fetchSavedFilters().then(setSavedFilters);
    } catch (error) {
      console.error("Error al eliminar el filtro:", error);
      showToast(`Error al eliminar el filtro: ${error.message}`, "error");
    }
  };

  const handleApplyFilter = (filter) => {
    const appliedFilters = {
      categoryId: filter.categoryId,
      startDate: filter.startDate,
      endDate: filter.endDate,
      deleted: filter.deleted === true ? "YES" : filter.deleted === false ? "NO" : null,
    };
    fetchDonationReport(appliedFilters);
    showToast(`Filtro "${filter.name}" aplicado`);
  };

  const handleSearch = (filters) => {
    fetchDonationReport(filters);
  };

  return (
    <div className="min-h-screen bg-empuje-bg p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-empuje-green">Informe de Donaciones</h1>
        <div className="flex gap-4">
          <button
            className="bg-empuje-green text-white px-4 py-2 rounded-lg hover:bg-green-700 transition"
            onClick={() => openFilterModal()}
          >
            Nuevo Filtro
          </button>
        </div>
      </div>

      {/* Filtros Guardados */}
      <div className="bg-white shadow-md rounded-xl p-4 mb-6">
        <h2 className="text-lg font-semibold text-empuje-green mb-3">Filtros Guardados</h2>
        {savedFilters.length > 0 ? (
          <ul className="space-y-2">
            {savedFilters.map((filter) => (
              <li key={filter.id} className="flex justify-between items-center">
                <span>
                  {filter.name} - {filter.categoryName || "Todas"} -{" "}
                  {filter.deleted === true ? "Eliminados" : filter.deleted === false ? "Activos" : "Todos"}
                </span>
                <div className="flex gap-2">
                  <button
                    className="bg-blue-600 text-white px-3 py-1 rounded hover:bg-blue-700 transition text-sm"
                    onClick={() => handleApplyFilter(filter)}
                  >
                    Aplicar
                  </button>
                  <button
                    className="bg-yellow-600 text-white px-3 py-1 rounded hover:bg-yellow-700 transition text-sm"
                    onClick={() => openFilterModal(filter)}
                  >
                    Editar
                  </button>
                  <button
                    className="bg-red-600 text-white px-3 py-1 rounded hover:bg-red-700 transition text-sm"
                    onClick={() => handleDeleteFilter(filter.id)}
                  >
                    Eliminar
                  </button>
                </div>
              </li>
            ))}
          </ul>
        ) : (
          <p className="text-gray-600">No hay filtros guardados.</p>
        )}
      </div>

      {/* Resultados */}
      <div className="bg-white shadow-md rounded-xl overflow-hidden">
        {loading ? (
          <div className="flex justify-center items-center py-12">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-empuje-green"></div>
            <span className="ml-3 text-gray-600">Cargando informe...</span>
          </div>
        ) : reports.length === 0 ? (
          <div className="text-center py-12 text-gray-500">
            <p className="text-lg">No se encontraron donaciones</p>
            <p className="text-sm mt-2">Configure filtros para ver resultados</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-empuje-green text-white">
                <tr>
                  <th className="px-6 py-3 text-left text-sm font-medium">Categoría</th>
                  <th className="px-6 py-3 text-left text-sm font-medium">Estado</th>
                  <th className="px-6 py-3 text-right text-sm font-medium">Cantidad Total</th>
                  <th className="px-6 py-3 text-left text-sm font-medium">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {reports.map((group) => (
                  <tr key={group.categoryId} className={group.deleted ? "opacity-60" : ""}>
                    <td className="px-6 py-4">
                      <div className="text-sm font-medium text-gray-900">
                        {group.categoryName} (ID: {group.categoryId})
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <span className={group.deleted ? "text-red-600" : "text-green-600"}>
                        {group.deleted ? "Eliminado" : "Activo"}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <span className="text-sm font-semibold text-gray-900">
                        {group.totalQuantity}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <button
                        className="bg-blue-600 text-white px-3 py-1 rounded hover:bg-blue-700 transition text-sm"
                        onClick={() => openFilterModal({ categoryId: group.categoryId })}
                      >
                        Filtrar por Categoría
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {toast.message && (
        <div
          className={`fixed bottom-4 right-4 p-4 rounded-lg text-white ${
            toast.type === "success" ? "bg-green-600" : "bg-red-600"
          }`}
        >
          {toast.message}
        </div>
      )}

      <DonationReportFilterModal
        isOpen={isModalOpen}
        onClose={() => {
          setIsModalOpen(false);
          setFilterToEdit(null);
        }}
        onSubmit={handleSubmitFilter}
        onSearch={handleSearch}
        filterToEdit={filterToEdit}
      />
    </div>
  );
}