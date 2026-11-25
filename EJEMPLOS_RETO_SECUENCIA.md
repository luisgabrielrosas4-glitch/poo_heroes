# Reto de secuencias

El jugador ve varios números y tiene que escribir el que sigue. No se usan números aleatorios, así que al generar de nuevo un nivel sale el mismo ejercicio.

| Nivel | Secuencia que aparece | Regla | Respuesta |
|---|---|---|---|
| FACIL | 2, 4, 6, 8, ? | Sumar 2 | 10 |
| MEDIO | 3, 6, 12, 24, ? | Multiplicar por 2 | 48 |
| DIFICIL | 2, 3, 5, 8, ? | Sumar los dos anteriores | 13 |

En ambos lenguajes el ejercicio se crea al construir `RetoSecuencia`. También se puede generar otra vez con `generarProblema()` o `generar_problema()`. `mostrarSecuencia()` y `mostrar_secuencia()` devuelven el texto con el signo de pregunta. La respuesta se compara como número entero positivo.

Cuando Joker crea una misión con `plantearMision()` (en Python, `plantear_mision()`), aumenta la dificultad del `RetoSecuencia`. El propio reto genera automáticamente los números y la respuesta del nuevo nivel, por lo que **no hace falta llamar a generar el problema otra vez**.

- Si el reto empezó en FACIL, Joker lo deja en MEDIO: `3, 6, 12, 24, ?` y respuesta `48`; la respuesta `10` ya no sirve.
- Si el reto empezó en MEDIO, Joker lo deja en DIFICIL: `2, 3, 5, 8, ?` y respuesta `13`; la respuesta `48` ya no sirve.

Los nuevos casos de prueba también comprueban que las misiones puedan recibir la respuesta correcta y que sigan contando los intentos fallidos.

## Probar en Python

Desde la carpeta principal del repositorio:

```bash
python -m unittest discover -s python -p "test_reto_secuencia.py" -v
python -m unittest discover -s python -p "test_mision_intentos.py" -v
```

## Probar en Java

Desde la carpeta principal del repositorio:

```bash
cd java
javac *.java
java TestRetoSecuencia
java TestMisionIntentos
```

No se cambiaron las firmas de `Reto` ni las reglas de `Mision`.
