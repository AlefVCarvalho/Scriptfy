package com.example.scriptfy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ArquivoService {

    private static final Logger log = LoggerFactory.getLogger(ArquivoService.class);

    private static final List<String> EXTENSOES_AUDIO = List.of("mp3", "wav", "m4a", "aac");
    private static final List<String> EXTENSOES_IMAGEM = List.of("jpg", "jpeg", "png", "webp");

    private final Path pastaAudio;
    private final Path pastaImagens;

    public ArquivoService(@Value("${app.upload.dir}") String diretorioBase) {
        Path base = Path.of(diretorioBase).toAbsolutePath().normalize();
        this.pastaAudio = base.resolve("audio");
        this.pastaImagens = base.resolve("imagens");
        try {
            Files.createDirectories(pastaAudio);
            Files.createDirectories(pastaImagens);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível criar a pasta de uploads: " + base, e);
        }
        log.info("Pasta de uploads: {}", base);
    }

    // Salvar arquivos

    public String salvarAudio(MultipartFile arquivo) {
        return salvar(arquivo, pastaAudio, EXTENSOES_AUDIO, "áudio");
    }

    public String salvarImagem(MultipartFile arquivo) {
        return salvar(arquivo, pastaImagens, EXTENSOES_IMAGEM, "imagem");
    }

    // Carregar

    public Resource carregarAudio(String nome) {
        return carregar(pastaAudio, nome);
    }

    public Resource carregarImagem(String nome) {
        return carregar(pastaImagens, nome);
    }

    // Excluir

    public void excluirAudio(String nome) {
        excluir(pastaAudio, nome);
    }

    public void excluirImagem(String nome) {
        excluir(pastaImagens, nome);
    }

    // ---------- MÉTODOS AUXILIARES ----------

    private String salvar(MultipartFile arquivo, Path pasta, List<String> extensoesPermitidas, String tipo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Nenhum arquivo de " + tipo + " foi enviado");
        }

        String extensao = extrairExtensao(arquivo.getOriginalFilename());
        if (!extensoesPermitidas.contains(extensao)) {
            throw new IllegalArgumentException(
                    "Formato de " + tipo + " inválido. Use: " + String.join(", ", extensoesPermitidas));
        }

        String nomeSalvo = UUID.randomUUID() + "." + extensao;
        try {
            Files.copy(arquivo.getInputStream(), pasta.resolve(nomeSalvo), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao salvar o arquivo de " + tipo, e);
        }
        return nomeSalvo;
    }

    private Resource carregar(Path pasta, String nome) {
        Resource recurso = new FileSystemResource(resolverSeguro(pasta, nome));
        if (!recurso.exists() || !recurso.isReadable()) {
            throw new NoSuchElementException("Arquivo não encontrado no servidor");
        }
        return recurso;
    }

    private void excluir(Path pasta, String nome) {
        if (nome == null || nome.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(resolverSeguro(pasta, nome));
        } catch (IOException | IllegalArgumentException e) {
            log.warn("Não foi possível excluir o arquivo {}: {}", nome, e.getMessage());
        }
    }

    private Path resolverSeguro(Path pasta, String nome) {
        Path caminho = pasta.resolve(nome).normalize();
        if (!caminho.startsWith(pasta)) {
            throw new IllegalArgumentException("Nome de arquivo inválido");
        }
        return caminho;
    }

    private String extrairExtensao(String nomeOriginal) {
        if (nomeOriginal == null || !nomeOriginal.contains(".")) {
            return "";
        }
        return nomeOriginal.substring(nomeOriginal.lastIndexOf('.') + 1).toLowerCase();
    }
}