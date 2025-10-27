import React, { useState, useEffect } from "react";
import { baseUrlWebServices } from "../constants/constants.js";
import { useAuth } from "../context/AuthContext"; // Add this import
import Toast from "../components/Toast"; // Add this import

export default function DonationReportFilterModal({
  isOpen,
  onClose,
  onSubmit,
  onSearch,
  filterToEdit,
  initialCategoryId,
}) {
  const { user } = useAuth(); // Add auth context
  const isPrivileged = user.role === "PRESIDENTE" || user.role === "COORDINADOR"; // Add role check
  const [formData, setFormData] = useState({
    name: "",
    categoryId: "",
    startDate: "",
    endDate: "",
    deleted: "",
  });
  const [categories, setCategories] = useState([]);
  const [error, setError] = useState("");
  const [toast, setToast] = useState(null); // Add toast state

  useEffect(() => {
    if (isOpen) {
      fetchCategories();
      if (filterToEdit) {
        if (!filterToEdit.id) {
          console.error("filterToEdit is missing id:", filterToEdit);
          setError("El filtro seleccionado no tiene un ID válido");
        }
        setFormData({
          id: filterToEdit.id,
          name: filterToEdit.name || "",
          categoryId: filterToEdit.categoryId ? filterToEdit.categoryId.toString() : "",
          startDate: filterToEdit.startDate ? filterToEdit.startDate.slice(0, 16) : "",
          endDate: filterToEdit.endDate ? filterToEdit.endDate.slice(0, 16) : "",
          deleted: isPrivileged ? (filterToEdit.isDeleted === true ? "YES" : filterToEdit.isDeleted === false ? "NO" : "") : "NO", // Restrict deleted filter for non-privileged
        });
      } else {
        setFormData({
          name: "",
          categoryId: initialCategoryId ? initialCategoryId.toString() : "",
          startDate: "",
          endDate: "",
          deleted: isPrivileged ? "" : "NO", // Restrict deleted filter for non-privileged
        });
      }
      setError("");
    }
  }, [isOpen, filterToEdit, initialCategoryId, isPrivileged]);

  const fetchCategories = async () => {
    const token = localStorage.getItem("token");
    try {
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
      setError(`Error al cargar categorías: ${error.message}`);
      showToast(`Error al cargar categorías: ${error.message}`, "error"); // Add toast
    }
  };

  const showToast = (message, type = "success") => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3000);
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (filterToEdit && !filterToEdit.id) {
      setError("No se puede actualizar un filtro sin un ID válido");
      showToast("No se puede actualizar un filtro sin un ID válido", "error");
      return;
    }
    if (!filterToEdit && !formData.name.trim()) {
      onSearch(formData);
      onClose();
    } else if (isPrivileged || !formData.name.trim()) { // Allow saving only for privileged users
      onSubmit(formData);
      showToast("Filtro guardado correctamente", "success");
    } else {
      showToast("No tienes permisos para guardar filtros", "error");
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    if (name === "deleted" && !isPrivileged && value !== "NO") { // Restrict deleted filter changes for non-privileged
      showToast("No tienes permisos para filtrar por estado eliminado", "error");
      return;
    }
    setFormData((prev) => ({ ...prev, [name]: value }));
    setError("");
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-xl max-w-md w-full max-h-[90vh] overflow-y-auto">
        <div className="p-6">
          <h2 className="text-xl font-bold text-gray-900 mb-4">
            {filterToEdit ? "Editar Filtro" : "Nuevo Filtro"}
          </h2>

          {error && (
            <div className="mb-4 p-3 bg-red-100 text-red-700 rounded-lg">
              {error}
            </div>
          )}
          {toast && <Toast type={toast.type} message={toast.message} />} {/* Add toast component */}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                {filterToEdit ? "Nombre del Filtro" : "Nombre del Filtro (opcional para buscar)"}
              </label>
              <input
                type="text"
                name="name"
                value={formData.name}
                onChange={handleChange}
                disabled={!isPrivileged && filterToEdit} // Disable for non-privileged when editing
                className={`w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-empuje-green ${!isPrivileged && filterToEdit ? "bg-gray-100" : ""}`}
                placeholder="Ej: Donaciones 2025"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Categoría</label>
              <select
                name="categoryId"
                value={formData.categoryId}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-empuje-green"
              >
                <option value="">Todas las categorías</option>
                {categories.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.name}
                  </option>
                ))}
              </select>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Fecha Inicio</label>
                <input
                  type="datetime-local"
                  name="startDate"
                  value={formData.startDate}
                  onChange={handleChange}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-empuje-green"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Fecha Fin</label>
                <input
                  type="datetime-local"
                  name="endDate"
                  value={formData.endDate}
                  onChange={handleChange}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-empuje-green"
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Estado Eliminado</label>
              <select
                name="deleted"
                value={formData.deleted}
                onChange={handleChange}
                disabled={!isPrivileged} // Disable for non-privileged
                className={`w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-empuje-green ${!isPrivileged ? "bg-gray-100" : ""}`}
              >
                <option value="">Todos</option>
                <option value="NO">Activos</option>
                <option value="YES">Eliminados</option>
              </select>
            </div>

            <div className="flex justify-end gap-3 pt-4">
              <button
                type="button"
                onClick={onClose}
                className="px-4 py-2 text-gray-600 border border-gray-300 rounded-lg hover:bg-gray-50"
              >
                Cancelar
              </button>
              <button
                type="submit"
                className="px-4 py-2 bg-empuje-green text-white rounded-lg hover:bg-green-700"
              >
                {filterToEdit ? "Actualizar" : formData.name.trim() ? "Guardar" : "Buscar"}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}