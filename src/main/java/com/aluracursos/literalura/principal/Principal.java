package com.aluracursos.literalura.principal;

import com.aluracursos.literalura.model.Datos;
import com.aluracursos.literalura.model.DatosAutor;
import com.aluracursos.literalura.model.DatosLibro;
import com.aluracursos.literalura.service.ConsumoAPI;
import com.aluracursos.literalura.service.ConvierteDatos;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private final String URL_BASE = "https://gutendex.com/books/?t=";
    private ConvierteDatos conversor = new ConvierteDatos();
    private List<DatosLibro> datosLibros = new ArrayList<>();



    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    =================
                    Elija la opción a través de su número:
                    1 - Buscar libro por título 
                    2 - Listar libros registrados
                    3 - Listar autores registrados
                    4 - Listar autores vivos en un determinado año
                    5 - Listar libros por idioma
                    0 - Salir
                    -----------------
                    """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    buscarLibro();
                    break;
                case 2:
                    //ListarlibrosRegistrados();
                    break;
                case 3:
                    System.out.println("Hola 3\n");
                    break;
                case 4:
                    System.out.println("Hola 4\n");
                    break;
                case 5:
                    System.out.println("Hola 5\n");
                    break;

                case 0:
                    System.out.println("Cerrando la aplicación...\n");
                    break;
                default:
                    System.out.println("Opción inválida\n");
            }
        }
    }

    private void buscarLibro() {
        System.out.println("Ingrese el nombre del libro que desea buscar:");
        var tituloLibro = teclado.nextLine();

        String json = consumoApi.obtenerDatos(URL_BASE + "?search=" + tituloLibro.replace(" ", "+"));
        Datos datos = conversor.obtenerDatos(json, Datos.class);

        if (datos != null && datos.resultados() != null && !datos.resultados().isEmpty()) {
            boolean libroEncontrado = false;

            for (DatosLibro libroBuscado : datos.resultados()) {
                if (libroBuscado.titulo().toUpperCase().contains(tituloLibro.toUpperCase())) {
                    libroEncontrado = true;
                    System.out.println("----- LIBRO -----");
                    System.out.println("Título: " + libroBuscado.titulo());
                    System.out.println("Autor: " + libroBuscado.autor().stream()
                                                .map(DatosAutor::nombre)
                                                .collect(Collectors.joining(", ")));
                    System.out.println("Idioma: " + String.join(", ", libroBuscado.idiomas()));
                    System.out.println("Número de descargas: " + libroBuscado.numeroDeDescargas());
                    System.out.println("------------------\n");
                }
            }

            if (!libroEncontrado) {
                System.out.println("Libro no encontrado\n");
            }
        } else {
            System.out.println("Libro no encontrado\n");
        }
    }
}
