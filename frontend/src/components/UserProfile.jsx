import avatarPres from "../assets/avatar-pres.png";
import avatarCoor from "../assets/avatar-coor.png";
import avatarVoc from "../assets/avatar-voc.png";
import avatarVol from "../assets/avatar-vol.png";
import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";

export default function UserProfile({ username, role }) {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const avatarMap = {
    PRESIDENTE: avatarPres,
    COORDINADOR: avatarCoor,
    VOCAL: avatarVoc,
    VOLUNTARIO: avatarVol,
  };

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <div className="relative mb-8 group">
      {/* Bloque de perfil */}
      <div className="flex items-center gap-4 cursor-default">
        <img
          src={avatarMap[role] || avatarVol}
          alt="Avatar"
          className="w-20 h-20 rounded-full"
        />
        <div className="flex flex-col">
          <span className="font-semibold text-lg">{username}</span>
          <span className="text-sm text-gray-500 capitalize">{role}</span>
        </div>
      </div>

      <div className="absolute top-24 left-0 opacity-0 group-hover:opacity-100 transition-opacity duration-200">
        <button
          onClick={handleLogout}
          className="w-full px-3 py-2 rounded bg-empuje-orange text-white hover:opacity-90 transition text-sm"
        >
          Cerrar sesión
        </button>
      </div>
    </div>
  );
}
