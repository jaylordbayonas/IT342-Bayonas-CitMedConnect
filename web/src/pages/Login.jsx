import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import { Mail, Lock, Eye, EyeOff } from 'lucide-react';
import './Login.css';

const logo = '/images/logo.jpg';

const Login = () => {
  const navigate = useNavigate();
  const { login, user, loading: authLoading } = useAuth();

  const [formData, setFormData] = useState({ email: '', password: '', rememberMe: false });
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  useEffect(() => {
    if (user && !authLoading) navigate('/dashboard', { replace: true });
  }, [user, authLoading, navigate]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
    if (errors[name]) setErrors(prev => ({ ...prev, [name]: '' }));
    setServerError('');
  };

  const validate = () => {
    const newErrors = {};
    if (!formData.email.trim()) newErrors.email = 'Email is required';
    if (!formData.password) newErrors.password = 'Password is required';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;
    setLoading(true);
    try {
      const result = await login(formData.email.trim(), formData.password, formData.rememberMe);
      if (result.success) {
        navigate('/dashboard');
      } else {
        setServerError(result.error || 'Login failed. Please try again.');
      }
    } catch {
      setServerError('An unexpected error occurred. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (authLoading) {
    return (
      <div className="login-loading">
        <div className="spinner" />
      </div>
    );
  }

  return (
    <div className="login-page">
      <div className="login-bg">
        <div className="login-orb login-orb-1" />
        <div className="login-orb login-orb-2" />
      </div>

      <div className="login-card">
        <Link to="/" className="login-brand-link">
          <div className="login-logo-wrap">
            <img src={logo} alt="CIT MedConnect+" onError={e => e.target.style.display = 'none'} />
          </div>
          <span className="login-brand-name">
            CIT MedConnect<span className="brand-plus">+</span>
          </span>
        </Link>

        <div className="login-header">
          <h1>Welcome Back</h1>
          <p>Sign in to access your account</p>
        </div>

        <form onSubmit={handleSubmit} className="login-form">
          {serverError && <div className="login-error">{serverError}</div>}

          <div className="lf-group">
            <div className="lf-input-wrap">
              <Mail size={18} className="lf-icon" />
              <input
                type="email"
                name="email"
                placeholder="firstname.lastname@cit.edu"
                value={formData.email}
                onChange={handleChange}
                className={errors.email ? 'error' : ''}
                disabled={loading}
                autoComplete="email"
              />
            </div>
            {errors.email && <span className="lf-error">{errors.email}</span>}
          </div>

          <div className="lf-group">
            <div className="lf-input-wrap">
              <Lock size={18} className="lf-icon" />
              <input
                type={showPassword ? 'text' : 'password'}
                name="password"
                placeholder="Password"
                value={formData.password}
                onChange={handleChange}
                className={errors.password ? 'error' : ''}
                disabled={loading}
                autoComplete="current-password"
              />
              <button
                type="button"
                className="lf-eye"
                onClick={() => setShowPassword(p => !p)}
                tabIndex="-1"
              >
                {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
            {errors.password && <span className="lf-error">{errors.password}</span>}
          </div>

          <label className="lf-remember">
            <input
              type="checkbox"
              name="rememberMe"
              checked={formData.rememberMe}
              onChange={handleChange}
            />
            <span>Remember me</span>
          </label>

          <button type="submit" className="lf-submit" disabled={loading}>
            {loading ? (
              <><div className="btn-spinner" /><span>Signing in...</span></>
            ) : 'Sign In'}
          </button>
        </form>

        <div className="login-footer">
          <p>
            Don&apos;t have an account?{' '}
            <Link to="/register" className="login-text-link">Create one</Link>
          </p>
          <Link to="/" className="login-back-link">← Back to Home</Link>
        </div>
      </div>
    </div>
  );
};

export default Login;
