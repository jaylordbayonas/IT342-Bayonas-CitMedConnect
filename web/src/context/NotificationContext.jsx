import React, { createContext, useContext, useReducer, useEffect, useCallback, useMemo, useRef } from 'react';
import PropTypes from 'prop-types';
import useAuth from '../hooks/useAuth';

// Action types
const NOTIFICATION_ACTIONS = {
  SET_LOADING: 'SET_LOADING',
  SET_NOTIFICATIONS: 'SET_NOTIFICATIONS',
  ADD_NOTIFICATION: 'ADD_NOTIFICATION',
  UPDATE_NOTIFICATION: 'UPDATE_NOTIFICATION',
  REMOVE_NOTIFICATION: 'REMOVE_NOTIFICATION',
  SET_ERROR: 'SET_ERROR',
  CLEAR_ERROR: 'CLEAR_ERROR'
};

// Initial state
const initialState = {
  userNotifications: [],
  loading: false,
  error: null,
  unreadCount: 0
};

const STORAGE_KEY = 'citmedconnect_notifications';

const loadStoredNotifications = () => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    const parsed = raw ? JSON.parse(raw) : [];
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
};

const saveStoredNotifications = (notifications) => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(notifications));
};

const normalizeNotification = (notification, index = 0) => ({
  notificationId: notification.notificationId || notification.id || `notif-${Date.now()}-${index}`,
  title: notification.title || 'Notification',
  message: notification.message || '',
  type: notification.type || 'info',
  recipient: notification.recipient || notification.target || 'everyone',
  schoolId: notification.schoolId || null,
  role: notification.role || null,
  isRead: notification.isRead === true,
  createdAt: notification.createdAt || new Date().toISOString(),
  ...notification,
});

const filterNotificationsForUser = (notifications, schoolId, userRole) => {
  const normalizedRole = (userRole || '').toLowerCase();

  return notifications.filter((notification) => {
    const recipient = (notification.recipient || notification.target || 'everyone').toLowerCase();

    if (notification.schoolId && schoolId && notification.schoolId === schoolId) {
      return true;
    }

    if (recipient === 'everyone') {
      return true;
    }

    if (normalizedRole === 'staff') {
      return recipient === 'staff' || recipient === 'students';
    }

    return recipient === 'students';
  });
};

// Reducer
const notificationReducer = (state, action) => {
  switch (action.type) {
    case NOTIFICATION_ACTIONS.SET_LOADING:
      return { ...state, loading: action.payload };
    
    case NOTIFICATION_ACTIONS.SET_NOTIFICATIONS:
      return { 
        ...state, 
        userNotifications: action.payload,
        unreadCount: action.payload.filter(n => !n.isRead).length
      };
    
    case NOTIFICATION_ACTIONS.ADD_NOTIFICATION:
      return {
        ...state,
        userNotifications: [action.payload, ...state.userNotifications],
        unreadCount: action.payload.isRead ? state.unreadCount : state.unreadCount + 1
      };
    
    case NOTIFICATION_ACTIONS.UPDATE_NOTIFICATION: {
      const updatedNotifications = state.userNotifications.map(notification => {
        if (notification.notificationId === action.payload.notificationId) {
          // Merge the existing notification with the updated data
          const updatedNotification = { ...notification, ...action.payload };
          console.log('Updating notification:', {
            original: notification,
            update: action.payload,
            result: updatedNotification
          });
          return updatedNotification;
        }
        return notification;
      });
      const updatedUnreadCount = updatedNotifications.filter(n => !n.isRead).length;
      console.log('Updated notifications count:', {
        total: updatedNotifications.length,
        unread: updatedUnreadCount,
        read: updatedNotifications.length - updatedUnreadCount
      });
      return {
        ...state,
        userNotifications: updatedNotifications,
        unreadCount: updatedUnreadCount
      };
    }
    
    case NOTIFICATION_ACTIONS.REMOVE_NOTIFICATION: {
      const filteredNotifications = state.userNotifications.filter(
        notification => notification.notificationId !== action.payload
      );
      const removedUnreadCount = filteredNotifications.filter(n => !n.isRead).length;
      return {
        ...state,
        userNotifications: filteredNotifications,
        unreadCount: removedUnreadCount
      };
    }
    
    case NOTIFICATION_ACTIONS.SET_ERROR:
      return { ...state, error: action.payload, loading: false };
    
    case NOTIFICATION_ACTIONS.CLEAR_ERROR:
      return { ...state, error: null };
    
    default:
      return state;
  }
};

