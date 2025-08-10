import React, { useEffect, useState } from "react";
import {
    Gauge,
    Fuel,
    Thermometer,
    MapPin,
    AlertTriangle,
    Car,
    Timer,
    TrendingUp
} from "lucide-react";
import api from "../api/client";

const DriverDashboard = ({ user }) => {
    const [telemetry, setTelemetry] = useState({
        speed: 0,
        fuelLevel: 100,
        temperature: 90,
        location: "-",
        lastUpdate: new Date().toISOString(),
    });
    const [carId, setCarId] = useState(null);
    const [alerts, setAlerts] = useState([]);
    const [dailyStats, setDailyStats] = useState({ distance: 0, driveTime: 0, avgSpeed: 0 });

    const fetchDriverAndTelemetry = async () => {
        try {
            if (!carId) {
                const dres = await api.get(`/drivers/user/${user.id}`);
                const dd = dres?.data?.data;
                if (dd?.assignedCarId) setCarId(dd.assignedCarId);
            }
            const id = carId || null;
            if (id) {
                const tRes = await api.get(`/telemetry/car/${id}/latest`);
                const list = tRes?.data?.data || [];
                const latest = list[0] || null;
                if (latest) {
                    setTelemetry({
                        speed: latest.speed ?? 0,
                        fuelLevel: latest.fuelLevel ?? 0,
                        temperature: latest.temperature ?? 0,
                        location: latest.location || "-",
                        lastUpdate: latest.timestamp || new Date().toISOString(),
                    });
                    const a = [];
                    if ((latest.fuelLevel ?? 0) < 25) a.push({ message: "Low Fuel Level", severity: (latest.fuelLevel ?? 0) < 10 ? "critical" : "high" });
                    if ((latest.temperature ?? 0) > 100) a.push({ message: "High Engine Temperature", severity: (latest.temperature ?? 0) > 110 ? "critical" : "high" });
                    setAlerts(a);
                    setDailyStats((prev) => ({
                        ...prev,
                        distance: parseFloat((prev.distance + (latest.speed ?? 0) / 360).toFixed(2)),
                        driveTime: prev.driveTime + ((latest.speed ?? 0) > 0 ? 10 : 0),
                        avgSpeed: parseFloat(((prev.avgSpeed + (latest.speed ?? 0)) / 2).toFixed(1)),
                    }));
                }
            }
        } catch (e) {
            // ignore
        }
    };

    useEffect(() => { fetchDriverAndTelemetry(); }, []);
    useEffect(() => {
        const interval = setInterval(fetchDriverAndTelemetry, 10000);
        return () => clearInterval(interval);
    }, [carId]);

    const formatDriveTime = (seconds) => {
        const mins = Math.floor(seconds / 60);
        return `${mins} mins`;
    };

    return (
        <div className="pt-16">
        <div className="p-6 bg-gray-100 min-h-screen">
            <div className="mb-6">
                <h1 className="text-3xl font-bold">Driver Dashboard</h1>
                <p className="text-gray-600">Vehicle ID: {carId ?? '-'}</p>
                <p className="text-sm text-gray-500">Last updated: {new Date(telemetry.lastUpdate).toLocaleTimeString()}</p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
                <div className="bg-white p-4 rounded shadow flex flex-col">
                    <div className="flex justify-between items-center mb-2">
                        <h2 className="text-gray-600">Current Speed</h2>
                        <Gauge size={24} className="text-blue-500" />
                    </div>
                    <p className="text-2xl font-bold">{telemetry.speed} km/h</p>
                    <p className="text-sm text-gray-500">Speed limit: 120 km/h</p>
                </div>

                <div className="bg-white p-4 rounded shadow flex flex-col">
                    <div className="flex justify-between items-center mb-2">
                        <h2 className="text-gray-600">Fuel Level</h2>
                        <Fuel size={24} className="text-green-500" />
                    </div>
                    <p className={`text-2xl font-bold ${telemetry.fuelLevel > 50 ? "text-green-600" : telemetry.fuelLevel > 25 ? "text-yellow-600" : "text-red-600"}`}>
                        {telemetry.fuelLevel}%
                    </p>
                    <div className="w-full bg-gray-200 rounded h-2 mt-2">
                        <div className={`${telemetry.fuelLevel > 50 ? "bg-green-500" : telemetry.fuelLevel > 25 ? "bg-yellow-500" : "bg-red-500"}`} style={{ width: `${telemetry.fuelLevel}%`, height: 8 }}></div>
                    </div>
                    <p className="text-sm text-gray-500">Min. Fuel limit: 25%</p>
                </div>

                <div className="bg-white p-4 rounded shadow flex flex-col">
                    <div className="flex justify-between items-center mb-2">
                        <h2 className="text-gray-600">Engine Temp</h2>
                        <Thermometer size={24} className="text-orange-500" />
                    </div>
                    <p className={`text-2xl font-bold ${telemetry.temperature <= 95 ? "text-green-600" : telemetry.temperature <= 100 ? "text-yellow-600" : "text-red-600"}`}>
                        {telemetry.temperature}°C
                    </p>
                    <p className="text-sm text-gray-500">Normal: 80-100°C</p>
                </div>

                <div className="bg-white p-4 rounded shadow flex flex-col">
                    <div className="flex justify-between items-center mb-2">
                        <h2 className="text-gray-600">Location</h2>
                        <MapPin size={24} className="text-purple-500" />
                    </div>
                    <p className="text-sm font-semibold truncate">{telemetry.location}</p>
                </div>
            </div>

            <div className="bg-white p-4 rounded shadow mb-6">
                <div className="flex items-center mb-4">
                    <AlertTriangle className="text-red-500 mr-2" size={20} />
                    <h3 className="text-lg font-semibold">Active Alerts</h3>
                </div>
                {alerts.length > 0 ? (
                    <ul className="space-y-2">
                        {alerts.map((alert, idx) => (
                            <li key={idx} className="flex justify-between items-center p-2 border rounded">
                                <span>{alert.message}</span>
                                <span className={`text-xs px-2 py-1 rounded ${alert.severity === "critical" ? "bg-red-100 text-red-600" : "bg-yellow-100 text-yellow-600"}`}>
                                    {alert.severity}
                                </span>
                            </li>
                        ))}
                    </ul>
                ) : (
                    <p className="text-gray-500">No active alerts at this time.</p>
                )}
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="bg-white p-4 rounded shadow flex flex-col">
                    <div className="flex justify-between items-center mb-2">
                        <h2 className="text-gray-600">Today's Distance</h2>
                        <Car size={24} className="text-blue-500" />
                    </div>
                    <p className="text-2xl font-bold">{dailyStats.distance} km</p>
                    <p className="text-green-600 flex items-center text-sm">
                        <TrendingUp size={16} className="mr-1" /> +12% from yesterday
                    </p>
                </div>

                <div className="bg-white p-4 rounded shadow flex flex-col">
                    <div className="flex justify-between items-center mb-2">
                        <h2 className="text-gray-600">Drive Time</h2>
                        <Timer size={24} className="text-purple-500" />
                    </div>
                    <p className="text-2xl font-bold">{formatDriveTime(dailyStats.driveTime)}</p>
                    <p className="text-sm text-gray-500">Includes stops</p>
                </div>

                <div className="bg-white p-4 rounded shadow flex flex-col">
                    <div className="flex justify-between items-center mb-2">
                        <h2 className="text-gray-600">Avg Speed</h2>
                        <Gauge size={24} className="text-orange-500" />
                    </div>
                    <p className="text-2xl font-bold">{dailyStats.avgSpeed} km/h</p>
                    <p className="text-sm text-gray-500">Overall average today</p>
                </div>
            </div>
        </div>
    </div>
    );
};

export default DriverDashboard;
