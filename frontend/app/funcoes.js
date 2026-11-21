// Aqui ficam as funções que não são de tela: falar com o servidor e arrumar os dados.

// Músicas de teste. Só aparecem quando o servidor está desligado.
export const musicasExemplo = [
  { id: 1, nome: "Música 1", album: "Álbum 1", artistas: "Artista 1", tempoSegundos: 200 },
  { id: 2, nome: "Música 2", album: "Álbum 1", artistas: "Artista 1", tempoSegundos: 185 },
  { id: 3, nome: "Música 3", album: "Álbum 1", artistas: "Artista 1, Artista 2", tempoSegundos: 240 },
  { id: 4, nome: "Música 4", album: "Álbum 2", artistas: "Artista 2", tempoSegundos: 210 },
  { id: 5, nome: "Música 5", album: "Álbum 2", artistas: "Artista 2", tempoSegundos: 175 },
  { id: 6, nome: "Música 6", album: "Álbum 3", artistas: "Artista 3", tempoSegundos: 230 },
  { id: 7, nome: "Música 7", album: "Álbum 3", artistas: "Artista 3", tempoSegundos: 195 },
  { id: 8, nome: "Música 8", album: "Álbum 3", artistas: "Artista 3", tempoSegundos: 260 },
];

// Conversa com o servidor.
// O next.config.mjs pega o "/api" e manda o pedido para o backend.
const URL_API = "/api/musicas";

async function pedir(url, opcoes) {
  const resposta = await fetch(url, opcoes);
  if (!resposta.ok) {
    let mensagem = "Erro " + resposta.status;
    try {
      const corpo = await resposta.json();
      mensagem = corpo.mensagem || mensagem;
    } catch (e) {}
    throw new Error(mensagem);
  }
  return resposta.status === 204 ? null : resposta.json();
}

export function listarMusicas() {
  return pedir(URL_API);
}

// Se a música já tem id, edita. Se não tem, cria uma nova.
export function salvarMusica(musica) {
  const corpo = {
    nome: musica.nome,
    album: musica.album,
    artistas: musica.artistas,
    tempoSegundos: musica.tempoSegundos,
  };
  return pedir(musica.id ? `${URL_API}/${musica.id}` : URL_API, {
    method: musica.id ? "PUT" : "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(corpo),
  });
}

export function excluirMusica(id) {
  return pedir(`${URL_API}/${id}`, { method: "DELETE" });
}

export function enviarAudio(id, arquivo) {
  const formulario = new FormData();
  formulario.append("arquivo", arquivo);
  return pedir(`${URL_API}/${id}/audio`, { method: "POST", body: formulario });
}

// Endereço do áudio da música (ou null se ela não tem áudio)
export function urlAudio(musica) {
  if (!musica.arquivoAudio) return null;
  if (musica.arquivoAudio.startsWith("blob:")) return musica.arquivoAudio;
  return `${URL_API}/${musica.id}/audio`;
}

// Texto pequeno embaixo do nome: artista principal e álbum
export function subtitulo(musica) {
  const principal = musica.artistas.split(",")[0].trim();
  return musica.album ? `${principal} · ${musica.album}` : principal;
}

// Transforma segundos em minutos:segundos (222 vira 3:42)
export function formatarTempo(segundos) {
  const total = Math.floor(segundos);
  const minutos = Math.floor(total / 60);
  const resto = total % 60;
  return minutos + ":" + String(resto).padStart(2, "0");
}

// Faz o contrário: "3:42" vira 222. Se não der certo, devolve 0.
export function converterTempo(texto) {
  const partes = texto.trim().split(":");
  if (partes.length === 2 && partes.every((p) => /^\d+$/.test(p))) {
    return Number(partes[0]) * 60 + Number(partes[1]);
  }
  if (partes.length === 1 && /^\d+$/.test(partes[0])) {
    return Number(partes[0]);
  }
  return 0;
}

// Confere os campos antes de mandar pro servidor. Devolve o erro ou "".
export function validarMusica(musica) {
  const campos = [
    ["O nome", musica.nome],
    ["O álbum", musica.album],
    ["O artista", musica.artistas],
  ];
  for (const [rotulo, valor] of campos) {
    if (!valor) return rotulo + " não pode estar em branco";
    if (valor.length > 100) return rotulo + " deve ter no máximo 100 caracteres";
  }
  if (!(musica.tempoSegundos > 0)) return "Informe o tempo no formato m:ss (ex.: 3:42)";
  return "";
}

// Tira acento e maiúscula, pra busca achar mesmo escrevendo diferente
export function semAcento(texto) {
  return texto.normalize("NFD").replace(/\p{M}/gu, "").toLowerCase();
}

// Junta as músicas que são do mesmo álbum
export function agruparAlbuns(musicas) {
  const albuns = [];
  for (const musica of musicas) {
    let album = albuns.find((a) => a.nome === musica.album);
    if (!album) {
      album = { nome: musica.album, artistas: musica.artistas, faixas: [] };
      albuns.push(album);
    }
    album.faixas.push(musica);
  }
  return albuns;
}

// Cria as cores da capa a partir do nome do álbum
export function estiloCapa(nome, comBrilho = true) {
  let h = 0;
  for (const letra of nome) h = (h * 31 + letra.charCodeAt(0)) % 360;
  const h2 = (h + 60) % 360;
  return {
    background:
      `radial-gradient(circle at 30% 25%, hsl(${h} 95% 62%), transparent 70%),` +
      `radial-gradient(circle at 75% 80%, hsl(${h2} 90% 55%), transparent 70%),` +
      `hsl(${h} 40% 10%)`,
    boxShadow: comBrilho ? `0 12px 30px -12px hsl(${h} 95% 60% / 0.7)` : "none",
  };
}
