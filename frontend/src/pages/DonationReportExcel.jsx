// frontend/src/pages/DonationReportExcel.jsx
import React, { useState, useEffect } from "react";
import { baseUrlWebServices } from "../constants/constants";
import { useAuth } from "../context/AuthContext";
import Toast from "../components/Toast";

const DonationReportExcel = () => {
  const { user } = useAuth();
  const [filters, setFilters] = useState({
    categoryId: "",
    startDate: "",
    endDate: "",
    deleted: false,
  });
  const [categories, setCategories] = useState([]);
  const [toast, setToast] = useState({ message: "", type: "success" });
  const [loading, setLoading] = useState(false);

  const showToast = (message, type = "success") => {
    setToast({ message, type });
    setTimeout(() => setToast({ message: "", type: "success" }), 3000);
  };

  const fetchCategories = async () => {
    const token = localStorage.getItem("token");
    try {
      setLoading(true);
      const response = await fetch(`${baseUrlWebServices}/graphql`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: token ? `Bearer ${token}` : "",
        },
        body: JSON.stringify({
          query: `
            query {
              categories {
                id
                name
              }
            }
          `,
        }),
      });

      if (!response.ok) {
        throw new Error(`Error de red: ${response.status} ${response.statusText}`);
      }

      const data = await response.json();
      if (data.errors) {
        throw new Error(data.errors[0]?.message || "Error desconocido al cargar categorías");
      }

      setCategories(data.data?.categories || []);
    } catch (error) {
      console.error("Error al cargar categorías:", error);
      showToast(`Error al cargar categorías: ${error.message}`, "error");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (user?.username) {
      fetchCategories();
    }
  }, [user?.username]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFilters((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handleDownloadExcel = async () => {
    try {
      if (!filters.startDate || !filters.endDate) {
        showToast("Por favor, selecciona ambas fechas", "error");
        return;
      }
      if (new Date(filters.startDate) > new Date(filters.endDate)) {
        showToast("La fecha de inicio debe ser anterior a la fecha de fin", "error");
        return;
      }

      setLoading(true);
      const token = localStorage.getItem("token");
      const categoryId = filters.categoryId ? parseInt(filters.categoryId, 10) : null;

      if (filters.categoryId && !categories.find((cat) => cat.id === parseInt(filters.categoryId, 10))) {
        console.warn(`[DonationReportExcel] Invalid category ID: ${filters.categoryId}`);
        showToast("Categoría seleccionada no válida", "error");
        return;
      }

      const payload = {
        categoryId,
        startDate: filters.startDate ? new Date(filters.startDate).toISOString() : null,
        endDate: filters.endDate ? new Date(filters.endDate).toISOString() : null,
        deleted: filters.deleted === "both" ? null : filters.deleted === "true"  // null for both
      };
      console.log("Payload deleted:", payload.deleted); 

      console.log("[handleDownloadExcel] Payload:", payload);

      const response = await fetch(`${baseUrlWebServices}/api/reports/donations/excel`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "Error al descargar el reporte");
      }

      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const timestamp = new Date().toISOString().replace(/[:.]/g, "");
      const a = document.createElement("a");
      a.href = url;
      a.download = `reporte_donaciones_${timestamp}.xlsx`;
      a.click();
      window.URL.revokeObjectURL(url);
      showToast("Reporte descargado correctamente", "success");
    } catch (error) {
      console.error("Error downloading Excel:", error);
      showToast(`Error al descargar el reporte: ${error.message}`, "error");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-empuje-bg p-6">
      {/* Header */}
      <div className="flex justify-between items-center mb-6 w-full">
        <h1 className="text-3xl font-bold text-empuje-green">Descargar Reporte de Donaciones</h1>
        <button
          className="bg-empuje-green text-white px-4 py-2 rounded-lg hover:bg-green-700 transition disabled:bg-gray-400"
          onClick={handleDownloadExcel}
          disabled={loading}
        >
          {loading ? "Descargando..." : "Descargar Excel"}
        </button>
      </div>

      {/* Form */}
      <div className="bg-white shadow-md rounded-xl p-6 w-full">
        <div className="grid grid-cols-1 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700">Categoría</label>
            <select
              name="categoryId"
              value={filters.categoryId}
              onChange={handleChange}
              className="mt-1 block w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-empuje-green focus:border-empuje-green"
              disabled={loading}
              aria-label="Selecciona una categoría"
            >
              <option value="">Todas las categorías</option>
              {categories.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700">Fecha de Inicio</label>
            <input
              type="date"
              name="startDate"
              value={filters.startDate}
              onChange={handleChange}
              className="mt-1 block w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-empuje-green focus:border-empuje-green"
              disabled={loading}
              aria-label="Fecha de inicio"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700">Fecha de Fin</label>
            <input
              type="date"
              name="endDate"
              value={filters.endDate}
              onChange={handleChange}
              className="mt-1 block w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-empuje-green focus:border-empuje-green"
              disabled={loading}
              aria-label="Fecha de fin"
            />
          </div>
          <div className="flex items-center">
            <div>
              <label className="block text-sm font-medium text-gray-700">Estado de Donaciones</label>
              <select
                name="deleted"
                value={filters.deleted || "both"}  // "both" por default
                onChange={handleChange}
                className="mt-1 block w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-empuje-green focus:border-empuje-green"
                disabled={loading}
                aria-label="Selecciona estado de donaciones"
              >
                <option value="both">Ambos (Activos y Eliminados)</option>
                <option value="false">Solo Activos</option>
                <option value="true">Solo Eliminados</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      {toast.message && <Toast message={toast.message} type={toast.type} />}
    </div>
  );
};

export default DonationReportExcel;