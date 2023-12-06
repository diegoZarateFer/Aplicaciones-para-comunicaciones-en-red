# Practica 05.
# Autores:
#   Valdes Castillo Diego.
#   Zarate Fernandez Diego.

import os
import requests
from concurrent.futures import ThreadPoolExecutor
from bs4 import BeautifulSoup
from urllib.parse import urljoin

# Lista para almacenar las URLs ya visitadas
urls_visitadas = set()

def descargar_recurso(url):
    try:
        response = requests.get(url)
        if response.status_code == 200:
            if url.endswith(('.html', '.htm')):  # Si es una página HTML, guarda el HTML
                nombre_archivo = os.path.basename(url)
                with open(nombre_archivo, 'wb') as archivo:
                    archivo.write(response.content)
                print(f'{nombre_archivo} descargado exitosamente.')
            else:  # Si es un recurso, guarda el recurso
                nombre_archivo = os.path.basename(url)
                with open(nombre_archivo, 'wb') as archivo:
                    archivo.write(response.content)
                print(f'{nombre_archivo} descargado exitosamente.')
        else:
            print("")
    except Exception as e:
        print("")

def descargar_enlaces_en_paralelo(links):
    with ThreadPoolExecutor(max_workers=50) as executor:
        futures = [executor.submit(descargar_recurso, link) for link in links]
        for future in futures:
            future.result()

def descargar_recursos_desde_pagina(url):
    try:
        response = requests.get(url)
        if response.status_code == 200:
            contenido_pagina = response.text
            soup = BeautifulSoup(contenido_pagina, 'html.parser')
            links = obtener_enlaces(soup, url)
            
            # Agregar la URL actual a las URLs visitadas
            urls_visitadas.add(url)
            
            # Descargar los enlaces que no se han visitado
            enlaces_nuevos = [link for link in links if link not in urls_visitadas]
            
            # Filtrar enlaces que ya se han visitado
            enlaces_por_visitar = [link for link in enlaces_nuevos if link not in urls_visitadas]
            
            # Descargar enlaces no visitados
            if enlaces_por_visitar:
                descargar_enlaces_en_paralelo(enlaces_por_visitar)
            
            # Visitar los enlaces si no se han visitado antes
            for link in enlaces_por_visitar:
                if link not in urls_visitadas:
                    descargar_recursos_desde_pagina(link)
        else:
            print(f'No se pudo acceder a la página {url}. Estado de respuesta: {response.status_code}')
    except Exception as e:
        print("....")

def obtener_enlaces(soup, base_url):
    enlaces = []
    for link in soup.find_all('a', href=True):
        enlace = link.get('href')
        enlace_completo = urljoin(base_url, enlace)
        enlaces.append(enlace_completo)
    return enlaces

if __name__ == "__main__":
    url_pagina = input("Ingresa la URL: ")
    descargar_recursos_desde_pagina(url_pagina)
