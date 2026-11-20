package com.example.scriptfy.service;

import com.example.scriptfy.model.ScriptfyModel;
import com.example.scriptfy.repository.ScriptfyRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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

    @Autowired
    public ScriptfyService(ScriptfyRepository repository, Validator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    // Funções de Listar e Buscar

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

    // Função para Cadastrar

    public ScriptfyModel cadastrar(ScriptfyModel musica) {
        validar(musica);
        musica.setId(null);
        musica.setArquivoAudio(null);
        return repository.save(musica);
    }

    // Função para Atualizar alguma Música

    public ScriptfyModel atualizar(Long id, ScriptfyModel dados) {
        ScriptfyModel existente = buscarPorId(id);
        validar(dados);

        existente.setNome(dados.getNome());
        existente.setAlbum(dados.getAlbum());
        existente.setArtistas(dados.getArtistas());
        existente.setTempoSegundos(dados.getTempoSegundos());
        // arquivoAudio não muda aqui: continua o que já estava salvo

        return repository.save(existente);
    }

    // Função de excluir

    public void excluir(Long id) {
        ScriptfyModel musica = buscarPorId(id);
        repository.delete(musica);
    }

    // Funções auxiliares para validar erros e realizar a ordenação

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