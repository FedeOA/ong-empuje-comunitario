import React from "react";

const ActionsModal = ({
  event,
  user,
  isAlreadyJoined,
  isFuture,
  onClose,
  onJoin,
  onLeave,
  onEdit,
  onDelete,
  onPublish,
}) => {
  const showJoinLeave = isFuture;
  const showAdminActions = isFuture && (user.role === "PRESIDENTE" || user.role === "COORDINADOR");
  const showPublish = showAdminActions;

  const hasActions =
    showJoinLeave ||
    showAdminActions ||
    (showPublish && !event.is_published);

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-lg p-6 w-full max-w-md">
        <div className="flex flex-col gap-3">
          {hasActions ? (
            <>
              {showJoinLeave && (
                isAlreadyJoined ? (
                  <button
                    className="bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700 transition"
                    onClick={() => onLeave(event.id)}
                  >
                    Abandonar
                  </button>
                ) : (
                  <button
                    className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 transition"
                    onClick={() => onJoin(event.id)}
                  >
                    Agregarse
                  </button>
                )
              )}

              {showAdminActions && (
                <>
                  <button
                    className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 transition"
                    onClick={() => onEdit(event)}
                  >
                    Modificar
                  </button>
                  <button
                    className="bg-orange-500 text-white px-4 py-2 rounded hover:bg-orange-600 transition"
                    onClick={() => onDelete(event)}
                  >
                    Eliminar
                  </button>
                </>
              )}

              {showPublish && (
                !event.is_published ? (
                  <button
                    className="bg-purple-600 text-white px-4 py-2 rounded hover:bg-purple-700 transition flex items-center gap-2"
                    onClick={() => onPublish(event)}
                  >
                    📢 <span>Publicar</span>
                  </button>
                ) : (
                  <div className="text-purple-700 font-semibold flex items-center gap-2">
                    ✅ <span>Publicado</span>
                  </div>
                )
              )}
            </>
          ) : (
            <div className="text-center text-gray-600 text-lg font-medium">
              Este evento ya ocurrió
            </div>
          )}
        </div>

        <div className="mt-6 flex justify-end">
          <button
            className="text-gray-600 hover:text-gray-800"
            onClick={onClose}
          >
            Cerrar
          </button>
        </div>
      </div>
    </div>
  );
};

export default ActionsModal;
