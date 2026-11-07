# -*- coding: utf-8 -*-
"""Genera el informe técnico del validador AFD de correo institucional UNI en formato .docx.

Requisitos: python-docx. Salida: informe/Informe-AFD-Validador-Correo-UNI.docx
Formato: Arial 12, interlineado 1.5, márgenes de 2.5 cm, tablas e imágenes numeradas.
"""

import os

from docx import Document
from docx.enum.section import WD_ORIENT, WD_SECTION
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Cm, Pt, RGBColor

AZUL = RGBColor(0x1F, 0x38, 0x64)
GRIS = RGBColor(0x59, 0x59, 0x59)
OSCURO = RGBColor(0x20, 0x20, 0x20)
VERDE = RGBColor(0x1B, 0x8A, 0x3A)
ROJO = RGBColor(0xC6, 0x28, 0x28)
CENTRO = WD_ALIGN_PARAGRAPH.CENTER
IZQUIERDA = WD_ALIGN_PARAGRAPH.LEFT
JUSTIFICADO = WD_ALIGN_PARAGRAPH.JUSTIFY

BASE = os.path.dirname(os.path.abspath(__file__))
DOCS = os.path.join(BASE, os.pardir, "afd-uni", "docs") + os.sep
SALIDA = os.path.join(BASE, "Informe-AFD-Validador-Correo-UNI.docx")

doc = Document()


def fuente(run, nombre="Arial", tam=12, negrita=False, cursiva=False, color=None):
    run.font.name = nombre
    run.font.size = Pt(tam)
    run.bold = negrita
    run.italic = cursiva
    if color is not None:
        run.font.color.rgb = color
    rPr = run._element.get_or_add_rPr()
    rFonts = rPr.find(qn("w:rFonts"))
    if rFonts is None:
        rFonts = OxmlElement("w:rFonts")
        rPr.insert(0, rFonts)
    for atributo in ("w:ascii", "w:hAnsi", "w:cs", "w:eastAsia"):
        rFonts.set(qn(atributo), nombre)


def parrafo(texto="", tam=12, alineacion=JUSTIFICADO, negrita=False, cursiva=False, color=None,
            espacio_tras=6, espaciado=1.5, izquierda=None, primera_linea=None, conservar=False):
    par = doc.add_paragraph()
    par.paragraph_format.alignment = alineacion
    par.paragraph_format.line_spacing = espaciado
    par.paragraph_format.space_after = Pt(espacio_tras)
    par.paragraph_format.space_before = Pt(0)
    if izquierda is not None:
        par.paragraph_format.left_indent = Cm(izquierda)
    if primera_linea is not None:
        par.paragraph_format.first_line_indent = Cm(primera_linea)
    if conservar:
        par.paragraph_format.keep_with_next = True
    for indice, trozo in enumerate(texto.split("**")):
        if not trozo:
            continue
        run = par.add_run(trozo)
        fuente(run, "Arial", tam, negrita or (indice % 2 == 1), cursiva, color)
    return par


def h(nivel, texto):
    tam = {1: 14, 2: 12, 3: 12}[nivel]
    color = AZUL if nivel < 3 else OSCURO
    par = doc.add_paragraph(style="Heading %d" % nivel)
    run = par.add_run(texto)
    fuente(run, "Arial", tam, True, False, color)
    return par


def vineta(texto, nivel=0, tam=12):
    par = doc.add_paragraph(style="List Bullet" if nivel == 0 else "List Bullet 2")
    par.paragraph_format.line_spacing = 1.5
    par.paragraph_format.space_after = Pt(3)
    par.paragraph_format.alignment = JUSTIFICADO
    for indice, trozo in enumerate(texto.split("**")):
        if not trozo:
            continue
        run = par.add_run(trozo)
        fuente(run, "Arial", tam, indice % 2 == 1, False, None)
    return par


def numerada(texto, tam=12):
    par = doc.add_paragraph(style="List Number")
    par.paragraph_format.line_spacing = 1.5
    par.paragraph_format.space_after = Pt(3)
    par.paragraph_format.alignment = JUSTIFICADO
    for indice, trozo in enumerate(texto.split("**")):
        if not trozo:
            continue
        run = par.add_run(trozo)
        fuente(run, "Arial", tam, indice % 2 == 1, False, None)
    return par


def sombrear(celda, color_hex):
    tcPr = celda._tc.get_or_add_tcPr()
    shd = OxmlElement("w:shd")
    shd.set(qn("w:val"), "clear")
    shd.set(qn("w:color"), "auto")
    shd.set(qn("w:fill"), color_hex)
    tcPr.append(shd)


def leyenda(texto, tam=10):
    cap = doc.add_paragraph()
    cap.paragraph_format.alignment = IZQUIERDA
    cap.paragraph_format.space_before = Pt(6)
    cap.paragraph_format.space_after = Pt(3)
    cap.paragraph_format.line_spacing = 1.0
    cap.paragraph_format.keep_with_next = True
    partes = texto.split(". ", 1)
    r1 = cap.add_run(partes[0] + ". ")
    fuente(r1, "Arial", tam, True, False, AZUL)
    if len(partes) > 1:
        r2 = cap.add_run(partes[1])
        fuente(r2, "Arial", tam, False, False, AZUL)
    return cap


def tabla(cabeceras, filas, anchos=None, tam=9, titulo=None, primera_col_negrita=False,
          alineaciones=None, tam_cabecera=None, color_total=None):
    if titulo:
        leyenda(titulo)
    t = doc.add_table(rows=1, cols=len(cabeceras))
    t.style = "Table Grid"
    t.alignment = WD_TABLE_ALIGNMENT.CENTER
    t.autofit = False
    for i, texto in enumerate(cabeceras):
        celda = t.rows[0].cells[i]
        celda.text = ""
        p = celda.paragraphs[0]
        p.paragraph_format.alignment = CENTRO
        p.paragraph_format.line_spacing = 1.0
        p.paragraph_format.space_before = Pt(2)
        p.paragraph_format.space_after = Pt(2)
        r = p.add_run(str(texto))
        fuente(r, "Arial", tam_cabecera or tam, True, False, RGBColor(0xFF, 0xFF, 0xFF))
        sombrear(celda, "1F3864")
    for numero_fila, fila in enumerate(filas):
        celdas = t.add_row().cells
        for i, valor in enumerate(fila):
            celdas[i].text = ""
            p = celdas[i].paragraphs[0]
            p.paragraph_format.line_spacing = 1.0
            p.paragraph_format.space_before = Pt(1)
            p.paragraph_format.space_after = Pt(1)
            p.paragraph_format.alignment = alineaciones[i] if alineaciones else IZQUIERDA
            for j, trozo in enumerate(str(valor).split("**")):
                if not trozo:
                    continue
                r = p.add_run(trozo)
                fuente(r, "Arial", tam, (primera_col_negrita and i == 0) or (j % 2 == 1))
            if color_total and numero_fila == len(filas) - 1:
                sombrear(celdas[i], "DCE6F1")
            elif numero_fila % 2 == 1:
                sombrear(celdas[i], "F2F5FA")
    if anchos:
        for fila in t.rows:
            for i, ancho in enumerate(anchos):
                fila.cells[i].width = Cm(ancho)
    trPr = t.rows[0]._tr.get_or_add_trPr()
    encabezado = OxmlElement("w:tblHeader")
    encabezado.set(qn("w:val"), "true")
    trPr.append(encabezado)
    for fila in t.rows:
        propiedades = fila._tr.get_or_add_trPr()
        sin_division = OxmlElement("w:cantSplit")
        sin_division.set(qn("w:val"), "true")
        propiedades.append(sin_division)
    doc.add_paragraph().paragraph_format.space_after = Pt(4)
    return t


def figura(ruta, titulo, ancho_cm=15.4):
    p = doc.add_paragraph()
    p.paragraph_format.alignment = CENTRO
    p.paragraph_format.space_before = Pt(6)
    p.paragraph_format.space_after = Pt(2)
    p.paragraph_format.line_spacing = 1.0
    p.paragraph_format.keep_with_next = True
    p.add_run().add_picture(ruta, width=Cm(ancho_cm))
    cap = doc.add_paragraph()
    cap.paragraph_format.alignment = CENTRO
    cap.paragraph_format.space_after = Pt(10)
    cap.paragraph_format.line_spacing = 1.0
    partes = titulo.split(". ", 1)
    r1 = cap.add_run(partes[0] + ". ")
    fuente(r1, "Arial", 10, True, False, AZUL)
    r2 = cap.add_run(partes[1] if len(partes) > 1 else "")
    fuente(r2, "Arial", 10, False, True, GRIS)


def codigo(lineas, tam=8.5):
    t = doc.add_table(rows=1, cols=1)
    t.style = "Table Grid"
    t.alignment = WD_TABLE_ALIGNMENT.CENTER
    celda = t.rows[0].cells[0]
    celda.text = ""
    sombrear(celda, "F5F5F5")
    for i, linea in enumerate(lineas):
        p = celda.paragraphs[0] if i == 0 else celda.add_paragraph()
        p.paragraph_format.line_spacing = 1.0
        p.paragraph_format.space_before = Pt(0)
        p.paragraph_format.space_after = Pt(0)
        p.paragraph_format.alignment = IZQUIERDA
        r = p.add_run(linea if linea else " ")
        fuente(r, "Courier New", tam, False, False, OSCURO)
    doc.add_paragraph().paragraph_format.space_after = Pt(4)


def campo(par, instruccion, texto_visible="1", tam=9):
    run = par.add_run()
    fuente(run, "Arial", tam, False, False, GRIS)
    inicio = OxmlElement("w:fldChar")
    inicio.set(qn("w:fldCharType"), "begin")
    instr = OxmlElement("w:instrText")
    instr.set(qn("xml:space"), "preserve")
    instr.text = " %s " % instruccion
    separador = OxmlElement("w:fldChar")
    separador.set(qn("w:fldCharType"), "separate")
    texto = OxmlElement("w:t")
    texto.text = texto_visible
    fin = OxmlElement("w:fldChar")
    fin.set(qn("w:fldCharType"), "end")
    for elemento in (inicio, instr, separador, texto, fin):
        run._r.append(elemento)


