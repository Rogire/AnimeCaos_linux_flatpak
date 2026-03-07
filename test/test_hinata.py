import requests
from bs4 import BeautifulSoup

HEADERS = {"User-Agent": "Mozilla/5.0"}
url = "https://hinatasoul.com/animes"
print(f"Buscando em {url}...")
try:
    # Testar busca no site Hinata-Soul e listar layout
    search_url = "https://hinatasoul.com/busca?busca=Devil+May+Cry"
    r = requests.get(search_url, headers=HEADERS, timeout=10)
    print("Status code:", r.status_code)
    
    soup = BeautifulSoup(r.text, "html.parser")
    # Tentar achar a tag que carrega animes
    items = soup.find_all("div", class_="item")
    if not items:
         items = soup.find_all("a")
    for a in items[:5]:
        href = a.get('href') if a.name == 'a' else (a.find('a')['href'] if a.find('a') else '')
        print("Link:", href)
except Exception as e:
    print("Erro Request:", e)
