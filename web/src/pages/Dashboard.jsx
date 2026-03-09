import React from 'react';
import { useNavigate } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import { LogOut, User, Calendar, FileText, Settings } from 'lucide-react';
import './Dashboard.css';

const Dashboard = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const initials = `${user?.firstName?.[0] ?? ''}${user?.lastName?.[0] ?? ''}`.toUpperCase();
  const roleName = user?.role ? user.role.charAt(0).toUpperCase() + user.role.slice(1) : '';

  return (
    <div className="dashboard">
      <header className="db-header">
        <div className="db-header-inner">
          <span className="db-brand">CIT MedConnect<span>+</span></span>
          <div className="db-header-right">
            <span className="db-welcome-text">Welcome, {user?.firstName}!</span>
            <button className="db-logout" onClick={handleLogout}>
              <LogOut size={16} />
              <span>Logout</span>
            </button>
          </div>
        </div>
      </header>

      <main className="db-main">
        {/* Profile Hero */}
        <div className="db-profile-hero">
          <div className="db-avatar">{initials}</div>
          <div className="db-profile-info">
            <h1>{user?.firstName} {user?.lastName}</h1>
            <p className="db-role-badge">{roleName}</p>
            <p className="db-email">{user?.email}</p>
          </div>
        </div>

        {/* Info Cards */}
        <div className="db-cards">
          <div className="db-card">
            <div className="db-card-icon-wrap">
              <User size={24} />
            </div>
            <div className="db-card-body">
              <h3>Profile Details</h3>
              <p>School ID: <strong>{user?.schoolId}</strong></p>
              <p>Gender: <strong>{user?.gender}</strong></p>
              <p>Age: <strong>{user?.age}</strong></p>
              <p>Phone: <strong>{user?.phone}</strong></p>
            </div>
          </div>

          <div className="db-card">
            <div className="db-card-icon-wrap">
              <Calendar size={24} />
            </div>
            <div className="db-card-body">
              <h3>Appointments</h3>
              <p className="db-card-empty">No upcoming appointments</p>
            </div>
          </div>

          <div className="db-card">
            <div className="db-card-icon-wrap">
              <FileText size={24} />
            </div>
            <div className="db-card-body">
              <h3>Medical Records</h3>
              <p className="db-card-empty">No records available</p>
            </div>
          </div>

          <div className="db-card">
            <div className="db-card-icon-wrap">
              <Settings size={24} />
            </div>
            <div className="db-card-body">
              <h3>Account Settings</h3>
              <p className="db-card-empty">Manage your account preferences</p>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default Dashboard;
