# MiniLang - Compilador por fases

## Descripcion general

MiniLang es un compilador desarrollado en Java como proyecto de Compiladores. El proyecto fue construido por fases: primero el analizador lexico, luego el analizador sintactico y finalmente el analizador semantico con tabla de simbolos y comprobacion de tipos.

El lenguaje trabaja con sintaxis basada en indentacion, por lo que los bloques se reconocen usando saltos de linea, INDENT y DEDENT. No se utilizan llaves para definir bloques.

## Estructura del proyecto

```txt
MiniLang/
├── src/
│   ├── App.java
│   ├── Inicio.java
│   ├── AnalizadorLexico/
│   │   ├── AnalizadorLexico.java
│   │   └── Token.java
│   ├── AnalizadorSintactico/
│   │   ├── AnalizadorSintactico.java
│   │   ├── Parser.java
│   │   ├── Grafo.java
│   │   ├── Estado.java
│   │   └── Reglas.java
│   ├── AnalizadorSemantico/
│   │   ├── AnalizadorSemantico.java
│   │   └── TablaDeSimbolos.java
│   ├── Archivo/
│   │   └── ArchivoMiniLang.java
│   └── Stack/
│       └── PilaIdentacion.java
├── pruebas/
├── bin/
└── README.md
```

## Como ejecutar el proyecto

Desde la carpeta raiz del proyecto, ejecutar:

```cmd
rmdir /s /q bin
mkdir bin
javac -encoding UTF-8 -d bin src\App.java src\Inicio.java src\AnalizadorLexico\*.java src\AnalizadorSintactico\*.java src\AnalizadorSemantico\*.java src\Archivo\*.java src\Stack\*.java
java -cp bin App
```

Luego el programa pedira la ruta del archivo `.mlng`.

Ejemplo:

```txt
pruebas\prueba1_hola_mundo.mlng
```

## Archivos de entrada y salida

El programa recibe archivos con extension:

```txt
.mlng
```

Al analizar un archivo se generan dos salidas:

```txt
archivo.out
archivo.tabla
```

El archivo `.out` contiene:

- listado de tokens
- errores lexicos
- errores de indentacion
- errores sintacticos
- errores semanticos
- resultado final de compilacion

El archivo `.tabla` contiene la tabla de simbolos generada durante el analisis semantico.

## Comentarios

MiniLang maneja comentarios con `#`.

Todo lo que aparezca despues de `#` en la misma linea se ignora.

Ejemplo:

```txt
void main()
    write("Hola") # esto es un comentario
```

El analizador solo procesa:

```txt
void main()
    write("Hola")
```

## Sintaxis basada en indentacion

MiniLang no usa llaves para abrir o cerrar bloques. Los bloques se definen usando indentacion.

Ejemplo valido:

```txt
void main()
    int x = 5;
    if(x > 3)
        write("Mayor");
```

Ejemplo no valido:

```txt
void main() {
    int x = 5;
}
```

## Tokens principales

El analizador lexico reconoce:

### Palabras reservadas

```txt
const
void
return
int
float
string
String
bool
if
else
for
while
true
false
read
write
```

### Operadores

```txt
+
-
*
/
=
<
>
<=
>=
==
!=
!
++
--
```

### Simbolos

```txt
(
)
;
,
```

### Tokens especiales

```txt
NEWLINE
INDENT
DEDENT
EOF
```

## Tipos de datos

MiniLang reconoce los siguientes tipos:

```txt
int
float
string
bool
void
```

`void` se utiliza para metodos.

## Declaracion de variables

Ejemplos validos:

```txt
int a = 10;
float b = 3.5;
string texto = "hola";
bool activo = true;
```

Tambien se permite declarar sin inicializar, aunque semanticamente se reporta error si queda sin valor:

```txt
int a;
```

## Declaracion de constantes

Las constantes usan la palabra reservada `const`.

Ejemplos validos:

```txt
const int a = 10;
const float pi = 3.1416;
const string mensaje = "hola";
const bool activo = true;
```

Tambien se permite declarar una constante sin valor inicial:

```txt
const int limite;
```

Pero semanticamente debe recibir valor una unica vez. Si se intenta cambiar su valor despues de inicializada, se genera error semantico.

Ejemplo con error:

```txt
void main()
    const int limite = 10;
    limite = 20;
```

## Funciones y metodos

MiniLang permite funciones con tipo de retorno y metodos `void`.

Ejemplo de funcion:

```txt
int suma(int a, int b)
    int resultado = a + b;
    return resultado;
```

Ejemplo de metodo:

```txt
void main()
    int x = suma(2, 3);
    write(x);
```

Las funciones y metodos pueden tener parametros separados por coma.

Ejemplo:

```txt
float promedio(float a, float b)
    return (a + b) / 2;
```

## Metodo main

El programa debe tener un metodo `main`.

Ejemplo:

```txt
void main()
    write("Hola Mundo");
```

Solo puede existir un metodo `main`. Si hay mas de uno, se genera error semantico.

## Tabla de simbolos

La tabla de simbolos se genera en un archivo `.tabla`.

La tabla tiene las siguientes columnas:

```txt
ID | Nombre | Categoria | Tipo | Ambito | Valor/info
```

### ID

Identificador numerico unico asignado a cada simbolo.

### Nombre

Nombre del simbolo encontrado.

Ejemplos:

```txt
a
main
suma
```

### Categoria

Indica que representa el simbolo.

Puede ser:

```txt
variable
constante
parametro
funcion
metodo
```

### Tipo

Tipo de dato del simbolo.

Ejemplos:

```txt
int
float
string
bool
void
```

### Ambito

Indica donde pertenece el simbolo.

