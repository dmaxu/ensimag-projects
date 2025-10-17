import numpy as np
from camera import Camera
from projection import Projection
import time
from graphicPipeline import GraphicPipeline
import matplotlib.pyplot as plt
from readply import readply
from PIL import Image
from numpy import asarray

def create_light_config(test_case):
    """Crée des configurations lumineuses prédéfinies"""
    configs = {
         # Lumière du dessus 
        'top_light': {
            'position': np.array([0.0, 0.0, 1.8]),
            'lookAt': np.array([0.0, 0.0, -1.0]),
            'up': np.array([0.0, 1.0, 0.0]),
            'right': np.array([1.0, 0.0, 0.0]),
            'fov': 0.5,
            'far': 15.0
        },
        
        # Lumière latérale droite
        'right_light': {
            'position': np.array([3.0, 0.0, 2.0]),
            'lookAt': np.array([-1.0, 0.0, -0.5]),
            'up': np.array([0.0, 1.0, 0.0]),
            'right': np.array([0.0, 0.0, 1.0]),
            'fov': 1.0,
            'far': 10.0
        },

        # Lumière latérale gauche 
        'left_light': {
            'position': np.array([-3.0, 0.0, 2.0]),
            'lookAt': np.array([1.0, 0.0, -0.5]),
            'up': np.array([0.0, 1.0, 0.0]),
            'right': np.array([0.0, 0.0, -1.0]),
            'fov': 1.0,
            'far': 10.0
        },
        
        # Lumière avant inclinée
        'tilted_front_light': {
            'position': np.array([0.0, 0.0, 2.4684829711914062]),
            'lookAt': np.array([-0.09396465103636216, -0.008624980208660761, -0.955381730863035]),
            'up': np.array([0.9293253771492707, -0.3594398503165051, -
                   0.08460104840257257]),
            'right': np.array([0.3571064096214568, 0.933128396166905, -0.04179005233607753]),
            'fov': 0.73,
            'far': 20.0
        },

        # Lumière qui n’éclaire pas directement l’objet
        'side_light': {
            'position': np.array([1.4, 1.1, 1.8]),
            'lookAt': np.array([-0.277, -0.277, -0.077]),
            'up': np.array([0.5333, 0.5333, -0.6667]),
            'right': np.array([-0.5774, 0.5774, 0.0]),
            'fov': 1.51986,
            'far': 10.0
        }
    }
    return configs[test_case]

# Paramètres initiaux
width = 1280
height = 720
pipeline = GraphicPipeline(width, height)

# Chargement des assets
vertices, triangles = readply('sceneUpward.ply')
image = asarray(Image.open('suzanne.png'))

# Configuration de la caméra principale
cam = Camera(
    position=np.array([1.1, 1.1, 1.8]),
    lookAt=np.array([-0.277, -0.277, -0.077]),
    up=np.array([0.5333, 0.5333, -0.6667]),
    right=np.array([-0.5774, 0.5774, 0.0])
)

# Choisir une configuration lumineuse à tester
TEST_CASE = 'top_light'  # Changer ici pour tester différentes lumières
light_config = create_light_config(TEST_CASE)

# Création de la lumière
light_cam = Camera(
    position=light_config['position'],
    lookAt=light_config['lookAt'],
    up=light_config['up'],
    right=light_config['right']
)

light_proj = Projection(
    near=0.1,
    far=light_config['far'],
    fov=light_config['fov'],
    aspectRatio=width/height
)

# Configuration des données
data = {
    'viewMatrix': cam.getMatrix(),
    'projMatrix': Projection(0.1, 10.0, 1.51986, width/height).getMatrix(),
    'cameraPosition': cam.position,
    'lightPosition': light_config['position'], 
    'texture': image
}

light_data = {
    'viewMatrix': light_cam.getMatrix(),
    'projMatrix': light_proj.getMatrix(),
    'cameraPosition': light_config['position'],
    'texture': image
}

testLightData = dict([
    ('viewMatrix', light_cam.getMatrix()),
    ('projMatrix', light_proj.getMatrix()),
    ('cameraPosition', light_config['position']),
    ('lightPosition', light_config['position']),  
    ('texture', image),
])

# Après avoir généré le rendu principal
start = time.time()

# Rendu principal avec ombres
pipeline.draw(vertices, triangles, data, light_data)

# Création d'une nouvelle pipeline pour la vue lumière
light_pipeline = GraphicPipeline(width, height)

# Rendu depuis la perspective de la lumière (sans ombres)
light_pipeline.draw(vertices, triangles, testLightData)  # Pas de lightData ici

end = time.time()
print(f"Temps total : {end - start:.2f}s")

# Affichage des trois vues
fig, (ax1, ax2, ax3) = plt.subplots(1, 3, figsize=(18, 6))

# 1. Rendu final avec ombres
ax1.imshow(pipeline.image)
ax1.set_title('Rendu Final avec Ombres')

# 2. Vue de la lumière (comme une caméra)
ax2.imshow(light_pipeline.image)
ax2.set_title('Vue de la Lumière')

# 3. Shadow Map brute
ax3.imshow(pipeline.shadowMap, cmap='gray')
ax3.set_title('Shadow Map Générée')

plt.show()