def configurar_pagina(seccion, horizontal=False):
    if horizontal:
        seccion.orientation = WD_ORIENT.LANDSCAPE
        seccion.page_width = Cm(29.7)
        seccion.page_height = Cm(21)
        seccion.left_margin = Cm(1.6)
        seccion.right_margin = Cm(1.6)
    else:
        seccion.orientation = WD_ORIENT.PORTRAIT
        seccion.page_width = Cm(21)
        seccion.page_height = Cm(29.7)
        seccion.left_margin = Cm(2.5)
        seccion.right_margin = Cm(2.5)
    seccion.top_margin = Cm(2.5)
    seccion.bottom_margin = Cm(2.0)
    seccion.header_distance = Cm(1.2)
    seccion.footer_distance = Cm(1.0)

# --------------------------------------------------------------------------
# Estilos del documento: Arial 12, interlineado 1.5
# --------------------------------------------------------------------------

normal = doc.styles["Normal"]
normal.font.name = "Arial"
normal.font.size = Pt(12)
normal.paragraph_format.line_spacing = 1.5
normal.paragraph_format.space_after = Pt(6)
normal.paragraph_format.alignment = JUSTIFICADO
rPr_normal = normal.element.get_or_add_rPr()
rFonts_normal = rPr_normal.find(qn("w:rFonts"))
if rFonts_normal is None:
    rFonts_normal = OxmlElement("w:rFonts")
    rPr_normal.insert(0, rFonts_normal)
for atributo in ("w:ascii", "w:hAnsi", "w:cs", "w:eastAsia"):
    rFonts_normal.set(qn(atributo), "Arial")


def preparar_estilo(nombre_estilo, tam, color=AZUL, antes=14, despues=6):
    est = doc.styles[nombre_estilo]
    est.font.name = "Arial"
    est.font.size = Pt(tam)
    est.font.bold = True
    est.font.italic = False
    est.font.color.rgb = color
    rPr = est.element.get_or_add_rPr()
    rFonts = rPr.find(qn("w:rFonts"))
    if rFonts is None:
        rFonts = OxmlElement("w:rFonts")
        rPr.insert(0, rFonts)
    for atributo in ("w:ascii", "w:hAnsi", "w:cs", "w:eastAsia"):
        rFonts.set(qn(atributo), "Arial")
    pf = est.paragraph_format
    pf.space_before = Pt(antes)
    pf.space_after = Pt(despues)
    pf.line_spacing = 1.5
    pf.keep_with_next = True
    pf.alignment = IZQUIERDA


preparar_estilo("Heading 1", 14, AZUL, 18, 8)
preparar_estilo("Heading 2", 12, AZUL, 12, 6)
preparar_estilo("Heading 3", 12, OSCURO, 10, 4)

# --------------------------------------------------------------------------
# Sección principal y encabezado/pie
# --------------------------------------------------------------------------

seccion = doc.sections[0]
configurar_pagina(seccion)
seccion.different_first_page_header_footer = True

encabezado = seccion.header.paragraphs[0]
encabezado.paragraph_format.alignment = IZQUIERDA
run = encabezado.add_run("Matemáticas Discretas · Validador de correo institucional @uni.edu.ni")
fuente(run, "Arial", 9, False, False, GRIS)

pie = seccion.footer.paragraphs[0]
pie.paragraph_format.alignment = CENTRO
run = pie.add_run("Página ")
fuente(run, "Arial", 9, False, False, GRIS)
campo(pie, "PAGE", "2")
run = pie.add_run(" de ")
fuente(run, "Arial", 9, False, False, GRIS)
campo(pie, "NUMPAGES", "20")

# --------------------------------------------------------------------------
# PORTADA
# --------------------------------------------------------------------------

parrafo("", espacio_tras=0)
parrafo("", espacio_tras=0)
parrafo("", espacio_tras=0)
parrafo("UNIVERSIDAD NACIONAL DE INGENIERÍA", tam=18, negrita=True, alineacion=CENTRO, espacio_tras=2)
parrafo("Facultad de Electrotecnia y Computación", tam=12, alineacion=CENTRO, espacio_tras=2)
parrafo("Carrera de Ingeniería en Computación", tam=12, alineacion=CENTRO, espacio_tras=18)
parrafo("Asignatura: Matemáticas Discretas", tam=12, alineacion=CENTRO, espacio_tras=42)
parrafo("Diseño e Implementación de un Autómata Finito Determinista (AFD) para la "
        "Validación de Correo Institucional (@uni.edu.ni)", tam=16, negrita=True, alineacion=CENTRO,
        espacio_tras=8)
parrafo("Informe técnico del proyecto final", tam=12, cursiva=True, alineacion=CENTRO, espacio_tras=30)

parrafo("Integrantes del equipo", tam=12, negrita=True, alineacion=CENTRO, espacio_tras=6)
tabla(["Nombre completo", "Número de carné", "Grupo académico"],
      [["", "", ""], ["", "", ""], ["", "", ""]],
      anchos=[7.6, 4.2, 3.2], tam=11, alineaciones=[CENTRO, CENTRO, CENTRO])

parrafo("", espacio_tras=6)
parrafo("**Docente:** MSc. Lizette C. Duarte Mora", tam=12, alineacion=CENTRO, espacio_tras=2)
parrafo("**Fecha de entrega:** ____ de ________________ de 2026", tam=12, alineacion=CENTRO,
        espacio_tras=40)
parrafo("Managua, Nicaragua", tam=12, alineacion=CENTRO, espacio_tras=0)

doc.add_page_break()

# --------------------------------------------------------------------------
# ÍNDICE
# --------------------------------------------------------------------------

h(1, "Índice de contenido")
indice_par = doc.add_paragraph()
indice_par.paragraph_format.space_after = Pt(6)
campo(indice_par, 'TOC \\o "1-2" \\h \\z \\u',
      "Para generar el índice, haga clic derecho sobre este texto y elija «Actualizar campo» "
      "(o pulse F9 en Word).", tam=12)
parrafo("", espacio_tras=0)
parrafo("**Nota:** este documento sigue la «Guía de Trabajo Final de Matemáticas Discretas: "
        "Validador de Formato de Correo Institucional» de la UNI y conserva la numeración de sus "
        "diez secciones.", tam=10, cursiva=True, color=GRIS)

doc.add_page_break()

# --------------------------------------------------------------------------
# 2. INTRODUCCIÓN
# --------------------------------------------------------------------------

h(1, "2. Introducción")

h(2, "2.1 Contexto institucional")
parrafo("En la Universidad Nacional de Ingeniería (UNI) el correo institucional con el dominio "
        "**@uni.edu.ni** funciona como la identidad digital del estudiante y del docente. Esa misma "
        "dirección es la credencial de acceso a la plataforma virtual de aprendizaje, al catálogo de "
        "la biblioteca digital, a los recursos de la red interna y a los servicios en línea de la "
        "institución. Cuando el alta de una cuenta se hace a mano, es frecuente que se cometan "
        "errores de digitación: una letra mayúscula, un dígito de más, un dominio escrito como "
        "@uni.edu o @university.edu, un espacio invisible al final, o incluso una dirección de un "
        "proveedor externo como @gmail.com. Cada una de esas variantes produce una cuenta que no "
        "puede autenticarse, duplica identidades o deja la puerta abierta a suplantaciones.")
parrafo("La validación automática del formato, ejecutada antes de crear la cuenta, resuelve el "
        "problema en el punto más barato del proceso: el instante en que el usuario escribe. Un "
        "validador determinista devuelve siempre el mismo veredicto para la misma entrada, explica "
        "con precisión en qué posición se rompió la regla y permite que el sistema de registro, el "
        "portal web o la aplicación de escritorio rechacen la solicitud antes de tocar la base de "
        "datos. Este informe documenta el diseño formal, la implementación y la verificación de ese "
        "validador, construido como un Autómata Finito Determinista (AFD) y no como una lista de "
        "condiciones ad hoc.")

h(2, "2.2 Justificación teórica")
parrafo("La teoría de lenguajes formales estudia los conjuntos de cadenas que pueden reconocerse "
        "con modelos matemáticos precisos. Un **autómata finito determinista** es la quinta tupla "
        "M = (Q, Σ, δ, q0, F), donde Q es un conjunto finito de estados, Σ un alfabeto finito, "
        "δ: Q × Σ → Q una función de transición **total**, q0 el estado inicial y F ⊆ Q el conjunto "
        "de estados finales. El lenguaje aceptado, L(M), está formado por las cadenas que llevan al "
        "autómata desde q0 hasta algún estado de F. Todo lenguaje regular, y el formato de correo "
        "institucional lo es, puede reconocerse con uno de estos autómatas (Hopcroft et al., 2007).")
parrafo("Frente a una solución basada en expresiones regulares, el AFD ofrece tres ventajas que "
        "resultan decisivas en un sistema institucional. Primero, el **determinismo**: para cada par "
        "(estado, símbolo) existe exactamente una transición, de modo que no hay ambigüedad ni "
        "necesidad de retroceder. Segundo, el **costo**: el reconocimiento examina cada símbolo una "
        "sola vez, es decir, trabaja en tiempo O(n) con memoria constante, independientemente de la "
        "longitud de la entrada. Tercero, la **trazabilidad**: como el autómata recuerda en qué "
        "estado está, es posible explicar el rechazo indicando la posición exacta y la causa, algo "
        "que una expresión regular no comunica por sí misma. La expresión regular equivalente se "
        "usa en este proyecto únicamente como verificación cruzada en las pruebas.")
