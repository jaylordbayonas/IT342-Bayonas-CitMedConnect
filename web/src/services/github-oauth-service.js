/**
 * GitHub OAuth2 Service
 * Handles OAuth2 flow with GitHub
 */

const GITHUB_CLIENT_ID =
  import.meta.env.VITE_GITHUB_CLIENT_ID ||
  'Ov23liMMOX0oCIllvWnK';
const GITHUB_AUTH_URL = 'https://github.com/login/oauth/authorize';
const REDIRECT_URI = import.meta.env.VITE_GITHUB_REDIRECT_URI;

/**
 * Initiate GitHub OAuth2 login
 * Redirects user to GitHub authorization page
 */
export const initiateGitHubLogin = () => {
  const scope = 'user:email,read:user';
  const state = generateRandomState();

  // Store state in both locations so the callback can survive browser quirks.
  sessionStorage.setItem('oauth_state', state);
  localStorage.setItem('oauth_state', state);

  const params = new URLSearchParams({
    client_id: GITHUB_CLIENT_ID,
    scope,
    state,
  });

  // Send redirect_uri only when explicitly provided to prevent mismatches.
  if (REDIRECT_URI) {
    params.set('redirect_uri', REDIRECT_URI);
  }

  const authUrl = `${GITHUB_AUTH_URL}?${params.toString()}`;

  window.location.href = authUrl;
};

/**
 * Exchange GitHub authorization code for access token
 * @param {string} code - Authorization code from GitHub
 * @returns {Promise<string>} Access token
 */
export const exchangeCodeForToken = async (code) => {
  try {
    const response = await fetch('/api/auth/oauth2/github/exchange-code', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ code, redirectUri: REDIRECT_URI || null }),
    });

    if (!response.ok) {
      let backendError = 'Failed to exchange code for token';
      try {
        const errBody = await response.json();
        if (errBody?.error) {
          backendError = errBody.error;
        }
      } catch {
        // Keep fallback message when backend response is not JSON.
      }
      throw new Error(backendError);
    }

    const data = await response.json();
    return data.accessToken;
  } catch (error) {
    console.error('Error exchanging code:', error);
    throw error;
  }
};

/**
 * Authenticate user with GitHub access token
 * @param {string} accessToken - GitHub access token
 * @returns {Promise<Object>} User data and auth token
 */
export const authenticateWithGithubToken = async (accessToken) => {
  try {
    const response = await fetch('/api/auth/oauth2/github/callback', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ accessToken }),
    });

    if (!response.ok) {
      throw new Error('GitHub authentication failed');
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Error authenticating with GitHub token:', error);
    throw error;
  }
};

/**
 * Generate random state for OAuth security
 * @returns {string} Random state string
 */
function generateRandomState() {
  const array = new Uint8Array(32);
  crypto.getRandomValues(array);
  return Array.from(array, byte => byte.toString(16).padStart(2, '0')).join('');
}

/**
 * Validate OAuth state
 * @param {string} state - State to validate
 * @returns {boolean} True if state is valid
 */
export const validateOAuthState = (state) => {
  const storedState = sessionStorage.getItem('oauth_state') || localStorage.getItem('oauth_state');
  sessionStorage.removeItem('oauth_state');
  localStorage.removeItem('oauth_state');
  return state === storedState;
};

/**
 * Get authorization code from URL
 * @returns {string|null} Authorization code or null
 */
export const getAuthorizationCode = () => {
  const params = new URLSearchParams(window.location.search);
  return params.get('code');
};

export default {
  initiateGitHubLogin,
  exchangeCodeForToken,
  authenticateWithGithubToken,
  validateOAuthState,
  getAuthorizationCode,
};
