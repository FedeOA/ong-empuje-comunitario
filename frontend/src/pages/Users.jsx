import React, { useState, useEffect } from "react";
import AddUserModal from "../components/AddUserModal";
import Toast from "../components/Toast";
import { baseUrl } from "../constants/constants.js";
import { getRoleName } from "../constants/roles";

export default function Users() {
  const [users, setUsers] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [userToEdit, setUserToEdit] = useState(null);
  const [toast, setToast] = useState({ message: "", type: "success" });

  const showToast = (message, type = "success") => {
    setToast({ message, type });
    setTimeout(() => setToast({ message: "", type: "success" }), 3000);
  };

  const fetchUsers = async () => {
    const token = localStorage.getItem("token");

    try {
      const response = await fetch(`${baseUrl}/users`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`,
        },
      });

      if (!response.ok) throw new Error("Error al cargar usuarios");

      const data = await response.json();
      setUsers(data);
    } catch (error) {
      console.error("Error al cargar usuarios:", error);
      showToast("Error al cargar usuarios", "error");
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const openUserModal = (user = null) => {
    setUserToEdit(user);
    setIsModalOpen(true);
  };

  const handleToggleActive = async (userId, newState) => {
    const token = localStorage.getItem("token");

    try {
      const response = await fetch(`${baseUrl}/users/${userId}`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`,
        },
        body: JSON.stringify({ is_active: newState }),
      });

      if (!response.ok) throw new Error("Error al actualizar el usuario");

      await response.json();
      fetchUsers();
      showToast(
        newState ? "Usuario activado correctamente" : "Usuario desactivado correctamente"
      );
    } catch (error) {
      console.error("Error al actualizar el usuario:", error);
      showToast("Error al actualizar el usuario", "error");
    }
  };

  const handleSubmitUser = async (data) => {
    const token = localStorage.getItem("token");

    try {
      let response;

      if (userToEdit) {
        response = await fetch(`${baseUrl}/users/${userToEdit.id}`, {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`,
          },
          body: JSON.stringify(data),
        });
        showToast("Usuario modificado correctamente");
      } else {
        response = await fetch(`${baseUrl}/users`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`,
          },
          body: JSON.stringify({ ...data, is_active: true }),
        });
        showToast("Usuario agregado correctamente");
      }

      if (!response.ok) throw new Error("Error al guardar el usuario");

      await response.json();
      fetchUsers();
      setIsModalOpen(false);
    } catch (error) {
      console.error("Error al guardar usuario:", error);
      showToast("Error al guardar usuario", "error");
    }
  };

  return (
    <div className="min-h-screen bg-empuje-bg p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-empuje-green">Usuarios</h1>
        <button
          className="bg-empuje-green text-white px-4 py-2 rounded-lg hover:bg-green-700 transition"
          onClick={() => openUserModal()}
        >
          Agregar Usuario
        </button>
      </div>

      <div className="bg-white shadow-md rounded-xl overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-empuje-green text-white">
            <tr>
              <th className="px-6 py-3 text-left text-sm font-medium">Usuario</th>
              <th className="px-6 py-3 text-left text-sm font-medium">Nombre</th>
              <th className="px-6 py-3 text-left text-sm font-medium">Apellido</th>
              <th className="px-6 py-3 text-left text-sm font-medium">Teléfono</th>
              <th className="px-6 py-3 text-left text-sm font-medium">Email</th>
              <th className="px-6 py-3 text-left text-sm font-medium">Rol</th>
              <th className="px-6 py-3 text-left text-sm font-medium">Activo</th>
              <th className="px-6 py-3 text-center text-sm font-medium">Acciones</th>
            </tr>
          </thead>

          <tbody className="divide-y divide-gray-200">
            {users.map((user) => (
              <tr key={user.id} className={!user.is_active ? "opacity-50" : ""}>
                <td className="px-6 py-4">{user.username}</td>
                <td className="px-6 py-4">{user.first_name}</td>
                <td className="px-6 py-4">{user.last_name}</td>
                <td className="px-6 py-4">{user.phone}</td>
                <td className="px-6 py-4">{user.email}</td>
                <td className="px-6 py-4">{getRoleName(user.role)}</td>
                <td className="px-6 py-4">{user.is_active ? "Si" : "No"}</td>
                <td className="px-6 py-4 flex justify-center gap-2">
                  <button
                    className="bg-empuje-blue text-white px-3 py-1 rounded hover:bg-blue-700 transition"
                    onClick={() => openUserModal(user)}
                  >
                    Modificar
                  </button>
                  <button
                    className={`${
                      user.is_active
                        ? "bg-empuje-orange hover:bg-orange-700"
                        : "bg-green-600 hover:bg-green-700"
                    } text-white px-3 py-1 rounded transition`}
                    onClick={() => handleToggleActive(user.id, !user.is_active)}
                  >
                    {user.is_active ? "Baja" : "Alta"}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <AddUserModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSubmit={handleSubmitUser}
        userToEdit={userToEdit}
      />

      {/* 👇 Toast renderizado */}
      <Toast
        message={toast.message}
        type={toast.type}
        onClose={() => setToast({ message: "", type: "success" })}
      />
    </div>
  );
}
