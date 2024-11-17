package com.aluracursos.literalura.model;

import jakarta.persistence.Entity;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "autores")
public class Autor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private Integer anioDeNacimiento;
    private Integer anioDeFallecimiento;

    @OneToMany(mappedBy = "autor", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Libro> libros;
    public Autor() {}

    public Autor(DatosAutor datosAutor) {
        this.nombre = datosAutor.nombre();
        this.anioDeNacimiento = datosAutor.añoNacimiento();
        this.anioDeFallecimiento = datosAutor.añoFallecimiento();
    }

    public List<Libro> getLibros() {
        return libros;
    }

    public void setLibros(List<Libro> libros) {
        this.libros = libros;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getAnioDeNacimiento() {
        return anioDeNacimiento;
    }

    public void setAnioDeNacimiento(Integer anioDeNacimiento) {
        this.anioDeNacimiento = anioDeNacimiento;
    }

    public Integer getAnioDeFallecimiento() {
        return anioDeFallecimiento;
    }

    public void setAnioDeFallecimiento(Integer anioDeFallecimiento) {
        this.anioDeFallecimiento = anioDeFallecimiento;
    }
}
