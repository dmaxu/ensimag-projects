#!/usr/bin/env python3
"""
fichier principal pour la detection des inclusions.
ce fichier est utilise pour les tests automatiques.
attention donc lors des modifications.
"""
import sys
from tycat import read_instance
from geo.segment import Segment
from geo.point import Point
from geo.quadrant import Quadrant
from geo.polygon import couples



# def point_inside_polygon(point, polygon):
#     """
#     Check if a point is inside a polygon using the ray casting algorithm.
#     """
#     max_x = max(p.coordinates[0] for p in polygon.points)
#     #ray = Segment([point, Point([point.coordinates[0]+10000000, point.coordinates[1]])])
#     ray = Segment([point, Point([max_x+1, point.coordinates[1]])])
#     intersections = 0
#     for segment in polygon.segments():
#         if ray.intersects(segment):
#             intersections += 1
#     return (intersections % 2 == 1)

def point_inside_polygon(point, polygon):
    """
    Check if a point is inside a polygon using the winding number algorithm.
    """
    wn = 0  
    for i in range(len(polygon.points)):
        p1 = polygon.points[i]
        p2 = polygon.points[(i + 1) % len(polygon.points)]  

        if p1.coordinates[1] <= point.coordinates[1]:
            if p2.coordinates[1] > point.coordinates[1]:
                if is_left(p1, p2, point) > 0:
                    wn += 1
        else:
            if p2.coordinates[1] <= point.coordinates[1]:
                if is_left(p1, p2, point) < 0:
                    wn -= 1

    return wn != 0



def is_left(p1, p2, point):
    """
    Check if a point is to the left of a directed line.
    """
    return (p2.coordinates[0] - p1.coordinates[0]) * (point.coordinates[1] - p1.coordinates[1]) - \
           (point.coordinates[0] - p1.coordinates[0]) * (p2.coordinates[1] - p1.coordinates[1])



# def trouve_inclusions(polygones): methode exhaustive
#     """
#     renvoie le vecteur des inclusions
#     la ieme case contient l'indice du polygone
#     contenant le ieme polygone (-1 si aucun).
#     (voir le sujet pour plus d'info)
#     """
#     inclusions = []
#     for i, poly1 in enumerate(polygones):
#         inclusion_index = -1
#         max_area = float('inf')
#         for j, poly2 in enumerate(polygones):
#             res = True
#             if i!=j :
#                 for pt in poly1.points :
#                     res = res and point_inside_polygon(pt, poly2)
#                     if not res :
#                         break
#                 if res and abs(poly2.area())<max_area :
#                     max_area = poly2.area()
#                     inclusion_index = j
#         inclusions.append(inclusion_index)
#     return inclusions

# def trouve_inclusions(polygones): algo exhaustive amoélioré
#     """
#     Renvoie le vecteur des inclusions
#     La ieme case contient l'indice du polygone
#     contenant le ieme polygone (-1 si aucun).
#     """
#     inclusions = [-1] * len(polygones)
#     sorted_polygons = sorted(enumerate(polygones), key=lambda x: abs(x[1].area()))

#     for i, (poly1_index, poly1) in enumerate(sorted_polygons):
#         inclusion_index = -1
#         for j in range(i + 1, len(sorted_polygons)):
#             poly2_index, poly2 = sorted_polygons[j]
#             res = all(point_inside_polygon(pt, poly2) for pt in poly1.points)
#             if res:
#                 inclusion_index = poly2_index
#                 break
#         inclusions[poly1_index]=inclusion_index

#     return inclusions





