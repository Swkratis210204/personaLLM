# Tech Stack Decisions

## Runtime
- **Java 21** (LTS) — modern, widely available, good HttpClient built-in
- **Maven** — standard build tool, easy dependency management

## UI Framework
- **Java Swing** — built into the JDK, no extra dependencies, works on all platforms
- No JavaFX — avoids extra setup and module system complexity for a simple app

## HTTP / Ollama Communication
- **java.net.HttpClient** (built-in since Java 11) — no third-party HTTP lib needed
- Ollama exposes a REST API at `http://localhost:11434`
- Key endpoints:
  - `POST /api/chat` — chat with history, supports streaming
  - `GET /api/tags` — list installed models

## JSON Parsing
- **Gson 2.x** — lightweight, simple API, good for parsing Ollama's streaming NDJSON

## Streaming
- Ollama streams responses as newline-delimited JSON (NDJSON)
- Each line is a partial message chunk: `{"message":{"content":"..."},"done":false}`
- We read line-by-line with `BufferedReader` and append to the chat UI on the EDT

## Threading Model
- Ollama calls run on a background thread (not the Event Dispatch Thread)
- UI updates via `SwingUtilities.invokeLater()`
- Send button disabled while a response is streaming; re-enabled on done

## Build Output
- Single fat JAR via `maven-assembly-plugin` (includes Gson)
- Run with: `java -jar personalllm.jar`

## Dependencies (pom.xml)
```xml
<dependency>
  <groupId>com.google.code.gson</groupId>
  <artifactId>gson</artifactId>
  <version>2.11.0</version>
</dependency>
```

## What We Are NOT Using
| Skipped              | Why                                          |
|----------------------|----------------------------------------------|
| LangChain4j          | Overkill for a simple single-model chat app  |
| Spring Boot          | Way too heavy for a desktop app              |
| JavaFX               | Extra setup, not needed for basic chat UI    |
| OkHttp               | java.net.HttpClient is sufficient            |
| SQLite               | No persistence in v1                         |

## Project Structure
```
personalllm/
├── pom.xml
├── src/
│   └── main/
│       └── java/
│           └── com/personalllm/
│               ├── Main.java          # entry point
│               ├── ui/
│               │   └── ChatWindow.java
│               ├── client/
│               │   └── OllamaClient.java
│               └── model/
│                   └── Message.java
└── docs/          (these planning MDs live here eventually)
```
