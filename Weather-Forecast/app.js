const form = document.getElementById("search-form");
const queryInput = document.getElementById("query");
const locationEl = document.getElementById("location");
const currentEl = document.getElementById("current");
const dailyEl = document.getElementById("daily");
const errorEl = document.getElementById("error");

async function searchCity(name) {
  const url = `https://geocoding-api.open-meteo.com/v1/search?name=${encodeURIComponent(
    name
  )}&count=1&language=en&format=json`;
  const res = await fetch(url);
  if (!res.ok) throw new Error("Failed to search location");
  const data = await res.json();
  if (!data.results || data.results.length === 0)
    throw new Error("No matching location found");
  return data.results[0];
}

async function getForecast(lat, lon) {
  const params = new URLSearchParams({
    latitude: String(lat),
    longitude: String(lon),
    current: "temperature_2m,wind_speed_10m",
    daily: "temperature_2m_max,temperature_2m_min,precipitation_sum",
    timezone: "auto",
  });
  const url = `https://api.open-meteo.com/v1/forecast?${params.toString()}`;
  const res = await fetch(url);
  if (!res.ok) throw new Error("Failed to fetch forecast");
  return res.json();
}

function formatDate(s) {
  const d = new Date(s);
  return d.toLocaleDateString(undefined, {
    weekday: "short",
    month: "short",
    day: "numeric",
  });
}

function renderCurrent(loc, forecast) {
  const temp = forecast.current.temperature_2m;
  const wind = forecast.current.wind_speed_10m;
  locationEl.textContent = `${loc.name}${
    loc.admin1 ? ", " + loc.admin1 : ""
  }, ${loc.country}`;
  currentEl.innerHTML = `
    <div class="current-card">
      <div class="value">${Math.round(temp)}°C</div>
      <div class="meta">Wind ${Math.round(wind)} m/s</div>
    </div>
  `;
}

function renderDaily(forecast) {
  const days = forecast.daily.time;
  const tmax = forecast.daily.temperature_2m_max;
  const tmin = forecast.daily.temperature_2m_min;
  const prcp = forecast.daily.precipitation_sum;
  const items = days.map((t, i) => {
    return `
      <div class="day">
        <div class="date">${formatDate(t)}</div>
        <div class="temps">
          <span class="high">${Math.round(tmax[i])}°</span>
          <span class="low">${Math.round(tmin[i])}°</span>
        </div>
        <div class="precip">${Math.round(prcp[i])} mm</div>
      </div>
    `;
  });
  dailyEl.innerHTML = `<div class="days">${items.join("")}</div>`;
}

function setLoading(loading) {
  const btn = form.querySelector("button[type=submit]");
  btn.disabled = loading;
  btn.textContent = loading ? "Loading…" : "Get Forecast";
}

function clearUI() {
  errorEl.textContent = "";
  currentEl.innerHTML = "";
  dailyEl.innerHTML = "";
  locationEl.textContent = "";
}

form.addEventListener("submit", async (e) => {
  e.preventDefault();
  clearUI();
  const q = queryInput.value.trim();
  if (!q) return;
  setLoading(true);
  try {
    const loc = await searchCity(q);
    const data = await getForecast(loc.latitude, loc.longitude);
    renderCurrent(loc, data);
    renderDaily(data);
  } catch (err) {
    errorEl.textContent = String(err.message || err);
  } finally {
    setLoading(false);
  }
});
