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

    private final LibroRepository libroRepository;
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private final String URL_BASE = "https://gutendex.com/books/?t=";
    private ConvierteDatos conversor = new ConvierteDatos();

    private LibroRepository repositorioLibro;
    private AutorRepository repositorioAutor;

    @Autowired
    public Principal(LibroRepository repositorioLibro, AutorRepository repositorioAutor, LibroRepository libroRepository) {
        this.repositorioLibro = repositorioLibro;
        this.repositorioAutor = repositorioAutor;
        this.libroRepository = libroRepository;
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
                teclado.nextLine();
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
                    ListarAutoresRegistrados();
                    break;
                case 4:
                    ListarAutoresVivosEnDeterminadoAnio();
                    break;
                case 5:
                    ListarLibrosPorIdioma();
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

    private void ListarAutoresRegistrados() {
        List<Autor> autores = repositorioAutor.findAll();

        for (Autor autor : autores) {
            System.out.println("\nNombre: " + autor.getNombre());
            if (autor.getAnioNacimiento() != null) {
                System.out.println("Fecha de nacimiento: " + autor.getAnioNacimiento());
            } else {
                System.out.println("Fecha de nacimiento: Desconocida \n");
            }
            if (autor.getAnioFallecimiento() != null) {
                System.out.println("Fecha de fallecimiento: " + autor.getAnioFallecimiento());
            } else {
                System.out.println("Fecha de fallecimiento: Desconocida \n");
            }

            // Obtener los libros del autor
            List<Libro> librosDelAutor = repositorioLibro.findByAutor(autor);  // Método que busca libros por autor
            // Si el autor tiene libros, los mostramos
            if (!librosDelAutor.isEmpty()) {
                // Creamos una lista con los títulos de los libros
                List<String> titulosLibros = librosDelAutor.stream()
                        .map(Libro::getTitulo)
                        .collect(Collectors.toList());

                System.out.println("Libros: " + titulosLibros + "\n");
            } else {
                System.out.println("Libros: [Ninguno] \n");
            }
        }
    }

    public void ListarAutoresVivosEnDeterminadoAnio() {
        final int anio = obtenerAnioValido(teclado);

        // Consultamos todos los autores registrados en la base de datos
        List<Autor> autoresVivos = repositorioAutor.findAll()
                .stream()
                // Filtramos los autores que están vivos en el año proporcionado
                .filter(autor -> esAutorVivoEnAnio(autor, anio))
                .collect(Collectors.toList());;

        // Verificamos si hay autores vivos en ese año y mostramos los resultados
        if (autoresVivos.isEmpty()) {
            System.out.println("No hay autores vivos en el año " + anio + ".");
        } else {
            System.out.println("\nEl autor(es) vivo(s) en el año " + anio + " son:");
            for (Autor autor : autoresVivos) {
                System.out.println(" Autor: " + autor.getNombre());
                System.out.println(" Fecha de nacimiento: " + autor.getAnioNacimiento());
                if (autor.getAnioFallecimiento() != null) {
                    System.out.println(" Fecha de fallecimiento: " + autor.getAnioFallecimiento());
                } else {
                    System.out.println(" Fecha de fallecimiento: Desconocida (Aún vivo)");
                }

                // Mostrar los libros de ese autor(es)
                List<Libro> librosDelAutor = libroRepository.findByAutorId(autor.getId());
                if (librosDelAutor.isEmpty()) {
                    System.out.println("Libros: Ninguno");
                } else {
                    System.out.println(" Libros: ");
                    for (Libro libro : librosDelAutor) {
                        System.out.println(" - " + libro.getTitulo() + "\n");
                    }
                }
            }
        }
    }

    private int obtenerAnioValido(Scanner teclado) {
        String input;
        boolean esValido = false;
        int anio = -1;

        while (!esValido) {
            System.out.print("\nIngrese el año vivo de autor(es) que desea buscar: \n");
            input = teclado.nextLine();
            System.out.println("El año ingresado es: " + input);

            // Verificar si el input tiene exactamente 4 dígitos numéricos
            if (input.matches("\\d{4}")) {
                anio = Integer.parseInt(input); // Convertimos el String a un número
                esValido = true; // Año válido, salimos del ciclo
            } else {
                System.out.println("Por favor ingrese un año válido con exactamente 4 dígitos numéricos.");
            }
        }
        return anio;
    }

    // Método auxiliar para determinar si un autor está vivo en un determinado año
    private boolean esAutorVivoEnAnio(Autor autor, int anio) {
        /*El autor está vivo si su año de nacimiento es antes o igual al año
        y su año de fallecimiento (si existe) es mayor o igual al año.*/
        return autor.getAnioNacimiento() <= anio &&
                (autor.getAnioFallecimiento() == null || autor.getAnioFallecimiento() >= anio);
    }


    private void ListarLibrosPorIdioma() {
        System.out.print("""
        Ingrese el idioma para buscar los libros (por ejemplo, 'es' para español):
        es - español
        en - inglés
        fr - francés
        pt - portugués
        """);
        String idioma = teclado.nextLine().trim();

        // Traducir la abreviatura del idioma a su nombre completo
        String nombreIdioma;
        switch(idioma) {
            case "es":
                nombreIdioma = "Español";
                break;
            case "en":
                nombreIdioma = "Inglés";
                break;
            case "fr":
                nombreIdioma = "Francés";
                break;
            case "pt":
                nombreIdioma = "Portugués";
                break;
            default:
                nombreIdioma = "Idioma desconocido";
                break;
        }

        // Consultar todos los libros que tienen el idioma especificado
        List<Libro> librosPorIdioma = libroRepository.findByIdioma(idioma);

        // Verificar si hay libros para ese idioma
        if (librosPorIdioma.isEmpty()) {
            System.out.println("No se encontraron libros en el idioma: " + nombreIdioma + "\n");
        } else {
            System.out.println("----- LIBROS EN " + nombreIdioma.toUpperCase() + " -----");
            for (Libro libro : librosPorIdioma) {
                System.out.println("Título: " + libro.getTitulo());
                System.out.println("Autor: " + libro.getAutor().getNombre());
                System.out.println("Idioma: " + libro.getIdioma());
                System.out.println("Número de descargas: " + libro.getNumeroDeDescargas());
                System.out.println("-------------------\n");
            }
        }
    }
}