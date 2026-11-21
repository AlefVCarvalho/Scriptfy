package com.example.scriptfy.controller;

import com.example.scriptfy.model.ScriptfyModel;
import com.example.scriptfy.service.ScriptfyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.NoSuchElementException;

// Aqui ficam os "endereços" que o app usa para conversar com o servidor
@RestController
@RequestMapping("/musicas")
public class ScriptfyController {
    private final ScriptfyService service;

    @Autowired
    public ScriptfyController(ScriptfyService service) {
        this.service = service;
    }

    // Mostra as músicas (dá pra buscar e escolher a ordem)
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

    // Mostra uma música só
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(service.buscarPorId(id));
        } catch (NoSuchElementException e) {
            return erro(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // Salva uma música nova
    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody ScriptfyModel musica) {
        try {
            ScriptfyModel salva = service.cadastrar(musica);
            return ResponseEntity.status(HttpStatus.CREATED).body(salva);
        } catch (IllegalArgumentException e) {
            return erro(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // Muda os dados de uma música
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

    // Apaga uma música
    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable("id") Long id) {
        try {
            service.excluir(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return erro(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // Recebe o arquivo de som da música
    @PostMapping(value = "/{id}/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> enviarAudio(@PathVariable("id") Long id,
                                         @RequestParam(name = "arquivo", required = false) MultipartFile arquivo) {
        try {
            return ResponseEntity.ok(service.salvarAudio(id, arquivo));
        } catch (NoSuchElementException e) {
            return erro(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            return erro(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // Entrega o som pra tocar
    @GetMapping("/{id}/audio")
    public ResponseEntity<?> tocarAudio(@PathVariable("id") Long id) {
        try {
            return respostaArquivo(service.carregarAudio(id));
        } catch (NoSuchElementException e) {
            return erro(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // Recebe a imagem da capa
    @PostMapping(value = "/{id}/imagem", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> enviarImagem(@PathVariable("id") Long id,
                                          @RequestParam(name = "arquivo", required = false) MultipartFile arquivo) {
        try {
            return ResponseEntity.ok(service.salvarImagem(id, arquivo));
        } catch (NoSuchElementException e) {
            return erro(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            return erro(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // Entrega a imagem da capa
    @GetMapping("/{id}/imagem")
    public ResponseEntity<?> verImagem(@PathVariable("id") Long id) {
        try {
            return respostaArquivo(service.carregarImagem(id));
        } catch (NoSuchElementException e) {
            return erro(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // Os dois métodos abaixo só ajudam a montar as respostas
    private ResponseEntity<Resource> respostaArquivo(Resource recurso) {
        MediaType tipo = MediaTypeFactory.getMediaType(recurso)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok().contentType(tipo).body(recurso);
    }

    private ResponseEntity<Map<String, String>> erro(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(Map.of("mensagem", mensagem));
    }
}