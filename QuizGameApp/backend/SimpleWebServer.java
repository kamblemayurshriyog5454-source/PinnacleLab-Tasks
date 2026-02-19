package backend;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.File;
import java.nio.file.Files;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Scanner;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;

public class SimpleWebServer {

    private static QuizService quizService;

    public static void start(int port) throws IOException {
        // Initialize Service
        quizService = new QuizService();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Static File Handlers
        server.createContext("/", new StaticFileHandler("web/index.html"));
        server.createContext("/database.html", new StaticFileHandler("web/database.html"));
        server.createContext("/style.css", new StaticFileHandler("web/style.css"));
        server.createContext("/app.js", new StaticFileHandler("web/app.js"));

        // API Handlers
        server.createContext("/api/questions", new QuestionsHandler());
        server.createContext("/api/submit", new SubmitHandler());
        server.createContext("/api/scores", new ScoresHandler());
        server.createContext("/api/database", new DatabaseHandler());
        server.createContext("/api/delete_db", new DeleteDbHandler());
        server.createContext("/api/reinit_db", new ReinitDbHandler());

        server.setExecutor(null); // creates a default executor
        System.out.println("Server started on http://localhost:" + port);
        server.start();
    }

    static class StaticFileHandler implements HttpHandler {
        private String filePath;

        public StaticFileHandler(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            File file = new File(filePath);
            if (!file.exists()) {
                String response = "404 (Not Found)\n";
                t.sendResponseHeaders(404, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                t.sendResponseHeaders(200, file.length());
                OutputStream os = t.getResponseBody();
                Files.copy(file.toPath(), os);
                os.close();
            }
        }
    }

    static class QuestionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            // NOTE: QuizService needs a method to return ALL questions
            // Since we are hacking this into existing structure, let's just use reflection or modify QuizService.
            // But modifying QuizService is better.
            // For now, let's assume we modify QuizService to expose `questions` list or add getAllQuestions()
            
            // Build JSON manually
            List<Question> list = quizService.getAllQuestions(); 
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                Question q = list.get(i);
                json.append("{");
                json.append("\"question\": \"").append(escape(q.getQuestion())).append("\",");
                
                json.append("\"options\": [");
                String[] opts = q.getOptions();
                for (int j = 0; j < opts.length; j++) {
                    json.append("\"").append(escape(opts[j])).append("\"");
                    if (j < opts.length - 1) json.append(",");
                }
                json.append("],");

                json.append("\"correctAnswer\": ").append(q.getCorrectAnswer()).append(",");
                json.append("\"explanation\": \"").append(escape(q.getExplanation())).append("\"");
                json.append("}");
                if (i < list.size() - 1) json.append(",");
            }
            json.append("]");

