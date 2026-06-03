# Model Selection — RTX 1650 (4GB VRAM)

## VRAM Budget
RTX 1650 has exactly 4GB VRAM. Rule of thumb for Ollama with Q4_K_M quantization:
- Model params × ~0.6 ≈ VRAM needed in GB

## Gemma 3 Options

| Model         | VRAM (Q4_K_M) | Speed (RTX 1650) | Quality  | Verdict          |
|---------------|---------------|------------------|----------|------------------|
| gemma3:1b     | ~0.8 GB       | Very fast        | Basic    | Good for testing |
| gemma3:4b     | ~2.7 GB       | Fast             | Good     | **Recommended**  |
| gemma3:8b     | ~5.2 GB       | Slow (CPU spill) | Better   | Too big          |
| gemma3:12b    | ~7.5 GB       | Very slow        | Best     | Not viable       |

## Recommendation
**Default: `gemma3:4b`** — fits comfortably in 4GB VRAM, good quality for chat,
fast enough for a smooth experience. Keep `gemma3:1b` as a fallback for low-load use.

## Why Not gemma3:8b?
When a model doesn't fully fit in VRAM, Ollama offloads layers to CPU RAM.
This causes a major speed drop (tokens/sec falls ~10x). On a 1650 with 4GB,
gemma3:8b Q4 (~5.2GB) would partially spill and feel sluggish.

## Quantization Notes
Ollama downloads Q4_K_M by default — good balance of size vs quality.
Avoid Q2 (too degraded). Q5/Q8 only if you have more VRAM headroom.

## Other Models to Try (via Ollama)
These also work well on a 1650:
- `llama3.2:3b` — fast, good general assistant
- `mistral:7b-instruct-q4_K_M` — borderline, may spill slightly
- `phi4-mini` — very fast, surprisingly capable for its size

## Ollama Commands
```
ollama pull gemma3:4b
ollama pull gemma3:1b
ollama list           # see installed models
ollama run gemma3:4b  # test in terminal
```
