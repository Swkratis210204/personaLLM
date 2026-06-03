# personaLM — Project Plan

## Goal
A lightweight Java desktop chat app running AI locally via Ollama.
No cloud, no API keys, no cost. Fully offline after initial setup.
The assistant is named **Anastasia**.

## Constraints
- RTX 1650 (4GB VRAM) — single model only: `gemma3:4b`
- Java only — no Node, no Python runtime required
- Free forever — no paid APIs, no subscriptions
- Simple to use — one JAR, no complex setup for end users

---

## v1 — Core Chat ✅ COMPLETE

- [x] Chat window with user / Anastasia message bubbles
- [x] Streaming responses (token by token)
- [x] Fixed model: gemma3:4b hardcoded
- [x] System prompt field
- [x] Clear History button + Ctrl+L shortcut
- [x] Welcome message from Anastasia on launch and after clear
- [x] Status bar (Ready / thinking... / errors)
- [x] Hang guard — 60s watchdog, recovers if Ollama freezes
- [x] Auto-start Ollama on launch
- [x] App icon
- [x] Enter to send, cursor stays in input while streaming

**Deferred to after v2:** fat JAR build + GitHub Release (want UI polish first)

---

## v2 — History + Distribution + UI Polish (next)

**Goal:** Conversations persist, app is ready to ship to friends.

### History
- [ ] Save each conversation as JSON in a `conversations/` folder
- [ ] Sidebar listing past conversations (click to reload)
- [ ] Delete individual conversations
- [ ] File naming: `{timestamp}_{first_few_words}.json`
- [ ] No database — plain JSON files only

### Distribution
- [ ] Build fat JAR (`mvn package` → `personallm.jar`)
- [ ] Test JAR runs standalone without Maven
- [ ] GitHub Release with JAR attached
- [ ] Update README with download link and screenshot

### UI Polish
- [ ] TBD — user will review current UI and specify what feels off

---

## v3 — Image Input (Multimodal)

- [ ] Image attach button + drag-and-drop
- [ ] Preview thumbnail before sending
- [ ] Send image + text to model (Ollama multimodal API)
- [ ] Graceful fallback if model doesn't support vision
- [ ] PNG / JPG / WEBP only — no video, no audio

---

## v4 — Native App

- [ ] Package with `jpackage` → `.exe` installer on Windows
- [ ] Real app icon in Start Menu, no terminal needed
- [ ] Auto-update mechanism (TBD)

---

## Build Phases

| Phase | Description | Status |
|---|---|---|
| 1 | Docs | ✅ |
| 2 | Scaffold — Maven, stubs | ✅ |
| 3 | OllamaClient — streaming, hang guard | ✅ |
| 4 | UI — chat window, Anastasia, status bar | ✅ |
| 5 | Polish — icon, shortcuts, welcome message | ✅ |
| 6 | History — save/load/delete conversations | v2 |
| 7 | Distribution — JAR, GitHub Release | v2 |
| 8 | UI sharpening | v2 |
| 9 | Image input — multimodal | v3 |
| 10 | Native installer — jpackage | v4 |
