import React, { useState, useMemo, useCallback } from 'react';
import useAuth from '../hooks/useAuth';
import useAppointments from '../hooks/useAppointments';
import { ChevronLeft, ChevronRight, Calendar as CalendarIcon } from 'lucide-react';
import { Button, Card } from '../components/common';
import './Calendar.css';

const Calendar = () => {
  const { isStaff } = useAuth();
  const { userAppointments, loading, error } = useAppointments();

  const [currentDate, setCurrentDate] = useState(new Date());
  const [selectedDate, setSelectedDate] = useState(new Date());

  // Generate time slots (8 AM – 6 PM, 30-min intervals)
  const timeSlots = useMemo(() => {
    const slots = [];
    for (let hour = 8; hour <= 18; hour++) {
      for (let minute = 0; minute < 60; minute += 30) {
        slots.push(`${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`);
      }
    }
    return slots;
  }, []);

  // Get week days starting from Monday
  const getWeekDays = useCallback((date) => {
    const d = new Date(date);
    const day = d.getDay();
    const diff = d.getDate() - day + (day === 0 ? -6 : 1);
    const monday = new Date(d.setDate(diff));
    const weekDays = [];
    for (let i = 0; i < 7; i++) {
      const wd = new Date(monday);
      wd.setDate(monday.getDate() + i);
      weekDays.push(wd);
    }
    return weekDays;
  }, []);

  const weekDays = useMemo(() => getWeekDays(currentDate), [currentDate, getWeekDays]);

  // Format date using local components to avoid UTC timezone shift in UTC+ zones
  const formatDate = useCallback((date) => {
    const d = new Date(date);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }, []);

  const isToday = useCallback((date) => {
    return formatDate(date) === formatDate(new Date());
  }, [formatDate]);

  const isSelected = useCallback((date) => {
    return formatDate(date) === formatDate(selectedDate);
  }, [formatDate, selectedDate]);

  // Map appointments by "YYYY-MM-DD-HH:MM" key → array
  const appointmentMap = useMemo(() => {
    const map = {};
    if (!userAppointments?.length) return map;

    userAppointments.forEach(apt => {
      try {
        const dateValue = apt.scheduledDate ?? apt.date ?? apt.appointmentDate;
        if (!dateValue) return;

        const timeStr = apt.scheduledTime ?? apt.time ?? apt.appointmentTime;
        if (!timeStr) return;

        const parts = String(timeStr).split(':');
        const normalizedTime = `${parts[0].padStart(2, '0')}:${(parts[1] ?? '00').padStart(2, '0')}`;

        const dateStr = String(dateValue).includes('T')
          ? String(dateValue).split('T')[0]
          : String(dateValue).substring(0, 10);

        const key = `${dateStr}-${normalizedTime}`;
        if (!map[key]) map[key] = [];
        map[key].push(apt);
      } catch {
        // skip malformed entries
      }
    });

    return map;
  }, [userAppointments]);

  const getAppointmentsForSlot = useCallback((date, time) => {
    const dateStr = formatDate(date);
    const parts = String(time).split(':');
    const normalizedTime = `${parts[0].padStart(2, '0')}:${(parts[1] ?? '00').padStart(2, '0')}`;
    return appointmentMap[`${dateStr}-${normalizedTime}`] ?? [];
  }, [formatDate, appointmentMap]);

  const previousWeek = useCallback(() => {
    const d = new Date(currentDate);
    d.setDate(d.getDate() - 7);
    setCurrentDate(d);
  }, [currentDate]);

  const nextWeek = useCallback(() => {
    const d = new Date(currentDate);
    d.setDate(d.getDate() + 7);
    setCurrentDate(d);
  }, [currentDate]);

  const goToToday = useCallback(() => {
    const today = new Date();
    setCurrentDate(today);
    setSelectedDate(today);
  }, []);

  const handleDateSelect = useCallback((date) => setSelectedDate(date), []);

  const appointmentCount = userAppointments?.length ?? 0;

  return (
    <div className="calendar-page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Calendar</h1>
          <p className="page-subtitle">
            {isStaff
              ? 'View all scheduled appointments'
              : 'View and manage your appointment schedule'}
          </p>
        </div>
        <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
          {appointmentCount > 0 && (
            <span style={{
              background: '#8B1A1A',
              color: 'white',
              padding: '0.375rem 0.875rem',
              borderRadius: '20px',
              fontSize: '0.8125rem',
              fontWeight: 600,
            }}>
              {appointmentCount} Appointment{appointmentCount === 1 ? '' : 's'}
            </span>
          )}
          <Button variant="primary" icon={CalendarIcon} onClick={goToToday}>
            Today
          </Button>
        </div>
      </div>

      {loading && (
        <Card style={{ padding: '2rem', textAlign: 'center' }}>
          <p>Loading appointments...</p>
        </Card>
      )}

      {error && (
        <Card style={{ padding: '2rem', background: '#fee2e2', border: '1px solid #fca5a5' }}>
          <p style={{ color: '#b91c1c', margin: 0 }}>Error loading appointments: {error}</p>
        </Card>
      )}

      {!loading && !error && userAppointments?.length === 0 && (
        <Card style={{ padding: '2rem', textAlign: 'center', marginBottom: '1rem' }}>
          <p style={{ margin: 0, color: '#6B7280' }}>
            {isStaff
              ? 'No appointments have been scheduled yet.'
              : 'No appointments scheduled yet. Book one to see it here!'}
          </p>
        </Card>
      )}

      {/* Week navigation */}
      <Card className="calendar-controls">
        <button className="nav-btn" onClick={previousWeek} aria-label="Previous week">
          <ChevronLeft size={18} />
        </button>

        <div className="calendar-title">
          <h2>
            {weekDays[0].toLocaleDateString('en-US', { month: 'long', year: 'numeric' })}
          </h2>
          <p className="week-range">
            {weekDays[0].toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}
            {' – '}
            {weekDays[6].toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}
          </p>
        </div>

        <button className="nav-btn" onClick={nextWeek} aria-label="Next week">
          <ChevronRight size={18} />
        </button>
      </Card>

      {/* Calendar grid */}
      <Card className="calendar-container">
        <div className="calendar-grid">

          {/* Day headers */}
          <div className="calendar-header">
            <div className="time-column-header">Time</div>
            {weekDays.map((day) => (
              <button
                key={formatDate(day)}
                type="button"
                className={`day-header${isToday(day) ? ' today' : ''}${isSelected(day) ? ' selected' : ''}`}
                onClick={() => handleDateSelect(day)}
              >
                <div className="day-name">
                  {day.toLocaleDateString('en-US', { weekday: 'short' })}
                </div>
                <div className="day-number">{day.getDate()}</div>
              </button>
            ))}
          </div>

          {/* Time slot rows */}
          <div className="calendar-body">
            {timeSlots.map((time) => (
              <div key={time} className="time-row">
                <div className="time-cell">{time}</div>
                {weekDays.map((day) => {
                  const slotAppointments = getAppointmentsForSlot(day, time);
                  return (
                    <div
                      key={formatDate(day)}
                      className={`slot-cell${slotAppointments.length > 0 ? ' has-appointment' : ''}`}
                    >
                      {slotAppointments.map((apt) => {
                        const status = (apt.status ?? 'scheduled').toLowerCase();
                        const aptTime = String(apt.scheduledTime ?? apt.time ?? '').substring(0, 5);
                        const studentLine = isStaff ? `Student: ${apt.studentId ?? 'Unknown'}\n` : '';
                        const tooltipText = `${studentLine}Status: ${apt.status ?? ''}\nReason: ${apt.reason ?? 'N/A'}\nTime: ${aptTime}`;
                        return (
                          <div
                            key={apt.appointmentId}
                            className={`appointment-block ${status}`}
                            title={tooltipText}
                          >
                            <div className="appointment-time">{aptTime}</div>
                            <div className="appointment-student">
                              {isStaff
                                ? (apt.user?.schoolId ?? apt.studentId ?? 'Unknown Student')
                                : (apt.reason ?? 'Appointment')}
                            </div>
                            <div className="appointment-concern">
                              {isStaff
                                ? (apt.reason ?? 'Medical Appointment')
                                : (apt.location ?? 'Clinic')}
                            </div>
                            {isStaff && apt.notes && (
                              <div className="appointment-notes">
                                {apt.notes.length > 30 ? `${apt.notes.substring(0, 30)}…` : apt.notes}
                              </div>
                            )}
                          </div>
                        );
                      })}
                    </div>
                  );
                })}
              </div>
            ))}
          </div>
        </div>
      </Card>

      {/* Legend */}
      <Card className="calendar-legend">
        <h3>Legend</h3>
        <div className="legend-items">
          {[
            { status: 'scheduled',   label: 'Scheduled' },
            { status: 'confirmed',   label: 'Confirmed' },
            { status: 'completed',   label: 'Completed' },
            { status: 'rescheduled', label: 'Rescheduled' },
            { status: 'cancelled',   label: 'Cancelled' },
            { status: 'pending',     label: 'Pending' },
          ].map(({ status, label }) => (
            <div key={status} className="legend-item">
              <div className={`legend-color ${status}`} />
              <span>{label}</span>
            </div>
          ))}
        </div>
      </Card>
    </div>
  );
};

export default Calendar;
