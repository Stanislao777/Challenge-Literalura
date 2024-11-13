package com.aluracursos.literalura.principal;

import com.aluracursos.literalura.service.ConsumoAPI;
import com.aluracursos.literalura.service.ConvierteDatos;

public class Principal {
    private static final String URL_BASE = "https://gutendex.com/books/ ";
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos  conversor = new ConvierteDatos();
    
    public void mostrarMenu(){
        var json = consumoAPI.obtenerDatos(URL_BASE);
        System.out.println(json);
    }
}
