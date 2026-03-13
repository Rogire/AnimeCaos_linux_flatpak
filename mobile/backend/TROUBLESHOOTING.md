# Troubleshooting - Busca não retorna resultados

## Problema
Backend retorna 200 OK mas lista de animes vem vazia.

## Causas Possíveis

### 1. Cloudscraper com erro `double_down`

**Sintoma no log:**
```
Aviso: falha ao buscar anime em uma fonte: Session.__init__() got an unexpected keyword argument 'double_down'
```

**Solução:**
O patch foi atualizado para lidar com versões diferentes do cloudscraper. Reinicie o backend:

```bash
cd ~/animecaos/mobile/backend
source .venv/bin/activate

# Parar backend atual (Ctrl+C)

# Reiniciar
python3 -m uvicorn app.main:app --host 0.0.0.0 --port 8000
```

### 2. Plugins não carregados corretamente

**Verificação:**
```bash
# No log de startup, deve aparecer:
# Info: plugins mobile ativos: animesonlinecc, animefire, animesvision
```

**Solução:**
Se não aparecer, verifique `ANIMECAOS_PLUGINS` env var:

```bash
export ANIMECAOS_PLUGINS=animesonlinecc,animefire,animesvision
python3 -m uvicorn app.main:app --host 0.0.0.0 --port 8000
```

### 3. Seletores CSS desatualizados

Os sites mudam frequentemente. Verifique cada plugin:

**Teste manual na VPS:**
```bash
# Testar animesonlinecc
curl -H "User-Agent: Mozilla/5.0" "https://animesonlinecc.to/search/hunter"

# Testar animefire
curl -H "User-Agent: Mozilla/5.0" "https://animefire.io/pesquisar/hunter"

# Testar animesvision (requer Selenium)
# Verificar se retorna resultados no navegador
```

**Se 403 em todos:**
- cloudscraper não está funcionando
- Verificar se está instalado: `pip show cloudscraper`
- Reinstalar: `pip install --upgrade cloudscraper`

**Se retornar HTML normal:**
- Seletores CSS podem estar errados
- Inspecionar HTML no navegador
- Atualizar `animesvision_selectors.py` ou código dos plugins

### 4. Selenium/Firefox não inicializa

**Sintoma:**
- animesvision não retorna nada
- Timeout após 10+ segundos

**Verificação:**
```bash
# Verificar Firefox
which firefox-esr
# Deve: /usr/bin/firefox-esr

# Verificar geckodriver
which geckodriver
# Deve: /usr/local/bin/geckodriver

# Testar manualmente
firefox-esr --version
geckodriver --version
```

**Solução:**
```bash
# Instalar Firefox ESR
sudo apt update
sudo apt install firefox-esr

# Instalar geckodriver
wget https://github.com/mozilla/geckodriver/releases/latest/download/geckodriver-linux64.tar.gz
tar -xzf geckodriver-linux64.tar.gz
sudo mv geckodriver /usr/local/bin/
sudo chmod +x /usr/local/bin/geckodriver
```

### 5. AnimeService não usa plugins corretos

**Problema:**
`AnimeService.ensure_plugins_loaded()` pode estar sobrescrevendo plugins.

**Verificação:**
No log do backend, após busca:
```bash
# Deve mostrar os 3 plugins tentando busca
# Se só mostrar um, há problema
```

**Solução:**
O `AnimeService` foi atualizado para aceitar parâmetro `plugins`. Reinicie backend.

## Debug Passo a Passo

### Passo 1: Testar health endpoint
```bash
curl http://localhost:8000/health
# Esperado: {"status":"ok"}
```

### Passo 2: Testar busca com verbose
```bash
curl -v "http://localhost:8000/search?q=hunter"
```

**Olhar no log do backend:**
```
[HTTP] GET /search -> 200 (XXXX ms)
```

**Se XXXX < 100ms:**
- Plugins não rodaram (retornou cache vazio)
- Verificar se plugins estão ativos

**Se XXXX > 5000ms:**
- Plugins rodaram mas podem ter falhado
- Verificar avisos no log

### Passo 3: Verificar logs detalhados

```bash
# Ativar debug mode
export ANIMECAOS_DEBUG=1
python3 -m uvicorn app.main:app --host 0.0.0.0 --port 8000
```

### Passo 4: Testar cada plugin individualmente

```python
# Na VPS, Python shell
cd ~/animecaos/mobile/backend
source .venv/bin/activate
python3

# No shell:
from animecaos.plugins import animesonlinecc, animefire, animesvision
from animecaos.core.repository import rep

# Testar animesonlinecc
rep.reset_runtime_data()
animesonlinecc.AnimesOnlineCC.search_anime("hunter")
print("animesonlinecc:", rep.get_anime_titles())

# Testar animefire
rep.reset_runtime_data()
animefire.AnimeFire.search_anime("hunter")
print("animefire:", rep.get_anime_titles())

# Testar animesvision (pode demorar)
rep.reset_runtime_data()
animesvision.AnimesVision.search_anime("hunter")
print("animesvision:", rep.get_anime_titles())
```

## Logs Esperados (Sucesso)

```
Info: selenium_patch aplicado — binary=/usr/bin/firefox-esr, geckodriver=/usr/local/bin/geckodriver
  → animecaos.plugins.animesonlinecc: requests substituído por cloudscraper
  → animecaos.plugins.animefire: requests substituído por cloudscraper
Info: cloudscraper_patch aplicado em 2 módulo(s)
INFO:     Started server process [XXXXX]
INFO:     Waiting for application startup.
Info: plugins mobile ativos: animesonlinecc, animefire, animesvision
Info: AnimeService legado detectado; plugins aplicados via loader.AVAILABLE_PLUGINS.
Info: pasta de dados mobile: animecaos-mobile
INFO:     Application startup complete.
INFO:     Uvicorn running on http://0.0.0.0:8000

# Após busca:
[HTTP] GET /search -> 200 (8000-15000 ms)
INFO:     10.0.0.X:XXXXX - "GET /search?q=hunter HTTP/1.1" 200 OK
```

## Logs de Erro Comuns

### Erro: 403 Forbidden
```
Aviso: falha ao buscar anime em uma fonte: 403 Client Error: Forbidden
```
**Causa:** cloudscraper não está funcionando
**Solução:** Reiniciar backend após fix do `double_down`

### Erro: Firefox/geckodriver nao encontrado
```
RuntimeError: Firefox/geckodriver nao encontrado.
```
**Causa:** Paths não configurados
**Solução:** Verificar selenium_patch.py e instalar Firefox/geckodriver

### Erro: Timeout
```
TimeoutException: Message: Reached error page
```
**Causa:** Selenium muito lento ou site bloqueou
**Solução:** Aumentar timeout ou verificar se site está acessível

## Solução Rápida (TL;DR)

```bash
cd ~/animecaos/mobile/backend
source .venv/bin/activate

# Parar backend (Ctrl+C se rodando)

# Reiniciar
python3 -m uvicorn app.main:app --host 0.0.0.0 --port 8000

# Testar
curl "http://localhost:8000/search?q=hunter"
```

Se ainda não funcionar, verificar logs linha por linha.

---

**Atualizado:** 2026-03-13  
**Status:** ✅ Patches aplicados, aguardando restart
