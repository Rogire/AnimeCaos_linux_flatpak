from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import time
from bs4 import BeautifulSoup

options = webdriver.FirefoxOptions()
options.add_argument("--headless")
driver = webdriver.Firefox(options=options)

url = "https://animesonlinecc.to/episodio/afro-samurai-episodio-1/"
print("Fetching:", url)
driver.get(url)
time.sleep(3)

soup = BeautifulSoup(driver.page_source, "html.parser")

# Procurar todos os elementos clicáveis suspeitos de ser player selector
print("=== Todos botões/divs na página ===")
for el in soup.find_all(["button", "a", "li", "div"]):
    cls = " ".join(el.get("class", []))
    text = el.text.strip()[:60]
    onclick = el.get("onclick", "")
    if any(kw in cls.lower() for kw in ["play", "mirror", "tab", "fonte", "server", "player"]):
        print(f"TAG: {el.name}, CLASS: {cls}, TEXT: {text}, ONCLICK: {onclick}")
    elif any(kw in text.lower() for kw in ["player", "server", "mirror", "fonte"]):
        print(f"TAG: {el.name}, CLASS: {cls}, TEXT: {text}")

driver.quit()
