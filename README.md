# 🤖 BetMaster AI — Asistente de Apuestas Deportivas

Aplicación Android nativa (Java) que descarga cuotas deportivas en tiempo real,
aplica un motor de inteligencia artificial para detectar **Value Bets** y ayuda al
usuario a gestionar su bankroll con el **Criterio de Kelly**.

> Proyecto académico · UDG · Equipo de 4 personas

---

## 📑 Tabla de Contenidos

1. [¿Qué hace la app?](#-qué-hace-la-app)
2. [Stack tecnológico](#-stack-tecnológico)
3. [Arquitectura del proyecto](#-arquitectura-del-proyecto)
4. [Cómo configurar el entorno](#-cómo-configurar-el-entorno)
5. [Configurar la API Key (OBLIGATORIO)](#-configurar-la-api-key-obligatorio)
6. [División de tareas del equipo](#-división-de-tareas-del-equipo)
7. [Flujo de trabajo en Git](#-flujo-de-trabajo-en-git)
8. [Conceptos matemáticos clave](#-conceptos-matemáticos-clave)
9. [Ligas disponibles](#-ligas-disponibles)
10. [Preguntas frecuentes](#-preguntas-frecuentes)

---

## 🎯 ¿Qué hace la app?

| Función | Descripción |
|---|---|
| **Cuotas en tiempo real** | Consume [The Odds API](https://the-odds-api.com) vía Retrofit |
| **Detector de Value Bets** | Compara la probabilidad de la IA vs la probabilidad implícita de la casa |
| **Criterio de Kelly** | Calcula qué % del bankroll apostar en cada partido |
| **Historial local** | Guarda apuestas en Room (SQLite) con estado WON / LOST / PENDING |
| **Gráfica de bankroll** | Visualiza la evolución del capital con MPAndroidChart |
| **Auto-refresco** | Actualiza las cuotas automáticamente cada 60 segundos |
| **Modo demo** | Si no hay API Key o internet, muestra 3 partidos ficticios de ejemplo |

---

## 🛠 Stack tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje | **Java 11** |
| SDK mínimo | Android **API 30** (Android 11) |
| SDK objetivo | Android **API 36** |
| UI | XML Layouts + **ViewBinding** + **Material Components** |
| Arquitectura | **MVVM** (ViewModel + LiveData) |
| Base de datos | **Room** (SQLite) |
| Red | **Retrofit 2** + Gson |
| Gráficas | **MPAndroidChart** |
| Build system | **Gradle Kotlin DSL** (`.gradle.kts`) |

---

## 🏗 Arquitectura del proyecto

El código sigue **Clean Architecture** con 3 capas separadas:

```
app/src/main/java/com/udg/betmasterai/
│
├── data/                        ← Capa de Datos
│   ├── local/                   ← Base de datos (Room)
│   │   ├── AppDatabase.java     # Singleton de la BD
│   │   ├── BetDao.java          # Queries SQL de apuestas
│   │   ├── BetHistory.java      # Entidad: historial de apuestas
│   │   └── UserBalance.java     # Entidad: saldo del usuario
│   │
│   ├── model/                   ← Modelos de datos
│   │   ├── MatchData.java       # Modelo interno de un partido
│   │   └── OddsApiMatch.java    # Modelo de respuesta de la API
│   │
│   ├── remote/                  ← Comunicación con la API
│   │   ├── ApiConstants.java    # ⚠️  API KEY y configuración aquí
│   │   ├── RetrofitClient.java  # Singleton de Retrofit
│   │   └── SportsApi.java       # Interfaz de endpoints
│   │
│   └── repository/              ← Orquestador de datos
│       └── MatchRepository.java # API → modelo interno (con fallback mock)
│
├── domain/                      ← Capa de Dominio (lógica de negocio)
│   └── BetEngine.java           # 🧠 Motor de IA: EV, Kelly, probabilidades
│
└── ui/                          ← Capa de Presentación (MVVM)
    ├── MainActivity.java        # Activity principal
    ├── MatchesAdapter.java      # RecyclerView de partidos
    └── MatchesViewModel.java    # ViewModel: estado y auto-refresh
```

### Flujo de datos (de la API a la pantalla)

```
The Odds API
     │
     ▼ Retrofit (hilo background)
MatchRepository
     │ mapApiResponseToMatchData()
     │ BetEngine.estimateAIProbabilityHome()
     ▼
  List<MatchData>
     │
     ▼ LiveData (hilo principal)
MatchesViewModel
     │ matchesLiveData.postValue(...)
     ▼
 MainActivity ──observe──▶ MatchesAdapter ──bind──▶ Pantalla
```

---

## ⚙️ Cómo configurar el entorno

### Requisitos previos

- **Android Studio** Ladybug (2024.2) o superior — [Descargar](https://developer.android.com/studio)
- **JDK 11** (viene incluido en Android Studio)
- **Git** instalado y configurado con tu usuario de GitHub

### Pasos para abrir el proyecto

```bash
# 1. Clonar el repositorio
git clone <URL-del-repositorio>

# 2. Abrir Android Studio
# File → Open → seleccionar la carpeta "proyecto/"

# 3. Esperar a que Gradle sincronice (puede tardar 2-5 minutos la primera vez)
# Verás "Gradle sync finished" en la barra inferior cuando termine

# 4. Conectar un dispositivo Android (API 30+) o crear un emulador en
#    Tools → Device Manager → Create Device
```

> **Nota:** Si Gradle falla, ir a `File → Invalidate Caches → Invalidate and Restart`.

---

## 🔑 Configurar la API Key (OBLIGATORIO)

Sin API Key la app funciona en **modo demo** con datos ficticios.
Para usar cuotas reales:

1. Ir a **[https://the-odds-api.com](https://the-odds-api.com)**
2. Crear una cuenta gratuita (incluye **500 requests/mes gratis**)
3. Copiar tu API Key del dashboard
4. Abrir el archivo:
   ```
   app/src/main/java/com/udg/betmasterai/data/remote/ApiConstants.java
   ```
5. Reemplazar la línea:
   ```java
   // ANTES:
   public static final String API_KEY = "TU_API_KEY_AQUI";

   // DESPUÉS:
   public static final String API_KEY = "abc123tuclavereal";
   ```

> ⚠️ **MUY IMPORTANTE:** No subas la API Key a Git. Agrégala al `.gitignore`
> o usa una variable de entorno para protegerla.

---

## 👥 División de tareas del equipo

El proyecto tiene **4 funcionalidades pendientes**, una por persona:

---

### 👤 Persona A — Pantalla para registrar apuestas

**Objetivo:** Crear un formulario que permita al usuario guardar una apuesta en la base de datos local.

**Archivos a crear:**
- `app/src/main/java/com/udg/betmasterai/ui/BetActivity.java` ← Activity nueva
- `app/src/main/res/layout/activity_bet.xml` ← Layout del formulario

**Archivos a modificar:**
- `app/src/main/AndroidManifest.xml` — Declarar la nueva Activity
- `app/src/main/java/com/udg/betmasterai/ui/MatchesAdapter.java` — Agregar botón "Apostar" en cada card que abra `BetActivity`
- `app/src/main/java/com/udg/betmasterai/data/local/BetDao.java` — Agregar método `insertBet(BetHistory bet)`

**Lo que debe hacer el formulario:**
1. Recibir los datos del partido seleccionado (nombre del partido, cuota, EV, Kelly sugerido)
2. Mostrar el importe sugerido por Kelly (pre-llenado, editable)
3. Tener un botón **"Guardar apuesta"** que inserte un `BetHistory` con `result = "PENDING"` en Room
4. Tener un botón **"Cancelar"**

**Referencia: estructura de `BetHistory`**
```java
// Campos disponibles en la entidad Room:
private String matchDetails;      // "Real Madrid vs Man City"
private double expectedValue;     // EV calculado por BetEngine
private double suggestedBet;      // % de kelly * bankroll
private double actualBetAmount;   // lo que el usuario decide apostar
private String result;            // "PENDING" al guardar, "WON" o "LOST" después
private long timestamp;           // System.currentTimeMillis()
```

---

### 👤 Persona B — Pantalla de historial de apuestas

**Objetivo:** Crear una pantalla que liste todas las apuestas pasadas y permita marcarlas como WON o LOST.

**Archivos a crear:**
- `app/src/main/java/com/udg/betmasterai/ui/HistoryActivity.java`
- `app/src/main/res/layout/activity_history.xml`
- `app/src/main/res/layout/item_bet_history.xml` ← Card del historial

**Archivos a modificar:**
- `app/src/main/AndroidManifest.xml` — Declarar la Activity
- `app/src/main/java/com/udg/betmasterai/ui/MainActivity.java` — Agregar botón de navegación al historial
- `app/src/main/java/com/udg/betmasterai/data/local/BetDao.java` — Agregar método `updateBetResult(int id, String result)`

**Lo que debe mostrar cada card:**
- Nombre del partido
- Monto apostado
- EV en el momento de la apuesta
- Estado: `PENDING` (amarillo) / `WON` (verde) / `LOST` (rojo)
- Fecha y hora

**Para actualizar un resultado:**
```java
// Ejemplo de cómo llamar al DAO desde un hilo background
new Thread(() -> {
    database.betDao().updateBetResult(bet.getId(), "WON");
}).start();
```

---

### 👤 Persona C — Selector de liga en tiempo real

**Objetivo:** Agregar un menú desplegable en la pantalla principal para que el usuario cambie de liga sin reiniciar la app.

**Archivos a modificar:**
- `app/src/main/res/layout/activity_main.xml` — Agregar un `Spinner` o `AutoCompleteTextView` de Material Design
- `app/src/main/java/com/udg/betmasterai/ui/MatchesViewModel.java` — Agregar método `fetchMatchesBySport(String sport)`
- `app/src/main/java/com/udg/betmasterai/ui/MainActivity.java` — Conectar el Spinner con el ViewModel

**Ligas disponibles para el Spinner:**

```java
// Mapa sugerido de nombre legible → código de API
Map<String, String> leagues = new LinkedHashMap<>();
leagues.put("⚽ La Liga (España)",        "soccer_spain_la_liga");
leagues.put("⚽ Premier League (Inglaterra)", "soccer_epl");
leagues.put("⚽ Bundesliga (Alemania)",   "soccer_germany_bundesliga");
leagues.put("⚽ Serie A (Italia)",        "soccer_italy_serie_a");
leagues.put("⚽ Ligue 1 (Francia)",       "soccer_france_ligue_one");
leagues.put("⚽ Champions League",        "soccer_uefa_champs_league");
leagues.put("⚽ Liga MX (México)",        "soccer_mexico_ligamx");
```

**Cambio clave en `MatchesViewModel`:**
```java
// Agregar este método al ViewModel
public void fetchMatchesBySport(String sport) {
    isLoading.setValue(true);
    repository.fetchMatches(sport, /* mismo callback de antes */);
}
```

---

### 👤 Persona D — Mejoras de UI + Tests

**Objetivo:** Pulir la interfaz, mejorar la experiencia de usuario y escribir pruebas básicas.

#### D1 — Mejoras de UI

**Archivos a modificar:**
- `app/src/main/res/layout/item_match.xml` — Revisar que los datos se vean bien en pantallas pequeñas
- `app/src/main/res/values/strings.xml` — Mover strings hard-coded del código a recursos
- `app/src/main/res/values/colors.xml` — Centralizar la paleta de colores

**Mejoras sugeridas:**
- Agregar animación de entrada (fade-in) a las cards del RecyclerView
- Mostrar un `Snackbar` al refrescar datos en lugar de solo cambiar el texto de estado
- Agregar ícono de la app personalizado en `res/mipmap-*/`

#### D2 — Tests unitarios

**Archivo a completar:**
- `app/src/test/java/com/udg/betmasterai/BetEngineTest.java` ← **Crear este archivo**

**Tests mínimos requeridos:**
```java
// Casos de prueba obligatorios para BetEngine:

// 1. EV positivo cuando la IA estima más probabilidad que la casa
@Test
public void testPositiveEV() {
    double ev = BetEngine.calculateExpectedValue(0.60, 2.10);
    assertTrue("EV debería ser positivo", ev > 0);
}

// 2. EV negativo con la probabilidad implícita (caso matemático crítico)
@Test
public void testNegativeEVWithImpliedProbability() {
    double odds = 2.10;
    double impliedProb = 1.0 / odds;
    double ev = BetEngine.calculateExpectedValue(impliedProb, odds);
    assertTrue("EV con prob. implícita siempre debe ser <= 0", ev <= 0);
}

// 3. Kelly nunca debe ser negativo
@Test
public void testKellyNeverNegative() {
    double kelly = BetEngine.calculateKellyCriterion(0.30, 2.50);
    assertTrue("Kelly no puede ser negativo", kelly >= 0);
}

// 4. Sin cuota, no hay Kelly
@Test
public void testKellyWithInvalidOdds() {
    double kelly = BetEngine.calculateKellyCriterion(0.50, 0.0);
    assertEquals(0.0, kelly, 0.001);
}
```

**Para correr los tests:**
```
En Android Studio: Click derecho en la clase → Run 'BetEngineTest'
```

---

## 🌿 Flujo de trabajo en Git

Para trabajar en equipo sin pisarse el código:

```bash
# Cada persona trabaja en su propia rama
git checkout -b feature/pantalla-apuestas       # Persona A
git checkout -b feature/historial-apuestas      # Persona B
git checkout -b feature/selector-liga           # Persona C
git checkout -b feature/ui-tests                # Persona D

# Commits frecuentes con mensajes descriptivos
git add .
git commit -m "feat: agregar formulario de apuesta con validación de monto"

# Subir la rama al repositorio remoto
git push origin feature/pantalla-apuestas

# Cuando esté listo, hacer Pull Request a main y pedir revisión
```

### Reglas de commits

| Prefijo | Cuándo usarlo |
|---|---|
| `feat:` | Nueva funcionalidad |
| `fix:` | Corrección de bug |
| `ui:` | Cambio solo visual |
| `test:` | Agregar o modificar tests |
| `docs:` | Cambios en documentación |
| `refactor:` | Reorganizar código sin cambiar funcionalidad |

---

## 🧮 Conceptos matemáticos clave

Para entender qué hace `BetEngine.java`:

### Probabilidad Implícita de la Casa
```
P_implícita = 1 / cuota
```
Ej: cuota 2.50 → P = 40%. Pero la suma de probabilidades de los 3 resultados
suele ser ~105% (el 5% extra es el margen de la casa).

### Probabilidad Estimada por la IA
La IA **no usa la cuota directamente**. En su lugar:
1. Calcula probabilidades implícitas brutas: `1/cuota_local`, `1/cuota_visitante`, `1/cuota_empate`
2. Las normaliza para eliminar el margen (suma = 100%)
3. Aplica un factor de ventaja local del **18%** al equipo anfitrión
4. Renormaliza de nuevo

Esto hace que la probabilidad de la IA sea **diferente** a la de la casa → puede existir un EV positivo.

### Valor Esperado (EV)
```
EV = (P_IA × (cuota - 1)) - ((1 - P_IA) × 1)
```
- `EV > 0` → **Value Bet**: la IA cree que hay ganancia esperada
- `EV < 0` → La casa tiene ventaja, se sugiere no apostar

### Criterio de Kelly (Half Kelly)
```
f* = (b × p - q) / b       donde b = cuota - 1, q = 1 - p
Half Kelly = f* / 2        (más conservador)
```
El resultado es el **porcentaje del bankroll** que se debería apostar.

---

## ⚽ Ligas disponibles

Modifica `DEFAULT_SPORT` en `ApiConstants.java` o implementa el selector dinámico (Persona C):

| Código para la API | Liga |
|---|---|
| `soccer_spain_la_liga` | 🇪🇸 La Liga (España) — **default** |
| `soccer_epl` | 🏴󠁧󠁢󠁥󠁮󠁧󠁿 Premier League (Inglaterra) |
| `soccer_germany_bundesliga` | 🇩🇪 Bundesliga (Alemania) |
| `soccer_italy_serie_a` | 🇮🇹 Serie A (Italia) |
| `soccer_france_ligue_one` | 🇫🇷 Ligue 1 (Francia) |
| `soccer_uefa_champs_league` | 🏆 UEFA Champions League |
| `soccer_mexico_ligamx` | 🇲🇽 Liga MX (México) |

---

## ❓ Preguntas frecuentes

**P: La app muestra "datos demo" y 3 partidos ficticios. ¿Por qué?**
> R: No tienes API Key configurada. Ver sección [Configurar la API Key](#-configurar-la-api-key-obligatorio).

**P: Gradle tarda mucho en sincronizar.**
> R: Es normal la primera vez porque descarga todas las dependencias. Con conexión rápida tarda ~3 min.

**P: ¿Cómo corro la app en mi teléfono?**
> R: Activar "Opciones de desarrollador" y "Depuración USB" en tu Android. Conectar el cable. Android Studio lo detecta automáticamente.

**P: ¿Por qué el EV siempre sería 0 si usamos 1/cuota como probabilidad?**
> R: Matemáticamente: `EV = (1/c) × (c-1) - (1 - 1/c) × 1 = 1 - 1/c - 1 + 1/c = 0`. Por eso la IA tiene su propio modelo de probabilidad independiente.

**P: ¿Cómo veo los logs de la app?**
> R: En Android Studio → `View → Tool Windows → Logcat`. Filtrar por tag `BetMasterAI`.

---

## 📋 Estado actual del proyecto

| Componente | Estado |
|---|---|
| Motor de IA (EV, Kelly, probabilidades) | ✅ Completo |
| Conexión a The Odds API | ✅ Completo (falta configurar API Key) |
| Base de datos Room | ✅ Completo |
| Pantalla principal con lista de partidos | ✅ Completo |
| Gráfica de bankroll | ✅ Completo |
| Auto-refresco cada 60 seg | ✅ Completo |
| **Formulario para registrar apuestas** | ⏳ Pendiente (Persona A) |
| **Pantalla de historial de apuestas** | ⏳ Pendiente (Persona B) |
| **Selector de liga dinámico** | ⏳ Pendiente (Persona C) |
| **Tests unitarios + pulido de UI** | ⏳ Pendiente (Persona D) |
