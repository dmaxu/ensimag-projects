# Shadow Mapping

## Nom d'équipe
- Mehdi DIMASSI
- Yi Jun BOON
- Sritesh NAIR

## Exécution du programme
Nous vous préparons 2 scènes :
- Objet sur le sol (`sceneSol.py`)
- Objet en vol (`sceneUpward.py`)

Pour choisir la scène à exécuter, modifiez cette ligne dans le fichier `main.py` :
```python
vertices, triangles = readply('sceneUpward.ply')
```

Remplacez 'sceneUpward.ply' par 'sceneSol.ply' pour tester l'autre scène.

-------

Pour choisir le test, modifiez cette ligne dans main.py :
```python
TEST_CASE = 'top_light'
```
Voici les options disponibles pour TEST_CASE :

- 'top_light' : Lumière placée au-dessus de l'objet

- 'right_light' : Lumière latérale droite

- 'left_light' : Lumière latérale gauche

- 'tilted_front_light' : Lumière inclinée en provenance de l'avant

- 'side_light' : Lumière qui n’éclaire pas directement l’objet

-------

Ensuite, tapez la commande suivante pour exécuter le programme :
```python
python main.py
```