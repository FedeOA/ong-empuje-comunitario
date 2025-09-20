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
        {/* Ruta pública */}
        <Route path="/login" element={<LoginPage />} />

        {/* Rutas protegidas */}
        <Route
          path="/home"
          element={
            <ProtectedRoute>
              <HomePage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/events"
          element={
            <ProtectedRoute allowedRoles={[roles.PRESIDENTE, roles.COORDINADOR, roles.VOLUNTARIO]}>
              <EventsPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/users"
          element={
            <ProtectedRoute allowedRoles={[roles.PRESIDENTE]}>
              <UsersPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/donations"
          element={
            <ProtectedRoute allowedRoles={[roles.PRESIDENTE, roles.VOCAL]}>
              <DonationsPage />
            </ProtectedRoute>
          }
        />

        {/* Ruta de acceso denegado */}
        <Route path="/forbidden" element={<ForbiddenPage />} />

        {/* Fallback */}
        <Route path="*" element={<Navigate to="/home" replace />} />
      </Routes>
    </BrowserRouter>
  );
};

export default AppRouter;
