# App móvil (Android e iOS)

La app nativa es el mismo frontend de Vue empaquetado con [Capacitor](https://capacitorjs.com): no hay
una segunda base de código. Las carpetas `frontend/android/` y `frontend/ios/` son los proyectos nativos
que genera Capacitor; no hace falta tocarlos a mano salvo para los permisos, el icono o la firma.

Lo que añade la app frente a la web:

- Icono, nombre y pantalla de bienvenida propios (el logo sobre el turquesa del proyecto).
- Botón «atrás» de Android: cierra el visor de fotos, los diálogos y los menús antes de volver atrás, y
  desde la portada sale de la app.
- Compartir con la hoja nativa del sistema, vibración al marcar me gusta, favorito o visita, y GPS
  nativo para «Cerca de mí».
- Barra de estado y zonas seguras (muesca, barra de gestos) bien resueltas.
- Funciona igual que la web: deslizar hacia abajo para recargar, pasar fotos con el dedo, chat en
  tiempo real, notificaciones…

## Cómo se conecta con el servidor

En la web, el frontend y la API comparten dirección. En la app, el frontend va dentro del paquete y la
API está en el servidor, cuya dirección se fija al compilar con `VITE_SERVIDOR` (por defecto, la de
`frontend/.env.movil`). Todo pasa por `frontend/src/plataforma.js`.

El backend ya admite las peticiones de la app (CORS para `https://localhost` y `capacitor://localhost`)
y la sesión va en la cabecera `Authorization`, no en cookies.

## Probarla en Android

Hace falta [Android Studio](https://developer.android.com/studio) (trae el SDK y un emulador) y Java 21.

```bash
# 1. El backend en marcha en el ordenador
cd backend && uvicorn app.main:app --port 8000

# 2. Compilar el frontend para la app y copiarlo al proyecto Android
cd frontend
npm run movil:android

# 3. Con el emulador abierto o un móvil conectado por USB (con la depuración USB activada):
adb reverse tcp:8000 tcp:8000      # el localhost:8000 del móvil pasa a ser el del ordenador
npx cap run android                # o: npx cap open android, y ▶ en Android Studio
```

Sin Android Studio abierto, el APK se genera con `cd android && ./gradlew assembleDebug`
(queda en `android/app/build/outputs/apk/debug/app-debug.apk`).

La app solo permite HTTP sin cifrar hacia `localhost` (para estas pruebas). Hacia cualquier otro
servidor exige HTTPS (`android/app/src/main/res/xml/network_security_config.xml`).

### Depurar

Las compilaciones de depuración dejan inspeccionar la app desde Chrome o Edge: abre
`chrome://inspect` (o `edge://inspect`) con el emulador en marcha y pulsa «inspect» en «Ocio Sin Gluten».

## APK desde GitHub

Cada push compila el APK en GitHub Actions (trabajo `android`) y lo deja en la pestaña **Actions** del
repositorio, como artefacto `ocio-sin-gluten-android`. Para que esa app se conecte al servidor publicado,
crea la variable del repositorio `SERVIDOR_APP` con su dirección (por ejemplo `https://ociosingluten.es`)
en *Settings → Secrets and variables → Actions → Variables*.

## Publicar en Google Play

1. Compila contra el servidor real: `VITE_SERVIDOR=https://tu-dominio.es npm run movil:android`.
2. Crea una clave de firma una sola vez y guárdala fuera del repositorio:
   `keytool -genkey -v -keystore ociosingluten.jks -alias ociosingluten -keyalg RSA -keysize 2048 -validity 10000`
3. En Android Studio: *Build → Generate Signed App Bundle*, con esa clave. Sube el `.aab` a Google Play
   Console.
4. Antes de cada versión nueva, sube `versionCode` y `versionName` en `android/app/build.gradle`.

## iOS

El proyecto está en `frontend/ios/`. Compilarlo exige un Mac con Xcode:

```bash
npm run movil:ios
npx cap open ios        # y ▶ en Xcode; para la App Store, Product → Archive
```

## Icono y pantalla de bienvenida

Salen de las imágenes de `frontend/resources/` (generadas a partir del logo). Si cambian:

```bash
npx capacitor-assets generate --assetPath resources --android --ios \
  --iconBackgroundColor "#9DD9D2" --splashBackgroundColor "#9DD9D2" \
  --iconBackgroundColorDark "#353535" --splashBackgroundColorDark "#353535"
```
