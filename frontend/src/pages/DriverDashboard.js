import React, { useEffect, useState } from "react";
import { Gauge, Fuel, Thermometer, MapPin, AlertTriangle } from "lucide-react";
import api from "../api/client";

const DriverDashboard = ({ user }) => {
  const [carId, setCarId] = useState(null);
  const [latest, setLatest] = useState(null);
  const [stats, setStats] = useState(null);
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchDriverCar = async () => {
    try {
      const dres = await api.get(`/drivers/user/${user.id}`);
      const d = dres?.data?.data;
      const assigned = d?.assignedCarId || null;
      setCarId(assigned);
      if (!assigned) {
        setLoading(false);
      }
    } catch (_) {
      setCarId(null);
      setLoading(false);
    }
  };

  const fetchData = async () => {
    if (!carId) return;
    try {
      // Latest telemetry entries for this car
      const tRes = await api.get(`/telemetry/car/${carId}/latest`);
      const list = tRes?.data?.data || [];
      setLatest(list[0] || null);

      // Stats for last 24 hours
      const end = new Date();
      const start = new Date(end.getTime() - 24 * 60 * 60 * 1000);
      const fmt = (d) => d.toISOString().slice(0, 19);
      const sRes = await api.get(`/telemetry/stats/car/${carId}`, {
        params: { startTime: fmt(start), endTime: fmt(end) },
      });
      setStats(sRes?.data?.data || null);

      // Alerts for this car
      const aRes = await api.get(`/alerts/car/${carId}`);
      setAlerts(aRes?.data?.data || []);
    } catch (e) {
      // ignore
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchDriverCar(); }, [user.id]);
  useEffect(() => { fetchData(); }, [carId]);
  useEffect(() => {
    const id = setInterval(fetchData, 10000);
    return () => clearInterval(id);
  }, [carId]);

  if (loading && carId === null) {
    return (
      <div className="pt-16">
        <div className="p-6 bg-gray-100 min-h-screen" />
      </div>
    );
  }

  if (!carId) {
    return (
      <div className="pt-16">
        <div className="p-6 bg-gray-100 min-h-screen flex items-center justify-center">
          <div className="bg-white p-6 rounded shadow text-center max-w-md">
            <h2 className="text-xl font-semibold mb-2">No vehicle assigned</h2>
            <p className="text-gray-600">You will see your dashboard data once a vehicle is assigned to you.</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="pt-16">
      <div className="p-6 bg-gray-100 min-h-screen">
        <div className="mb-6">
          <h1 className="text-3xl font-bold">Driver Dashboard</h1>
          <p className="text-gray-600">Vehicle ID: {carId}</p>
          {latest && (
            <p className="text-sm text-gray-500">Last updated: {new Date(latest.timestamp).toLocaleTimeString()}</p>
          )}
        </div>

        {/* Key metrics from stats */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
          <div className="bg-white p-4 rounded shadow flex flex-col">
            <div className="flex justify-between items-center mb-2">
              <h2 className="text-gray-600">Avg Speed (24h)</h2>
              <Gauge size={24} className="text-blue-500" />
            </div>
            <p className="text-2xl font-bold">{stats?.averageSpeed ?? 0} km/h</p>
            <p className="text-sm text-gray-500">Min {stats?.minSpeed ?? 0} • Max {stats?.maxSpeed ?? 0}</p>
          </div>

          <div className="bg-white p-4 rounded shadow flex flex-col">
            <div className="flex justify-between items-center mb-2">
              <h2 className="text-gray-600">Avg Fuel (24h)</h2>
              <Fuel size={24} className="text-green-500" />
            </div>
            <p className="text-2xl font-bold">{stats?.averageFuel ?? 0}%</p>
            <p className="text-sm text-gray-500">Min {stats?.minFuel ?? 0}% • Max {stats?.maxFuel ?? 0}%</p>
          </div>

          <div className="bg-white p-4 rounded shadow flex flex-col">
            <div className="flex justify-between items-center mb-2">
              <h2 className="text-gray-600">Avg Temp (24h)</h2>
              <Thermometer size={24} className="text-orange-500" />
            </div>
            <p className="text-2xl font-bold">{stats?.averageTemperature ?? 0}°C</p>
            <p className="text-sm text-gray-500">Min {stats?.minTemperature ?? 0}°C • Max {stats?.maxTemperature ?? 0}°C</p>
          </div>

          <div className="bg-white p-4 rounded shadow flex flex-col">
            <div className="flex justify-between items-center mb-2">
              <h2 className="text-gray-600">Active Alerts (24h)</h2>
              <AlertTriangle size={24} className="text-red-500" />
            </div>
            <p className="text-2xl font-bold text-red-600">{alerts?.length ?? 0}</p>
            <p className="text-sm text-gray-500">Recent fuel/temp/speed issues</p>
          </div>
        </div>

        {/* Latest snapshot */}
        {latest && (
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
            <div className="bg-white p-4 rounded shadow">
              <h3 className="text-gray-600 mb-1">Current Speed</h3>
              <p className="text-2xl font-bold">{latest.speed} km/h</p>
            </div>
            <div className="bg-white p-4 rounded shadow">
              <h3 className="text-gray-600 mb-1">Fuel Level</h3>
              <p className="text-2xl font-bold">{latest.fuelLevel}%</p>
            </div>
            <div className="bg-white p-4 rounded shadow">
              <h3 className="text-gray-600 mb-1">Engine Temp</h3>
              <p className="text-2xl font-bold">{latest.temperature}°C</p>
            </div>
            <div className="bg-white p-4 rounded shadow">
              <h3 className="text-gray-600 mb-1">Location</h3>
              <p className="font-medium flex items-center gap-1"><MapPin size={16} /> {latest.location}</p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default DriverDashboard;
