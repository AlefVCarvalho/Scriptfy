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

// Aqui ficam as regras do app: o que pode e o que não pode fazer com as músicas
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

    // Lista as músicas, com busca e ordem
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

    // Cria música nova
    public ScriptfyModel cadastrar(ScriptfyModel musica, MultipartFile audio, MultipartFile imagem) {
        validar(musica);
        if (audio == null || audio.isEmpty()) {
            throw new IllegalArgumentException("O arquivo de áudio é obrigatório");
        }

        musica.setId(null);
        musica.setArquivoAudio(null);
        musica.setImagemCapa(null);

        String nomeAudio = null;
        String nomeImagem = null;
        try {
            nomeAudio = arquivoService.salvarAudio(audio);
            if (imagem != null && !imagem.isEmpty()) {
                nomeImagem = arquivoService.salvarImagem(imagem);
            }
            musica.setArquivoAudio(nomeAudio);
            musica.setImagemCapa(nomeImagem);
            return repository.save(musica);
        } catch (RuntimeException e) {
            // se algo falhar, não deixa arquivo solto no disco
            arquivoService.excluirAudio(nomeAudio);
            arquivoService.excluirImagem(nomeImagem);
            throw e;
        }
    }

    // Edita os dados; áudio e imagem só mudam se um arquivo novo for enviado
    public ScriptfyModel atualizar(Long id, ScriptfyModel dados, MultipartFile audio, MultipartFile imagem) {
        ScriptfyModel existente = buscarPorId(id);
        validar(dados);

        boolean temNovoAudio = audio != null && !audio.isEmpty();
        boolean temNovaImagem = imagem != null && !imagem.isEmpty();

        if (!temNovoAudio && existente.getArquivoAudio() == null) {
            throw new IllegalArgumentException("O arquivo de áudio é obrigatório");
        }

        String audioAntigo = existente.getArquivoAudio();
        String imagemAntiga = existente.getImagemCapa();
        String novoAudio = null;
        String novaImagem = null;

        existente.setNome(dados.getNome());
        existente.setAlbum(dados.getAlbum());
        existente.setArtistas(dados.getArtistas());
        existente.setTempoSegundos(dados.getTempoSegundos());

        ScriptfyModel salva;
        try {
            if (temNovoAudio) {
                novoAudio = arquivoService.salvarAudio(audio);
                existente.setArquivoAudio(novoAudio);
            }
            if (temNovaImagem) {
                novaImagem = arquivoService.salvarImagem(imagem);
                existente.setImagemCapa(novaImagem);
            }
            salva = repository.save(existente);
        } catch (RuntimeException e) {
            arquivoService.excluirAudio(novoAudio);
            arquivoService.excluirImagem(novaImagem);
            throw e;
        }

        if (novoAudio != null) {
            arquivoService.excluirAudio(audioAntigo);
        }
        if (novaImagem != null) {
            arquivoService.excluirImagem(imagemAntiga);
        }
        return salva;
    }

    // Apaga a música e também o áudio e a imagem dela
    public void excluir(Long id) {
        ScriptfyModel musica = buscarPorId(id);
        repository.delete(musica);
        arquivoService.excluirAudio(musica.getArquivoAudio());
        arquivoService.excluirImagem(musica.getImagemCapa());
    }


    public Resource carregarAudio(Long id) {
        ScriptfyModel musica = buscarPorId(id);
        if (musica.getArquivoAudio() == null) {
            throw new NoSuchElementException("Esta música não possui arquivo de áudio");
        }
        return arquivoService.carregarAudio(musica.getArquivoAudio());
    }

    public Resource carregarImagem(Long id) {
        ScriptfyModel musica = buscarPorId(id);
        if (musica.getImagemCapa() == null) {
            throw new NoSuchElementException("Esta música não possui imagem de capa");
        }
        return arquivoService.carregarImagem(musica.getImagemCapa());
    }

    // Confere se os dados da música estão certinhos
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

    // Escolhe por qual campo e em qual ordem listar
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