Contenido del README
Objetivo del proyecto
Requisitos
Estructura del proyecto
Cómo ejecutar
Explicación completa del código
Representación del grafo
Interfaz gráfica y eventos
Algoritmo Dijkstra
Método de ordenamiento (Merge Sort)
Uso de la aplicación
Pruebas rápidas
Mejoras recomendadas
Licencia
Objetivo del proyecto

Este proyecto permite:

✔ Dibujar un mapa real de Colombia
✔ Agregar ciudades visualmente
✔ Crear rutas entre ciudades con pesos (distancia)
✔ Calcular el camino más corto entre dos ciudades
✔ Mostrar las ciudades ordenadas alfabéticamente (Merge Sort)
✔ Mostrar visualmente el camino mínimo sobre el mapa

Requisitos

Java JDK 11+
No requiere librerías externas
Archivo de fondo: fondo_colombia.jpg

Estructura del proyecto
/proyecto_mapa
│
├─ 
│  └─ MapaColombiaEditor.java
│
├─ 
│  └─ fondo_colombia.jpg
│
└─ README.md
Cómo ejecutar
4.1 Compilar
javac -d out -sourcepath src src/MapaColombiaEditor.java
4.2 Ejecutar
cd out
java MapaColombiaEditor

4.3 (Opcional) Crear JAR ejecutable
jar cfe MapaColombiaEditor.jar MapaColombiaEditor .
java -jar MapaColombiaEditor.jar

Explicación del código (paso a paso)
 Representación del grafo

El grafo se almacena como:

Ciudades (nodos)
Map<String, Point> pos;


Guarda:
"Bogotá" → (x,y)
"Cali" → (x,y)

🛣️ Rutas (aristas)
Map<String, List<Edge>> grafo;
Cada ciudad tiene una lista de destinos con su peso.

🔗 Clase Edge
public class Edge {
    public final String destino;
    public final int peso;
    public Edge(String destino, int peso) {
        this.destino = destino;
        this.peso = peso;    }
🖥️ 5.2 Interfaz gráfica y eventos
🖼 Fondo del mapa

Se carga con:
Image fondo = ImageIO.read(new File("fondo_colombia.jpg"));

En modo edición:
Clic en la pantalla
Pedir nombre con JOptionPane

Guardar en pos.put(nombre, punto)
Crear ruta
Seleccionar origen
Seleccionar destino
Pedir distancia
Agregar:

grafo.get(origen).add(new Edge(destino, peso));
grafo.get(destino).add(new Edge(origen, peso));

Actualización visual
Cada acción usa:
repaint();

Algoritmo de Dijkstra (paso a paso)
1. Inicialización
Map<String, Integer> dist; // distancia mínima
Map<String, String> prev;  // nodo anterior
PriorityQueue<String> pq;  // cola priorizada


Todas las distancias = infinito, excepto el origen = 0.

2. Procesamiento
Sacar el nodo con menor distancia
Relajar cada arista

Si se mejora el camino:

dist.put(v, alt);
prev.put(v, u);
pq.remove(v);
pq.add(v);

Reconstrucción del camino
List<String> camino;


Se reconstruye desde el destino hacia atrás usando prev.

Complejidad
O((V + E) log V)
Eficiente para grafos medianos
Método de ordenamiento: Merge Sort
El proyecto implementa Merge Sort manualmente para ordenar la lista de ciudades.

1. Caso base
Si la lista tiene 0–1 elementos → ya está ordenada.
2. División
mid = n/2;
left  = mergeSort(sublista izquierda)
right = mergeSort(sublista derecha)

3. Merge (mezcla)
Comparar elementos de left y right:
if (left[i].compareToIgnoreCase(right[j]) <= 0)

4. Resultado
Devuelve una nueva lista ordenada.

 Complejidad
Tiempo: O(n log n)

Espacio: O(n)
 Uso de la aplicación
 Agregar ciudad

Activar modo edición
Clic en el mapa
Escribir nombre

Agregar ruta
Seleccionar origen
Seleccionar destino
Ingresar distancia
Calcular camino
Elegir origen y destino
Click en “Calcular Ruta”

Se mostrará:
El camino óptimo
La distancia total
El camino resaltado en el mapa
 Ver lista ordenada

El programa usa Merge Sort para mostrar las ciudades ordenadas alfabéticamente.

Pruebas rápidas recomendadas
 Prueba 1 — Rutas básicas
A → B (5)
B → C (3)
A → C (20)
Dijkstra(A,C) debe devolver:
A → B → C (distancia 8)
Prueba 2 — Ordenamiento
Insertar:
["Cali","bogota","Armenia"]
Resultado debe ser:
["Armenia","bogota","Cali"]

 Mejoras sugeridas
Exportar/Importar grafo a JSON
Arrastrar ciudades con el mouse
Añadir heurística A*
Evitar pq.remove() usando tuplas (dist, nodo)
Añadir colores y estilos al mapa

 Licencia
MIT License
Copyright (c) 2025
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files...

