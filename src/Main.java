import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        try {
            Map<Integer, String> productos = loadProductos();
            Map<Long, String> vendedores = loadVendedores();

            Map<Long, Integer> ventasPorVendedor = new HashMap<>();
            Map<Integer, Integer> ventasPorProducto = new HashMap<>();

            File folder = new File(".");
            for (File file : folder.listFiles((d, name) -> name.startsWith("ventas_"))) {
                processSalesFile(file, ventasPorVendedor, ventasPorProducto);
            }

            createReporteVendedores(vendedores, ventasPorVendedor);
            createReporteProductos(productos, ventasPorProducto);
            System.out.println("Reportes generados exitosamente.");
        } catch (Exception e) {
            System.out.println("Error procesando archivos: " + e.getMessage());
        }
    }

    private static Map<Integer, String> loadProductos() throws IOException {
        Map<Integer, String> map = new HashMap<>();
        try (Scanner sc = new Scanner(new File("productos.txt"))) {
            while (sc.hasNextLine()) {
                String[] parts = sc.nextLine().split(";");
                map.put(Integer.parseInt(parts[0]), parts[1] + ";" + parts[2]);
            }
        }
        return map;
    }

    private static Map<Long, String> loadVendedores() throws IOException {
        Map<Long, String> map = new HashMap<>();
        try (Scanner sc = new Scanner(new File("vendedores.txt"))) {
            while (sc.hasNextLine()) {
                String[] parts = sc.nextLine().split(";");
                map.put(Long.parseLong(parts[1]), parts[2] + " " + parts[3]);
            }
        }
        return map;
    }

    private static void processSalesFile(File file, Map<Long, Integer> vtasVend, Map<Integer, Integer> vtasProd) throws IOException {
        try (Scanner sc = new Scanner(file)) {
            String header = sc.nextLine(); // CC;ID
            long vendedorId = Long.parseLong(header.split(";")[1]);
            while (sc.hasNextLine()) {
                String[] parts = sc.nextLine().split(";");
                int idProd = Integer.parseInt(parts[0]);
                int cant = Integer.parseInt(parts[1]);
                vtasVend.put(vendedorId, vtasVend.getOrDefault(vendedorId, 0) + cant);
                vtasProd.put(idProd, vtasProd.getOrDefault(idProd, 0) + cant);
            }
        }
    }

    private static void createReporteVendedores(Map<Long, String> vendedores, Map<Long, Integer> ventas) throws IOException {
        List<Map.Entry<Long, Integer>> lista = new ArrayList<>(ventas.entrySet());
        lista.sort((a, b) -> b.getValue() - a.getValue());
        try (PrintWriter pw = new PrintWriter(new FileWriter("reporte_vendedores.csv"))) {
            for (Map.Entry<Long, Integer> e : lista) {
                pw.println(vendedores.get(e.getKey()) + ";" + e.getValue());
            }
        }
    }

    private static void createReporteProductos(Map<Integer, String> productos, Map<Integer, Integer> ventas) throws IOException {
        List<Map.Entry<Integer, Integer>> lista = new ArrayList<>(ventas.entrySet());
        lista.sort((a, b) -> b.getValue() - a.getValue());
        try (PrintWriter pw = new PrintWriter(new FileWriter("reporte_productos.csv"))) {
            for (Map.Entry<Integer, Integer> e : lista) {
                pw.println(productos.get(e.getKey()) + ";" + e.getValue());
            }
        }
    }
}
