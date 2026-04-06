// ============================================
// APPOINTMENT SERVICE
// Handles all API calls for appointments and time slots
// ============================================

import { API_BASE_URL, API_ENDPOINTS } from './api-endpoints';
import { getUserId, getUserRole, getAuthHeaders } from './auth-helper';
import { transformAppointment, transformTimeSlot } from './data-transformer';
import { notificationService } from './notificationService';

/**
 * APPOINTMENT SERVICE CLASS
 * Provides methods for all appointment and time slot operations
 */
class AppointmentService {
    constructor(user) {
        this.user = user;
        this.baseHeaders = getAuthHeaders(user);
    }

    // Update user and headers when authentication changes
    updateUser(user) {
        this.user = user;
        this.baseHeaders = getAuthHeaders(user);
    }

    /**
     * STUDENT OPERATIONS
     */

    // Get available time slots for students
    async getAvailableSlots(date = null) {
        try {
            console.log('Fetching available slots for:', getUserRole(this.user), 'user');

            let slots = [];

            if (date) {
                const url = `${API_BASE_URL}${API_ENDPOINTS.AVAILABLE_SLOTS}?date=${date}`;
                console.log('Fetching slots for date:', date, 'URL:', url);

                const response = await fetch(url, {
                    method: 'GET',
                    headers: this.baseHeaders
                });

                if (!response.ok) {
                    const errorText = await response.text();
                    throw new Error(`Failed to fetch available slots: ${response.status} - ${errorText}`);
                }

                slots = await response.json();
            } else {
                // By default fetch all slots so newly created future slots are visible to students.
                const allSlotsResponse = await fetch(`${API_BASE_URL}${API_ENDPOINTS.TIME_SLOTS}`, {
                    method: 'GET',
                    headers: this.baseHeaders
                });

                if (!allSlotsResponse.ok) {
                    const errorText = await allSlotsResponse.text();
                    throw new Error(`Failed to fetch time slots: ${allSlotsResponse.status} - ${errorText}`);
                }

                const allSlots = await allSlotsResponse.json();
                slots = allSlots.filter((slot) => {
                    const maxBookings = Number(slot.maxBookings ?? 1);
                    const currentBookings = Number(slot.currentBookings ?? 0);
                    return slot.available === true && currentBookings < maxBookings;
                });
            }

            slots.sort((a, b) => {
                const dateCompare = String(a.slotDate || a.date || '').localeCompare(String(b.slotDate || b.date || ''));
                if (dateCompare !== 0) {
                    return dateCompare;
                }
                return String(a.startTime || a.time || '').localeCompare(String(b.startTime || b.time || ''));
            });
            
            // Transform slots for frontend compatibility
            const transformedSlots = slots.map(slot => transformTimeSlot(slot));
            console.log('Transformed slots:', transformedSlots);
            
            return transformedSlots;
        } catch (error) {
            console.error('Error fetching available slots:', error);
            throw error;
        }
    }

