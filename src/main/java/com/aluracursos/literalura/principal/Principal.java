package com.aluracursos.literalura.principal;

import com.aluracursos.literalura.model.*;
import com.aluracursos.literalura.repository.AutorRepository;
import com.aluracursos.literalura.repository.LibroRepository;
import com.aluracursos.literalura.service.ConsumoAPI;
import com.aluracursos.literalura.service.ConvierteDatos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class Principal {

    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private final String URL_BASE = "https://gutendex.com/books/?t=";
    private ConvierteDatos conversor = new ConvierteDatos();

    private LibroRepository repositorioLibro;
    private AutorRepository repositorioAutor;

    @Autowired
    public Principal(LibroRepository repositorioLibro, AutorRepository repositorioAutor) {
        this.repositorioLibro = repositorioLibro;
        this.repositorioAutor = repositorioAutor;
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

            // Intentamos leer una opción del menú
            try {
                opcion = teclado.nextInt();  // Intentamos leer un número
            } catch (InputMismatchException e) {
                // Si la entrada no es un número, mostramos un mensaje de error
                System.out.println("Opción inválida. Por favor ingrese un número.");
                teclado.nextLine();  // Limpiamos el buffer de la entrada incorrecta
                continue;  // Volvemos a mostrar el menú
            }

            switch (opcion) {
                case 1:
                    buscarLibro();
                    break;
                case 2:
                    ListarlibrosRegistrados();
                    break;
                case 3:
                    //ListarAutoresRegistrados();
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
        teclado.nextLine();  // Limpiar el buffer de la entrada
        var tituloLibro = teclado.nextLine().trim();

        // Si el libro no está en la base de datos, se hará la búsqueda en la API externa
        String json = consumoApi.obtenerDatos(URL_BASE + "?search=" + tituloLibro.replace(" ", "+"));
        Datos datos = conversor.obtenerDatos(json, Datos.class);

        if (datos != null && datos.resultados() != null && !datos.resultados().isEmpty()) {
            boolean libroGuardado = false;

            for (DatosLibro libroBuscado : datos.resultados()) {
                if (libroBuscado.titulo().toUpperCase().contains(tituloLibro.toUpperCase())) {

                    // Verifica si el libro ya está registrado en la base de datos
                    Optional<Libro> libroEnApiExistente = repositorioLibro.findByTitulo(libroBuscado.titulo());
                    if (libroEnApiExistente.isPresent()) {
                        // Si el libro ya está registrado, mostramos el mensaje y terminamos la búsqueda
                        System.out.println("No se puede registrar el mismo libro más de una vez.\n");
                        return;  // Salimos del método sin continuar con el flujo
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

                        // Aquí manejamos la relación con el autor
                        Autor autor = null;
                        for (DatosAutor datosAutor : libroBuscado.autor()) {
                            String nombreAutor = datosAutor.nombre();

                            // Buscar si el autor ya existe en la base de datos
                            autor = repositorioAutor.findByNombre(nombreAutor);
                            if (autor == null) {
                                // Si el autor no existe, lo creamos
                                autor = new Autor();
                                autor.setNombre(nombreAutor);

                                // Asignamos los años de nacimiento y fallecimiento del autor
                                Integer nacimiento = Integer.parseInt(datosAutor.fechaDeNacimiento());
                                Integer fallecimiento = datosAutor.fechaDeFallecimiento() != null ? Integer.parseInt(datosAutor.fechaDeFallecimiento()) : null;

                                autor.setAnioNacimiento(nacimiento);
                                if (fallecimiento != null) {
                                    autor.setAnioFallecimiento(fallecimiento);
                                }

                                repositorioAutor.save(autor);  // Guardamos el nuevo autor
                            }

                            // Asignamos el autor al libro
                            // El autor debe ser asignado en la entidad Libro como un solo autor
                            // Si un libro tiene solo un autor, usamos el método setAutor
                            Libro libro = new Libro(
                                    libroBuscado.titulo(),
                                    String.join(", ", libroBuscado.idiomas()),
                                    libroBuscado.numeroDeDescargas(),
                                    autor  // Asignamos el autor encontrado o creado
                            );

                            repositorioLibro.save(libro);  // Guardamos el libro en la base de datos
                            System.out.println("Libro guardado en la base de datos.\n");
                        }

                        libroGuardado = true; // Marca el libro como guardado
                    }
                }
            }

            if (!libroGuardado) {
                System.out.println("\nLibro no encontrado\n");
            }
        } else {
            System.out.println("No se encontraron resultados en la API de gutendex.\n");
        }
    }

    private void ListarlibrosRegistrados() {
        // Obtener todos los libros de la base de datos
        List<Libro> libros = repositorioLibro.findAll();

        // Recorrer los libros y mostrar los detalles en el formato deseado
        for (Libro libro : libros) {
            System.out.println("----- LIBRO -----");
            System.out.println("Título: " + libro.getTitulo());

            // Accede al autor del libro y muestra el nombre
            if (libro.getAutor() != null) {  // Asegúrate de que el autor no sea null
                System.out.println("Autor: " + libro.getAutor().getNombre());
            } else {
                System.out.println("Autor: Desconocido");
            }

            // Mostrar el idioma
            System.out.println("Idioma: " + libro.getIdioma());

            // Mostrar el número de descargas
            System.out.println("Número de descargas: " + libro.getNumeroDeDescargas());
            System.out.println("------------------\n");
        }
    }


}