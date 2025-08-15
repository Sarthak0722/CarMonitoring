import React, { useState, useEffect } from "react";
import { AlertTriangle, Fuel, Thermometer, Wrench, Zap } from "lucide-react";
import api from "../api/client";

const AlertsPage = ({ user }) => {
  const [alerts, setAlerts] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [severityFilter, setSeverityFilter] = useState("");
  const [carId, setCarId] = useState(null);

  const loadAlerts = async () => {
    try {
      if (user.role === 'DRIVER') {
        if (!carId) return;
        const res = await api.get(`/alerts/car/${carId}`);
        setAlerts(res?.data?.data || []);
      } else {
        const res = await api.get("/alerts");
        setAlerts(res?.data?.data || []);
      }
    } catch (e) {}
  };

  const loadDriverCar = async () => {
    if (user.role !== 'DRIVER') return;
    try {
      const dres = await api.get(`/drivers/user/${user.id}`);
      const d = dres?.data?.data;
      setCarId(d?.assignedCarId || null);
    } catch (_) { setCarId(null); }
  };

  useEffect(() => { loadDriverCar(); }, [user.id, user.role]);
  useEffect(() => { loadAlerts(); }, [carId, user.role]);

  const getAlertIcon = (type) => {
    switch ((type || '').toLowerCase()) {
      case "low_fuel":
      case "fuel":
        return <Fuel className="text-blue-500" size={18} />;
      case "high_temperature":
      case "temperature":
        return <Thermometer className="text-red-500" size={18} />;
      case "maintenance":
        return <Wrench className="text-yellow-500" size={18} />;
      case "high_speed":
      case "speed":
        return <Zap className="text-green-500" size={18} />;
      default:
        return <AlertTriangle className="text-gray-500" size={18} />;
    }
  };

  const severityColor = {
    LOW: "bg-green-200 text-green-800",
    MEDIUM: "bg-yellow-200 text-yellow-800",
    HIGH: "bg-orange-200 text-orange-800",
    CRITICAL: "bg-red-200 text-red-800",
  };

  const filteredAlerts = alerts.filter((a) => {
    const matchesTerm = [a?.type, a?.message, String(a?.car?.id || a?.carId)].some((v) => (v || '').toLowerCase().includes(searchTerm.toLowerCase()));
    const matchesSeverity = !severityFilter || (a?.severity || '').toUpperCase() === severityFilter.toUpperCase();
    return matchesTerm && matchesSeverity;
  });

  if (user.role === 'DRIVER' && !carId) {
    return (
      <div className="pt-16">
        <div className="p-6 bg-gray-100 min-h-screen flex items-center justify-center">
          <div className="bg-white p-6 rounded shadow text-center max-w-md">
            <h2 className="text-xl font-semibold mb-2">No vehicle assigned</h2>
            <p className="text-gray-600">You will see your alerts once a vehicle is assigned to you.</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="pt-16">
      <div className="p-6 bg-gray-100 min-h-screen">
        <h1 className="text-3xl font-bold mb-4">Alerts</h1>

        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
          <div className="bg-white p-4 rounded shadow">
            <h2 className="text-lg font-semibold">Total</h2>
            <p className="text-2xl">{alerts.length}</p>
          </div>
          <div className="bg-white p-4 rounded shadow">
            <h2 className="text-lg font-semibold">Critical</h2>
            <p className="text-2xl text-red-500">{alerts.filter(a => a.severity === 'CRITICAL').length}</p>
          </div>
          <div className="bg-white p-4 rounded shadow">
            <h2 className="text-lg font-semibold">Search</h2>
            <input value={searchTerm} onChange={(e)=>setSearchTerm(e.target.value)} className="border px-2 py-1 rounded w-full" placeholder="Search..." />
          </div>
          <div className="bg-white p-4 rounded shadow">
            <h2 className="text-lg font-semibold">Severity</h2>
            <select value={severityFilter} onChange={(e)=>setSeverityFilter(e.target.value)} className="border px-2 py-1 rounded w-full">
              <option value="">All</option>
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
              <option value="CRITICAL">Critical</option>
            </select>
          </div>
        </div>

        <div className="overflow-x-auto bg-white rounded shadow">
          <table className="w-full border-collapse">
            <thead>
              <tr className="bg-gray-200 text-left">
                <th className="p-3">Type</th>
                <th className="p-3">Car</th>
                <th className="p-3">Message</th>
                <th className="p-3">Severity</th>
                <th className="p-3">Timestamp</th>
              </tr>
            </thead>
            <tbody>
              {filteredAlerts.map((a) => (
                <tr key={a.id} className="border-b">
                  <td className="p-3 flex items-center gap-2">{getAlertIcon(a.type)} {a.type}</td>
                  <td className="p-3">{a.car?.id ?? '-'}</td>
                  <td className="p-3">{a.message}</td>
                  <td className="p-3"><span className={`px-2 py-1 rounded text-xs ${severityColor[a.severity] || 'bg-gray-100 text-gray-600'}`}>{a.severity}</span></td>
                  <td className="p-3">{a.timestamp}</td>
                </tr>
              ))}
              {filteredAlerts.length === 0 && (
                <tr><td className="p-3 text-gray-500" colSpan="5">No alerts found.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default AlertsPage;