    // Book an appointment (Student only)
    async bookAppointment(timeSlotId, bookingData) {
        try {
            const userRole = getUserRole(this.user);
            const userId = getUserId(this.user);
            
            console.log('Booking appointment with userRole:', userRole, 'userId:', userId);
            
            const requestBody = {
                studentId: bookingData.studentId || userId,
                reason: bookingData.reason,
                notes: bookingData.notes || bookingData.symptoms
            };
            
            console.log('Booking request body:', requestBody);
            
            const response = await fetch(`${API_BASE_URL}${API_ENDPOINTS.BOOK_APPOINTMENT(timeSlotId)}`, {
                method: 'POST',
                headers: this.baseHeaders,
                body: JSON.stringify(requestBody)
            });
            
            console.log('Booking response status:', response.status);
            
            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                console.log('Booking error response:', errorData);
                throw new Error(errorData.message || `Failed to book appointment: ${response.status}`);
            }
            
            const result = await response.json();
            console.log('Booking success response:', result);
            
            // Send notification to all staff members after successful booking
            try {
                const studentId = bookingData.studentId || userId;
                await notificationService.sendNotificationToAllStaff(
                    'New Appointment Booked',
                    `Student ${studentId} has booked a new appointment. Reason: ${bookingData.reason}`,
                    'info'
                );
                console.log('Notification sent successfully to all staff members');
            } catch (notificationError) {
                console.error('Failed to send notification to staff:', notificationError);
                // Don't fail the whole operation if notification fails
            }
            
            return result;
        } catch (error) {
            console.error('Error booking appointment:', error);
            throw error;
        }
    }

    // Get student's own appointments (Student only)
    async getStudentAppointments() {
        try {
            console.log('Fetching student appointments...');
            const url = `${API_BASE_URL}/appointments/student/my-appointments`;
            console.log('URL:', url);
            
            const response = await fetch(url, {
                method: 'GET',
                headers: this.baseHeaders
            });

            console.log('Response status:', response.status);
            if (!response.ok) {
                throw new Error(`Failed to fetch student appointments: ${response.status}`);
            }

            const appointments = await response.json();
            console.log('Raw appointments from API:', appointments);
            // Transform appointments for frontend compatibility
            const transformed = appointments.map(apt => transformAppointment(apt));
            console.log('Transformed appointments:', transformed);
            return transformed;
        } catch (error) {
            console.error('Error fetching student appointments:', error);
            throw error;
        }
    }

    // Get user's appointments (Student - own appointments only, Staff - all appointments)
    async getUserAppointments(userId) {
        try {
            console.log('Fetching appointments for user:', userId);
            const url = `${API_BASE_URL}${API_ENDPOINTS.USER_APPOINTMENTS(userId)}`;
            console.log('URL:', url);
            
            const response = await fetch(url, {
                method: 'GET',
                headers: this.baseHeaders
            });

            console.log('Response status:', response.status);
            if (!response.ok) {
                throw new Error(`Failed to fetch user appointments: ${response.status}`);
            }

            const appointments = await response.json();
            console.log('Raw appointments from API:', appointments);
            // Transform appointments for frontend compatibility
            const transformed = appointments.map(apt => transformAppointment(apt));
            console.log('Transformed appointments:', transformed);
            return transformed;
        } catch (error) {
            console.error('Error fetching user appointments:', error);
            throw error;
        }
    }

    // Reschedule appointment (Student only)
    async rescheduleAppointment(appointmentId, newTimeSlotId) {
        try {
            const response = await fetch(`${API_BASE_URL}${API_ENDPOINTS.RESCHEDULE_APPOINTMENT(appointmentId)}`, {
                method: 'PUT',
                headers: this.baseHeaders,
                body: JSON.stringify({ newTimeSlotId })
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to reschedule appointment: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Error rescheduling appointment:', error);
            throw error;
        }
    }

    // Cancel appointment (Student/Staff)
    async cancelAppointment(appointmentId) {
        try {
            const response = await fetch(`${API_BASE_URL}${API_ENDPOINTS.CANCEL_APPOINTMENT(appointmentId)}`, {
                method: 'PUT',
                headers: this.baseHeaders
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to cancel appointment: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Error cancelling appointment:', error);
            throw error;
        }
    }

    /**
     * STAFF OPERATIONS
     */

    // Get all appointments (Staff only)
    async getAllAppointments(startDate = null, endDate = null) {
        try {
            console.log('=== GET ALL APPOINTMENTS (STAFF) ===');
            console.log('User:', this.user);
            console.log('User role:', this.user?.role);
            console.log('Headers:', this.baseHeaders);
            
            const url = `${API_BASE_URL}${API_ENDPOINTS.STAFF_ALL_APPOINTMENTS}`;
            console.log('Fetching from URL:', url);
            
            const response = await fetch(url, {
                method: 'GET',
                headers: this.baseHeaders
            });

            console.log('Response status:', response.status);
            console.log('Response headers:', response.headers);
            
            if (!response.ok) {
                const errorText = await response.text();
                console.error('Failed to fetch all appointments:', response.status, errorText);
                throw new Error(`Failed to fetch all appointments: ${response.status} - ${errorText}`);
            }

            const appointments = await response.json();
            console.log('Raw appointments from API:', appointments);
            console.log('Number of appointments:', appointments.length);
            
            // Log first appointment details if available
            if (appointments.length > 0) {
                console.log('First appointment sample:', appointments[0]);
                console.log('First appointment keys:', Object.keys(appointments[0]));
            }
            
            // Transform appointments for frontend compatibility
            const transformed = appointments.map(apt => {
                const result = transformAppointment(apt);
                console.log('Transformed appointment:', result);
                return result;
            });
            console.log('All transformed appointments:', transformed);
            return transformed;
        } catch (error) {
            console.error('Error fetching all appointments:', error);
            throw error;
        }
    }

    // Complete appointment (Staff only)
    async completeAppointment(appointmentId) {
        try {
            const response = await fetch(`${API_BASE_URL}${API_ENDPOINTS.COMPLETE_APPOINTMENT(appointmentId)}`, {
                method: 'PUT',
                headers: this.baseHeaders
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to complete appointment: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Error completing appointment:', error);
            throw error;
        }
    }

    // Mark appointment as success (Staff only)
    async successAppointment(appointmentId) {
        try {
            const response = await fetch(`${API_BASE_URL}${API_ENDPOINTS.COMPLETE_APPOINTMENT(appointmentId)}`, {
                method: 'PUT',
                headers: this.baseHeaders,
                body: JSON.stringify({ status: 'success' })
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to mark appointment as success: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Error marking appointment as success:', error);
            throw error;
        }
    }

    /**
     * TIME SLOT MANAGEMENT (Staff)
     */

    // Create time slot (Staff only)
    async createTimeSlot(slotData) {
        try {
            console.log('createTimeSlot called with user:', this.user);
            console.log('User role:', this.user?.role);
            
            const requestBody = {
                slotDate: slotData.date,
                startTime: this.formatTimeForAPI(slotData.time),
                endTime: slotData.endTime || this.calculateEndTime(slotData.time),
                maxBookings: slotData.maxBookings || 1,
                currentBookings: 0,
                staffId: slotData.staffId || getUserId(this.user) || 'STAFF001',
                isAvailable: true
            };
            
            console.log('Request body:', requestBody);
            
            const response = await fetch(`${API_BASE_URL}${API_ENDPOINTS.TIME_SLOTS}`, {
                method: 'POST',
                headers: this.baseHeaders,
                body: JSON.stringify(requestBody)
            });

            console.log('API response status:', response.status);
            console.log('API response headers:', response.headers);

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                console.log('Error response:', errorData);
                throw new Error(errorData.message || `Failed to create time slot: ${response.status}`);
            }

            const result = await response.json();
            console.log('Success response:', result);
            return result;
        } catch (error) {
            console.error('Error creating time slot:', error);
            throw error;
        }
    }

    // Update time slot (Staff only)
    async updateTimeSlot(timeSlotId, slotData) {
        try {
            const response = await fetch(`${API_BASE_URL}${API_ENDPOINTS.TIME_SLOT_BY_ID(timeSlotId)}`, {
                method: 'PUT',
                headers: this.baseHeaders,
                body: JSON.stringify({
                    slotDate: slotData.date,
                    startTime: this.formatTimeForAPI(slotData.time),
                    endTime: slotData.endTime || this.calculateEndTime(slotData.time),
                    maxBookings: slotData.maxBookings || 1,
                    staffId: slotData.staffId || this.user?.userId || 'STAFF001',
                    isAvailable: slotData.isAvailable !== undefined ? slotData.isAvailable : true
                })
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to update time slot: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Error updating time slot:', error);
            throw error;
        }
    }

    // Delete time slot (Staff only)
    async deleteTimeSlot(timeSlotId) {
        try {
            const response = await fetch(`${API_BASE_URL}${API_ENDPOINTS.TIME_SLOT_BY_ID(timeSlotId)}`, {
                method: 'DELETE',
                headers: this.baseHeaders
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to delete time slot: ${response.status}`);
            }

            return true; // Delete operations typically return 204 No Content
        } catch (error) {
            console.error('Error deleting time slot:', error);
            throw error;
        }
    }

    // Get staff's time slots (Staff only)
    async getStaffSlots(staffId = null) {
        try {
            const id = staffId || getUserId(this.user);
            if (!id) {
                console.warn('Staff ID is missing, falling back to available slots');
                return await this.getAvailableSlots();
            }

            const response = await fetch(`${API_BASE_URL}${API_ENDPOINTS.STAFF_SLOTS(id)}`, {
                method: 'GET',
                headers: this.baseHeaders
            });

            if (!response.ok) {
                console.warn(`Failed to fetch staff slots (${response.status}), falling back to available slots`);
                return await this.getAvailableSlots();
            }

            const slots = await response.json();
            // Transform slots for frontend compatibility
            return slots.map(slot => transformTimeSlot(slot));
        } catch (error) {
            console.error('Error fetching staff slots, falling back to available slots:', error);
            return await this.getAvailableSlots();
        }
    }

    /**
     * UTILITY METHODS
     */

    // Calculate end time based on start time (assuming 1-hour slots)
    calculateEndTime(startTime) {
        if (!startTime) return null;
        
        // Handle different time formats
        let timeStr = startTime;
        if (startTime.length === 5) { // "HH:MM" format
            timeStr = `${startTime}:00`; // Convert to "HH:MM:00"
        }
        
        const [hours, minutes] = timeStr.split(':').map(Number);
        const endHours = hours + 1;
        
        return `${endHours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:00`;
    }

    // Convert time string to proper format
    formatTimeForAPI(time) {
        if (!time) return null;
        
        // Handle different time formats
        if (time.length === 5) { // "HH:MM" format
            return `${time}:00`; // Convert to "HH:MM:00"
        }
        
        return time; // Already in correct format
    }
}

// Export singleton instance
export default AppointmentService;
