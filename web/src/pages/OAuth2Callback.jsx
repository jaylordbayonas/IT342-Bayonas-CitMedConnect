import React, { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  getAuthorizationCode,
  validateOAuthState,
  exchangeCodeForToken,
  authenticateWithGithubToken,
} from '../services/github-oauth-service';
import '../pages/OAuth2Callback.css';

const OAuth2Callback = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const hasProcessedCallback = useRef(false);

  useEffect(() => {
    if (hasProcessedCallback.current) {
      return;
    }
    hasProcessedCallback.current = true;

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

        const accessToken = await exchangeCodeForToken(code);

        // Authenticate with GitHub token
        const authResponse = await authenticateWithGithubToken(accessToken);

        if (!authResponse.success) {
          throw new Error(authResponse.message || 'GitHub authentication failed');
        }

        // Store user and token using keys expected by AuthContext
        const userToSave = {
          ...authResponse.user,
          isAuthenticated: true,
        };

        // Session expiry: 2 hours from now
        const expiry = new Date(Date.now() + 2 * 60 * 60 * 1000);

        localStorage.setItem('medconnect_user', JSON.stringify(userToSave));
        localStorage.setItem('medconnect_session_expiry', expiry.toISOString());
        // Keep authToken as well for API calls
        localStorage.setItem('authToken', authResponse.token);

        // Optional: Show notification
        if (authResponse.isNewUser) {
          console.log('New user created via GitHub OAuth');
        }

        // Redirect to dashboard after storing auth state
        setTimeout(() => {
          window.location.replace('/dashboard');
        }, 500);
      } catch (err) {
        console.error('OAuth2 callback error:', err);
        setError(err?.message || 'Authentication failed. Redirecting to login...');
        
        // Redirect back to landing after 3 seconds
        setTimeout(() => {
          navigate('/', { replace: true });
        }, 3000);
      } finally {
        setLoading(false);
      }
    };

    handleCallback();
  }, [navigate]);

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
