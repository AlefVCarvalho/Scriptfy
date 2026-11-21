package com.example.scriptfy.service;

import com.example.scriptfy.model.ScriptfyModel;
import com.example.scriptfy.repository.ScriptfyRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ScriptfyService {

    private static final List<String> CAMPOS_ORDENAVEIS =
            List.of("id", "nome", "album", "artistas", "tempoSegundos");

    private final ScriptfyRepository repository;
    private final Validator validator;
    private final ArquivoService arquivoService;

    @Autowired
    public ScriptfyService(ScriptfyRepository repository, Validator validator, ArquivoService arquivoService) {
        this.repository = repository;
        this.validator = validator;
        this.arquivoService = arquivoService;
    }

    // Listagem, pesquisa e ordenação

    public List<ScriptfyModel> listar(String busca, String campo, String ordenarPor, String direcao) {
        Sort ordenacao = montarOrdenacao(ordenarPor, direcao);

        if (busca == null || busca.isBlank()) {
            return repository.findAll(ordenacao);
        }

        String termo = busca.trim();
        String campoBusca = (campo == null || campo.isBlank()) ? "nome" : campo.toLowerCase();

        return switch (campoBusca) {
            case "nome" -> repository.findByNomeContainingIgnoreCase(termo, ordenacao);
            case "album" -> repository.findByAlbumContainingIgnoreCase(termo, ordenacao);
            case "artistas" -> repository.findByArtistasContainingIgnoreCase(termo, ordenacao);
            default -> throw new IllegalArgumentException(
                    "Campo de pesquisa inválido: " + campo + ". Use nome, album ou artistas.");
        };
    }

    public ScriptfyModel buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Música não encontrada com id: " + id));
    }

    // Cadastro

    public ScriptfyModel cadastrar(ScriptfyModel musica) {
        validar(musica);
        musica.setId(null);
        musica.setArquivoAudio(null);
        musica.setImagemCapa(null);
        return repository.save(musica);
    }

    // Atualizar

    public ScriptfyModel atualizar(Long id, ScriptfyModel dados) {
        ScriptfyModel existente = buscarPorId(id);
        validar(dados);

        existente.setNome(dados.getNome());
        existente.setAlbum(dados.getAlbum());
        existente.setArtistas(dados.getArtistas());
        existente.setTempoSegundos(dados.getTempoSegundos());

        return repository.save(existente);
    }

    // Excluir

    public void excluir(Long id) {
        ScriptfyModel musica = buscarPorId(id);
        repository.delete(musica);
        arquivoService.excluirAudio(musica.getArquivoAudio());
        arquivoService.excluirImagem(musica.getImagemCapa());
    }

    // Audio

    public ScriptfyModel salvarAudio(Long id, MultipartFile arquivo) {
        ScriptfyModel musica = buscarPorId(id);
        String novoArquivo = arquivoService.salvarAudio(arquivo);
        String arquivoAntigo = musica.getArquivoAudio();

        musica.setArquivoAudio(novoArquivo);
        ScriptfyModel salva;
        try {
            salva = repository.save(musica);
        } catch (RuntimeException e) {
            arquivoService.excluirAudio(novoArquivo);
            throw e;
        }

        arquivoService.excluirAudio(arquivoAntigo);
        return salva;
    }

    public Resource carregarAudio(Long id) {
        ScriptfyModel musica = buscarPorId(id);
        if (musica.getArquivoAudio() == null) {
            throw new NoSuchElementException("Esta música não possui arquivo de áudio");
        }
        return arquivoService.carregarAudio(musica.getArquivoAudio());
    }

    // Imagem

    public ScriptfyModel salvarImagem(Long id, MultipartFile arquivo) {
        ScriptfyModel musica = buscarPorId(id);
        String novoArquivo = arquivoService.salvarImagem(arquivo);
        String arquivoAntigo = musica.getImagemCapa();

        musica.setImagemCapa(novoArquivo);
        ScriptfyModel salva;
        try {
            salva = repository.save(musica);
        } catch (RuntimeException e) {
            arquivoService.excluirImagem(novoArquivo);
            throw e;
        }

        arquivoService.excluirImagem(arquivoAntigo);
        return salva;
    }

    public Resource carregarImagem(Long id) {
        ScriptfyModel musica = buscarPorId(id);
        if (musica.getImagemCapa() == null) {
            throw new NoSuchElementException("Esta música não possui imagem de capa");
        }
        return arquivoService.carregarImagem(musica.getImagemCapa());
    }

    // Funções auxiliares

    private void validar(ScriptfyModel musica) {
        if (musica == null) {
            throw new IllegalArgumentException("Os dados da música são obrigatórios");
        }

        Set<ConstraintViolation<ScriptfyModel>> erros = validator.validate(musica);
        if (!erros.isEmpty()) {
            String mensagem = erros.stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining("; "));
            throw new IllegalArgumentException(mensagem);
        }
    }

    private Sort montarOrdenacao(String ordenarPor, String direcao) {
        String campo = (ordenarPor == null || ordenarPor.isBlank()) ? "nome" : ordenarPor;
        if (!CAMPOS_ORDENAVEIS.contains(campo)) {
            throw new IllegalArgumentException(
                    "Campo de ordenação inválido: " + ordenarPor
                            + ". Use: " + String.join(", ", CAMPOS_ORDENAVEIS));
        }

        Sort.Direction sentido;
        if (direcao == null || direcao.isBlank() || direcao.equalsIgnoreCase("asc")) {
            sentido = Sort.Direction.ASC;
        } else if (direcao.equalsIgnoreCase("desc")) {
            sentido = Sort.Direction.DESC;
        } else {
            throw new IllegalArgumentException("Direção inválida: " + direcao + ". Use asc ou desc.");
        }

        return Sort.by(sentido, campo);
    }
}