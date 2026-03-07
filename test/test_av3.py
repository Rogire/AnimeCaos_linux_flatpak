import requests
from bs4 import BeautifulSoup

HEADERS = {"User-Agent": "Mozilla/5.0"}
url = "https://animesvision.biz/search?nome=Devil+May+Cry"
r = requests.get(url, headers=HEADERS, timeout=10)
soup = BeautifulSoup(r.text, "html.parser")

found = False
for a in soup.find_all("a", href=True):
    href = a['href']
    if 'anime' in href and 'devil-may-cry' in href:
        print("Link:", href, "| Texto:", a.text.strip())
        found = True

if not found:
    print("Nenhuma tag <a> util achada")
    print(r.text[:1000])
