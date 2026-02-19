package com.example.flightbooking;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class WebServer {
    private static int PORT = 8080;
    private static final Path DATA_DIR = Path.of("data");
    private static final Path BOOKINGS_CSV = DATA_DIR.resolve("bookings.csv");

    public static void main(String[] args) throws IOException {
        if (args != null && args.length > 0) {
            try { PORT = Integer.parseInt(args[0]); } catch (Exception ignored) {}
        } else {
            String p = System.getenv("PORT");
            if (p != null) {
                try { PORT = Integer.parseInt(p); } catch (Exception ignored) {}
            }
        }
        if (!Files.exists(DATA_DIR)) {
            Files.createDirectories(DATA_DIR);
        }
        if (!Files.exists(BOOKINGS_CSV)) {
            Files.writeString(BOOKINGS_CSV, "timestamp,name,email,flightId,origin,destination,seat,price\n",
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        PORT = server.getAddress().getPort();
        server.createContext("/", new RootHandler());
        server.createContext("/book", new BookHandler());
        server.createContext("/bookings", new BookingsHandler());
        server.setExecutor(null);
        System.out.println("Server running at http://localhost:" + PORT);
        server.start();
    }

    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String html = """
                    <!doctype html>
                    <html>
                    <head>
                      <meta charset="utf-8">
                      <title>Flight Booking</title>
                      <meta name="viewport" content="width=device-width, initial-scale=1">
                      <style>
                        :root {
                          --bg: #0f172a;
                          --card: #111827;
                          --accent: #22c55e;
                          --accent-2: #3b82f6;
                          --text: #e5e7eb;
                          --muted: #9ca3af;
                        }
                        * { box-sizing: border-box; }
                        body {
                          margin: 0;
                          font-family: ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial;
                          color: var(--text);
                          background: radial-gradient(1000px 500px at 10% 10%, #1f2937 10%, var(--bg) 55%);
                        }
                        header {
                          display:flex; align-items:center; justify-content:space-between;
                          padding: 18px 28px; position: sticky; top:0; backdrop-filter: blur(6px);
                          background: rgba(17,24,39,.6); border-bottom: 1px solid #1f2937;
                        }
                        header h1 { font-size: 20px; margin:0; letter-spacing:.5px; }
                        header nav a { color: var(--text); text-decoration:none; margin-left:18px; }
                        .container { max-width: 1080px; margin: 28px auto; padding: 0 20px; }
                        .hero {
                          display:grid; grid-template-columns: 1.2fr .8fr; gap: 24px; align-items:center;
                          padding: 28px; border-radius: 16px; background: linear-gradient(135deg, #0b1021, #121a33);
                          border: 1px solid #1f2937; box-shadow: 0 10px 35px rgba(0,0,0,.35);
                        }
                        .hero h2 { font-size: 28px; margin: 0 0 8px; }
                        .hero p { color: var(--muted); margin:0 0 16px; }
                        .badge { display:inline-block; padding:6px 10px; border-radius:999px; background:#0b3b25; color:#6ee7b7; border:1px solid #164e34; font-size:12px; }
                        .grid { display:grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 16px; margin-top: 22px; }
                        .card {
                          background: var(--card); border:1px solid #1f2937; border-radius: 14px; padding: 14px;
                          transition: transform .15s ease, box-shadow .15s ease, border-color .15s ease;
                        }
                        .card:hover { transform: translateY(-3px); border-color:#2a3a58; box-shadow:0 10px 24px rgba(0,0,0,.35); }
                        .card h3 { margin:0 0 6px; font-size:16px; }
                        .card .muted { color: var(--muted); font-size:13px; }
                        .price { color: #86efac; font-weight:600; }
                        .form {
                          background: var(--card); border:1px solid #1f2937; border-radius: 14px; padding: 16px; margin-top: 20px;
                        }
                        label { display:block; margin:.5rem 0 .25rem; font-size:13px; color:#cbd5e1; }
                        input, select {
                          width:100%; padding:.55rem .65rem; border-radius:10px; border:1px solid #283142; background:#0b1324; color: var(--text);
                        }
                        .row { display:grid; grid-template-columns: 1fr 1fr; gap: 12px; }
                        button {
                          margin-top:1rem; padding:.7rem 1rem; border-radius:12px; border:none; font-weight:600;
                          background: linear-gradient(135deg, var(--accent), #16a34a); color:#0a0f1f; cursor:pointer;
                        }
                        footer { color: var(--muted); text-align:center; margin: 26px 0 10px; font-size:12px; }
                        .toast { position: fixed; right: 16px; bottom: 16px; background:#04260f; color:#86efac; border:1px solid #14532d; padding: 10px 12px; border-radius: 10px; display:none; }
                      </style>
                    </head>
                    <body>
                      <header>
                        <h1>Flight Booking System</h1>
                        <nav>
                          <a href="/">Home</a>
                          <a href="/bookings">Bookings</a>
                        </nav>
                      </header>
                      <div class="container">
                        <section class="hero">
                          <div>
                            <span class="badge">Java Web • CSV Storage</span>
                            <h2>Plan your next trip</h2>
                            <p>Choose a flight and confirm your booking in seconds. Your bookings are stored locally and can be viewed anytime.</p>
                            <div class="grid" id="flight-grid"></div>
                          </div>
                          <div class="form">
                            <h3 style="margin:0 0 6px">Booking Details</h3>
                            <form id="booking-form" method="post" action="/book">
                              <label>Name</label>
                              <input name="name" required />
                              <label>Email</label>
                              <input name="email" type="email" required />
                              <div class="row">
                                <div>
                                  <label>Flight ID</label>
                                  <input name="flightId" id="flightId" placeholder="Select from cards" required />
                                </div>
                                <div>
                                  <label>Seat</label>
                                  <select name="seat" id="seat" required></select>
                                </div>
                              </div>
                              <div class="row">
                                <div>
                                  <label>Origin</label>
                                  <input name="origin" id="origin" required />
                                </div>
                                <div>
                                  <label>Destination</label>
                                  <input name="destination" id="destination" required />
                                </div>
                              </div>
                              <label>Price</label>
                              <input name="price" id="price" type="number" step="0.01" required />
                              <button type="submit">Confirm Booking</button>
                            </form>
                          </div>
                        </section>
                        <footer>Made with Java HttpServer • Demo purposes only</footer>
                      </div>
                      <div class="toast" id="toast">Booking form ready</div>
                      <script>
                        const flights = [
                          { id: "AI101", origin: "Mumbai", destination: "Delhi", price: 5999 },
                          { id: "UK202", origin: "Pune", destination: "Bengaluru", price: 4499 },
                          { id: "6E303", origin: "Delhi", destination: "Goa", price: 6999 },
                          { id: "SG404", origin: "Chennai", destination: "Kolkata", price: 4999 }
                        ];
                        const grid = document.getElementById("flight-grid");
                        grid.innerHTML = flights.map(f => `
                          <div class="card">
                            <h3>${f.id} • <span class="muted">${f.origin} → ${f.destination}</span></h3>
                            <div class="muted">Direct • 1 bag • Snacks</div>
                            <div style="display:flex; align-items:center; justify-content:space-between; margin-top:10px">
                              <div class="price">₹ ${f.price.toLocaleString()}</div>
                              <button type="button" data-id="${f.id}">Select</button>
                            </div>
                          </div>
                        `).join("");
                        grid.querySelectorAll("button[data-id]").forEach(btn => {
                          btn.addEventListener("click", () => {
                            const f = flights.find(x => x.id === btn.dataset.id);
                            document.getElementById("flightId").value = f.id;
                            document.getElementById("origin").value = f.origin;
                            document.getElementById("destination").value = f.destination;
                            document.getElementById("price").value = f.price;
                            showToast("Selected " + f.id);
                          });
                        });
                        const seatSel = document.getElementById("seat");
                        const rows = Array.from({length: 20}, (_, i) => i + 1);
                        const cols = ["A","B","C","D","E","F"];
                        seatSel.innerHTML = rows.flatMap(r => cols.map(c => `<option>${r}${c}</option>`)).join("");
                        const form = document.getElementById("booking-form");
                        form.addEventListener("submit", (e) => {
                          if (!document.getElementById("flightId").value) {
                            e.preventDefault();
                            showToast("Select a flight first");
                          }
                        });
                        function showToast(text) {
                          const t = document.getElementById("toast");
                          t.textContent = text;
                          t.style.display = "block";
                          setTimeout(() => t.style.display = "none", 1600);
                        }
                      </script>
                    </body>
                    </html>
                    """;
            respond(ex, 200, "text/html; charset=utf-8", html);
        }
    }

    static class BookHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                respond(ex, 405, "text/plain", "Method Not Allowed");
                return;
            }
            String body = readBody(ex.getRequestBody());
            Map<String, String> params = parseUrlEncoded(body);

            String timestamp = LocalDateTime.now().toString();
            String line = String.join(",",
                    escapeCsv(timestamp),
                    escapeCsv(params.getOrDefault("name", "")),
                    escapeCsv(params.getOrDefault("email", "")),
                    escapeCsv(params.getOrDefault("flightId", "")),
                    escapeCsv(params.getOrDefault("origin", "")),
                    escapeCsv(params.getOrDefault("destination", "")),
                    escapeCsv(params.getOrDefault("seat", "")),
                    escapeCsv(params.getOrDefault("price", ""))) + "\n";
            Files.writeString(BOOKINGS_CSV, line, StandardCharsets.UTF_8, StandardOpenOption.APPEND);

            String html = """
                    <!doctype html>
                    <html>
                      <head>
                        <meta charset="utf-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1">
                        <title>Booking Confirmed</title>
                        <style>
                          body { margin:0; font-family: ui-sans-serif, system-ui; background:#0f172a; color:#e5e7eb; display:grid; place-items:center; height:100vh; }
                          .card { background:#111827; border:1px solid #1f2937; border-radius:16px; padding:22px; width: min(520px, 92vw); box-shadow:0 10px 35px rgba(0,0,0,.35); }
                          h2 { margin:0 0 8px; }
                          .muted { color:#9ca3af; }
                          .row { display:grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-top: 12px; }
                          a.btn { display:inline-block; margin-top: 14px; padding: 10px 12px; border-radius: 12px; background:#22c55e; color:#0a0f1f; font-weight:600; text-decoration:none; }
                          a.btn.alt { background:#3b82f6; }
                        </style>
                      </head>
                      <body>
                        <div class="card">
                          <h2>Booking Confirmed</h2>
                          <div class="muted">Thank you, your ticket has been saved.</div>
                          <div class="row">
                            <div><strong>Flight</strong><br/>%s</div>
                            <div><strong>Seat</strong><br/>%s</div>
                            <div><strong>Route</strong><br/>%s → %s</div>
                            <div><strong>Amount</strong><br/>INR %s</div>
                          </div>
                          <a class="btn" href="/">Back</a>
                          <a class="btn alt" href="/bookings">View Bookings</a>
                        </div>
                      </body>
                    </html>
                    """.formatted(
                        escapeHtml(params.getOrDefault("flightId","")),
                        escapeHtml(params.getOrDefault("seat","")),
                        escapeHtml(params.getOrDefault("origin","")),
                        escapeHtml(params.getOrDefault("destination","")),
                        escapeHtml(params.getOrDefault("price",""))
                    );
            respond(ex, 200, "text/html; charset=utf-8", html);
        }
    }

    static class BookingsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            StringBuilder rows = new StringBuilder();
            if (Files.exists(BOOKINGS_CSV)) {
                for (String line : Files.readAllLines(BOOKINGS_CSV, StandardCharsets.UTF_8)) {
                    if (line.startsWith("timestamp,")) continue;
                    String[] cols = line.split(",", -1);
                    if (cols.length >= 8) {
                        rows.append("<tr>");
                        for (int i = 0; i < 8; i++) {
                            rows.append("<td>").append(escapeHtml(cols[i])).append("</td>");
                        }
                        rows.append("</tr>");
                    }
                }
            }
            String html = """
                    <!doctype html>
                    <html>
                      <head>
                        <meta charset="utf-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1">
                        <title>Bookings</title>
                        <style>
                          body { margin:0; font-family: ui-sans-serif, system-ui; background:#0f172a; color:#e5e7eb; }
                          header { display:flex; align-items:center; justify-content:space-between; padding: 18px 28px; background:#111827; border-bottom:1px solid #1f2937; }
                          .container { max-width: 1080px; margin: 18px auto; padding: 0 20px; }
                          table { width:100%; border-collapse: collapse; background:#111827; border:1px solid #1f2937; border-radius:14px; overflow:hidden; }
                          thead th { text-align:left; font-size:12px; letter-spacing:.3px; color:#9ca3af; background:#0b1324; padding: 10px; }
                          tbody td { padding: 10px; border-top:1px solid #1f2937; }
                          a { color:#93c5fd; text-decoration:none; }
                        </style>
                      </head>
                      <body>
                        <header>
                          <div>Flight Bookings</div>
                          <nav><a href="/">Home</a></nav>
                        </header>
                        <div class="container">
                          <table>
                            <thead>
                              <tr><th>Timestamp</th><th>Name</th><th>Email</th><th>Flight</th><th>Origin</th><th>Destination</th><th>Seat</th><th>Price</th></tr>
                            </thead>
                            <tbody>
                    """ + rows + """
                            </tbody>
                          </table>
                        </div>
                      </body>
                    </html>
                    """;
            respond(ex, 200, "text/html; charset=utf-8", html);
        }
    }

    private static void respond(HttpExchange ex, int status, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", contentType);
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String readBody(InputStream is) throws IOException {
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static Map<String, String> parseUrlEncoded(String body) throws IOException {
        Map<String, String> map = new LinkedHashMap<>();
        if (body == null || body.isEmpty()) return map;
        String[] pairs = body.split("&");
        for (String p : pairs) {
            String[] kv = p.split("=", 2);
            String k = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String v = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            map.put(k, v);
        }
        return map;
    }

    private static String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
