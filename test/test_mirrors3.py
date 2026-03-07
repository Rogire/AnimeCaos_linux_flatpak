from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import time

options = webdriver.FirefoxOptions()
options.add_argument("--headless")
driver = webdriver.Firefox(options=options)

url = "https://animesonlinecc.to/episodio/afro-samurai-episodio-1/"
print("Fetching:", url)
driver.get(url)
time.sleep(3)

# Get all player tabs (links/buttons that switch player sources)
tabs = driver.find_elements(By.CSS_SELECTOR, ".player_sist, .playex")
print(f"Found {len(tabs)} player source tabs")

for i, tab in enumerate(tabs):
    try:
        print(f"\n--- Tab {i}: '{tab.text.strip() or tab.get_attribute('data-id') or tab.get_attribute('id')}' ---")
        driver.execute_script("arguments[0].click();", tab)
        time.sleep(2)
        
        # Get current iframe src
        iframes = driver.find_elements(By.TAG_NAME, "iframe")
        for iframe in iframes:
            src = iframe.get_attribute("src")
            if src:
                print(f"  iframe src: {src[:120]}")
    except Exception as e:
        print(f"  Err: {e}")

driver.quit()
