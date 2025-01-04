package com.aluracursos.literalura.principal;

import com.aluracursos.literalura.model.Datos;
import com.aluracursos.literalura.model.DatosAutor;
import com.aluracursos.literalura.model.DatosLibro;
import com.aluracursos.literalura.model.Libro;
import com.aluracursos.literalura.repository.LibroRepository;
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

    private LibroRepository repositorio;

    public Principal(LibroRepository repository) {
        this.repositorio = repository;
    }


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
        var tituloLibro = teclado.nextLine().trim();

        // Si el libro no está en la base de datos, se hará la búsqueda en la API externa
        String json = consumoApi.obtenerDatos(URL_BASE + "?search=" + tituloLibro.replace(" ", "+"));
        Datos datos = conversor.obtenerDatos(json, Datos.class);

        if (datos != null && datos.resultados() != null && !datos.resultados().isEmpty()) {
            boolean libroGuardado = false;

            for (DatosLibro libroBuscado : datos.resultados()) {
                if (libroBuscado.titulo().toUpperCase().contains(tituloLibro.toUpperCase())) {

                    // Verifica si el libro ya está registrado en la base de datos
                    Optional<Libro> libroEnApiExistente = repositorio.findByTitulo(libroBuscado.titulo());
                    if (libroEnApiExistente.isPresent()) {
                        // Si el libro ya está registrado, mostrará el siguiente mensaje
                        System.out.println("No se puede registrar el mismo libro más de una vez.\n");
                        return; // Sale del método si el libro ya está registrado
                    }

                    // Muestra los detalles del libro si no ha sido guardado aún
                    if (!libroGuardado) {
                        System.out.println("----- LIBRO -----");
                        System.out.println("Título: " + libroBuscado.titulo());
                        System.out.println("Autor: " + libroBuscado.autor().stream()
                                .map(DatosAutor::nombre)
                                .collect(Collectors.joining(", ")));
                        System.out.println("Idioma: " + String.join(", ", libroBuscado.idiomas()));
                        System.out.println("Número de descargas: " + libroBuscado.numeroDeDescargas());
                        System.out.println("------------------\n");

                        // Crea el objeto Libro y lo guardar en la BD
                        Libro libro = new Libro(
                                libroBuscado.titulo(),
                                libroBuscado.autor().stream().map(DatosAutor::nombre).collect(Collectors.joining(", ")),
                                String.join(", ", libroBuscado.idiomas()),
                                libroBuscado.numeroDeDescargas()
                        );

                        // Guardar el libro en la base de datos
                        repositorio.save(libro);
                        System.out.println("Libro guardado en la base de datos.\n");
                        libroGuardado = true; // Marca el libro como guardado
                    }
                }
            }

            if (!libroGuardado) {
                System.out.println("\nLibro no encontrado\n");
            }
        } else {
            System.out.println("No se encontraron resultados en la API externa.\n");
        }
    }
}
