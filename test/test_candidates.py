from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.wait import WebDriverWait
import time
from bs4 import BeautifulSoup

options = webdriver.FirefoxOptions()
options.add_argument("--headless")
driver = webdriver.Firefox(options=options)

candidates = [
    ("animeszone.net", "https://animeszone.net/?s=Devil+May+Cry"),
    ("animeslan.net", "https://animeslan.net/?s=Devil+May+Cry"),
    ("animes.online", "https://animes.online/search?q=Devil+May+Cry"),
]

for name, url in candidates:
    try:
        print(f"\n--- {name} ---")
        driver.get(url)
        time.sleep(2)
        soup = BeautifulSoup(driver.page_source, "html.parser")
        for a in soup.find_all("a", href=True):
            href = a['href']
            if "devil" in href.lower() and ("anime" in href.lower() or name in href):
                print("FOUND:", href[:100])
    except Exception as e:
        print(f"Erro {name}:", e)

driver.quit()
