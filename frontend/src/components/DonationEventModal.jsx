import { categoriesIndexes } from "../constants/Categories.js";

const DonationsEventModal = ({ donations, onClose }) => (
    console.log(donations) ||
  <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div className="bg-white rounded-lg shadow-lg p-6 w-full max-w-md">
      <h2 className="text-xl font-semibold mb-4 text-empuje-green">Donaciones</h2>
      <ul className="space-y-2">
        {donations.length === 0 ? (
            <p className="text-sm text-gray-500">Este evento no tiene donaciones registradas.</p>
            ) : (
            <ul className="space-y-3">
                {donations.map((d, i) => (
                <li key={i} className="border p-2 rounded">
                    <p><strong>Categoría:</strong> {categoriesIndexes[d.category] || "Desconocido"}</p>
                    <p><strong>Descripción:</strong> {d.description}</p>
                    <p><strong>Cantidad:</strong> {d.quantity}</p>
                </li>
                ))}
            </ul>
            )}

      </ul>
      <div className="mt-4 text-right">
        <button
          className="bg-empuje-orange text-white px-4 py-2 rounded hover:bg-orange-600"
          onClick={onClose}
        >
          Cerrar
        </button>
      </div>
    </div>
  </div>
);

export default DonationsEventModal;