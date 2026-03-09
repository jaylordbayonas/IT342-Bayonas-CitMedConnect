import React, { useState, useCallback, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Settings, Stethoscope, Calendar, Users, Shield, Clock,
  ChevronRight, Sparkles, Activity, Heart, Zap, Award,
  CheckCircle2, Star, TrendingUp, Globe
} from 'lucide-react';
import './Landing.css';

const logo = '/images/logo.jpg';

const Landing = () => {
  const navigate = useNavigate();
  const [isScrolled, setIsScrolled] = useState(false);
  const [mousePosition, setMousePosition] = useState({ x: 0, y: 0 });

  useEffect(() => {
    let ticking = false;
    const handleScroll = () => {
      if (!ticking) {
        window.requestAnimationFrame(() => {
          setIsScrolled(window.scrollY > 20);
          ticking = false;
        });
        ticking = true;
      }
    };
    window.addEventListener('scroll', handleScroll, { passive: true });
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  useEffect(() => {
    const handleMouseMove = (e) => {
      setMousePosition({
        x: (e.clientX / window.innerWidth) * 20,
        y: (e.clientY / window.innerHeight) * 20
      });
    };
    window.addEventListener('mousemove', handleMouseMove);
    return () => window.removeEventListener('mousemove', handleMouseMove);
  }, []);

  const handleNavLinkClick = useCallback((e, section) => {
    e.preventDefault();
    const element = document.querySelector(`#${section}`);
    if (element) {
      const offsetPosition = element.getBoundingClientRect().top + window.pageYOffset - 100;
      window.scrollTo({ top: offsetPosition, behavior: 'smooth' });
    }
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

  return (
    <div className="landing-page">
      <Navigation
        onNavLinkClick={handleNavLinkClick}
        isScrolled={isScrolled}
        onLogin={() => navigate('/login')}
        onRegister={() => navigate('/register')}
      />

      <HeroSection
        mousePosition={mousePosition}
        onGetStarted={() => navigate('/register')}
        onLogin={() => navigate('/login')}
      />

      <TrustBadges />
      <FeaturesSection features={features} />
      <BenefitsSection benefits={benefits} />
      <StatsSection stats={stats} />
      <TestimonialsSection testimonials={testimonials} />
      <CTASection onGetStarted={() => navigate('/register')} />
      <Footer links={footerLinks} />
    </div>
  );
};

// ============================================
// NAVIGATION
// ============================================

const Navigation = React.memo(({ onNavLinkClick, isScrolled, onLogin, onRegister }) => (
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

      <div className="nav-right">
        <button onClick={onLogin} className="nav-btn nav-btn-ghost">Login</button>
        <button onClick={onRegister} className="nav-btn nav-btn-primary">Register</button>
      </div>
    </div>
  </nav>
));

Navigation.displayName = 'Navigation';

// ============================================
// HERO SECTION
// ============================================

const HeroSection = React.memo(({ mousePosition, onGetStarted, onLogin }) => (
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
            <span className="title-gradient"> Reimagined</span>
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
            <button className="btn-hero-secondary" onClick={onLogin}>
              <Globe className="btn-icon-left" />
              <span>Sign In</span>
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

        {/* Decorative visual instead of auth form */}
        <div className="hero-right">
          <HeroVisual />
        </div>
      </div>
    </div>
  </section>
));

HeroSection.displayName = 'HeroSection';

// ============================================
// HERO VISUAL (replaces auth card)
// ============================================

const HeroVisual = React.memo(() => (
  <div className="hero-visual">
    <div className="hv-card hv-card-1">
      <div className="hv-icon-wrap"><Calendar size={28} /></div>
      <div className="hv-text">
        <span className="hv-num">24/7</span>
        <span className="hv-label">Instant Booking</span>
      </div>
    </div>

    <div className="hv-card hv-card-2">
      <div className="hv-icon-wrap"><Users size={28} /></div>
      <div className="hv-text">
        <span className="hv-num">50K+</span>
        <span className="hv-label">Active Users</span>
      </div>
    </div>

    <div className="hv-card hv-card-3">
      <div className="hv-icon-wrap"><Heart size={28} /></div>
      <div className="hv-text">
        <span className="hv-num">99.8%</span>
        <span className="hv-label">Satisfaction</span>
      </div>
    </div>

    <div className="hv-card hv-card-4">
      <div className="hv-icon-wrap"><Shield size={28} /></div>
      <div className="hv-text">
        <span className="hv-num">HIPAA</span>
        <span className="hv-label">Compliant</span>
      </div>
    </div>

    <div className="hv-center-logo">
      <img src={logo} alt="CIT MedConnect+" onError={e => e.target.style.display = 'none'} />
    </div>
  </div>
));

HeroVisual.displayName = 'HeroVisual';

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
