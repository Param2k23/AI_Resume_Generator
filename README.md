# AI Resume Generator

AI Resume Generator is a three-tier application that turns a free-form career description into a structured, designer-friendly resume. The React client collects user input, a Spring Boot backend orchestrates AI calls, and a dedicated Spring AI microservice talks to a local Ollama instance (DeepSeek R1) to produce JSON the UI can edit, preview, and export.

## Architecture at a Glance

```
ai_resume_generator/
+- resume-ai-microservice/    # Spring Boot + Spring AI gateway to Ollama (port 8081)
+- resume-ai-backend/         # Spring Boot REST API consumed by the UI (port 8080)
+- resume_frontend/           # React + Vite client (dev server on 5173)
```

1. `resume_frontend` sends descriptions or edited resumes to the backend via Axios.
2. `resume-ai-backend` exposes `POST /api/v1/resume/generate`, calls the AI microservice, normalizes the response, and returns both the model's "thinking" stream and the JSON payload.
3. `resume-ai-microservice` loads the prompt from `src/main/resources/prompts/resume_prompt.txt`, calls `spring-ai`'s Ollama client, and relays the DeepSeek output.

## Key Features

- **AI-tailored resumes** powered by the `deepseek-r1:1.5b` model running inside Ollama.
- **Prompt-engineered JSON contract** that guarantees stable keys for personal info, skills, experience, education, certifications, projects, achievements, languages, and interests.
- **Manual editing workflow** built with React Hook Form, dynamic `useFieldArray` sections, validation, and toast feedback.
- **Instant preview & export** via the `Resume` component, HTML-to-image rendering, and on-demand PDF download/print actions.
- **Modular deployment** lets you scale or swap any layer (UI, API, inference service, or model) independently.
- **Developer-friendly stack** using Vite + Tailwind/DaisyUI on the front end and Spring Boot 3.2+/Spring AI on the back end.

## Prerequisites

- Java 21 and Maven 3.9+ on your PATH.
- Node.js 18+ (or newer) and npm.
- [Ollama](https://ollama.com/) running locally (default `http://localhost:11434`) with the `deepseek-r1:1.5b` model pulled: `ollama pull deepseek-r1:1.5b`.
- Open ports 8080 (REST API), 8081 (AI microservice), and 5173 (Vite dev server).
- A modern Chromium-based browser for the front-end preview/export features.

## Quick Start

### 1. AI inference microservice (port 8081)

```bash
cd ai_resume_generator/resume-ai-microservice/demo
ollama pull deepseek-r1:1.5b        # no-op if already installed
mvn spring-boot:run
```

- Configuration lives in `src/main/resources/application.properties`:
  - `spring.ai.ollama.base-url` – where your Ollama socket is exposed.
  - `spring.ai.ollama.chat.options.model` – swap models if needed.
  - `spring.ai.ollama.chat.options.temperature` – control creativity.
- Sanity check once the service is running:

```bash
curl -X POST http://localhost:8081/api/ai/generate \
  -H "Content-Type: application/json" \
  -d "{\"userDescription\":\"Full-stack engineer with 5 years in fintech...\"}"
```

You should see the model's raw response that includes `<think>` tags and a JSON block.

### 2. Resume API backend (port 8080)

```bash
cd ai_resume_generator/resume-ai-backend
mvn spring-boot:run
```

- By default the backend calls `http://localhost:8081/api/ai/generate`. Override it with `--ai.service.url=<url>` or by adding `ai.service.url=<url>` to `src/main/resources/application.properties` if the microservice lives elsewhere.
- The service returns a payload shaped as `{ "think": "...", "data": { ...resume fields... } }`, which the UI consumes directly.
- `@CrossOrigin("*")` is enabled for quick local development. Lock it down before production.

### 3. React front-end (Vite on 5173)

```bash
cd ai_resume_generator/resume_frontend
npm install
npm run dev -- --host
```

- Update `src/api/ResumeService.js` (`baseURLL`) if your backend is not on `http://localhost:8080`.
- `npm run build` emits a production-ready bundle inside `dist/`.
- The Generate Resume page guides users through:
  1. Entering a natural-language description.
  2. Reviewing the AI-generated form data.
  3. Editing/augmenting sections.
  4. Downloading/printing the styled resume.

## API Reference

`POST http://localhost:8080/api/v1/resume/generate`

**Request**

```json
{
  "userDescription": "Cloud-focused DevOps engineer with AWS, Terraform, EKS..."
}
```

**Response**

```json
{
  "think": "Deliberation text from the DeepSeek reasoning model...",
  "data": {
    "personalInformation": {
      "fullName": "Alex Taylor",
      "email": "alex@example.com",
      "phoneNumber": "+1 555-123-4567",
      "location": "Austin, TX",
      "linkedIn": "https://linkedin.com/in/alextaylor",
      "gitHub": "https://github.com/alextaylor",
      "portfolio": null
    },
    "summary": "SRE with 6+ years...",
    "skills": [
      { "title": "Kubernetes", "level": "Expert" }
    ],
    "experience": [],
    "education": [],
    "projects": []
  }
}
```

- `think` can be displayed in the UI or logged for debugging.
- `data` mirrors the form schema in `resume_frontend/src/pages/GenerateResume.jsx`.

## Prompt & Output Customization

- Edit `resume-ai-microservice/demo/src/main/resources/prompts/resume_prompt.txt` to change the JSON contract, tone, or additional sections.
- Swap Ollama models (`spring.ai.ollama.chat.options.model`) to experiment with different LLMs.
- Extend the React form/preview by adding fields to `GenerateResume.jsx` and `components/Resume.jsx`. The backend will pass through any keys emitted by the microservice.
- If you need additional sanitation before the UI consumes the payload, update `ResumeServiceImpl.parseMultipleResponses(...)`.

## Useful Commands

| Area | Command | Purpose |
|------|---------|---------|
| AI microservice | `mvn spring-boot:run` | Starts the Spring AI service on port 8081. |
| AI microservice | `mvn clean package` | Builds an executable JAR under `target/`. |
| Backend API | `mvn spring-boot:run` | Runs the orchestration API on port 8080. |
| Front-end | `npm run dev` | Launches the Vite dev server with hot reload. |
| Front-end | `npm run build` | Produces production assets inside `dist/`. |
| Front-end | `npm run lint` | Runs ESLint with the project rules. |

## Troubleshooting

- **HTTP 500 / connection refused** – ensure the microservice is up on port 8081 and reachable via the URL configured in `ai.service.url`.
- **Ollama errors or empty responses** – verify `ollama serve` is running and that `deepseek-r1:1.5b` (or your chosen model) is present via `ollama list`.
- **CORS issues when deploying** – replace the global `@CrossOrigin("*")` in `ResumeController` with an allowed-origins list that matches your hosting domain.
- **Blank or cropped PDF exports** – keep the resume section within A4 proportions and avoid browser zooming before pressing the **Print** button.
- **Model output schema drift** – update the prompt template so every required key is always present, or add null-safe defaults in the backend parser.

---

Feel free to adapt any layer (model, prompt, UI theme, storage) to match your production needs. The current setup is intentionally simple so you can swap components without rewriting the entire stack.

