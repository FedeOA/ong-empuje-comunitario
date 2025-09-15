import React, { useState } from "react";
import DonationModal from "../components/DonationModal";

// Donaciones iniciales de ejemplo
const initialDonations = [
  { id: 1, description: "Alimentos no perecederos", amount: 50, category: "Comida" },
  { id: 2, description: "Ropa de abrigo", amount: 30, category: "Ropa" }
];


export default function Donations() {
  const [donations, setDonations] = useState(initialDonations);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [donationToEdit, setDonationToEdit] = useState(null);

  // Abrir modal para agregar nueva donación
  const handleAddDonation = () => {
    setDonationToEdit(null);
    setIsModalOpen(true);
  };

  // Abrir modal para modificar donación
  const handleEditDonation = (donation) => {
    setDonationToEdit(donation);
    setIsModalOpen(true);
  };

  // Guardar donación agregada o modificada
  const handleSubmitDonation = (data) => {
    if (donationToEdit) {
      setDonations(donations.map(d => d.id === donationToEdit.id ? { ...d, ...data } : d));
    } else {
      const id = donations.length + 1;
      setDonations([...donations, { ...data, id }]);
    }
    setIsModalOpen(false);
  };

  return (
    <div className="min-h-screen bg-empuje-bg p-6">
      {/* Título + botón agregar */}
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-empuje-green">Donaciones</h1>
        <button
          className="bg-empuje-green text-white px-4 py-2 rounded-lg hover:bg-green-700 transition"
          onClick={handleAddDonation}
        >
          Registrar Donación
        </button>
      </div>

      {/* Tabla de donaciones */}
      <div className="bg-white shadow-md rounded-xl overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-empuje-green text-white">
            <tr>
              <th className="px-6 py-3 text-left text-sm font-medium">Descripción</th>
              <th className="px-6 py-3 text-left text-sm font-medium">Cantidad</th>
              <th className="px-6 py-3 text-left text-sm font-medium">Categoría</th>
              <th className="px-6 py-3 text-center text-sm font-medium">Acciones</th>
            </tr>
          </thead>

          <tbody className="divide-y divide-gray-200">
            {donations.map(donation => (
              <tr key={donation.id}>
                <td className="px-6 py-4">{donation.description}</td>
                <td className="px-6 py-4">{donation.amount}</td>
                <td className="px-6 py-4">{donation.category}</td>
                <td className="px-6 py-4 flex justify-center gap-2">
                  <button
                    className="bg-empuje-blue text-white px-3 py-1 rounded hover:bg-blue-700 transition"
                    onClick={() => handleEditDonation(donation)}
                  >
                    Modificar
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Modal de agregar/modificar donación */}
      <DonationModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSubmit={handleSubmitDonation}
        donationToEdit={donationToEdit}
      />
    </div>
  );
}
