# <h1 align="center"> Tetris Retro em JavaFX 🕹️ </h1>

<p align="center">
  <a href="#🎯-objetivo-e-funcionalidades">Objetivo e Funcionalidades</a>&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;
  <a href="#📋-estrutura-do-projeto">Estrutura do Projeto</a>&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;
  <a href="#💾-persistência-e-replay">Persistência e Replay</a>&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;
  <a href="#🚀-tecnologias">Tecnologias</a>&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;
  <a href="#✴️-compilar-e-executar">Compilar e Executar</a>&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;
  <a href="#👽-feito-por">Feito por</a>
</p>
<br>

<a href="https://github.com/Ncgrande">
  <img align="center" src="https://img.shields.io/static/v1?label=github&message=NilsonGrande&color=7159c1&style=for-the-badge&logo=ghost"/>
</a>

---

# 🎯 Objetivo e Funcionalidades

<p align="justify">Este projeto implementa uma versão estilizada do clássico Tetris utilizando JavaFX, seguindo princípios de arquitetura limpa (DDD), persistência em banco de dados e multithreading. O jogo apresenta um visual retro/neon e funcionalidades avançadas, como um sistema de replay determinístico.</p>

### ✅ Funcionalidades principais:

- **Game Loop em thread dedicada:** A lógica do jogo roda de forma independente da interface para garantir fluidez.
- **Controles customizados:** Pausar jogo, iniciar nova partida e atalhos de teclado.
- **Persistência de dados:** Usuários, partidas e estatísticas são gravados via DAOs em MySQL.
- **Sistema de Replay:** O jogo salva a semente e os eventos da partida, permitindo recriação determinística do último jogo.
- **Visual retrô/neon:** UI construída totalmente em JavaFX com estética pixelada e efeitos luminosos.

---

# 📋 Estrutura do Projeto

O projeto segue uma arquitetura organizada por camadas:

```
meu-projeto-tetris/
├── src/main/java/tetris/
│   ├── model/                 # Tetrominós e classes de domínio
│   ├── replay/                # Lógica de Replay
│   │   ├── ReplayData.java
│   │   └── ReplayManager.java
│   ├── dao/                   # Acesso a dados (MySQL)
│   ├── GameController.java    # Engine do jogo (Loop + Threads)
│   ├── Partida.java           # Estado/agregado raiz do jogo
│   └── TetrisApp.java         # Classe principal (UI JavaFX)
├── src/main/resources/        # Fonts, sprites, sons
├── last_replay.dat            # Replay serializado
└── pom.xml                    # Configuração Maven
```

---

# 💾 Persistência e Replay

### 🗄️ Banco de Dados
A aplicação utiliza **MySQL** para registrar:

- `jogadores` – Nome e ID de cada usuário.
- `partidas` – Pontuações e informações de jogo.
- `estatisticas_jogador` – Melhor pontuação e total de partidas.

A comunicação ocorre via **JDBC**.

### 🎞️ Replay
O arquivo `last_replay.dat` armazena:
- Semente usada pelo gerador de peças.
- Lista de eventos do jogador.

Isso permite **recriar a última partida com precisão**.

---

# 🚀 Tecnologias

- **Java 21**
- **JavaFX** para interface gráfica
- **Maven** para build e dependências
- **MySQL**
- **JDBC (MySQL Connector)**
- **Multithreading** para o game loop

---

# ✴️ Compilar e Executar

Certifique-se de ter **Java 21+** e **Maven** instalados.

### 1. Instalar dependências
```bash
mvn clean install
```

### 2. Executar o jogo
```bash
mvn javafx:run
```

O Maven configurará o classpath com JavaFX e MySQL Connector automaticamente.

---

# 👽 Feito por
Estudante do 3º semestre de Análise e Desenvolvimento de Sistemas:

**Nilson Grande**

