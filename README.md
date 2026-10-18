# SharedExpenses

Aplicación web para gestionar gastos compartidos en grupo (viajes, pisos, cenas): registra quién
pagó qué, calcula el balance de cada miembro y propone el mínimo número de pagos para saldar las deudas.

Trabajo de Fin de Máster — Máster de Desarrollo con IA (BIG School).

> 🚧 Documentación en construcción. Se completa al final del desarrollo.

## Descripción general

_Pendiente._

## Stack tecnológico

- **Backend:** Java 21, Spring Boot 3.5, Spring Security (JWT), Spring Data JPA, springdoc-openapi, Maven
- **Base de datos:** PostgreSQL (Neon) en producción, H2 en desarrollo, migraciones con Flyway
- **Frontend:** React + Vite, React Router
- **Infraestructura:** Docker, docker-compose, Render, Vercel

## Instalación y ejecución

Requisitos: JDK 21, Node.js 22 LTS, Docker.

```bash
# Backend en local (perfil dev, H2 en memoria)
cd backend
./mvnw spring-boot:run          # http://localhost:8080/swagger-ui.html

# Backend + PostgreSQL con Docker
cp .env.example .env            # opcional: ajusta los valores
docker compose up --build

# Frontend (con el backend arrancado en el puerto 8080)
cd frontend
cp .env.example .env            # VITE_API_URL=http://localhost:8080
npm install
npm run dev                     # http://localhost:5173
```

## Estructura del proyecto

```
.
├── backend/        # API REST Spring Boot (monolito modular, hexagonal)
├── frontend/       # SPA React + Vite (api/, auth/, pages/, components/, utils/)
├── docs/           # Diagramas, capturas y slides
└── docker-compose.yml
```

## Funcionalidades principales

- Registro e inicio de sesión con JWT.
- Grupos: crear, listar, ver detalle, añadir miembros por email, eliminar miembros (solo el creador) o abandonar el grupo.
- Gastos con tres tipos de reparto: a partes iguales, por importes o por porcentajes. Se pueden editar y eliminar. Las partes suman siempre el
  importe exacto; los céntimos sobrantes se asignan de forma determinista.
- Balance de cada miembro (la suma del grupo es siempre 0) y liquidación sugerida con el mínimo de pagos (como mucho N−1).
- Registro de pagos entre miembros para saldar deudas, con historial y aviso si un pago supera la deuda.
- Edición y eliminación de grupos (solo el creador puede eliminarlos).
- Cuenta de usuario: cambio de contraseña y eliminación de la cuenta (los datos personales se anonimizan y el
  historial compartido se conserva para el resto de miembros).

## Documentación de la API

Swagger UI: `http://localhost:8080/swagger-ui.html` (en local). Para probar los endpoints protegidos, haz login en
`POST /api/auth/login` y pega el `token` en el botón **Authorize**.

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/auth/register` | Registro (devuelve token) |
| POST | `/api/auth/login` | Inicio de sesión (devuelve token) |
| PUT | `/api/users/me/password` | Cambiar contraseña |
| DELETE | `/api/users/me` | Eliminar la cuenta |
| GET | `/api/groups` | Mis grupos |
| POST | `/api/groups` | Crear grupo |
| GET | `/api/groups/{groupId}` | Detalle con miembros |
| PUT | `/api/groups/{groupId}` | Modificar nombre y descripción |
| DELETE | `/api/groups/{groupId}` | Eliminar grupo (solo el creador) |
| POST | `/api/groups/{groupId}/members` | Añadir miembro por email |
| DELETE | `/api/groups/{groupId}/members/{memberId}` | Eliminar miembro / abandonar el grupo |
| GET | `/api/groups/{groupId}/expenses` | Gastos del grupo |
| POST | `/api/groups/{groupId}/expenses` | Registrar gasto |
| PUT | `/api/groups/{groupId}/expenses/{expenseId}` | Modificar gasto |
| DELETE | `/api/groups/{groupId}/expenses/{expenseId}` | Eliminar gasto |
| GET | `/api/groups/{groupId}/balances` | Balance de cada miembro |
| GET | `/api/groups/{groupId}/settlement` | Liquidación sugerida |
| GET | `/api/groups/{groupId}/payments` | Pagos registrados |
| POST | `/api/groups/{groupId}/payments` | Registrar pago |
| DELETE | `/api/groups/{groupId}/payments/{paymentId}` | Eliminar pago |

Los errores siguen el formato estándar `ProblemDetail` (RFC 9457):
400 datos no válidos, 401 sin sesión, 403 sin permiso, 404 no encontrado, 409 conflicto, 422 regla de negocio,
429 demasiados intentos.

Protección contra bots en la autenticación:
- Límite de peticiones: como máximo 5 registros por hora por IP y 50 en total, y 10 intentos de login cada
  5 minutos por IP.
- CAPTCHA en el registro con Cloudflare Turnstile (gratuito). El backend lo activa si existe la variable
  `TURNSTILE_SECRET`, y el frontend muestra el widget si existe `VITE_TURNSTILE_SITE_KEY`.

## Usuario de prueba

Las credenciales del usuario de prueba se facilitan en el formulario de entrega del TFM. La cuenta incluye dos
grupos de ejemplo con gastos, balances y pagos.

## Despliegue

_Pendiente (frontend en Vercel, backend en Render, BD en Neon)._

## Presentación y vídeo

_Pendiente._
