/* eslint-disable react/prop-types */
import React, { useState, useCallback, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import {
  Settings, Stethoscope, Calendar, Users, Shield, Clock,
  ChevronRight, Sparkles, Activity, Heart, Zap, Award,
  CheckCircle2, Star, TrendingUp, Globe, Mail, Lock, Eye, EyeOff, Github
} from 'lucide-react';
import { initiateGitHubLogin } from '../services/github-oauth-service';
import './Landing.css';

const logo = '/images/logo.jpg';

const Landing = () => {
  const navigate = useNavigate();
  const { login, register, user, loading: authLoading } = useAuth();

  const [activeTab, setActiveTab] = useState('login');
  const [isScrolled, setIsScrolled] = useState(false);
  const [mousePosition, setMousePosition] = useState({ x: 0, y: 0 });

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [loginData, setLoginData] = useState({
    schoolId: '',
    password: '',
    rememberMe: false,
  });

  const [registerData, setRegisterData] = useState({
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
  const [loading, setLoading] = useState(false);
  const [serverError, setServerError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    if (user && !authLoading) {
      navigate('/dashboard', { replace: true });
    }
  }, [user, authLoading, navigate]);

  useEffect(() => {
    let ticking = false;
    const handleScroll = () => {
      if (!ticking) {
        globalThis.requestAnimationFrame(() => {
          setIsScrolled(globalThis.scrollY > 20);
          ticking = false;
        });
        ticking = true;
      }
    };
    globalThis.addEventListener('scroll', handleScroll, { passive: true });
    return () => globalThis.removeEventListener('scroll', handleScroll);
  }, []);

  useEffect(() => {
    const handleMouseMove = (e) => {
      setMousePosition({
        x: (e.clientX / globalThis.innerWidth) * 20,
        y: (e.clientY / globalThis.innerHeight) * 20
      });
    };
    globalThis.addEventListener('mousemove', handleMouseMove);
    return () => globalThis.removeEventListener('mousemove', handleMouseMove);
  }, []);

  const handleNavLinkClick = useCallback((e, section) => {
    e.preventDefault();
    const element = document.querySelector(`#${section}`);
    if (element) {
      const offsetPosition = element.getBoundingClientRect().top + globalThis.pageYOffset - 100;
      globalThis.scrollTo({ top: offsetPosition, behavior: 'smooth' });
    }
  }, []);

  const handleLoginChange = useCallback((e) => {
    const { name, value, type, checked } = e.target;
    setLoginData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));

    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }));
    }
    setServerError('');
  }, [errors]);

  const validateLogin = useCallback(() => {
    const newErrors = {};
    if (!loginData.schoolId.trim()) newErrors.schoolId = 'Email is required';
    if (!loginData.password) newErrors.password = 'Password is required';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  }, [loginData]);

  const handleLogin = useCallback(async (e) => {
    e.preventDefault();
    setServerError('');

    if (!validateLogin()) return;

    setLoading(true);
    try {
      const result = await login(loginData.schoolId.trim(), loginData.password, loginData.rememberMe);
      if (!result.success) {
        setServerError(result.error || 'Login failed. Please try again.');
      }
    } catch {
      setServerError('An unexpected error occurred. Please try again.');
    } finally {
      setLoading(false);
    }
  }, [loginData, login, validateLogin]);

  const handleRegisterChange = useCallback((e) => {
    const { name, value } = e.target;
    setRegisterData((prev) => ({ ...prev, [name]: value }));

    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }));
    }
    setServerError('');
    setSuccess('');
  }, [errors]);

  const validateRegister = useCallback(() => {
    const newErrors = {};

    if (!registerData.schoolId.trim()) {
      newErrors.schoolId = 'School ID is required';
    }
    if (!registerData.firstName.trim()) {
      newErrors.firstName = 'First name is required';
    }
    if (!registerData.lastName.trim()) {
      newErrors.lastName = 'Last name is required';
    }
    if (!/^[a-z]+\.[a-z]+@cit\.edu$/i.test(registerData.email)) {
      newErrors.email = 'Must be in format: firstname.lastname@cit.edu';
    }
    if (!/^\d{7,15}$/.test(registerData.phone.replaceAll(/[-\s+()]/g, ''))) {
      newErrors.phone = 'Enter a valid phone number';
    }
    if (!registerData.gender) {
      newErrors.gender = 'Please select a gender';
    }
    const ageNum = Number.parseInt(registerData.age, 10);
    if (!registerData.age || Number.isNaN(ageNum) || ageNum < 15 || ageNum > 100) {
      newErrors.age = 'Enter a valid age (15-100)';
    }
    if (registerData.password.length < 6) {
      newErrors.password = 'Password must be at least 6 characters';
    }
    if (registerData.password !== registerData.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  }, [registerData]);

  const handleRegister = useCallback(async (e) => {
    e.preventDefault();
    setServerError('');
    setSuccess('');

    if (!validateRegister()) return;

    setLoading(true);
    try {
      const result = await register({
        schoolId: registerData.schoolId.trim(),
        firstName: registerData.firstName.trim(),
        lastName: registerData.lastName.trim(),
        email: registerData.email.trim().toLowerCase(),
        phone: registerData.phone.trim(),
        gender: registerData.gender,
        age: Number.parseInt(registerData.age, 10),
        password: registerData.password,
        confirmPassword: registerData.confirmPassword,
        role: registerData.role,
      });

      if (result?.success) {
        setSuccess(`Registration successful! Your School ID is: ${result.user.schoolId}. You can now log in.`);
        setRegisterData({
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
        setTimeout(() => {
          setActiveTab('login');
          setSuccess('');
        }, 3000);
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

  const handleGetStarted = useCallback(() => {
    setActiveTab('register');
    setTimeout(() => {
      const authCard = document.querySelector('.auth-card');
      if (authCard) {
        authCard.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }
    }, 120);
  }, []);

  const features = [
    {
      icon: Calendar,
      title: "Smart Scheduling",
      description: "AI-powered appointment booking that adapts to your schedule and preferences",
      gradient: "from-blue-500",
      iconColor: "#3B82F6"
    },
    {
      icon: Stethoscope,
      title: "Expert Medical Care",
      description: "Connect with board-certified healthcare professionals instantly",
      gradient: "from-purple-500",
      iconColor: "#A855F7"
    },
    {
      icon: Shield,
      title: "Military-Grade Security",
      description: "Your health data protected with end-to-end encryption and HIPAA compliance",
      gradient: "from-emerald-500",
      iconColor: "#10B981"
    },
    {
      icon: Activity,
      title: "Real-Time Monitoring",
      description: "Track your health metrics and receive instant insights from your care team",
      gradient: "from-orange-500",
      iconColor: "#F97316"
    },
    {
      icon: Heart,
      title: "Personalized Care Plans",
      description: "Custom treatment plans tailored to your unique health journey",
      gradient: "from-rose-500",
      iconColor: "#F43F5E"
    },
    {
      icon: Zap,
      title: "Lightning Fast Access",
      description: "Get medical advice and prescriptions in minutes, not days",
      gradient: "from-amber-500",
      iconColor: "#F59E0B"
    }
  ];

  const stats = [
    { value: "50K+", label: "Active Users", trend: "+127%", icon: Users },
    { value: "200+", label: "Healthcare Providers", trend: "+85%", icon: Stethoscope },
    { value: "99.8%", label: "Satisfaction Rate", trend: "+2.3%", icon: Star },
    { value: "24/7", label: "Support Available", trend: "Always", icon: Clock }
  ];

  const benefits = [
    { icon: CheckCircle2, text: "No waiting rooms - virtual consultations" },
    { icon: CheckCircle2, text: "Instant prescription refills" },
    { icon: CheckCircle2, text: "Secure medical records access" },
    { icon: CheckCircle2, text: "24/7 emergency support" },
    { icon: CheckCircle2, text: "Multi-specialist coordination" },
    { icon: CheckCircle2, text: "Insurance integration" }
  ];

  const testimonials = [
    {
      name: "Dr. Sarah Chen",
      role: "Chief Medical Officer",
      content: "The most comprehensive healthcare platform I've encountered in my 20-year career.",
      rating: 5,
      avatar: "SC"
    },
    {
      name: "Marcus Rodriguez",
      role: "CIT Student",
      content: "Changed how I manage my health. Appointments are seamless and doctors are incredibly responsive.",
      rating: 5,
      avatar: "MR"
    },
    {
      name: "Prof. Amanda Lee",
      role: "Faculty Member",
      content: "Finally, a healthcare system that respects my time. The scheduling intelligence is remarkable.",
      rating: 5,
      avatar: "AL"
    }
  ];

  const footerLinks = {
    product: [
      { label: "Features", href: "#features" },
      { label: "Pricing", href: "#pricing" },
      { label: "Security", href: "#security" },
      { label: "Updates", href: "#updates" }
    ],
    company: [
      { label: "About Us", href: "#about" },
      { label: "Careers", href: "#careers" },
      { label: "Press Kit", href: "#press" },
      { label: "Contact", href: "#contact" }
    ],
    resources: [
      { label: "Documentation", href: "#docs" },
      { label: "API Reference", href: "#api" },
      { label: "Support Center", href: "#support" },
      { label: "System Status", href: "#status" }
    ],
    legal: [
      { label: "Privacy Policy", href: "#privacy" },
      { label: "Terms of Service", href: "#terms" },
      { label: "HIPAA Compliance", href: "#hipaa" },
      { label: "Cookie Policy", href: "#cookies" }
    ]
  };

  if (authLoading) {
    return (
      <div className="loading-container">
        <div className="spinner" />
        <p>Loading...</p>
      </div>
    );
  }

  return (
    <div className="landing-page">
      <Navigation
        activeTab={activeTab}
        onTabChange={handleTabChange}
        onNavLinkClick={handleNavLinkClick}
        isScrolled={isScrolled}
      />

      <HeroSection
        activeTab={activeTab}
        onTabChange={handleTabChange}
        mousePosition={mousePosition}
        onGetStarted={handleGetStarted}
        loginData={loginData}
        registerData={registerData}
        handleLoginChange={handleLoginChange}
        handleRegisterChange={handleRegisterChange}
        handleLogin={handleLogin}
        handleRegister={handleRegister}
        showPassword={showPassword}
        setShowPassword={setShowPassword}
        showConfirmPassword={showConfirmPassword}
        setShowConfirmPassword={setShowConfirmPassword}
        errors={errors}
        loading={loading}
        serverError={serverError}
        success={success}
      />

      <TrustBadges />
      <FeaturesSection features={features} />
      <BenefitsSection benefits={benefits} />
      <StatsSection stats={stats} />
      <TestimonialsSection testimonials={testimonials} />
      <CTASection onGetStarted={handleGetStarted} />
      <Footer links={footerLinks} />
    </div>
  );
};

// ============================================
// NAVIGATION
// ============================================

const Navigation = React.memo(({ activeTab, onTabChange, onNavLinkClick, isScrolled }) => (
  <nav className={`landing-nav ${isScrolled ? 'scrolled' : ''}`}>
    <div className="nav-container">
      <div className="nav-left">
        <div className="nav-logo-wrapper">
          <img src={logo} alt="CIT MedConnect+" className="nav-logo" onError={e => e.target.style.display = 'none'} />
        </div>
        <span className="nav-brand">
          CIT MedConnect<span className="brand-plus">+</span>
        </span>
      </div>

      <div className="nav-center">
        <div className="nav-links">
          <button className="nav-link" onClick={e => onNavLinkClick(e, 'features')}>Features</button>
          <button className="nav-link" onClick={e => onNavLinkClick(e, 'benefits')}>Benefits</button>
          <button className="nav-link" onClick={e => onNavLinkClick(e, 'testimonials')}>Testimonials</button>
          <button className="nav-link" onClick={e => onNavLinkClick(e, 'contact')}>Contact</button>
        </div>
      </div>

    </div>
  </nav>
));

Navigation.displayName = 'Navigation';

// ============================================
// HERO SECTION
// ============================================

const HeroSection = React.memo(({
  activeTab,
  onTabChange,
  mousePosition,
  onGetStarted,
  loginData,
  registerData,
  handleLoginChange,
  handleRegisterChange,
  handleLogin,
  handleRegister,
  showPassword,
  setShowPassword,
  showConfirmPassword,
  setShowConfirmPassword,
  errors,
  loading,
  serverError,
  success,
}) => (
  <section className="hero-section">
    <div className="hero-gradient-bg">
      <div className="gradient-orb gradient-orb-1" style={{ transform: `translate(${mousePosition.x}px, ${mousePosition.y}px)` }} />
      <div className="gradient-orb gradient-orb-2" style={{ transform: `translate(${-mousePosition.x}px, ${-mousePosition.y}px)` }} />
      <div className="gradient-orb gradient-orb-3" />
    </div>
    <div className="hero-grid-pattern" />

    <div className="hero-container">
      <div className="hero-content">
        <div className="hero-left">
          <div className="hero-badge-premium">
            <Sparkles className="badge-icon" />
            <span>Trusted by 50,000+ Students &amp; Faculty</span>
            <div className="badge-shine" />
          </div>

          <h1 className="hero-title">
            Healthcare
            {' '}<span className="title-gradient">Reimagined</span>
            <br />
            For Modern Living
          </h1>

          <p className="hero-subtitle">
            Experience the future of healthcare with CIT MedConnect+.
            Seamless appointments, instant consultations, and comprehensive
            care—all powered by cutting-edge technology.
          </p>

          <div className="hero-features-mini">
            <div className="mini-feature"><CheckCircle2 className="mini-icon" /><span>Instant Booking</span></div>
            <div className="mini-feature"><CheckCircle2 className="mini-icon" /><span>HIPAA Certified</span></div>
            <div className="mini-feature"><CheckCircle2 className="mini-icon" /><span>24/7 Support</span></div>
          </div>

          <div className="hero-actions">
            <button className="btn-hero-primary" onClick={onGetStarted}>
              <span>Get Started</span>
              <ChevronRight className="btn-icon" />
              <div className="btn-shine" />
            </button>
            <button className="btn-hero-secondary" onClick={() => onTabChange('login')}>
              <Globe className="btn-icon-left" />
              <span>Watch Demo</span>
            </button>
          </div>

          <div className="hero-social-proof">
            <div className="avatar-stack">
              <div className="avatar">JD</div>
              <div className="avatar">SK</div>
              <div className="avatar">AL</div>
              <div className="avatar">+50K</div>
            </div>
            <div className="social-proof-text">
              <div className="rating-stars">
                <Star className="star-filled" /><Star className="star-filled" />
                <Star className="star-filled" /><Star className="star-filled" />
                <Star className="star-filled" />
                <span className="rating-text">4.9/5</span>
              </div>
              <p>from 12,000+ reviews</p>
            </div>
          </div>
        </div>

        <div className="hero-right">
          <div className="hero-visual">
            <AuthCard
              activeTab={activeTab}
              onTabChange={onTabChange}
              loginData={loginData}
              registerData={registerData}
              handleLoginChange={handleLoginChange}
              handleRegisterChange={handleRegisterChange}
              handleLogin={handleLogin}
              handleRegister={handleRegister}
              showPassword={showPassword}
              setShowPassword={setShowPassword}
              showConfirmPassword={showConfirmPassword}
              setShowConfirmPassword={setShowConfirmPassword}
              errors={errors}
              loading={loading}
              serverError={serverError}
              success={success}
            />
          </div>
        </div>
      </div>
    </div>
  </section>
));

HeroSection.displayName = 'HeroSection';

// ============================================
// AUTH CARD
// ============================================

const AuthCard = React.memo(({
  activeTab,
  onTabChange,
  loginData,
  registerData,
  handleLoginChange,
  handleRegisterChange,
  handleLogin,
  handleRegister,
  showPassword,
  setShowPassword,
  showConfirmPassword,
  setShowConfirmPassword,
  errors,
  loading,
  serverError,
  success,
}) => (
  <div className="auth-card">
    <div className="auth-card-glow" />

    <div className="auth-tabs">
      <button
        type="button"
        className={`auth-tab ${activeTab === 'login' ? 'active' : ''}`}
        onClick={() => onTabChange('login')}
      >
        Login
      </button>
      <button
        type="button"
        className={`auth-tab ${activeTab === 'register' ? 'active' : ''}`}
        onClick={() => onTabChange('register')}
      >
        Register
      </button>
    </div>

    <div className={`auth-form ${activeTab === 'login' ? 'active' : ''}`}>
      <div className="auth-header">
        <h2>Welcome Back</h2>
        <p>Sign in to access your account</p>
      </div>

      <form onSubmit={handleLogin} className="auth-form-content">
        {serverError && <div className="error-message">{serverError}</div>}
        {success && <div className="success-message">{success}</div>}

        <div className="form-group">
          <div className="input-wrapper">
            <Mail size={18} className="input-icon" />
            <input
              type="text"
              name="schoolId"
              placeholder="firstname.lastname@cit.edu"
              value={loginData.schoolId}
              onChange={handleLoginChange}
              className={`form-input ${errors.schoolId ? 'error' : ''}`}
              disabled={loading}
              autoComplete="username"
            />
          </div>
          {errors.schoolId && <span className="form-error">{errors.schoolId}</span>}
        </div>

        <div className="form-group">
          <div className="input-wrapper">
            <Lock size={18} className="input-icon" />
            <input
              type={showPassword ? 'text' : 'password'}
              name="password"
              placeholder="Password"
              value={loginData.password}
              onChange={handleLoginChange}
              className={`form-input ${errors.password ? 'error' : ''}`}
              disabled={loading}
              autoComplete="current-password"
            />
            <button
              type="button"
              className="password-toggle"
              onClick={() => setShowPassword((prev) => !prev)}
              tabIndex="-1"
            >
              {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
            </button>
          </div>
          {errors.password && <span className="form-error">{errors.password}</span>}
        </div>

        <div className="form-options">
          <label className="checkbox-label">
            <input
              type="checkbox"
              name="rememberMe"
              checked={loginData.rememberMe}
              onChange={handleLoginChange}
              disabled={loading}
            />
            <span>Remember me</span>
          </label>
        </div>

        <button type="submit" className="btn btn-primary btn-full" disabled={loading}>
          {loading ? (
            <>
              <div className="btn-spinner" />
              <span>Signing in...</span>
            </>
          ) : (
            'Sign In'
          )}
        </button>

        <div className="oauth-divider">
          <span>or</span>
        </div>

        <button
          type="button"
          className="btn btn-oauth btn-github"
          onClick={initiateGitHubLogin}
          disabled={loading}
        >
          <Github size={18} className="oauth-icon" />
          <span>Sign in with GitHub</span>
        </button>
      </form>

      <div className="auth-switch">
        <p>
          Don&apos;t have an account?{' '}
          <button type="button" onClick={() => onTabChange('register')} className="auth-switch-link">
            Sign up
          </button>
        </p>
      </div>
    </div>

    <div className={`auth-form ${activeTab === 'register' ? 'active' : ''}`}>
      <div className="auth-header">
        <h2>Create Account</h2>
        <p>Register to get started</p>
      </div>

      <form onSubmit={handleRegister} className="auth-form-content">
        {serverError && <div className="error-message">{serverError}</div>}
        {success && <div className="success-message">{success}</div>}

        <div className="form-group">
          <div className="input-wrapper">
            <Mail size={18} className="input-icon" />
            <input
              type="text"
              name="schoolId"
              placeholder="School ID (e.g. 21-1234-567)"
              value={registerData.schoolId}
              onChange={handleRegisterChange}
              className={`form-input ${errors.schoolId ? 'error' : ''}`}
              disabled={loading}
            />
          </div>
          {errors.schoolId && <span className="form-error">{errors.schoolId}</span>}
        </div>

        <div className="auth-row">
          <div className="form-group">
            <div className="input-wrapper">
              <Mail size={18} className="input-icon" />
              <input
                type="text"
                name="firstName"
                placeholder="First Name"
                value={registerData.firstName}
                onChange={handleRegisterChange}
                className={`form-input ${errors.firstName ? 'error' : ''}`}
                disabled={loading}
              />
            </div>
            {errors.firstName && <span className="form-error">{errors.firstName}</span>}
          </div>

          <div className="form-group">
            <div className="input-wrapper">
              <Mail size={18} className="input-icon" />
              <input
                type="text"
                name="lastName"
                placeholder="Last Name"
                value={registerData.lastName}
                onChange={handleRegisterChange}
                className={`form-input ${errors.lastName ? 'error' : ''}`}
                disabled={loading}
              />
            </div>
            {errors.lastName && <span className="form-error">{errors.lastName}</span>}
          </div>
        </div>

        <div className="form-group">
          <div className="input-wrapper">
            <Mail size={18} className="input-icon" />
            <input
              type="email"
              name="email"
              placeholder="firstname.lastname@cit.edu"
              value={registerData.email}
              onChange={handleRegisterChange}
              className={`form-input ${errors.email ? 'error' : ''}`}
              disabled={loading}
              autoComplete="email"
            />
          </div>
          {errors.email && <span className="form-error">{errors.email}</span>}
        </div>

        <div className="auth-row">
          <div className="form-group">
            <div className="input-wrapper">
              <Mail size={18} className="input-icon" />
              <input
                type="tel"
                name="phone"
                placeholder="Phone Number"
                value={registerData.phone}
                onChange={handleRegisterChange}
                className={`form-input ${errors.phone ? 'error' : ''}`}
                disabled={loading}
              />
            </div>
            {errors.phone && <span className="form-error">{errors.phone}</span>}
          </div>

          <div className="form-group">
            <select
              name="gender"
              value={registerData.gender}
              onChange={handleRegisterChange}
              className={`auth-select ${errors.gender ? 'error' : ''}`}
              disabled={loading}
            >
              <option value="">Gender</option>
              <option value="Male">Male</option>
              <option value="Female">Female</option>
              <option value="Other">Other / Prefer not to say</option>
            </select>
            {errors.gender && <span className="form-error">{errors.gender}</span>}
          </div>
        </div>

        <div className="form-group">
          <div className="input-wrapper">
            <input
              type="number"
              name="age"
              placeholder="Age"
              value={registerData.age}
              onChange={handleRegisterChange}
              className={`form-input no-icon ${errors.age ? 'error' : ''}`}
              disabled={loading}
              min="15"
              max="100"
            />
          </div>
          {errors.age && <span className="form-error">{errors.age}</span>}
        </div>

        <div className="form-group">
          <div className="input-wrapper">
            <Lock size={18} className="input-icon" />
            <input
              type={showPassword ? 'text' : 'password'}
              name="password"
              placeholder="Password (min 6 characters)"
              value={registerData.password}
              onChange={handleRegisterChange}
              className={`form-input ${errors.password ? 'error' : ''}`}
              disabled={loading}
              autoComplete="new-password"
            />
            <button
              type="button"
              className="password-toggle"
              onClick={() => setShowPassword((prev) => !prev)}
              tabIndex="-1"
            >
              {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
            </button>
          </div>
          {errors.password && <span className="form-error">{errors.password}</span>}
        </div>

        <div className="form-group">
          <div className="input-wrapper">
            <Lock size={18} className="input-icon" />
            <input
              type={showConfirmPassword ? 'text' : 'password'}
              name="confirmPassword"
              placeholder="Confirm Password"
              value={registerData.confirmPassword}
              onChange={handleRegisterChange}
              className={`form-input ${errors.confirmPassword ? 'error' : ''}`}
              disabled={loading}
              autoComplete="new-password"
            />
            <button
              type="button"
              className="password-toggle"
              onClick={() => setShowConfirmPassword((prev) => !prev)}
              tabIndex="-1"
            >
              {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
            </button>
          </div>
          {errors.confirmPassword && <span className="form-error">{errors.confirmPassword}</span>}
        </div>

        <button type="submit" className="btn btn-primary btn-full" disabled={loading}>
          {loading ? (
            <>
              <div className="btn-spinner" />
              <span>Creating account...</span>
            </>
          ) : (
            'Create Account'
          )}
        </button>

        <div className="oauth-divider">
          <span>or</span>
        </div>

        <button
          type="button"
          className="btn btn-oauth btn-github"
          onClick={initiateGitHubLogin}
          disabled={loading}
        >
          <Github size={18} className="oauth-icon" />
          <span>Sign up with GitHub</span>
        </button>
      </form>

      <div className="auth-switch">
        <p>
          Already have an account?{' '}
          <button type="button" onClick={() => onTabChange('login')} className="auth-switch-link">
            Sign in
          </button>
        </p>
      </div>
    </div>

    <div className="hv-center-logo">
      <img src={logo} alt="CIT MedConnect+" onError={e => e.target.style.display = 'none'} />
    </div>
  </div>
));

AuthCard.displayName = 'AuthCard';

// ============================================
// TRUST BADGES
// ============================================

const TrustBadges = React.memo(() => {
  const badges = [
    { icon: Shield, text: "HIPAA Compliant" },
    { icon: Award, text: "ISO 27001 Certified" },
    { icon: CheckCircle2, text: "SOC 2 Type II" },
    { icon: Globe, text: "Global Coverage" }
  ];
  return (
    <section className="trust-badges">
      <div className="trust-container">
        {badges.map((badge, i) => (
          <div key={i} className="trust-badge">
            <badge.icon className="trust-icon" />
            <span>{badge.text}</span>
          </div>
        ))}
      </div>
    </section>
  );
});

TrustBadges.displayName = 'TrustBadges';

// ============================================
// FEATURES SECTION
// ============================================

const FeaturesSection = React.memo(({ features }) => (
  <section className="features-section" id="features">
    <div className="features-container">
      <div className="section-header-premium">
        <h2 className="section-title-premium">
          Everything You Need,
          <span className="title-accent"> Nothing You Don&apos;t</span>
        </h2>
        <p className="section-subtitle-premium">
          Built with cutting-edge technology to deliver unparalleled healthcare experience
        </p>
      </div>
      <div className="features-grid-premium">
        {features.map((feature, i) => (
          <FeatureCard key={i} feature={feature} index={i} />
        ))}
      </div>
    </div>
  </section>
));

FeaturesSection.displayName = 'FeaturesSection';

const FeatureCard = React.memo(({ feature, index }) => {
  const IconComponent = feature.icon;
  return (
    <div className="feature-card-premium" style={{ animationDelay: `${index * 0.1}s` }}>
      <div className="feature-card-inner">
        <div className={`feature-icon-container bg-gradient-to-br ${feature.gradient}`}
             style={{ backgroundColor: `${feature.iconColor}15` }}>
          <IconComponent className="feature-icon-premium" style={{ color: feature.iconColor }} />
          <div className="icon-glow" />
        </div>
        <h3 className="feature-title-premium">{feature.title}</h3>
        <p className="feature-description-premium">{feature.description}</p>
      </div>
    </div>
  );
});

FeatureCard.displayName = 'FeatureCard';

// ============================================
// BENEFITS SECTION
// ============================================

const BenefitsSection = React.memo(({ benefits }) => (
  <section className="benefits-section" id="benefits">
    <div className="benefits-container">
      <div className="benefits-content">
        <div className="benefits-left">
          <h2 className="benefits-title">
            Why Healthcare Professionals
            <span className="benefits-accent"> Choose Us</span>
          </h2>
          <p className="benefits-description">
            Join thousands of healthcare providers and patients who trust
            MedConnect+ for their daily healthcare needs.
          </p>
          <div className="benefits-list">
            {benefits.map((benefit, i) => (
              <div key={i} className="benefit-item">
                <benefit.icon className="benefit-icon" />
                <span>{benefit.text}</span>
              </div>
            ))}
          </div>
        </div>
        <div className="benefits-right">
          <div className="benefits-visual">
            <div className="visual-card visual-card-1">
              <TrendingUp className="visual-icon" />
              <div className="visual-stats">
                <span className="visual-number">245%</span>
                <span className="visual-label">Growth Rate</span>
              </div>
            </div>
            <div className="visual-card visual-card-2">
              <Users className="visual-icon" />
              <div className="visual-stats">
                <span className="visual-number">50K+</span>
                <span className="visual-label">Active Users</span>
              </div>
            </div>
            <div className="visual-card visual-card-3">
              <Heart className="visual-icon pulse-icon" />
              <div className="visual-stats">
                <span className="visual-number">99.8%</span>
                <span className="visual-label">Satisfaction</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
));

BenefitsSection.displayName = 'BenefitsSection';

// ============================================
// STATS SECTION
// ============================================

const StatsSection = React.memo(({ stats }) => (
  <section className="stats-section-premium">
    <div className="stats-container-premium">
      {stats.map((stat, i) => (
        <div key={i} className="stat-card-premium">
          <div className="stat-icon-wrapper">
            <stat.icon className="stat-icon-premium" />
          </div>
          <div className="stat-content">
            <div className="stat-value-premium">{stat.value}</div>
            <div className="stat-label-premium">{stat.label}</div>
            <div className="stat-trend">
              <TrendingUp className="trend-icon" />
              <span>{stat.trend}</span>
            </div>
          </div>
        </div>
      ))}
    </div>
  </section>
));

StatsSection.displayName = 'StatsSection';

// ============================================
// TESTIMONIALS SECTION
// ============================================

const TestimonialsSection = React.memo(({ testimonials }) => (
  <section className="testimonials-section" id="testimonials">
    <div className="testimonials-container">
      <div className="section-header-premium">
        <h2 className="section-title-premium">
          Loved by Healthcare
          <span className="title-accent"> Professionals</span>
        </h2>
      </div>
      <div className="testimonials-grid">
        {testimonials.map((t, i) => (
          <div key={i} className="testimonial-card">
            <div className="testimonial-rating">
              {[...Array(t.rating)].map((_, j) => <Star key={j} className="star-filled" />)}
            </div>
            <p className="testimonial-content">&ldquo;{t.content}&rdquo;</p>
            <div className="testimonial-author">
              <div className="author-avatar">{t.avatar}</div>
              <div className="author-info">
                <div className="author-name">{t.name}</div>
                <div className="author-role">{t.role}</div>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  </section>
));

TestimonialsSection.displayName = 'TestimonialsSection';

// ============================================
// CTA SECTION
// ============================================

const CTASection = React.memo(({ onGetStarted }) => (
  <section className="cta-section">
    <div className="cta-container">
      <div className="cta-content">
        <h2 className="cta-title">Ready to Transform Your Healthcare?</h2>
        <p className="cta-description">
          Join 50,000+ users who are already experiencing the future of healthcare
        </p>
        <div className="cta-actions">
          <button className="btn-cta-primary" onClick={onGetStarted}>
            Get Started Free
            <ChevronRight className="btn-icon" />
          </button>
          <button className="btn-cta-secondary">Schedule a Demo</button>
        </div>
      </div>
    </div>
  </section>
));

CTASection.displayName = 'CTASection';

// ============================================
// FOOTER
// ============================================

const Footer = React.memo(({ links }) => {
  const currentYear = new Date().getFullYear();
  return (
    <footer className="footer-premium" id="contact">
      <div className="footer-container-premium">
        <div className="footer-top">
          <div className="footer-brand-section">
            <div className="footer-logo-premium">
              <Settings className="footer-icon" />
              <span>CIT MedConnect<span className="brand-plus">+</span></span>
            </div>
            <p className="footer-tagline">Empowering healthcare through innovation and technology</p>
            <div className="footer-social">
              <button className="social-btn">X</button>
              <button className="social-btn">Li</button>
              <button className="social-btn">Fb</button>
              <button className="social-btn">Ig</button>
            </div>
          </div>
          <div className="footer-links-grid">
            {[
              { title: "Product", items: links.product },
              { title: "Company", items: links.company },
              { title: "Resources", items: links.resources },
              { title: "Legal", items: links.legal },
            ].map(col => (
              <div key={col.title} className="footer-links-column">
                <h4>{col.title}</h4>
                <ul>
                  {col.items.map((link, i) => (
                    <li key={i}><a href={link.href}>{link.label}</a></li>
                  ))}
                </ul>
              </div>
            ))}
          </div>
        </div>
        <div className="footer-bottom">
          <p>&copy; {currentYear} CIT MedConnect+. All rights reserved.</p>
          <div className="footer-badges">
            <span className="footer-badge">HIPAA Compliant</span>
            <span className="footer-badge">SOC 2 Certified</span>
          </div>
        </div>
      </div>
    </footer>
  );
});

Footer.displayName = 'Footer';

export default Landing;
