import React, { useEffect, useMemo, useState } from "react";
import {
    LineChart,
    Line,
    BarChart,
    Bar,
    PieChart,
    Pie,
    Cell,
    Tooltip,
    XAxis,
    YAxis,
    CartesianGrid,
    Legend,
    ResponsiveContainer
} from "recharts";
import { AlertTriangle, Gauge, Fuel, Thermometer } from "lucide-react";
import api from "../api/client";

const COLORS = ["#0088FE", "#FF8042", "#00C49F", "#FFBB28", "#AA336A"];

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

const getRange = (rangeKey) => {
    const end = new Date();
    const start = new Date(end.getTime());
    if (rangeKey === "24h") start.setHours(end.getHours() - 24);
    else if (rangeKey === "7d") start.setDate(end.getDate() - 7);
    else if (rangeKey === "30d") start.setDate(end.getDate() - 30);
    else start.setDate(end.getDate() - 7);
    return { start, end };
};

const AnalyticsPage = () => {
    const [timeRange, setTimeRange] = useState("7d");
    const [cars, setCars] = useState([]);
    const [selectedCarId, setSelectedCarId] = useState("ALL");

    const [alertsCounts, setAlertsCounts] = useState({ totalAlerts: 0, unacknowledgedAlerts: 0, criticalAlerts: 0 });
    const [alertsAll, setAlertsAll] = useState([]);

    const [latestAll, setLatestAll] = useState([]);
    const [carTelemetry, setCarTelemetry] = useState([]);
    const [carStats, setCarStats] = useState(null);

    // Load base data: active cars, alert stats, recent alerts, latest telemetry snapshot
    useEffect(() => {
        const loadBase = async () => {
            try {
                const [carsRes, countRes, alertsRes, teleRes] = await Promise.all([
                    api.get("/cars"),
                    api.get("/alerts/stats/count"),
                    api.get("/alerts"),
                    api.get("/telemetry/latest/all"),
                ]);
                const carList = carsRes?.data?.data || [];
                setCars(carList);
                setAlertsCounts(countRes?.data?.data || { totalAlerts: 0, unacknowledgedAlerts: 0, criticalAlerts: 0 });
                setAlertsAll((alertsRes?.data?.data || []).sort((a,b) => new Date(a.timestamp) - new Date(b.timestamp)));

                const activeIds = new Set((carList || []).map((c) => c.id));
                const latest = (teleRes?.data?.data || []).filter((t) => activeIds.has(t.carId));
                setLatestAll(latest);
            } catch (_) {}
        };
        loadBase();
    }, []);

    // When selected car or range changes, fetch telemetry series and stats for that car
    useEffect(() => {
        const run = async () => {
            if (selectedCarId === "ALL") {
                setCarTelemetry([]);
                setCarStats(null);
                return;
            }
            const { start, end } = getRange(timeRange);
            try {
                const [rangeRes, statsRes] = await Promise.all([
                    api.get(`/telemetry/car/${selectedCarId}/range`, { params: { startTime: formatLocalDateTime(start), endTime: formatLocalDateTime(end) } }),
                    api.get(`/telemetry/stats/car/${selectedCarId}`, { params: { startTime: formatLocalDateTime(start), endTime: formatLocalDateTime(end) } }),
                ]);
                setCarTelemetry(rangeRes?.data?.data || []);
                setCarStats(statsRes?.data?.data || null);
            } catch (_) {
                setCarTelemetry([]);
                setCarStats(null);
            }
        };
        run();
    }, [selectedCarId, timeRange]);

    // Derived: fleet snapshot averages from latestAll
    const fleetSnapshot = useMemo(() => {
        if (!latestAll.length) return { avgSpeed: 0, avgFuel: 0, avgTemp: 0 };
        const avgSpeed = latestAll.reduce((s, t) => s + (t.speed || 0), 0) / latestAll.length;
        const avgFuel = latestAll.reduce((s, t) => s + (t.fuelLevel || 0), 0) / latestAll.length;
        const avgTemp = latestAll.reduce((s, t) => s + (t.temperature || 0), 0) / latestAll.length;
        return { avgSpeed: Math.round(avgSpeed * 10) / 10, avgFuel: Math.round(avgFuel * 10) / 10, avgTemp: Math.round(avgTemp * 10) / 10 };
    }, [latestAll]);

    // Derived: status distribution of active cars
    const statusDistribution = useMemo(() => {
        const counts = cars.reduce((acc, c) => {
            const key = String(c.status || "-").toUpperCase();
            acc[key] = (acc[key] || 0) + 1;
            return acc;
        }, {});
        return Object.entries(counts).map(([name, value]) => ({ name, value }));
    }, [cars]);

    // Alerts in selected time window (and car if selected)
    const alertsInRange = useMemo(() => {
        const { start, end } = getRange(timeRange);
        return (alertsAll || []).filter((a) => {
            const ts = new Date(a.timestamp);
            if (ts < start || ts > end) return false;
            if (selectedCarId !== "ALL" && String(a.carId) !== String(selectedCarId)) return false;
            return true;
        });
    }, [alertsAll, timeRange, selectedCarId]);

    // Derived: severity pie from alertsInRange
    const severityPie = useMemo(() => {
        const counts = { LOW: 0, MEDIUM: 0, HIGH: 0, CRITICAL: 0 };
        for (const a of alertsInRange) {
            const s = String(a.severity || "").toUpperCase();
            if (counts[s] !== undefined) counts[s]++;
        }
        return Object.entries(counts).map(([name, value]) => ({ name, value }));
    }, [alertsInRange]);

    // Derived: alerts over time buckets from recentAlerts
    const alertsOverTime = useMemo(() => {
        const { start, end } = getRange(timeRange);
        const inRange = (alertsAll || []).filter((a) => {
            const ts = new Date(a.timestamp);
            if (ts < start || ts > end) return false;
            if (selectedCarId !== "ALL" && String(a.carId) !== String(selectedCarId)) return false;
            return true;
        });
        const byKey = {};
        for (const a of inRange) {
            const ts = new Date(a.timestamp);
            const key = timeRange === "24h"
                ? `${ts.getFullYear()}-${(ts.getMonth()+1).toString().padStart(2,"0")}-${ts.getDate().toString().padStart(2,"0")} ${ts.getHours().toString().padStart(2,"0")}:00`
                : `${ts.getFullYear()}-${(ts.getMonth()+1).toString().padStart(2,"0")}-${ts.getDate().toString().padStart(2,"0")}`;
            byKey[key] = (byKey[key] || 0) + 1;
        }
        return Object.entries(byKey)
            .sort((a,b) => new Date(a[0]) - new Date(b[0]))
            .map(([name, value]) => ({ name, value }));
    }, [alertsAll, timeRange, selectedCarId]);

    // Derived: top alerting cars in range
    const topAlertCars = useMemo(() => {
        const { start, end } = getRange(timeRange);
        const inRange = (alertsAll || []).filter((a) => {
            const ts = new Date(a.timestamp);
            return ts >= start && ts <= end;
        });
        const byCar = {};
        for (const a of inRange) {
            const cid = a.carId;
            if (!cid) continue;
            byCar[cid] = (byCar[cid] || 0) + 1;
        }
        return Object.entries(byCar)
            .map(([carId, value]) => ({ carId, value }))
            .sort((a,b) => b.value - a.value)
            .slice(0, 5);
    }, [alertsAll, timeRange]);

    // Real-time thresholds from latest snapshot
    const LOW_FUEL = 20;
    const HIGH_SPEED = 100;
    const HIGH_TEMP = 50;
    const lowFuelCount = useMemo(() => latestAll.filter((t) => (t.fuelLevel ?? 0) < LOW_FUEL).length, [latestAll]);
    const highSpeedCount = useMemo(() => latestAll.filter((t) => (t.speed ?? 0) > HIGH_SPEED).length, [latestAll]);
    const highTempCount = useMemo(() => latestAll.filter((t) => (t.temperature ?? 0) > HIGH_TEMP).length, [latestAll]);

    // Chart data for selected car: map telemetry entries to chart points
    const carSeries = useMemo(() => {
        return (carTelemetry || []).map((t) => ({
            time: t.timestamp,
            speed: t.speed,
            fuel: t.fuelLevel,
            temp: t.temperature,
        }));
    }, [carTelemetry]);

    return (
        <div className="pt-16">
        <div className="p-6 bg-gray-100 min-h-screen">
            <div className="mb-6">
                <h1 className="text-3xl font-bold">Analytics</h1>
                <p className="text-gray-600">Operational insights from real telemetry and alerts</p>
            </div>

            {/* Filters */}
            <div className="flex flex-wrap gap-4 mb-6">
                <select className="border px-4 py-2 rounded" value={selectedCarId} onChange={(e) => setSelectedCarId(e.target.value)}>
                    <option value="ALL">All cars</option>
                    {cars.map((c) => (
                        <option key={c.id} value={c.id}>Car {c.id} ({String(c.status || "").toUpperCase()})</option>
                    ))}
                </select>
                <select className="border px-4 py-2 rounded" value={timeRange} onChange={(e) => setTimeRange(e.target.value)}>
                    <option value="24h">Last 24 hours</option>
                    <option value="7d">Last 7 days</option>
                    <option value="30d">Last 30 days</option>
                </select>
            </div>

            {/* Key metrics */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
                <div className="bg-white p-4 rounded shadow">
                    <div className="text-gray-600">Total Alerts</div>
                    <div className="text-2xl font-bold flex items-center gap-2"><AlertTriangle size={18} className="text-red-500" /> {alertsCounts.totalAlerts}</div>
                    <div className="text-xs text-gray-500">Active across fleet</div>
                </div>
                <div className="bg-white p-4 rounded shadow">
                    <div className="text-gray-600">Unacknowledged</div>
                    <div className="text-2xl font-bold text-orange-600">{alertsCounts.unacknowledgedAlerts}</div>
                    <div className="text-xs text-gray-500">Requires attention</div>
                </div>
                <div className="bg-white p-4 rounded shadow">
                    <div className="text-gray-600">Critical Alerts</div>
                    <div className="text-2xl font-bold text-red-600">{alertsCounts.criticalAlerts}</div>
                    <div className="text-xs text-gray-500">High severity</div>
                </div>
                <div className="bg-white p-4 rounded shadow">
                    <div className="text-gray-600">Fleet Snapshot</div>
                    <div className="text-sm mt-1 flex items-center gap-3">
                        <span className="flex items-center gap-1"><Gauge size={16} className="text-blue-600" /> {fleetSnapshot.avgSpeed} km/h</span>
                        <span className="flex items-center gap-1"><Fuel size={16} className="text-green-600" /> {fleetSnapshot.avgFuel}%</span>
                        <span className="flex items-center gap-1"><Thermometer size={16} className="text-orange-600" /> {fleetSnapshot.avgTemp}°C</span>
                    </div>
                </div>
            </div>

            {/* Operational insights from latest snapshot */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
                <div className="bg-white p-4 rounded shadow"><div className="text-gray-600">Low Fuel ({'< '}{LOW_FUEL}%)</div><div className="text-2xl font-bold text-emerald-700">{lowFuelCount}</div></div>
                <div className="bg-white p-4 rounded shadow"><div className="text-gray-600">Overspeed (> {HIGH_SPEED} km/h)</div><div className="text-2xl font-bold text-blue-700">{highSpeedCount}</div></div>
                <div className="bg-white p-4 rounded shadow"><div className="text-gray-600">High Temp (> {HIGH_TEMP}°C)</div><div className="text-2xl font-bold text-orange-700">{highTempCount}</div></div>
            </div>

            {/* Charts */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
                <div className="bg-white p-4 rounded shadow">
                    <h3 className="text-lg font-semibold mb-3">Alert Severity Distribution</h3>
                    <ResponsiveContainer width="100%" height={260}>
                        <PieChart>
                            <Pie data={severityPie} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={90} label>
                                {severityPie.map((entry, index) => (
                                    <Cell key={`sev-${index}`} fill={COLORS[index % COLORS.length]} />
                                ))}
                            </Pie>
                            <Tooltip />
                            <Legend />
                        </PieChart>
                    </ResponsiveContainer>
                </div>

                <div className="bg-white p-4 rounded shadow">
                    <h3 className="text-lg font-semibold mb-3">Fleet Status Distribution</h3>
                    <ResponsiveContainer width="100%" height={260}>
                        <PieChart>
                            <Pie data={statusDistribution} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={90} label>
                                {statusDistribution.map((entry, index) => (
                                    <Cell key={`stat-${index}`} fill={COLORS[(index+2) % COLORS.length]} />
                                ))}
                            </Pie>
                            <Tooltip />
                            <Legend />
                        </PieChart>
                    </ResponsiveContainer>
                </div>

                <div className="bg-white p-4 rounded shadow lg:col-span-2">
                    <h3 className="text-lg font-semibold mb-3">Alerts Over Time ({timeRange === "24h" ? "by hour" : "by day"})</h3>
                    <ResponsiveContainer width="100%" height={280}>
                        <BarChart data={alertsOverTime}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="name" />
                            <YAxis allowDecimals={false} />
                            <Tooltip />
                            <Bar dataKey="value" fill="#FF6B6B" name="Alerts" />
                        </BarChart>
                    </ResponsiveContainer>
                </div>

                <div className="bg-white p-4 rounded shadow lg:col-span-2">
                    <h3 className="text-lg font-semibold mb-3">Top Alerting Cars</h3>
                    <ResponsiveContainer width="100%" height={260}>
                        <BarChart data={topAlertCars}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="carId" tickFormatter={(v) => `Car ${v}`} />
                            <YAxis allowDecimals={false} />
                            <Tooltip formatter={(v) => [`${v}`, "Alerts"]} labelFormatter={(l) => `Car ${l}`} />
                            <Bar dataKey="value" fill="#6366F1" name="Alerts" />
                        </BarChart>
                    </ResponsiveContainer>
                </div>
            </div>

            {selectedCarId !== "ALL" && (
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                    <div className="bg-white p-4 rounded shadow">
                        <h3 className="text-lg font-semibold mb-3">Car {selectedCarId} Telemetry</h3>
                        <ResponsiveContainer width="100%" height={280}>
                            <LineChart data={carSeries}>
                                <CartesianGrid strokeDasharray="3 3" />
                                <XAxis dataKey="time" />
                                <YAxis yAxisId="left" domain={[0, "auto"]} />
                                <YAxis yAxisId="right" orientation="right" domain={[0, 120]} />
                                <Tooltip />
                                <Legend />
                                <Line yAxisId="left" type="monotone" dataKey="speed" stroke="#3B82F6" name="Speed (km/h)" dot={false} />
                                <Line yAxisId="left" type="monotone" dataKey="fuel" stroke="#10B981" name="Fuel (%)" dot={false} />
                                <Line yAxisId="right" type="monotone" dataKey="temp" stroke="#F59E0B" name="Temp (°C)" dot={false} />
                            </LineChart>
                        </ResponsiveContainer>
                    </div>
                    <div className="bg-white p-4 rounded shadow">
                        <h3 className="text-lg font-semibold mb-3">Car {selectedCarId} Summary</h3>
                        {carStats ? (
                            <div className="grid grid-cols-2 gap-3 text-sm">
                                <div className="p-3 bg-gray-50 rounded"><div className="text-gray-600">Avg Speed</div><div className="text-xl font-semibold">{carStats.averageSpeed} km/h</div></div>
                                <div className="p-3 bg-gray-50 rounded"><div className="text-gray-600">Avg Fuel</div><div className="text-xl font-semibold">{carStats.averageFuel}%</div></div>
                                <div className="p-3 bg-gray-50 rounded"><div className="text-gray-600">Avg Temp</div><div className="text-xl font-semibold">{carStats.averageTemperature}°C</div></div>
                                <div className="p-3 bg-gray-50 rounded"><div className="text-gray-600">Records</div><div className="text-xl font-semibold">{carStats.totalRecords}</div></div>
                                <div className="p-3 bg-gray-50 rounded"><div className="text-gray-600">Speed Range</div><div className="text-xl font-semibold">{carStats.minSpeed} - {carStats.maxSpeed}</div></div>
                                <div className="p-3 bg-gray-50 rounded"><div className="text-gray-600">Fuel Range</div><div className="text-xl font-semibold">{carStats.minFuel}% - {carStats.maxFuel}%</div></div>
                                <div className="p-3 bg-gray-50 rounded"><div className="text-gray-600">Temp Range</div><div className="text-xl font-semibold">{carStats.minTemperature}° - {carStats.maxTemperature}°</div></div>
                            </div>
                        ) : (
                            <div className="text-gray-500 text-sm">No data available for the selected range.</div>
                        )}
                    </div>
                </div>
            )}
        </div>
    </div>
    );
};

export default AnalyticsPage;
