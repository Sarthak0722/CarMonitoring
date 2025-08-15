import React, { useEffect, useState } from 'react';
import api from '../api/client';
import { useNavigate } from 'react-router-dom';

const RegisterPage = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    username: '',
    password: '',
    role: 'DRIVER',
    name: '',
    age: 25,
    gender: 'MALE',
    contactNumber: '',
    email: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [users, setUsers] = useState([]);
  const [refreshKey, setRefreshKey] = useState(0);

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const fetchUsers = async () => {
    try {
      const res = await api.get('/users');
      setUsers(res?.data?.data || []);
    } catch (e) {
      // ignore listing errors
    }
  };

  useEffect(() => { fetchUsers(); }, [refreshKey]);

  const onSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      // Basic client-side checks matching backend constraints
      if (!form.username || form.username.length < 3) throw new Error('Username must be at least 3 characters');
      if (!form.password || form.password.length < 6) throw new Error('Password must be at least 6 characters');
      if (!form.name) throw new Error('Name is required');
      if (!form.age || Number(form.age) < 18) throw new Error('Age must be at least 18');
      if (!form.contactNumber) throw new Error('Contact number is required');
      if (!form.email) throw new Error('Email is required');

      const res = await api.post('/users/register', form);
      const user = res?.data?.data;
      if (!user?.id) throw new Error(res?.data?.message || 'Registration failed');

      // If DRIVER, create driver entity so it appears in drivers list
      if (form.role === 'DRIVER') {
        try {
          await api.post('/drivers', { userId: user.id, assignedCarId: null });
        } catch (_) { /* ignore driver creation failure */ }
      }

      // Clear form and refresh users list
      setForm({ username: '', password: '', role: 'DRIVER', name: '', age: 25, gender: 'MALE', contactNumber: '', email: '' });
      setRefreshKey((k) => k + 1);
      alert('Registered successfully!');
    } catch (err) {
      setError(err?.response?.data?.message || err.message || 'Failed to register');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="pt-16">
      <div className="max-w-4xl mx-auto p-6">
        <div className="bg-white p-6 shadow rounded mb-6">
          <h1 className="text-2xl font-bold mb-4">Register User</h1>
          {error && <div className="bg-red-100 text-red-700 p-2 mb-3 rounded">{error}</div>}
          <form onSubmit={onSubmit} className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium mb-1">Username</label>
              <input name="username" placeholder="Username" className="w-full border px-3 py-2 rounded" value={form.username} onChange={onChange} required />
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">Password</label>
              <input name="password" type="password" placeholder="Password" className="w-full border px-3 py-2 rounded" value={form.password} onChange={onChange} required />
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">Role</label>
              <select name="role" className="w-full border px-3 py-2 rounded" value={form.role} onChange={onChange}>
                <option value="DRIVER">Driver</option>
                <option value="ADMIN">Admin</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">Full Name</label>
              <input name="name" placeholder="Full Name" className="w-full border px-3 py-2 rounded" value={form.name} onChange={onChange} required />
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">Age</label>
              <input name="age" type="number" min="18" max="100" placeholder="Age" className="w-full border px-3 py-2 rounded" value={form.age} onChange={onChange} required />
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">Gender</label>
              <select name="gender" className="w-full border px-3 py-2 rounded" value={form.gender} onChange={onChange}>
                <option value="MALE">Male</option>
                <option value="FEMALE">Female</option>
                <option value="OTHER">Other</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">Contact Number</label>
              <input name="contactNumber" placeholder="e.g. 9999999999" className="w-full border px-3 py-2 rounded" value={form.contactNumber} onChange={onChange} required />
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">Email</label>
              <input name="email" type="email" placeholder="Email" className="w-full border px-3 py-2 rounded" value={form.email} onChange={onChange} required />
            </div>
            <div className="md:col-span-2">
              <button type="submit" className="w-full bg-blue-600 text-white py-2 rounded" disabled={loading}>
                {loading ? 'Registering...' : 'Register'}
              </button>
            </div>
          </form>
        </div>

        <div className="bg-white p-6 shadow rounded">
          <div className="flex items-center justify-between mb-3">
            <h2 className="text-xl font-semibold">Users (DB)</h2>
            <button className="text-sm px-3 py-1 border rounded" onClick={() => setRefreshKey((k) => k + 1)}>Refresh</button>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full border-collapse">
              <thead>
                <tr className="bg-gray-100 text-left">
                  <th className="p-2">ID</th>
                  <th className="p-2">Username</th>
                  <th className="p-2">Role</th>
                  <th className="p-2">Name</th>
                  <th className="p-2">Age</th>
                  <th className="p-2">Gender</th>
                  <th className="p-2">Contact</th>
                  <th className="p-2">Email</th>
                  <th className="p-2">Active</th>
                </tr>
              </thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.id} className="border-b">
                    <td className="p-2">{u.id}</td>
                    <td className="p-2">{u.username}</td>
                    <td className="p-2">{u.role}</td>
                    <td className="p-2">{u.name}</td>
                    <td className="p-2">{u.age}</td>
                    <td className="p-2">{u.gender}</td>
                    <td className="p-2">{u.contactNumber}</td>
                    <td className="p-2">{u.email}</td>
                    <td className="p-2">{String(u.isActive)}</td>
                  </tr>
                ))}
                {users.length === 0 && (
                  <tr>
                    <td className="p-3 text-gray-500" colSpan="9">No users found.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;