parrafo("El modelo se implementó además con una separación estricta entre la lógica del autómata "
        "(paquete **modelo**), la interfaz gráfica (paquete **vista**) y el controlador que los "
        "conecta (paquete **controlador**), siguiendo el patrón Modelo–Vista–Controlador descrito "
        "por Gamma et al. (1994). Esa separación permite probar el autómata sin abrir una ventana y "
        "dibujar el diagrama de estados en pantalla a partir de la misma matriz de transición que "
        "valida las cadenas.")

h(2, "2.3 Alcance del trabajo")
parrafo("El entregable es una aplicación de escritorio en **Java 17 con Swing** que valida cadenas "
        "del lenguaje institucional, muestra la traza completa de estados, dibuja el AFD en pantalla "
        "y ejecuta una batería de 22 casos de prueba. El alcance se limita a la **validación de "
        "formato**: el programa no comprueba si la cuenta existe en el directorio institucional ni "
        "consulta servicios externos. El código fuente, la documentación técnica y las pruebas "
        "automatizadas acompañan a este informe.")

# --------------------------------------------------------------------------
# 3. OBJETIVOS
# --------------------------------------------------------------------------

h(1, "3. Objetivos")

h(2, "3.1 Objetivo general")
parrafo("Diseñar, implementar y validar un Autómata Finito Determinista formal que reconozca y "
        "acepte exclusivamente correos electrónicos institucionales válidos con la estructura "
        "**<iniciales><número>@uni.edu.ni**, verificando su comportamiento con cadenas válidas e "
        "inválidas y demostrando que la función de transición es total y determinista.")

h(2, "3.2 Objetivos específicos")
numerada("Formalizar la tupla matemática del autómata **M = (Q, Σ, δ, q0, F)**, identificando el "
         "alfabeto de 38 símbolos y los 20 estados requeridos para contar iniciales, dígitos y "
         "reconocer el dominio institucional.")
numerada("Modelar la función de transición de estados mediante una **tabla formal** de 760 "
         "transiciones (20 × 38) y su **diagrama de transición de estados**, con el estado inicial "
         "señalado con flecha entrante y el estado de aceptación con doble círculo.")
numerada("Implementar el autómata en Java 17 como una matriz explícita, sin expresiones regulares "
         "en la lógica de validación, y construir una interfaz de escritorio que muestre el "
         "veredicto, la traza y el diagrama del autómata.")
numerada("Probar y evaluar el comportamiento del AFD procesando cadenas válidas e inválidas, con "
         "una batería de 22 casos y 47 pruebas automatizadas, para verificar la precisión del "
         "reconocimiento.")
numerada("Documentar las decisiones de diseño, las ambigüedades resueltas del enunciado y los "
         "resultados obtenidos, de modo que el trabajo pueda reproducirse y auditarse.")

# --------------------------------------------------------------------------
# 4. DESCRIPCIÓN Y REGLAS DEL PROBLEMA
# --------------------------------------------------------------------------

h(1, "4. Descripción y reglas del problema")

h(2, "4.1 Estructura del correo institucional")
parrafo("La dirección institucional se compone de tres bloques consecutivos: un conjunto de "
        "iniciales en minúsculas, un número y el dominio fijo de la universidad. La gramática "
        "siguiente resume la estructura exigida; las llaves y corchetes indican repetición y "
        "opcionalidad, respectivamente.")
codigo([
    "<correo>     ::= <iniciales> <numero> \"@uni.edu.ni\"",
    "<iniciales>  ::= [a-z] [a-z] [a-z]? [a-z]?         (2 a 4 letras)",
    "<numero>     ::= [0-9] [0-9] [0-9]?                (2 a 3 dígitos)",
])
parrafo("Ejemplos válidos: **js25@uni.edu.ni**, **lmra123@uni.edu.ni**, **ab12@uni.edu.ni** y "
        "**abcd999@uni.edu.ni** (los dos últimos representan los límites mínimos y máximos). "
        "Ejemplos inválidos: **Js25@unicomp.edu** (mayúscula no permitida), **j@unicomp.edu** "
        "(falta el número), **js25@unicom.edu** (dominio erróneo) y **abc123@gmail.com** (dominio "
        "incorrecto).")

h(2, "4.2 Reglas formales de sintaxis")
parrafo("Las reglas se aplican de forma conjunta y sin excepciones. En particular, la validación "
        "es **estricta**: la cadena no se recorta, no se convierte a minúsculas y no admite "
        "caracteres ocultos, porque un espacio o un salto de línea no pertenecen al alfabeto.")
tabla(
    ["Elemento", "Regla formal", "Ejemplos válidos", "Ejemplos inválidos"],
    [
        ["Iniciales", "Entre 2 y 4 letras minúsculas: [a-z]{2,4}", "js, ab, lmra, abcd", "j, abcde, Js, JS"],
        ["Número", "Entre 2 y 3 dígitos: [0-9]{2,3}", "25, 12, 999, 123", "2, 2500"],
        ["Separador", "Exactamente una arroba que inicia el dominio", "@", "@@, sin arroba"],
        ["Dominio", "Sufijo literal @uni.edu.ni, en minúsculas", "@uni.edu.ni",
         "@unicom.edu, @gmail.com, @UNI.EDU.NI"],
        ["Cierre", "Aceptación solo al consumir toda la cadena", "js25@uni.edu.ni",
         "js25@uni.edu.ni.mx, js25@uni.edu.ni#"],
        ["Alfabeto", "Solo los 38 símbolos de Σ: sin espacios, acentos ni mayúsculas", "—",
         "'J', ' ', '#', salto de línea"],
        ["Normalización", "Ninguna: el texto se valida tal como se escribe", "—",
         "\" js25@uni.edu.ni\", \"js25@uni.edu.ni \""],
    ],
    titulo="Tabla 1. Reglas formales de sintaxis del correo institucional",
    anchos=[2.6, 6.2, 3.4, 3.8], tam=9)

h(2, "4.3 Clasificación de los veredictos")
parrafo("El autómata no se limita a aceptar o rechazar: cuando rechaza, clasifica la causa según "
        "el estado en el que se encontraba y el símbolo que provocó el fallo. Esa clasificación se "
        "deriva del estado de origen de la transición y se utiliza para construir el mensaje que ve "
        "el usuario.")
tabla(
    ["Veredicto", "Cuándo se produce", "Ejemplo"],
    [
        ["Válido", "La cadena se consumió por completo y terminó en q18", "js25@uni.edu.ni"],
        ["Símbolo no permitido",
         "Aparece un carácter que no pertenece a Σ (mayúscula, espacio, '#', salto de línea, acento)",
         "Js25@unicomp.edu"],
        ["Iniciales inválidas",
         "El símbolo es válido, pero la parte de iniciales no llegó a 2 letras o superó las 4",
         "j@unicomp.edu, a12@uni.edu.ni"],
        ["Dígitos inválidos",
         "La parte numérica no llegó a 2 dígitos o superó los 3", "js2@uni.edu.ni, js2500@uni.edu.ni"],
        ["Dominio inválido",
         "El dominio no coincide con @uni.edu.ni o quedó incompleto",
         "js25@unicom.edu, js25@uni.edu"],
        ["Caracteres extra",
         "Se leyó un símbolo de Σ después de alcanzar q18", "js25@uni.edu.ni.mx"],
        ["Cadena vacía", "No hay símbolos que leer; q0 no es un estado final", "cadena vacía y null"],
    ],
    titulo="Tabla 2. Veredictos del validador y su origen en el autómata",
    anchos=[3.4, 8.6, 4.0], tam=9)

h(2, "4.4 Casos de prueba de referencia")
parrafo("El enunciado propone cuatro cadenas inválidas y dos válidas. La tabla siguiente contrasta "
        "cada una con el veredicto que produce la implementación; todas coinciden con lo esperado.")
tabla(
    ["Cadena", "Clasificación en el enunciado", "Veredicto del programa", "Tipo de rechazo"],
    [
        ["js25@uni.edu.ni", "Válida", "Aceptado", "—"],
        ["lmra123@uni.edu.ni", "Válida", "Aceptado", "—"],
        ["Js25@unicomp.edu", "Inválida: mayúscula no permitida", "Rechazado", "Símbolo no permitido"],
        ["j@unicomp.edu", "Inválida: falta el número", "Rechazado", "Iniciales inválidas"],
        ["js25@unicom.edu", "Inválida: dominio erróneo", "Rechazado", "Dominio inválido"],
        ["abc123@gmail.com", "Inválida: dominio incorrecto", "Rechazado", "Dominio inválido"],
    ],
    titulo="Tabla 3. Cadenas de referencia del enunciado y su resultado real",
    anchos=[4.2, 5.0, 3.2, 3.6], tam=9)

h(2, "4.5 Ambigüedades del enunciado y decisiones adoptadas")
parrafo("El enunciado contiene tres imprecisiones que debían resolverse antes de construir la "
        "matriz de transición. En todos los casos se adoptó la interpretación más estricta y "
        "coherente con la teoría de conjuntos, y la decisión quedó registrada en la documentación "
        "técnica del proyecto.")
tabla(
    ["Punto ambiguo", "Decisión adoptada"],
    [
        ["El alfabeto del enunciado repite e, d, u, n, i y omite la llave de cierre",
         "Esas letras ya pertenecen a a–z, de modo que el alfabeto correcto es "
         "Σ = {a–z} ∪ {0–9} ∪ {@, .}, con |Σ| = 38 símbolos"],
        ["Una mayúscula al final, como en js25@uni.edu.niX, se clasificaba como «caracteres extra»",
         "Como 'X' no pertenece a Σ, se clasifica como **símbolo no permitido**; «caracteres extra» "
         "se reserva para símbolos de Σ leídos después de q18 (por ejemplo '.', dígitos o minúsculas)"],
        ["No se aclara si se deben recortar los espacios de la entrada",
         "No se recorta nada: un espacio inicial o final es un símbolo fuera de Σ y la cadena se "
         "rechaza como **símbolo no permitido**"],
        ["No se indica qué ocurre con la cadena vacía o con null",
         "Se rechazan como **cadena vacía** sin lanzar excepciones, porque q0 no es un estado final "
         "y una función de validación no debe fallar con entradas degeneradas"],
        ["No se precisa si la aceptación puede ser parcial",
         "La aceptación es estricta al final: q18 solo es final si no quedan símbolos, porque toda "
         "transición desde q18 conduce a q19"],
    ],
    titulo="Tabla 4. Ambigüedades resueltas y su justificación",
    anchos=[7.0, 9.0], tam=9)

