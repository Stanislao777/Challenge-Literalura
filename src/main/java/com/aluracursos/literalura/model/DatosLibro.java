package com.aluracursos.literalura.model;


import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DatosLibro (
        @JsonAlias("title") String titulo,
        @JsonAlias("authors") String autor,
        @JsonAlias("languages") String idioma,
        @JsonAlias("download_count") Integer numeroDescargas){
}
