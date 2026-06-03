# PersonalLLM — Project Plan

## Goal
A lightweight Java desktop chat app that runs local LLMs via Ollama.
No cloud, no API keys, no cost. Runs fully offline after initial model download.

## Constraints
- RTX 1650 (4GB VRAM) — limits usable model size (see MODELS.md)
- Java only — no Node, no Python runtime required to run the app
- Free forever — no paid APIs, no subscriptions
- Simple to use — one JAR or launcher, no complex setup for end user

---

## v1 — Core Chat (current)

**Goal:** A working chat app. Basic, solid, shippable.

- [ ] Chat window with user / assistant message bubbles
- [ ] Text input + Send button (Enter to send)
- [ ] Fixed model: gemma3:4b (no selector — keeps it simple, saves disk space)
- [ ] Streaming responses (text appears token by token)
- [ ] Clear conversation button
- [ ] System prompt field (set the assistant's persona / context)

**Out of scope for v1:**
- Saving conversation history to disk
- Multiple chat sessions / tabs
- Image input
- Auto-installer script

---

## v2 — History & Broader Model Support

**Goal:** Make the app more useful day-to-day. Conversations persist, more models work well.

- [ ] Save conversation history to disk (JSON files, one per session)
- [ ] Load / browse past conversations from a sidebar or menu
- [ ] Delete individual conversations
- [ ] Single model: gemma3:4b across all versions (no multi-model support — disk space concern)
- [ ] Auto-detect if Ollama is not running and show a clear actionable error
- [ ] Auto-installer script — installs Java + Ollama + pulls default model in one click

**Storage approach (planned):**
- Conversations saved as JSON in a local `conversations/` folder next to the JAR
- No database — plain files, easy to back up or delete
- File format: `{timestamp}_{first_few_words_of_chat}.json`

---

## v3 — Image Input (Multimodal)

**Goal:** Send images to models that support vision (e.g. `llava`, `gemma3` multimodal variants).

- [ ] Image attachment button in the chat input area
- [ ] Drag-and-drop image onto the chat window
- [ ] Preview thumbnail before sending
- [ ] Send image + text together to the model (Ollama multimodal API)
- [ ] Graceful fallback — if selected model doesn't support images, show a clear message

**Notes:**
- Only static images (PNG, JPG, WEBP) — no video, no audio
- Model must support vision; not all Ollama models do
- VRAM requirement goes up with image input — will document in MODELS.md

---

## Build Phases

| Phase | Description | Version |
|---|---|---|
| 1 | Docs — planning MDs | v1 |
| 2 | Scaffold — Maven structure, stubs | v1 ✓ |
| 3 | Core — OllamaClient, streaming | v1 ✓ |
| 4 | UI — Swing chat window | v1 |
| 5 | Polish — model selector, system prompt, shortcuts | v1 |
| 6 | Package — fat JAR + launcher | v1 |
| 7 | History — save/load conversations | v2 |
| 8 | Model support — Llama/Mistral/Phi, model info UI | v2 |
| 9 | Auto-installer script | v2 |
| 10 | Image input — multimodal support | v3 |

---

## Open Questions (v1)
- Dark mode by default, or light with a toggle?
- Installer (jpackage) or just a JAR for v1?