# --------------------------------------------------------------------------
# 5. FORMALIZACIÓN MATEMÁTICA
# --------------------------------------------------------------------------

h(1, "5. Formalización matemática del AFD")
parrafo("El validador se define como el autómata **M = (Q, Σ, δ, q0, F)**, en el que cada elemento "
        "está determinado por las reglas de la sección anterior. La tabla siguiente resume la tupla "
        "completa; los apartados posteriores justifican cada componente.")
tabla(
    ["Símbolo", "Elemento", "Valor en este proyecto"],
    [
        ["M", "Autómata", "M = (Q, Σ, δ, q0, F)"],
        ["Q", "Conjunto finito de estados", "{q0, q1, …, q19}, con |Q| = 20"],
        ["Σ", "Alfabeto", "{a, b, …, z, 0, 1, …, 9, \u0040, .}, con |Σ| = 38"],
        ["δ", "Función de transición", "δ: Q × Σ → Q, total, definida en 760 celdas"],
        ["q0", "Estado inicial", "q0"],
        ["F", "Conjunto de estados finales", "F = {q18}"],
    ],
    titulo="Tabla 5. Quinta tupla del autómata M",
    anchos=[2.0, 5.0, 9.0], tam=9, alineaciones=[CENTRO, IZQUIERDA, IZQUIERDA])

h(2, "5.1 Alfabeto (Σ)")
parrafo("El alfabeto reúne todos los símbolos que pueden aparecer en una dirección institucional "
        "bien formada: las 26 letras minúsculas sin acentos, los 10 dígitos decimales y los dos "
        "signos especiales del formato. La cardinalidad es 38 y **ningún otro carácter** forma "
        "parte de Σ, de modo que mayúsculas, espacios, guiones, guiones bajos, acentos y saltos de "
        "línea quedan fuera del lenguaje.")
tabla(
    ["Subconjunto", "Símbolos", "Cardinalidad"],
    [
        ["Letras minúsculas", "a, b, c, …, x, y, z", "26"],
        ["Dígitos", "0, 1, 2, …, 7, 8, 9", "10"],
        ["Signos especiales", "@ (arroba) y . (punto)", "2"],
        ["Total", "Σ = {a–z} ∪ {0–9} ∪ {@, .}", "38"],
    ],
    titulo="Tabla 6. Composición del alfabeto Σ",
    anchos=[4.0, 8.0, 4.0], tam=9, alineaciones=[IZQUIERDA, IZQUIERDA, CENTRO], color_total=True)

h(2, "5.2 Conjunto de estados (Q)")
parrafo("Los 20 estados se agrupan en cuatro etapas: conteo de iniciales, conteo de dígitos, "
        "reconocimiento del dominio y estados terminales. Cada estado recuerda exactamente la parte "
        "de la cadena que ya se leyó, que es la información mínima necesaria para decidir la "
        "siguiente transición.")
tabla(
    ["Estado", "Etapa", "Qué se ha leído y qué se espera"],
    [
        ["q0", "Inicio", "Nada leído; se espera la primera inicial"],
        ["q1", "Iniciales (1)", "Una letra, por ejemplo «j»; se espera otra letra"],
        ["q2", "Iniciales (2)", "«js»; mínimo cumplido, admite letra o dígito"],
        ["q3", "Iniciales (3)", "«jsx»; admite letra o dígito"],
        ["q4", "Iniciales (4)", "«jsxy»; máximo alcanzado, solo admite dígito"],
        ["q5", "Número (1)", "Un dígito; se espera otro dígito"],
        ["q6", "Número (2)", "Dos dígitos; admite dígito o '@' para cerrar la parte local"],
        ["q7", "Número (3)", "Tres dígitos; máximo alcanzado, solo admite '@'"],
        ["q8", "Dominio (1)", "Se leyó '@'; se espera 'u'"],
        ["q9", "Dominio (2)", "Se leyó «@u»; se espera 'n'"],
        ["q10", "Dominio (3)", "Se leyó «@un»; se espera 'i'"],
        ["q11", "Dominio (4)", "Se leyó «@uni»; se espera '.'"],
        ["q12", "Dominio (5)", "Se leyó «@uni.»; se espera 'e'"],
        ["q13", "Dominio (6)", "Se leyó «@uni.e»; se espera 'd'"],
        ["q14", "Dominio (7)", "Se leyó «@uni.ed»; se espera 'u'"],
        ["q15", "Dominio (8)", "Se leyó «@uni.edu»; se espera '.'"],
        ["q16", "Dominio (9)", "Se leyó «@uni.edu.»; se espera 'n'"],
        ["q17", "Dominio (10)", "Se leyó «@uni.edu.n»; se espera 'i'"],
        ["q18", "Aceptación", "Se completó «@uni.edu.ni»; único estado final"],
        ["q19", "Sumidero", "Error; absorbe cualquier símbolo y no permite salir (qE en la guía)"],
    ],
    titulo="Tabla 7. Los 20 estados del autómata y su significado",
    anchos=[1.8, 3.2, 11.0], tam=9, alineaciones=[CENTRO, IZQUIERDA, IZQUIERDA])

h(2, "5.3 Estado inicial y conjunto de estados finales")
parrafo("El estado inicial es **q0**, en el que el autómata no ha leído ningún símbolo. El conjunto "
        "de estados finales contiene un único elemento, **F = {q18}**, al que solo se llega tras "
        "procesar el sufijo exacto «.ni». Como toda transición desde q18 conduce a q19, la "
        "aceptación exige que la cadena termine justo en ese estado: no existe aceptación parcial ni "
        "prefijos aceptados.")

h(2, "5.4 Función de transición (δ)")
parrafo("La función **δ: Q × Σ → Q** es total: para cada uno de los 20 estados y cada uno de los 38 "
        "símbolos existe exactamente un estado de destino, lo que da **760 transiciones**. La "
        "matriz se construye en dos pasos: primero todas las celdas se inicializan en el sumidero "
        "q19 y después se sobrescriben únicamente las transiciones definidas. Así, ninguna celda "
        "queda indefinida y el autómata nunca se bloquea.")
tabla(
    ["Estado de origen", "Transiciones definidas", "Hacia q19", "Total"],
    [
        ["q0, q1", "26 (todas las letras a–z)", "12", "38 cada uno"],
        ["q2, q3", "36 (26 letras + 10 dígitos)", "2", "38 cada uno"],
        ["q4", "10 (solo dígitos)", "28", "38"],
        ["q5", "10 (solo dígitos)", "28", "38"],
        ["q6", "11 (10 dígitos + '@')", "27", "38"],
        ["q7", "1 (solo '@')", "37", "38"],
        ["q8 … q17", "1 por estado (la letra o el punto esperado)", "37 por estado", "38 cada uno"],
        ["q18", "0 (ninguna: no se admite nada después)", "38", "38"],
        ["q19", "38 (auto-lazo con todos los símbolos)", "0", "38"],
        ["Total", "—", "—", "760"],
    ],
    titulo="Tabla 8. Distribución de las 760 transiciones de δ",
    anchos=[3.4, 7.0, 3.0, 2.6], tam=9, color_total=True)

h(2, "5.5 Propiedades del autómata")
vineta("**Determinismo.** Cada celda de la matriz contiene un único estado de destino y no existen "
       "transiciones ε; para todo par (q, s) hay exactamente un δ(q, s).")
vineta("**Totalidad.** δ está definida en las 760 celdas, incluidas las de q19, que es un "
       "auto-lazo con todos los símbolos del alfabeto.")
vineta("**Sumidero absorbente.** Una vez en q19 no existe transición de salida; el rechazo es "
       "inmediato y definitivo, lo que evita seguir procesando una cadena que ya no puede aceptarse.")
vineta("**Aceptación estricta.** δ(q18, s) = q19 para todo s ∈ Σ, de modo que q18 solo es final si "
       "la cadena termina allí.")
vineta("**Lenguaje aceptado.** L(M) = { w ∈ Σ* : w = iniciales · número · «@uni.edu.ni», con entre "
       "2 y 4 iniciales y entre 2 y 3 dígitos }.")
vineta("**Complejidad.** El reconocimiento recorre la cadena una sola vez: tiempo O(n) y memoria "
       "constante O(1), con n = |w|.")

h(2, "5.6 Equivalencia de nomenclatura con la guía")
parrafo("La guía del curso nombra al estado de error como **qE**. En la implementación se usa el "
        "identificador **q19** para mantener una numeración continua de q0 a q19; la equivalencia "
        "es exacta y se resume a continuación.")
tabla(
    ["Nomenclatura de la guía", "Nomenclatura del proyecto", "Observación"],
    [
        ["q0", "q0", "Estado inicial, sin símbolos leídos"],
        ["q1 … q4", "q1 … q4", "Conteo de iniciales (2 a 4 letras)"],
        ["q5 … q7", "q5 … q7", "Conteo de dígitos (2 a 3 dígitos)"],
        ["q8 … q18", "q8 … q18", "Secuencia del dominio @uni.edu.ni; q18 es el estado final"],
        ["qE", "q19", "Estado pozo o de error (sumidero)"],
    ],
    titulo="Tabla 9. Correspondencia entre la notación de la guía y la del código",
    anchos=[4.6, 5.2, 6.2], tam=9, alineaciones=[CENTRO, CENTRO, IZQUIERDA])

