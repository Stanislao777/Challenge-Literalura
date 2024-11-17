package com.aluracursos.literalura.model;

import jakarta.persistence.*;

@Entity
@Table(name = "libros")
public class Libro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String titulo;
    private String autor;
    private String idioma;
    private Integer numeroDescargas;


    public Libro(DatosLibro datosLibro) {
        this.titulo = datosLibro.titulo();
//        @ManyToOne
//        @JoinColumn(name = "autor_id")
        this.autor = datosLibro.autor();
        this.idioma = datosLibro.idioma();
        this.numeroDescargas = datosLibro.numeroDescargas();
    }

    public Libro() {}

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    public Integer getNumeroDescargas() {
        return numeroDescargas;
    }

    public void setNumeroDescargas(Integer numeroDescargas) {
        this.numeroDescargas = numeroDescargas;
    }
}
