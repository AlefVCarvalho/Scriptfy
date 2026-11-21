package com.example.scriptfy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "musicas")
public class ScriptfyModel {

    // Variaveis, restrições para limite, não estar em branco, getters e setters

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome da música não pode estar em branco")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "O álbum não pode estar em branco")
    @Size(max = 100, message = "O álbum deve ter no máximo 100 caracteres")
    @Column(nullable = false, length = 100)
    private String album;

    @NotBlank(message = "Os artistas não podem estar em branco")
    @Size(max = 100, message = "Os artistas devem ter no máximo 100 caracteres")
    @Column(nullable = false, length = 100)
    private String artistas;

    @NotNull(message = "O tempo é obrigatório")
    @Positive(message = "O tempo deve ser maior que 0 segundos")
    @Column(name = "tempo_segundos", nullable = false)
    private Integer tempoSegundos;

    @Column(name = "arquivo_audio")
    private String arquivoAudio;

    @Column(name = "imagem_capa")
    private String imagemCapa;

    public ScriptfyModel() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }

    public String getArtistas() { return artistas; }
    public void setArtistas(String artistas) { this.artistas = artistas; }

    public Integer getTempoSegundos() { return tempoSegundos; }
    public void setTempoSegundos(Integer tempoSegundos) { this.tempoSegundos = tempoSegundos; }

    public String getArquivoAudio() { return arquivoAudio; }
    public void setArquivoAudio(String arquivoAudio) { this.arquivoAudio = arquivoAudio; }

    public String getImagemCapa() { return imagemCapa; }
    public void setImagemCapa(String imagemCapa) { this.imagemCapa = imagemCapa; }
}