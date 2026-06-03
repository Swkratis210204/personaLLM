# personaLLM

A lightweight desktop chat app that runs AI locally on your machine.
No cloud. No API keys. No subscriptions. Fully offline after setup.

Powered by [Ollama](https://ollama.com) and `gemma3:4b`.

---

## What it looks like

> Chat window with streaming responses, a system prompt field to customize the assistant's personality, and a clear history button.

---

## Requirements

- **Java 19+** — [Download from Adoptium](https://adoptium.net)
- **Ollama** — [Download from ollama.com](https://ollama.com/download)
- **gemma3:4b model** — run once after installing Ollama:
  ```
  ollama pull gemma3:4b
  ```

For detailed setup instructions see [SETUP.md](SETUP.md).

---

## Running the app

Download the latest `personalllm.jar` from the [Releases](../../releases) page and run:

```
java -jar personalllm.jar
```

Ollama starts automatically in the background when you launch the app.

---

## Building from source

Requires Java 19+ and Maven.

```
git clone https://github.com/your-username/personalllm.git
cd personalllm
mvn package
java -jar target/personalllm.jar
```

---

## Features

- Streaming responses — text appears token by token as it's generated
- System prompt — customize the assistant's persona from the top bar
- Clear History — wipe the conversation and start fresh
- Auto-starts Ollama — no need to launch it manually
- Runs 100% offline after initial model download

---

## System requirements

| | Minimum | Recommended |
|---|---|---|
| OS | Windows 10, macOS 12, Ubuntu 20.04 | Windows 11 |
| RAM | 8 GB | 16 GB |
| GPU VRAM | None (CPU fallback, slow) | 4 GB |
| Storage | 4 GB free | 6 GB free |
| Java | 19+ | 21 LTS |

---

## License

MIT