# --------------------------------------------------------------------------
# 6. DIAGRAMA DE TRANSICIÓN Y TABLA DE TRANSICIONES
# --------------------------------------------------------------------------

h(1, "6. Diagrama de transición de estados y tabla de transiciones")

h(2, "6.1 Tabla de transiciones: parte local")
parrafo("En la parte local, δ depende únicamente de la **clase** del símbolo: cualquier letra de a "
        "a z produce el mismo destino, e igual ocurre con cualquier dígito. La matriz agrupada "
        "siguiente es, por tanto, una representación completa y sin pérdida de la función para los "
        "estados q0 a q7 y para los estados terminales.")
tabla(
    ["Estado", "L = {a … z}", "D = {0 … 9}", "@", "."],
    [
        ["q0", "q1", "q19", "q19", "q19"],
        ["q1", "q2", "q19", "q19", "q19"],
        ["q2", "q3", "q5", "q19", "q19"],
        ["q3", "q4", "q5", "q19", "q19"],
        ["q4", "q19", "q5", "q19", "q19"],
        ["q5", "q19", "q6", "q19", "q19"],
        ["q6", "q19", "q7", "q8", "q19"],
        ["q7", "q19", "q19", "q8", "q19"],
        ["q18", "q19", "q19", "q19", "q19"],
        ["q19", "q19", "q19", "q19", "q19"],
    ],
    titulo="Tabla 10. Transiciones de la parte local y de los estados terminales (por clase de símbolo)",
    anchos=[2.4, 3.6, 3.6, 2.6, 2.6], tam=9,
    alineaciones=[CENTRO, CENTRO, CENTRO, CENTRO, CENTRO], primera_col_negrita=True)

h(2, "6.2 Tabla de transiciones: parte del dominio")
parrafo("En el dominio la situación cambia: cada estado espera un **carácter exacto**, no una clase "
        "de símbolos. Por eso la tabla se presenta carácter por carácter; en todos estos estados "
        "cualquier otro símbolo del alfabeto conduce a q19.")
tabla(
    ["Estado", "Símbolo leído hasta ahora", "Carácter esperado", "Destino"],
    [
        ["q8", "@", "u", "q9"],
        ["q9", "@u", "n", "q10"],
        ["q10", "@un", "i", "q11"],
        ["q11", "@uni", ".", "q12"],
        ["q12", "@uni.", "e", "q13"],
        ["q13", "@uni.e", "d", "q14"],
        ["q14", "@uni.ed", "u", "q15"],
        ["q15", "@uni.edu", ".", "q16"],
        ["q16", "@uni.edu.", "n", "q17"],
        ["q17", "@uni.edu.n", "i", "q18"],
    ],
    titulo="Tabla 11. Transiciones del dominio: reconocimiento del sufijo @uni.edu.ni",
    anchos=[2.4, 6.0, 4.0, 3.0], tam=9,
    alineaciones=[CENTRO, CENTRO, CENTRO, CENTRO], primera_col_negrita=True)
parrafo("La combinación de las tablas 10 y 11, junto con la regla «cualquier símbolo no listado "
        "conduce a q19», define las 760 celdas de δ. La matriz completa, con una columna por cada "
        "uno de los 38 símbolos, se incluye en el **Anexo D** y puede consultarse en el propio "
        "programa con el botón **Tabla de transiciones δ**.", espacio_tras=10)

h(2, "6.3 Diagrama de transición de estados")
parrafo("El diagrama se generó con el mismo renderizador que utiliza la aplicación, de modo que la "
        "figura y lo que el usuario ve en pantalla son la misma representación. El trazado dispone "
        "los estados en dos filas en serpentina (q0 a q9 arriba y q10 a q19 abajo) y dibuja 22 "
        "aristas: 9 de la parte local, 10 del dominio, la arista de rechazo de q18 a q19 y las "
        "transiciones agrupadas hacia el sumidero.")
figura(DOCS + "diagrama-afd.png",
       "Figura 1. Diagrama de transición de estados del AFD, dibujado por la aplicación con Java2D",
       ancho_cm=15.6)
tabla(
    ["Convención", "Significado en el diagrama"],
    [
        ["Flecha entrante sin origen", "Estado inicial q0"],
        ["Círculo simple azul", "Estado intermedio"],
        ["Doble círculo verde", "Estado final q18 (aceptación)"],
        ["Círculo punteado rojo", "Estado sumidero q19, etiquetado como «sumidero»"],
        ["Etiquetas de las aristas", "Símbolos o conjuntos de símbolos: a–z, 0–9, @, ., y las letras exactas del dominio"],
        ["Aristas en curva", "Transiciones de rechazo agrupadas (L, D, @, . hacia q19)"],
    ],
    titulo="Tabla 12. Convenciones gráficas del diagrama de estados",
    anchos=[5.2, 10.8], tam=9)

h(2, "6.4 Diagrama con la traza de una validación")
parrafo("La aplicación resalta en naranja el camino recorrido por la última validación, tanto en los "
        "estados como en las aristas, y marca en rojo el carácter que provocó el rechazo cuando lo "
        "hay. La figura siguiente muestra el diagrama con la traza de **lmra123@uni.edu.ni**, una "
        "cadena aceptada que ejercita los límites máximos de iniciales (4) y dígitos (3).")
figura(DOCS + "diagrama-traza.png",
       "Figura 2. El mismo diagrama con la traza de lmra123@uni.edu.ni resaltada en naranja",
       ancho_cm=15.6)

# --------------------------------------------------------------------------
# 7. PRUEBAS Y VALIDACIÓN DE CADENAS
# --------------------------------------------------------------------------

h(1, "7. Pruebas y validación de cadenas")

h(2, "7.1 Metodología de prueba")
parrafo("La verificación se organizó en cuatro niveles complementarios, de modo que ningún aspecto "
        "del autómata quedara sin evidencia:")
numerada("**Trazas manuales**: ejecución paso a paso de cadenas representativas, registrando la "
         "secuencia completa de estados.")
numerada("**Batería funcional**: 22 casos con veredicto esperado, que el programa ejecuta desde el "
         "botón «Ejecutar pruebas» y contrasta automáticamente.")
numerada("**Pruebas unitarias automatizadas**: 47 invocaciones con JUnit 5 sobre el autómata, el "
         "dibujo del diagrama y el banco de casos.")
numerada("**Verificación cruzada**: 2 000 cadenas aleatorias comparadas contra la expresión regular "
         "equivalente ^[a-z]{2,4}[0-9]{2,3}@uni\\.edu\\.ni$, usada solo como referencia externa.")

h(2, "7.2 Trazas de cadenas válidas")
parrafo("Las cinco cadenas siguientes son aceptadas. Observe que todas terminan en q18 y que el "
        "número de transiciones es siempre igual a la longitud de la cadena.")
tabla(
    ["Cadena", "Secuencia de estados", "Trans.", "Resultado", "¿Esperado?"],
    [
        ["js25@uni.edu.ni",
         "q0 → q1 → q2 → q5 → q6 → q8 → q9 → q10 → q11 → q12 → q13 → q14 → q15 → q16 → q17 → q18",
         "15", "Aceptado", "Sí"],
        ["lmra123@uni.edu.ni",
         "q0 → q1 → q2 → q3 → q4 → q5 → q6 → q7 → q8 → q9 → q10 → q11 → q12 → q13 → q14 → q15 → q16 → q17 → q18",
         "18", "Aceptado", "Sí"],
        ["ab12@uni.edu.ni",
         "q0 → q1 → q2 → q5 → q6 → q8 → q9 → q10 → q11 → q12 → q13 → q14 → q15 → q16 → q17 → q18",
         "15", "Aceptado", "Sí"],
        ["abcd999@uni.edu.ni",
         "q0 → q1 → q2 → q3 → q4 → q5 → q6 → q7 → q8 → q9 → q10 → q11 → q12 → q13 → q14 → q15 → q16 → q17 → q18",
         "18", "Aceptado", "Sí"],
        ["zz99@uni.edu.ni",
         "q0 → q1 → q2 → q5 → q6 → q8 → q9 → q10 → q11 → q12 → q13 → q14 → q15 → q16 → q17 → q18",
         "15", "Aceptado", "Sí"],
    ],
    titulo="Tabla 13. Trazas de ejecución de cinco cadenas válidas",
    anchos=[3.4, 8.6, 1.4, 2.0, 1.6], tam=7.5,
    alineaciones=[IZQUIERDA, IZQUIERDA, CENTRO, CENTRO, CENTRO])

h(2, "7.3 Trazas de cadenas inválidas")
parrafo("Las cinco cadenas siguientes son rechazadas. En todos los casos el autómata identifica la "
        "posición exacta del fallo y clasifica el motivo, información que la interfaz muestra en la "
        "columna de traza y en la alerta modal.")
tabla(
    ["Cadena", "Secuencia de estados", "Tipo de rechazo", "Resultado", "¿Esperado?"],
    [
        ["Js25@unicomp.edu", "q0 → q19", "Símbolo no permitido: 'J' en la posición 0", "Rechazado", "Sí"],
        ["j@unicomp.edu", "q0 → q1 → q19", "Iniciales inválidas: '@' en la posición 1", "Rechazado", "Sí"],
        ["js25@unicom.edu",
         "q0 → q1 → q2 → q5 → q6 → q8 → q9 → q10 → q11 → q19",
         "Dominio inválido: 'c' en la posición 8 (se esperaba '.')", "Rechazado", "Sí"],
        ["abc123@gmail.com",
         "q0 → q1 → q2 → q3 → q5 → q6 → q7 → q8 → q19",
         "Dominio inválido: 'g' en la posición 7 (se esperaba 'u')", "Rechazado", "Sí"],
        ["js25@uni.edu.ni.mx",
         "q0 → q1 → q2 → q5 → q6 → q8 → q9 → q10 → q11 → q12 → q13 → q14 → q15 → q16 → q17 → q18 → q19",
         "Caracteres extra: '.' en la posición 15, después de q18", "Rechazado", "Sí"],
    ],
    titulo="Tabla 14. Trazas de ejecución de cinco cadenas inválidas",
    anchos=[3.4, 6.8, 4.6, 1.8, 1.4], tam=7.5,
    alineaciones=[IZQUIERDA, IZQUIERDA, IZQUIERDA, CENTRO, CENTRO])

