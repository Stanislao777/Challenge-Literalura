package com.aluracursos.literalura.repository;

//import com.aluracursos.literalura.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

//public interface LibroRepository extends JpaRepository<Libro, Long> {
//    Optional<Libro> findByTitulo(String titulo);
//
//    List<Libro> findByIdioma(String idioma);
//
//    @Query("select l from Libro as l where l.titulo like %:titulo%")
//    Optional<Libro> filtrarPorTitulo(String titulo);
//
//    @Query("SELECT l FROM Libro as l order by l.numeroDescargas desc")
//    List<Libro> top10Libros();
//}
