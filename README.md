# Async Task Processor

Proyecto de aprendizaje: una API que recibe tareas pesadas, las encola y las procesa con workers en background, con reintentos, prioridades, tareas programadas y manejo de errores.

**Stack:** Spring Boot 3.5, Java 21, PostgreSQL, RabbitMQ, Docker Compose.

## Por qué

`POST /tasks` no debería hacer esperar al cliente mientras se procesa el trabajo pesado. La API valida, persiste la tarea con estado `PENDING` y responde `202 Accepted` con un id — el procesamiento real lo hace un worker asincrono que consume de una cola.

## Arquitectura actual

```mermaid
flowchart LR
    Client([Cliente]) -->|"POST /tasks"| Controller["TaskController"]
    Controller --> Service["TaskService.createTask()"]
    Service -->|"save, status=PENDING"| DB[("Postgres")]
    Service -->|"convertAndSend(id)"| Exchange{{"task.exchange"}}
    Exchange --> Queue[["task.queue"]]
    Queue -->|"@RabbitListener"| Worker["TaskWorker"]
    Worker -->|"PROCESSING → DONE/FAILED"| DB
    Controller -->|"202 Accepted + id"| Client
```

El mensaje de la cola lleva solo el `id` de la tarea, no el payload — el payload pesado vive en Postgres. Mensajes livianos y estado siempre consultable aunque el worker esté caído.

Reintentos con backoff exponencial vía TTL + Dead Letter Exchange: si el worker falla, el mensaje va a `task.retry` con un TTL creciente (`2^intentos * 10s`); al expirar, vuelve a `task.queue` para reintentarse. Agotados los reintentos, la tarea pasa a `FAILED` (con el error guardado) y el mensaje se publica en `task.dlq`. `GET /tasks/failed` lista las tareas en ese estado.

Colas de prioridad: `task.queue` declarada con `x-max-priority=10`. `POST /tasks` acepta un campo opcional `priority` (0-10, default 0 si no se manda) que se setea en el mensaje vía `MessagePostProcessor` al publicar. Con varios mensajes esperando en la cola, RabbitMQ entrega primero los de mayor prioridad — no reordena mensajes ya entregados ni afecta tareas que llegan cuando la cola está vacía.

## Cómo levantarlo

```bash
docker compose up -d       # Postgres + RabbitMQ
./mvnw spring-boot:run      # API en http://localhost:8080
```

RabbitMQ management UI: http://localhost:15672 (`guest`/`guest`)

```bash
curl -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{"type": "demo", "payload": "hello", "priority": 8}'
```

## Estado

Proyecto en curso, construido objetivo por objetivo:

- [x] **Objetivo 1** — API que encola tareas
- [x] **Objetivo 2** — Worker que consume y procesa
- [x] **Objetivo 3** — Reintentos con backoff exponencial
- [x] **Objetivo 4** — Dead Letter Queue
- [x] **Objetivo 5** — Colas de prioridad *(implementado, confirmando en vivo)*
- [ ] **Objetivo 6** — Tareas programadas
- [ ] **Objetivo 7** — Endpoint de consulta de estado
- [ ] **Objetivo 8** — Dockerizar todo el sistema (API + worker + broker)
- [ ] **Objetivo 9** — Escalar workers + idempotencia
