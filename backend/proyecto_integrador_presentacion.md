# Proyecto Integrador — Presentación de diseño

> **Curso:** Arquitectura Empresarial — PUCE · Kotlin + Spring Boot 4
> **Entrega:** **Presentación en diapositivas** · en clase del **sábado 18 de julio de 2026**
> **Modalidad:** **en parejas** (2 estudiantes) · exposición oral corta (~7–10 min por grupo)
> **Qué se entrega hoy:** el **diseño** del proyecto. Todavía **no** se programa nada.

---

## 1. ¿Qué es el Proyecto Integrador?

Es el proyecto **de fin de semestre**. Consiste en una **app móvil** conectada a un **backend** como los que
venimos construyendo en clase:

- **Arquitectura en capas**: `controllers`, `services`, `repositories`, `entities`, `dto`, `mappers`, `exceptions`, `config`.
- **Kotlin + Spring Boot 4**.
- **AWS Cognito** para autenticación (JWT).
- **PostgreSQL** como base de datos.

Esta primera entrega es **solo el diseño**: nos cuentan **qué** van a construir y **cómo** lo pensaron.
El código viene después. Por eso todo cabe en unas diapositivas.

> **La idea:** si el diseño está claro (entidades, relaciones, quién puede hacer qué), programarlo
> es "más de lo mismo" que ya hicimos con la polla y el estacionamiento. Un mal diseño no se arregla programando.

---

## 2. Reglas del proyecto (esto es lo que evaluamos)

| Requisito | Detalle |
|---|---|
| **Equipo** | En **parejas**. Los dos deben poder explicar cualquier parte. |
| **Entidades (tablas)** | **Mínimo 3, máximo 5.** Ni una más, ni una menos. |
| **Relaciones** | Al menos una relación entre tablas (`@OneToMany` / `@ManyToOne`, con su **FK**). |
| **Autenticación** | Con **Cognito** (JWT). Endpoints privados exigen token válido. |
| **Autorización** | **Por roles** (ej. `ADMIN`/`USER`) **o por usuario/propiedad** (cada quien ve/edita lo suyo). Puede ser una o ambas. |
| **Endpoints** | Debe quedar claro **cuáles son públicos y cuáles privados** (y con qué rol). |
| **Diagrama ER** | Obligatorio, con **PK, FK, tipos de dato y relaciones** (ver §4). |
| **App móvil** | Basta con describir **qué pantallas** tendría y **qué endpoint** consume cada una. Sin código todavía. |

> **Sobre las tablas:** los usuarios y los roles **NO** son tablas de tu backend. Viven en **Cognito**.
> Tus 3–5 tablas son de tu **dominio** (productos, reservas, mascotas, canchas, lo que sea), nunca `users`/`roles`.

---

## 3. Qué debe contener la presentación (guion de diapositivas)

Usen esta estructura. Puede ser una diapositiva por punto o agrupar; lo importante es que **estén todos**.

1. **Portada** — nombre del proyecto, integrantes, NRC.
2. **El problema / la idea** — ¿qué app es? ¿a quién sirve? En 2–3 frases.
3. **Actores y roles** — ¿quiénes usan la app? (ej. *dueño de local* vs *cliente*). ¿Qué puede hacer cada uno?
4. **Diagrama ER** — el corazón de la entrega. Con PK, FK, tipos y relaciones (ver §4).
5. **Modelo de autorización** — ¿usan roles, propiedad por usuario, o ambos? Justifíquenlo en una frase.
6. **Matriz de endpoints** — tabla de método + ruta + público/privado + rol requerido (ver §5).
7. **Pantallas de la app móvil** — un boceto simple (aunque sea a mano) y qué endpoint consume cada pantalla.
8. **Cierre** — qué es lo más difícil que anticipan y cómo piensan resolverlo.

> **En la exposición:** cada integrante habla de al menos una parte. Vamos a preguntar cosas como
> *"¿por qué esta tabla y no un campo?"*, *"¿este endpoint por qué es público?"*, *"¿este `403` de dónde sale?"*.

