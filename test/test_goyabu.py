import requests
from bs4 import BeautifulSoup

HEADERS = {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"}

def test_goyabu():
    url = "https://goyabu.com/?s=Devil+May+Cry"
    try:
        r = requests.get(url, headers=HEADERS, timeout=10)
        print("Goyabu status:", r.status_code)
        soup = BeautifulSoup(r.text, "html.parser")
        for a in soup.find_all("a", href=True)[:10]:
            if "devil-may-cry" in a['href'].lower():
                print("Found:", a['href'])
    except Exception as e:
        print("Goyabu erro:", e)

test_goyabu()
