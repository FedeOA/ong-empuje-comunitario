import React, { useState, useEffect } from "react";
import { categoriesIndexes } from "../constants/Categories.js";
import { organizationById } from "../constants/organizations.js";

export default function DonationRequestModal({ isOpen, onClose, onSubmit, requestToEdit }) {
  const [items, setItems] = useState([{ categoryId: 1, description: "" }]);
  const [id, setId] = useState(null);
  const [organizationId, setOrganizationId] = useState(1); // Default to 1

  useEffect(() => {
    if (requestToEdit) {
      setId(requestToEdit.requestId);
      setOrganizationId(requestToEdit.organizationId || 1);
      setItems(requestToEdit.items || [{ categoryId: 1, description: "" }]);
    } else {
      setId(null);
      setOrganizationId(1);
      setItems([{ categoryId: 1, description: "" }]);
    }
  }, [requestToEdit]);

  const handleAddItem = () => {
    setItems([...items, { categoryId: 1, description: "" }]);
  };

  const handleItemChange = (index, field, value) => {
    const newItems = [...items];
    newItems[index][field] = field === "categoryId" ? parseInt(value) : value;
    setItems(newItems);
  };

  const handleRemoveItem = (index) => {
    setItems(items.filter((_, i) => i !== index));
  };

  const handleSubmit = () => {
    if (!organizationId || organizationId <= 0) {
      alert("Por favor, seleccione una organización válida");
      return;
    }
    const data = { id, organizationId, items };
    onSubmit(data);
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-gray-600 bg-opacity-50 flex justify-center items-center">
      <div className="bg-white p-6 rounded-lg shadow-lg w-1/2">
        <h2 className="text-2xl font-bold mb-4">
          {id ? "Modificar Solicitud" : "Crear Solicitud"}
        </h2>
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Organización
          </label>
          <select
            value={organizationId}
            onChange={(e) => setOrganizationId(parseInt(e.target.value))}
            className="border p-2 rounded w-full mb-2"
          >
            <option value="" disabled>Seleccione una organización</option>
            {Object.entries(organizationById).map(([id, name]) => (
              <option key={id} value={id}>
                {name}
              </option>
            ))}
          </select>
          {items.map((item, index) => (
            <div key={index} className="flex gap-2 mb-2">
              <select
                value={item.categoryId}
                onChange={(e) => handleItemChange(index, "categoryId", e.target.value)}
                className="border p-2 rounded"
              >
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
              <button
                onClick={() => handleRemoveItem(index)}
                className="bg-red-600 text-white px-2 py-1 rounded"
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