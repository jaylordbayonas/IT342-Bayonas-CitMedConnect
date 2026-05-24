import React, { createContext, useContext, useReducer, useEffect, useCallback, useMemo, useRef } from 'react';
import PropTypes from 'prop-types';
import useAuth from '../hooks/useAuth';
import { notificationService } from '../services/notificationService';

const ACTIONS = {
  SET_LOADING: 'SET_LOADING',
  SET_NOTIFICATIONS: 'SET_NOTIFICATIONS',
  UPDATE_NOTIFICATION: 'UPDATE_NOTIFICATION',
  REMOVE_NOTIFICATION: 'REMOVE_NOTIFICATION',
  SET_ERROR: 'SET_ERROR',
  CLEAR_ERROR: 'CLEAR_ERROR'
};

const initialState = {
  userNotifications: [],
  loading: false,
  error: null,
  unreadCount: 0
};

// Backend returns notificationType (not type); normalize to a unified `type` field.
const normalizeNotification = (item, index = 0) => ({
  notificationId: item.notificationId || item.id || `notif-${Date.now()}-${index}`,
  title: item.title || 'Notification',
  message: item.message || '',
  type: item.notificationType || item.type || 'info',
  isRead: item.isRead === true,
  isGlobal: item.isGlobal === true,
  schoolId: item.schoolId || null,
  createdAt: item.createdAt || new Date().toISOString(),
});

const reducer = (state, action) => {
  switch (action.type) {
    case ACTIONS.SET_LOADING:
      return { ...state, loading: action.payload };

    case ACTIONS.SET_NOTIFICATIONS:
      return {
        ...state,
        userNotifications: action.payload,
        unreadCount: action.payload.filter(n => !n.isRead).length
      };

    case ACTIONS.UPDATE_NOTIFICATION: {
      const updated = state.userNotifications.map(n =>
        n.notificationId === action.payload.notificationId ? { ...n, ...action.payload } : n
      );
      return {
        ...state,
        userNotifications: updated,
        unreadCount: updated.filter(n => !n.isRead).length
      };
    }

    case ACTIONS.REMOVE_NOTIFICATION: {
      const filtered = state.userNotifications.filter(n => n.notificationId !== action.payload);
      return {
        ...state,
        userNotifications: filtered,
        unreadCount: filtered.filter(n => !n.isRead).length
      };
    }

    case ACTIONS.SET_ERROR:
      return { ...state, error: action.payload, loading: false };

    case ACTIONS.CLEAR_ERROR:
      return { ...state, error: null };

    default:
      return state;
  }
};

const NotificationContext = createContext();

