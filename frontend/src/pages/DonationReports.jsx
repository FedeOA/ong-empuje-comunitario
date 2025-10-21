import React, { useState, useEffect } from "react";
import DonationReportFilterModal from "../components/DonationReportFilterModal";
import { baseUrlWebServices } from "../constants/constants.js";
import { categoriesIndexes } from "../constants/Categories.js";
import { useAuth } from "../context/AuthContext";
import Toast from "../components/Toast";

export default function DonationReport() {
  const { user, loading: authLoading } = useAuth();
  const isPrivileged = user?.role === "PRESIDENTE" || user?.role === "COORDINADOR";
  const [reports, setReports] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [filterToEdit, setFilterToEdit] = useState(null);
  const [initialCategoryId, setInitialCategoryId] = useState(null);
  const [toast, setToast] = useState({ message: "", type: "success" });
  const [loading, setLoading] = useState(false);
  const [savedFilters, setSavedFilters] = useState([]);

  const showToast = (message, type = "success") => {
    console.log(`[Toast] Displaying toast: ${message}, type: ${type}`);
    setToast({ message, type });
    setTimeout(() => setToast({ message: "", type: "success" }), 5000);
  };

  const fetchDonationReport = async (filters = {}) => {
    console.log("[fetchDonationReport] Starting with filters:", filters);
    if (!user || !user.username) {
      console.error("[fetchDonationReport] No user or username found:", { user });
      showToast("Debes iniciar sesión para ver el informe", "error");
      return;
    }

    setLoading(true);
    try {
      console.log("[fetchDonationReport] User authenticated:", { username: user.username, role: user.role });
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

      const variables = {
        categoryId: filters.categoryId ? parseInt(filters.categoryId) : null,
        startDate: filters.startDate || null,
        endDate: filters.endDate || null,
        deleted: isPrivileged ? (filters.deleted === "YES" ? true : filters.deleted === "NO" ? false : null) : false,
      };
      console.log("[fetchDonationReport] Query variables:", variables);

      console.log("[fetchDonationReport] Fetching token from localStorage");
      const token = localStorage.getItem("token");
      if (!token) {
        console.error("[fetchDonationReport] No token found, redirecting to login");
        // Redirect to login or refresh token
        return;
      }
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        console.log("[fetchDonationReport] Token payload:", payload);
        if (payload.exp * 1000 < Date.now()) {
          console.error("[fetchDonationReport] Token expired, redirecting to login");
          // Handle token refresh or logout
        }
      } catch (e) {
        console.error("[fetchDonationReport] Invalid token format:", e);
      }
      console.log("[fetchDonationReport] Token retrieved:", token.substring(0, 10) + "...");

      console.log("[fetchDonationReport] Sending request to:", `${baseUrlWebServices}/graphql`);
      const response = await fetch(`${baseUrlWebServices}/graphql`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: token ? `Bearer ${token}` : "",
        },
        body: JSON.stringify({ query, variables }),
      });

      if (!response.ok) {
        const text = await response.text();
        console.error(`[fetchDonationReport] Error: ${response.status} ${response.statusText}`, text);
        throw new Error(`Error: ${response.status} ${response.statusText}: ${text}`);
      }

      console.log("[fetchDonationReport] Response status:", response.status);
      if (!response.ok) {
        console.error("[fetchDonationReport] Network error:", { status: response.status, statusText: response.statusText });
        throw new Error(`Error de red: ${response.status} ${response.statusText}`);
      }

      const data = await response.json();
      console.log("[fetchDonationReport] Response data:", data);
      if (data.errors) {
        console.error("[fetchDonationReport] GraphQL errors:", data.errors);
        throw new Error(data.errors[0]?.message || "Error desconocido en el servidor");
      }

      console.log("[fetchDonationReport] Setting reports:", data.data.donationReport?.length || 0, "items");
      setReports(data.data.donationReport || []);
    } catch (error) {
      console.error("[fetchDonationReport] Error:", error.message, error);
      showToast(`Error al cargar informe de donaciones: ${error.message}`, "error");
    } finally {
      console.log("[fetchDonationReport] Completed, setting loading to false");
      setLoading(false);
    }
  };

  const fetchSavedFilters = async () => {
    console.log("[fetchSavedFilters] Starting");
    if (!user || !user.username) {
      console.error("[fetchSavedFilters] No user or username found:", { user });
      showToast("Debes iniciar sesión para ver los filtros guardados", "error");
      return;
    }

    try {
      console.log("[fetchSavedFilters] User authenticated:", { username: user.username, role: user.role });
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
            userId
            username
          }
        }
      `;

      console.log("[fetchSavedFilters] Fetching token from localStorage");
      const token = localStorage.getItem("token");
      if (!token) {
        console.error("[fetchSavedFilters] No token found in localStorage");
        throw new Error("No authentication token found");
      }
      console.log("[fetchSavedFilters] Token retrieved:", token.substring(0, 10) + "...");

      console.log("[fetchSavedFilters] Sending request to:", `${baseUrlWebServices}/graphql`);
      const response = await fetch(`${baseUrlWebServices}/graphql`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: token ? `Bearer ${token}` : "",
        },
        body: JSON.stringify({ query }), // Remove variables since none are used
      });
      if (!response.ok) {
        const text = await response.text();
        console.error(`[fetchSavedFilters] Error: ${response.status} ${response.statusText}`, text);
        throw new Error(`Error al cargar filtros guardados: ${response.status}`);
      }

      console.log("[fetchSavedFilters] Response status:", response.status);
      const data = await response.json();
      console.log("[fetchSavedFilters] Response data:", data);
      if (data.errors) {
        console.error("[fetchSavedFilters] GraphQL errors:", data.errors);
        throw new Error(data.errors[0]?.message || "Error desconocido en el servidor");
      }

      const userFilters = data.data.savedFilters.filter(
        (filter) => filter.username === user.username && (isPrivileged || !filter.isDeleted)
      );
      console.log("[fetchSavedFilters] Filtered user filters:", userFilters.length, "items");
      setSavedFilters(userFilters || []);
    } catch (error) {
      console.error("[fetchSavedFilters] Error:", error.message, error);
      showToast(`Error al cargar filtros guardados: ${error.message}`, "error");
    }
  };

  useEffect(() => {
    if (!authLoading && user?.username) {
      fetchDonationReport();
      fetchSavedFilters();
    }
  }, [authLoading, user?.username]);

  const openFilterModal = (filter = null, categoryId = null) => {
    console.log("[openFilterModal] Opening modal, filter:", filter, "categoryId:", categoryId);
    if (!user || !user.username) {
      console.error("[openFilterModal] No user or username found:", { user });
      showToast("Debes iniciar sesión para gestionar filtros", "error");
      return;
    }
    if (!isPrivileged && filter?.isDeleted) {
      console.error("[openFilterModal] Non-privileged user attempting to edit deleted filter:", filter);
      showToast("No tienes permisos para editar filtros eliminados", "error");
      return;
    }
    setFilterToEdit(filter);
    setInitialCategoryId(categoryId);
    setIsModalOpen(true);
    console.log("[openFilterModal] Modal opened with filterToEdit:", filter, "initialCategoryId:", categoryId);
  };

  const handleSubmitFilter = async (formData) => {
    console.log("[handleSubmitFilter] Starting with formData:", formData);
    if (!user || !user.username) {
      console.error("[handleSubmitFilter] No user or username found:", { user });
      showToast("Debes iniciar sesión y tener un nombre de usuario válido para guardar filtros", "error");
      return;
    }
    if (!isPrivileged && formData.deleted === "YES") {
      console.error("[handleSubmitFilter] Non-privileged user attempting to set deleted filter:", formData);
      showToast("No tienes permisos para guardar filtros con estado eliminado", "error");
      return;
    }
    if (!isPrivileged && formData.id && filterToEdit?.isDeleted) {
      console.error("[handleSubmitFilter] Non-privileged user attempting to edit deleted filter:", filterToEdit);
      showToast("No tienes permisos para editar filtros eliminados", "error");
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
          userId
          username
        }
      }
    `;

    const variables = {
      id: formData.id,
      input: {
        name: formData.name,
        categoryId: formData.categoryId ? parseInt(formData.categoryId) : null,
        startDate: formData.startDate || null,
        endDate: formData.endDate || null,
        deleted: isPrivileged ? (formData.deleted === "YES" ? true : formData.deleted === "NO" ? false : null) : false,
        username: user.username,
      },
    };
    console.log("[handleSubmitFilter] Mutation variables:", variables);

    try {
      console.log("[handleSubmitFilter] Fetching token from localStorage");
      const token = localStorage.getItem("token");
      if (!token) {
        console.error("[handleSubmitFilter] No token found in localStorage");
        showToast("No se encontró el token de autenticación. Por favor, inicia sesión nuevamente.", "error");
        return;
      }
      console.log("[handleSubmitFilter] Token retrieved:", token.substring(0, 10) + "...");

      console.log("[handleSubmitFilter] Sending request to:", `${baseUrlWebServices}/graphql`);
      const response = await fetch(`${baseUrlWebServices}/graphql`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: token ? `Bearer ${token}` : "",
        },
        body: JSON.stringify({ query: mutation, variables }), // Use mutation instead of query
      });

      if (!response.ok) {
        const text = await response.text();
        console.error(`[handleSubmitFilter] Error: ${response.status} ${response.statusText}`, text);
        throw new Error(`Error: ${response.status} ${response.statusText}: ${text}`);
      }

      console.log("[handleSubmitFilter] Response status:", response.status);
      const data = await response.json();
      console.log("[handleSubmitFilter] Response data:", data);
      if (data.errors) {
        console.error("[handleSubmitFilter] GraphQL errors:", data.errors);
        throw new Error(data.errors[0]?.message || "Error desconocido al procesar el filtro");
      }

      console.log("[handleSubmitFilter] Filter processed successfully");
      showToast(formData.id ? "Filtro actualizado con éxito" : "Filtro guardado con éxito");
      fetchSavedFilters();
      setIsModalOpen(false);
    } catch (error) {
      console.error("[handleSubmitFilter] Error:", error.message, error);
      showToast(`Error al procesar el filtro: ${error.message}`, "error");
    }
  };

  const handleDeleteFilter = async (filterId) => {
    console.log("[handleDeleteFilter] Starting with filterId:", filterId);
    if (!user || !user.username) {
      console.error("[handleDeleteFilter] No user or username found:", { user });
      showToast("Debes iniciar sesión para eliminar filtros", "error");
      return;
    }
    if (!isPrivileged && savedFilters.find((f) => f.id === filterId)?.isDeleted) {
      console.error("[handleDeleteFilter] Non-privileged user attempting to delete deleted filter:", filterId);
      showToast("No tienes permisos para eliminar filtros eliminados", "error");
      return;
    }

    if (!confirm("¿Está seguro de eliminar este filtro?")) {
      console.log("[handleDeleteFilter] Deletion cancelled by user");
      return;
    }

    try {
      console.log("[handleDeleteFilter] Fetching token from localStorage");
      const token = localStorage.getItem("token");
      if (!token) {
        console.error("[handleDeleteFilter] No token found in localStorage");
        throw new Error("No authentication token found");
      }
      console.log("[handleDeleteFilter] Token retrieved:", token.substring(0, 10) + "...");

      const mutation = `
        mutation DeleteFilter($id: ID!) {
          deleteFilter(id: $id)
        }
      `;

      const variables = { id: filterId }; // Define variables for the mutation

      console.log("[handleDeleteFilter] Sending request to:", `${baseUrlWebServices}/graphql`);
      const response = await fetch(`${baseUrlWebServices}/graphql`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: token ? `Bearer ${token}` : "",
        },
        body: JSON.stringify({ query: mutation, variables }), // Use mutation here
      });

      if (!response.ok) {
        const text = await response.text();
        console.error(`[handleDeleteFilter] Error: ${response.status} ${response.statusText}`, text);
        throw new Error(`Error: ${response.status} ${response.statusText}: ${text}`);
      }

      console.log("[handleDeleteFilter] Response status:", response.status);
      const data = await response.json();
      console.log("[handleDeleteFilter] Response data:", data);
      if (data.errors) {
        console.error("[handleDeleteFilter] GraphQL errors:", data.errors);
        throw new Error(data.errors[0]?.message || "Error desconocido en el servidor");
      }

      console.log("[handleDeleteFilter] Filter deleted successfully");
      showToast("Filtro eliminado correctamente");
      fetchSavedFilters();
    } catch (error) {
      console.error("[handleDeleteFilter] Error:", error.message, error);
      showToast(`Error al eliminar el filtro: ${error.message}`, "error");
    }
  };

  const handleApplyFilter = (filter) => {
    console.log("[handleApplyFilter] Starting with filter:", filter);
    if (!user || !user.username) {
      console.error("[handleApplyFilter] No user or username found:", { user });
      showToast("Debes iniciar sesión para aplicar filtros", "error");
      return;
    }
    if (!isPrivileged && filter.isDeleted) {
      console.error("[handleApplyFilter] Non-privileged user attempting to apply deleted filter:", filter);
      showToast("No tienes permisos para aplicar filtros eliminados", "error");
      return;
    }

    const appliedFilters = {
      categoryId: filter.categoryId,
      startDate: filter.startDate,
      endDate: filter.endDate,
      deleted: isPrivileged ? (filter.isDeleted === true ? "YES" : filter.isDeleted === false ? "NO" : null) : "NO",
    };
    console.log("[handleApplyFilter] Applying filters:", appliedFilters);
    fetchDonationReport(appliedFilters);
    showToast(`Filtro "${filter.name}" aplicado`);
  };

  const handleSearch = (filters) => {
    console.log("[handleSearch] Starting with filters:", filters);
    if (!user || !user.username) {
      console.error("[handleSearch] No user or username found:", { user });
      showToast("Debes iniciar sesión para buscar", "error");
      return;
    }
    if (!isPrivileged && filters.deleted === "YES") {
      console.error("[handleSearch] Non-privileged user attempting to search deleted donations:", filters);
      showToast("No tienes permisos para buscar donaciones eliminadas", "error");
      return;
    }
    console.log("[handleSearch] Triggering fetch with filters:", filters);
    fetchDonationReport(filters);
  };

  console.log("[DonationReport] Rendering, authLoading:", authLoading, "user:", user, "reports:", reports.length, "savedFilters:", savedFilters.length);

  if (authLoading) {
    return (
      <div className="min-h-screen bg-empuje-bg p-6 flex justify-center items-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-empuje-green"></div>
        <span className="ml-3 text-gray-600">Cargando...</span>
      </div>
    );
  }

  if (!user) {
    console.warn("[DonationReport] No user, rendering login prompt");
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
            disabled={!isPrivileged}
          >
            Nuevo Filtro
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
                  {filter.isDeleted === true ? "Eliminados" : filter.isDeleted === false ? "Activos" : "Todos"}
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
                    disabled={!isPrivileged && filter.isDeleted}
                  >
                    Editar
                  </button>
                  <button
                    className="bg-red-600 text-white px-3 py-1 rounded hover:bg-red-700 transition text-sm"
                    onClick={() => handleDeleteFilter(filter.id)}
                    disabled={!isPrivileged && filter.isDeleted}
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
                  <th className="px-6 py-3 text-left text-sm font-medium">Estado</th>
                  <th className="px-6 py-3 text-right text-sm font-medium">Cantidad Total</th>
                  <th className="px-6 py-3 text-left text-sm font-medium">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {reports.map((group) => (
                  <tr
                    key={`${group.categoryId}-${group.deleted}`}
                    className={group.deleted ? "opacity-60" : ""}
                  >
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
                        onClick={() => openFilterModal(null, group.categoryId)}
                        disabled={!isPrivileged && group.deleted}
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

      {toast.message && <Toast message={toast.message} type={toast.type} />}

      <DonationReportFilterModal
        isOpen={isModalOpen}
        onClose={() => {
          console.log("[DonationReportFilterModal] Closing modal");
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