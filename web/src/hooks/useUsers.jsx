import { useEffect, useMemo, useState } from 'react';

const useUsers = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadUsers = async () => {
      try {
        const res = await fetch('/api/users/');
        if (!res.ok) {
          setUsers([]);
          return;
        }
        const data = await res.json();
        setUsers(Array.isArray(data) ? data : []);
      } catch {
        setUsers([]);
      } finally {
        setLoading(false);
      }
    };

    loadUsers();
  }, []);

  const studentsOnly = useMemo(
    () => users.filter((u) => (u.role || '').toLowerCase() === 'student'),
    [users]
  );
  const staffOnly = useMemo(
    () => users.filter((u) => (u.role || '').toLowerCase() === 'staff'),
    [users]
  );

  return {
    users,
    usersCount: users.length,
    studentsOnly,
    staffOnly,
    loading,
  };
};

export default useUsers;
