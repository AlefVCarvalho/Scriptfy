"use client";

// Essa é a tela do app inteira. O app só tem uma página, e o que aparece muda conforme o que a pessoa clica.

import { useEffect, useRef, useState } from "react";
import {
  musicasExemplo,
  listarMusicas,
  salvarMusica,
  excluirMusica,
  enviarAudio,
  urlAudio,
  formatarTempo,
  subtitulo,
  converterTempo,
  validarMusica,
  semAcento,
  agruparAlbuns,
  estiloCapa,
} from "./funcoes";

// Símbolos dos botões do player
const PLAY = "▶︎";
const PAUSE = "❚❚";
const ANTERIOR = "⏮︎";
const PROXIMA = "⏭︎";

export default function Home() {
  // Tudo que a tela precisa lembrar
  const [musicas, setMusicas] = useState([]);
  const [demo, setDemo] = useState(false); // true = backend desligado
  const [aba, setAba] = useState("musicas"); // "musicas" ou "albuns"
  const [busca, setBusca] = useState("");
  const [albumAberto, setAlbumAberto] = useState(null); // nome do álbum aberto
  const [formulario, setFormulario] = useState(null); // null = fechado, {} = nova, música = editar

  // Player: lista que está tocando, qual música é, se está tocando e em que segundo
  const [fila, setFila] = useState([]);
  const [indice, setIndice] = useState(0);
  const [tocando, setTocando] = useState(false);
  const [progresso, setProgresso] = useState(0); // segundos
  const audioRef = useRef(null);

  const atual = fila[indice];
  const temAudio = atual && atual.arquivoAudio;

  // Quando o app abre, pega as músicas do servidor. Se ele estiver desligado, usa as de teste.
  useEffect(() => {
    listarMusicas()
      .then((lista) => setMusicas(lista))
      .catch(() => {
        setMusicas(musicasExemplo);
        setDemo(true);
      });
  }, []);

  // Trocou de música: volta pro começo e carrega o áudio dela
  useEffect(() => {
    setProgresso(0);
    const audio = audioRef.current;
    if (!atual || !audio) return;
    if (temAudio) {
      audio.src = urlAudio(atual);
      if (tocando) audio.play().catch(() => {});
    } else {
      audio.pause();
    }
  }, [atual]);

  // Aperta play ou pause
  useEffect(() => {
    const audio = audioRef.current;
    if (!audio || !temAudio) return;
    if (tocando) audio.play().catch(() => {});
    else audio.pause();
  }, [tocando]);

  // Música sem áudio: a barrinha anda sozinha, como um cronômetro
  useEffect(() => {
    if (!tocando || !atual || temAudio) return;
    const cronometro = setInterval(() => setProgresso((p) => p + 1), 1000);
    return () => clearInterval(cronometro);
  }, [tocando, atual]);

  // Acabou a música: passa pra próxima
  useEffect(() => {
    if (atual && !temAudio && progresso >= atual.tempoSegundos) proxima();
  }, [progresso]);

  function tocar(lista, posicao) {
    setFila(lista);
    setIndice(posicao);
    setTocando(true);
  }

  function proxima() {
    setIndice((indice + 1) % fila.length);
    setProgresso(0);
  }

  function anterior() {
    if (progresso > 3) return buscar(0);
    setIndice((indice - 1 + fila.length) % fila.length);
  }

  function buscar(segundos) {
    setProgresso(segundos);
    if (audioRef.current && temAudio) audioRef.current.currentTime = segundos;
  }

  // Salvar uma música nova ou editada
  async function aoSalvar(dados, arquivo) {
    if (demo) {
      // Sem servidor: mexe só na lista que está na tela
      const audio = arquivo ? URL.createObjectURL(arquivo) : dados.arquivoAudio;
      if (dados.id) {
        setMusicas(musicas.map((m) => (m.id === dados.id ? { ...m, ...dados, arquivoAudio: audio } : m)));
      } else {
        setMusicas([{ ...dados, id: Date.now(), arquivoAudio: audio }, ...musicas]);
      }
    } else {
      let salva = await salvarMusica(dados);
      if (arquivo) {
        try {
          salva = await enviarAudio(salva.id, arquivo);
        } catch (erro) {
          // a música já foi salva, só o áudio que não foi
          alert("A música foi salva, mas o áudio não: " + erro.message);
        }
      }
      if (dados.id) setMusicas(musicas.map((m) => (m.id === salva.id ? salva : m)));
      else setMusicas([salva, ...musicas]);
    }
    setFormulario(null);
  }

  async function aoExcluir(musica) {
    if (!confirm(`Remover "${musica.nome}"?`)) return;
    try {
      if (!demo) await excluirMusica(musica.id);
    } catch (erro) {
      alert("Não foi possível remover: " + erro.message);
      return;
    }
    setMusicas(musicas.filter((m) => m.id !== musica.id));
    if (atual && atual.id === musica.id) {
      setFila([]);
      setTocando(false);
    }
  }

  // Lista já pronta pra mostrar (com a busca aplicada)
  const albuns = agruparAlbuns(musicas);
  const album = albuns.find((a) => a.nome === albumAberto);
  const termo = semAcento(busca);
  const musicasFiltradas = musicas.filter((m) => semAcento(`${m.nome} ${m.artistas} ${m.album}`).includes(termo));
  const albunsFiltrados = albuns.filter((a) => semAcento(`${a.nome} ${a.artistas}`).includes(termo));

  function irPara(novaAba) {
    setAba(novaAba);
    setAlbumAberto(null);
    setBusca("");
  }

  return (
    <div className="celular">
      <main className={"tela" + (atual ? " com-player" : "")}>
        <header className="topo">
          <img className="logo" src="/logo.png" alt="Scriptfy" />
        </header>

        {album ? (
          /* Tela de um álbum */
          <section>
            <button className="voltar" onClick={() => setAlbumAberto(null)}>
              ‹ Voltar
            </button>
            <div className="album-topo">
              <Capa nome={album.nome} tamanho="grande" />
              <h1>{album.nome}</h1>
              <p>{album.artistas}</p>
            </div>
            {album.faixas.map((m, i) => (
              <LinhaMusica
                key={m.id}
                musica={m}
                numero={i + 1}
                ativa={atual && atual.id === m.id}
                onTocar={() => tocar(album.faixas, i)}
                onEditar={() => setFormulario(m)}
                onExcluir={() => aoExcluir(m)}
              />
            ))}
          </section>
        ) : (
          /* Tela com a lista de músicas ou de álbuns */
          <section>
            <h1 className="titulo">{aba === "musicas" ? "Músicas" : "Álbuns"}</h1>
            <input
              className="busca"
              placeholder="Buscar..."
              value={busca}
              onChange={(e) => setBusca(e.target.value)}
            />

            {aba === "musicas" && (
              <>
                {musicasFiltradas.map((m, i) => (
                  <LinhaMusica
                    key={m.id}
                    musica={m}
                    ativa={atual && atual.id === m.id}
                    onTocar={() => tocar(musicasFiltradas, i)}
                    onEditar={() => setFormulario(m)}
                    onExcluir={() => aoExcluir(m)}
                  />
                ))}
                {musicasFiltradas.length === 0 && <p className="vazio">Nenhuma música encontrada.</p>}
              </>
            )}

            {aba === "albuns" && (
              <div className="grade">
                {albunsFiltrados.map((a) => (
                  <button key={a.nome} className="cartao-album" onClick={() => setAlbumAberto(a.nome)}>
                    <Capa nome={a.nome} tamanho="media" />
                    <strong>{a.nome}</strong>
                    <small>{a.artistas}</small>
                  </button>
                ))}
                {albunsFiltrados.length === 0 && <p className="vazio">Nenhum álbum encontrado.</p>}
              </div>
            )}
          </section>
        )}
      </main>

      {/* Player (só aparece quando tem música tocando) */}
      {atual && (
        <div className="player">
          <div className="player-linha">
            <Capa nome={atual.album} tamanho="pequena" />
            <span className="textos">
              <strong>{atual.nome}</strong>
              <small>{subtitulo(atual)}</small>
            </span>
            <button className="icone" onClick={anterior}>
              {ANTERIOR}
            </button>
            <button className="botao-play pequeno" onClick={() => setTocando(!tocando)}>
              {tocando ? PAUSE : PLAY}
            </button>
            <button className="icone" onClick={proxima}>
              {PROXIMA}
            </button>
          </div>
          <div className="player-barra">
            <span>{formatarTempo(progresso)}</span>
            <input
              type="range"
              min="0"
              max={atual.tempoSegundos}
              value={Math.min(progresso, atual.tempoSegundos)}
              onChange={(e) => buscar(Number(e.target.value))}
            />
            <span>{formatarTempo(atual.tempoSegundos)}</span>
          </div>
        </div>
      )}

      {/* Menu de baixo */}
      <nav className="menu">
        <button className={aba === "musicas" ? "ativo" : ""} onClick={() => irPara("musicas")}>
          Músicas
        </button>
        <button className="botao-adicionar" onClick={() => setFormulario({})} aria-label="Adicionar música">
          +
        </button>
        <button className={aba === "albuns" ? "ativo" : ""} onClick={() => irPara("albuns")}>
          Álbuns
        </button>
      </nav>

      {formulario && (
        <Formulario
          key={formulario.id || "nova"}
          inicial={formulario}
          albuns={albuns}
          onSalvar={aoSalvar}
          onFechar={() => setFormulario(null)}
        />
      )}

      <audio ref={audioRef} onTimeUpdate={(e) => setProgresso(e.target.currentTime)} onEnded={proxima} />
    </div>
  );
}