---

## 4. El diagrama ER (lo más importante)

El diagrama debe dejar **cero dudas** sobre la estructura de la base. Cada tabla debe mostrar:

- **El nombre de la tabla**.
- **Cada columna con su tipo de dato** (`BIGINT`, `VARCHAR`, `TIMESTAMP`, `BOOLEAN`, `DECIMAL`, etc.).
- **La PK** (Primary Key) claramente marcada — normalmente `id BIGINT`.
- **Las FK** (Foreign Key) claramente marcadas, indicando **a qué tabla apuntan**.
- **Las relaciones** entre tablas y su cardinalidad (1:N, N:1).

> **Cómo marcar:** usen `PK` para la llave primaria y `FK` para las foráneas, o el estándar que prefieran
> (crow's foot, UML). Lo que **no** puede faltar es: **qué columna es PK, qué columna es FK y de qué tipo es cada una**.

Herramientas sugeridas: **draw.io / diagrams.net**, dbdiagram.io, Lucidchart, o incluso a mano y foto — siempre
que se lea claro.

### Mini-ejemplo de cómo se lee una tabla en el diagrama

```
┌──────────── appointments ────────────┐
│ [PK] id          BIGINT               │
│ [FK] pet_id      BIGINT  → pets.id    │
│      status      VARCHAR(20)          │
│      notes       VARCHAR(255) (null)  │
└────────────────────────────────────────┘
        │ N
        │
        │ 1
┌──────────────── pets ────────────────┐
│ [PK] id          BIGINT               │
│      name        VARCHAR(60)          │
│      species     VARCHAR(30)          │
└────────────────────────────────────────┘
```

Se lee: *una `pet` tiene muchas `appointments`; cada `appointment` pertenece a una `pet`
(por eso `pet_id` es FK en `appointments`).*

---

## 5. Resumen de endpoints (matriz de autorización)

Incluyan una tabla como esta (aquí con el ejemplo de PetCare de la §6). Es la que traduce su idea a permisos concretos:

| Método | Endpoint | Acceso | Rol / regla |
|---|---|---|---|
| `GET` | `/slots/available` | **Público** | ninguno |
| `POST` | `/slots` | Privado | **`VET`** |
| `POST` | `/pets` | Privado | **`OWNER`** |
| `POST` | `/appointments` | Privado | **`OWNER`** |

Y anoten qué respuesta esperan en cada caso:

- **Sin token** en un endpoint privado → **`401`** (no sé quién eres).
- **Token válido pero rol equivocado** → **`403`** (sé quién eres y no puedes).
- **Autorización por propiedad** (editar/borrar algo que **no es tuyo**) → **`403`** que lanza **tu service**.

> Si necesitan repasar la diferencia entre `401`/`403`, roles vs propiedad, y cómo Cognito y Spring se
> reparten el trabajo, revisen **`autenticacion_vs_autorizacion.html`**.

---

## 6. Ejemplo completo de referencia

Este es el nivel de detalle que esperamos en su presentación. **Es solo un ejemplo para que vean el formato:
su proyecto debe ser de otro dominio (no de veterinarias).**

### Proyecto: *"PetCare" — reserva de citas veterinarias*

**La idea:** una app donde los **dueños de mascotas** agendan citas en una veterinaria, y el **personal**
(veterinarios) gestiona la agenda del día.

**Actores y roles (en Cognito):**
- `VET` (ADMIN del dominio): publica horarios disponibles y marca citas como atendidas.
- `OWNER` (USER): registra sus mascotas y agenda/cancela **sus** citas.
- Cualquiera (sin cuenta): ve la información pública de la veterinaria.

**Entidades: 3 tablas** (dentro del rango 3–5, y **sin** tabla de usuarios).

```
┌──────────── pets ────────────┐          ┌──────────── appointments ────────────┐
│ [PK] id         BIGINT        │  1     N │ [PK] id          BIGINT               │
│      name       VARCHAR(60)   │◀─────────│ [FK] pet_id      BIGINT → pets.id     │
│      species    VARCHAR(30)   │          │ [FK] slot_id     BIGINT → slots.id    │
│      owner_user VARCHAR(60)   │          │      status      VARCHAR(20)          │
└───────────────────────────────┘          │      notes       VARCHAR(255) (null)  │
                                            └────────────────────────────────────────┘
   owner_user = username del JWT                        N │
   (NO es FK a users: no hay tabla users)                 │ 1
                                            ┌──────────── slots ────────────┐
                                            │ [PK] id        BIGINT          │
                                            │      starts_at TIMESTAMP       │
                                            │      vet_name  VARCHAR(60)     │
                                            │      booked    BOOLEAN         │
                                            └─────────────────────────────────┘
```

**Relaciones:**
- Una **mascota** (`pets`) tiene muchas **citas** (`appointments`) → `pet_id` es FK.
- Un **horario** (`slots`) se usa en muchas **citas** → `slot_id` es FK.

**Matriz de endpoints:**

| Método | Endpoint | Acceso | Rol / regla |
|---|---|---|---|
| `GET` | `/slots/available` | Público | ninguno |
| `POST` | `/slots` | Privado | **`VET`** |
| `PATCH`| `/appointments/{id}/attended` | Privado | **`VET`** |
| `POST` | `/pets` | Privado | **`OWNER`** |
| `POST` | `/appointments` | Privado | **`OWNER`** |
| `DELETE`| `/appointments/{id}` | Privado | **`OWNER`** + **dueño de la cita** |

**Autorización usada:** *ambas*. **Por rol** (`VET` publica horarios, `OWNER` agenda) **y por propiedad**
(un `OWNER` solo puede cancelar **sus** citas → el `DELETE` valida `appointment.ownerUser == jwt.username`,
si no lanza `403` desde el service).

**Pantallas de la app (bocetos):**
- *Login* → Cognito.
- *Horarios disponibles* → `GET /slots/available`.
- *Mis mascotas* → `GET /pets/me` + `POST /pets`.
- *Agendar cita* → `POST /appointments`.

---

## 7. Rúbrica de la presentación

| Criterio | Puntos |
|---|---|
| Idea clara del proyecto + actores/roles bien definidos | 15 |
| **Diagrama ER** con PK, FK, **tipos de dato** y relaciones correctas | 30 |
| Cantidad de entidades dentro del rango (3–5) y sin tabla de usuarios/roles | 10 |
| **Matriz de endpoints** (público vs privado + rol) coherente | 20 |
| Modelo de **autenticación (Cognito)** y **autorización** (roles y/o propiedad) bien justificado | 15 |
| Bocetos de pantallas de la app conectadas a endpoints | 5 |
| Claridad de la exposición (ambos integrantes participan) | 5 |
| **Subir la presentación al aula virtual (en formato PDF)** | requisito para evaluar |
| **Total** | **100** |

---

## 8. Checklist antes de presentar

- [ ] Somos **2** integrantes y los dos entendemos todo el diseño.
- [ ] El proyecto tiene **entre 3 y 5 entidades** de dominio (ninguna es `users`/`roles`).
- [ ] Hay **al menos una relación** con su **FK** bien identificada.
- [ ] El **diagrama ER** muestra **PK, FK, tipo de dato de cada columna** y las **relaciones**.
- [ ] Definimos **autenticación con Cognito** y un modelo de **autorización** (roles y/o propiedad).
- [ ] Tenemos la **matriz de endpoints** (público / privado / rol) y sabemos dónde sale cada `401` y `403`.
- [ ] Tenemos **bocetos** de las pantallas de la app y qué endpoint consume cada una.
- [ ] Las diapositivas están listas para el **sábado 18 de julio**.

> **Recuerden:** esta entrega es el **diseño**. Un buen diagrama ER y una buena matriz de endpoints
> ahora = un backend que después se programa casi solo, con las mismas capas de siempre.
