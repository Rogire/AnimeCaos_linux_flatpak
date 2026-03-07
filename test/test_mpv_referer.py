import subprocess
url = "https://www.blogger.com/video.g?token=AD6v5dzvjQc7oOP78n8hb0kG2Bd1bY5BWmn-DeZiCNcJzlDhp8Fbanxy4ZIvfFkm1B68U_-5l4XssyYyRnxthK3D5t-8-5fCnE86vpVRL0wEqsf5UySdm_dD1-ypBe41iu3zu6MRD3Rn"
print("Tentando rodar no mpv com spoofing de referrer...")
try:
    subprocess.run([
        "mpv",
        url,
        "--http-header-fields=Referer: https://www.blogger.com/",
        "--log-file=mpv_test.log"
    ], check=True)
    print("MPV abriu com sucesso!")
except subprocess.CalledProcessError as e:
    print("Falha:", e)

with open("mpv_test.log", "r") as f:
    text = f.read()
    if "Failed to recognize file format" in text:
         print("Continua quebrado! Referrer não ajudou.")
    else:
         print("Pode ter funcionado, olhe o log")
