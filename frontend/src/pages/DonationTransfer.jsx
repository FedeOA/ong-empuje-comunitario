import React, { useState, useEffect } from "react";
import DonationTransferModal from "../components/DonationTransferModal";
import { categoriesIndexes } from "../constants/Categories.js";
import { organizationById } from "../constants/organizations.js";
import Toast from "../components/Toast";
import { useAuth } from "../context/AuthContext";

export default function DonationTransfer() {
  const { user, loading: authLoading } = useAuth();
  const [transfers, setTransfers] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [transferToEdit, setTransferToEdit] = useState(null);
  const [toast, setToast] = useState({ message: "", type: "success" });

  const showToast = (message, type = "success") => {
    console.log(`[Toast] Displaying toast: ${message}, type: ${type}`);
    setToast({ message, type });
    setTimeout(() => setToast({ message: "", type: "success" }), 3000);
  };

  const fetchTransfers = async () => {
    if (!user || !user.username) {
      console.error("[fetchTransfers] No user or username found:", { user });
      showToast("Debes iniciar sesión para ver las transferencias", "error");
      return;
    }

    const token = localStorage.getItem("token");
    if (!token) {
      console.error("[fetchTransfers] No authentication token found");
      showToast("No se encontró el token de autenticación. Por favor, inicia sesión.", "error");
      return;
    }

    try {
      console.log("[fetchTransfers] Fetching donation transfers");
      const response = await fetch(`http://localhost:8092/api/donation-transfers`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        if (response.status === 403 || response.status === 401) {
          console.error("[fetchTransfers] Unauthorized or Forbidden access:", response.status);
          showToast("Acceso no autorizado. Verifica que tengas los permisos necesarios (PRESIDENTE o VOCAL).", "error");
          return;
        }
        throw new Error(`Error al cargar transferencias de donación: ${response.status}`);
      }

      const data = await response.json();
      console.log("[fetchTransfers] Fetched transfers:", JSON.stringify(data, null, 2));
      setTransfers(data);
    } catch (error) {
      console.error("[fetchTransfers] Error:", error.message, error);
      showToast(error.message || "Error al cargar las transferencias de donación", "error");
    }
  };

  useEffect(() => {
    if (!authLoading && user?.username) {
      fetchTransfers();
    }
  }, [authLoading, user]);

  const handleAddTransfer = () => {
    if (!user || !user.username) {
      console.error("[handleAddTransfer] No user or username found:", { user });
      showToast("Debes iniciar sesión para registrar transferencias", "error");
      return;
    }
    console.log("[handleAddTransfer] Opening modal for new transfer");
    setTransferToEdit(null);
    setIsModalOpen(true);
  };

  const handleEditTransfer = (transfer) => {
    if (!user || !user.username) {
      console.error("[handleEditTransfer] No user or username found:", { user });
      showToast("Debes iniciar sesión para modificar transferencias", "error");
      return;
    }
    console.log("[handleEditTransfer] Opening modal for transfer:", transfer);
    setTransferToEdit(transfer);
    setIsModalOpen(true);
  };

  
  const handleSubmitTransfer = async (data) => {
    if (!user || !user.username) {
      console.error("[handleSubmitTransfer] No user or username found:", { user });
      showToast("Debes iniciar sesión para procesar transferencias", "error");
      return;
    }
    const token = localStorage.getItem("token");
    if (!token) {
      console.error("[handleSubmitTransfer] No authentication token found");
      showToast("No se encontró el token de autenticación. Por favor, inicia sesión.", "error");
      return;
    }

    try {
      const { id, ...payload } = data;
      const transformedPayload = {
        organization_id: payload.organizationId,
        items: payload.items.map(item => ({
          category_id: item.categoryId,
          description: item.description,
          quantity: item.quantity
        }))
      };
      console.log("[handleSubmitTransfer] Submitting transfer payload:", JSON.stringify(transformedPayload, null, 2));

      const isEdit = id && id !== null && id !== undefined;
      const url = isEdit
        ? `http://localhost:8092/api/donation-transfers/${id}`
        : `http://localhost:8092/api/donation-transfers/create`;
      const method = isEdit ? "PUT" : "POST";

      console.log(`[handleSubmitTransfer] Sending ${method} request to ${url}`);

      const response = await fetch(url, {
        method,
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(transformedPayload),
      });

      if (!response.ok) {
        if (response.status === 403 || response.status === 401) {
          console.error("[handleSubmitTransfer] Unauthorized or Forbidden access:", response.status);
          showToast("Acceso no autorizado. Verifica que tengas los permisos necesarios (PRESIDENTE o VOCAL).", "error");
          return;
        }
        let errorMessage = `Error al ${isEdit ? "modificar" : "guardar"} la transferencia de donación: ${response.status}`;
        try {
          const errorData = await response.text();
          errorMessage = errorData || errorMessage;
          if (errorData.includes("Duplicate transfer_id")) {
            errorMessage = "El ID de transferencia ya existe. Por favor, usa un ID diferente o edita la transferencia existente.";
          }
        } catch (jsonError) {
          console.error("[handleSubmitTransfer] Failed to parse error response:", jsonError);
        }
        throw new Error(errorMessage);
      }

      await fetchTransfers();
      setIsModalOpen(false);
      setTransferToEdit(null);
      showToast(
        isEdit ? "Transferencia modificada con éxito" : `Transferencia registrada correctamente con ID: ${await response.text()}`,
        "success"
      );
    } catch (error) {
      console.error("[handleSubmitTransfer] Error:", error.message, error);
      showToast(error.message || "Hubo un problema al procesar la transferencia.", "error");
    }
  };

  const handleDeleteTransfer = async (transfer) => {
    if (!user || !user.username) {
      console.error("[handleDeleteTransfer] No user or username found:", { user });
      showToast("Debes iniciar sesión para eliminar transferencias", "error");
      return;
    }
    const token = localStorage.getItem("token");
    if (!token) {
      console.error("[handleDeleteTransfer] No authentication token found");
      showToast("No se encontró el token de autenticación. Por favor, inicia sesión.", "error");
      return;
    }

    const confirm = window.confirm("¿Estás seguro de que querés dar de baja esta transferencia?");
    if (!confirm) {
      console.log("[handleDeleteTransfer] Cancellation cancelled by user");
      return;
    }

    try {
      console.log("[handleDeleteTransfer] Deleting transfer: transferId=", transfer.transferId);
      const response = await fetch(`http://localhost:8092/api/donation-transfers/${transfer.transferId}`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ processed: true }),
      });

      if (!response.ok) {
        if (response.status === 403 || response.status === 401) {
          console.error("[handleDeleteTransfer] Unauthorized or Forbidden access:", response.status);
          showToast("Acceso no autorizado. Verifica que tengas los permisos necesarios (PRESIDENTE o VOCAL).", "error");
          return;
        }
        let errorMessage = `Error al dar de baja la transferencia: ${response.status}`;
        try {
          const errorData = await response.text();
          errorMessage = errorData || errorMessage;
        } catch (jsonError) {
          console.error("[handleDeleteTransfer] Failed to parse error response:", jsonError);
        }
        throw new Error(errorMessage);
      }

      await fetchTransfers();
      showToast("Transferencia dada de baja correctamente", "success");
    } catch (error) {
      console.error("[handleDeleteTransfer] Error:", error.message, error);
      showToast(error.message || "Hubo un problema al dar de baja la transferencia.", "error");
    }
  };

  if (authLoading) {
    return (
      <div className="min-h-screen bg-empuje-bg p-6 flex justify-center items-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-empuje-green"></div>
        <span className="ml-3 text-gray-600">Cargando...</span>
      </div>
    );
  }

  if (!user || !user.username) {
    console.warn("[DonationTransfer] No user, rendering login prompt");
    return (
      <div className="min-h-screen bg-empuje-bg p-6 flex justify-center items-center">
        <p className="text-lg text-gray-600">
          Por favor, inicia sesión para ver las transferencias de donación.
        </p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-empuje-bg p-6 relative">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-empuje-green">
          Transferencias de Donación
        </h1>
        <button
          className="bg-empuje-green text-white px-4 py-2 rounded-lg hover:bg-green-700 transition"
          onClick={handleAddTransfer}
        >
          Registrar Transferencia
        </button>
      </div>

      <div className="bg-white shadow-md rounded-xl overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-empuje-green text-white">
            <tr>
              <th className="px-6 py-3 text-left text-sm font-medium">ID Transferencia</th>
              <th className="px-6 py-3 text-left text-sm font-medium">Organización</th>
              <th className="px-6 py-3 text-left text-sm font-medium">ID Solicitud</th>
              <th className="px-6 py-3 text-left text-sm font-medium">Fecha Creación</th>
              <th className="px-6 py-3 text-left text-sm font-medium">Ítems</th>
              <th className="px-6 py-3 text-center text-sm font-medium">Acciones</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {transfers.length === 0 ? (
              <tr>
                <td colSpan="6" className="px-6 py-4 text-center text-gray-700">
                  No hay transferencias de donación disponibles.
                </td>
              </tr>
            ) : (
              transfers.map((transfer) => (
                <tr key={transfer.transferId}>
                  <td className="px-6 py-4">{transfer.transferId}</td>
                  <td className="px-6 py-4">{organizationById[transfer.organizationId] || "Desconocida"}</td>
                  <td className="px-6 py-4">{transfer.requestId || "-"}</td>
                  <td className="px-6 py-4">{transfer.createdAt}</td>
                  <td className="px-6 py-4">
                    <ul className="list-disc pl-5">
                      {transfer.items && transfer.items.length > 0 ? (
                        transfer.items.map((item, index) => (
                          <li key={index}>
                            {categoriesIndexes[item.categoryId] || "Desconocida"}: {item.description} (Cantidad: {item.quantity})
                          </li>
                        ))
                      ) : (
                        <li>Sin ítems</li>
                      )}
                    </ul>
                  </td>
                  <td className="px-6 py-4 flex justify-center gap-2">
                    <button
                      className="bg-empuje-blue text-white px-3 py-1 rounded hover:bg-blue-700 transition"
                      onClick={() => handleEditTransfer(transfer)}
                    >
                      Modificar
                    </button>
                    <button
                      className="bg-empuje-orange text-white px-3 py-1 rounded hover:bg-orange-700 transition"
                      onClick={() => handleDeleteTransfer(transfer)}
                    >
                      Eliminar
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      <DonationTransferModal
        isOpen={isModalOpen}
        onClose={() => {
          console.log("[DonationTransferModal] Closing modal");
          setIsModalOpen(false);
          setTransferToEdit(null);
        }}
        onSubmit={handleSubmitTransfer}
        transferToEdit={transferToEdit}
      />

      <Toast
        message={toast.message}
        type={toast.type}
        onClose={() => setToast({ message: "", type: "success" })}
      />
    </div>
  );
}