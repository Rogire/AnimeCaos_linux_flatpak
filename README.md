<div align="center">
  <img src="icon.png" alt="animecaos logo" width="128" />
  <h1>animecaos</h1>
  <p><em>Premium, minimal anime streaming desktop application</em></p>
</div>

---

**animecaos** is a sleek, modern desktop application designed to search, select, and seamlessly stream anime from online sources directly into your favorite local video player (`mpv`). It features a high-quality user interface built in PySide6 with dark frosted glass styling, bypassing browser ads and external clutter.

## ✨ Características (Features)

- **Design Premium**: Interface gráfica moderna (GUI) desenvolvida com **PySide6** utilizando tema Escuro (_Dark Mode_), design _Frosted Glass_ com acentos na cor vermelha.
- **Player Integrado e Otimizado**: Integração nativa e direta com o **`mpv`** para uma reprodução leve, extremamente rápida e fluida.
- **Sistema de Plugins**: Módulos e roteirizadores independentes para buscar metadados e fontes de streaming fáceis de expandir (pasta `animecaos/plugins/`).
- **Histórico Inteligente**: O app salva seu histórico em um repositório local automatizado para que você continue assistindo os últimos episódios exatamente de onde parou.
- **Modo CLI Integrado**: Quer usar pelo terminal? O modo antigo Command Line Edge ainda funciona.

## 🛠 Pré-requisitos (Requirements)

O **animecaos** roda majoritariamente via fontes extraídas localmente, os seguintes requerimentos devem constar no sistema:

- **Python 3.10+**
- **Git**
- **[mpv](https://mpv.io/)** (Adicionado ao `PATH` do sistema do seu Desktop)
- **Firefox** (Para resolver alguns bloqueios visuais ou web scraping com _headless mode_ do Selenium)

## 📦 Instalação (Installation)

Para rodar do código fonte, faça o git clone e crie seu ambiente de Python local:

```bash
git clone https://github.com/henriqqw/anicaos.git
cd anicaos
python -m venv venv
```

### Windows (cmd / PowerShell)

```powershell
.\venv\Scripts\activate
pip install -r requirements.txt
```

### Linux / macOS

```bash
source venv/bin/activate
pip install -r requirements.txt
```

## 🚀 Como Usar (Usage)

Abra a **Interface Gráfica (GUI)** completa. Este é o modo padrão de execução:

```bash
python main.py
```

### Linha de Comando Opcional (CLI)

Se prefere a versão legado sem componentes visuais em janela:

```bash
python main.py --cli
```

**Comandos rápidos de utilidade no Terminal:**

- `-q`, `--query <Nome do Anime>`: Busca pelo nome de forma instantânea em modo texto.
- `-c`, `--continue_watching`: Salta direto e começa a reproduzir o seu último episódio gravado no histórico.
- `-d`, `--debug`: Liga verbose para inspeção e relatórios de bugs na navegação Selenium e extração de playlists (M3U8).
- `--help`: Imprime a biblioteca de opções de uso.

## 🏗 Gerando o Executável Nativo (Build)

Ao rodar sem interpretador nativamente, gere os binários localmente (Isso irá anexar o `icon.ico` ao binário final no Windows).

### Windows

```powershell
pyinstaller -n animecaos --onefile --icon=icon.ico --add-data "icon.png;." main.py --hidden-import animecaos --hidden-import animecaos.plugins --noconsole
```

> Encontre seu arquivo `.exe` empacotado dentro do diretório `.\dist\animecaos.exe`.

### Linux / macOS

```bash
./build.sh
```

## 📂 Arquitetura Modular

- `main.py`: Ponto de entrada (Entrypoint e ArgParse handler).
- `animecaos/app.py`: Roteamento Principal (Alterna modo CLI ou Janela).
- `animecaos/core/`: Domínio de Injeção de Dependência, Logs Internos e Regras da Aplicação.
- `animecaos/services/`: Camada de manipulação abstrata de Histórico, Pesquisas e Reproduções do Player.
- `animecaos/ui/gui/`: Elementos desenhados do PySide6. Organiza Threads (workers), Theme, Grid Panels.
- `animecaos/ui/cli/`: Rotinas baseadas no Terminal (curses + menus interativos).
- `animecaos/plugins/`: _Web Scrapers_ e resolvedores por provedor visualizador do anime.
- `animecaos/player/`: Comunicação bidirecional local com sua instalação do **mpv**.
