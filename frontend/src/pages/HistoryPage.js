import React, { useEffect, useState } from "react";
import { Calendar } from "lucide-react";
import api from "../api/client";

const formatLocalDateTime = (date) => {
  const pad = (n) => String(n).padStart(2, "0");
  const y = date.getFullYear();
  const m = pad(date.getMonth() + 1);
  const d = pad(date.getDate());
  const hh = pad(date.getHours());
  const mm = pad(date.getMinutes());
  const ss = pad(date.getSeconds());
  return `${y}-${m}-${d}T${hh}:${mm}:${ss}`;
};

const HistoryPage = ({ user }) => {
  const [carId, setCarId] = useState(null);
  const [timeRange, setTimeRange] = useState("24h");
  const [telemetry, setTelemetry] = useState([]);
  const [stats, setStats] = useState(null);

  const loadDriverCar = async () => {
    if (user.role !== 'DRIVER') return;
    try {
      const dres = await api.get(`/drivers/user/${user.id}`);
      setCarId(dres?.data?.data?.assignedCarId || null);
    } catch (_) { setCarId(null); }
  };

  const rangeToDates = () => {
    const end = new Date();
    const start = new Date(end);
    if (timeRange === '24h') start.setHours(end.getHours() - 24);
    else if (timeRange === '7d') start.setDate(end.getDate() - 7);
    else if (timeRange === '30d') start.setDate(end.getDate() - 30);
    return { start, end };
  };

  const loadHistory = async () => {
    if (!carId) return;
    try {
      const { start, end } = rangeToDates();
      const startStr = formatLocalDateTime(start);
      const endStr = formatLocalDateTime(end);
      const tRes = await api.get(`/telemetry/car/${carId}/range`, { params: { startTime: startStr, endTime: endStr } });
      setTelemetry(tRes?.data?.data || []);
      const sRes = await api.get(`/telemetry/stats/car/${carId}`, { params: { startTime: startStr, endTime: endStr } });
      setStats(sRes?.data?.data || null);

      // Fallback if nothing returned: try without params (backend default 7d)
      if ((tRes?.data?.data || []).length === 0) {
        const t2 = await api.get(`/telemetry/car/${carId}`);
        setTelemetry(t2?.data?.data || []);
      }
      if (!sRes?.data?.data) {
        const s2 = await api.get(`/telemetry/stats/car/${carId}`);
        setStats(s2?.data?.data || null);
      }
    } catch (_) { setTelemetry([]); setStats(null); }
  };

  useEffect(() => { if (user.role === 'DRIVER') loadDriverCar(); }, [user.id, user.role]);
  useEffect(() => { loadHistory(); }, [carId, timeRange]);

  if (user.role === 'DRIVER' && !carId) {
    return (
      <div className="pt-16">
        <div className="p-6 bg-gray-100 min-h-screen flex items-center justify-center">
          <div className="bg-white p-6 rounded shadow text-center max-w-md">
            <h2 className="text-xl font-semibold mb-2">No vehicle assigned</h2>
            <p className="text-gray-600">You will see your history once a vehicle is assigned to you.</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="pt-16">
      <div className="p-6 bg-gray-100 min-h-screen">
        <div className="mb-6 flex flex-wrap justify-between items-center">
          <div>
            <h1 className="text-2xl font-bold">Telemetry History</h1>
            <p className="text-gray-600">{user.role === 'DRIVER' ? `Car ${carId}` : 'Fleet'}</p>
          </div>
          <div className="flex gap-4 mt-4 md:mt-0">
            <select className="border px-3 py-2 rounded" value={timeRange} onChange={(e)=>setTimeRange(e.target.value)}>
              <option value="24h">Last 24 hours</option>
              <option value="7d">Last 7 days</option>
              <option value="30d">Last 30 days</option>
            </select>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-3 mb-4">
          <div className="bg-white p-3 rounded shadow">
            <h2 className="text-gray-600">Avg Speed</h2>
            <p className="text-2xl font-bold">{stats?.averageSpeed ?? 0} km/h</p>
            <p className="text-xs text-gray-500">Min {stats?.minSpeed ?? 0} • Max {stats?.maxSpeed ?? 0}</p>
          </div>
          <div className="bg-white p-3 rounded shadow">
            <h2 className="text-gray-600">Avg Fuel</h2>
            <p className="text-2xl font-bold">{stats?.averageFuel ?? 0}%</p>
            <p className="text-xs text-gray-500">Min {stats?.minFuel ?? 0}% • Max {stats?.maxFuel ?? 0}%</p>
          </div>
          <div className="bg-white p-3 rounded shadow">
            <h2 className="text-gray-600">Avg Temperature</h2>
            <p className="text-2xl font-bold">{stats?.averageTemperature ?? 0}°C</p>
            <p className="text-xs text-gray-500">Min {stats?.minTemperature ?? 0}°C • Max {stats?.maxTemperature ?? 0}°C</p>
          </div>
        </div>

        <div className="bg-white p-3 rounded shadow">
          <div className="flex items-center mb-2">
            <Calendar className="text-blue-500 mr-2" size={18} />
            <h3 className="text-lg font-semibold">Telemetry Records</h3>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full border-collapse">
              <thead>
                <tr className="bg-gray-200 text-left text-sm">
                  <th className="p-2 border">Timestamp</th>
                  <th className="p-2 border">Speed (km/h)</th>
                  <th className="p-2 border">Fuel (%)</th>
                  <th className="p-2 border">Temp (°C)</th>
                  <th className="p-2 border">Location</th>
                </tr>
              </thead>
              <tbody>
                {telemetry.length > 0 ? (
                  telemetry.map((t, idx) => (
                    <tr key={idx} className="hover:bg-gray-100 text-sm">
                      <td className="p-2 border">{t.timestamp}</td>
                      <td className="p-2 border">{t.speed}</td>
                      <td className="p-2 border">{t.fuelLevel}</td>
                      <td className="p-2 border">{t.temperature}</td>
                      <td className="p-2 border">{t.location}</td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="5" className="text-center p-4 text-gray-500">No telemetry found for the selected period.</td>
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

export default HistoryPage;

