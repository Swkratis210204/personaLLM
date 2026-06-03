package com.personalllm.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.personalllm.model.Message;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class OllamaClient {

    private static final String BASE_URL        = "http://localhost:11434";
    private static final Duration TIMEOUT       = Duration.ofSeconds(5);
    private static final int STARTUP_WAIT_MS    = 500;
    private static final int STARTUP_RETRIES    = 10;
    private static final long HANG_TIMEOUT_MS   = 60_000;

    // Common install locations for the Ollama executable
    private static final List<String> OLLAMA_PATHS = List.of(
        System.getenv().getOrDefault("LOCALAPPDATA", "") + "\\Programs\\Ollama\\ollama.exe",
        "C:\\Program Files\\Ollama\\ollama.exe",
        "/usr/local/bin/ollama",
        "/usr/bin/ollama"
    );

    private final HttpClient http;
    private final Gson gson;

    public OllamaClient() {
        this.http = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();
        this.gson = new Gson();
    }

    // Returns true if Ollama is reachable right now.
    public boolean isRunning() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/tags"))
                    .GET()
                    .timeout(TIMEOUT)
                    .build();
            http.send(req, HttpResponse.BodyHandlers.discarding());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Tries to start Ollama in the background.
    // Returns true if Ollama becomes reachable within ~5 seconds.
    public boolean startOllama() {
        String execPath = findOllamaExecutable();
        if (execPath == null) return false;

        try {
            new ProcessBuilder(execPath, "serve")
                    .redirectErrorStream(true)
                    .start();

            for (int i = 0; i < STARTUP_RETRIES; i++) {
                Thread.sleep(STARTUP_WAIT_MS);
                if (isRunning()) return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private String findOllamaExecutable() {
        for (String path : OLLAMA_PATHS) {
            if (path.isBlank()) continue;
            if (Path.of(path).toFile().exists()) return path;
        }
        // Fallback: hope it's in PATH
        try {
            Process p = new ProcessBuilder("ollama", "serve")
                    .redirectErrorStream(true)
                    .start();
            p.destroy();
            return "ollama";
        } catch (Exception e) {
            return null;
        }
    }

    // Streams a chat response token by token.
    // Must be called from a background thread (not the EDT).
    // onToken receives each partial content string.
    // onDone is called once when the stream ends.
    // onError is called if the request fails.
    public void streamChat(String model,
                           String systemPrompt,
                           List<Message> history,
                           Consumer<String> onToken,
                           Runnable onDone,
                           Consumer<String> onError) {
        AtomicLong lastActivity = new AtomicLong(System.currentTimeMillis());
        Thread callerThread = Thread.currentThread();

        Thread watchdog = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(5_000);
                    if (System.currentTimeMillis() - lastActivity.get() > HANG_TIMEOUT_MS) {
                        callerThread.interrupt();
                        return;
                    }
                }
            } catch (InterruptedException ignored) {}
        });
        watchdog.setDaemon(true);
        watchdog.start();

        try {
            String body = buildChatRequest(model, systemPrompt, history);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/chat"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<java.io.InputStream> response = http.send(
                    request,
                    HttpResponse.BodyHandlers.ofInputStream()
            );

            if (response.statusCode() != 200) {
                onError.accept("Ollama returned status " + response.statusCode());
                return;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.body()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;

                    JsonObject json = JsonParser.parseString(line).getAsJsonObject();
                    boolean done = json.has("done") && json.get("done").getAsBoolean();

                    if (json.has("message")) {
                        String token = json.getAsJsonObject("message")
                                .get("content").getAsString();
                        if (!token.isEmpty()) {
                            lastActivity.set(System.currentTimeMillis());
                            onToken.accept(token);
                        }
                    }

                    if (done) break;
                }
            }

            watchdog.interrupt();
            onDone.run();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            onError.accept("Ollama stopped responding — please try again.");
        } catch (java.net.ConnectException e) {
            onError.accept("Lost connection to Ollama.");
        } catch (Exception e) {
            onError.accept("Error: " + e.getMessage());
        } finally {
            watchdog.interrupt();
        }
    }

    // Returns model names currently installed in Ollama.
    // Returns an empty list if Ollama is unreachable.
    public List<String> listModels() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/tags"))
                    .GET()
                    .timeout(TIMEOUT)
                    .build();

            HttpResponse<String> response = http.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray models = json.getAsJsonArray("models");

            List<String> names = new ArrayList<>();
            for (var element : models) {
                names.add(element.getAsJsonObject().get("name").getAsString());
            }
            return names;

        } catch (Exception e) {
            return List.of();
        }
    }

    private String buildChatRequest(String model, String systemPrompt, List<Message> history) {
        JsonObject body = new JsonObject();
        body.addProperty("model", model);
        body.addProperty("stream", true);

        JsonArray messages = new JsonArray();

        if (systemPrompt != null && !systemPrompt.isBlank()) {
            JsonObject sys = new JsonObject();
            sys.addProperty("role", "system");
            sys.addProperty("content", systemPrompt.trim());
            messages.add(sys);
        }

        for (Message msg : history) {
            JsonObject m = new JsonObject();
            m.addProperty("role", msg.getRole() == Message.Role.USER ? "user" : "assistant");
            m.addProperty("content", msg.getContent());
            messages.add(m);
        }

        body.add("messages", messages);
        return gson.toJson(body);
    }
}
