import React, { useState, useEffect } from "react";
import { categoriesIndexes } from "../constants/Categories.js";
import { organizationById } from "../constants/organizations.js";

export default function DonationTransferModal({ isOpen, onClose, onSubmit, transferToEdit }) {
  const [organizationId, setOrganizationId] = useState("");
  const [items, setItems] = useState([{ categoryId: "", description: "", quantity: "" }]);

  useEffect(() => {
    if (transferToEdit) {
      setOrganizationId(transferToEdit.organizationId || "");
      setItems(
        transferToEdit.items && transferToEdit.items.length > 0
          ? transferToEdit.items.map(item => ({
              categoryId: item.categoryId,
              description: item.description,
              quantity: item.quantity,
            }))
          : [{ categoryId: "", description: "", quantity: "" }]
      );
    } else {
      setOrganizationId("");
      setItems([{ categoryId: "", description: "", quantity: "" }]);
    }
  }, [transferToEdit]);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!organizationId || organizationId <= 0) {
      alert("Por favor, seleccione una organización válida");
      return;
    }
    const formData = {
      id: transferToEdit?.id,
      transferId: transferToEdit?.transferId, 
      organizationId: parseInt(organizationId),
      items: items.map(item => ({
        categoryId: parseInt(item.categoryId),
        description: item.description,
        quantity: parseInt(item.quantity),
      })),
    };
    console.log("[DonationTransferModal] Submitting form data:", formData);
    onSubmit(formData);
  };

  const handleAddItem = () => {
    setItems([...items, { categoryId: "", description: "", quantity: "" }]);
  };

  const handleItemChange = (index, field, value) => {
    const newItems = [...items];
    newItems[index][field] = field === "categoryId" || field === "quantity" ? parseInt(value) || "" : value;
    setItems(newItems);
  };

  const handleRemoveItem = (index) => {
    setItems(items.filter((_, i) => i !== index));
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-gray-600 bg-opacity-50 flex justify-center items-center">
      <div className="bg-white p-6 rounded-lg shadow-lg w-1/2">
        <h2 className="text-2xl font-bold mb-4">
          {transferToEdit ? "Modificar Transferencia" : "Crear Transferencia"}
        </h2>
        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Organización
            </label>
            <select
              value={organizationId}
              onChange={(e) => setOrganizationId(e.target.value)}
              className="border p-2 rounded w-full mb-2"
              required
            >
              <option value="" disabled>Seleccione una organización</option>
              {Object.entries(organizationById).map(([id, name]) => (
                <option key={id} value={id}>{name}</option>
              ))}
            </select>
          </div>
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Ítems
            </label>
            {items.map((item, index) => (
              <div key={index} className="flex gap-2 mb-2">
                <select
                  value={item.categoryId}
                  onChange={(e) => handleItemChange(index, "categoryId", e.target.value)}
                  className="border p-2 rounded"
                  required
                >
                  <option value="" disabled>Categoría</option>
                  {Object.entries(categoriesIndexes).map(([id, name]) => (
                    <option key={id} value={id}>{name}</option>
                  ))}
                </select>
                <input
                  type="text"
                  value={item.description}
                  onChange={(e) => handleItemChange(index, "description", e.target.value)}
                  placeholder="Descripción"
                  className="border p-2 rounded flex-grow"
                  required
                />
                <input
                  type="number"
                  value={item.quantity}
                  onChange={(e) => handleItemChange(index, "quantity", e.target.value)}
                  placeholder="Cantidad"
                  className="border p-2 rounded w-1/4"
                  required
                />
                {items.length > 1 && (
                  <button
                    type="button"
                    onClick={() => handleRemoveItem(index)}
                    className="bg-red-600 text-white px-2 py-1 rounded"
                  >
                    Eliminar
                  </button>
                )}
              </div>
            ))}
            <button
              type="button"
              onClick={handleAddItem}
              className="bg-empuje-green text-white px-4 py-2 rounded mt-2"
            >
              Agregar Ítem
            </button>
          </div>
          <div className="flex justify-end gap-2">
            <button
              type="button"
              onClick={onClose}
              className="bg-gray-500 text-white px-4 py-2 rounded"
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="bg-empuje-blue text-white px-4 py-2 rounded"
            >
              Guardar
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}