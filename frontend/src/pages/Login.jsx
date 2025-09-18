import { useState } from "react";
import { useAuth } from "../context/AuthContext"; 
import { useNavigate } from "react-router-dom";
import logo from "../assets/logo.png";

export default function Login() {
  const { login } = useAuth(); 
  const navigate = useNavigate(); 
  const [usernameOrEmail, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault(); 
    try {
      await login(usernameOrEmail, password); 
      navigate("/home");
    } catch (err) {
      setError("Credenciales invalidas");
      console.error("Error during login:", err);
    }
  };

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-empuje-bg space-y-6 px-4">
      <div className="flex justify-center">
        <img src={logo} alt="Empuje Comunitario" className="w-80 h-40 object-contain" />
      </div>

      <div className="bg-white shadow-lg rounded-xl p-8 w-full max-w-md">
        <h2 className="text-2xl font-bold text-center text-empuje-green mb-6">
          Iniciar Sesión
        </h2>

        <form className="space-y-4" onSubmit={handleSubmit}>
          <div>
            <label className="block text-sm font-medium text-gray-700">
              Correo electrónico o usuario
            </label>
            <input
              type="email"
              value={usernameOrEmail}
              onChange={(e) => setEmail(e.target.value)}
              className="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-empuje-green focus:border-empuje-green"
              placeholder="correo o usuario"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">
              Contraseña
            </label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-empuje-green focus:border-empuje-green"
              placeholder="********"
              required
            />
          </div>

          {error && <p className="text-red-500">{error}</p>}

          <button
            type="submit"
            className="w-full bg-empuje-green text-white py-2 rounded-lg font-medium hover:bg-green-700 transition"
          >
            Ingresar
          </button>
        </form>
      </div>
    </div>
  );
}