class QuadTree:
    def __init__(self, boundary, capacity=4):
        self.boundary = boundary
        self.capacity = capacity
        self.polygons = []  # List to store polygons
        self.polygon_indices = []  # List to store indices of polygons
        self.children = []  # List to store child nodes

    def insert(self, index, polygon):
        if not (polygon.bounding_quadrant()).is_inside(self.boundary):
            return False

        if len(self.polygons) < self.capacity:
            self.polygons.append(polygon)
            self.polygon_indices.append(index)
            return True
        else:
            if not self.children:
                self.split()
            for child in self.children:
                if child.insert(index, polygon):
                    return True

            # If the polygon cannot fit into any child node, add it to the current node
            self.polygons.append(polygon)
            self.polygon_indices.append(index)
            return True

    def split(self):
        half_width = (self.boundary.max_coordinates[0] - self.boundary.min_coordinates[0]) / 2
        half_height = (self.boundary.max_coordinates[1] - self.boundary.min_coordinates[1]) / 2

        self.children.append(QuadTree(
            Quadrant(self.boundary.min_coordinates, [self.boundary.min_coordinates[0] + half_width,
                                                     self.boundary.min_coordinates[1] + half_height]),
            self.capacity
        ))
        self.children.append(QuadTree(
            Quadrant([self.boundary.min_coordinates[0] + half_width, self.boundary.min_coordinates[1]],
                     [self.boundary.max_coordinates[0], self.boundary.min_coordinates[1] + half_height]),
            self.capacity
        ))
        self.children.append(QuadTree(
            Quadrant([self.boundary.min_coordinates[0], self.boundary.min_coordinates[1] + half_height],
                     [self.boundary.min_coordinates[0] + half_width, self.boundary.max_coordinates[1]]),
            self.capacity
        ))
        self.children.append(QuadTree(
            Quadrant([self.boundary.min_coordinates[0] + half_width, self.boundary.min_coordinates[1] + half_height],
                     self.boundary.max_coordinates),
            self.capacity
        ))
        new_polygones = []
        new_polygones_i = []
        # Reinsert polygons into child nodes
        for poly_index, polygon in enumerate(self.polygons):
            child_inserted = False
            for child in self.children:
                if child.insert(self.polygon_indices[poly_index], polygon):
                    child_inserted = True
                    break
            if not child_inserted:
                # Polygon does not fit into any child node, keep it in the current node
                new_polygones.append(polygon)
                new_polygones_i.append(self.polygon_indices[poly_index])

        self.polygons = new_polygones 
        self.polygon_indices = new_polygones_i

    def query(self, query_quadrant):
        polygons_in_range = []
        if not self.boundary.intersect(query_quadrant):
            return polygons_in_range

        for poly_index, polygon in enumerate(self.polygons):
            if polygon.bounding_quadrant().intersect(query_quadrant):
                polygons_in_range.append((self.polygon_indices[poly_index], polygon))

        if self.children:
            for child in self.children:
                polygons_in_range.extend(child.query(query_quadrant))

        return polygons_in_range


def trouve_inclusions(polygones):
    inclusions = [-1] * len(polygones)
    
    # Determine the overall spatial boundary to use for the QuadTree
    overall_min_coordinates = [float('inf'), float('inf')]
    overall_max_coordinates = [float('-inf'), float('-inf')]
    quadrants = []
    for polygon in polygones:
        bounding_quadrant = polygon.bounding_quadrant()
        quadrants.append(bounding_quadrant)
        for i in range(2):
            overall_min_coordinates[i] = min(overall_min_coordinates[i], bounding_quadrant.min_coordinates[i])
            overall_max_coordinates[i] = max(overall_max_coordinates[i], bounding_quadrant.max_coordinates[i])

    # Create the QuadTree with the computed overall boundary
    boundary = Quadrant(overall_min_coordinates, overall_max_coordinates)
    spatial_index = QuadTree(boundary)

    for index, polygon in enumerate(polygones):
        spatial_index.insert( index,polygon)

    # Use the spatial index to perform efficient containment checks
    for i, poly1 in enumerate(polygones):
        inclusion_index = -1
        candidates = spatial_index.query(quadrants[i])
        sorted_candidates = sorted(candidates, key=lambda x: abs(x[1].area()))
        for candidate_index, candidate in sorted_candidates:
            if candidate_index!=i and point_inside_polygon(poly1.points[0], candidate):
                inclusion_index = candidate_index
                break
        

        inclusions[i] = inclusion_index

    return inclusions

# def trouve_inclusions(polygones): 
    # """
    # Renvoie le vecteur des inclusions
    # La ieme case contient l'indice du polygone
    # contenant le ieme polygone (-1 si aucun).
    # """
    # inclusions = [-1] * len(polygones)
    # sorted_polygons = sorted(enumerate(polygones), key=lambda x: abs(x[1].area()))
    # sorted_quadrants =[(i,polygon.bounding_quadrant()) for (i,polygon) in sorted_polygons]
    # for i, (quad1_index, quad1) in enumerate(sorted_quadrants):
    #     inclusion_index = -1
    #     for j in range(i + 1, len(sorted_quadrants)):
    #         quad2_index, quad2 = sorted_quadrants[j]
    #         if quad1.is_inside(quad2) :
    #             res = point_inside_polygon(sorted_polygons[i][1].points[0], sorted_polygons[j][1])
    #             if res:
    #                 inclusion_index = quad2_index
    #                 break

    #     inclusions[quad1_index]=inclusion_index

    # return inclusions


def main():
    """
    charge chaque fichier .poly donne
    trouve les inclusions
    affiche l'arbre en format texte
    """
    for fichier in sys.argv[1:]:
        polygones = read_instance(fichier)
        inclusions = trouve_inclusions(polygones)
        print(inclusions)


if __name__ == "__main__":
    main()