            byte[] response = json.toString().getBytes("UTF-8");
            t.getResponseHeaders().set("Content-Type", "application/json");
            t.sendResponseHeaders(200, response.length);
            OutputStream os = t.getResponseBody();
            os.write(response);
            os.close();
        }

        private String escape(String s) {
            if (s == null) return "";
            return s.replace("\"", "\\\"").replace("\n", " ");
        }
    }

    static class SubmitHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if ("POST".equals(t.getRequestMethod())) {
                InputStream is = t.getRequestBody();
                Scanner s = new Scanner(is).useDelimiter("\\A");
                String body = s.hasNext() ? s.next() : "";
                
                // Parse simple JSON: {"username":"foo","score":1,"total":4}
                String username = extract(body, "username");
                int score = Integer.parseInt(extract(body, "score"));
                int total = Integer.parseInt(extract(body, "total"));

                quizService.saveScore(username, score, total);

                String response = "{\"status\":\"ok\"}";
                t.sendResponseHeaders(200, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }

        private String extract(String json, String key) {
            // Very hacky JSON parser
            int start = json.indexOf("\"" + key + "\"");
            if (start == -1) return "0";
            
            start = json.indexOf(":", start) + 1;
            while (json.charAt(start) == ' ' || json.charAt(start) == '"') start++;
            
            int end = start;
            while (end < json.length() && json.charAt(end) != '"' && json.charAt(end) != ',' && json.charAt(end) != '}') {
                end++;
            }
            return json.substring(start, end);
        }
    }

    static class ScoresHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            List<String> scores = quizService.getTopScores();
            
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < scores.size(); i++) {
                json.append("\"").append(scores.get(i).replace("\"", "\\\"")).append("\"");
                if (i < scores.size() - 1) json.append(",");
            }
            json.append("]");

            byte[] response = json.toString().getBytes("UTF-8");
            t.getResponseHeaders().set("Content-Type", "application/json");
            t.sendResponseHeaders(200, response.length);
            OutputStream os = t.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    static class DatabaseHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            StringBuilder json = new StringBuilder("{\"tables\": [");
            
            try (Connection conn = DBConnection.getConnection()) {
                if (conn != null) {
                    DatabaseMetaData dbMeta = conn.getMetaData();
                    ResultSet tables = dbMeta.getTables(null, null, "%", new String[] {"TABLE"});
                    
                    boolean firstTable = true;
                    while (tables.next()) {
                        String tableName = tables.getString("TABLE_NAME");
                        
                        // Skip internal/weird tables if necessary, but better to just handle errors
                        
                        if (!firstTable) json.append(",");
                        firstTable = false;

                        json.append("{");
                        json.append("\"name\": \"").append(tableName).append("\",");
                        
                        try (Statement stmt = conn.createStatement();
                             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {
                            
                            json.append("\"columns\": [");
                            ResultSetMetaData meta = rs.getMetaData();
                            int colCount = meta.getColumnCount();
                            
                            // Columns
                            for (int i = 1; i <= colCount; i++) {
                                json.append("\"").append(meta.getColumnName(i)).append("\"");
                                if (i < colCount) json.append(",");
                            }
                            json.append("], \"rows\": [");
                            
                            // Rows
                            boolean firstRow = true;
                            while (rs.next()) {
                                if (!firstRow) json.append(",");
                                firstRow = false;
                                json.append("[");
                                for (int i = 1; i <= colCount; i++) {
                                    String val = rs.getString(i);
                                    if (val == null) val = "NULL";
                                    json.append("\"").append(escape(val)).append("\"");
                                    if (i < colCount) json.append(",");
                                }
                                json.append("]");
                            }
                            json.append("]");
                            
                        } catch (Exception e) {
                            // If table read fails (e.g. ghost table), just return empty/error
                            json.append("\"error\": \"").append(escape(e.getMessage())).append("\",");
                            json.append("\"columns\": [], \"rows\": []");
                        }
                        
                        json.append("}");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            json.append("]}");

            byte[] response = json.toString().getBytes("UTF-8");
            t.getResponseHeaders().set("Content-Type", "application/json");
            t.sendResponseHeaders(200, response.length);
            OutputStream os = t.getResponseBody();
            os.write(response);
            os.close();
        }

        private String escape(String s) {
            if (s == null) return "";
            return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", "");
        }
    }
    static class DeleteDbHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            boolean ok = false;
            if ("POST".equals(t.getRequestMethod())) {
                ok = DatabaseInitializer.dropDatabase();
            }
            String response = ok ? "{\"status\":\"ok\"}" : "{\"status\":\"error\"}";
            byte[] bytes = response.getBytes("UTF-8");
            t.getResponseHeaders().set("Content-Type", "application/json");
            t.sendResponseHeaders(ok ? 200 : 500, bytes.length);
            OutputStream os = t.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

    static class ReinitDbHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            boolean ok = false;
            if ("POST".equals(t.getRequestMethod())) {
                ok = DatabaseInitializer.initialize();
            }
            String response = ok ? "{\"status\":\"ok\"}" : "{\"status\":\"error\"}";
            byte[] bytes = response.getBytes("UTF-8");
            t.getResponseHeaders().set("Content-Type", "application/json");
            t.sendResponseHeaders(ok ? 200 : 500, bytes.length);
            OutputStream os = t.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }
}
