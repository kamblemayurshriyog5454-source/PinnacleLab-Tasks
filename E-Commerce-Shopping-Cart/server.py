import os
import time
import json
from http.server import SimpleHTTPRequestHandler, HTTPServer
from urllib.parse import parse_qs

ASSETS_DIR = os.path.join(os.path.dirname(__file__), "assets")
os.makedirs(ASSETS_DIR, exist_ok=True)

class Handler(SimpleHTTPRequestHandler):
    def end_headers(self):
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Headers", "Content-Type")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        super().end_headers()

    def do_OPTIONS(self):
        self.send_response(204)
        self.end_headers()

    def do_POST(self):
        if self.path != "/upload":
            self.send_error(404, "Not Found")
            return
        ctype = self.headers.get('Content-Type', '')
        if "multipart/form-data" not in ctype:
            self.send_error(400, "Expected multipart/form-data")
            return

        boundary = ctype.split("boundary=")[-1].encode()
        length = int(self.headers.get('Content-Length', 0))
        body = self.rfile.read(length)

        parts = body.split(b"--" + boundary)
        saved_path = None
        for part in parts:
            if b"Content-Disposition" in part and b"name=\"file\"" in part:
                header, _, filedata = part.partition(b"\r\n\r\n")
                header_str = header.decode(errors="ignore")
                # Extract filename
                fname = "upload_" + str(int(time.time())) + ".jpg"
                for token in header_str.split(";"):
                    token = token.strip()
                    if token.startswith("filename="):
                        raw = token.split("=", 1)[1].strip().strip('"')
                        base = os.path.basename(raw)
                        if base:
                            fname = str(int(time.time())) + "_" + base
                filedata = filedata.rstrip(b"\r\n")
                path = os.path.join(ASSETS_DIR, fname)
                with open(path, "wb") as f:
                    f.write(filedata)
                saved_path = "/assets/" + fname
                break

        if not saved_path:
            self.send_error(400, "No file found in upload")
            return

        data = {"path": saved_path}
        payload = json.dumps(data).encode()
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(payload)))
        self.end_headers()
        self.wfile.write(payload)

def main():
    port = int(os.environ.get("PORT", "8081"))
    os.chdir(os.path.dirname(__file__))
    httpd = HTTPServer(("0.0.0.0", port), Handler)
    print(f"Serving on http://localhost:{port}/")
    httpd.serve_forever()

if __name__ == "__main__":
    main()
