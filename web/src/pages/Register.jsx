import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import { User, Mail, Lock, Eye, EyeOff, Phone, BookOpen } from 'lucide-react';
import './Register.css';

const logo = '/images/logo.jpg';

const Register = () => {
  const navigate = useNavigate();
  const { register, user, loading: authLoading } = useAuth();

  const [formData, setFormData] = useState({
    schoolId: '',
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    gender: '',
    age: '',
    password: '',
    confirmPassword: '',
    role: 'student',
  });
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  useEffect(() => {
    if (user && !authLoading) navigate('/dashboard', { replace: true });
  }, [user, authLoading, navigate]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (errors[name]) setErrors(prev => ({ ...prev, [name]: '' }));
    setServerError('');
    setSuccess('');
  };

  const validate = () => {
    const newErrors = {};

    if (!formData.schoolId.trim())
      newErrors.schoolId = 'School ID is required';

    if (!formData.firstName.trim())
      newErrors.firstName = 'First name is required';

    if (!formData.lastName.trim())
      newErrors.lastName = 'Last name is required';

    if (!/^[a-z]+\.[a-z]+@cit\.edu$/i.test(formData.email))
      newErrors.email = 'Must be in format: firstname.lastname@cit.edu';

    if (!/^\d{7,15}$/.test(formData.phone.replace(/[-\s+()]/g, '')))
      newErrors.phone = 'Enter a valid phone number';

    if (!formData.gender)
      newErrors.gender = 'Please select a gender';

    const ageNum = parseInt(formData.age);
    if (!formData.age || isNaN(ageNum) || ageNum < 15 || ageNum > 100)
      newErrors.age = 'Enter a valid age (15–100)';

    if (formData.password.length < 6)
      newErrors.password = 'Password must be at least 6 characters';

    if (formData.password !== formData.confirmPassword)
      newErrors.confirmPassword = 'Passwords do not match';

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;
    setLoading(true);
    try {
      const result = await register({
        schoolId: formData.schoolId.trim(),
        firstName: formData.firstName.trim(),
        lastName: formData.lastName.trim(),
        email: formData.email.trim().toLowerCase(),
        phone: formData.phone.trim(),
        gender: formData.gender,
        age: parseInt(formData.age),
        password: formData.password,
        role: formData.role,
      });

      if (result.success) {
        setSuccess(`Account created! School ID: ${result.user.schoolId}. Redirecting to login...`);
        setTimeout(() => navigate('/login'), 3000);
      } else {
        setServerError(result.error || 'Registration failed. Please try again.');
      }
    } catch {
      setServerError('An unexpected error occurred. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (authLoading) {
    return (
      <div className="register-loading">
        <div className="spinner" />
      </div>
    );
  }

  return (
    <div className="register-page">
      <div className="register-bg">
        <div className="register-orb register-orb-1" />
        <div className="register-orb register-orb-2" />
      </div>

      <div className="register-card">
        <Link to="/" className="register-brand-link">
          <div className="register-logo-wrap">
            <img src={logo} alt="CIT MedConnect+" onError={e => e.target.style.display = 'none'} />
          </div>
          <span className="register-brand-name">
            CIT MedConnect<span className="brand-plus">+</span>
          </span>
        </Link>

        <div className="register-header">
          <h1>Create Account</h1>
          <p>Fill in your details to get started</p>
        </div>

        <form onSubmit={handleSubmit} className="register-form">
          {serverError && <div className="register-error">{serverError}</div>}
          {success && <div className="register-success">{success}</div>}

          {/* School ID */}
          <InputField
            icon={<BookOpen size={16} />}
            name="schoolId"
            type="text"
            placeholder="School ID (e.g. 21-1234-567)"
            value={formData.schoolId}
            onChange={handleChange}
            error={errors.schoolId}
            disabled={loading}
          />

          {/* First & Last Name */}
          <div className="rf-row">
            <InputField
              icon={<User size={16} />}
              name="firstName"
              type="text"
              placeholder="First Name"
              value={formData.firstName}
              onChange={handleChange}
              error={errors.firstName}
              disabled={loading}
            />
            <InputField
              icon={<User size={16} />}
              name="lastName"
              type="text"
              placeholder="Last Name"
              value={formData.lastName}
              onChange={handleChange}
              error={errors.lastName}
              disabled={loading}
            />
          </div>

          {/* Email */}
          <InputField
            icon={<Mail size={16} />}
            name="email"
            type="email"
            placeholder="firstname.lastname@cit.edu"
            value={formData.email}
            onChange={handleChange}
            error={errors.email}
            disabled={loading}
            autoComplete="email"
          />

          {/* Phone & Gender */}
          <div className="rf-row">
            <InputField
              icon={<Phone size={16} />}
              name="phone"
              type="tel"
              placeholder="Phone Number"
              value={formData.phone}
              onChange={handleChange}
              error={errors.phone}
              disabled={loading}
            />
            <div className="rf-group">
              <div className="rf-select-wrap">
                <select
                  name="gender"
                  value={formData.gender}
                  onChange={handleChange}
                  className={errors.gender ? 'error' : ''}
                  disabled={loading}
                >
                  <option value="">Gender</option>
                  <option value="Male">Male</option>
                  <option value="Female">Female</option>
                  <option value="Other">Other / Prefer not to say</option>
                </select>
              </div>
              {errors.gender && <span className="rf-error">{errors.gender}</span>}
            </div>
          </div>

          {/* Age */}
          <InputField
            name="age"
            type="number"
            placeholder="Age"
            value={formData.age}
            onChange={handleChange}
            error={errors.age}
            disabled={loading}
            min="15"
            max="100"
          />

          {/* Password */}
          <InputField
            icon={<Lock size={16} />}
            name="password"
            type={showPassword ? 'text' : 'password'}
            placeholder="Password (min 6 characters)"
            value={formData.password}
            onChange={handleChange}
            error={errors.password}
            disabled={loading}
            autoComplete="new-password"
            onToggleEye={() => setShowPassword(p => !p)}
            showEye={showPassword}
          />

          {/* Confirm Password */}
          <InputField
            icon={<Lock size={16} />}
            name="confirmPassword"
            type={showConfirmPassword ? 'text' : 'password'}
            placeholder="Confirm Password"
            value={formData.confirmPassword}
            onChange={handleChange}
            error={errors.confirmPassword}
            disabled={loading}
            autoComplete="new-password"
            onToggleEye={() => setShowConfirmPassword(p => !p)}
            showEye={showConfirmPassword}
          />

          <button type="submit" className="rf-submit" disabled={loading}>
            {loading ? (
              <><div className="btn-spinner" /><span>Creating account...</span></>
            ) : 'Create Account'}
          </button>
        </form>

        <div className="register-footer">
          <p>
            Already have an account?{' '}
            <Link to="/login" className="register-text-link">Sign in</Link>
          </p>
          <Link to="/" className="register-back-link">← Back to Home</Link>
        </div>
      </div>
    </div>
  );
};

/* Reusable input field */
const InputField = ({
  icon, name, type, placeholder, value, onChange,
  error, disabled, autoComplete, onToggleEye, showEye, min, max
}) => (
  <div className="rf-group">
    <div className="rf-input-wrap">
      {icon && <span className="rf-icon">{icon}</span>}
      <input
        type={type}
        name={name}
        placeholder={placeholder}
        value={value}
        onChange={onChange}
        className={error ? 'error' : ''}
        disabled={disabled}
        autoComplete={autoComplete}
        min={min}
        max={max}
        style={!icon ? { paddingLeft: '1rem' } : {}}
      />
      {onToggleEye && (
        <button type="button" className="rf-eye" onClick={onToggleEye} tabIndex="-1">
          {showEye ? <EyeOff size={16} /> : <Eye size={16} />}
        </button>
      )}
    </div>
    {error && <span className="rf-error">{error}</span>}
  </div>
);

export default Register;
