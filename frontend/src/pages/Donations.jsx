import React, { useState, useEffect } from "react";
import DonationModal from "../components/DonationModal";
import { baseUrl } from "../constants/constants.js";
import Toast from "../components/Toast";

export default function Donations() {
  const [donations, setDonations] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [donationToEdit, setDonationToEdit] = useState(null);
  const [toast, setToast] = useState({ message: "", type: "success" });

  const showToast = (message, type = "success") => {
    setToast({ message, type });
    setTimeout(() => setToast({ message: "", type: "success" }), 3000);
  };

  const fetchDonations = async () => {
    try {
      const token = localStorage.getItem("token");
      const response = await fetch(`${baseUrl}/donations`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`,
        },
      });
      const data = await response.json();
      setDonations(data);
    } catch (error) {
      console.error("Error al cargar donaciones:", error);
      showToast("Error al cargar donaciones", "error");
    }
  };

  useEffect(() => {
    fetchDonations();
  }, []);

  const handleAddDonation = () => {
    setDonationToEdit(null);
    setIsModalOpen(true);
  };

  const handleEditDonation = (donation) => {
    setDonationToEdit(donation);
    setIsModalOpen(true);
  };

  const handleSubmitDonation = async (data) => {
    try {
      const token = localStorage.getItem("token");
      const { id, ...payload } = data;

      const response = await fetch(
        id ? `${baseUrl}/donations/${id}` : `${baseUrl}/donations`,
        {
          method: id ? "PUT" : "POST",
          headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
          },
          body: JSON.stringify(payload)
        }
      );

      if (!response.ok) throw new Error("Error al guardar la donación");

      await fetchDonations();
      setIsModalOpen(false);
      setDonationToEdit(null);

      showToast(
        id
          ? "Donación modificada con éxito"
          : "Donación registrada correctamente",
        "success"
      );
    } catch (error) {
      console.error(error);
      showToast("Hubo un problema al procesar la donación.", "error");
    }
  };

  const handleDeleteDonation = async (donation) => {
    const confirm = window.confirm("¿Estás seguro de que querés dar de baja esta donación?");
    if (!confirm) return;

    try {
      const token = localStorage.getItem("token");
      const response = await fetch(`${baseUrl}/donations/${donation.id}`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({ eliminado: true })
      });

      if (!response.ok) throw new Error("Error al dar de baja la donación");

      await fetchDonations();
      showToast("Donación dada de baja correctamente", "success");
    } catch (error) {
      console.error(error);
      showToast("Hubo un problema al dar de baja la donación.", "error");
    }
  };

  return (
    <div className="min-h-screen bg-empuje-bg p-6 relative">
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
                <td className="px-6 py-4">{donation.quantity}</td>
                <td className="px-6 py-4">{donation.category}</td>
                <td className="px-6 py-4 flex justify-center gap-2">
                  <button
                    className="bg-empuje-blue text-white px-3 py-1 rounded hover:bg-blue-700 transition"
                    onClick={() => handleEditDonation(donation)}
                  >
                    Modificar
                  </button>
                  <button
                    className="bg-empuje-orange text-white px-3 py-1 rounded hover:bg-orange-700 transition"
                    onClick={() => handleDeleteDonation(donation)}
                  >
                    Eliminar
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

      {/* Toast visual */}
      {toast.message && (
        <Toast message={toast.message} type={toast.type} />
      )}
    </div>
  );
}
