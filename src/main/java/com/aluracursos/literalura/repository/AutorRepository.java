package com.aluracursos.literalura.repository;

import com.aluracursos.literalura.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AutorRepository extends JpaRepository<Autor, Long> {
    @Query("SELECT a FROM Autor a WHERE :año BETWEEN a.anioDeNacimiento AND a.anioDeFallecimiento")
    List<Autor> filtrarAutorvivoporAño(@Param("año") int año);

    Optional<Autor> findByFechaNacimiento(int fechaNacimiento);
}
