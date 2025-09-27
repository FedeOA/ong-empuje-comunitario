import React, { useState, useEffect } from "react";
import { categories } from "../constants/Categories.js";

export default function DonationModal({ isOpen, onClose, onSubmit, donationToEdit }) {
  const [description, setDescription] = useState("");
  const [quantity, setQuantity] = useState("");
  const [category, setCategory] = useState("");

  useEffect(() => {
    if (donationToEdit) {
      setDescription(donationToEdit.description);
      setQuantity(donationToEdit.quantity);
      setCategory(donationToEdit.category);
    } else {
      setDescription("");
      setQuantity("");
      setCategory("");
    }
  }, [donationToEdit, isOpen]);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!description || !quantity || !category) {
      alert("Por favor completa todos los campos.");
      return;
    }
   onSubmit({
    id: donationToEdit?.id,
    description,
    quantity,
    category
  });

  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div className="bg-white rounded-xl shadow-lg p-6 w-full max-w-md relative">
        <button
          className="absolute top-3 right-3 text-gray-500 hover:text-gray-800"
          onClick={onClose}
        >
          ✕
        </button>

        <h2 className="text-2xl font-bold text-empuje-green mb-4">
          {donationToEdit ? "Modificar Donación" : "Registrar Donación"}
        </h2>

        <form className="space-y-4" onSubmit={handleSubmit}>
          <div>
            <label className="block text-sm font-medium text-gray-700">
              Descripción
            </label>
            <input
              type="text"
              className="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-empuje-green focus:border-empuje-green"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Descripción de la donación"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">
              Cantidad
            </label>
            <input
              type="number"
              className="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-empuje-green focus:border-empuje-green"
              value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
              placeholder="Cantidad"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">
              Categoría
            </label>
            <select
              className="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-empuje-green focus:border-empuje-green"
              value={category}
              onChange={(e) => setCategory(e.target.value)}
            >
              <option value="">Selecciona una categoría</option>
              {Object.entries(categories).map(([key, label]) => (
                <option key={key} value={key}>
                  {label}
                </option>
              ))}
            </select>
          </div>


          <button
            type="submit"
            className="w-full bg-empuje-green text-white py-2 rounded-lg font-medium hover:bg-green-700 transition"
          >
            {donationToEdit ? "Confirmar" : "Agregar Donación"}
          </button>
        </form>
      </div>
    </div>
  );
}
