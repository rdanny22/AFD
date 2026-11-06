# Tabla de transiciones δ del AFD

`M = (Q, Σ, δ, q0, F)` con `Q = {q0…q19}`, `Σ = {a–z, 0–9, @, .}` (38 símbolos), `q0 = q0` y `F = {q18}`.
La función δ es **total**: cada par `(estado, símbolo)` tiene exactamente un destino, y las
transiciones no listadas a continuación conducen a `q19`.

## 1. Parte local — iniciales y número (por clase de símbolo)

`L` = cualquier letra `a–z` (26 símbolos) · `D` = cualquier dígito `0–9` (10 símbolos).

| Estado | `L` | `D` | `@` | `.` | otro |
|---|---|---|---|---|---|
| `q0` | `q1` | `q19` | `q19` | `q19` | `q19` |
| `q1` | `q2` | `q19` | `q19` | `q19` | `q19` |
| `q2` | `q3` | `q5` | `q19` | `q19` | `q19` |
| `q3` | `q4` | `q5` | `q19` | `q19` | `q19` |
| `q4` | `q19` | `q5` | `q19` | `q19` | `q19` |
| `q5` | `q19` | `q6` | `q19` | `q19` | `q19` |
| `q6` | `q19` | `q7` | `q8` | `q19` | `q19` |
| `q7` | `q19` | `q19` | `q8` | `q19` | `q19` |

## 2. Parte del dominio — `@uni.edu.ni` (por carácter exacto)

En cada estado, cualquier símbolo distinto del esperado conduce a `q19`.

| Estado | Símbolo leído hasta ahora | Carácter esperado | Destino |
|---|---|---|---|
| `q8` | `@` | `u` | `q9` |
| `q9` | `@u` | `n` | `q10` |
| `q10` | `@un` | `i` | `q11` |
| `q11` | `@uni` | `.` | `q12` |
| `q12` | `@uni.` | `e` | `q13` |
| `q13` | `@uni.e` | `d` | `q14` |
| `q14` | `@uni.ed` | `u` | `q15` |
| `q15` | `@uni.edu` | `.` | `q16` |
| `q16` | `@uni.edu.` | `n` | `q17` |
| `q17` | `@uni.edu.n` | `i` | `q18` |

## 3. Estados absorbentes

| Estado | Todos los símbolos de Σ | Observación |
|---|---|---|
| `q18` | `q19` | No se admiten símbolos después de `@uni.edu.ni`; la aceptación es estricta al final |
| `q19` | `q19` | Sumidero: una vez alcanzado, el rechazo es definitivo |

## 4. Conteo de transiciones

| Origen | Definidas explícitamente | Hacia `q19` | Total |
|---|---|---|---|
| `q0`, `q1` | 26 | 12 | 38 |
| `q2`, `q3` | 36 | 2 | 38 |
| `q4` | 10 | 28 | 38 |
| `q5` | 10 | 28 | 38 |
| `q6` | 11 | 27 | 38 |
| `q7` | 1 | 37 | 38 |
| `q8`–`q17` | 1 cada uno | 37 cada uno | 38 cada uno |
| `q18` | 0 | 38 | 38 |
| `q19` | 38 (auto-lazo) | 0 | 38 |

Total: **20 × 38 = 760 transiciones**.

## 5. Equivalencia con una expresión regular (solo para pruebas)

`L(M) = { w ∈ Σ* | w ∈ [a-z]{2,4} · [0-9]{2,3} · "@uni.edu.ni" }`

La expresión `^[a-z]{2,4}[0-9]{2,3}@uni\.edu\.ni$` se usa **únicamente** en `AutomataTest` como
verificación cruzada sobre cadenas aleatorias; el validador de la aplicación nunca la utiliza.