Si esta fuera de una funcion o metodo, su ambito es:

```txt
global
```

Si esta dentro de una funcion o metodo, su ambito es el nombre de esa funcion o metodo.

Ejemplo:

```txt
main
suma
imprimir
```

### Valor/info

Guarda el valor del simbolo o informacion adicional.

Ejemplos:

```txt
10
hola
true
no inicializado
parametro
parametros a, b
sin parametros
```

## Ambitos

MiniLang permite que existan variables con el mismo nombre en ambitos diferentes.

Ejemplo:

```txt
int a = 10;

void main()
    int a = 5;

int suma(int a, int b)
    return a + b;
```

En la tabla se registran como simbolos diferentes porque pertenecen a ambitos distintos.

## Analisis lexico

El analizador lexico se encarga de leer el archivo fuente y convertirlo en tokens.

Tambien detecta errores como caracteres no reconocidos.

Ejemplo:

```txt
int x = 5 @ 2;
```

Error esperado:

```txt
ERROR Caracter inesperado '@'
```

## Analisis de indentacion

La pila de indentacion valida que los niveles de indentacion sean correctos.

Reglas principales:

- La indentacion se calcula usando tabs y grupos de 4 espacios
- El nivel maximo permitido es 5
- No se permiten saltos invalidos de indentacion
- Se reporta si el archivo termina con indentacion abierta
- Se intenta recuperar la pila para continuar analizando

## Analisis sintactico

El analizador sintactico utiliza una estrategia bottom-up por reducciones.

El parser mantiene una pila de simbolos y reduce cuando el final de la pila coincide con el cuerpo de alguna regla gramatical.

Tambien utiliza lookaheads para evitar reducciones incorrectas.

El parser no se detiene al primer error. Intenta seguir analizando para reportar todos los errores posibles.

## Analisis semantico

El analizador semantico genera la tabla de simbolos y valida el significado del programa.

Validaciones principales:

- Solo puede existir un metodo main
- Las variables deben estar declaradas antes de usarse
- Las variables y constantes no deben duplicarse en el mismo ambito
- Los parametros no deben duplicarse en el mismo ambito
- Las variables y constantes deben inicializarse
- Las constantes solo pueden recibir valor una vez
- Las asignaciones deben respetar tipos
- Las funciones deben retornar un valor compatible con su tipo
- Los metodos void no deben retornar valor
- No se puede usar un metodo void como valor
- Las llamadas de funcion deben respetar cantidad y tipo de parametros
- No se pueden operar tipos incompatibles

## Coercion de tipos

MiniLang permite coercion de `int` hacia `float`.

Ejemplo valido:

```txt
void main()
    int a = 9;
    float b = 34.35 * a;
```

No se permite asignar `float` a `int` directamente.

Ejemplo con error:

```txt
void main()
    float a = 4.5;
    int b = a;
```

## Operaciones incompatibles

No se permite operar `string` o `bool` con operadores aritmeticos.

Ejemplo con error:

```txt
void main()
    string a = "hola";
    int b = 34 * a;
```

Ejemplo con error:

```txt
void main()
    bool activo = true;
    int x = activo + 1;
```

## Casos de prueba

El proyecto incluye una carpeta `pruebas` con los archivos requeridos por la fase 3.

### Prueba 1

Archivo:

```txt
prueba1_hola_mundo.mlng
```

Objetivo:

```txt
Programa que imprime Hola Mundo
```

### Prueba 2

Archivo:

```txt
prueba2_operacion_aritmetica.mlng
```

Objetivo:

```txt
Programa que realiza una operacion aritmetica basica
```

### Prueba 3

Archivo:

```txt
prueba3_read_if.mlng
```

Objetivo:

```txt
Programa con entrada de datos y flujo if
```

### Prueba 4

Archivo:

```txt
prueba4_funcion_parametros.mlng
```

Objetivo:

```txt
Funcion con parametros y retorno
```

### Prueba 5

Archivo:

```txt
prueba5_while.mlng
```

Objetivo:

```txt
Programa con variables, salida y ciclo while
```

### Prueba 6

Archivo:

```txt
prueba6_error_lexico.mlng
```

Objetivo:

```txt
Prueba negativa con error lexico
```

### Prueba 7

Archivo:

```txt
prueba7_error_sintactico.mlng
```

Objetivo:

```txt
Prueba negativa con error sintactico
```

### Prueba 8

Archivo:

```txt
prueba8_error_semantico.mlng
```

Objetivo:

```txt
Prueba negativa con error semantico
```

## Ejemplo completo valido

```txt
int suma(int a, int b)
    int resultado = a + b;
    return resultado;

void main()
    const int limite = 10;
    int x = suma(2, 3);
    float y = x * 2.5;
    if(x < limite)
        write("Dentro del limite");
    else
        write("Fuera del limite");
```

## Decisiones de diseno

El lenguaje se dejo basado en indentacion para simplificar el manejo de bloques y mantener coherencia con la pila de indentacion.

Los comentarios se manejan desde el analizador lexico y no llegan al parser.

La tabla de simbolos se implemento como una lista de objetos `Simbolo`, porque permite almacenar de forma clara el nombre, categoria, tipo, ambito, valor e informacion de ubicacion.

El analisis semantico usa recuperacion de errores, por lo que no se detiene con el primer problema encontrado.

## Resultado esperado

Si el archivo es correcto, el programa imprime:

```txt
OK
Sin errores
CADENA ACEPTADA
```

Si el archivo tiene errores, el programa imprime todos los errores encontrados y rechaza la cadena.

## Versionamiento

El proyecto fue trabajado usando Git y GitHub, con commits separados por fases para evidenciar el avance del compilador.