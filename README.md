# Jerry (mod Fabric 1.20.1)

## Compilar
1. Instala JDK 17 y Gradle 8.6+ (o copia esta carpeta `src/` + `build.gradle` sobre la plantilla oficial de Fabric).
2. `gradle wrapper` y luego `./gradlew build`  ->  `build/libs/jerrymod-1.0.0.jar`
3. Mete el .jar en `mods/` junto con Fabric API 0.92.2+1.20.1.

## IA
Arranca el juego una vez y edita `config/jerrymod.json`:
{ "apiKey": "TU_API_KEY_DE_ANTHROPIC", "model": "claude-haiku-5-5" }
Sin API key funcionan los trucos ("dame la pata", "da la vuelta", "siéntate"), pero no pedir objetos.

## Uso
- Escribe en el chat "Jerry dame 64 de madera de abedul", o pulsa J / el botón "Jerry" del chat.
- Acariciar: agáchate + click derecho con la mano vacía. Hueso: +felicidad. Click derecho normal: sentar.
- Ojo de araña (click derecho en Jerry): cambia los 4 tipos de ojos.
- Felicidad baja <= 5: se transforma en el monstruo de 4 m. Prueba: /summon jerrymod:jerry_monster
- Texturas: regenera con `python3 make_textures.py` o reemplaza los PNG en assets/jerrymod/textures/entity/
