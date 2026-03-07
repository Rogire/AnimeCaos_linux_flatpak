from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import time
from bs4 import BeautifulSoup

options = webdriver.FirefoxOptions()
options.add_argument("--headless")
driver = webdriver.Firefox(options=options)

# GOAL: find all iframes in the episode page AND try clicking on each player tab to switch
url = "https://animesonlinecc.to/episodio/afro-samurai-episodio-1/"
print("Fetching:", url)
driver.get(url)
time.sleep(3)

soup = BeautifulSoup(driver.page_source, "html.parser")

# Procurar botões de player
for btn in soup.find_all(["button", "a", "li"]):
    text = btn.text.strip()
    if any(kw in text.lower() for kw in ["player", "mirror", "titulo", "fonte", "srv", "server"]):
        print("Player Btn:", text[:80])

print("\nAll iframes:")
for f in soup.find_all("iframe"):
    print("iframe src:", f.get("src", "")[:100])

driver.quit()
