import requests
from bs4 import BeautifulSoup

HEADERS = {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"}

url = "https://animesvision.biz/search?nome=Devil+May+Cry"
r = requests.get(url, headers=HEADERS, timeout=10)
print("Status AV:", r.status_code)
soup = BeautifulSoup(r.text, "html.parser")

for div in soup.find_all("div", class_="film-detail"):
    a = div.find("a", href=True)
    if a:
        print("Found:", a['href'], "->", a.text.strip())
