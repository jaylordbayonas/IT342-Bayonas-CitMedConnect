/* eslint-disable react/prop-types */
import React, { useState, useCallback, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import { Mail, Lock, Eye, EyeOff, Github } from 'lucide-react';
import { initiateGitHubLogin } from '../services/github-oauth-service';
import './LandingPage.css';
import Galaxy from './Galaxy';

const logo   = '/images/medconnet_appiconv2.png';
const mascot = '/images/MascotCit.png';

/* ============================================
   MAIN COMPONENT
   ============================================ */

const LandingPage = () => {
  const navigate = useNavigate();
  const { login, register, user, loading: authLoading } = useAuth();

  const [activeTab,           setActiveTab]           = useState('login');
  const [showPassword,        setShowPassword]        = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [loginData, setLoginData] = useState({
    schoolId:   '',
    password:   '',
    rememberMe: false,
  });

  const [registerData, setRegisterData] = useState({
    schoolId:        '',
    firstName:       '',
    lastName:        '',
    email:           '',
    phone:           '',
    gender:          '',
    age:             '',
    password:        '',
    confirmPassword: '',
    role:            'student',
  });

  const [errors,      setErrors]      = useState({});
  const [loading,     setLoading]     = useState(false);
  const [serverError, setServerError] = useState('');
  const [success,     setSuccess]     = useState('');

  useEffect(() => {
    if (user && !authLoading) navigate('/dashboard', { replace: true });
  }, [user, authLoading, navigate]);

  /* ── handlers ── */
  const handleLoginChange = useCallback((e) => {
    const { name, value, type, checked } = e.target;
    setLoginData((p) => ({ ...p, [name]: type === 'checkbox' ? checked : value }));
    if (errors[name]) setErrors((p) => ({ ...p, [name]: '' }));
    setServerError('');
  }, [errors]);

  const validateLogin = useCallback(() => {
    const errs = {};
    if (!loginData.schoolId.trim()) errs.schoolId = 'Email is required';
    if (!loginData.password)        errs.password  = 'Password is required';
    setErrors(errs);
    return Object.keys(errs).length === 0;
  }, [loginData]);

  const handleLogin = useCallback(async (e) => {
    e.preventDefault();
    setServerError('');
    if (!validateLogin()) return;
    setLoading(true);
    try {
      const result = await login(loginData.schoolId.trim(), loginData.password, loginData.rememberMe);
      if (!result.success) setServerError(result.error || 'Login failed. Please try again.');
    } catch {
      setServerError('An unexpected error occurred. Please try again.');
    } finally {
      setLoading(false);
    }
  }, [loginData, login, validateLogin]);

  const handleRegisterChange = useCallback((e) => {
    const { name, value } = e.target;
    setRegisterData((p) => ({ ...p, [name]: value }));
    if (errors[name]) setErrors((p) => ({ ...p, [name]: '' }));
    setServerError('');
    setSuccess('');
  }, [errors]);

  const validateRegister = useCallback(() => {
    const errs = {};
    if (!registerData.schoolId.trim())   errs.schoolId    = 'School ID is required';
    if (!registerData.firstName.trim())  errs.firstName   = 'First name is required';
    if (!registerData.lastName.trim())   errs.lastName    = 'Last name is required';
    if (!/^[a-z]+\.[a-z]+@cit\.edu$/i.test(registerData.email))
      errs.email = 'Must be in format: firstname.lastname@cit.edu';
    if (!/^\d{7,15}$/.test(registerData.phone.replaceAll(/[-\s+()]/g, '')))
      errs.phone = 'Enter a valid phone number';
    if (!registerData.gender) errs.gender = 'Please select a gender';
    const ageNum = Number.parseInt(registerData.age, 10);
    if (!registerData.age || Number.isNaN(ageNum) || ageNum < 15 || ageNum > 100)
      errs.age = 'Enter a valid age (15–100)';
    if (registerData.password.length < 6)
      errs.password = 'Password must be at least 6 characters';
    if (registerData.password !== registerData.confirmPassword)
      errs.confirmPassword = 'Passwords do not match';
    setErrors(errs);
    return Object.keys(errs).length === 0;
  }, [registerData]);

  const handleRegister = useCallback(async (e) => {
    e.preventDefault();
    setServerError('');
    setSuccess('');
    if (!validateRegister()) return;
    setLoading(true);
    try {
      const result = await register({
        schoolId:        registerData.schoolId.trim(),
        firstName:       registerData.firstName.trim(),
        lastName:        registerData.lastName.trim(),
        email:           registerData.email.trim().toLowerCase(),
        phone:           registerData.phone.trim(),
        gender:          registerData.gender,
        age:             Number.parseInt(registerData.age, 10),
        password:        registerData.password,
        confirmPassword: registerData.confirmPassword,
        role:            registerData.role,
      });
      if (result?.success) {
        setSuccess(`Registration successful! Your School ID is: ${result.user.schoolId}. You can now log in.`);
        setRegisterData({ schoolId: '', firstName: '', lastName: '', email: '', phone: '', gender: '', age: '', password: '', confirmPassword: '', role: 'student' });
        setTimeout(() => { setActiveTab('login'); setSuccess(''); }, 3000);
      } else {
        setServerError(result?.error || 'Registration failed. Please try again.');
      }
    } catch (error) {
      setServerError(error.message || 'An unexpected error occurred. Please try again.');
    } finally {
      setLoading(false);
    }
  }, [register, registerData, validateRegister]);

  const handleTabChange = useCallback((tab) => {
    setActiveTab(tab);
    setErrors({});
    setServerError('');
    setSuccess('');
  }, []);

  if (authLoading) {
    return (
      <div className="lp-loading">
        <div className="lp-spinner" />
        <p className="lp-loading-text">Loading…</p>
      </div>
    );
  }

  return (
    <div className="lp-root">
      {/* Ambient glow orbs */}
      <div className="lp-orb lp-orb-1" />
      <div className="lp-orb lp-orb-2" />
      <div className="lp-orb lp-orb-3" />

      <div className="lp-split">

        {/* ────────────────────── LEFT ────────────────────── */}
        <div className="lp-left">
          <Galaxy
            className="lp-galaxy"
            mouseRepulsion
            mouseInteraction
            density={1.2}
            glowIntensity={0.25}
            twinkleIntensity={0.4}
            rotationSpeed={0.04}
            repulsionStrength={1.5}
            autoCenterRepulsion={0}
            starSpeed={0.4}
            speed={0.8}
            transparent
          />

          {/* Logo row */}
          <div className="lp-brand">
            <img
              src={logo} alt="CIT MedConnect+ logo" className="lp-logo-img"
              onError={(e) => { e.target.style.display = 'none'; }}
            />
            <span className="lp-logo-text">
              CIT MedConnect<span className="lp-logo-plus">+</span>
            </span>
          </div>

          {/* Mascot + glow rings */}
          <div className="lp-mascot-wrap">
            <div className="lp-ring lp-ring-outer" />
            <div className="lp-ring lp-ring-mid"   />
            <div className="lp-ring lp-ring-inner" />
            <img
              src={mascot} alt="CIT MedConnect+ mascot" className="lp-mascot-img"
              onError={(e) => { e.target.style.display = 'none'; }}
            />
          </div>

          {/* Hero text */}
          <div className="lp-hero-text">
            <h1 className="lp-heading">
              Welcome to<br />
              <span className="lp-heading-accent">CIT MedConnect+</span>
            </h1>
            <p className="lp-subtitle">
              Your smart companion for appointments<br />
              and campus healthcare services.
            </p>
          </div>
        </div>

        {/* ────────────────────── RIGHT ────────────────────── */}
        <div className="lp-right">
          <div className="lp-card">

            {/* Tabs */}
            <div className="lp-tabs">
              <button
                type="button"
                className={`lp-tab ${activeTab === 'login'    ? 'lp-tab-active' : ''}`}
                onClick={() => handleTabChange('login')}
              >
                Login
              </button>
              <button
                type="button"
                className={`lp-tab ${activeTab === 'register' ? 'lp-tab-active' : ''}`}
                onClick={() => handleTabChange('register')}
              >
                Register
              </button>
            </div>

            {/* ── LOGIN ── */}
            {activeTab === 'login' && (
              <div className="lp-panel">
                <div className="lp-panel-header">
                  <h2 className="lp-panel-title">Welcome Back</h2>
                  <p className="lp-panel-sub">Sign in to access your account</p>
                </div>

                <form onSubmit={handleLogin} className="lp-form">
                  {serverError && <div className="lp-alert lp-alert-error">{serverError}</div>}
                  {success     && <div className="lp-alert lp-alert-success">{success}</div>}

                  <div className="lp-field">
                    <div className="lp-input-wrap">
                      <Mail size={16} className="lp-input-icon" />
                      <input
                        type="text" name="schoolId"
                        placeholder="firstname.lastname@cit.edu"
                        value={loginData.schoolId}
                        onChange={handleLoginChange}
                        className={`lp-input ${errors.schoolId ? 'lp-input-err' : ''}`}
                        disabled={loading} autoComplete="username"
                      />
                    </div>
                    {errors.schoolId && <span className="lp-err-msg">{errors.schoolId}</span>}
                  </div>

                  <div className="lp-field">
                    <div className="lp-input-wrap">
                      <Lock size={16} className="lp-input-icon" />
                      <input
                        type={showPassword ? 'text' : 'password'} name="password"
                        placeholder="Password"
                        value={loginData.password}
                        onChange={handleLoginChange}
                        className={`lp-input ${errors.password ? 'lp-input-err' : ''}`}
                        disabled={loading} autoComplete="current-password"
                      />
                      <button type="button" className="lp-eye-btn"
                              onClick={() => setShowPassword((v) => !v)} tabIndex="-1">
                        {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                      </button>
                    </div>
                    {errors.password && <span className="lp-err-msg">{errors.password}</span>}
                  </div>

                  <div className="lp-options-row">
                    <label className="lp-checkbox-label">
                      <input
                        type="checkbox" name="rememberMe"
                        checked={loginData.rememberMe}
                        onChange={handleLoginChange}
                        disabled={loading}
                      />
                      <span>Remember me</span>
                    </label>
                  </div>

                  <button type="submit" className="lp-btn-primary" disabled={loading}>
                    {loading
                      ? <><div className="lp-btn-spinner" /><span>Signing in…</span></>
                      : 'Sign In'}
                  </button>

                  <div className="lp-divider"><span>or</span></div>

                  <button type="button" className="lp-btn-oauth"
                          onClick={initiateGitHubLogin} disabled={loading}>
                    <Github size={16} />
                    <span>Sign in with GitHub</span>
                  </button>
                </form>

                <p className="lp-switch-text">
                  Don&apos;t have an account?{' '}
                  <button type="button" className="lp-switch-link"
                          onClick={() => handleTabChange('register')}>
                    Sign up
                  </button>
                </p>
              </div>
            )}

            {/* ── REGISTER ── */}
            {activeTab === 'register' && (
              <div className="lp-panel">
                <div className="lp-panel-header">
                  <h2 className="lp-panel-title">Create Account</h2>
                  <p className="lp-panel-sub">Register to get started</p>
                </div>

                <form onSubmit={handleRegister} className="lp-form">
                  {serverError && <div className="lp-alert lp-alert-error">{serverError}</div>}
                  {success     && <div className="lp-alert lp-alert-success">{success}</div>}

                  <div className="lp-field">
                    <div className="lp-input-wrap">
                      <Mail size={16} className="lp-input-icon" />
                      <input
                        type="text" name="schoolId" placeholder="School ID (e.g. 21-1234-567)"
                        value={registerData.schoolId} onChange={handleRegisterChange}
                        className={`lp-input ${errors.schoolId ? 'lp-input-err' : ''}`}
                        disabled={loading}
                      />
                    </div>
                    {errors.schoolId && <span className="lp-err-msg">{errors.schoolId}</span>}
                  </div>

                  <div className="lp-row">
                    <div className="lp-field">
                      <div className="lp-input-wrap">
                        <Mail size={16} className="lp-input-icon" />
                        <input type="text" name="firstName" placeholder="First Name"
                               value={registerData.firstName} onChange={handleRegisterChange}
                               className={`lp-input ${errors.firstName ? 'lp-input-err' : ''}`}
                               disabled={loading} />
                      </div>
                      {errors.firstName && <span className="lp-err-msg">{errors.firstName}</span>}
                    </div>
                    <div className="lp-field">
                      <div className="lp-input-wrap">
                        <Mail size={16} className="lp-input-icon" />
                        <input type="text" name="lastName" placeholder="Last Name"
                               value={registerData.lastName} onChange={handleRegisterChange}
                               className={`lp-input ${errors.lastName ? 'lp-input-err' : ''}`}
                               disabled={loading} />
                      </div>
                      {errors.lastName && <span className="lp-err-msg">{errors.lastName}</span>}
                    </div>
                  </div>

                  <div className="lp-field">
                    <div className="lp-input-wrap">
                      <Mail size={16} className="lp-input-icon" />
                      <input type="email" name="email" placeholder="firstname.lastname@cit.edu"
                             value={registerData.email} onChange={handleRegisterChange}
                             className={`lp-input ${errors.email ? 'lp-input-err' : ''}`}
                             disabled={loading} autoComplete="email" />
                    </div>
                    {errors.email && <span className="lp-err-msg">{errors.email}</span>}
                  </div>

                  <div className="lp-row">
                    <div className="lp-field">
                      <div className="lp-input-wrap">
                        <Mail size={16} className="lp-input-icon" />
                        <input type="tel" name="phone" placeholder="Phone Number"
                               value={registerData.phone} onChange={handleRegisterChange}
                               className={`lp-input ${errors.phone ? 'lp-input-err' : ''}`}
                               disabled={loading} />
                      </div>
                      {errors.phone && <span className="lp-err-msg">{errors.phone}</span>}
                    </div>
                    <div className="lp-field">
                      <select name="gender" value={registerData.gender}
                              onChange={handleRegisterChange}
                              className={`lp-select ${errors.gender ? 'lp-input-err' : ''}`}
                              disabled={loading}>
                        <option value="">Gender</option>
                        <option value="Male">Male</option>
                        <option value="Female">Female</option>
                        <option value="Other">Other / Prefer not to say</option>
                      </select>
                      {errors.gender && <span className="lp-err-msg">{errors.gender}</span>}
                    </div>
                  </div>

                  <div className="lp-field">
                    <div className="lp-input-wrap">
                      <input type="number" name="age" placeholder="Age"
                             value={registerData.age} onChange={handleRegisterChange}
                             className={`lp-input lp-input-noicon ${errors.age ? 'lp-input-err' : ''}`}
                             disabled={loading} min="15" max="100" />
                    </div>
                    {errors.age && <span className="lp-err-msg">{errors.age}</span>}
                  </div>

                  <div className="lp-field">
                    <div className="lp-input-wrap">
                      <Lock size={16} className="lp-input-icon" />
                      <input type={showPassword ? 'text' : 'password'} name="password"
                             placeholder="Password (min 6 characters)"
                             value={registerData.password} onChange={handleRegisterChange}
                             className={`lp-input ${errors.password ? 'lp-input-err' : ''}`}
                             disabled={loading} autoComplete="new-password" />
                      <button type="button" className="lp-eye-btn"
                              onClick={() => setShowPassword((v) => !v)} tabIndex="-1">
                        {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                      </button>
                    </div>
                    {errors.password && <span className="lp-err-msg">{errors.password}</span>}
                  </div>

                  <div className="lp-field">
                    <div className="lp-input-wrap">
                      <Lock size={16} className="lp-input-icon" />
                      <input type={showConfirmPassword ? 'text' : 'password'} name="confirmPassword"
                             placeholder="Confirm Password"
                             value={registerData.confirmPassword} onChange={handleRegisterChange}
                             className={`lp-input ${errors.confirmPassword ? 'lp-input-err' : ''}`}
                             disabled={loading} autoComplete="new-password" />
                      <button type="button" className="lp-eye-btn"
                              onClick={() => setShowConfirmPassword((v) => !v)} tabIndex="-1">
                        {showConfirmPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                      </button>
                    </div>
                    {errors.confirmPassword && <span className="lp-err-msg">{errors.confirmPassword}</span>}
                  </div>

                  <button type="submit" className="lp-btn-primary" disabled={loading}>
                    {loading
                      ? <><div className="lp-btn-spinner" /><span>Creating account…</span></>
                      : 'Create Account'}
                  </button>

                  <div className="lp-divider"><span>or</span></div>

                  <button type="button" className="lp-btn-oauth"
                          onClick={initiateGitHubLogin} disabled={loading}>
                    <Github size={16} />
                    <span>Sign up with GitHub</span>
                  </button>
                </form>

                <p className="lp-switch-text">
                  Already have an account?{' '}
                  <button type="button" className="lp-switch-link"
                          onClick={() => handleTabChange('login')}>
                    Sign in
                  </button>
                </p>
              </div>
            )}

          </div>
        </div>
      </div>
    </div>
  );
};

export default LandingPage;
