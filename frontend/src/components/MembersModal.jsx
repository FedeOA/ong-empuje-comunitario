import React, { useState, useEffect } from "react";
import { baseUrl } from "../constants/constants.js";

export default function MembersModal({ isOpen, onClose, event, onUpdateUsers, user }) {
  const [members, setMembers] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [allUsers, setAllUsers] = useState([]);

  useEffect(() => {
    if (event?.id) {
      const token = localStorage.getItem("token");

      fetch(`${baseUrl}/events`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`
        }
      })
        .then(res => res.json())
        .then(data => {
          const matched = data.find(e => e.id === event.id);
          setMembers(matched?.users || []);
        })
        .catch(err => {
          console.error("Error al obtener eventos:", err);
          setMembers([]);
        });
    }
  }, [event?.id, isOpen]);

  useEffect(() => {
    const token = localStorage.getItem("token");

    fetch(`${baseUrl}/users`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
      }
    })
      .then(res => res.json())
      .then(data => setAllUsers(data))
      .catch(err => {
        console.error("Error al obtener usuarios:", err);
        setAllUsers([]);
      });
  }, []);

  const handleRemoveUser = async (eventId, username) => {
    const token = localStorage.getItem("token");

    try {
      const response = await fetch(`${baseUrl}/events/${eventId}/users/${username}`, {
        method: "DELETE",
        headers: {
          "Authorization": `Bearer ${token}`
        }
      });

      if (!response.ok) throw new Error("Error al eliminar miembro");

      setMembers(prev => prev.filter(m => m !== username));
      onUpdateUsers();
    } catch (error) {
      console.error("Error al eliminar miembro:", error);
    }
  };

  const handleAddUser = async (eventId, username) => {
    const token = localStorage.getItem("token");

    try {
      const response = await fetch(`${baseUrl}/events/${eventId}/users/${username}`, {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${token}`
        }
      });

      if (!response.ok) throw new Error("Error al agregar miembro");

      setMembers(prev => [...prev, username]);
      setSearchTerm("");
      onUpdateUsers();
    } catch (error) {
      console.error("Error al agregar miembro:", error);
    }
  };

  if (!isOpen || !event) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
      <div className="bg-white shadow-lg rounded-xl p-6 w-full max-w-md relative">
        <button
          className="absolute top-3 right-3 text-gray-500 hover:text-gray-800 font-bold"
          onClick={onClose}
        >
          ×
        </button>

        <h2 className="text-2xl font-bold text-empuje-green mb-4 text-center">
          Miembros de {event.name}
        </h2>

        {/* Lista de miembros */}
        <ul className="mb-4 space-y-2 max-h-64 overflow-y-auto">
          {members.length === 0 && <li className="text-gray-500">No hay miembros aún.</li>}
          {members.map((m, idx) => (
            <li key={idx} className="flex justify-between items-center bg-gray-100 px-3 py-2 rounded">
              <span>
                {m}
                {m === user.username && " (vos)"}
              </span>
              {m !== user.username && (
                <button
                  className="text-red-500 hover:text-red-700 font-bold"
                  onClick={() => handleRemoveUser(event.id, m)}
                >
                  Eliminar
                </button>
              )}
            </li>
          ))}
        </ul>

        {/* Agregar miembro */}
        <div className="relative">
          <input
            type="text"
            placeholder="Buscar usuario..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-empuje-green focus:border-empuje-green"
          />

          {searchTerm &&
            !members.includes(searchTerm) &&
            allUsers.some(u =>
              u.username.toLowerCase().includes(searchTerm.toLowerCase()) &&
              u.username.toLowerCase() !== searchTerm.toLowerCase() &&
              !members.includes(u.username)
            ) && (
              <ul className="absolute z-10 bg-white border border-gray-300 rounded-lg mt-1 max-h-60 overflow-y-auto w-full">
                {allUsers
                  .filter(u =>
                    u.username.toLowerCase().includes(searchTerm.toLowerCase()) &&
                    u.username.toLowerCase() !== searchTerm.toLowerCase() &&
                    !members.includes(u.username)
                  )
                  .map((u, idx) => (
                    <li
                      key={idx}
                      className="px-3 py-2 hover:bg-gray-100 cursor-pointer"
                      onClick={() => setSearchTerm(u.username)}
                    >
                      {u.username}
                    </li>
                  ))}
              </ul>
            )}

          {searchTerm &&
            !members.includes(searchTerm) &&
            !allUsers.some(u => u.username.toLowerCase() === searchTerm.toLowerCase()) &&
            allUsers.filter(u =>
              u.username.toLowerCase().includes(searchTerm.toLowerCase()) &&
              !members.includes(u.username)
            ).length === 0 && (
              <div className="mt-1 px-3 py-2 text-gray-500 border border-gray-300 rounded-lg bg-white">
                No se encontraron usuarios
              </div>
          )}

          <button
            className="bg-empuje-green text-white px-4 py-2 rounded-lg hover:bg-green-700 transition mt-2 w-full"
            onClick={() => handleAddUser(event.id, searchTerm)}
            disabled={!searchTerm || members.includes(searchTerm)}
          >
            Agregar
          </button>
        </div>
      </div>
    </div>
  );
}
