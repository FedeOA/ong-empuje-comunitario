import React, { useState } from "react";
import { baseUrlWebServices } from "../constants/constants";
import { useAuth } from "../context/AuthContext";
import Toast from "../components/Toast";

const SoapData = () => {
  const { user } = useAuth();
  const [orgIdsInput, setOrgIdsInput] = useState("");
  const [organizations, setOrganizations] = useState([]);
  const [presidents, setPresidents] = useState([]);
  const [toast, setToast] = useState({ message: "", type: "success" });
  const [loading, setLoading] = useState(false);

  const showToast = (message, type = "success") => {
    setToast({ message, type });
    setTimeout(() => setToast({ message: "", type: "success" }), 3000);
  };

  const handleInputChange = (e) => {
    setOrgIdsInput(e.target.value);
  };

  const fetchSoapData = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem("token");
      if (!token) {
        showToast("No authentication token found", "error");
        return;
      }
      if (user?.role !== "PRESIDENTE") {
        showToast("Only presidents can access this data", "error");
        return;
      }
      const orgIds = orgIdsInput
        .split(",")
        .map((id) => parseInt(id.trim(), 10))
        .filter((id) => !isNaN(id));
      if (orgIds.length === 0) {
        showToast("Please enter at least one valid ID", "error");
        return;
      }

      // Fetch organizations
      const orgResponse = await fetch(`${baseUrlWebServices}/api/soap/organizations`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(orgIds),
      });
      if (!orgResponse.ok) {
        const errorMessage = await orgResponse.text() || "Failed to fetch organizations";
        throw new Error(errorMessage);
      }
      const orgData = await orgResponse.json();
      setOrganizations(orgData);

      // Fetch presidents
      const presResponse = await fetch(`${baseUrlWebServices}/api/soap/presidents`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(orgIds),
      });
      if (!presResponse.ok) {
        const errorMessage = await presResponse.text() || "Failed to fetch presidents";
        throw new Error(errorMessage);
      }
      const presData = await presResponse.json();
      setPresidents(presData);

      showToast("Data fetched successfully", "success");
    } catch (error) {
      console.error("Error fetching data:", error);
      showToast(`Error: ${error.message}`, "error");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-empuje-bg p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-empuje-green">Consultar Organizaciones y Presidentes</h1>
        <button
          className="bg-empuje-green text-white px-4 py-2 rounded-lg hover:bg-green-700 transition disabled:bg-gray-400"
          onClick={fetchSoapData}
          disabled={loading || !user}
        >
          {loading ? "Consultando..." : "Consultar Datos"}
        </button>
      </div>
      <div className="bg-white shadow-md rounded-xl p-6 mb-6">
        <div className="grid grid-cols-1 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700">IDs de Organizaciones (separados por comas)</label>
            <input
              type="text"
              value={orgIdsInput}
              onChange={handleInputChange}
              className="mt-1 block w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-empuje-green focus:border-empuje-green"
              disabled={loading}
              placeholder="Ej: 1,2,3"
              aria-label="IDs de organizaciones"
            />
          </div>
        </div>
      </div>
      <div className="mb-6">
        <div className="bg-white shadow-md rounded-xl overflow-x-auto">
          <table className="w-full divide-y divide-gray-200">
            <thead className="bg-empuje-green text-white">
              <tr>
                <th className="px-6 py-3 text-left text-sm font-medium">ID</th>
                <th className="px-6 py-3 text-left text-sm font-medium">Nombre</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {organizations.length === 0 ? (
                <tr>
                  <td colSpan="2" className="px-6 py-4 text-sm text-gray-600 text-center">
                    No hay organizaciones para mostrar
                  </td>
                </tr>
              ) : (
                organizations.map((org) => (
                  <tr key={org.id}>
                    <td className="px-6 py-4 text-sm text-gray-600">{org.id}</td>
                    <td className="px-6 py-4 text-sm text-gray-600">{org.name}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
      <div>
        <div className="bg-white shadow-md rounded-xl overflow-x-auto">
          <table className="w-full divide-y divide-gray-200">
            <thead className="bg-empuje-green text-white">
              <tr>
                <th className="px-6 py-3 text-left text-sm font-medium">ID</th>
                <th className="px-6 py-3 text-left text-sm font-medium">Usuario</th>
                <th className="px-6 py-3 text-left text-sm font-medium">Nombre</th>
                <th className="px-6 py-3 text-left text-sm font-medium">Apellido</th>
                <th className="px-6 py-3 text-left text-sm font-medium">Email</th>
                <th className="px-6 py-3 text-left text-sm font-medium">Teléfono</th>
                <th className="px-6 py-3 text-left text-sm font-medium">Rol</th>
                <th className="px-6 py-3 text-left text-sm font-medium">ID Organización</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {presidents.length === 0 ? (
                <tr>
                  <td colSpan="8" className="px-6 py-4 text-sm text-gray-600 text-center">
                    No hay presidentes para mostrar
                  </td>
                </tr>
              ) : (
                presidents.map((pres) => (
                  <tr key={pres.id}>
                    <td className="px-6 py-4 text-sm text-gray-600">{pres.id}</td>
                    <td className="px-6 py-4 text-sm text-gray-600">{pres.username}</td>
                    <td className="px-6 py-4 text-sm text-gray-600">{pres.firstName}</td>
                    <td className="px-6 py-4 text-sm text-gray-600">{pres.lastName}</td>
                    <td className="px-6 py-4 text-sm text-gray-600">{pres.email}</td>
                    <td className="px-6 py-4 text-sm text-gray-600">{pres.phone || 'N/A'}</td>
                    <td className="px-6 py-4 text-sm text-gray-600">{pres.role}</td>
                    <td className="px-6 py-4 text-sm text-gray-600">{pres.organizationId}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
      {toast.message && <Toast message={toast.message} type={toast.type} />}
    </div>
  );
};

export default SoapData;