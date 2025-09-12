import React, { useState, useEffect } from "react";

export default function MembersModal({ isOpen, onClose, event, onUpdateMembers }) {
  const [members, setMembers] = useState([]);
  const [newMember, setNewMember] = useState("");

  useEffect(() => {
    if (event) {
      setMembers(event.members || []);
    }
  }, [event, isOpen]);

  const handleAddMember = () => {
    if (newMember.trim() && !members.includes(newMember.trim())) {
      const updated = [...members, newMember.trim()];
      setMembers(updated);
      onUpdateMembers(event.id, updated);
      setNewMember("");
    }
  };

  const handleRemoveMember = (member) => {
    const updated = members.filter(m => m !== member);
    setMembers(updated);
    onUpdateMembers(event.id, updated);
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
              <span>{m}</span>
              <button
                className="text-red-500 hover:text-red-700 font-bold"
                onClick={() => handleRemoveMember(m)}
              >
                Eliminar
              </button>
            </li>
          ))}
        </ul>

        {/* Agregar miembro */}
        <div className="flex gap-2">
          <input
            type="text"
            placeholder="Nuevo miembro"
            value={newMember}
            onChange={(e) => setNewMember(e.target.value)}
            className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:ring-empuje-green focus:border-empuje-green"
          />
          <button
            className="bg-empuje-green text-white px-4 py-2 rounded-lg hover:bg-green-700 transition"
            onClick={handleAddMember}
          >
            Agregar
          </button>
        </div>
      </div>
    </div>
  );
}
