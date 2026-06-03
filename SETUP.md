# Setup Guide

PersonalLLM runs fully offline. No accounts, no API keys, no internet after setup.
You need two things installed before launching the app: **Java** and **Ollama**.

---

## 1. Java (required to run the app)

PersonalLLM is a Java app. You need the Java Runtime Environment (JRE) version 19 or newer.

**Check if you already have it:**
```
java -version
```
If you see `version "19"` or higher, you're good. Skip to step 2.

**Download Java:**
- Go to: https://adoptium.net
- Download the **JRE 21 (LTS)** installer for your operating system
- Run the installer, follow the steps, done

---

## 2. Ollama (required to run AI models locally)

Ollama is the engine that runs the AI model on your machine.

**Download Ollama:**
- Go to: https://ollama.com/download
- Download and install for your OS (Windows / macOS / Linux)
- After install, Ollama runs silently in the background

**Verify it's running:**
```
ollama list
```
If it prints a table (even empty), Ollama is working.

---

## 3. Download a model

Once Ollama is installed, pull a model. We recommend `gemma3:4b` — it's fast, good quality,
and fits on most computers with 4GB+ of GPU memory (or runs on CPU if needed).

```
ollama pull gemma3:4b
```

This downloads ~2.5 GB. Do it once; it stays on your machine forever.

**Lower-end PC? Use the smaller model instead:**
```
ollama pull gemma3:1b
```
Only ~800 MB, very fast, slightly less capable.

---

## 4. Run PersonalLLM

Once Java and Ollama are ready:

```
java -jar personalllm.jar
```

Or double-click `personalllm.jar` if your OS opens JAR files with Java automatically.

The app will connect to Ollama automatically. Select your model from the dropdown and start chatting.

---

## Troubleshooting

| Problem | Fix |
|---|---|
| `java` not found | Install Java from https://adoptium.net and restart your terminal |
| App opens but no models in dropdown | Run `ollama list` — if empty, pull a model (step 3) |
| App says "Could not connect to Ollama" | Open a terminal and run `ollama serve` to start Ollama manually |
| Very slow responses | Your model may be running on CPU. Try `gemma3:1b` for faster output |

---

## System Requirements

| | Minimum | Recommended |
|---|---|---|
| OS | Windows 10, macOS 12, Ubuntu 20.04 | Windows 11, macOS 13+, Ubuntu 22.04 |
| RAM | 8 GB | 16 GB |
| GPU VRAM | None (CPU fallback) | 4 GB (runs gemma3:4b fully on GPU) |
| Storage | 3 GB free | 5 GB free |
| Java | 19+ | 21 LTS |

---

*A setup script that installs everything automatically is planned for a future release.*
