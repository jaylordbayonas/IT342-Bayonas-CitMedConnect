import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import PropTypes from 'prop-types';
import useAuth, { AuthProvider } from './hooks/useAuth';
import { NotificationProvider } from './context/NotificationContext';
import { AuditLogProvider } from './context/AuditLogContext';
import { AppointmentProvider } from './context/AppointmentContext';
import Layout from './components/common/layout/Layout';
import Landing from './pages/Landing';
import Dashboard from './pages/Dashboard';
import Appointments from './pages/Appointments';
import Notifications from './pages/Notifications';
import Profile from './pages/Profile';
import Calendar from './pages/Calendar';
import MedicalRecords from './pages/MedicalRecords';

const ProtectedRoute = ({ children }) => {
  const { user, loading } = useAuth();
  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <div className="spinner" />
      </div>
    );
  }
  return user ? children : <Navigate to="/" replace />;
};

const ProtectedLayout = ({ children }) => (
  <ProtectedRoute>
    <Layout>{children}</Layout>
  </ProtectedRoute>
);

const AppRoutes = () => (
  <Routes>
    <Route path="/" element={<Landing />} />
    <Route path="/dashboard" element={<ProtectedLayout><Dashboard /></ProtectedLayout>} />
    <Route path="/appointments" element={<ProtectedLayout><Appointments /></ProtectedLayout>} />
    <Route path="/notifications" element={<ProtectedLayout><Notifications /></ProtectedLayout>} />
    <Route path="/profile" element={<ProtectedLayout><Profile /></ProtectedLayout>} />
    <Route path="/calendar" element={<ProtectedLayout><Calendar /></ProtectedLayout>} />
    <Route path="/medical-records" element={<ProtectedLayout><MedicalRecords /></ProtectedLayout>} />
    <Route path="*" element={<Navigate to="/" replace />} />
  </Routes>
);

const App = () => (
  <BrowserRouter>
    <AuthProvider>
      <AuditLogProvider>
        <NotificationProvider>
          <AppointmentProvider>
            <AppRoutes />
          </AppointmentProvider>
        </NotificationProvider>
      </AuditLogProvider>
    </AuthProvider>
  </BrowserRouter>
);

ProtectedRoute.propTypes = {
  children: PropTypes.node.isRequired,
};

ProtectedLayout.propTypes = {
  children: PropTypes.node.isRequired,
};

export default App;
