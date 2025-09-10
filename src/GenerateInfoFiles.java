import java.io.*;
import java.util.*;

public class GenerateInfoFiles {
    private static Random rand = new Random();

    public static void main(String[] args) {
        try {
            createProductsFile(5);
            createSalesManInfoFile(3);
            createSalesMenFile(10, "Ana Perez", 101);
            createSalesMenFile(8, "Luis Gomez", 102);
            createSalesMenFile(12, "Carlos Ruiz", 103);
            System.out.println("Archivos generados exitosamente.");
        } catch (Exception e) {
            System.out.println("Error generando archivos: " + e.getMessage());
        }
    }

    public static void createProductsFile(int productsCount) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter("productos.txt"))) {
            for (int i = 1; i <= productsCount; i++) {
                pw.println(i + ";Producto" + i + ";" + (rand.nextInt(50) + 10));
            }
        }
    }

    public static void createSalesManInfoFile(int salesmanCount) throws IOException {
        String[] nombres = {"Ana", "Luis", "Carlos", "Maria", "Sofia"};
        String[] apellidos = {"Perez", "Gomez", "Ruiz", "Diaz", "Lopez"};

        try (PrintWriter pw = new PrintWriter(new FileWriter("vendedores.txt"))) {
            for (int i = 0; i < salesmanCount; i++) {
                String nombre = nombres[rand.nextInt(nombres.length)];
                String apellido = apellidos[rand.nextInt(apellidos.length)];
                long id = 100 + i;
                pw.println("CC;" + id + ";" + nombre + ";" + apellido);
            }
        }
    }

    public static void createSalesMenFile(int randomSalesCount, String name, long id) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter("ventas_" + id + ".txt"))) {
            pw.println("CC;" + id);
            for (int i = 0; i < randomSalesCount; i++) {
                int idProducto = rand.nextInt(5) + 1;
                int cantidad = rand.nextInt(10) + 1;
                pw.println(idProducto + ";" + cantidad);
            }
        }
    }
}
