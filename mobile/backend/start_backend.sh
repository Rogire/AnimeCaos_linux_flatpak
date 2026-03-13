#!/bin/bash
# Script de inicialização do Backend Mobile AnimeCaos
# Uso: ./start_backend.sh

set -e

cd "$(dirname "$0")"

echo "=============================================="
echo "AnimeCaos Mobile Backend - Inicialização"
echo "=============================================="

# Ativar ambiente virtual
if [ ! -d ".venv" ]; then
    echo "❌ Erro: Ambiente virtual não encontrado em .venv"
    echo "   Execute: python3 -m venv .venv && source .venv/bin/activate && pip install -r requirements.txt"
    exit 1
fi

source .venv/bin/activate

# Verificar dependências
echo "Verificando dependências..."
python3 -c "import cloudscraper" 2>/dev/null || {
    echo "⚠️  cloudscraper não instalado. Instalando..."
    pip install cloudscraper==1.2.71
}

# Verificar Firefox e geckodriver
if [ ! -f "/usr/bin/firefox-esr" ]; then
    echo "⚠️  Firefox ESR não encontrado em /usr/bin/firefox-esr"
    echo "   Execute: sudo apt install firefox-esr"
fi

if [ ! -f "/usr/local/bin/geckodriver" ]; then
    echo "⚠️  Geckodriver não encontrado em /usr/local/bin/geckodriver"
    echo "   Baixe em: https://github.com/mozilla/geckodriver/releases"
fi

echo ""
echo "=============================================="
echo "Iniciando backend na porta 8000..."
echo "=============================================="
echo ""
echo "Endpoints disponíveis:"
echo "  - GET  /health        → Teste de saúde"
echo "  - GET  /search?q=...  → Buscar anime"
echo "  - GET  /episodes?...  → Listar episódios"
echo "  - GET  /player-url?... → URL do player"
echo ""
echo "Plugins ativos: animesonlinecc, animefire, animesvision"
echo ""
echo "IP da VPS: $(hostname -I | awk '{print $1}')"
echo "URL externa: http://$(hostname -I | awk '{print $1}'):8000"
echo ""
echo "Para testar:"
echo "  curl http://localhost:8000/health"
echo "  curl \"http://localhost:8000/search?q=hunter\""
echo ""
echo "=============================================="
echo ""

# Iniciar servidor
python3 -m uvicorn app.main:app --host 0.0.0.0 --port 8000
