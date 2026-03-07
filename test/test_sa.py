import requests
from bs4 import BeautifulSoup
import json

HEADERS = {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"}

def test_sa():
    url = "https://goyabu.to/?s=Devil+May+Cry"
    try:
        r = requests.get(url, headers=HEADERS, timeout=10)
        print("Status:", r.status_code)
        soup = BeautifulSoup(r.text, "html.parser")
        for a in soup.find_all("a", href=True):
            if "devil-may-cry" in a['href'].lower() and "anime" in a['href'].lower():
                print("Found Anime:", a['href'])
                return a['href']
    except Exception as e:
        print("Erro:", e)

url_anime = test_sa()