h(2, "7.4 Batería completa de 22 casos")
parrafo("La batería cubre los cuatro límites de longitud, los tres errores de dominio más frecuentes, "
        "las mayúsculas, los espacios, los caracteres extra y las entradas degeneradas. La columna "
        "«¿Esperado?» compara el veredicto del autómata con el veredicto declarado en el archivo de "
        "casos.")
tabla(
    ["#", "Cadena", "Tipo de resultado", "Veredicto", "¿Esperado?"],
    [
        ["1", "js25@uni.edu.ni", "Válido", "Aceptado", "Sí"],
        ["2", "lmra123@uni.edu.ni", "Válido", "Aceptado", "Sí"],
        ["3", "ab12@uni.edu.ni", "Válido", "Aceptado", "Sí"],
        ["4", "abcd999@uni.edu.ni", "Válido", "Aceptado", "Sí"],
        ["5", "Js25@unicomp.edu", "Símbolo no permitido", "Rechazado", "Sí"],
        ["6", "j@unicomp.edu", "Iniciales inválidas", "Rechazado", "Sí"],
        ["7", "js25@unicom.edu", "Dominio inválido", "Rechazado", "Sí"],
        ["8", "abc123@gmail.com", "Dominio inválido", "Rechazado", "Sí"],
        ["9", "JS25@uni.edu.ni", "Símbolo no permitido", "Rechazado", "Sí"],
        ["10", "js25@UNI.EDU.NI", "Símbolo no permitido", "Rechazado", "Sí"],
        ["11", "js2@uni.edu.ni", "Dígitos inválidos", "Rechazado", "Sí"],
        ["12", "js2500@uni.edu.ni", "Dígitos inválidos", "Rechazado", "Sí"],
        ["13", "a12@uni.edu.ni", "Iniciales inválidas", "Rechazado", "Sí"],
        ["14", "abcde12@uni.edu.ni", "Iniciales inválidas", "Rechazado", "Sí"],
        ["15", "js25@uni.edu.ni.mx", "Caracteres extra", "Rechazado", "Sí"],
        ["16", "js25uni.edu.ni", "Dígitos inválidos", "Rechazado", "Sí"],
        ["17", "js25@uni.edu.ni (con espacio final)", "Símbolo no permitido", "Rechazado", "Sí"],
        ["18", "js25@uni.edu", "Dominio inválido", "Rechazado", "Sí"],
        ["19", "js25@uni.edu.n", "Dominio inválido", "Rechazado", "Sí"],
        ["20", "js25@uni.edu.ni#", "Símbolo no permitido", "Rechazado", "Sí"],
        ["21", "js25", "Dígitos inválidos", "Rechazado", "Sí"],
        ["22", "(cadena vacía)", "Cadena vacía", "Rechazado", "Sí"],
    ],
    titulo="Tabla 15. Resultado de los 22 casos de la batería funcional",
    anchos=[1.0, 6.2, 4.6, 2.4, 1.8], tam=8.5,
    alineaciones=[CENTRO, IZQUIERDA, IZQUIERDA, CENTRO, CENTRO])

h(2, "7.5 Resultados de la ejecución automatizada")
parrafo("La ejecución completa del programa sobre la batería produce **22/22 casos esperados "
        "correctos**, tal como muestra la figura siguiente: la tabla registra cada caso con su tipo "
        "de resultado y el veredicto obtenido.")
figura(DOCS + "captura-bateria.png",
       "Figura 3. Ejecución de la batería de 22 casos desde la interfaz del programa",
       ancho_cm=15.6)
parrafo("Las pruebas automatizadas confirman los mismos resultados y añaden la verificación "
        "estructural del autómata: **47 de 47 invocaciones correctas**, distribuidas en 39 pruebas "
        "del autómata (que incluyen las 760 celdas de δ, el comportamiento absorbente de q18 y q19 y "
        "las 2 000 cadenas aleatorias), 5 pruebas del dibujo del diagrama y 3 pruebas del banco de "
        "casos. La compilación de las 18 clases con la opción `-Xlint:all` de Java 17 no produce "
        "ni errores ni advertencias.")

h(2, "7.6 Análisis de casos límite")
tabla(
    ["Caso límite", "Cadena", "Comportamiento observado"],
    [
        ["Mínimo de iniciales y dígitos", "ab12@uni.edu.ni", "Aceptado en q18 con 15 transiciones"],
        ["Máximo de iniciales y dígitos", "abcd999@uni.edu.ni", "Aceptado en q18 con 18 transiciones"],
        ["Una sola inicial", "a12@uni.edu.ni", "Rechazado en la posición 1: iniciales inválidas"],
        ["Cinco iniciales", "abcde12@uni.edu.ni", "q4 no admite otra letra; rechazo en la posición 4"],
        ["Un solo dígito", "js2@uni.edu.ni", "Rechazado en la posición 3: dígitos inválidos"],
        ["Cuatro dígitos", "js2500@uni.edu.ni", "q7 solo admite '@'; rechazo en la posición 5"],
        ["Espacio final", "js25@uni.edu.ni ", "Rechazado en la posición 15: símbolo no permitido"],
        ["Espacio inicial", " js25@uni.edu.ni", "Rechazado en la posición 0: símbolo no permitido"],
        ["Salto de línea final", "js25@uni.edu.ni\\n", "Rechazado en la posición 15: símbolo no permitido"],
        ["Dominio truncado", "js25@uni.edu", "Termina en q15, que no es final: dominio inválido"],
        ["Dominio en mayúsculas", "js25@UNI.EDU.NI", "Rechazado en la posición 5: símbolo no permitido"],
        ["Cadena vacía", "\"\"", "Rechazada: q0 no es final y no se consume ningún símbolo"],
        ["Cadena nula", "null", "Rechazada sin excepción, con el motivo «Cadena nula»"],
    ],
    titulo="Tabla 16. Comportamiento del autómata en los casos límite",
    anchos=[4.2, 4.6, 7.2], tam=8.5)

h(2, "7.7 Discusión de los resultados")
parrafo("Los resultados muestran tres propiedades que conviene destacar. Primera, el autómata "
        "**detecta el error lo antes posible**: en Js25@unicomp.edu el rechazo ocurre en la "
        "posición 0, sin necesidad de procesar el resto de la cadena, gracias a que q19 es "
        "absorbente. Segunda, la clasificación del rechazo es **consecuencia del diseño**, no de una "
        "lista de condiciones: el tipo se deduce del estado de origen de la transición fallida, por "
        "lo que añadir un caso nuevo no obliga a reescribir la lógica de diagnóstico. Tercera, la "
        "coincidencia con la expresión regular en 2 000 cadenas aleatorias confirma que el lenguaje "
        "aceptado es exactamente el especificado, sin aceptar de más ni de menos.")
parrafo("La única diferencia relevante frente al enunciado se documentó como decisión de diseño: una "
        "mayúscula al final de la cadena se clasifica como símbolo no permitido y no como caracteres "
        "extra, porque no pertenece al alfabeto. Esta interpretación es más estricta y consistente "
        "con la definición de Σ, y está cubierta por las pruebas 9, 10 y 20 de la batería.")

# --------------------------------------------------------------------------
# 8. CONCLUSIONES Y RECOMENDACIONES
# --------------------------------------------------------------------------

h(1, "8. Conclusiones y recomendaciones")

h(2, "8.1 Conclusiones")
parrafo("El autómata diseñado reconoce exactamente el lenguaje institucional: acepta toda cadena de "
        "la forma <iniciales><número>@uni.edu.ni con entre 2 y 4 iniciales y entre 2 y 3 dígitos, y "
        "rechaza cualquier otra. La correspondencia con la expresión regular equivalente, verificada "
        "sobre 2 000 cadenas aleatorias, confirma que el modelo no acepta de más ni de menos.")
parrafo("La función de transición es **total y determinista**: las 760 celdas de la matriz tienen un "
        "único destino y no existen transiciones indefinidas ni transiciones ε. Esa propiedad es la "
        "que garantiza que, para cada par (estado, símbolo), exista una sola respuesta posible y que "
        "la validación se realice sin ambigüedades, condición indispensable en un sistema "
        "universitario donde dos validadores distintos no pueden dar veredictos diferentes.")
parrafo("El estado sumidero q19 aporta dos beneficios concretos. Por un lado, hace que el rechazo "
        "sea **inmediato e irreversible**, de modo que el autómata no continúa procesando una cadena "
        "que ya no puede aceptarse. Por otro, permite **explicar el error con precisión**: como el "
        "tipo de rechazo se deduce del estado de origen de la transición fallida, el programa indica "
        "la posición exacta y la causa, en lugar de devolver un simple «formato inválido».")
parrafo("La aceptación es estricta al final de la cadena, porque toda transición desde q18 conduce a "
        "q19. Esto evita un error frecuente en validadores caseros: aceptar un prefijo correcto y "
        "pasar por alto caracteres adicionales, como ocurre con js25@uni.edu.ni.mx.")
parrafo("La verificación fue exhaustiva y reproducible: **22/22** casos de la batería funcional, "
        "**47/47** pruebas automatizadas y compilación sin advertencias con `-Xlint:all`. El modelo "
        "matemático, el código y el diagrama se mantienen sincronizados porque el diagrama se dibuja "
        "en pantalla a partir de la misma matriz que valida las cadenas.")

