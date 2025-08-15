import React, { useState, useEffect, useMemo } from "react";
import { AlertTriangle, Fuel, Thermometer, Wrench, Zap } from "lucide-react";
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

const AlertsPage = ({ user }) => {
  const [alerts, setAlerts] = useState([]);
  const [carId, setCarId] = useState(null);
  const [telemetryWindow, setTelemetryWindow] = useState([]);

  const loadDriverCar = async () => {
    if (user.role !== 'DRIVER') return;
    try {
      const dres = await api.get(`/drivers/user/${user.id}`);
      const d = dres?.data?.data;
      setCarId(d?.assignedCarId || null);
    } catch (_) { setCarId(null); }
  };

  const loadAlerts = async () => {
    try {
      if (user.role === 'DRIVER') {
        if (!carId) return;
        const res = await api.get(`/alerts/car/${carId}`);
        const arr = (res?.data?.data || []).sort((a,b) => new Date(b.timestamp) - new Date(a.timestamp));
        setAlerts(arr);
      } else {
        const res = await api.get("/alerts");
        setAlerts(res?.data?.data || []);
      }
    } catch (_) {}
  };

  const loadTelemetryWindow = async () => {
    if (user.role !== 'DRIVER' || !carId) { setTelemetryWindow([]); return; }
    try {
      const end = new Date();
      const start = new Date(end.getTime() - 24 * 60 * 60 * 1000);
      const tRes = await api.get(`/telemetry/car/${carId}/range`, { params: { startTime: formatLocalDateTime(start), endTime: formatLocalDateTime(end) } });
      setTelemetryWindow(tRes?.data?.data || []);
    } catch (_) { setTelemetryWindow([]); }
  };

  useEffect(() => { loadDriverCar(); }, [user.id, user.role]);
  useEffect(() => { loadAlerts(); }, [carId, user.role]);
  useEffect(() => { loadTelemetryWindow(); }, [carId]);

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

  const countsByType = useMemo(() => {
    const c = { speed: 0, fuel: 0, temperature: 0, maintenance: 0, other: 0 };
    for (const a of alerts) {
      const t = (a.type || '').toLowerCase();
      if (t.includes('speed')) c.speed++;
      else if (t.includes('fuel')) c.fuel++;
      else if (t.includes('temp')) c.temperature++;
      else if (t.includes('maint')) c.maintenance++;
      else c.other++;
    }
    return c;
  }, [alerts]);

  const deriveValueForAlert = (a) => {
    if (!telemetryWindow.length) return null;
    const t = new Date(a.timestamp).getTime();
    let best = null, diff = Number.MAX_SAFE_INTEGER;
    for (const rec of telemetryWindow) {
      const d = Math.abs(new Date(rec.timestamp).getTime() - t);
      if (d < diff) { best = rec; diff = d; }
    }
    if (!best) return null;
    const type = (a.type || '').toLowerCase();
    if (type.includes('fuel')) return `${best.fuelLevel}%`;
    if (type.includes('temp')) return `${best.temperature}°C`;
    if (type.includes('speed')) return `${best.speed} km/h`;
    return null;
  };

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

  if (user.role === 'DRIVER') {
    return (
      <div className="pt-16">
        <div className="p-4 bg-gray-100 min-h-screen">
          <h1 className="text-2xl font-bold mb-3">Alerts</h1>
          <div className="grid grid-cols-2 md:grid-cols-5 gap-3 mb-4">
            <div className="bg-white p-3 rounded shadow"><div className="text-gray-600">Speed</div><div className="text-2xl font-bold">{countsByType.speed}</div></div>
            <div className="bg-white p-3 rounded shadow"><div className="text-gray-600">Fuel</div><div className="text-2xl font-bold">{countsByType.fuel}</div></div>
            <div className="bg-white p-3 rounded shadow"><div className="text-gray-600">Temperature</div><div className="text-2xl font-bold">{countsByType.temperature}</div></div>
            <div className="bg-white p-3 rounded shadow"><div className="text-gray-600">Maintenance</div><div className="text-2xl font-bold">{countsByType.maintenance}</div></div>
            <div className="bg-white p-3 rounded shadow"><div className="text-gray-600">Other</div><div className="text-2xl font-bold">{countsByType.other}</div></div>
          </div>

          <div className="overflow-x-auto bg-white rounded shadow">
            <table className="w-full border-collapse">
              <thead>
                <tr className="bg-gray-200 text-left">
                  <th className="p-3">Type</th>
                  <th className="p-3">Cause</th>
                  <th className="p-3">Value at event</th>
                  <th className="p-3">Severity</th>
                  <th className="p-3">Timestamp</th>
                </tr>
              </thead>
              <tbody>
                {alerts.map((a) => (
                  <tr key={a.id} className="border-b">
                    <td className="p-3 flex items-center gap-2">{getAlertIcon(a.type)} {a.type}</td>
                    <td className="p-3">{a.message || '-'}</td>
                    <td className="p-3">{deriveValueForAlert(a) || '-'}</td>
                    <td className="p-3"><span className={`px-2 py-1 rounded text-xs ${severityColor[a.severity] || 'bg-gray-100 text-gray-600'}`}>{a.severity}</span></td>
                    <td className="p-3">{a.timestamp}</td>
                  </tr>
                ))}
                {alerts.length === 0 && (
                  <tr><td className="p-3 text-gray-500" colSpan="5">No alerts found.</td></tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    );
  }

  // Admin or others: keep previous simple list
  return (
    <div className="pt-16">
      <div className="p-6 bg-gray-100 min-h-screen">
        <h1 className="text-3xl font-bold mb-4">Alerts</h1>
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
              {alerts.map((a) => (
                <tr key={a.id} className="border-b">
                  <td className="p-3 flex items-center gap-2">{getAlertIcon(a.type)} {a.type}</td>
                  <td className="p-3">{a.car?.id ?? '-'}</td>
                  <td className="p-3">{a.message}</td>
                  <td className="p-3"><span className={`px-2 py-1 rounded text-xs ${severityColor[a.severity] || 'bg-gray-100 text-gray-600'}`}>{a.severity}</span></td>
                  <td className="p-3">{a.timestamp}</td>
                </tr>
              ))}
              {alerts.length === 0 && (
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