// Pedaços menores da tela

// A capa colorida do álbum
function Capa({ nome, tamanho }) {
  return <div className={"capa " + tamanho} style={estiloCapa(nome, tamanho !== "grande")} />;
}

// Uma música da lista (dentro do álbum mostra o número no lugar da capa)
function LinhaMusica({ musica, numero, ativa, onTocar, onEditar, onExcluir }) {
  return (
    <div className={"linha" + (ativa ? " ativa" : "")}>
      <button className="linha-info" onClick={onTocar}>
        {numero ? <span className="numero">{numero}</span> : <Capa nome={musica.album} tamanho="pequena" />}
        <span className="textos">
          <strong>{musica.nome}</strong>
          <small>{subtitulo(musica)}</small>
        </span>
        <span className="tempo">{formatarTempo(musica.tempoSegundos)}</span>
      </button>
      <button className="icone" onClick={onEditar} aria-label="Editar">
        ✎
      </button>
      <button className="icone" onClick={onExcluir} aria-label="Remover">
        ✕
      </button>
    </div>
  );
}

// Janela de adicionar ou editar música
function Formulario({ inicial, albuns, onSalvar, onFechar }) {
  const [nome, setNome] = useState(inicial.nome || "");
  const [artistas, setArtistas] = useState(inicial.artistas || "");
  const [album, setAlbum] = useState(inicial.album || "");
  const [tempo, setTempo] = useState(inicial.tempoSegundos ? formatarTempo(inicial.tempoSegundos) : "");
  const [arquivo, setArquivo] = useState(null);
  const [erro, setErro] = useState("");
  const [salvando, setSalvando] = useState(false);

  async function enviar(e) {
    e.preventDefault();
    const dados = {
      id: inicial.id,
      nome: nome.trim(),
      artistas: artistas.trim(),
      album: album.trim(),
      tempoSegundos: converterTempo(tempo),
      arquivoAudio: inicial.arquivoAudio,
    };
    const mensagem = validarMusica(dados);
    if (mensagem) return setErro(mensagem);

    setSalvando(true);
    try {
      await onSalvar(dados, arquivo);
    } catch (err) {
      setErro(err.message || "Não foi possível salvar");
      setSalvando(false);
    }
  }

  return (
    <div className="fundo-janela" onClick={onFechar}>
      <form className="janela" onClick={(e) => e.stopPropagation()} onSubmit={enviar}>
        <h2>{inicial.id ? "Editar música" : "Nova música"}</h2>

        <label>
          Nome
          <input value={nome} onChange={(e) => setNome(e.target.value)} autoFocus />
        </label>
        <label>
          Artistas
          <input value={artistas} onChange={(e) => setArtistas(e.target.value)} />
        </label>
        <div className="duas-colunas">
          <label>
            Álbum
            <input value={album} onChange={(e) => setAlbum(e.target.value)} list="lista-albuns" />
          </label>
          <label>
            Tempo
            <input value={tempo} onChange={(e) => setTempo(e.target.value)} placeholder="3:42" />
          </label>
        </div>
        <datalist id="lista-albuns">
          {albuns.map((a) => (
            <option key={a.nome} value={a.nome} />
          ))}
        </datalist>

        <label className="arquivo">
          {arquivo ? arquivo.name : "Anexar áudio (opcional)"}
          <input type="file" accept=".mp3,.wav,.m4a,.aac" hidden onChange={(e) => setArquivo(e.target.files[0] || null)} />
        </label>

        {erro && <p className="erro">{erro}</p>}

        <button className="botao-salvar" type="submit" disabled={salvando}>
          {salvando ? "Salvando..." : "Salvar"}
        </button>
      </form>
    </div>
  );
}
