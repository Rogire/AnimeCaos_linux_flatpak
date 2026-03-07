from selenium import webdriver
import time
import json

options = webdriver.FirefoxOptions()
options.add_argument("--headless")
driver = webdriver.Firefox(options=options)

url = "https://www.blogger.com/video.g?token=AD6v5dzvjQc7oOP78n8hb0kG2Bd1bY5BWmn-DeZiCNcJzlDhp8Fbanxy4ZIvfFkm1B68U_-5l4XssyYyRnxthK3D5t-8-5fCnE86vpVRL0wEqsf5UySdm_dD1-ypBe41iu3zu6MRD3Rn"
print("Acessando:", url)
driver.get(url)

# Wait for scripts to execute and WIZ_global_data to populate
time.sleep(5)

print("\nTentando extrair do WIZ_global_data via JS injection:")
try:
    # A variável window.WIZ_global_data normalmente guarda a config de player no Youtube/Blogger
    data_json = driver.execute_script("return JSON.stringify(window.WIZ_global_data || {});")
    data = json.loads(data_json)
    
    # Vamos achar qualquer URL em string que termine com formato de video
    import re
    matches = re.findall(r'https://[^\s\"\'\\]+(?:play|videoplayback|mp4)[^\s\"\'\\]*', data_json)
    
    if matches:
        print(">> Encontradas URLs escondidas no objeto WIZ_global_data:")
        for m in set(matches):
            print("---", m)
    else:
        print(">> Nenhuma URL de video detectada no WIZ_global_data.")
        # print("Tamanho do objeto:", len(data_json), "chars")
        
    # fallback: tentar buscar o _CONFIG_
    cfgs = driver.execute_script("""
        var urls = [];
        // varredura global
        for (var k in window) {
            try {
                var v = window[k];
                if (typeof v === "string" && v.indexOf("videoplayback") !== -1) urls.push(v);
                if (v && v.streams) urls.push(JSON.stringify(v.streams));
            } catch(e) {}
        }
        return urls;
    """)
    if cfgs:
        print("\n>> Variaveis globais de stream suspeitas achadas:")
        print(cfgs)

except Exception as e:
    print("Erro JS:", e)

driver.quit()
