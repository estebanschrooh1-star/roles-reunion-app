# Asignaciones v2.1

Un solo código para dos apps: **Android** (APK) e **iPhone/iPad** (PWA). La pantalla es la misma
(`app/src/main/assets/index.html`); GitHub Actions genera las dos versiones en cada push.

## Android
Cada push compila el APK (pestaña **Actions** → artefacto **Asignaciones-APK**). Para publicarlo como **Release**
(sección *Releases*, solo con el APK) se usa el botón *Run workflow* (ver más abajo).
> Mantené en `app/` tu archivo `asignaciones.keystore` (no viene en este zip). Si lo cambiás, Android
> no te dejará actualizar encima de la app instalada.

## iPhone / iPad (PWA)
1. Una sola vez: en GitHub → **Settings → Pages → Build and deployment → Source: GitHub Actions**.
2. Hacé un push (o **Actions → Compilar APK y PWA → Run workflow**).
3. La dirección queda en `https://TU-USUARIO.github.io/TU-REPOSITORIO/`.
4. En el iPhone, abrila en **Safari** → Compartir → **Agregar a inicio**, y usá siempre ese ícono.

Si no querés usar Pages, el artefacto **Asignaciones-iOS-PWA** trae la carpeta lista para alojar en cualquier hosting con https.

## Diferencias en iPhone
- Sin pestaña **Avisos** (recordatorios y aviso de cambiar planilla): iOS no permite notificaciones programadas en una PWA.
- Sin copia automática en Google Drive ni recibir PDF compartido desde otra app. Sí funcionan Exportar/Importar,
  la planilla PDF (se sube desde Configuración), el historial, el bloqueo con patrón y el modo oscuro.
- Los respaldos `.txt` son compatibles entre Android e iPhone.

## Archivos nuevos
- `pwa/` manifiesto, service worker (uso sin conexión) e íconos.
- `scripts/build-pwa.sh` arma la versión web; `scripts/get-pdfjs.sh` descarga el visor de PDF (pdf.js 3.11.174).

## Actualizaciones automáticas (solo Android)
- La app busca versiones nuevas en silencio cada vez que se abre, o con **Configuración → Actualizaciones → Buscar actualizaciones**.
- Si hay una, aparece un aviso sobre los días del menú principal: **Descargar** (barra de progreso e instalación) o **Quizás luego**
  (queda pendiente en Configuración).
- **Cómo publicar una actualización (a mano):** los push solo compilan el APK. Cuando quieras que llegue a los teléfonos:
  GitHub → **Actions** → *Compilar APK y PWA* → **Run workflow** → marcá **Publicar una actualización** → **Run workflow**.
  Se crea el Release `v<VERSION>.<n>` con `Asignaciones-v<VERSION>.<n>.apk`; el último número es la cantidad de commits y sube solo,
  así que no hay que tocar `versionName` ni `versionCode`. Para saltar de versión (2.1 → 2.2) editá el archivo `VERSION`.
- **El repositorio debe ser público**: la app lee los Releases sin iniciar sesión. El repositorio se toma solo del que corre el Action.
- La primera vez que descargue, Android pedirá permitir **instalar apps desconocidas** para Asignaciones.
- Mantené el mismo `app/asignaciones.keystore` siempre; si cambia, Android rechaza la actualización.
- Las versiones anteriores a la 2.0 no traen el actualizador: la primera con actualizador hay que instalarla a mano una vez.

## Cambios de esta revisión
- **iPhone (PWA):** se corrige el espacio vacío de abajo (el alto real de pantalla se mide y se fuerza en `fitViewport()`), y la barra de estado pasa a `default` (como la otra app que funciona) en lugar de `black-translucent`, que era la causa probable del velo de arriba y de la franja de abajo.
- **WhatsApp en iOS:** usa `whatsapp://send?text=…` en lugar de `wa.me`, que abría el navegador interno de la PWA y dejaba una pantalla en blanco al volver. Si no funciona, poné `HIDE_WA_IOS = true` en `index.html` y el botón se oculta solo en iOS (queda *Compartir*).
- **Pantallas anchas (≥ 900×520 px):** barra lateral, contenido centrado y puestos en columnas. En el celular no cambia nada (aunque se gire, no llega a ese tamaño). En escritorio el PDF siempre se abre a pantalla completa. Bloque CSS `v7` al final de la hoja de estilos.
- **Diagnóstico oculto:** tocá 5 veces seguidas el texto «Versión» (Configuración) para ver medidas de pantalla.
