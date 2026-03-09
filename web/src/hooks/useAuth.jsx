import { useState, useEffect, createContext, useContext } from 'react';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const stored =
      localStorage.getItem('citmedconnect_user') ||
      sessionStorage.getItem('citmedconnect_user');
    if (stored) {
      try {
        setUser(JSON.parse(stored));
      } catch {
        localStorage.removeItem('citmedconnect_user');
        sessionStorage.removeItem('citmedconnect_user');
      }
    }
    setLoading(false);
  }, []);

  const login = async (email, password, rememberMe = false) => {
    try {
      const res = await fetch('/api/users/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      });

      if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        return { success: false, error: err.error || 'Invalid email or password' };
      }

      const userData = await res.json();
      setUser(userData);
      (rememberMe ? localStorage : sessionStorage)
        .setItem('citmedconnect_user', JSON.stringify(userData));
      return { success: true, user: userData };
    } catch {
      return { success: false, error: 'Unable to connect to server. Please try again.' };
    }
  };

  const register = async (userData) => {
    try {
      const res = await fetch('/api/users/', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(userData),
      });

      if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        return { success: false, error: err.error || 'Registration failed. Please try again.' };
      }

      const newUser = await res.json();
      return { success: true, user: newUser };
    } catch {
      return { success: false, error: 'Unable to connect to server. Please try again.' };
    }
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('citmedconnect_user');
    sessionStorage.removeItem('citmedconnect_user');
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
};

export default useAuth;
