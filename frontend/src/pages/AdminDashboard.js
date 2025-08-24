import React, { useState, useEffect, useMemo } from "react";
import { FaMapMarkerAlt, FaGasPump, FaThermometerHalf } from "react-icons/fa";
import { Bell } from "lucide-react";
import api from "../api/client";

const AdminDashboard = () => {
    const [vehicles, setVehicles] = useState([]);
    const [driversByCarId, setDriversByCarId] = useState({});
    const [searchTerm, setSearchTerm] = useState("");
    const [autoRefresh, setAutoRefresh] = useState(true);
    const [alertCounts, setAlertCounts] = useState({ totalAlerts: 0, unacknowledgedAlerts: 0, criticalAlerts: 0 });
    const [notifOpen, setNotifOpen] = useState(false);
    const [criticalUnack, setCriticalUnack] = useState([]);

    const fetchData = async () => {
        try {
            const [telemetryRes, driversRes, alertsStatsRes, carsRes] = await Promise.all([
                api.get("/telemetry/latest/all"),
                api.get("/drivers/assigned"),
                api.get("/alerts/stats/count"),
                api.get("/cars"),
            ]);

            const telemetryList = telemetryRes?.data?.data || [];
            // Only include cars that are active in the database
            const activeCars = carsRes?.data?.data || [];
            const activeCarIdSet = new Set((activeCars || []).map((c) => c.id));
            const activeTelemetry = telemetryList.filter((t) => activeCarIdSet.has(t.carId));
            const drivers = (driversRes?.data?.data || []).reduce((acc, d) => {
                if (d.assignedCarId) acc[d.assignedCarId] = d.name || d.username;
                return acc;
            }, {});
            const counts = alertsStatsRes?.data?.data || { totalAlerts: 0, unacknowledgedAlerts: 0, criticalAlerts: 0 };

            const carStatusById = (activeCars || []).reduce((acc, c) => {
                acc[c.id] = c.status || "";
                return acc;
            }, {});

            const rows = activeTelemetry.map(t => ({
                id: t.carId,
                driver: drivers[t.carId] || "-",
                location: t.location || "-",
                speed: t.speed ?? 0,
                fuel: t.fuelLevel ?? 0,
                temp: t.temperature ?? 0,
                status: carStatusById[t.carId] || "-",
                lastUpdate: t.timestamp || "-",
            }));

            setVehicles(rows);
            setDriversByCarId(drivers);
            setAlertCounts(counts);
        } catch (e) {
            // silently ignore for dashboard
        }
    };

    useEffect(() => { fetchData(); }, []);

    // Load unacknowledged critical alerts for notifications
    useEffect(() => {
        const loadCritical = async () => {
            try {
                const res = await api.get('/alerts/critical');
                const list = (res?.data?.data || []).filter(a => !a.acknowledged);
                setCriticalUnack(list);
            } catch (_) {
                setCriticalUnack([]);
            }
        };
        loadCritical();
        const id = setInterval(loadCritical, 30000);
        return () => clearInterval(id);
    }, []);

    const acknowledgeAlert = async (id) => {
        try {
            await api.put(`/alerts/${id}/acknowledge`);
            setCriticalUnack((prev) => prev.filter((a) => a.id !== id));
        } catch (_) {}
    };

    useEffect(() => {
        if (!autoRefresh) return;
        const interval = setInterval(fetchData, 30000);
        return () => clearInterval(interval);
    }, [autoRefresh]);

    const filteredVehicles = useMemo(() => {
        const q = (searchTerm || "").toLowerCase().trim();
        if (!q) return vehicles;
        return vehicles.filter((v) => {
            const idStr = String(v.id || "").toLowerCase();
            const driverStr = String(v.driver || "").toLowerCase();
            const locationStr = String(v.location || "").toLowerCase();
            return idStr.includes(q) || driverStr.includes(q) || locationStr.includes(q);
        });
    }, [vehicles, searchTerm]);

    const activeCount = vehicles.filter((v) => String(v.status || "").toLowerCase() === "active").length;
    const idleCount = vehicles.filter((v) => String(v.status || "").toLowerCase() === "idle").length;
    const maintenanceCount = vehicles.filter((v) => String(v.status || "").toLowerCase() === "maintenance").length;

    const getFuelColor = (fuel) => {
        if (fuel > 50) return "green";
        if (fuel > 25) return "orange";
        return "red";
    };

    const getTempColor = (temp) => {
        if (temp <= 95) return "green";
        if (temp <= 100) return "orange";
        return "red";
    };

    return (
        <div className="pt-16">
        <div className="p-6 bg-gray-100 min-h-screen">
            <h1 className="text-3xl font-bold mb-4">Admin Dashboard</h1>

            <div className="grid grid-cols-4 gap-4 mb-6">
                <div className="bg-white p-4 rounded shadow">
                    <h2 className="text-lg font-bold">Active Vehicles</h2>
                    <p className="text-2xl">{activeCount}</p>
                </div>
                <div className="bg-white p-4 rounded shadow">
                    <h2 className="text-lg font-bold">Idle Vehicles</h2>
                    <p className="text-2xl">{idleCount}</p>
                </div>
                <div className="bg-white p-4 rounded shadow">
                    <h2 className="text-lg font-bold">Under Maintenance</h2>
                    <p className="text-2xl">{maintenanceCount}</p>
                </div>
                <div className="bg-white p-4 rounded shadow">
                    <h2 className="text-lg font-bold">Alerts</h2>
                    <p className="text-2xl text-red-500">{alertCounts.totalAlerts}</p>
                </div>
            </div>

            <div className="flex justify-between items-center mb-6 relative">
                <input
                    type="text"
                    placeholder="Search by ID, driver or location"
                    className="border px-4 py-2 rounded w-1/2"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                />
                <div className="flex items-center gap-4">
                    <div className="text-sm text-gray-600">Auto refresh every 30s</div>
                    <button className="relative" onClick={() => setNotifOpen(o => !o)}>
                        <Bell size={20} />
                        {criticalUnack.length > 0 && (
                            <span className="absolute -top-1 -right-1 bg-red-600 text-white text-[10px] rounded-full px-1">{criticalUnack.length}</span>
                        )}
                    </button>
                    {notifOpen && (
                        <div className="absolute right-0 top-10 w-96 bg-white rounded shadow-lg border z-10">
                            <div className="px-3 py-2 border-b font-semibold">Critical Alerts</div>
                            <div className="max-h-80 overflow-y-auto">
                                {criticalUnack.length === 0 ? (
                                    <div className="p-3 text-sm text-gray-500">No critical unacknowledged alerts.</div>
                                ) : (
                                    criticalUnack.map((a) => (
                                        <div key={a.id} className="p-3 border-b text-sm flex items-start justify-between gap-2">
                                            <div>
                                                <div className="font-medium">Car {a.carId} • {a.type}</div>
                                                <div className="text-gray-600 text-xs">{a.timestamp}</div>
                                            </div>
                                            <button
                                                className="px-2 py-1 text-xs bg-green-600 text-white rounded"
                                                onClick={() => acknowledgeAlert(a.id)}
                                            >Acknowledge</button>
                                        </div>
                                    ))
                                )}
                            </div>
                        </div>
                    )}
                </div>
            </div>

            <table className="w-full bg-white shadow rounded">
                <thead>
                    <tr className="bg-gray-200 text-left">
                        <th className="p-3">Car ID</th>
                        <th className="p-3">Driver</th>
                        <th className="p-3">Location</th>
                        <th className="p-3">Speed (km/h)</th>
                        <th className="p-3">Fuel Level</th>
                        <th className="p-3">Engine Temp</th>
                        <th className="p-3">Status</th>
                        <th className="p-3">Last Update</th>
                    </tr>
                </thead>
                <tbody>
                    {filteredVehicles.map((v, i) => (
                        <tr key={i} className="border-b">
                            <td className="p-3">{v.id}</td>
                            <td className="p-3">{v.driver}</td>
                            <td className="p-3 flex items-center">
                                <FaMapMarkerAlt className="text-blue-500 mr-2" />
                                {v.location}
                            </td>
                            <td className="p-3">{v.speed}</td>
                            <td className="p-3 items-center">
                                <FaGasPump className="mr-2" />
                                <span style={{ color: getFuelColor(v.fuel) }}>{v.fuel}%</span>
                            </td>
                            <td className="p-3 items-center">
                                <FaThermometerHalf className="mr-2" />
                                <span style={{ color: getTempColor(v.temp) }}>{v.temp}°C</span>
                            </td>
                            <td className="p-3">
                                {(() => { const s = String(v.status || "").toLowerCase(); return (
                                    <span className={`px-2 py-1 rounded text-white ${s === "active" ? "bg-green-500"
                                        : s === "idle" ? "bg-yellow-500"
                                            : s === "maintenance" ? "bg-orange-500"
                                                : "bg-red-500"
                                        }`}>
                                        {v.status}
                                    </span>
                                ); })()}
                            </td>
                            <td className="p-3">{v.lastUpdate}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    </div> 
    );
};

export default AdminDashboard;
