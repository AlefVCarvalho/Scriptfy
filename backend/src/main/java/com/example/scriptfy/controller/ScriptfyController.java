package com.example.scriptfy.controller;

import com.example.scriptfy.model.ScriptfyModel;
import com.example.scriptfy.service.ScriptfyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/musicas")
public class ScriptfyController {

    private final ScriptfyService service;

    @Autowired
    public ScriptfyController(ScriptfyService service) {
        this.service = service;
    }

    // Função get para listar as músicas 
    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(name = "busca", required = false) String busca,
            @RequestParam(name = "campo", required = false) String campo,
            @RequestParam(name = "ordenarPor", required = false) String ordenarPor,
            @RequestParam(name = "direcao", required = false) String direcao) {
        try {
            return ResponseEntity.ok(service.listar(busca, campo, ordenarPor, direcao));
        } catch (IllegalArgumentException e) {
            return erro(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // Função get para buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(service.buscarPorId(id));
        } catch (NoSuchElementException e) {
            return erro(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // Função post para cadastrar músicas
    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody ScriptfyModel musica) {
        try {
            ScriptfyModel salva = service.cadastrar(musica);
            return ResponseEntity.status(HttpStatus.CREATED).body(salva);
        } catch (IllegalArgumentException e) {
            return erro(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // Função put para atualizar músicas
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable("id") Long id,
                                       @RequestBody ScriptfyModel dados) {
        try {
            return ResponseEntity.ok(service.atualizar(id, dados));
        } catch (NoSuchElementException e) {
            return erro(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            return erro(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // Função delete para excluir músicas
    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable("id") Long id) {
        try {
            service.excluir(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return erro(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    private ResponseEntity<Map<String, String>> erro(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(Map.of("mensagem", mensagem));
    }
}