import React, { useState, useEffect } from "react";
import DonationOfferModal from "../components/DonationOfferModal";
import Toast from "../components/Toast";
import { categoriesIndexes } from "../constants/Categories.js";
import { organizationById } from "../constants/organizations.js";
import { useAuth } from "../context/AuthContext";

export default function DonationOffers() {
  const { user, loading: authLoading } = useAuth();
  const [offers, setOffers] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [offerToEdit, setOfferToEdit] = useState(null);
  const [toast, setToast] = useState({ message: "", type: "success" });

  const showToast = (message, type = "success") => {
    console.log(`[Toast] Displaying toast: ${message}, type: ${type}`);
    setToast({ message, type });
    setTimeout(() => setToast({ message: "", type: "success" }), 3000);
  };

  const fetchDonationOffers = async () => {
    if (!user || !user.username) {
      console.error("[fetchDonationOffers] No user or username found:", { user });
      showToast("Debes iniciar sesión para ver las ofertas de donación", "error");
      return;
    }

    const token = localStorage.getItem("token");
    try {
      console.log("[fetchDonationOffers] Fetching donation offers");
      const response = await fetch("http://localhost:8092/api/donation-offers", {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) throw new Error("Error al cargar ofertas de donación");

      const data = await response.json();
      console.log("Fetched donation offers:", JSON.stringify(data, null, 2));
      setOffers(data);
    } catch (error) {
      console.error("[fetchDonationOffers] Error:", error.message, error);
      showToast("Error al cargar ofertas de donación", "error");
    }
  };

  useEffect(() => {
    if (!authLoading && user?.username) {
      fetchDonationOffers();
    }
  }, [authLoading, user?.username]);

  const openOfferModal = (offer = null) => {
    if (!user || !user.username) {
      console.error("[openOfferModal] No user or username found:", { user });
      showToast("Debes iniciar sesión para gestionar ofertas", "error");
      return;
    }
    console.log("[openOfferModal] Opening modal, offer:", offer);
    setOfferToEdit(offer);
    setIsModalOpen(true);
  };

  const handleSubmitOffer = async (data) => {
    if (!user || !user.username) {
      console.error("[handleSubmitOffer] No user or username found:", { user });
      showToast("Debes iniciar sesión para procesar ofertas", "error");
      return;
    }

    const token = localStorage.getItem("token");
    try {
      const payload = {
        offerId: data.id || Math.floor(Math.random() * 1000000),
        organizationId: data.organizationId,
        expiresAt: data.expiresAt || new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString(),
        items: data.items.map(item => ({
          categoryId: item.categoryId || 1, // Default to 1 (ALIMENTOS) if missing
          description: item.description || "Sin descripción",
          quantity: item.quantity || 1,
        })),
      };

      console.log("[handleSubmitOffer] Submitting donation offer payload:", JSON.stringify(payload, null, 2));

      const response = await fetch("http://localhost:8092/api/donation-offers/create", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) throw new Error("Error al procesar la oferta de donación");

      showToast(data.id ? "Oferta modificada correctamente" : "Oferta creada correctamente");
      setTimeout(() => fetchDonationOffers(), 3000); // Delay refresh by 3 seconds
      setIsModalOpen(false);
    } catch (error) {
      console.error("[handleSubmitOffer] Error:", error.message, error);
      showToast("Error al procesar la oferta: " + error.message, "error");
    }
  };

  const handleCancelOffer = async (offerId, orgId) => {
    if (!user || !user.username) {
      console.error("[handleCancelOffer] No user or username found:", { user });
      showToast("Debes iniciar sesión para cancelar ofertas", "error");
      return;
    }

    if (!confirm("¿Está seguro de cancelar esta oferta?")) {
      console.log("[handleCancelOffer] Cancellation cancelled by user");
      return;
    }

    const token = localStorage.getItem("token");
    try {
      console.log("[handleCancelOffer] Cancelling offer: offerId={}, orgId={}", offerId, orgId);
      const response = await fetch("http://localhost:8092/api/donation-offers/cancel", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ offerId, organizationId: orgId }),
      });

      if (!response.ok) throw new Error("Error al cancelar la oferta");

      showToast("Oferta cancelada correctamente");
      setTimeout(() => fetchDonationOffers(), 3000); // Delay refresh by 3 seconds
    } catch (error) {
      console.error("[handleCancelOffer] Error:", error.message, error);
      showToast("Error al cancelar la oferta: " + error.message, "error");
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

  if (!user) {
    console.warn("[DonationOffers] No user, rendering login prompt");
    return (
      <div className="min-h-screen bg-empuje-bg p-6 flex justify-center items-center">
        <p className="text-lg text-gray-600">Por favor, inicia sesión para ver las ofertas de donación.</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-empuje-bg p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-empuje-green">Ofertas de Donación</h1>
        <button
          className="bg-empuje-green text-white px-4 py-2 rounded-lg hover:bg-green-700 transition"
          onClick={() => openOfferModal()}
        >
          Crear Oferta
        </button>
      </div>

      <div className="bg-white shadow-md rounded-xl overflow-x-auto">
        {offers.length === 0 ? (
          <div className="text-center py-12 text-gray-500">
            <p className="text-lg">No se encontraron ofertas de donación</p>
          </div>
        ) : (
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-empuje-green text-white">
              <tr>
                <th className="px-6 py-3 text-left text-sm font-medium">ID Oferta</th>
                <th className="px-6 py-3 text-left text-sm font-medium">Organización</th>
                <th className="px-6 py-3 text-left text-sm font-medium">Ítems</th>
                <th className="px-6 py-3 text-left text-sm font-medium">Estado</th>
                <th className="px-6 py-3 text-center text-sm font-medium">Acciones</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {offers.map((offer) => (
                <tr key={`${offer.offerId}-${offer.organizationId}`}>
                  <td className="px-6 py-4">{offer.offerId || "Sin ID"}</td>
                  <td className="px-6 py-4">{organizationById[offer.organizationId] || "Sin Org"}</td>
                  <td className="px-6 py-4">
                    <ul>
                      {offer.items && offer.items.length > 0 ? (
                        offer.items.map((item, idx) => (
                          <li key={idx}>
                            {categoriesIndexes[item.categoryId] || "Desconocido"}: {item.description || "Sin descripción"} (Cantidad: {item.quantity})
                          </li>
                        ))
                      ) : (
                        <li>Sin ítems</li>
                      )}
                    </ul>
                  </td>
                  <td className="px-6 py-4">
                    <span className={offer.available ? "text-green-600" : "text-red-600"}>
                      {offer.available ? "Activa" : "Cancelada"}
                    </span>
                  </td>
                  <td className="px-6 py-4 flex justify-center gap-2">
                    {offer.available && (
                      <button
                        className="bg-red-600 text-white px-3 py-1 rounded hover:bg-red-700 transition"
                        onClick={() => handleCancelOffer(offer.offerId, offer.organizationId)}
                      >
                        Cancelar
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <DonationOfferModal
        isOpen={isModalOpen}
        onClose={() => {
          console.log("[DonationOfferModal] Closing modal");
          setIsModalOpen(false);
          setOfferToEdit(null);
        }}
        onSubmit={handleSubmitOffer}
        offerToEdit={offerToEdit}
      />

      <Toast
        message={toast.message}
        type={toast.type}
        onClose={() => setToast({ message: "", type: "success" })}
      />
    </div>
  );
}