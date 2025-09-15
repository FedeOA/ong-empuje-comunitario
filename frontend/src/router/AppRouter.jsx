import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import LoginPage from "../pages/Login";
import HomePage from "../pages/Home";
import EventsPage from "../pages/Events";
import UsersPage from "../pages/Users";
import DonationsPage from "../pages/Donations";
import ForbiddenPage from "../pages/Forbidden";
import ProtectedRoute from "../components/ProtectedRoute";
import { roles } from "../constants/roles";

const AppRouter = () => {
  return (
    <BrowserRouter>
      <Routes>
        {/* Login: única ruta pública */}
        <Route path="/login" element={<LoginPage />} />

        {/* Rutas protegidas */}
        <Route
          path="*"
          element={
            <ProtectedRoute>
              <Routes>
                <Route path="/home" element={<HomePage />} />
                <Route path="/events" element={<EventsPage />} />
                <Route path="/users" element={<UsersPage />} />
                <Route
                  path="/donations"
                  element={
                    <ProtectedRoute allowedRoles={[roles.PRESIDENTE, roles.VOCAL]}>
                      <DonationsPage />
                    </ProtectedRoute>
                  }
                />
                <Route path="/forbidden" element={<ForbiddenPage />} />
                {/* Fallback: cualquier otra ruta redirige a /home */}
                <Route path="*" element={<Navigate to="/home" replace />} />
              </Routes>
            </ProtectedRoute>
          }
        />
      </Routes>
    </BrowserRouter>
  );
};

export default AppRouter;
