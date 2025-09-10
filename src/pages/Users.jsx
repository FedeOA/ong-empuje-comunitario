import React, { useState } from "react";
import AddUserModal from "../components/AddUserModal";

// Lista inicial de usuarios
const initialUsers = [
  {
    id: 1,
    username: "jdoe",
    first_name: "John",
    last_name: "Doe",
    phone: "123456789",
    email: "jdoe@example.com",
    role: "SOCIO",
    active: true
  },
  {
    id: 2,
    username: "apresident",
    first_name: "Alice",
    last_name: "Presidente",
    phone: "987654321",
    email: "alice@example.com",
    role: "PRESIDENTE",
    active: true
  }
];

export default function Users() {
  const [users, setUsers] = useState(initialUsers);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [userToEdit, setUserToEdit] = useState(null);

  // Abrir modal para agregar usuario
  const handleAdd = () => {
    setUserToEdit(null);
    setIsModalOpen(true);
  };

  // Abrir modal para modificar usuario
  const handleEdit = (user) => {
    setUserToEdit(user);
    setIsModalOpen(true);
  };

  // Función para dar de baja un usuario
  const handleDelete = (userId) => {
    setUsers(users.map(u => u.id === userId ? { ...u, active: false } : u));
  };

  // Función que maneja tanto agregar como modificar
  const handleSubmitUser = (data) => {
    if (userToEdit) {
      // Modificar usuario existente
      setUsers(users.map(u => u.id === userToEdit.id ? { ...u, ...data } : u));
    } else {
      // Agregar usuario nuevo
      const id = users.length + 1;
      setUsers([...users, { ...data, id, active: true }]);
    }
  };

  return (
    <div className="min-h-screen bg-empuje-bg p-6">
      {/* Título + botón agregar */}
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-empuje-green">Usuarios</h1>
        <button
          className="bg-empuje-green text-white px-4 py-2 rounded-lg hover:bg-green-700 transition"
          onClick={handleAdd}
        >
          Agregar Usuario
        </button>
      </div>

      {/* Tabla de usuarios */}
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
            {users.map(user => (
              <tr key={user.id} className={!user.active ? "opacity-50" : ""}>
                <td className="px-6 py-4">{user.username}</td>
                <td className="px-6 py-4">{user.first_name}</td>
                <td className="px-6 py-4">{user.last_name}</td>
                <td className="px-6 py-4">{user.phone}</td>
                <td className="px-6 py-4">{user.email}</td>
                <td className="px-6 py-4">{user.role}</td>
                <td className="px-6 py-4">{user.active ? "Sí" : "No"}</td>
                <td className="px-6 py-4 flex justify-center gap-2">
                  <button
                    className="bg-empuje-blue text-white px-3 py-1 rounded hover:bg-blue-700 transition"
                    onClick={() => handleEdit(user)}
                  >
                    Modificar
                  </button>
                  <button
                    className="bg-empuje-orange text-white px-3 py-1 rounded hover:bg-orange-700 transition"
                    onClick={() => handleDelete(user.id)}
                    disabled={!user.active}
                  >
                    Baja
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Modal para agregar o modificar usuario */}
      <AddUserModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSubmit={handleSubmitUser}
        userToEdit={userToEdit}
      />
    </div>
  );
}
