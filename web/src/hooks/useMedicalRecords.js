import { useCallback, useEffect, useMemo, useState } from 'react';
import useAuth from './useAuth';
import { medicalRecordsService } from '../services/medicalRecordsService';

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
});

const parsePrescriptionCount = (record) => {
  if (!record?.prescription) return 0;
  if (Array.isArray(record.prescription)) return record.prescription.length;
  return String(record.prescription)
    .split(',')
    .map((value) => value.trim())
    .filter(Boolean).length;
};

const toVitalSignsString = (vitalSigns) => {
  if (!vitalSigns) return '{}';
  if (typeof vitalSigns === 'string') return vitalSigns;
  return JSON.stringify(vitalSigns);
};

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
      let result;
      if (isStaff) {
        result = await medicalRecordsService.getAllMedicalRecords();
      } else if (currentUserId) {
        result = await medicalRecordsService.getMedicalRecordsByUserId(currentUserId);
      } else {
        setRecords([]);
        setLoading(false);
        return;
      }

      if (result.success) {
        const normalized = (Array.isArray(result.data) ? result.data : []).map((item, i) =>
          normalizeRecord(item, i)
        );
        setRecords(normalized);
      } else {
        setError(result.error || 'Unable to load medical records.');
        setRecords([]);
      }
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
      const payload = {
        userId: recordData.studentId || recordData.userId,
        appointmentId: recordData.appointmentId || null,
        diagnosis: recordData.diagnosis,
        symptoms: recordData.symptoms || '',
        treatment: recordData.treatment || '',
        prescription: recordData.prescriptions || recordData.prescription || '',
        vitalSigns: toVitalSignsString(recordData.vitalSigns),
        allergies: recordData.allergies || '',
        medicalHistory: recordData.medicalHistory || '',
        notes: recordData.notes || '',
        createdBy: currentUserId,
      };

      const result = await medicalRecordsService.createMedicalRecord(payload);
      if (result.success) {
        await fetchMedicalRecords();
      }
      return result;
    } catch {
      return { success: false, error: 'Failed to create record.' };
    }
  }, [currentUserId, isStaff, fetchMedicalRecords]);

  const updateRecord = useCallback(async (recordId, recordData) => {
    if (!isStaff) {
      return { success: false, error: 'Unauthorized: Staff access required' };
    }

    try {
      const payload = {
        userId: recordData.studentId || recordData.userId,
        appointmentId: recordData.appointmentId ?? null,
        diagnosis: recordData.diagnosis,
        symptoms: recordData.symptoms ?? '',
        treatment: recordData.treatment ?? '',
        prescription: recordData.prescriptions ?? recordData.prescription ?? '',
        vitalSigns: toVitalSignsString(recordData.vitalSigns),
        allergies: recordData.allergies ?? '',
        medicalHistory: recordData.medicalHistory ?? '',
        notes: recordData.notes ?? '',
      };

      const result = await medicalRecordsService.updateMedicalRecord(recordId, payload);
      if (result.success) {
        await fetchMedicalRecords();
      }
      return result;
    } catch {
      return { success: false, error: 'Failed to update record.' };
    }
  }, [isStaff, fetchMedicalRecords]);

  const deleteRecord = useCallback(async (recordId) => {
    if (!isStaff) {
      return { success: false, error: 'Unauthorized: Staff access required' };
    }

    try {
      const result = await medicalRecordsService.deleteMedicalRecord(recordId);
      if (result.success) {
        await fetchMedicalRecords();
      }
      return result;
    } catch {
      return { success: false, error: 'Failed to delete record.' };
    }
  }, [isStaff, fetchMedicalRecords]);

  const latestVitalSigns = useMemo(() => {
    if (!records.length) return null;
    const latest = [...records].sort(
      (left, right) =>
        new Date(right.recordDate || right.createdAt) - new Date(left.recordDate || left.createdAt)
    )[0];
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
