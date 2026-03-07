import requests
import json
from bs4 import BeautifulSoup

HEADERS = {"User-Agent": "Mozilla/5.0"}
url = "https://betteranime.net/pesquisa?pesquisa=Devil+May+Cry"
try:
    r = requests.get(url, headers=HEADERS, timeout=10)
    print("BetterAnime:", r.status_code)
    soup = BeautifulSoup(r.text, "html.parser")
    # cards
    for art in soup.find_all("article"):
         a = art.find("a", href=True)
         if a: print("Found:", a.get('href'))
except Exception as e:
    print(e)
