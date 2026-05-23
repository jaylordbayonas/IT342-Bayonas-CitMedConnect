import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const getAuthHeaders = () => {
  const headers = { 'Content-Type': 'application/json' };
  const token = localStorage.getItem('token');
  if (token) headers['Authorization'] = `Bearer ${token}`;
  return headers;
};

export const notificationService = {
  fetchUserNotifications: async (schoolId, userRole) => {
    try {
      const response = await axios.get(
        `${API_URL}/notifications/user/${encodeURIComponent(schoolId)}/role/${encodeURIComponent(userRole)}`,
        { headers: getAuthHeaders() }
      );
      return { success: true, data: Array.isArray(response.data) ? response.data : [] };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.error || error.message || 'Failed to fetch notifications',
        data: []
      };
    }
  },

  markAsRead: async (notificationId) => {
    try {
      const response = await axios.put(
        `${API_URL}/notifications/${notificationId}/read`,
        {},
        { headers: getAuthHeaders() }
      );
      return { success: true, data: response.data };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.error || error.message || 'Failed to mark as read'
      };
    }
  },

  deleteNotification: async (notificationId) => {
    try {
      await axios.delete(
        `${API_URL}/notifications/${notificationId}`,
        { headers: getAuthHeaders() }
      );
      return { success: true };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.error || error.message || 'Failed to delete notification'
      };
    }
  },

  sendNotificationToAllStudents: async (title, message) => {
    try {
      const response = await axios.post(
        `${API_URL}/notifications/broadcast/students`,
        { title, message },
        { headers: getAuthHeaders() }
      );
      return { success: true, data: response.data };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.error || error.message || 'Failed to send notification to students'
      };
    }
  },

  sendNotificationToEveryone: async (title, message) => {
    try {
      const response = await axios.post(
        `${API_URL}/notifications/broadcast/all`,
        { title, message },
        { headers: getAuthHeaders() }
      );
      return { success: true, data: response.data };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.error || error.message || 'Failed to send notification to everyone'
      };
    }
  },

  sendNotificationToUser: async (schoolId, title, message, type = 'info') => {
    try {
      const response = await axios.post(
        `${API_URL}/notifications/send`,
        { recipientId: schoolId, title, message, type },
        { headers: getAuthHeaders() }
      );
      return { success: true, data: response.data };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.error || error.message || 'Failed to send notification'
      };
    }
  },

  sendNotificationToAllStaff: async (title, message) => {
    try {
      const response = await axios.post(
        `${API_URL}/notifications/broadcast/staff`,
        { title, message },
        { headers: getAuthHeaders() }
      );
      return { success: true, data: response.data };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.error || error.message || 'Failed to send notification to staff'
      };
    }
  }
};

export default notificationService;
