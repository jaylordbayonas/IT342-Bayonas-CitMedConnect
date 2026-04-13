import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import {
  getAuthorizationCode,
  validateOAuthState,
  authenticateWithGithubToken,
} from '../services/github-oauth-service';
import '../pages/OAuth2Callback.css';

const OAuth2Callback = () => {
  const navigate = useNavigate();
  const { login: authLogin } = useAuth();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const handleCallback = async () => {
      try {
        // Get authorization code from URL
        const code = getAuthorizationCode();
        const state = new URLSearchParams(window.location.search).get('state');

        if (!code) {
          throw new Error('No authorization code received from GitHub');
        }

        // Validate state
        if (!validateOAuthState(state)) {
          throw new Error('Invalid OAuth state - potential CSRF attack');
        }

        // Exchange code for token (backend endpoint)
        const tokenResponse = await fetch('/api/auth/oauth2/github/exchange-code', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ code }),
        });

        if (!tokenResponse.ok) {
          throw new Error('Failed to exchange authorization code');
        }

        const { accessToken } = await tokenResponse.json();

        // Authenticate with GitHub token
        const authResponse = await authenticateWithGithubToken(accessToken);

        if (!authResponse.success) {
          throw new Error(authResponse.message || 'GitHub authentication failed');
        }

        // Store user and token
        localStorage.setItem('user', JSON.stringify(authResponse.user));
        localStorage.setItem('authToken', authResponse.token);

        // Optional: Show notification
        if (authResponse.isNewUser) {
          console.log('New user created via GitHub OAuth');
        }

        // Redirect to dashboard
        setTimeout(() => {
          navigate('/dashboard', { replace: true });
        }, 500);
      } catch (err) {
        console.error('OAuth2 callback error:', err);
        setError(err.message || 'Authentication failed. Redirecting to login...');
        
        // Redirect back to landing after 3 seconds
        setTimeout(() => {
          navigate('/', { replace: true });
        }, 3000);
      } finally {
        setLoading(false);
      }
    };

    handleCallback();
  }, [navigate, authLogin]);

  return (
    <div className="oauth2-callback-container">
      {loading ? (
        <div className="callback-loading">
          <div className="spinner"></div>
          <p>Completing GitHub login...</p>
        </div>
      ) : error ? (
        <div className="callback-error">
          <p className="error-message">{error}</p>
          <p className="redirect-message">Redirecting to login...</p>
        </div>
      ) : null}
    </div>
  );
};

export default OAuth2Callback;
