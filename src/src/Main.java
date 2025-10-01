package src;

import java.io.*;
import java.util.*;

/**
 * Clase principal que procesa los archivos de vendedores, productos y ventas,
 * y genera los reportes solicitados.
 */
public class Main {

    public static void main(String[] args) {
        try {
            Map<Integer, Producto> productos = loadProductos();
            Map<Long, String> vendedores = loadVendedores();

            Map<Long, Double> dineroPorVendedor = new HashMap<>();
            Map<Integer, Integer> ventasPorProducto = new HashMap<>();

            File folder = new File(".");
            for (File file : folder.listFiles((d, name) -> name.startsWith("ventas_"))) {
                processSalesFile(file, productos, dineroPorVendedor, ventasPorProducto);
            }

            createReporteVendedores(vendedores, dineroPorVendedor);
            createReporteProductos(productos, ventasPorProducto);
            System.out.println("Reportes generados exitosamente.");
        } catch (Exception e) {
            System.out.println("Error procesando archivos: " + e.getMessage());
        }
    }

    /**
     * Carga los productos desde productos.txt
     */
    private static Map<Integer, Producto> loadProductos() throws IOException {
        Map<Integer, Producto> map = new HashMap<>();
        try (Scanner sc = new Scanner(new File("productos.txt"))) {
            while (sc.hasNextLine()) {
                String[] parts = sc.nextLine().split(";");
                if (parts.length != 3) {
                    logError("Formato incorrecto en productos.txt: " + Arrays.toString(parts));
                    continue;
                }
                try {
                    int id = Integer.parseInt(parts[0]);
                    String nombre = parts[1];
                    double precio = Double.parseDouble(parts[2]);
                    if (precio < 0) {
                        logError("Precio negativo en producto ID " + id);
                        continue;
                    }
                    map.put(id, new Producto(id, nombre, precio));
                } catch (NumberFormatException ex) {
                    logError("Error numérico en productos.txt: " + Arrays.toString(parts));
                }
            }
        }
        return map;
    }

    /**
     * Carga la información de vendedores desde vendedores.txt
     */
    private static Map<Long, String> loadVendedores() throws IOException {
        Map<Long, String> map = new HashMap<>();
        try (Scanner sc = new Scanner(new File("vendedores.txt"))) {
            while (sc.hasNextLine()) {
                String[] parts = sc.nextLine().split(";");
                if (parts.length != 4) {
                    logError("Formato incorrecto en vendedores.txt: " + Arrays.toString(parts));
                    continue;
                }
                try {
                    long id = Long.parseLong(parts[1]);
                    String nombreCompleto = parts[2] + " " + parts[3];
                    map.put(id, nombreCompleto);
                } catch (NumberFormatException ex) {
                    logError("Error numérico en vendedores.txt: " + Arrays.toString(parts));
                }
            }
        }
        return map;
    }

    /**
     * Procesa un archivo de ventas de un vendedor.
     */
    private static void processSalesFile(File file,
                                         Map<Integer, Producto> productos,
                                         Map<Long, Double> dineroPorVendedor,
                                         Map<Integer, Integer> ventasPorProducto) throws IOException {
        try (Scanner sc = new Scanner(file)) {
            if (!sc.hasNextLine()) {
                logError("Archivo vacío: " + file.getName());
                return;
            }
            String header = sc.nextLine(); // CC;ID
            long vendedorId;
            try {
                vendedorId = Long.parseLong(header.split(";")[1]);
            } catch (Exception e) {
                logError("Cabecera inválida en archivo " + file.getName() + ": " + header);
                return;
            }

            while (sc.hasNextLine()) {
                String[] parts = sc.nextLine().split(";");
                if (parts.length != 2) {
                    logError("Línea inválida en " + file.getName() + ": " + Arrays.toString(parts));
                    continue;
                }
                try {
                    int idProd = Integer.parseInt(parts[0]);
                    int cant = Integer.parseInt(parts[1]);
                    if (cant <= 0) {
                        logError("Cantidad inválida (" + cant + ") en " + file.getName());
                        continue;
                    }
                    Producto p = productos.get(idProd);
                    if (p == null) {
                        logError("Producto no encontrado ID " + idProd + " en " + file.getName());
                        continue;
                    }
                    // Acumular dinero para vendedor
                    dineroPorVendedor.put(vendedorId,
                            dineroPorVendedor.getOrDefault(vendedorId, 0.0) + (cant * p.getPrecio()));
                    // Acumular cantidad para producto
                    ventasPorProducto.put(idProd,
                            ventasPorProducto.getOrDefault(idProd, 0) + cant);
                } catch (NumberFormatException ex) {
                    logError("Error numérico en " + file.getName() + ": " + Arrays.toString(parts));
                }
            }
        }
    }

    /**
     * Genera reporte de vendedores en dinero recaudado.
     */
    private static void createReporteVendedores(Map<Long, String> vendedores,
                                                Map<Long, Double> ventas) throws IOException {
        List<Map.Entry<Long, Double>> lista = new ArrayList<>(ventas.entrySet());
        lista.sort((a, b) -> Double.compare(b.getValue(), a.getValue())); // Descendente
        try (PrintWriter pw = new PrintWriter(new FileWriter("reporte_vendedores.csv"))) {
            for (Map.Entry<Long, Double> e : lista) {
                String nombre = vendedores.getOrDefault(e.getKey(), "Desconocido");
                pw.println(nombre + ";" + String.format("%.2f", e.getValue()));
            }
        }
    }

    /**
     * Genera reporte de productos vendidos.
     */
    private static void createReporteProductos(Map<Integer, Producto> productos,
                                               Map<Integer, Integer> ventas) throws IOException {
        List<Map.Entry<Integer, Integer>> lista = new ArrayList<>(ventas.entrySet());
        lista.sort((a, b) -> b.getValue() - a.getValue());
        try (PrintWriter pw = new PrintWriter(new FileWriter("reporte_productos.csv"))) {
            for (Map.Entry<Integer, Integer> e : lista) {
                Producto p = productos.get(e.getKey());
                if (p != null) {
                    pw.println(p.getNombre() + ";" + p.getPrecio() + ";" + e.getValue());
                }
            }
        }
    }

    /**
     * Escribe un mensaje de error en errores.log
     */
    private static void logError(String mensaje) {
        try (PrintWriter pw = new PrintWriter(new FileWriter("errores.log", true))) {
            pw.println(mensaje);
        } catch (IOException e) {
            System.out.println("No se pudo escribir en errores.log: " + e.getMessage());
        }
    }
}

/**
 * Clase auxiliar para representar productos.
 */
class Producto {
    private int id;
    private String nombre;
    private double precio;

    public Producto(int id, String nombre, double precio) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
}
