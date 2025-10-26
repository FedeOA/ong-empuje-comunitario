import React, { useState, useEffect } from "react";
import { baseUrlGraphQL } from "../constants/constants.js";

export default function DonationReportFilterModal({
  isOpen,
  onClose,
  onSubmit,
  onSearch,
  filterToEdit,
  initialCategoryId,
}) {
  const [formData, setFormData] = useState({
    name: "",
    categoryId: "",
    startDate: "",
    endDate: "",
    filterDeleted: "",
  });
  const [categories, setCategories] = useState([]);
  const [error, setError] = useState("");

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
          filterDeleted: filterToEdit.filterDeleted === true ? "true" : filterToEdit.filterDeleted === false ? "false" : "",
        });
      } else {
        setFormData({
          name: "",
          categoryId: initialCategoryId ? initialCategoryId.toString() : "",
          startDate: "",
          endDate: "",
          filterDeleted: "",
        });
      }
      setError("");
    }
  }, [isOpen, filterToEdit, initialCategoryId]);

  const fetchCategories = async () => {
    const token = localStorage.getItem("token");
    try {
      const response = await fetch(`${baseUrlGraphQL}/graphql`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          ...(token && { Authorization: `Bearer ${token}` }),
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
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (filterToEdit && !filterToEdit.id) {
      setError("No se puede actualizar un filtro sin un ID válido");
      return;
    }
    if (!formData.categoryId) {
      setError("Debes seleccionar una categoría");
      return;
    }
    if (!formData.filterDeleted) {
      setError("Debes seleccionar un estado (Activos o Inactivos)");
      return;
    }
    if (!filterToEdit && !formData.name.trim()) {
      const searchData = {
        ...formData,
        filterDeleted: formData.filterDeleted === "true" ? true : formData.filterDeleted === "false" ? false : null,
      };
      onSearch(searchData);
      onClose();
    } else {
      const submitData = {
        ...formData,
        filterDeleted: formData.filterDeleted === "true" ? true : formData.filterDeleted === "false" ? false : null,
      };
      onSubmit(submitData);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
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

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                {filterToEdit ? "Nombre del Filtro" : "Nombre del Filtro"}
              </label>
              <input
                type="text"
                name="name"
                value={formData.name}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-empuje-green"
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
                <option value="" disabled>
                  Selecciona una categoría
                </option>
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
              <label className="block text-sm font-medium text-gray-700 mb-2">Estado</label>
              <select
                name="filterDeleted"
                value={formData.filterDeleted}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-empuje-green"
              >
                <option value="" disabled>
                  Selecciona un estado
                </option>
                <option value="false">Activos</option>
                <option value="true">Inactivos</option>
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