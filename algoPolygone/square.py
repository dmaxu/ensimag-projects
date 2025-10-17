import random
from geo.point import Point

def generate_poly_file(filename, polygons):
    with open(filename, 'w') as f:
        index = 0
        for polygon in polygons:
            for point in polygon:
                f.write(f"{index} {point.coordinates[0]} {point.coordinates[1]}\n")
            index += 1

def generate_square(center, side_length):
    """
    Generate a square polygon given the center and side length.
    """
    half_length = side_length / 2
    x, y = center.coordinates
    points = [
        Point([x - half_length, y - half_length]),
        Point([x + half_length, y - half_length]),
        Point([x + half_length, y + half_length]),
        Point([x - half_length, y + half_length]),
    ]
    return points

def main():
    num_squares = 4096
    min_coord = 0
    max_coord = 1000
    polygons = []
    center = Point([0, 0])
    side_length = max_coord - min_coord  # Initial side length

    for _ in range(num_squares):
        square = generate_square(center, side_length)
        polygons.append(square)
        # Decrease the side length for the next square
        side_length -= 1

    filename = f"nested_squares_{num_squares}.poly"
    generate_poly_file(filename, polygons)
    print(f"Generated {filename} with {num_squares} nested squares.")

if __name__ == "__main__":
    main()
