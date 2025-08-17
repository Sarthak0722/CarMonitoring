import React, { useState, useEffect, useMemo } from "react";
import { AlertTriangle, Fuel, Thermometer, Wrench, Zap, ChevronDown, ChevronRight, Clock, Car as CarIcon, Search } from "lucide-react";
import { Link } from "react-router-dom";
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

const THRESHOLDS = { speed: 100, fuel: 20, temperature: 100 };
const compactCause = (type, valueStr) => {
  const t = (type || "").toLowerCase();
  const num = parseFloat((valueStr || "").replace(/[^0-9.\-]/g, ""));
  if (t.includes("speed")) return `Speed ${isNaN(num) ? valueStr : num} > ${THRESHOLDS.speed} (overspeed)`;
  if (t.includes("fuel")) return `Fuel ${isNaN(num) ? valueStr : num}% < ${THRESHOLDS.fuel}% (low fuel)`;
  if (t.includes("temp")) return `Temp ${isNaN(num) ? valueStr : num}°C > ${THRESHOLDS.temperature}°C (overheat)`;
  return "Threshold exceeded";
};

const AlertsPage = ({ user }) => {
  const [alerts, setAlerts] = useState([]);
  const [carId, setCarId] = useState(null);
  const [telemetryWindow, setTelemetryWindow] = useState([]);
  const [page, setPage] = useState(1);
  const pageSize = 8;

  // Admin-specific state
  const [alertStats, setAlertStats] = useState({ totalAlerts: 0, unacknowledgedAlerts: 0, criticalAlerts: 0 });
  const [severityStats, setSeverityStats] = useState({ lowAlerts: 0, mediumAlerts: 0, highAlerts: 0, criticalAlerts: 0 });
  const [recentDetailed, setRecentDetailed] = useState([]);
  const [alertsByCar, setAlertsByCar] = useState({});
  const [expandedCars, setExpandedCars] = useState({});
  const [driversByCarId, setDriversByCarId] = useState({});
  const [carSearch, setCarSearch] = useState("");
  const [carPage, setCarPage] = useState(1);
  const carPageSize = 8;

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
        const arr = (res?.data?.data || []).sort((a,b) => new Date(b.timestamp) - new Date(a.timestamp));
        setAlerts(arr);
        // group by car for car-wise section
        const grouped = arr.reduce((acc, a) => {
          const cid = a.carId ?? a.car?.id;
          if (!cid) return acc;
          if (!acc[cid]) acc[cid] = [];
          acc[cid].push(a);
          return acc;
        }, {});
        setAlertsByCar(grouped);
      }
      setPage(1);
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

  // Admin: fetch stats
  const loadAdminStats = async () => {
    if (user.role === 'DRIVER') return;
    try {
      const [countRes, sevRes] = await Promise.all([
        api.get('/alerts/stats/count'),
        api.get('/alerts/stats/severity'),
      ]);
      setAlertStats(countRes?.data?.data || { totalAlerts: 0, unacknowledgedAlerts: 0, criticalAlerts: 0 });
      setSeverityStats(sevRes?.data?.data || { lowAlerts: 0, mediumAlerts: 0, highAlerts: 0, criticalAlerts: 0 });
    } catch (_) {}
  };

  // Admin: fetch drivers to map carId -> driver details
  const loadAssignedDrivers = async () => {
    if (user.role === 'DRIVER') return;
    try {
      const dRes = await api.get('/drivers/assigned');
      const list = dRes?.data?.data || [];
      const map = {};
      for (const d of list) {
        if (d.assignedCarId) map[d.assignedCarId] = { id: d.id, name: d.name || d.username || "", username: d.username || "" };
      }
      setDriversByCarId(map);
    } catch (_) {
      setDriversByCarId({});
    }
  };

  useEffect(() => { loadDriverCar(); }, [user.id, user.role]);
  useEffect(() => { loadAlerts(); }, [carId, user.role]);
  useEffect(() => { loadTelemetryWindow(); }, [carId]);
  useEffect(() => { loadAdminStats(); }, [user.role]);
  useEffect(() => { loadAssignedDrivers(); }, [user.role]);

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

  // Admin: for recent 5 alerts, fetch nearby telemetry to compute cause strings
  useEffect(() => {
    const run = async () => {
      if (user.role === 'DRIVER') { setRecentDetailed([]); return; }
      const top5 = (alerts || []).slice(0, 5);
      const results = [];
      for (const a of top5) {
        try {
          const ts = new Date(a.timestamp);
          const start = new Date(ts.getTime() - 10 * 60 * 1000);
          const end = new Date(ts.getTime() + 10 * 60 * 1000);
          const tRes = await api.get(`/telemetry/car/${a.carId}/range`, { params: { startTime: formatLocalDateTime(start), endTime: formatLocalDateTime(end) } });
          const tList = tRes?.data?.data || [];
          let best = null, bestDiff = Number.MAX_SAFE_INTEGER;
          for (const rec of tList) {
            const diff = Math.abs(new Date(rec.timestamp).getTime() - ts.getTime());
            if (diff < bestDiff) { best = rec; bestDiff = diff; }
          }
          let valueStr = null;
          const type = (a.type || '').toLowerCase();
          if (best) {
            if (type.includes('fuel')) valueStr = `${best.fuelLevel}%`;
            else if (type.includes('temp')) valueStr = `${best.temperature}°C`;
            else if (type.includes('speed')) valueStr = `${best.speed} km/h`;
          }
          results.push({ ...a, derivedValue: valueStr, cause: compactCause(a.type, valueStr) });
        } catch (_) {
          results.push({ ...a, derivedValue: null, cause: compactCause(a.type, null) });
        }
      }
      setRecentDetailed(results);
    };
    run();
  }, [alerts, user.role]);

  const pagedAlerts = useMemo(() => {
    const start = (page - 1) * pageSize;
    return alerts.slice(start, start + pageSize);
  }, [alerts, page]);

  const totalPages = Math.max(1, Math.ceil(alerts.length / pageSize));

  // Admin: compute car-wise filtered and paginated entries
  const carEntries = useMemo(() => Object.entries(alertsByCar), [alertsByCar]);
  const filteredCarEntries = useMemo(() => {
    const q = (carSearch || '').toLowerCase().trim();
    if (!q) return carEntries;
    return carEntries.filter(([cid]) => {
      const driver = driversByCarId[cid];
      const cidStr = String(cid).toLowerCase();
      const dName = String(driver?.name || '').toLowerCase();
      const dUser = String(driver?.username || '').toLowerCase();
      const dId = String(driver?.id || '').toLowerCase();
      return cidStr.includes(q) || dName.includes(q) || dUser.includes(q) || dId.includes(q);
    });
  }, [carEntries, carSearch, driversByCarId]);
  const carTotalPages = Math.max(1, Math.ceil(filteredCarEntries.length / carPageSize));
  const pagedCarEntries = filteredCarEntries.slice((carPage - 1) * carPageSize, (carPage - 1) * carPageSize + carPageSize);

  useEffect(() => { setCarPage(1); }, [carSearch, alertsByCar, driversByCarId]);

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
                {pagedAlerts.map((a) => {
                  const v = deriveValueForAlert(a);
                  const cause = compactCause(a.type, v);
                  return (
                    <tr key={a.id} className="border-b">
                      <td className="p-3 flex items-center gap-2">{getAlertIcon(a.type)} {a.type}</td>
                      <td className="p-3">{cause}</td>
                      <td className="p-3">{v || '-'}</td>
                      <td className="p-3"><span className={`px-2 py-1 rounded text-xs ${severityColor[a.severity] || 'bg-gray-100 text-gray-600'}`}>{a.severity}</span></td>
                      <td className="p-3">{a.timestamp}</td>
                    </tr>
                  );
                })}
                {pagedAlerts.length === 0 && (
                  <tr><td className="p-3 text-gray-500" colSpan="5">No alerts found.</td></tr>
                )}
              </tbody>
            </table>
          </div>

          <div className="flex items-center justify-center gap-2 mt-3">
            <button className="px-3 py-1 border rounded disabled:opacity-50" onClick={() => setPage(p => Math.max(1, p-1))} disabled={page === 1}>Prev</button>
            <span className="text-sm">Page {page} of {totalPages}</span>
            <button className="px-3 py-1 border rounded disabled:opacity-50" onClick={() => setPage(p => Math.min(totalPages, p+1))} disabled={page === totalPages}>Next</button>
          </div>
        </div>
      </div>
    );
  }

  // Admin redesigned view
  return (
    <div className="pt-16">
      <div className="p-6 bg-gray-100 min-h-screen">
        <h1 className="text-3xl font-bold mb-4">Alerts Overview</h1>

        {/* Summary cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-3 mb-4">
          <div className="bg-white p-4 rounded shadow"><div className="text-gray-600">Total Alerts</div><div className="text-2xl font-bold">{alertStats.totalAlerts}</div></div>
          <div className="bg-white p-4 rounded shadow"><div className="text-gray-600">Unacknowledged</div><div className="text-2xl font-bold text-orange-600">{alertStats.unacknowledgedAlerts}</div></div>
          <div className="bg-white p-4 rounded shadow"><div className="text-gray-600">Critical</div><div className="text-2xl font-bold text-red-600">{alertStats.criticalAlerts}</div></div>
          <div className="bg-white p-4 rounded shadow">
            <div className="text-gray-600">By Severity</div>
            <div className="mt-1 text-sm grid grid-cols-2 gap-x-2">
              <div><span className="inline-block w-2 h-2 bg-green-500 mr-2 rounded-full"></span>Low: {severityStats.lowAlerts}</div>
              <div><span className="inline-block w-2 h-2 bg-yellow-500 mr-2 rounded-full"></span>Medium: {severityStats.mediumAlerts}</div>
              <div><span className="inline-block w-2 h-2 bg-orange-500 mr-2 rounded-full"></span>High: {severityStats.highAlerts}</div>
              <div><span className="inline-block w-2 h-2 bg-red-500 mr-2 rounded-full"></span>Critical: {severityStats.criticalAlerts}</div>
            </div>
          </div>
        </div>

        {/* Recent 5 alerts */}
        <div className="bg-white p-4 rounded shadow mb-4">
          <div className="flex items-center justify-between mb-2">
            <h2 className="text-lg font-semibold flex items-center gap-2"><AlertTriangle /> Recent Alerts</h2>
            <span className="text-sm text-gray-500">Last 5</span>
          </div>
          {recentDetailed.length === 0 ? (
            <p className="text-gray-500 text-sm">No recent alerts.</p>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-5 gap-3">
              {recentDetailed.map((a) => (
                <div key={a.id} className="border rounded p-3">
                  <div className="text-sm text-gray-600 flex items-center gap-2"><CarIcon size={14} /> Car {a.carId}</div>
                  <div className="font-semibold mt-1 flex items-center gap-2">{getAlertIcon(a.type)} {a.type}</div>
                  <div className="text-gray-600 text-sm mt-1">{a.cause || compactCause(a.type, a.derivedValue)}</div>
                  <div className="flex items-center justify-between mt-2">
                    <span className={`px-2 py-1 rounded text-xs ${severityColor[a.severity] || 'bg-gray-100 text-gray-600'}`}>{a.severity}</span>
                    <span className="text-xs text-gray-500 flex items-center gap-1"><Clock size={12} /> {a.timestamp}</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Car-wise alerts accordion with search and pagination */}
        <div className="bg-white p-4 rounded shadow">
          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-3 mb-2">
            <h2 className="text-lg font-semibold flex items-center gap-2">Car-wise Alerts</h2>
            <div className="flex items-center gap-2">
              <div className="relative">
                <Search size={16} className="absolute left-2 top-1/2 -translate-y-1/2 text-gray-500" />
                <input
                  className="border px-7 py-2 rounded w-64"
                  placeholder="Search car ID or driver"
                  value={carSearch}
                  onChange={(e) => setCarSearch(e.target.value)}
                />
              </div>
              <div className="flex items-center gap-2">
                <button className="px-3 py-1 border rounded disabled:opacity-50" onClick={() => setCarPage(p => Math.max(1, p-1))} disabled={carPage === 1}>Prev</button>
                <span className="text-sm">{carPage} / {carTotalPages}</span>
                <button className="px-3 py-1 border rounded disabled:opacity-50" onClick={() => setCarPage(p => Math.min(carTotalPages, p+1))} disabled={carPage === carTotalPages}>Next</button>
              </div>
            </div>
          </div>
          <div className="divide-y">
            {pagedCarEntries.map(([cid, list]) => {
              const open = !!expandedCars[cid];
              const toggle = () => setExpandedCars((s) => ({ ...s, [cid]: !s[cid] }));
              const latest5 = list.slice(0, 5);
              const d = driversByCarId[cid];
              return (
                <div key={cid} className="py-2">
                  <div className="w-full flex items-center justify-between">
                    <button className="flex items-center gap-2" onClick={toggle}>
                      {open ? <ChevronDown size={16} /> : <ChevronRight size={16} />}
                      <span className="font-medium">Car {cid}</span>
                      {d && <span className="text-xs text-gray-500">• Driver {d.id}{d.name ? ` (${d.name})` : ''}</span>}
                      <span className="text-xs text-gray-500">({list.length} alerts)</span>
                    </button>
                    <Link to={`/alerts/car/${cid}`} className="text-blue-600 text-sm hover:underline">View</Link>
                  </div>
                  {open && (
                    <div className="mt-2 grid grid-cols-1 md:grid-cols-2 gap-2">
                      {latest5.map((a) => (
                        <div key={a.id} className="border rounded p-2 flex items-center justify-between">
                          <div>
                            <div className="text-sm font-medium flex items-center gap-2">{getAlertIcon(a.type)} {a.type}</div>
                            <div className="text-xs text-gray-600">{compactCause(a.type, null)}</div>
                          </div>
                          <div className="text-right">
                            <div><span className={`px-2 py-0.5 rounded text-[10px] ${severityColor[a.severity] || 'bg-gray-100 text-gray-600'}`}>{a.severity}</span></div>
                            <div className="text-[10px] text-gray-500 mt-1">{a.timestamp}</div>
                          </div>
                        </div>
                      ))}
                      {list.length > 5 && (
                        <div className="text-xs text-gray-600 p-2">And {list.length - 5} more…</div>
                      )}
                    </div>
                  )}
                </div>
              );
            })}
            {pagedCarEntries.length === 0 && (
              <div className="text-gray-500 text-sm">No cars match your search.</div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default AlertsPage;