h(2, "8.2 Recomendaciones")
numerada("**Ampliar el lenguaje a subdominios institucionales**, como @doc.uni.edu.ni para docentes o "
         "@est.uni.edu.ni para estudiantes. La extensión es natural: basta añadir estados para el "
         "nuevo sufijo manteniendo el resto de la matriz.")
numerada("**Integrar el validador con los servicios institucionales** (directorio, API de registro) "
         "para comprobar no solo el formato, sino también la existencia y disponibilidad de la "
         "cuenta antes de crearla.")
numerada("**Ofrecer el validador como servicio**: un pequeño servicio web REST o una biblioteca "
         "reutilizable permitiría que la plataforma virtual, el sistema de biblioteca y las "
         "aplicaciones internas compartan exactamente la misma regla de validación.")
numerada("**Añadir una normalización opcional y explícita** (por ejemplo, eliminar espacios "
         "accidentales o aplicar Unicode NFC) como un paso previo configurable y registrado, sin "
         "alterar el autómata, que debe seguir siendo estricto.")
numerada("**Automatizar la integración continua**: ejecutar `mvn test` en cada cambio, ampliar la "
         "batería con casos generados y medir el rendimiento con cadenas de longitud extrema.")
numerada("**Internacionalizar los mensajes** de la interfaz y exportar la traza a CSV o PDF para "
         "facilitar la auditoría de los registros rechazados.")

# --------------------------------------------------------------------------
# 9. REFERENCIAS
# --------------------------------------------------------------------------

h(1, "9. Referencias bibliográficas")


def referencia(partes):
    par = doc.add_paragraph()
    par.paragraph_format.alignment = IZQUIERDA
    par.paragraph_format.line_spacing = 1.5
    par.paragraph_format.space_after = Pt(10)
    par.paragraph_format.left_indent = Cm(1.27)
    par.paragraph_format.first_line_indent = Cm(-1.27)
    for texto, cursiva in partes:
        run = par.add_run(texto)
        fuente(run, "Arial", 12, False, cursiva)


parrafo("Las referencias siguen el formato APA, séptima edición.", tam=10, cursiva=True, color=GRIS,
        espacio_tras=10)
referencia([("Gamma, E., Helm, R., Johnson, R., & Vlissides, J. (1994). ", False),
            ("Design patterns: Elements of reusable object-oriented software", True),
            (". Addison-Wesley.", False)])
referencia([("Hopcroft, J. E., Motwani, R., & Ullman, J. D. (2007). ", False),
            ("Introducción a la teoría de autómatas, lenguajes y computación", True),
            (" (3.ª ed.). Pearson Educación.", False)])
referencia([("Oracle. (2024). ", False),
            ("Java Platform, Standard Edition 17 API specification", True),
            (". https://docs.oracle.com/en/java/javase/17/docs/api/", False)])
referencia([("Rosen, K. H. (2019). ", False),
            ("Matemáticas discretas y sus aplicaciones", True),
            (" (8.ª ed.). McGraw-Hill.", False)])
referencia([("Universidad Nacional de Ingeniería. (2026). ", False),
            ("Guía de Trabajo Final de Matemáticas Discretas: Validador de formato de correo "
             "institucional", True),
            (". Managua, Nicaragua.", False)])

# --------------------------------------------------------------------------
# 10. ANEXOS
# --------------------------------------------------------------------------

h(1, "10. Anexos: interfaces de usuario del programa")

h(2, "Anexo A. Diseño de la interfaz")
parrafo("La interfaz se construyó con Swing y se organiza en tres zonas: la cabecera institucional, el "
        "panel de validación (campo de entrada, botón y veredicto) y el panel de traza (tabla con el "
        "historial de validaciones). El pie de la ventana abre el diagrama del autómata y la tabla de "
        "transiciones, y muestra la barra de estado con los datos del modelo. El diseño busca que el "
        "usuario vea en un solo vistazo el veredicto, el motivo y el camino recorrido por el autómata.")
tabla(
    ["Componente", "Tipo", "Función"],
    [
        ["Campo de correo", "JTextField", "Entrada del texto; monoespaciado, límite de 64 caracteres y selección automática al recibir el foco"],
        ["VALIDAR", "JButton", "Ejecuta el autómata; es el botón por defecto, por lo que Enter también valida (atajo Alt+V)"],
        ["Etiqueta de veredicto", "JLabel", "Muestra ACEPTADO en verde o RECHAZADO en rojo; gris neutro antes de validar"],
        ["Etiqueta de detalle", "JLabel", "Indica el estado final, la posición del fallo y el tipo de rechazo"],
        ["Alerta modal", "JOptionPane", "Informativa al aceptar y de advertencia al rechazar; incluye el motivo y la secuencia"],
        ["Tabla de traza", "JTable", "Seis columnas: número, cadena, tipo, secuencia de estados, resultado y veredicto esperado"],
        ["Ejecutar pruebas", "JButton", "Valida los 22 casos de la batería y resume los aciertos"],
        ["Limpiar", "JButton", "Vacía la tabla, el campo y el veredicto, y devuelve el foco al campo"],
        ["Ver diagrama del AFD", "JButton", "Abre una ventana no modal que dibuja el autómata con Java2D (zoom, resaltado y exportación a PNG)"],
        ["Tabla de transiciones δ", "JButton", "Muestra la matriz agrupada por clases de símbolos"],
        ["Barra de estado", "JLabel", "Resume el modelo: estado inicial, |Σ| = 38, 20 estados y 1 estado final"],
    ],
    titulo="Tabla 17. Componentes de la interfaz gráfica",
    anchos=[3.6, 2.6, 9.8], tam=9)
figura(DOCS + "captura-interfaz.png",
       "Figura 4. Interfaz del programa en su estado inicial, antes de validar una cadena",
       ancho_cm=15.6)

h(2, "Anexo B. Capturas de pantalla del sistema en ejecución")
parrafo("Las capturas se obtuvieron ejecutando la aplicación. La alerta modal aparece sobre la ventana "
        "principal y se cierra con el botón «Aceptar»; su texto se transcribe bajo cada figura para "
        "dejar constancia exacta del mensaje que recibe el usuario.")

parrafo("**Figura B.1 — Validación exitosa de una cadena válida.**", tam=12, espacio_tras=2,
        alineacion=IZQUIERDA)
figura(DOCS + "captura-aceptado.png",
       "Figura 5. Interfaz del programa procesando correctamente js25@uni.edu.ni",
       ancho_cm=15.6)
parrafo("Texto de la alerta que muestra el programa al aceptar:", tam=10, cursiva=True, color=GRIS,
        espacio_tras=2)
codigo([
    "ACEPTADO",
    "",
    "«js25@uni.edu.ni»",
    "El correo cumple el formato institucional de la UNI.",
    "",
    "Secuencia: q0 → q1 → q2 → q5 → q6 → q8 → … → q17 → q18",
], tam=9)

parrafo("**Figura B.2 — Detección de un error de sintaxis en una cadena inválida.**", tam=12,
        espacio_tras=2, alineacion=IZQUIERDA)
figura(DOCS + "captura-rechazado.png",
       "Figura 6. Interfaz del programa rechazando Js25@unicomp.edu por una mayúscula",
       ancho_cm=15.6)
parrafo("Texto de la alerta que muestra el programa al rechazar:", tam=10, cursiva=True, color=GRIS,
        espacio_tras=2)
codigo([
    "RECHAZADO",
    "",
    "«Js25@unicomp.edu»",
    "Posición 0 ('J'): es una letra mayúscula, que no pertenece a Σ.",
    "Σ admite únicamente minúsculas a–z, dígitos 0–9, '@' y '.'.",
    "",
    "Secuencia: q0 → q19",
], tam=9)

h(2, "Anexo C. Extracto del código fuente (lógica del AFD)")
parrafo("El código se organiza en tres paquetes: **modelo** (autómata y datos), **vista** (interfaz) y "
        "**controlador** (conexión entre ambos). Los fragmentos siguientes pertenecen al modelo y "
        "muestran la matriz de transición y el bucle de validación. El código completo, con "
        "documentación y pruebas, se entrega junto con este informe.")

parrafo("**C.1 Construcción de la matriz δ (Delta.java).**", alineacion=IZQUIERDA, espacio_tras=2)
codigo([
    "private static final Estado[][] MATRIZ =",
    "        new Estado[Estado.values().length][Alfabeto.TAMANO];",
    "",
    "static {",
    "    for (Estado[] fila : MATRIZ) {",
    "        Arrays.fill(fila, Estado.q19);          // todas las celdas al sumidero",
    "    }",
    "",
    "    // Parte A: iniciales (2 a 4 letras minúsculas)",
    "    for (int columna = INICIO_LETRAS; columna < FIN_LETRAS; columna++) {",
    "        MATRIZ[Estado.q0.ordinal()][columna] = Estado.q1;",
    "        MATRIZ[Estado.q1.ordinal()][columna] = Estado.q2;",
    "        MATRIZ[Estado.q2.ordinal()][columna] = Estado.q3;",
    "        MATRIZ[Estado.q3.ordinal()][columna] = Estado.q4;",
    "    }",
    "",
    "    // Parte A: número (2 o 3 dígitos)",
    "    for (int columna = INICIO_DIGITOS; columna < FIN_DIGITOS; columna++) {",
    "        MATRIZ[Estado.q2.ordinal()][columna] = Estado.q5;",
    "        MATRIZ[Estado.q3.ordinal()][columna] = Estado.q5;",
    "        MATRIZ[Estado.q4.ordinal()][columna] = Estado.q5;",
    "        MATRIZ[Estado.q5.ordinal()][columna] = Estado.q6;",
    "        MATRIZ[Estado.q6.ordinal()][columna] = Estado.q7;",
    "    }",
    "",
    "    // Cierre de la parte local: solo '@' con 2 o 3 dígitos ya leídos",
    "    conectar(Estado.q6, Alfabeto.ARROBA, Estado.q8);",
    "    conectar(Estado.q7, Alfabeto.ARROBA, Estado.q8);",
    "",
    "    // Parte B: dominio @uni.edu.ni, transición por carácter exacto",
    "    conectar(Estado.q8, 'u', Estado.q9);",
    "    conectar(Estado.q9, 'n', Estado.q10);",
    "    conectar(Estado.q10, 'i', Estado.q11);",
    "    conectar(Estado.q11, Alfabeto.PUNTO, Estado.q12);",
    "    conectar(Estado.q12, 'e', Estado.q13);",
    "    conectar(Estado.q13, 'd', Estado.q14);",
    "    conectar(Estado.q14, 'u', Estado.q15);",
    "    conectar(Estado.q15, Alfabeto.PUNTO, Estado.q16);",
    "    conectar(Estado.q16, 'n', Estado.q17);",
    "    conectar(Estado.q17, 'i', Estado.q18);",
    "",
    "    // Parte C: q18 y q19 permanecen en q19",
    "}",
], tam=8)

