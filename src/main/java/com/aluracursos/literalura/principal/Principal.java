package com.aluracursos.literalura.principal;

import com.aluracursos.literalura.model.Autor;
import com.aluracursos.literalura.model.DatosLibro;
import com.aluracursos.literalura.model.Libro;
import com.aluracursos.literalura.repository.AutorRepository;
import com.aluracursos.literalura.repository.LibroRepository;
import com.aluracursos.literalura.service.ConsumoAPI;
import com.aluracursos.literalura.service.ConvierteDatos;

import java.util.*;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private final String URL_BASE = "https://gutendex.com/books/?t=";
    private ConvierteDatos conversor = new ConvierteDatos();

    private List<DatosLibro> datosLibros = new ArrayList<>();

    private LibroRepository libroRepositorio;
    private AutorRepository autorRepositorio;

    public Principal(LibroRepository libroRepository, AutorRepository autorRepository) {
        this.libroRepositorio = libroRepository;
        this.autorRepositorio = autorRepository;
    }


    private List<Libro> libros;
    private List<Autor> autores;

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
                    System.out.println("Hola 2\n");
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

    private DatosLibro getDatosLibro() {
        System.out.println("Ingrese el nombre del libro que desea buscar");
        var nombreLibro = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + nombreLibro);
        System.out.println(json);
        DatosLibro datos = conversor.obtenerDatos(json, DatosLibro.class);
        return datos;
    }

    private void buscarLibro() {
        DatosLibro datos = getDatosLibro();
        datosLibros.add(datos);
        System.out.println(datos);
    }

    private void mostrarLibrosBuscados(){
        datosLibros.forEach(System.out::println);
    }
}
