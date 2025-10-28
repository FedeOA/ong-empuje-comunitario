import React, { useState, useEffect } from "react";
import DonationReportFilterModal from "../components/DonationReportFilterModal";
import { baseUrlGraphQL } from "../constants/constants.js";
import { useAuth } from "../context/AuthContext";

export default function DonationReport() {
  const { user, loading: authLoading } = useAuth();
  const [reports, setReports] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [filterToEdit, setFilterToEdit] = useState(null);
  const [initialCategoryId, setInitialCategoryId] = useState(null);
  const [toast, setToast] = useState({ message: "", type: "success" });
  const [loading, setLoading] = useState(false);
  const [savedFilters, setSavedFilters] = useState([]);

  const showToast = (message, type = "success") => {
    setToast({ message, type });
    setTimeout(() => setToast({ message: "", type: "success" }), 5000);
  };

  const fetchDonationReport = async (filters = {}) => {
    if (!user || !user.username) {
      showToast("Debes iniciar sesión para ver el informe", "error");
      return;
    }

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
              deleted
            }
          }
          categories {
            id
            name
          }
        }
      `;

      // Primero, normaliza el valor de "deleted"
      const deletedValue = filters.filterDeleted === "true" ? true
                         : filters.filterDeleted === "false" ? false
                         : (filters.filterDeleted === true || filters.filterDeleted === false) ? filters.filterDeleted // Si ya es boolean, úsalo
                         : null; // "both", null, o undefined se convierten en null

      const variables = {
        categoryId: filters.categoryId ? parseInt(filters.categoryId) : null,
        startDate: filters.startDate || null,
        endDate: filters.endDate || null,
        deleted: deletedValue,
      };

      if (variables.startDate && variables.endDate && new Date(variables.startDate) > new Date(variables.endDate)) {
        throw new Error("La fecha de fin no puede ser anterior a la fecha de inicio");
      }

      const token = localStorage.getItem("token");
      const response = await fetch(`${baseUrlGraphQL}/graphql`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: token ? `Bearer ${token}` : "",
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
    if (!user || !user.username) {
      showToast("Debes iniciar sesión para ver los filtros guardados", "error");
      return;
    }

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
            isDeleted
            filterDeleted
            userId
            username
          }
        }
      `;

      const token = localStorage.getItem("token");
      const response = await fetch(`${baseUrlGraphQL}/graphql`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: token ? `Bearer ${token}` : "",
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

      const userFilters = data.data.savedFilters.filter(
        (filter) => filter.username === user.username && !filter.isDeleted
      );
      setSavedFilters(userFilters || []);
    } catch (error) {
      console.error("Error al cargar filtros:", error);
      showToast(`Error al cargar filtros guardados: ${error.message}`, "error");
    }
  };

  useEffect(() => {
    if (!authLoading && user) {
      fetchDonationReport({});  // Null dates = all donations
      fetchSavedFilters();
    }
  }, [authLoading, user]);

  const openFilterModal = (filter = null, categoryId = null) => {
    if (!user || !user.username) {
      showToast("Debes iniciar sesión para gestionar filtros", "error");
      return;
    }
    setFilterToEdit(filter);
    setInitialCategoryId(categoryId);
    setIsModalOpen(true);
  };

  const handleResetFilter = () => {
    if (!user || !user.username) {
      showToast("Debes iniciar sesión para resetear filtros", "error");
      return;
    }
    fetchDonationReport({});  // Null dates = all donations
    showToast("Filtros reseteados, mostrando todas las donaciones", "success");
  };

  const handleSubmitFilter = async (formData) => {
    if (!user || !user.username) {
      showToast("Debes iniciar sesión y tener un nombre de usuario válido para guardar filtros", "error");
      return;
    }

    const mutation = formData.id ? `
      mutation UpdateFilter($id: ID!, $input: FilterInput!) {
        updateFilter(id: $id, input: $input) {
          id
          name
          categoryId
          startDate
          endDate
          isDeleted
          filterDeleted
          userId
          username
        }
      }
    ` : `
      mutation SaveFilter($input: FilterInput!) {
        saveFilter(input: $input) {
          id
          name
          categoryId
          startDate
          endDate
          isDeleted
          filterDeleted
          userId
          username
        }
      }
    `;

    const variables = {
      ...(formData.id && { id: formData.id }),
      input: {
        name: formData.name,
        categoryId: formData.categoryId ? parseInt(formData.categoryId) : null,
        startDate: formData.startDate || null,
        endDate: formData.endDate || null,
        filterDeleted: formData.filterDeleted,
        username: user.username,
      },
    };

    try {
      const token = localStorage.getItem("token");
      if (!token) {
        showToast("No se encontró el token de autenticación. Por favor, inicia sesión nuevamente.", "error");
        return;
      }

      const response = await fetch(`${baseUrlGraphQL}/graphql`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ query: mutation, variables }),
      });

      const data = await response.json();
      if (data.errors) {
        const errorMessage = data.errors[0]?.message || "Error desconocido al procesar el filtro";
        throw new Error(errorMessage);
      }

      showToast(formData.id ? "Filtro actualizado con éxito" : "Filtro guardado con éxito");
      fetchSavedFilters();
      setIsModalOpen(false);
    } catch (error) {
      console.error("Error al procesar el filtro:", error);
      showToast(`Error al procesar el filtro: ${error.message}`, "error");
    }
  };

  const handleDeleteFilter = async (filterId) => {
    if (!user || !user.username) {
      showToast("Debes iniciar sesión para eliminar filtros", "error");
      return;
    }

    if (!confirm("¿Está seguro de eliminar este filtro?")) return;

    try {
      const mutation = `
        mutation DeleteFilter($id: ID!) {
          deleteFilter(id: $id)
        }
      `;

      const token = localStorage.getItem("token");
      const response = await fetch(`${baseUrlGraphQL}/graphql`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: token ? `Bearer ${token}` : "",
        },
        body: JSON.stringify({
          query: mutation,
          variables: { id: filterId.toString() },
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
      fetchSavedFilters();
    } catch (error) {
      console.error("Error al eliminar el filtro:", error);
      showToast(`Error al eliminar el filtro: ${error.message}`, "error");
    }
  };

  const handleApplyFilter = (filter) => {
    if (!user || !user.username) {
      showToast("Debes iniciar sesión para aplicar filtros", "error");
      return;
    }

    const appliedFilters = {
      categoryId: filter.categoryId,
      startDate: filter.startDate,
      endDate: filter.endDate,
      filterDeleted: filter.filterDeleted
    };
    fetchDonationReport(appliedFilters);
    showToast(`Filtro "${filter.name}" aplicado`);
  };

  const handleSearch = (filters) => {
    if (!user || !user.username) {
      showToast("Debes iniciar sesión para buscar", "error");
      return;
    }
    fetchDonationReport(filters);
  };

  if (authLoading) {
    return (
      <div className="min-h-screen bg-empuje-bg p-6 flex justify-center items-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-empuje-green"></div>
        <span className="ml-3 text-gray-600">Cargando...</span>
      </div>
    );
  }

  if (!user) {
    return (
      <div className="min-h-screen bg-empuje-bg p-6 flex justify-center items-center">
        <p className="text-lg text-gray-600">Por favor, inicia sesión para ver el informe de donaciones.</p>
      </div>
    );
  }

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
          <button
            className="bg-gray-600 text-white px-4 py-2 rounded-lg hover:bg-gray-700 transition"
            onClick={handleResetFilter}
          >
            Resetear Filtro
          </button>
        </div>
      </div>

      <div className="bg-white shadow-md rounded-xl p-4 mb-6">
        <h2 className="text-lg font-semibold text-empuje-green mb-3">Filtros Guardados</h2>
        {savedFilters.length > 0 ? (
          <ul className="space-y-2">
            {savedFilters.map((filter) => (
              <li key={filter.id} className="flex justify-between items-center">
                <span>
                  {filter.name} - {filter.categoryName || "Todas"} -{" "}
                  {filter.filterDeleted === true ? "Inactivos" 
                  : filter.filterDeleted === false ? "Activos" 
                  : "Ambos"}
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
                  <th className="px-6 py-3 text-left text-sm font-medium">Descripción</th>
                  <th className="px-6 py-3 text-left text-sm font-medium">Fecha</th>
                  <th className="px-6 py-3 text-left text-sm font-medium">Estado</th>
                  <th className="px-6 py-3 text-right text-sm font-medium">Cantidad</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {reports.flatMap((group) =>
                  group.donations.map((donation) => (
                    <tr
                      key={donation.id}
                      className={donation.deleted ? "opacity-60" : ""}
                    >
                      <td className="px-6 py-4 text-sm text-gray-900">
                        {group.categoryName}
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-900">
                        {donation.description}
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-900">
                        {donation.createdAt}
                      </td>
                      <td className="px-6 py-4">
                        <span className={donation.deleted ? "text-red-600" : "text-green-600"}>
                          {donation.deleted ? "Inactivo" : "Activo"}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-right text-sm text-gray-900">
                        {donation.quantity}
                      </td>
                    </tr>
                  ))
                )}
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
          setInitialCategoryId(null);
        }}
        onSubmit={handleSubmitFilter}
        onSearch={handleSearch}
        filterToEdit={filterToEdit}
        initialCategoryId={initialCategoryId}
      />
    </div>
  );
}