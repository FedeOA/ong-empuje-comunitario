import React, { useState, useEffect } from "react";
import { getRoleOptions, roles } from "../constants/roles";

export default function AddUserModal({ isOpen, onClose, onSubmit, userToEdit }) {
  const defaultRole = "VOLUNTARIO";

  const [formData, setFormData] = useState({
    username: "",
    first_name: "",
    last_name: "",
    phone: "",
    email: "",
    password: "",
    role: defaultRole,
  });

  useEffect(() => {
    if (userToEdit) {
      setFormData({
        username: userToEdit.username || "",
        first_name: userToEdit.first_name || "",
        last_name: userToEdit.last_name || "",
        phone: userToEdit.phone || "",
        email: userToEdit.email || "",
        password: "", // nunca mostrar contraseña
        role: roles[userToEdit.role] ? userToEdit.role : defaultRole,
      });
    } else {
      setFormData({
        username: "",
        first_name: "",
        last_name: "",
        phone: "",
        email: "",
        password: "",
        role: defaultRole,
      });
    }
  }, [userToEdit, isOpen]);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit(formData);
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
      <div className="bg-white shadow-lg rounded-xl p-6 w-full max-w-lg relative">
        <button
          className="absolute top-3 right-3 text-gray-500 hover:text-gray-800 font-bold"
          onClick={onClose}
        >
          ×
        </button>

        <h2 className="text-2xl font-bold text-empuje-green mb-6 text-center">
          {userToEdit ? "Modificar Usuario" : "Agregar Usuario"}
        </h2>

        <form className="space-y-4" onSubmit={handleSubmit}>
          {/* Campos de usuario */}
          {["username", "first_name", "last_name", "phone", "email"].map((field) => (
            <div key={field}>
              <label className="block text-sm font-medium text-gray-700">
                {field === "username"
                  ? "Usuario"
                  : field === "first_name"
                  ? "Nombre"
                  : field === "last_name"
                  ? "Apellido"
                  : field === "phone"
                  ? "Teléfono"
                  : "Email"}
              </label>
              <input
                type={field === "email" ? "email" : "text"}
                name={field}
                value={formData[field]}
                onChange={handleChange}
                className="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-empuje-green focus:border-empuje-green"
                required={["username", "first_name", "last_name", "email"].includes(field)}
              />
            </div>
          ))}

          {/* Rol */}
          <div>
            <label className="block text-sm font-medium text-gray-700">Rol</label>
            <select
              name="role"
              value={formData.role}
              onChange={handleChange}
              className="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-empuje-green focus:border-empuje-green"
            >
              {getRoleOptions().map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>

          {/* Botón */}
          <button
            type="submit"
            className="w-full bg-empuje-green text-white py-2 rounded-lg font-medium hover:bg-green-700 transition"
          >
            {userToEdit ? "Confirmar" : "Agregar Usuario"}
          </button>
        </form>
      </div>
    </div>
  );
}
