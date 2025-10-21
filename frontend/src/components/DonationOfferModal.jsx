import React, { useState, useEffect } from "react";
import { categoriesIndexes } from "../constants/Categories.js";
import { organizationById } from "../constants/organizations.js";

export default function DonationOfferModal({ isOpen, onClose, onSubmit, offerToEdit }) {
  const [formData, setFormData] = useState({
    id: null,
    organizationId: 1, // Default to 1
    expiresAt: "", // Store as YYYY-MM-DD for input
    items: [{ categoryId: 1, description: "", quantity: 1 }],
  });

  useEffect(() => {
    if (offerToEdit) {
      setFormData({
        id: offerToEdit.offerId || null,
        organizationId: offerToEdit.organizationId || 1,
        expiresAt: offerToEdit.expiresAt ? new Date(offerToEdit.expiresAt).toISOString().split('T')[0] : "",
        items: offerToEdit.items || [{ categoryId: 1, description: "", quantity: 1 }],
      });
    } else {
      setFormData({
        id: null,
        organizationId: 1,
        expiresAt: "",
        items: [{ categoryId: 1, description: "", quantity: 1 }],
      });
    }
  }, [offerToEdit]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleItemChange = (index, field, value) => {
    const newItems = [...formData.items];
    newItems[index][field] = field === "categoryId" || field === "quantity" ? parseInt(value) : value;
    setFormData((prev) => ({ ...prev, items: newItems }));
  };

  const handleAddItem = () => {
    setFormData((prev) => ({
      ...prev,
      items: [...prev.items, { categoryId: 1, description: "", quantity: 1 }],
    }));
  };

  const handleRemoveItem = (index) => {
    setFormData((prev) => ({
      ...prev,
      items: prev.items.filter((_, i) => i !== index),
    }));
  };

  const handleSubmit = () => {
    if (!formData.organizationId || formData.organizationId <= 0) {
      alert("Por favor, seleccione una organización válida");
      return;
    }
    if (formData.items.some(item => !item.categoryId || !item.description || item.quantity <= 0)) {
      alert("Por favor, complete todos los campos de los ítems correctamente");
      return;
    }
    const formattedData = {
      ...formData,
      organizationId: parseInt(formData.organizationId),
      expiresAt: formData.expiresAt ? 
        new Date(formData.expiresAt)
          .toISOString()
          .split('T')[0]
          .replace(/\d{2}(\d{2})-(\d{2})-(\d{2})/, '$1:$2:$3') : null, // Convert YYYY-MM-DD to YY:MM:DD
      items: formData.items.map((item) => ({
        ...item,
        categoryId: parseInt(item.categoryId),
        quantity: parseInt(item.quantity),
      })),
    };
    console.log("[DonationOfferModal] Submitting form data:", formattedData);
    onSubmit(formattedData);
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-gray-600 bg-opacity-50 flex justify-center items-center">
      <div className="bg-white p-6 rounded-lg shadow-lg w-1/2">
        <h2 className="text-2xl font-bold mb-4">
          {formData.id ? "Modificar Oferta de Donación" : "Crear Oferta de Donación"}
        </h2>
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Organización
          </label>
          <select
            name="organizationId"
            value={formData.organizationId}
            onChange={handleChange}
            className="border p-2 rounded w-full mb-2"
          >
            <option value="" disabled>Seleccione una organización</option>
            {Object.entries(organizationById).map(([id, name]) => (
              <option key={id} value={id}>
                {name}
              </option>
            ))}
          </select>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Fecha de Expiración
          </label>
          <input
            type="date"
            name="expiresAt"
            value={formData.expiresAt}
            onChange={handleChange}
            className="border p-2 rounded w-full mb-2"
          />
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Ítems
          </label>
          {formData.items.map((item, index) => (
            <div key={index} className="flex gap-2 mb-2">
              <select
                value={item.categoryId}
                onChange={(e) => handleItemChange(index, "categoryId", e.target.value)}
                className="border p-2 rounded"
              >
                <option value="" disabled>Seleccione una categoría</option>
                {Object.entries(categoriesIndexes).map(([id, name]) => (
                  <option key={id} value={id}>
                    {name}
                  </option>
                ))}
              </select>
              <input
                type="text"
                value={item.description}
                onChange={(e) => handleItemChange(index, "description", e.target.value)}
                placeholder="Descripción"
                className="border p-2 rounded flex-grow"
              />
              <input
                type="number"
                value={item.quantity}
                onChange={(e) => handleItemChange(index, "quantity", e.target.value)}
                placeholder="Cantidad"
                className="border p-2 rounded w-24"
                min="1"
              />
              <button
                onClick={() => handleRemoveItem(index)}
                className="bg-red-600 text-white px-2 py-1 rounded"
                disabled={formData.items.length === 1}
              >
                Eliminar
              </button>
            </div>
          ))}
          <button
            onClick={handleAddItem}
            className="bg-empuje-green text-white px-4 py-2 rounded mt-2"
          >
            Agregar Ítem
          </button>
        </div>
        <div className="flex justify-end gap-2">
          <button
            onClick={onClose}
            className="bg-gray-500 text-white px-4 py-2 rounded"
          >
            Cancelar
          </button>
          <button
            onClick={handleSubmit}
            className="bg-empuje-blue text-white px-4 py-2 rounded"
          >
            Guardar
          </button>
        </div>
      </div>
    </div>
  );
}