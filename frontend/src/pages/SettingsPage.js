import React, { useEffect, useMemo, useState } from "react";
import {
    Users,
    Truck,
    Edit,
    Trash,

} from "lucide-react";
import api from "../api/client";

const SettingsPage = () => {
    const [activeTab, setActiveTab] = useState("users");
    const [drivers, setDrivers] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState("");
    const [search, setSearch] = useState("");

    const [vehicles] = useState([
        { id: "CAR001", model: "Tesla Model 3", year: 2023, status: "Active", assignedDriver: "driver1" },
        { id: "CAR002", model: "Ford F-150", year: 2022, status: "Maintenance", assignedDriver: null },
    ]);

    useEffect(() => {
        const fetchDrivers = async () => {
            setIsLoading(true);
            setError("");
            try {
                const res = await api.get("/users/role/DRIVER");
                const { success, data, message } = res.data || {};
                if (!success || !Array.isArray(data)) {
                    throw new Error(message || "Failed to load drivers");
                }
                setDrivers(data);
            } catch (err) {
                setError(err?.response?.data?.message || err.message || "Failed to load drivers");
            } finally {
                setIsLoading(false);
            }
        };
        fetchDrivers();
    }, []);

    const handleTabChange = (tab) => {
        setActiveTab(tab);
    };

    const filteredDrivers = useMemo(() => {
        if (!search) return drivers;
        const q = search.toLowerCase().trim();
        return drivers.filter((d) => {
            const idStr = String(d.id ?? "");
            const username = (d.username || "").toLowerCase();
            const name = (d.name || "").toLowerCase();
            const contact = (d.contactNumber || "").toLowerCase();
            return (
                idStr.includes(q) ||
                username.includes(q) ||
                name.includes(q) ||
                contact.includes(q)
            );
        });
    }, [drivers, search]);

    const formatDateTime = (value) => {
        if (!value) return "";
        try {
            const d = new Date(value);
            if (!isNaN(d.getTime())) return d.toLocaleString();
            // Fallback for LocalDateTime serialized as array [yyyy,mm,dd,hh,mm,ss,...]
            if (Array.isArray(value)) {
                const [y, m, day, h = 0, min = 0, s = 0] = value;
                return new Date(y, (m || 1) - 1, day || 1, h, min, s).toLocaleString();
            }
            return String(value);
        } catch {
            return String(value);
        }
    };

    return (
        <div className="pt-16">
        <div className="p-6 bg-gray-100 min-h-screen">
            {/* Header */}
            <div className="mb-6">
                <h1 className="text-2xl font-bold">System Settings</h1>
                <p className="text-gray-600">Manage users, vehicles, and configure system settings</p>
            </div>

            {/* Tabs */}
            <div className="flex gap-4 mb-6 border-b pb-2">
                <button
                    onClick={() => handleTabChange("users")}
                    className={`flex items-center gap-2 px-3 py-2 rounded ${activeTab === "users" ? "bg-blue-500 text-white" : "hover:bg-gray-200"
                        }`}
                >
                    <Users size={18} /> User Management
                </button>
                <button
                    onClick={() => handleTabChange("fleet")}
                    className={`flex items-center gap-2 px-3 py-2 rounded ${activeTab === "fleet" ? "bg-blue-500 text-white" : "hover:bg-gray-200"
                        }`}
                >
                    <Truck size={18} /> Fleet Management
                </button>
            </div>

            {/* Tab Content */}
            <div className="bg-white p-6 rounded-lg shadow">
                {activeTab === "users" && (
                    <div>
                        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-3 mb-4">
                            <h2 className="text-lg font-bold">User Management</h2>
                            <input
                                type="text"
                                placeholder="Search by ID, username, name, or contact no"
                                value={search}
                                onChange={(e) => setSearch(e.target.value)}
                                className="border px-3 py-2 rounded w-full md:w-96"
                            />
                        </div>
                        {error && (
                            <div className="mb-3 text-sm text-red-600">{error}</div>
                        )}
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
                                    <th className="p-2">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {isLoading ? (
                                    <tr>
                                        <td className="p-2" colSpan={11}>Loading drivers...</td>
                                    </tr>
                                ) : filteredDrivers.length === 0 ? (
                                    <tr>
                                        <td className="p-2" colSpan={11}>No drivers found</td>
                                    </tr>
                                ) : (
                                    filteredDrivers.map((u) => (
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
                                            <td className="p-2 flex gap-2">
                                                <button className="text-blue-500 hover:text-blue-700">
                                                    <Edit size={16} />
                                                </button>
                                                <button className="text-red-500 hover:text-red-700">
                                                    <Trash size={16} />
                                                </button>
                                            </td>
                                        </tr>
                                    ))
                                )}
                            </tbody>
                        </table>
                    </div>
                )}

                {activeTab === "fleet" && (
                    <div>
                        <div className="flex justify-between items-center mb-4">
                            <h2 className="text-lg font-bold">Fleet Management</h2>
                            {/* Add Vehicle removed intentionally? Keeping as-is per requirements only mention removing Add User */}
                        </div>
                        <table className="w-full border">
                            <thead>
                                <tr className="bg-gray-100 text-left">
                                    <th className="p-2">ID</th>
                                    <th className="p-2">Model</th>
                                    <th className="p-2">Year</th>
                                    <th className="p-2">Status</th>
                                    <th className="p-2">Assigned Driver</th>
                                    <th className="p-2">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {vehicles.map((vehicle, index) => (
                                    <tr key={index} className="border-t">
                                        <td className="p-2">{vehicle.id}</td>
                                        <td className="p-2">{vehicle.model}</td>
                                        <td className="p-2">{vehicle.year}</td>
                                        <td className="p-2">{vehicle.status}</td>
                                        <td className="p-2">{vehicle.assignedDriver || "None"}</td>
                                        <td className="p-2 flex gap-2">
                                            <button className="text-blue-500 hover:text-blue-700">
                                                <Edit size={16} />
                                            </button>
                                            <button className="text-red-500 hover:text-red-700">
                                                <Trash size={16} />
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    </div>
    );
};

export default SettingsPage;
