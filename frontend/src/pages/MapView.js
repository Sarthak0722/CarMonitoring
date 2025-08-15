// MapView.js
import React, { useState, useEffect } from "react";
import { MapPin, RefreshCcw } from "lucide-react";
import { MapContainer, TileLayer, Marker, Popup, useMap } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import L from "leaflet";
import api from "../api/client";

// Fix for default marker icon missing in some setups
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.7.1/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.7.1/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.7.1/dist/images/marker-shadow.png',
});

const ChangeView = ({ center }) => {
  const map = useMap();
  useEffect(() => { if (center) map.flyTo(center, map.getZoom()); }, [center, map]);
  return null;
};

// Minimal city-to-coordinates mapping for simulator locations
const CITY_COORDS = {
  "New York, NY": [40.7128, -74.0060],
  "Los Angeles, CA": [34.0522, -118.2437],
  "Chicago, IL": [41.8781, -87.6298],
  "Houston, TX": [29.7604, -95.3698],
  "Phoenix, AZ": [33.4484, -112.0740],
  "Philadelphia, PA": [39.9526, -75.1652],
  "San Antonio, TX": [29.4241, -98.4936],
  "San Diego, CA": [32.7157, -117.1611],
  "Dallas, TX": [32.7767, -96.7970],
  "San Jose, CA": [37.3382, -121.8863]
};

const MapView = ({ user }) => {
  const [carId, setCarId] = useState(null);
  const [latest, setLatest] = useState(null);
  const [center, setCenter] = useState([40, -100]);

  const fetchDriverCar = async () => {
    try {
      const dres = await api.get(`/drivers/user/${user.id}`);
      const d = dres?.data?.data;
      setCarId(d?.assignedCarId || null);
    } catch (_) {
      setCarId(null);
    }
  };

  const fetchLatest = async () => {
    if (!carId) return;
    try {
      const tRes = await api.get(`/telemetry/car/${carId}/latest`);
      const list = tRes?.data?.data || [];
      const l = list[0] || null;
      setLatest(l);
      const coords = l?.location && CITY_COORDS[l.location] ? CITY_COORDS[l.location] : null;
      if (coords) setCenter(coords);
    } catch (_) { /* ignore */ }
  };

  useEffect(() => { fetchDriverCar(); }, [user.id]);
  useEffect(() => { fetchLatest(); }, [carId]);
  useEffect(() => { const id = setInterval(fetchLatest, 10000); return () => clearInterval(id); }, [carId]);

  if (!carId) {
    return (
      <div className="pt-16">
        <div className="p-6 bg-gray-100 min-h-screen flex items-center justify-center">
          <div className="bg-white p-6 rounded shadow text-center max-w-md">
            <h2 className="text-xl font-semibold mb-2">No vehicle assigned</h2>
            <p className="text-gray-600">You will see the map once a vehicle is assigned to you.</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="pt-16">
      <div className="p-6 bg-gray-100 min-h-screen">
        <div className="w-full bg-white rounded-lg shadow p-4">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-lg font-bold flex items-center gap-2">
              <MapPin size={20} /> Vehicle Location (Car {carId})
            </h2>
            <button onClick={fetchLatest} className="flex items-center gap-2 text-blue-500 hover:text-blue-700">
              <RefreshCcw size={18} /> Refresh
            </button>
          </div>

          <div className="w-full h-96 border rounded overflow-hidden">
            <MapContainer center={center} zoom={latest ? 13 : 4} scrollWheelZoom={true} style={{ height: "100%", width: "100%" }}>
              <ChangeView center={center} />
              <TileLayer attribution='&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors' url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
              {latest && (
                <Marker position={center}>
                  <Popup>
                    <div>
                      <h4 className="font-bold">Car {carId}</h4>
                      <p>{latest.location || "Unknown"}</p>
                    </div>
                  </Popup>
                </Marker>
              )}
            </MapContainer>
          </div>
        </div>
      </div>
    </div>
  );
};

export default MapView;