export const NotificationProvider = ({ children }) => {
  const [state, dispatch] = useReducer(reducer, initialState);
  const { user } = useAuth();
  const schoolId = user?.schoolId;
  const userRole = user?.role;
  const intervalRef = useRef(null);
  const initialLoadDone = useRef(false);

  const setLoading = useCallback(
    (loading) => dispatch({ type: ACTIONS.SET_LOADING, payload: loading }),
    []
  );
  const setError = useCallback(
    (error) => dispatch({ type: ACTIONS.SET_ERROR, payload: error }),
    []
  );
  const clearError = useCallback(() => dispatch({ type: ACTIONS.CLEAR_ERROR }), []);

  const fetchUserNotifications = useCallback(
    async (showLoading = false) => {
      if (!schoolId || !userRole) return;

      try {
        if (showLoading) setLoading(true);
        clearError();

        const result = await notificationService.fetchUserNotifications(schoolId, userRole);

        if (result.success) {
          const normalized = (result.data || []).map((item, i) => normalizeNotification(item, i));
          dispatch({ type: ACTIONS.SET_NOTIFICATIONS, payload: normalized });
        } else {
          setError(result.error || 'Failed to fetch notifications');
        }
      } catch {
        setError('Failed to fetch notifications');
      } finally {
        if (showLoading) setLoading(false);
      }
    },
    [schoolId, userRole, setLoading, setError, clearError]
  );

  // Initial fetch (with loading indicator) + silent 30-second background poll.
  useEffect(() => {
    if (!schoolId || !userRole) {
      initialLoadDone.current = false;
      return;
    }

    initialLoadDone.current = false;
    fetchUserNotifications(true).then(() => {
      initialLoadDone.current = true;
    });

    intervalRef.current = setInterval(() => fetchUserNotifications(false), 30000);
    return () => clearInterval(intervalRef.current);
  }, [schoolId, userRole, fetchUserNotifications]);

  const markAsRead = useCallback(async (notificationId) => {
    try {
      const result = await notificationService.markAsRead(notificationId);
      if (result.success) {
        dispatch({ type: ACTIONS.UPDATE_NOTIFICATION, payload: { notificationId, isRead: true } });
        return { success: true };
      }
      return { success: false, error: result.error || 'Failed to mark as read' };
    } catch {
      return { success: false, error: 'Failed to mark as read' };
    }
  }, []);

  const deleteNotification = useCallback(async (notificationId) => {
    try {
      const result = await notificationService.deleteNotification(notificationId);
      if (result.success) {
        dispatch({ type: ACTIONS.REMOVE_NOTIFICATION, payload: notificationId });
        return { success: true };
      }
      return { success: false, error: result.error || 'Failed to delete notification' };
    } catch {
      return { success: false, error: 'Failed to delete notification' };
    }
  }, []);

  const markAllAsRead = useCallback(async () => {
    const unread = state.userNotifications.filter(n => !n.isRead);
    if (unread.length > 0) {
      await Promise.all(unread.map(n => notificationService.markAsRead(n.notificationId)));
      await fetchUserNotifications(false);
    }
    return { success: true };
  }, [state.userNotifications, fetchUserNotifications]);

  const clearAll = useCallback(async () => {
    await Promise.all(
      state.userNotifications.map(n => notificationService.deleteNotification(n.notificationId))
    );
    dispatch({ type: ACTIONS.SET_NOTIFICATIONS, payload: [] });
    return { success: true };
  }, [state.userNotifications]);

  const sendNotificationToAllStudents = useCallback(async (title, message, type = 'info') => {
    try {
      return await notificationService.sendNotificationToAllStudents(title, message);
    } catch {
      return { success: false, error: 'Failed to send notification to students' };
    }
  }, []);

  const sendNotificationToEveryone = useCallback(async (title, message, type = 'info') => {
    try {
      return await notificationService.sendNotificationToEveryone(title, message);
    } catch {
      return { success: false, error: 'Failed to send notification to everyone' };
    }
  }, []);

  const unreadNotifications = useMemo(
    () => state.userNotifications.filter(n => !n.isRead),
    [state.userNotifications]
  );
  const readNotifications = useMemo(
    () => state.userNotifications.filter(n => n.isRead),
    [state.userNotifications]
  );

  const value = useMemo(() => ({
    userNotifications: state.userNotifications,
    unreadNotifications,
    readNotifications,
    unreadCount: state.unreadCount,
    loading: state.loading,
    error: state.error,
    fetchUserNotifications: () => fetchUserNotifications(true),
    markAsRead,
    deleteNotification,
    markAllAsRead,
    clearAll,
    sendNotificationToAllStudents,
    sendNotificationToEveryone,
    clearError
  }), [
    state.userNotifications,
    state.unreadCount,
    state.loading,
    state.error,
    unreadNotifications,
    readNotifications,
    fetchUserNotifications,
    markAsRead,
    deleteNotification,
    markAllAsRead,
    clearAll,
    sendNotificationToAllStudents,
    sendNotificationToEveryone,
    clearError
  ]);

  return (
    <NotificationContext.Provider value={value}>
      {children}
    </NotificationContext.Provider>
  );
};

export const useNotifications = () => {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error('useNotifications must be used within a NotificationProvider');
  }
  return context;
};

export default NotificationContext;

NotificationProvider.propTypes = {
  children: PropTypes.node.isRequired,
};
