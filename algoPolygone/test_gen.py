import random
from geo.point import Point
from geo.segment import Segment

def generate_poly_file(filename, polygons):
    with open(filename, 'w') as f:
        for i, polygon in enumerate(polygons):
            for point in polygon:
                f.write(f"{i} {point.coordinates[0]} {point.coordinates[1]}\n")

def generate_point(min_coord, max_coord):
    x = random.uniform(min_coord, max_coord)
    y = random.uniform(min_coord, max_coord)
    return Point([x, y])

def generate_polygon(num_points, min_coord, max_coord, max_distance, existing_polygons):
    while True:
        polygon = [generate_point(min_coord, max_coord)]
        for _ in range(num_points - 1):
            new_point = generate_point(min_coord, max_coord)
            while any(new_point.distance_to(p) > max_distance for p in polygon):
                new_point = generate_point(min_coord, max_coord)
            polygon.append(new_point)
        if not any(intersects(polygon, p) for p in existing_polygons):
            return polygon

def intersects(polygon1, polygon2):
    for p1, p2 in zip(polygon1, polygon1[1:] + [polygon1[0]]):
        for q1, q2 in zip(polygon2, polygon2[1:] + [polygon2[0]]):
            if Segment([p1, p2]).intersects(Segment([q1, q2])):
                return True
    return False

def main():
    num_polygons = 5000
    num_points_per_polygon = 5
    min_coord = 0.0
    max_coord = 10000.0
    max_distance = 30.0  # Maximum distance between consecutive points
    polygons = []
    for _ in range(num_polygons):
        polygon = generate_polygon(num_points_per_polygon, min_coord, max_coord, max_distance, polygons)
        polygons.append(polygon)

    filename = f"test_{num_polygons}.poly"
    generate_poly_file(filename, polygons)
    print(f"Generated {filename} with {num_polygons} polygons.")

if __name__ == "__main__":
    main()
