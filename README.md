# AFD — Validador de correo institucional UNI

Autómata Finito Determinista que reconoce correos institucionales con la estructura
`<iniciales><número>@uni.edu.ni` (2–4 letras minúsculas, 2–3 dígitos y el dominio exacto).

| Ruta | Contenido |
|---|---|
| [`afd-uni/`](afd-uni/) | Aplicación de escritorio en Java 17 + Swing: código del AFD, interfaz, pruebas JUnit 5, diagrama y documentación |
| [`informe/`](informe/) | **Informe técnico en Word** (`Informe-AFD-Validador-Correo-UNI.docx`): portada, formalización, tablas, trazas, conclusiones y anexos |
| [`PROMPT_AFD_Validador_Correo_UNI.md`](PROMPT_AFD_Validador_Correo_UNI.md) | Prompt/especificación profesional usada para construir la aplicación |

## Inicio rápido

```bash
cd afd-uni
mvn -q clean package
java -jar target/afd-uni.jar      # o bien: ./scripts/ejecutar.sh
```

Para generar una **aplicación nativa** con su propio Java (sin instalar nada en el equipo destino):
`./scripts/empaquetar-nativo.sh`

- Documentación técnica, tupla `M = (Q, Σ, δ, q0, F)`, diagramas e instrucciones: [`afd-uni/README.md`](afd-uni/README.md)
- Tabla de transiciones completa: [`afd-uni/docs/tabla-transiciones.md`](afd-uni/docs/tabla-transiciones.md)
- Informe técnico del proyecto: [`informe/Informe-AFD-Validador-Correo-UNI.docx`](informe/Informe-AFD-Validador-Correo-UNI.docx)
