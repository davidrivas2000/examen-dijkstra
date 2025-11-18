import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;

class Edge {
    String destino;
    int peso;
    Edge(String d, int p) { destino = d; peso = p; }
}

public class MapaColombiaEditor extends JPanel {

    //Posiciones (coordenadas en el panel)
    private Map<String, Point> pos = new LinkedHashMap<>(); 
    //Grafo:lista de adyacencia
    private Map<String, List<Edge>> grafo = new HashMap<>();
    //Camino resultante
    private List<String> caminoMinimo = new ArrayList<>();

    private String origen = "Cali";
    private String destino = "Cartagena";

    // Estado UI
    private boolean modoEdicion = false;
    private BufferedImage fondo = null;
    private File archivoFondo = null;
    // Orden de visualización de ciudades
    private boolean ordenarAsc = true;

    //Tamaños de dibujo
    private static final int NODO_RADIO = 8;

    public MapaColombiaEditor() {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        //Cargar fondo de Colombia automáticamente
        try {
            File imgFile = new File("fondo_colombia.jpg");
            if (imgFile.exists()) {
                fondo = ImageIO.read(imgFile);
                archivoFondo = imgFile;
            }
        } catch (Exception ex) {
            System.err.println("No se pudo cargar la imagen de fondo: " + ex.getMessage());
        }

        //Posiciones iniciales de ejemplo
        agregarCiudad("Cali", 100, 350);
        agregarCiudad("Buga", 130, 330);
        agregarCiudad("Armenia", 180, 300);
        agregarCiudad("Popayán", 120, 390);
        agregarCiudad("Ibagué", 220, 250);
        agregarCiudad("Bogotá", 260, 200);
        agregarCiudad("Medellín", 200, 180);
        agregarCiudad("Sincelejo", 150, 100);
        agregarCiudad("Cartagena", 180, 60);

        // Aristas iniciales
        addEdge("Cali", "Buga", 60);
        addEdge("Cali", "Armenia", 200);
        addEdge("Cali", "Popayán", 140);
        addEdge("Armenia", "Ibagué", 100);
        addEdge("Ibagué", "Bogotá", 200);
        addEdge("Bogotá", "Medellín", 420);
        addEdge("Medellín", "Sincelejo", 350);
        addEdge("Sincelejo", "Cartagena", 200);

        // Calcular ruta por defecto
        caminoMinimo = dijkstra(origen, destino);

        // Mouse listener para modo edición: agregar ciudades al hacer click
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (modoEdicion && SwingUtilities.isLeftMouseButton(e)) {
                    int x = e.getX();
                    int y = e.getY();
                    String nombre = JOptionPane.showInputDialog(MapaColombiaEditor.this,
                            "Nombre de la nueva ciudad:", "Agregar Ciudad", JOptionPane.PLAIN_MESSAGE);
                    if (nombre != null) {
                        nombre = nombre.trim();
                        if (nombre.isEmpty()) {
                            JOptionPane.showMessageDialog(MapaColombiaEditor.this,
                                    "Nombre vacío. No se agregó la ciudad.", "Error", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        if (pos.containsKey(nombre)) {
                            JOptionPane.showMessageDialog(MapaColombiaEditor.this,
                                    "Ya existe una ciudad con ese nombre.", "Error", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        agregarCiudad(nombre, x, y);
                        repaint();
                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                
                String ciudad = ciudadEnPunto(e.getX(), e.getY());
                setToolTipText(ciudad != null ? ciudad : null);
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    // Añade ciudad con coordenadas a estructuras
    private void agregarCiudad(String nombre, int x, int y) {
        pos.put(nombre, new Point(x, y));
        grafo.putIfAbsent(nombre, new ArrayList<>());
    }

    // Añadir arista bidireccional
    private void addEdge(String a, String b, int peso) {
        grafo.putIfAbsent(a, new ArrayList<>());
        grafo.putIfAbsent(b, new ArrayList<>());
        // Evitar duplicados
        boolean actualizado = false;
        for (Edge e : grafo.get(a)) {
            if (e.destino.equals(b)) {
                e.peso = peso;
                actualizado = true;
                break;
            }
        }
        for (Edge e : grafo.get(b)) {
            if (e.destino.equals(a)) {
                e.peso = peso;
                actualizado = true;
                break;
            }
        }
        if (!actualizado) {
            grafo.get(a).add(new Edge(b, peso));
            grafo.get(b).add(new Edge(a, peso));
        }
    }

    // Eliminar arista
    private void removeEdge(String a, String b) {
        if (grafo.containsKey(a)) {
            grafo.get(a).removeIf(e -> e.destino.equals(b));
        }
        if (grafo.containsKey(b)) {
            grafo.get(b).removeIf(e -> e.destino.equals(a));
        }
    }

    // Encontrar ciudad en punto dentro del radio del nodo
    private String ciudadEnPunto(int x, int y) {
        for (Map.Entry<String, Point> en : pos.entrySet()) {
            Point p = en.getValue();
            int dx = x - p.x, dy = y - p.y;
            if (dx * dx + dy * dy <= (NODO_RADIO + 3) * (NODO_RADIO + 3)) {
                return en.getKey();
            }
        }
        return null;
    }

    // Dijkstra que devuelve camino 
    private List<String> dijkstra(String inicio, String destino) {
        if (!grafo.containsKey(inicio) || !grafo.containsKey(destino)) return new ArrayList<>();

        Map<String, Integer> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));

        for (String c : grafo.keySet()) dist.put(c, Integer.MAX_VALUE);
        dist.put(inicio, 0);
        pq.add(inicio);

        while (!pq.isEmpty()) {
            String u = pq.poll();
            int distU = dist.get(u);
            for (Edge e : grafo.get(u)) {
                int nd = distU + e.peso;
                if (nd < dist.get(e.destino)) {
                    dist.put(e.destino, nd);
                    prev.put(e.destino, u);
                    pq.remove(e.destino);
                    pq.add(e.destino);
                }
            }
        }

        //Reconstruir camino
        List<String> camino = new ArrayList<>();
        if (dist.get(destino) == Integer.MAX_VALUE) {
            return camino;
        }
        String nodo = destino;
        while (nodo != null) {
            camino.add(0, nodo);
            nodo = prev.get(nodo);
        }
        return camino;
    }

    //Suma las distancias del camino 
    private int distanciaTotalCamino(List<String> camino) {
        if (camino == null || camino.size() < 2) return 0;
        int sum = 0;
        for (int i = 0; i < camino.size() - 1; i++) {
            String a = camino.get(i);
            String b = camino.get(i + 1);
            boolean encontrado = false;
            for (Edge e : grafo.getOrDefault(a, Collections.emptyList())) {
                if (e.destino.equals(b)) {
                    sum += e.peso;
                    encontrado = true;
                    break;
                }
            }
            if (!encontrado) return Integer.MAX_VALUE;
        }
        return sum;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //Dibujar fondo 
        if (fondo != null) {
            int w = getWidth(), h = getHeight();
            double sx = (double) w / fondo.getWidth();
            double sy = (double) h / fondo.getHeight();
            double s = Math.min(sx, sy);
            int iw = (int) (fondo.getWidth() * s);
            int ih = (int) (fondo.getHeight() * s);
            int x = (w - iw) / 2;
            int y = (h - ih) / 2;
            AffineTransform at = AffineTransform.getTranslateInstance(x, y);
            at.scale(s, s);
            g2.drawImage(fondo, at, null);
        }

        //dibujar rutas
        g2.setStroke(new BasicStroke(2));
        g2.setColor(Color.LIGHT_GRAY);
        Font fontDistancia = new Font("Arial", Font.BOLD, 11);
        Font fontOriginal = g2.getFont();
        
        for (String a : grafo.keySet()) {
            Point p1 = pos.get(a);
            if (p1 == null) continue;
            for (Edge e : grafo.get(a)) {
                String b = e.destino;
            
                if (!pos.containsKey(b)) continue;
                if (a.compareTo(b) > 0) continue;
                Point p2 = pos.get(b);
                g2.drawLine(p1.x, p1.y, p2.x, p2.y);

                //etiqueta distancia
                int midX = (p1.x + p2.x) / 2;
                int midY = (p1.y + p2.y) / 2;
                String distStr = e.peso + " km";
                
                g2.setFont(fontDistancia);
                //Sombra
                g2.setColor(new Color(0, 0, 0, 150));
                g2.drawString(distStr, midX + 5, midY - 2);
                //Contorno blanco
                g2.setColor(Color.WHITE);
                g2.drawString(distStr, midX + 4, midY - 3);
                g2.drawString(distStr, midX + 6, midY - 3);
                g2.drawString(distStr, midX + 4, midY - 1);
                g2.drawString(distStr, midX + 6, midY - 1);
                //Texto principal
                g2.setColor(new Color(255, 100, 100));
                g2.drawString(distStr, midX + 5, midY - 2);
                g2.setFont(fontOriginal);
            }
        }

        //dibujar camino minimo
        if (caminoMinimo != null && caminoMinimo.size() >= 2) {
            g2.setColor(Color.GREEN.darker());
            g2.setStroke(new BasicStroke(4));
            for (int i = 0; i < caminoMinimo.size() - 1; i++) {
                String a = caminoMinimo.get(i);
                String b = caminoMinimo.get(i + 1);
                Point p1 = pos.get(a);
                Point p2 = pos.get(b);
                if (p1 != null && p2 != null) {
                    g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
            }
        }

        //dibujar nodos
        Font fontCiudades = new Font("Arial", Font.BOLD, 14);
        g2.setFont(fontCiudades);
        
        for (Map.Entry<String, Point> en : pos.entrySet()) {
            String ciudad = en.getKey();
            Point p = en.getValue();
            boolean enCamino = caminoMinimo != null && caminoMinimo.contains(ciudad);

            if (enCamino) {
                g2.setColor(Color.ORANGE);
                g2.fillOval(p.x - (NODO_RADIO + 2), p.y - (NODO_RADIO + 2), (NODO_RADIO + 2) * 2, (NODO_RADIO + 2) * 2);
            } else {
                g2.setColor(Color.BLUE);
                g2.fillOval(p.x - NODO_RADIO, p.y - NODO_RADIO, NODO_RADIO * 2, NODO_RADIO * 2);
            }

            //Dibujar texto 
            int textX = p.x + NODO_RADIO + 4;
            int textY = p.y - NODO_RADIO - 2;
            
            //Sombra negra
            g2.setColor(new Color(0, 0, 0, 180));
            g2.drawString(ciudad, textX + 2, textY + 2);
            
            //Contorno blanco grueso
            g2.setColor(Color.WHITE);
            g2.drawString(ciudad, textX - 1, textY - 1);
            g2.drawString(ciudad, textX + 1, textY - 1);
            g2.drawString(ciudad, textX - 1, textY + 1);
            g2.drawString(ciudad, textX + 1, textY + 1);
            g2.drawString(ciudad, textX, textY - 1);
            g2.drawString(ciudad, textX, textY + 1);
            g2.drawString(ciudad, textX - 1, textY);
            g2.drawString(ciudad, textX + 1, textY);
            
            //Texto principal en negro
            g2.setColor(Color.BLACK);
            g2.drawString(ciudad, textX, textY);
        }
        
        g2.setFont(fontOriginal);

        //Si estamos en modo edición, mostrar un indicador
        if (modoEdicion) {
            Font fontModo = new Font("Arial", Font.BOLD, 13);
            g2.setFont(fontModo);
            g2.setColor(new Color(255, 140, 0, 180));
            g2.fillRoundRect(8, 8, 240, 26, 8, 8);
            
            // Contorno oscuro
            g2.setColor(new Color(100, 70, 0, 200));
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(8, 8, 240, 26, 8, 8);
            
            g2.setColor(Color.WHITE);
            g2.drawString("MODO EDICIÓN: Clic → agregar ciudad", 16, 26);
        }

        //Mostrar indicador de orden en esquina superior derecha
        String ord = ordenarAsc ? "Orden: A→Z" : "Orden: Z→A";
        Font fontOrden = new Font("Arial", Font.BOLD, 12);
        g2.setFont(fontOrden);
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(ord) + 14;
        int tx = getWidth() - tw - 10;
        
        g2.setColor(new Color(70, 130, 180, 200));
        g2.fillRoundRect(tx, 8, tw, 26, 8, 8);
        
        //Contorno
        g2.setColor(new Color(30, 60, 120, 220));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(tx, 8, tw, 26, 8, 8);
        
        g2.setColor(Color.WHITE);
        g2.drawString(ord, tx + 7, 26);
        g2.setFont(fontOriginal);
    }

    //Interfaz
    private static void crearYMostrarUI() {
        JFrame frame = new JFrame("Mapa de Colombia - Editor & Dijkstra");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700);

        MapaColombiaEditor mapa = new MapaColombiaEditor();
        frame.add(mapa, BorderLayout.CENTER);

        //Panel de controles superior
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT));

        //origen/destino
        JComboBox<String> comboOrigen = new JComboBox<>(mapa.getCiudadesArray());
        JComboBox<String> comboDestino = new JComboBox<>(mapa.getCiudadesArray());
        comboOrigen.setSelectedItem(mapa.origen);
        comboDestino.setSelectedItem(mapa.destino);

    JButton btnCalcular = new JButton("Calcular Ruta");
    JToggleButton toggleEdicion = new JToggleButton("Activar Modo Edición");
    JButton btnAgregarRuta = new JButton("Agregar/Actualizar Ruta");
    JButton btnEliminarRuta = new JButton("Eliminar Ruta");
    JButton btnCargarFondo = new JButton("Cargar Fondo (imagen)");
    JButton btnGuardar = new JButton("Exportar Lista (console)");
    // Indicador y control de orden
    final JLabel lblOrden = new JLabel("Orden: A→Z");
    final JButton btnToggleOrden = new JButton("Orden: A→Z");

        controles.add(new JLabel("Origen:"));
        controles.add(comboOrigen);
        controles.add(new JLabel("Destino:"));
        controles.add(comboDestino);
        controles.add(btnCalcular);
        controles.add(toggleEdicion);
        controles.add(btnAgregarRuta);
        controles.add(btnEliminarRuta);
    controles.add(btnCargarFondo);
    controles.add(btnGuardar);
    controles.add(lblOrden);
    controles.add(btnToggleOrden);

        frame.add(controles, BorderLayout.NORTH);

        //Acciones
        btnCalcular.addActionListener(e -> {
            String o = (String) comboOrigen.getSelectedItem();
            String d = (String) comboDestino.getSelectedItem();
            mapa.origen = o;
            mapa.destino = d;
            mapa.caminoMinimo = mapa.dijkstra(o, d);
            mapa.repaint();

            if (mapa.caminoMinimo == null || mapa.caminoMinimo.isEmpty()) {
                JOptionPane.showMessageDialog(frame,
                        "No existe camino entre " + o + " y " + d + ".",
                        "Resultado", JOptionPane.WARNING_MESSAGE);
            } else {
                int total = mapa.distanciaTotalCamino(mapa.caminoMinimo);
                StringBuilder sb = new StringBuilder();
                sb.append("Camino más corto:\n");
                for (int i = 0; i < mapa.caminoMinimo.size(); i++) {
                    sb.append(mapa.caminoMinimo.get(i));
                    if (i < mapa.caminoMinimo.size() - 1) sb.append(" → ");
                }
                sb.append("\n\nDistancia total: ").append(total).append(" km");
                JOptionPane.showMessageDialog(frame, sb.toString(), "Resultado", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        toggleEdicion.addActionListener(e -> {
            mapa.modoEdicion = toggleEdicion.isSelected();
            toggleEdicion.setText(mapa.modoEdicion ? "Desactivar Modo Edición" : "Activar Modo Edición");
            mapa.repaint();
        });

        //Acción para alternar orden ascendente/descendente
        btnToggleOrden.addActionListener(e -> {
            mapa.ordenarAsc = !mapa.ordenarAsc;
            String texto = mapa.ordenarAsc ? "A→Z" : "Z→A";
            btnToggleOrden.setText("Orden: " + texto);
            lblOrden.setText("Orden: " + texto);
            // actualizar combos para reflejar nuevo orden
            actualizarCombos(comboOrigen, comboDestino, mapa);
            mapa.repaint();
        });

        btnAgregarRuta.addActionListener(e -> {
            // Ventana para seleccionar dos ciudades y distancia
            JPanel panel = new JPanel(new GridLayout(3, 2, 6, 6));
            JComboBox<String> cbA = new JComboBox<>(mapa.getCiudadesArray());
            JComboBox<String> cbB = new JComboBox<>(mapa.getCiudadesArray());
            JTextField tfDist = new JTextField();

            panel.add(new JLabel("Ciudad A:"));
            panel.add(cbA);
            panel.add(new JLabel("Ciudad B:"));
            panel.add(cbB);
            panel.add(new JLabel("Distancia (km):"));
            panel.add(tfDist);

            int res = JOptionPane.showConfirmDialog(frame, panel, "Agregar/Actualizar Ruta", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res == JOptionPane.OK_OPTION) {
                String a = (String) cbA.getSelectedItem();
                String b = (String) cbB.getSelectedItem();
                if (a.equals(b)) {
                    JOptionPane.showMessageDialog(frame, "Selecciona dos ciudades diferentes.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String txt = tfDist.getText().trim();
                int distancia;
                try {
                    distancia = Integer.parseInt(txt);
                    if (distancia <= 0) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Introduce un número entero positivo para la distancia.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                mapa.addEdge(a, b, distancia);
                // actualizar comboboxes
                actualizarCombos(comboOrigen, comboDestino, mapa);
                mapa.repaint();
            }
        });

        btnEliminarRuta.addActionListener(e -> {
            JPanel panel = new JPanel(new GridLayout(2, 2, 6, 6));
            JComboBox<String> cbA = new JComboBox<>(mapa.getCiudadesArray());
            JComboBox<String> cbB = new JComboBox<>(mapa.getCiudadesArray());
            panel.add(new JLabel("Ciudad A:"));
            panel.add(cbA);
            panel.add(new JLabel("Ciudad B:"));
            panel.add(cbB);

            int res = JOptionPane.showConfirmDialog(frame, panel, "Eliminar Ruta", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res == JOptionPane.OK_OPTION) {
                String a = (String) cbA.getSelectedItem();
                String b = (String) cbB.getSelectedItem();
                if (a.equals(b)) {
                    JOptionPane.showMessageDialog(frame, "Selecciona dos ciudades diferentes.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                mapa.removeEdge(a, b);
                mapa.repaint();
            }
        });

        btnCargarFondo.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int r = chooser.showOpenDialog(frame);
            if (r == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                try {
                    BufferedImage img = ImageIO.read(f);
                    if (img == null) throw new Exception("Archivo no es una imagen válida.");
                    mapa.fondo = img;
                    mapa.archivoFondo = f;
                    mapa.repaint();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "No se pudo cargar la imagen:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        //imprime lista de ciudades y rutas en consola
        btnGuardar.addActionListener(e -> {
            System.out.println("Ciudades y posiciones:");
            for (Map.Entry<String, Point> en : mapa.pos.entrySet()) {
                System.out.printf("%s -> (%d,%d)%n", en.getKey(), en.getValue().x, en.getValue().y);
            }
            System.out.println("\nRutas:");
            // imprimir cada arista una sola vez
            Set<String> seen = new HashSet<>();
            for (String a : mapa.grafo.keySet()) {
                for (Edge edge : mapa.grafo.get(a)) {
                    String key = a + "##" + edge.destino;
                    String rev = edge.destino + "##" + a;
                    if (!seen.contains(rev)) {
                        System.out.printf("%s <-> %s : %d km%n", a, edge.destino, edge.peso);
                        seen.add(key);
                    }
                }
            }
            JOptionPane.showMessageDialog(frame, "Exportado a consola. Mira la salida estándar.", "Exportado", JOptionPane.INFORMATION_MESSAGE);
        });

        // Mostrar ventana
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void actualizarCombos(JComboBox<String> c1, JComboBox<String> c2, MapaColombiaEditor mapa) {
        String sel1 = (String) c1.getSelectedItem();
        String sel2 = (String) c2.getSelectedItem();
        c1.removeAllItems();
        c2.removeAllItems();
        for (String s : mapa.getCiudadesArray()) {
            c1.addItem(s);
            c2.addItem(s);
        }
        if (sel1 != null) c1.setSelectedItem(sel1);
        if (sel2 != null) c2.setSelectedItem(sel2);
    }

    // Devuelve arreglo de ciudades ordenado alfabéticamente
    public String[] getCiudadesArray() {
        List<String> lista = new ArrayList<>(pos.keySet());
        List<String> ordenada = mergeSort(lista);
        if (!ordenarAsc) {
            Collections.reverse(ordenada);
        }
        return ordenada.toArray(new String[0]);
    }

    //Implementación simple de Merge Sort para listas de String
    private List<String> mergeSort(List<String> input) {
        if (input == null) return new ArrayList<>();
        int n = input.size();
        if (n <= 1) return new ArrayList<>(input);

        int mid = n / 2;
        List<String> left = mergeSort(new ArrayList<>(input.subList(0, mid)));
        List<String> right = mergeSort(new ArrayList<>(input.subList(mid, n)));

        List<String> merged = new ArrayList<>(n);
        int i = 0, j = 0;
        while (i < left.size() && j < right.size()) {
            String a = left.get(i);
            String b = right.get(j);
            if (a.compareToIgnoreCase(b) <= 0) {
                merged.add(a);
                i++;
            } else {
                merged.add(b);
                j++;
            }
        }
        while (i < left.size()) merged.add(left.get(i++));
        while (j < right.size()) merged.add(right.get(j++));
        return merged;
    }

    public static void main(String[] args) {
        // Swing UI thread
        SwingUtilities.invokeLater(MapaColombiaEditor::crearYMostrarUI);
    }
}
