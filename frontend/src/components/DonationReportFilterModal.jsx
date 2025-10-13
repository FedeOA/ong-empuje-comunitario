import React, { useState, useEffect } from "react";
import { categoriesIndexes } from "../constants/Categories.js";

export default function DonationReportFilterModal({ 
  isOpen, 
  onClose, 
  onSubmit, 
  onSearch, 
  filterToEdit 
}) {
  const [formData, setFormData] = useState({
    name: "",
    categoryId: "",
    startDate: "",
    endDate: "",
    deleted: "",
  });
  const [categories, setCategories] = useState([]);

  useEffect(() => {
    if (isOpen) {
      // Cargar categorías si es necesario
      fetchCategories();
      
      if (filterToEdit) {
        setFormData({
          name: filterToEdit.name || "",
          categoryId: filterToEdit.categoryId || "",
          startDate: filterToEdit.startDate || "",
          endDate: filterToEdit.endDate || "",
          deleted: filterToEdit.deleted || "",
        });
      } else {
        setFormData({
          name: "",
          categoryId: "",
          startDate: "",
          endDate: "",
          deleted: "",
        });
      }
    }
  }, [isOpen, filterToEdit]);

  const fetchCategories = async () => {
    const token = localStorage.getItem("token");
    try {
      const response = await fetch(`${baseUrl}/graphql`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`,
        },
        body: JSON.stringify({
          query: `
            query {
              categories {
                id
                name
              }
            }
          `
        }),
      });

      const data = await response.json();
      setCategories(data.data?.categories || []);
    } catch (error) {
      console.error("Error loading categories:", error);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (filterToEdit || formData.name.trim()) {
      onSubmit(formData);
    } else {
      onSearch(formData);
      onClose();
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-xl max-w-md w-full max-h-[90vh] overflow-y-auto">
        <div className="p-6">
          <h2 className="text-xl font-bold text-gray-900 mb-4">
            {filterToEdit ? "Editar Filtro" : "Nuevo Filtro"}
          </h2>
          
          <form onSubmit={handleSubmit} className="space-y-4">
            {!filterToEdit && (
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Nombre del Filtro (opcional)
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
            )}

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
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-empuje-green"
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