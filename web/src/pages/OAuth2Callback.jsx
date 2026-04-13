import React, { useEffect, useState } from 'react';
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

        const accessToken = await exchangeCodeForToken(code);

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
          window.location.replace('/dashboard');
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
