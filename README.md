# Scriptfy

Aplicativo de músicas para celular, feito como projeto da faculdade (Laboratório de Programação).
Com ele dá para **cadastrar, editar, apagar e ouvir músicas**, ver os álbuns e procurar o que quiser.

---

## O que o app faz

- **Lista de músicas** com nome, artista, álbum e duração
- **Busca** por nome, artista ou álbum
- **Álbuns**: o app junta automaticamente as músicas que têm o mesmo álbum
- **Player** com play/pause, música anterior e próxima, e barrinha de tempo
- **Adicionar, editar e apagar** músicas
- **Enviar o arquivo de áudio** da música (formatos mp3, wav, m4a e aac, até 30 MB)

---

## Como o projeto é dividido

O Scriptfy tem duas partes que conversam entre si:

| Parte | O que é | Pasta |
|---|---|---|
| **Frontend** | A parte que você vê e usa (as telas) | `frontend/` |
| **Backend** | O "cérebro": guarda as músicas no banco de dados e os arquivos de áudio | `backend/` |

Para o app funcionar completo, **as duas partes precisam estar ligadas ao mesmo tempo**, cada uma no seu terminal.

---

## O que instalar antes

Confira no terminal se você já tem tudo (cada comando deve mostrar uma versão):

| Programa | Para que serve | Como conferir |
|---|---|---|
| **Java 25** | Rodar o backend | `java -version` |
| **MySQL** | Guardar as músicas | `mysql --version` |
| **Node.js 18 ou mais novo** | Rodar o frontend | `node -v` |

---

## Passo a passo para ligar o app

### Passo 1 — Criar o banco de dados

1. Abra o MySQL (pode ser o MySQL Workbench).
2. Abra o arquivo `backend/sql/scriptfy.sql` e execute tudo.
   Isso cria o banco `scriptfy` e a tabela de músicas.

> **Atenção:** se você já tinha criado o banco antes de uma versão antiga, ele pode estar sem a coluna da imagem de capa.
> Para consertar, rode no MySQL: `ALTER TABLE musicas ADD COLUMN imagem_capa VARCHAR(255);`

### Passo 2 — Colocar a senha do banco

1. Entre na pasta `backend/src/main/resources/`.
2. Copie o arquivo `application-local.properties.example` e dê à cópia o nome `application-local.properties`.
3. Abra a cópia e preencha o usuário e a senha do seu MySQL:

```
spring.datasource.username=root
spring.datasource.password=SUA_SENHA
```

Esse arquivo é só seu e não vai para o GitHub.

### Passo 3 — Ligar o backend

Abra um terminal na pasta `backend` e rode:

- **Windows:** `.\mvnw spring-boot:run`
- **Mac / Linux:** `./mvnw spring-boot:run`

Na primeira vez demora um pouco, porque ele baixa o que precisa.
Quando aparecer uma mensagem com **"Started ScriptfyApplication"**, está pronto.

**Deixe esse terminal aberto.**

Para conferir, abra no navegador: http://localhost:8080/musicas
Se aparecer `[]` (ou uma lista de músicas), o backend está funcionando.

### Passo 4 — Ligar o frontend

Abra **outro** terminal, na pasta `frontend`, e rode:

```
npm install
npm run dev
```

(O `npm install` só é necessário na primeira vez.)
Quando aparecer **"Ready"**, abra no navegador: **http://localhost:3000**

### Passo 5 — Ver como celular

No navegador (Chrome ou Edge), aperte **F12** e depois **Ctrl + Shift + M**.
Escolha um celular na lista (por exemplo, iPhone 12 Pro) e o app aparece do tamanho certo.

**Para ver no celular de verdade:** conecte o celular no **mesmo Wi-Fi** do computador e abra `http://IP_DO_COMPUTADOR:3000`.
Para descobrir o IP, use `ipconfig` (Windows) ou `ifconfig` (Mac/Linux). Se não abrir, o firewall do computador pode estar bloqueando.

### Para desligar

Em cada terminal, aperte **Ctrl + C**.

---

## Roteiro para a apresentação

Uma sugestão de ordem para mostrar o app:

1. **Explicar as duas partes** (frontend e backend) e mostrar que o backend responde em http://localhost:8080/musicas.
2. **Abrir o app** e mostrar a tela de músicas.
3. **Adicionar uma música** pelo botão **+** (mostrar também o aviso quando um campo fica em branco).
4. **Anexar um áudio** (mp3) e **tocar** a música, mostrando o player e a barra de tempo.
5. **Editar** a música com o lápis e **apagar** com o **X**.
6. **Buscar** uma música pela caixa de busca.
7. Abrir a aba **Álbuns** e entrar em um álbum.
8. **Recarregar a página** (F5) para mostrar que tudo continua lá, porque está salvo no banco.

---

## Se algo der errado

| O que aconteceu | O que fazer |
|---|---|
| O app abre com "Música 1", "Música 2"... | O frontend não achou o backend. Confira se o backend está ligado (Passo 3) e se http://localhost:8080/musicas abre. Essas músicas são só de teste e não são salvas. |
| O backend dá erro de acesso ao banco | Confira o usuário e a senha no `application-local.properties` e se o MySQL está ligado. |
| O backend dá erro de coluna `imagem_capa` | Rode o comando do aviso do Passo 1. |
| "Port 8080 was already in use" | Alguma coisa já está usando essa porta. Feche-a ou reinicie o computador. |
| Erro de versão do Java | Rode `java -version`. O projeto precisa do Java 25. |
| A música não toca | Veja se o arquivo é mp3, wav, m4a ou aac e se tem até 30 MB. |
| Aparece um aviso vermelho no navegador sobre "hydration" | É causado por alguma extensão do navegador. O app funciona normalmente. Se incomodar, abra numa janela anônima. |

---

## O que tem em cada pasta

```
Scriptfy/
├── backend/                   O servidor
│   ├── sql/scriptfy.sql       Cria o banco de dados
│   └── src/main/java/...      O código do servidor
│       ├── controller/        Recebe os pedidos do app
│       ├── service/           Tem as regras (o que pode e o que não pode)
│       ├── repository/        Conversa com o banco de dados
│       └── model/             Como é uma "música"
│
└── frontend/                  O app
    ├── app/page.js            As telas
    ├── app/funcoes.js         Funções de apoio (falar com o servidor, tempo, busca...)
    ├── app/style.css          As cores e o visual
    ├── app/layout.js          A base da página
    ├── next.config.mjs        Liga o app ao servidor
    └── public/logo.png        A logo
```

---

## Tecnologias usadas

- **Frontend:** Next.js (React), CSS
- **Backend:** Java, Spring Boot
- **Banco de dados:** MySQL

---

## Possíveis melhorias

- Usar a imagem de capa enviada pelo usuário (o servidor já aceita, mas o app por enquanto usa capas coloridas geradas pelo nome do álbum)
- Criar playlists
- Cadastro de usuários