// Context
const NotificationContext = createContext();

// Provider component
export const NotificationProvider = ({ children }) => {
  const [state, dispatch] = useReducer(notificationReducer, initialState);
  const { user } = useAuth();
  const schoolId = user?.schoolId;
  const userRole = user?.role;
  
  // Ref to track if we've already fetched for current user
  const lastFetchedUser = useRef('');

  // Set loading state
  const setLoading = (loading) => {
    dispatch({ type: NOTIFICATION_ACTIONS.SET_LOADING, payload: loading });
  };

  // Set error state
  const setError = (error) => {
    dispatch({ type: NOTIFICATION_ACTIONS.SET_ERROR, payload: error });
  };

  // Clear error state
  const clearError = () => {
    dispatch({ type: NOTIFICATION_ACTIONS.CLEAR_ERROR });
  };

  // Fetch user notifications
  const fetchUserNotifications = useCallback(async () => {
    console.log('fetchUserNotifications called:', { schoolId, userRole });
    
    if (!schoolId || !userRole) {
      console.log('Missing required data:', { schoolId, userRole });
      return;
    }

    try {
      setLoading(true);
      clearError();

      const storedNotifications = loadStoredNotifications();
      const visibleNotifications = filterNotificationsForUser(
        storedNotifications.map((notification, index) => normalizeNotification(notification, index)),
        schoolId,
        userRole
      );

      dispatch({
        type: NOTIFICATION_ACTIONS.SET_NOTIFICATIONS,
        payload: visibleNotifications
      });
    } catch (error) {
      console.error('Error fetching notifications:', error);
      setError('Failed to fetch notifications');
    } finally {
      setLoading(false);
    }
  }, [schoolId, userRole]);

  // Mark notification as read
  const markAsRead = async (notificationId) => {
    try {
      clearError();

      const storedNotifications = loadStoredNotifications();
      const updatedStoredNotifications = storedNotifications.map((notification) =>
        notification.notificationId === notificationId
          ? { ...notification, isRead: true }
          : notification
      );
      saveStoredNotifications(updatedStoredNotifications);

      const updatedNotification = updatedStoredNotifications.find(
        (notification) => notification.notificationId === notificationId
      ) || { notificationId, isRead: true };
      
      dispatch({
        type: NOTIFICATION_ACTIONS.UPDATE_NOTIFICATION,
        payload: updatedNotification
      });

      return { success: true };
    } catch (error) {
      console.error('Error marking notification as read:', error);
      return { success: false, error: 'Failed to mark notification as read' };
    }
  };

  // Delete notification
  const deleteNotification = async (notificationId) => {
    try {
      clearError();

      const storedNotifications = loadStoredNotifications().filter(
        (notification) => notification.notificationId !== notificationId
      );
      saveStoredNotifications(storedNotifications);

      dispatch({
        type: NOTIFICATION_ACTIONS.REMOVE_NOTIFICATION,
        payload: notificationId
      });

      return { success: true };
    } catch (error) {
      console.error('Error deleting notification:', error);
      setError(error.response?.data?.message || 'Failed to delete notification');
      return { success: false, error: error.response?.data?.message };
    }
  };

  // Mark all notifications as read
  const markAllAsRead = async () => {
    try {
      clearError();
      
      const updatedNotifications = state.userNotifications.map((notification) => ({
        ...notification,
        isRead: true
      }));

      const storedNotifications = loadStoredNotifications().map((notification) => ({
        ...notification,
        isRead: true
      }));
      saveStoredNotifications(storedNotifications);

      dispatch({
        type: NOTIFICATION_ACTIONS.SET_NOTIFICATIONS,
        payload: updatedNotifications
      });

      return { success: true };
    } catch (error) {
      console.error('Error marking all notifications as read:', error);
      setError(error.response?.data?.message || 'Failed to mark all notifications as read');
      return { success: false, error: error.response?.data?.message };
    }
  };

  // Clear all notifications
  const clearAll = async () => {
    try {
      clearError();
      saveStoredNotifications(
        loadStoredNotifications().filter((notification) => {
          const recipient = (notification.recipient || notification.target || 'everyone').toLowerCase();
          if (notification.schoolId && schoolId && notification.schoolId === schoolId) {
            return false;
          }
          if (recipient === 'everyone') {
            return false;
          }
          if ((userRole || '').toLowerCase() === 'staff') {
            return recipient !== 'staff' && recipient !== 'students';
          }
          return recipient !== 'students';
        })
      );

      dispatch({
        type: NOTIFICATION_ACTIONS.SET_NOTIFICATIONS,
        payload: []
      });

      return { success: true };
    } catch (error) {
      console.error('Error clearing all notifications:', error);
      setError(error.response?.data?.message || 'Failed to clear all notifications');
      return { success: false, error: error.response?.data?.message };
    }
  };

  // Send notification to all students (Staff only)
  const sendNotificationToAllStudents = async (title, message, type = 'info') => {
    try {
      clearError();

      const notification = normalizeNotification({
        title,
        message,
        type,
        recipient: 'students',
        role: 'student'
      });

      const storedNotifications = [notification, ...loadStoredNotifications()];
      saveStoredNotifications(storedNotifications);

      if ((userRole || '').toLowerCase() === 'staff') {
        dispatch({ type: NOTIFICATION_ACTIONS.ADD_NOTIFICATION, payload: notification });
      }

      return { success: true, data: notification };
    } catch (error) {
      console.error('Error sending notification to students:', error);
      setError('Failed to send notification to students');
      return { success: false, error: 'Failed to send notification to students' };
    }
  };

  // Send notification to everyone (Staff only)
  const sendNotificationToEveryone = async (title, message, type = 'info') => {
    try {
      clearError();

      const notification = normalizeNotification({
        title,
        message,
        type,
        recipient: 'everyone'
      });

      const storedNotifications = [notification, ...loadStoredNotifications()];
      saveStoredNotifications(storedNotifications);

      if (user) {
        dispatch({ type: NOTIFICATION_ACTIONS.ADD_NOTIFICATION, payload: notification });
      }

      return { success: true, data: notification };
    } catch (error) {
      console.error('Error sending notification to everyone:', error);
      setError('Failed to send notification to everyone');
      return { success: false, error: 'Failed to send notification to everyone' };
    }
  };

  // Fetch notifications when user changes
  useEffect(() => {
    const currentUser = `${schoolId}-${userRole}`;

    // Only fetch if user has changed and we haven't already fetched for this user
    if (schoolId && userRole && !state.loading && currentUser !== lastFetchedUser.current) {
      console.log('User changed, fetching notifications for:', currentUser);
      lastFetchedUser.current = currentUser;
      fetchUserNotifications();
    }
  }, [schoolId, userRole, fetchUserNotifications, state.loading]);

  // Computed values
  const unreadNotifications = state.userNotifications.filter(n => !n.isRead);
  const readNotifications = state.userNotifications.filter(n => n.isRead);

  const value = useMemo(() => ({
    // State
    userNotifications: state.userNotifications,
    unreadNotifications,
    readNotifications,
    unreadCount: state.unreadCount,
    loading: state.loading,
    error: state.error,

    // Actions
    fetchUserNotifications,
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

// Hook to use the notification context
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