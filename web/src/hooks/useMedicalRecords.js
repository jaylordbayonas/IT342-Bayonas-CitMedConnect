import { useCallback, useEffect, useMemo, useState } from 'react';
import useAuth from './useAuth';

const STORAGE_KEY = 'citmedconnect_medical_records';

const loadStoredRecords = () => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    const parsed = raw ? JSON.parse(raw) : [];
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
};

const saveStoredRecords = (records) => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(records));
};

const normalizeVitalSigns = (vitalSigns) => {
  if (!vitalSigns) return null;
  if (typeof vitalSigns === 'object') return vitalSigns;
  try {
    return JSON.parse(vitalSigns);
  } catch {
    return null;
  }
};

const normalizeRecord = (item, index = 0) => ({
  recordId: item.recordId || item.id || `record-${Date.now()}-${index}`,
  userId: item.userId || item.studentId || item.schoolId || '',
  userName: item.userName || item.studentName || item.studentId || item.schoolId || 'Unknown User',
  appointmentId: item.appointmentId ?? null,
  recordDate: item.recordDate || item.createdAt || new Date().toISOString(),
  diagnosis: item.diagnosis || '',
  symptoms: item.symptoms || '',
  treatment: item.treatment || '',
  prescription: item.prescription || item.prescriptions || '',
  vitalSigns: normalizeVitalSigns(item.vitalSigns),
  allergies: item.allergies || '',
  medicalHistory: item.medicalHistory || '',
  notes: item.notes || '',
  createdAt: item.createdAt || new Date().toISOString(),
  updatedAt: item.updatedAt || new Date().toISOString(),
  createdBy: item.createdBy || item.staffId || null,
  ...item,
});

const parsePrescriptionCount = (record) => {
  if (!record?.prescription) return 0;
  if (Array.isArray(record.prescription)) return record.prescription.length;
  return String(record.prescription)
    .split(',')
    .map((value) => value.trim())
    .filter(Boolean).length;
};

const normalizeStoredRecords = (records) => records.map((item, index) => normalizeRecord(item, index));

const useMedicalRecords = () => {
  const { user, isStaff } = useAuth();
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const currentUserId = user?.userId || user?.schoolId || user?.id || '';

  const fetchMedicalRecords = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const storedRecords = normalizeStoredRecords(loadStoredRecords());
      const visibleRecords = isStaff
        ? storedRecords
        : storedRecords.filter((record) => !currentUserId || record.userId === currentUserId);

      setRecords(visibleRecords);
    } catch {
      setError('Unable to load medical records.');
      setRecords([]);
    } finally {
      setLoading(false);
    }
  }, [currentUserId, isStaff]);

  useEffect(() => {
    fetchMedicalRecords();
  }, [fetchMedicalRecords]);

  const createRecord = useCallback(async (recordData) => {
    if (!isStaff) {
      return { success: false, error: 'Unauthorized: Staff access required' };
    }

    try {
      const nextRecord = normalizeRecord({
        recordId: `record-${Date.now()}`,
        userId: recordData.studentId || recordData.userId,
        userName: recordData.studentName || recordData.userName || recordData.studentId || 'Student',
        appointmentId: recordData.appointmentId || null,
        diagnosis: recordData.diagnosis,
        symptoms: recordData.symptoms || '',
        treatment: recordData.treatment || '',
        prescription: recordData.prescriptions || '',
        vitalSigns: recordData.vitalSigns || null,
        allergies: recordData.allergies || '',
        medicalHistory: recordData.medicalHistory || '',
        notes: recordData.notes || '',
        createdBy: currentUserId,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      });

      const storedRecords = [nextRecord, ...normalizeStoredRecords(loadStoredRecords())];
      saveStoredRecords(storedRecords);
      setRecords(isStaff ? storedRecords : storedRecords.filter((record) => !currentUserId || record.userId === currentUserId));

      return { success: true, data: nextRecord, message: 'Record created successfully.' };
    } catch {
      return { success: false, error: 'Failed to create record.' };
    }
  }, [currentUserId, isStaff]);

  const updateRecord = useCallback(async (recordId, recordData) => {
    if (!isStaff) {
      return { success: false, error: 'Unauthorized: Staff access required' };
    }

    try {
      const updatedRecords = normalizeStoredRecords(loadStoredRecords()).map((record) => {
        if (record.recordId !== recordId) return record;

        return {
          ...record,
          userId: recordData.studentId || recordData.userId || record.userId,
          appointmentId: recordData.appointmentId ?? record.appointmentId,
          diagnosis: recordData.diagnosis ?? record.diagnosis,
          symptoms: recordData.symptoms ?? record.symptoms,
          treatment: recordData.treatment ?? record.treatment,
          prescription: recordData.prescriptions ?? record.prescription,
          vitalSigns: recordData.vitalSigns ?? record.vitalSigns,
          allergies: recordData.allergies ?? record.allergies,
          medicalHistory: recordData.medicalHistory ?? record.medicalHistory,
          notes: recordData.notes ?? record.notes,
          updatedAt: new Date().toISOString(),
        };
      });

      saveStoredRecords(updatedRecords);
      setRecords(isStaff ? updatedRecords : updatedRecords.filter((record) => !currentUserId || record.userId === currentUserId));

      const updatedRecord = updatedRecords.find((record) => record.recordId === recordId);
      return { success: true, data: updatedRecord, message: 'Record updated successfully.' };
    } catch {
      return { success: false, error: 'Failed to update record.' };
    }
  }, [currentUserId, isStaff]);

  const deleteRecord = useCallback(async (recordId) => {
    if (!isStaff) {
      return { success: false, error: 'Unauthorized: Staff access required' };
    }

    try {
      const updatedRecords = normalizeStoredRecords(loadStoredRecords()).filter((record) => record.recordId !== recordId);
      saveStoredRecords(updatedRecords);
      setRecords(isStaff ? updatedRecords : updatedRecords.filter((record) => !currentUserId || record.userId === currentUserId));
      return { success: true, message: 'Record deleted successfully.' };
    } catch {
      return { success: false, error: 'Failed to delete record.' };
    }
  }, [currentUserId, isStaff]);

  const latestVitalSigns = useMemo(() => {
    if (!records.length) return null;
    const latest = [...records].sort((left, right) => new Date(right.recordDate || right.createdAt) - new Date(left.recordDate || left.createdAt))[0];
    return normalizeVitalSigns(latest.vitalSigns);
  }, [records]);

  const activePrescriptions = useMemo(
    () => records.reduce((count, record) => count + parsePrescriptionCount(record), 0),
    [records]
  );

  const recordStats = useMemo(() => ({ total: records.length }), [records]);

  return {
    records,
    loading,
    error,
    setError,
    createRecord,
    updateRecord,
    deleteRecord,
    latestVitalSigns,
    activePrescriptions,
    recordStats,
    fetchMedicalRecords,
    refreshRecords: fetchMedicalRecords,
  };
};

export default useMedicalRecords;
