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

  const showToast = (message, type = "success") => {
    setToast({ message, type });
    setTimeout(() => setToast({ message: "", type: "success" }), 3000);
  };

  const fetchDonationReport = async (filters = {}) => {
    const token = localStorage.getItem("token");
    setLoading(true);
    try {
      const params = new URLSearchParams();
      
      if (filters.categoryId) params.append('categoryId', filters.categoryId);
      if (filters.startDate) params.append('startDate', filters.startDate);
      if (filters.endDate) params.append('endDate', filters.endDate);
      if (filters.deleted) params.append('deleted', filters.deleted);

      const url = `${baseUrlGraphQL}/graphql`;
      
      const query = `
        query DonationReport($categoryId: Int, $startDate: String, $endDate: String, $deleted: String) {
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

      const response = await fetch(url, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`,
        },
        body: JSON.stringify({
          query,
          variables: filters
        }),
      });

      if (!response.ok) throw new Error("Error al cargar informe de donaciones");

      const data = await response.json();
      console.log("Fetched donation report:", JSON.stringify(data, null, 2));
      
      if (data.errors) {
        throw new Error(data.errors[0].message);
      }

      setReports(data.data.donationReport || []);
    } catch (error) {
      console.error("Error al cargar informe de donaciones:", error);
      showToast("Error al cargar informe de donaciones", "error");
    } finally {
      setLoading(false);
    }
  };

  const fetchSavedFilters = async () => {
    const token = localStorage.getItem("token");
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
          "Authorization": `Bearer ${token}`,
        },
        body: JSON.stringify({ query }),
      });

      if (!response.ok) throw new Error("Error al cargar filtros guardados");

      const data = await response.json();
      if (data.errors) {
        throw new Error(data.errors[0].message);
      }

      return data.data.savedFilters || [];
    } catch (error) {
      console.error("Error al cargar filtros:", error);
      showToast("Error al cargar filtros guardados", "error");
      return [];
    }
  };

  useEffect(() => {
    fetchDonationReport();
  }, []);

  const openFilterModal = (filter = null) => {
    setFilterToEdit(filter);
    setIsModalOpen(true);
  };

  const handleSubmitFilter = async (data) => {
    const token = localStorage.getItem("token");
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
              startDate: data.startDate || null,
              endDate: data.endDate || null,
              deleted: data.deleted || null,
            }
          }
        : { 
            input: {
              name: data.name,
              categoryId: data.categoryId ? parseInt(data.categoryId) : null,
              startDate: data.startDate || null,
              endDate: data.endDate || null,
              deleted: data.deleted || null,
            }
          };

      const response = await fetch(`${baseUrlGraphQL}/graphql`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`,
        },
        body: JSON.stringify({ query: mutation, variables }),
      });

      if (!response.ok) throw new Error("Error al procesar el filtro");

      const result = await response.json();
      if (result.errors) {
        throw new Error(result.errors[0].message);
      }

      showToast(
        filterToEdit ? "Filtro actualizado correctamente" : "Filtro guardado correctamente"
      );
      
      setTimeout(() => {
        fetchDonationReport(data);
      }, 1000);
      
      setIsModalOpen(false);
      setFilterToEdit(null);
    } catch (error) {
      console.error("Error al procesar el filtro:", error);
      showToast("Error al procesar el filtro", "error");
    }
  };

  const handleDeleteFilter = async (filterId) => {
    if (!confirm("¿Está seguro de eliminar este filtro?")) return;

    const token = localStorage.getItem("token");
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
          "Authorization": `Bearer ${token}`,
        },
        body: JSON.stringify({ 
          query: mutation, 
          variables: { id: filterId } 
        }),
      });

      if (!response.ok) throw new Error("Error al eliminar el filtro");

      const data = await response.json();
      if (data.errors) {
        throw new Error(data.errors[0].message);
      }

      showToast("Filtro eliminado correctamente");
    } catch (error) {
      console.error("Error al eliminar el filtro:", error);
      showToast("Error al eliminar el filtro", "error");
    }
  };

  const handleApplyFilter = (filter) => {
    const appliedFilters = {
      categoryId: filter.categoryId,
      startDate: filter.startDate,
      endDate: filter.endDate,
      deleted: filter.deleted,
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
      {false && ( // Ocultar por ahora, mostrar cuando tengamos datos
        <div className="bg-white shadow-md rounded-xl p-4 mb-6">
          <h2 className="text-lg font-semibold text-empuje-green mb-3">Filtros Guardados</h2>
          {/* Aquí irían los filtros guardados */}
        </div>
      )}

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