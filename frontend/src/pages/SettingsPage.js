import React, { useEffect, useMemo, useState } from "react";
import { Users, Truck } from "lucide-react";
import api from "../api/client";

const PAGE_SIZE = 8;

const SettingsPage = () => {
    const [activeTab, setActiveTab] = useState("users");

    // Drivers (User Management)
    const [drivers, setDrivers] = useState([]);
    const [driversLoading, setDriversLoading] = useState(false);
    const [driversError, setDriversError] = useState("");
    const [driversSearch, setDriversSearch] = useState("");
    const [driversPage, setDriversPage] = useState(1);

    // Fleet
    const [cars, setCars] = useState([]);
    const [carsLoading, setCarsLoading] = useState(false);
    const [carsError, setCarsError] = useState("");
    const [carsSearch, setCarsSearch] = useState("");
    const [carsPage, setCarsPage] = useState(1);
    const [availableDrivers, setAvailableDrivers] = useState([]);
    const [availableLoading, setAvailableLoading] = useState(false);
    const [driverSelection, setDriverSelection] = useState({});

    // Add Car modal
    const [showAddCarModal, setShowAddCarModal] = useState(false);
    const [newCarStatus, setNewCarStatus] = useState("IDLE");
    const [newCarDriverId, setNewCarDriverId] = useState("");
    const [creatingCar, setCreatingCar] = useState(false);
    const [createError, setCreateError] = useState("");

    // Fleet feedback
    const [fleetMessage, setFleetMessage] = useState("");
    const [fleetMessageType, setFleetMessageType] = useState("");

    useEffect(() => {
        const fetchDrivers = async () => {
            setDriversLoading(true);
            setDriversError("");
            try {
                const res = await api.get("/users/role/DRIVER");
                const { success, data, message } = res.data || {};
                if (!success || !Array.isArray(data)) throw new Error(message || "Failed to load drivers");
                setDrivers(data);
            } catch (err) {
                setDriversError(err?.response?.data?.message || err.message || "Failed to load drivers");
            } finally {
                setDriversLoading(false);
            }
        };
        const fetchCars = async () => {
            setCarsLoading(true);
            setCarsError("");
            try {
                const res = await api.get("/cars");
                const { success, data, message } = res.data || {};
                if (!success || !Array.isArray(data)) throw new Error(message || "Failed to load cars");
                setCars(data);
                const initSel = {};
                data.forEach((c) => { initSel[c.id] = c.driverId || ""; });
                setDriverSelection(initSel);
            } catch (err) {
                setCarsError(err?.response?.data?.message || err.message || "Failed to load cars");
            } finally {
                setCarsLoading(false);
            }
        };
        const fetchAvailable = async () => {
            setAvailableLoading(true);
            try {
                const res = await api.get("/drivers/available");
                const { success, data, message } = res.data || {};
                if (!success || !Array.isArray(data)) throw new Error(message || "Failed to load available drivers");
                setAvailableDrivers(data);
            } catch (_e) {
                // ignore
            } finally {
                setAvailableLoading(false);
            }
        };
        fetchDrivers();
        fetchCars();
        fetchAvailable();
    }, []);

    const refreshFleet = async () => {
        try {
            const [carsRes, availRes] = await Promise.all([api.get("/cars"), api.get("/drivers/available")]);
            if (carsRes.data?.success && Array.isArray(carsRes.data?.data)) {
                const carData = carsRes.data.data;
                setCars(carData);
                const sel = {};
                carData.forEach((c) => { sel[c.id] = c.driverId || ""; });
                setDriverSelection(sel);
            }
            if (availRes.data?.success && Array.isArray(availRes.data?.data)) {
                setAvailableDrivers(availRes.data.data);
            }
        } catch (_e) {}
    };

    useEffect(() => { setDriversPage(1); }, [driversSearch, drivers.length]);
    useEffect(() => { setCarsPage(1); }, [carsSearch, cars.length]);
    useEffect(() => {
        if (!fleetMessage) return;
        const t = setTimeout(() => setFleetMessage(""), 2500);
        return () => clearTimeout(t);
    }, [fleetMessage]);

    const filteredDrivers = useMemo(() => {
        if (!driversSearch) return drivers;
        const q = driversSearch.toLowerCase().trim();
        return drivers.filter((d) =>
            String(d.id ?? "").includes(q) ||
            (d.username || "").toLowerCase().includes(q) ||
            (d.name || "").toLowerCase().includes(q) ||
            (d.contactNumber || "").toLowerCase().includes(q)
        );
    }, [drivers, driversSearch]);
    const totalDriversPages = Math.max(1, Math.ceil(filteredDrivers.length / PAGE_SIZE));
    const currentDriversPage = Math.min(driversPage, totalDriversPages);
    const paginatedDrivers = useMemo(() => {
        const start = (currentDriversPage - 1) * PAGE_SIZE;
        return filteredDrivers.slice(start, start + PAGE_SIZE);
    }, [filteredDrivers, currentDriversPage]);

    const filteredCars = useMemo(() => {
        if (!carsSearch) return cars;
        const q = carsSearch.toLowerCase().trim();
        return cars.filter((c) => String(c.id ?? "").includes(q) || (c.driverName || "").toLowerCase().includes(q));
    }, [cars, carsSearch]);
    const totalCarsPages = Math.max(1, Math.ceil(filteredCars.length / PAGE_SIZE));
    const currentCarsPage = Math.min(carsPage, totalCarsPages);
    const paginatedCars = useMemo(() => {
        const start = (currentCarsPage - 1) * PAGE_SIZE;
        return filteredCars.slice(start, start + PAGE_SIZE);
    }, [filteredCars, currentCarsPage]);

    const formatDateTime = (value) => {
        if (!value) return "";
        try {
            const d = new Date(value);
            if (!isNaN(d.getTime())) return d.toLocaleString();
            if (Array.isArray(value)) {
                const [y, m, day, h = 0, min = 0, s = 0] = value;
                return new Date(y, (m || 1) - 1, day || 1, h, min, s).toLocaleString();
            }
            return String(value);
        } catch {
            return String(value);
        }
    };

    const handleCreateCar = async (e) => {
        e.preventDefault();
        setCreateError("");
        setCreatingCar(true);
        try {
            const payload = { status: newCarStatus || "IDLE", speed: 0, fuelLevel: 100, temperature: 40, location: "New York, NY" };
            const res = await api.post("/cars", payload);
            const { success, data, message } = res.data || {};
            if (!success || !data?.id) throw new Error(message || "Failed to create car");
            if (newCarDriverId) await api.put(`/cars/${data.id}/assign-driver`, null, { params: { driverId: newCarDriverId } });
            setNewCarStatus("IDLE");
            setNewCarDriverId("");
            setShowAddCarModal(false);
            await refreshFleet();
            setCarsPage(1);
            setFleetMessage("Car added successfully");
            setFleetMessageType("success");
        } catch (err) {
            const msg = err?.response?.data?.message || err.message || "Failed to create car";
            setCreateError(msg);
            setFleetMessage(msg);
            setFleetMessageType("error");
        } finally {
            setCreatingCar(false);
        }
    };

    const handleDriverSelectionChange = (carId, value) => setDriverSelection((prev) => ({ ...prev, [carId]: value }));

    const handleApplyDriverChange = async (car) => {
        const selected = driverSelection[car.id] || "";
        const current = car.driverId || "";
        if (selected === current) return;
        try {
            if (!selected) await api.put(`/cars/${car.id}/remove-driver`);
            else await api.put(`/cars/${car.id}/assign-driver`, null, { params: { driverId: selected } });
            await refreshFleet();
            setFleetMessage(selected ? "Driver updated successfully" : "Driver removed successfully");
            setFleetMessageType("success");
        } catch (err) {
            setFleetMessage(err?.response?.data?.message || err.message || "Failed to update driver");
            setFleetMessageType("error");
        }
    };

    const handleDeleteCar = async (car) => {
        const ok = window.confirm(`Delete car #${car.id}? This cannot be undone.`);
        if (!ok) return;
        try {
            const res = await api.delete(`/cars/${car.id}`);
            if (!res.data?.success) throw new Error(res.data?.message || "Failed to delete car");
            await refreshFleet();
            setFleetMessage("Car deleted successfully");
            setFleetMessageType("success");
        } catch (err) {
            setFleetMessage(err?.response?.data?.message || err.message || "Failed to delete car");
            setFleetMessageType("error");
        }
    };

    return (
        <div className="pt-16">
            <div className="p-6 bg-gray-100 min-h-screen">
                <div className="mb-6">
                    <h1 className="text-2xl font-bold">System Settings</h1>
                    <p className="text-gray-600">Manage users, vehicles, and configure system settings</p>
                </div>

                <div className="flex gap-4 mb-6 border-b pb-2">
                    <button onClick={() => setActiveTab("users")} className={`flex items-center gap-2 px-3 py-2 rounded ${activeTab === "users" ? "bg-blue-500 text-white" : "hover:bg-gray-200"}`}>
                        <Users size={18} /> User Management
                    </button>
                    <button onClick={() => setActiveTab("fleet")} className={`flex items-center gap-2 px-3 py-2 rounded ${activeTab === "fleet" ? "bg-blue-500 text-white" : "hover:bg-gray-200"}`}>
                        <Truck size={18} /> Fleet Management
                    </button>
                </div>

                <div className="bg-white p-6 rounded-lg shadow">
                    {activeTab === "users" && (
                        <div>
                            <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-3 mb-4">
                                <h2 className="text-lg font-bold">User Management</h2>
                                <input type="text" placeholder="Search by ID, username, name, or contact no" value={driversSearch} onChange={(e) => setDriversSearch(e.target.value)} className="border px-3 py-2 rounded w-full md:w-96" />
                            </div>
                            {driversError && <div className="mb-3 text-sm text-red-600">{driversError}</div>}
                            <table className="w-full border">
                                <thead>
                                    <tr className="bg-gray-100 text-left">
                                        <th className="p-2">ID</th>
                                        <th className="p-2">Username</th>
                                        <th className="p-2">Name</th>
                                        <th className="p-2">Contact No</th>
                                        <th className="p-2">Email</th>
                                        <th className="p-2">Age</th>
                                        <th className="p-2">Gender</th>
                                        <th className="p-2">License Number</th>
                                        <th className="p-2">Creation Date</th>
                                        <th className="p-2">Last Update</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {driversLoading ? (
                                        <tr><td className="p-2" colSpan={10}>Loading drivers...</td></tr>
                                    ) : paginatedDrivers.length === 0 ? (
                                        <tr><td className="p-2" colSpan={10}>No drivers found</td></tr>
                                    ) : (
                                        paginatedDrivers.map((u) => (
                                            <tr key={u.id} className="border-t">
                                                <td className="p-2">{u.id}</td>
                                                <td className="p-2">{u.username}</td>
                                                <td className="p-2">{u.name}</td>
                                                <td className="p-2">{u.contactNumber}</td>
                                                <td className="p-2">{u.email}</td>
                                                <td className="p-2">{u.age}</td>
                                                <td className="p-2">{u.gender}</td>
                                                <td className="p-2">{u.licenseNumber}</td>
                                                <td className="p-2">{formatDateTime(u.creationDate)}</td>
                                                <td className="p-2">{formatDateTime(u.lastUpdateOn)}</td>
                                            </tr>
                                        ))
                                    )}
                                </tbody>
                            </table>
                            <div className="flex items-center justify-end gap-2 mt-3">
                                <button className="px-3 py-1 border rounded disabled:opacity-50" onClick={() => setDriversPage((p) => Math.max(1, p - 1))} disabled={currentDriversPage <= 1}>Prev</button>
                                <span className="text-sm">Page {currentDriversPage} of {totalDriversPages}</span>
                                <button className="px-3 py-1 border rounded disabled:opacity-50" onClick={() => setDriversPage((p) => Math.min(totalDriversPages, p + 1))} disabled={currentDriversPage >= totalDriversPages}>Next</button>
                            </div>
                        </div>
                    )}

                    {activeTab === "fleet" && (
                        <div>
                            <div className="flex flex-col gap-4">
                                <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-3">
                                    <h2 className="text-lg font-bold">Fleet Management</h2>
                                    <div className="flex flex-col md:flex-row gap-2 md:items-center">
                                        <input type="text" placeholder="Search by Car ID or Driver Name" value={carsSearch} onChange={(e) => setCarsSearch(e.target.value)} className="border px-3 py-2 rounded w-full md:w-80" />
                                        <button onClick={() => setShowAddCarModal(true)} className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600">Add Car</button>
                                    </div>
                                </div>

                                {fleetMessage && (
                                    <div className={`text-sm px-3 py-2 rounded ${fleetMessageType === 'success' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>{fleetMessage}</div>
                                )}

                                {showAddCarModal && (
                                    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
                                        <div className="bg-white rounded-lg shadow-lg w-full max-w-md p-4">
                                            <div className="flex items-center justify-between mb-2">
                                                <h3 className="text-lg font-semibold">Add New Car</h3>
                                                <button onClick={() => { setShowAddCarModal(false); setCreateError(""); }} className="px-2 py-1 text-gray-500 hover:text-gray-700">✕</button>
                                            </div>
                                            <form onSubmit={handleCreateCar} className="space-y-3">
                                                <div>
                                                    <label className="block text-sm mb-1">Status</label>
                                                    <input type="text" value={newCarStatus} onChange={(e) => setNewCarStatus(e.target.value)} className="border px-3 py-2 rounded w-full" placeholder="e.g., IDLE or ACTIVE" required />
                                                </div>
                                                <div>
                                                    <label className="block text-sm mb-1">Assign Driver (optional)</label>
                                                    <select className="border px-3 py-2 rounded w-full" value={newCarDriverId} onChange={(e) => setNewCarDriverId(e.target.value)}>
                                                        <option value="">None</option>
                                                        {availableDrivers.length === 0 && (<option value="" disabled>No drivers available</option>)}
                                                        {availableDrivers.map((d) => (<option key={d.id} value={d.id}>{d.name} (Driver ID: {d.id})</option>))}
                                                    </select>
                                                    {availableLoading && (<div className="text-xs text-gray-500 mt-1">Loading available drivers…</div>)}
                                                </div>
                                                {createError && <div className="text-red-600 text-sm">{createError}</div>}
                                                <div className="flex justify-end gap-2 pt-2">
                                                    <button type="button" onClick={() => { setShowAddCarModal(false); setCreateError(""); }} className="px-4 py-2 border rounded hover:bg-gray-50">Cancel</button>
                                                    <button type="submit" disabled={creatingCar} className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600 disabled:opacity-60">{creatingCar ? "Adding…" : "Save"}</button>
                                                </div>
                                            </form>
                                        </div>
                                    </div>
                                )}

                                <div>
                                    <table className="w-full border">
                                        <thead>
                                            <tr className="bg-gray-100 text-left">
                                                <th className="p-2">ID</th>
                                                <th className="p-2">Status</th>
                                                <th className="p-2">Speed</th>
                                                <th className="p-2">Fuel</th>
                                                <th className="p-2">Temperature</th>
                                                <th className="p-2">Location</th>
                                                <th className="p-2">Driver ID</th>
                                                <th className="p-2">Driver Name</th>
                                                <th className="p-2">Assign/Change Driver</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {carsLoading ? (
                                                <tr><td className="p-2" colSpan={9}>Loading cars...</td></tr>
                                            ) : carsError ? (
                                                <tr><td className="p-2 text-red-600" colSpan={9}>{carsError}</td></tr>
                                            ) : paginatedCars.length === 0 ? (
                                                <tr><td className="p-2" colSpan={9}>No cars found</td></tr>
                                            ) : (
                                                paginatedCars.map((c) => {
                                                    const currentDriverId = c.driverId || "";
                                                    const currentDriverName = c.driverName || "";
                                                    const optionMap = new Map();
                                                    if (currentDriverId) optionMap.set(String(currentDriverId), { id: currentDriverId, label: `${currentDriverName} (Driver ID: ${currentDriverId})` });
                                                    availableDrivers.forEach((d) => { optionMap.set(String(d.id), { id: d.id, label: `${d.name} (Driver ID: ${d.id})` }); });
                                                    const options = Array.from(optionMap.values());
                                                    return (
                                                        <tr key={c.id} className="border-t">
                                                            <td className="p-2">{c.id}</td>
                                                            <td className="p-2">{c.status}</td>
                                                            <td className="p-2">{c.speed}</td>
                                                            <td className="p-2">{c.fuelLevel}</td>
                                                            <td className="p-2">{c.temperature}</td>
                                                            <td className="p-2">{c.location}</td>
                                                            <td className="p-2">{currentDriverId || "-"}</td>
                                                            <td className="p-2">{currentDriverName || "-"}</td>
                                                            <td className="p-2">
                                                                <div className="flex items-center gap-2">
                                                                    <select className="border px-2 py-1 rounded" value={driverSelection[c.id] ?? (currentDriverId || "")} onChange={(e) => setDriverSelection((prev) => ({ ...prev, [c.id]: e.target.value }))}>
                                                                        <option value="">None</option>
                                                                        {availableDrivers.length === 0 && !currentDriverId && (<option value="" disabled>No drivers available</option>)}
                                                                        {options.map((opt) => (<option key={opt.id} value={opt.id}>{opt.label}</option>))}
                                                                    </select>
                                                                    <button className="px-3 py-1 border rounded hover:bg-gray-50" onClick={() => handleApplyDriverChange(c)}>Apply</button>
                                                                    <button className="px-3 py-1 border rounded text-red-600 hover:bg-red-50" onClick={() => handleDeleteCar(c)}>Delete</button>
                                                                </div>
                                                            </td>
                                                        </tr>
                                                    );
                                                })
                                            )}
                                        </tbody>
                                    </table>
                                    <div className="flex items-center justify-end gap-2 mt-3">
                                        <button className="px-3 py-1 border rounded disabled:opacity-50" onClick={() => setCarsPage((p) => Math.max(1, p - 1))} disabled={currentCarsPage <= 1}>Prev</button>
                                        <span className="text-sm">Page {currentCarsPage} of {totalCarsPages}</span>
                                        <button className="px-3 py-1 border rounded disabled:opacity-50" onClick={() => setCarsPage((p) => Math.min(totalCarsPages, p + 1))} disabled={currentCarsPage >= totalCarsPages}>Next</button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default SettingsPage;

