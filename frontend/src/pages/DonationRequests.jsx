import React, { useState, useEffect } from "react";
import DonationRequestModal from "../components/DonationRequestModal";
import Toast from "../components/Toast";
import { baseUrl } from "../constants/constants.js";
import { categoriesIndexes } from "../constants/Categories.js";
import { organizationById } from "../constants/organizations.js";

export default function DonationRequests() {
  const [requests, setRequests] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [requestToEdit, setRequestToEdit] = useState(null);
  const [toast, setToast] = useState({ message: "", type: "success" });

  const showToast = (message, type = "success") => {
    setToast({ message, type });
    setTimeout(() => setToast({ message: "", type: "success" }), 3000);
  };

  const fetchDonationRequests = async () => {
    const token = localStorage.getItem("token");
    try {
      const response = await fetch("http://localhost:8092/api/donation-requests", {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`,
        },
      });

      if (!response.ok) throw new Error("Error al cargar solicitudes de donación");

      const data = await response.json();
      console.log("Fetched donation requests:", JSON.stringify(data, null, 2));
      setRequests(data);
    } catch (error) {
      console.error("Error al cargar solicitudes de donación:", error);
      showToast("Error al cargar solicitudes de donación", "error");
    }
  };

  useEffect(() => {
    fetchDonationRequests();
  }, []);

  const openRequestModal = (request = null) => {
    setRequestToEdit(request);
    setIsModalOpen(true);
  };

  const handleSubmitRequest = async (data) => {
    const token = localStorage.getItem("token");
    try {
      const payload = {
        requestId: data.id || Math.floor(Math.random() * 1000000),
        organizationId: data.organizationId,
        items: data.items.map(item => ({
          categoryId: item.categoryId || 1, // Default to 1 (ALIMENTOS) if missing
          description: item.description || "Sin descripción",
        })),
      };

      console.log("Submitting donation request payload:", JSON.stringify(payload, null, 2));

      const response = await fetch("http://localhost:8081/kafka/publish/donation-request", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`,
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) throw new Error("Error al procesar la solicitud de donación");

      showToast(data.id ? "Solicitud modificada correctamente" : "Solicitud creada correctamente");
      setTimeout(() => fetchDonationRequests(), 3000); // Delay refresh by 3 seconds
      setIsModalOpen(false);
    } catch (error) {
      console.error("Error al procesar la solicitud:", error);
      showToast("Error al procesar la solicitud", "error");
    }
  };

  const handleCancelRequest = async (requestId, orgId) => {
    const token = localStorage.getItem("token");
    try {
      const response = await fetch("http://localhost:8081/kafka/publish/donation-cancellation", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`,
        },
        body: JSON.stringify({ requestId, organizationId: orgId }),
      });

      if (!response.ok) throw new Error("Error al cancelar la solicitud");

      showToast("Solicitud cancelada correctamente");
      setTimeout(() => fetchDonationRequests(), 3000); // Delay refresh by 3 seconds
    } catch (error) {
      console.error("Error al cancelar la solicitud:", error);
      showToast("Error al cancelar la solicitud", "error");
    }
  };

  return (
    <div className="min-h-screen bg-empuje-bg p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-empuje-green">Solicitudes de Donación</h1>
        <button
          className="bg-empuje-green text-white px-4 py-2 rounded-lg hover:bg-green-700 transition"
          onClick={() => openRequestModal()}
        >
          Crear Solicitud
        </button>
      </div>

      <div className="bg-white shadow-md rounded-xl overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-empuje-green text-white">
            <tr>
              <th className="px-6 py-3 text-left text-sm font-medium">ID Solicitud</th>
              <th className="px-6 py-3 text-left text-sm font-medium">Organización</th>
              <th className="px-6 py-3 text-left text-sm font-medium">Ítems</th>
              <th className="px-6 py-3 text-left text-sm font-medium">Estado</th>
              <th className="px-6 py-3 text-center text-sm font-medium">Acciones</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {requests.map((request) => (
              <tr key={`${request.requestId}-${request.organizationId}`}>
                <td className="px-6 py-4">{request.requestId || "Sin ID"}</td>
                <td className="px-6 py-4">{organizationById[request.organizationId] || "Sin Org"}</td>
                <td className="px-6 py-4">
                  <ul>
                    {request.items && request.items.length > 0 ? (
                      request.items.map((item, idx) => (
                        <li key={idx}>
                          {categoriesIndexes[item.categoryId] || "Desconocido"}: {item.description || "Sin descripción"}
                        </li>
                      ))
                    ) : (
                      <li>Sin ítems</li>
                    )}
                  </ul>
                </td>
                <td className="px-6 py-4">
                  <span className={request.deleted ? "text-red-600" : "text-green-600"}>
                    {request.deleted ? "Cancelada" : "Activa"}
                  </span>
                </td>
                <td className="px-6 py-4 flex justify-center gap-2">
                  {!request.deleted && (
                    <>
                      <button
                        className="bg-red-600 text-white px-3 py-1 rounded hover:bg-red-700 transition"
                        onClick={() => handleCancelRequest(request.requestId, request.organizationId)}
                      >
                        Cancelar
                      </button>
                    </>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <DonationRequestModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSubmit={handleSubmitRequest}
        requestToEdit={requestToEdit}
      />

      <Toast
        message={toast.message}
        type={toast.type}
        onClose={() => setToast({ message: "", type: "success" })}
      />
    </div>
  );
}