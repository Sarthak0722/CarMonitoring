import React, { useState } from 'react';
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
    contactNumber: '0000000000',
    email: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const onSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await api.post('/users/register', form);
      const user = res?.data?.data;
      if (!user?.id) throw new Error('Registration failed');
      // Create Driver entity so the user appears in drivers list
      await api.post('/drivers', { userId: user.id, assignedCarId: null });
      navigate('/');
    } catch (err) {
      setError(err?.response?.data?.message || err.message || 'Failed to register');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="pt-16">
      <div className="max-w-lg mx-auto bg-white p-6 shadow rounded">
        <h1 className="text-2xl font-bold mb-4">Driver Registration</h1>
        {error && <div className="bg-red-100 text-red-700 p-2 mb-3 rounded">{error}</div>}
        <form onSubmit={onSubmit} className="space-y-3">
          <input name="username" placeholder="Username" className="w-full border px-3 py-2 rounded" value={form.username} onChange={onChange} required />
          <input name="password" type="password" placeholder="Password" className="w-full border px-3 py-2 rounded" value={form.password} onChange={onChange} required />
          <input name="name" placeholder="Full Name" className="w-full border px-3 py-2 rounded" value={form.name} onChange={onChange} required />
          <input name="age" type="number" placeholder="Age" className="w-full border px-3 py-2 rounded" value={form.age} onChange={onChange} required />
          <select name="gender" className="w-full border px-3 py-2 rounded" value={form.gender} onChange={onChange}>
            <option value="MALE">Male</option>
            <option value="FEMALE">Female</option>
            <option value="OTHER">Other</option>
          </select>
          <input name="contactNumber" placeholder="Contact Number" className="w-full border px-3 py-2 rounded" value={form.contactNumber} onChange={onChange} required />
          <input name="email" type="email" placeholder="Email" className="w-full border px-3 py-2 rounded" value={form.email} onChange={onChange} required />
          <button type="submit" className="w-full bg-blue-600 text-white py-2 rounded" disabled={loading}>
            {loading ? 'Registering...' : 'Register'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default RegisterPage;