parrafo("**C.2 Bucle de validación (Automata.java).**", alineacion=IZQUIERDA, espacio_tras=2)
codigo([
    "List<Estado> secuencia = new ArrayList<>(cadena.length() + 1);",
    "List<Paso> pasos = new ArrayList<>(cadena.length());",
    "Estado actual = ESTADO_INICIAL;",
    "secuencia.add(actual);",
    "",
    "for (int indice = 0; indice < cadena.length(); indice++) {",
    "    char simbolo = cadena.charAt(indice);",
    "    Estado siguiente = Delta.transicion(actual, simbolo);",
    "    pasos.add(new Paso(indice, simbolo, actual, siguiente));",
    "    secuencia.add(siguiente);",
    "",
    "    if (siguiente.esSumidero()) {",
    "        TipoResultado tipo = TipoResultado.desdeFallo(actual, simbolo);",
    "        return new ResultadoValidacion(cadena, false, tipo,",
    "                List.copyOf(secuencia), List.copyOf(pasos),",
    "                indice, Character.valueOf(simbolo),",
    "                motivoDeFallo(actual, simbolo, indice, tipo));",
    "    }",
    "    actual = siguiente;",
    "}",
    "",
    "if (ESTADOS_FINALES.contains(actual)) {",
    "    return new ResultadoValidacion(cadena, true, TipoResultado.VALIDO,",
    "            List.copyOf(secuencia), List.copyOf(pasos), -1, null,",
    "            \"Aceptada: la cadena se consumió por completo y terminó en q18.\");",
    "}",
], tam=8)

parrafo("**C.3 Clasificación del rechazo a partir del estado de origen (TipoResultado.java).**",
        alineacion=IZQUIERDA, espacio_tras=2)
codigo([
    "public static TipoResultado desdeFallo(Estado origen, char simbolo) {",
    "    if (!Alfabeto.pertenece(simbolo)) {",
    "        return SIMBOLO_INVALIDO;",
    "    }",
    "    if (origen.esFinal()) {",
    "        return CARACTERES_EXTRA;",
    "    }",
    "    if (origen.ordinal() <= Estado.q4.ordinal()) {",
    "        return INICIALES_INVALIDAS;",
    "    }",
    "    if (origen.ordinal() <= Estado.q7.ordinal()) {",
    "        return DIGITOS_INVALIDOS;",
    "    }",
    "    return DOMINIO_INVALIDO;",
    "}",
], tam=8)

# --------------------------------------------------------------------------
# ANEXO D: matriz completa (página horizontal)
# --------------------------------------------------------------------------

seccion_horizontal = doc.add_section(WD_SECTION.NEW_PAGE)
configurar_pagina(seccion_horizontal, horizontal=True)

h(2, "Anexo D. Matriz completa de transición δ (20 × 38 = 760 celdas)")
parrafo("La matriz siguiente reproduce, celda por celda, la función de transición implementada en el "
        "programa. La fila indica el estado de origen y la columna el símbolo leído; el valor de cada "
        "celda es el estado de destino. Las celdas sombreadas en azul son transiciones definidas y "
        "las celdas grises conducen al sumidero q19. La misma matriz se puede consultar dentro de la "
        "aplicación, en la ventana «Tabla de transiciones δ».", tam=10, espacio_tras=6)


def tabla_matriz(simbolos_encabezado, filas_matriz):
    t = doc.add_table(rows=1, cols=len(simbolos_encabezado) + 1)
    t.style = "Table Grid"
    t.alignment = WD_TABLE_ALIGNMENT.CENTER
    t.autofit = False
    celda = t.rows[0].cells[0]
    celda.text = ""
    p = celda.paragraphs[0]
    p.paragraph_format.alignment = CENTRO
    p.paragraph_format.line_spacing = 1.0
    p.paragraph_format.space_before = Pt(0)
    p.paragraph_format.space_after = Pt(0)
    r = p.add_run("δ")
    fuente(r, "Arial", 6, True, False, RGBColor(0xFF, 0xFF, 0xFF))
    sombrear(celda, "1F3864")
    for i, simbolo in enumerate(simbolos_encabezado):
        celda = t.rows[0].cells[i + 1]
        celda.text = ""
        p = celda.paragraphs[0]
        p.paragraph_format.alignment = CENTRO
        p.paragraph_format.line_spacing = 1.0
        p.paragraph_format.space_before = Pt(0)
        p.paragraph_format.space_after = Pt(0)
        r = p.add_run(simbolo)
        fuente(r, "Arial", 6, True, False, RGBColor(0xFF, 0xFF, 0xFF))
        sombrear(celda, "1F3864")
    for numero_estado, fila in enumerate(filas_matriz):
        celdas = t.add_row().cells
        celdas[0].text = ""
        p = celdas[0].paragraphs[0]
        p.paragraph_format.alignment = CENTRO
        p.paragraph_format.line_spacing = 1.0
        p.paragraph_format.space_before = Pt(0)
        p.paragraph_format.space_after = Pt(0)
        r = p.add_run("q%d" % numero_estado)
        fuente(r, "Arial", 6, True, False, OSCURO)
        sombrear(celdas[0], "E7ECF5")
        for i, destino in enumerate(fila):
            celda = celdas[i + 1]
            celda.text = ""
            p = celda.paragraphs[0]
            p.paragraph_format.alignment = CENTRO
            p.paragraph_format.line_spacing = 1.0
            p.paragraph_format.space_before = Pt(0)
            p.paragraph_format.space_after = Pt(0)
            r = p.add_run(destino)
            fuente(r, "Arial", 6, False, False, OSCURO if destino != "q19" else RGBColor(0x80, 0x80, 0x80))
            sombrear(celda, "DCE6F1" if destino != "q19" else "F2F2F2")
    anchos = [1.5] + [0.65] * len(simbolos_encabezado)
    for fila in t.rows:
        for i, ancho in enumerate(anchos):
            fila.cells[i].width = Cm(ancho)
    # Márgenes de celda mínimos y ancho fijo: "q19" debe caber en una sola línea.
    tblPr = t._tbl.tblPr
    margenes = OxmlElement("w:tblCellMar")
    for lado, valor in (("w:top", "0"), ("w:left", "28"), ("w:bottom", "0"), ("w:right", "28")):
        borde = OxmlElement(lado)
        borde.set(qn("w:w"), valor)
        borde.set(qn("w:type"), "dxa")
        margenes.append(borde)
    tblPr.insert_element_before(margenes, "w:tblLook", "w:tblCaption", "w:tblDescription")
    ancho_total = OxmlElement("w:tblW")
    ancho_total.set(qn("w:w"), str(int(sum(anchos) * 567)))
    ancho_total.set(qn("w:type"), "dxa")
    tblPr.insert_element_before(ancho_total, "w:jc", "w:tblCellSpacing", "w:tblInd", "w:tblBorders",
                                "w:shd", "w:tblLayout", "w:tblCellMar", "w:tblLook", "w:tblCaption",
                                "w:tblDescription")
    for fila in t.rows:
        propiedades = fila._tr.get_or_add_trPr()
        sin_division = OxmlElement("w:cantSplit")
        sin_division.set(qn("w:val"), "true")
        propiedades.append(sin_division)
    return t


LETRAS = "abcdefghijklmnopqrstuvwxyz"
DIGITOS = "0123456789"
SIMBOLOS = list(LETRAS + DIGITOS + "@.")
ESPERADOS = list("uni.edu.ni")


def calcular_matriz():
    filas = []
    for estado in range(20):
        fila = []
        for simbolo in SIMBOLOS:
            destino = "q19"
            if estado == 0 and simbolo in LETRAS:
                destino = "q1"
            elif estado == 1 and simbolo in LETRAS:
                destino = "q2"
            elif estado == 2:
                if simbolo in LETRAS:
                    destino = "q3"
                elif simbolo in DIGITOS:
                    destino = "q5"
            elif estado == 3:
                if simbolo in LETRAS:
                    destino = "q4"
                elif simbolo in DIGITOS:
                    destino = "q5"
            elif estado == 4 and simbolo in DIGITOS:
                destino = "q5"
            elif estado == 5 and simbolo in DIGITOS:
                destino = "q6"
            elif estado == 6:
                if simbolo in DIGITOS:
                    destino = "q7"
                elif simbolo == "@":
                    destino = "q8"
            elif estado == 7 and simbolo == "@":
                destino = "q8"
            elif 8 <= estado <= 17 and simbolo == ESPERADOS[estado - 8]:
                destino = "q%d" % (estado + 1)
            fila.append(destino)
        filas.append(fila)
    return filas


tabla_matriz(SIMBOLOS, calcular_matriz())
parrafo("", espacio_tras=2)
parrafo("Fin del informe.", tam=10, cursiva=True, color=GRIS, alineacion=CENTRO)

doc.save(SALIDA)
print("Informe generado:", SALIDA)
