import os
import time
import random
import subprocess
import matplotlib.pyplot as plt
import test_gen

def generate_polygons(num_polygons, num_points_per_polygon, min_coord, max_coord):
    return [test_gen.generate_polygon(num_points_per_polygon, min_coord, max_coord) for _ in range(num_polygons)]

def measure_execution_time(filename): 
    start_time = time.time()
    subprocess.run(["python3", "main.py", filename])
    end_time = time.time()
    return end_time - start_time

def main():
    num_points_list = [100, 250, 500, 1000, 1750, 2500, 3500, 5000]  
    execution_times = []

    num_polygons = 200 # Number of polygons (constant)
    min_coord = 0
    max_coord = 100000

    for num_points_per_polygon in num_points_list:
        polygons = generate_polygons(num_polygons, num_points_per_polygon, min_coord, max_coord) 
        filename = f"test_{num_points_per_polygon}.poly"
        test_gen.generate_poly_file(filename, polygons)
        execution_time = measure_execution_time(filename)
        execution_times.append(execution_time)

    plt.plot(num_points_list, execution_times, marker='o')
    plt.xlabel('Number of Points per Polygon')
    plt.ylabel('Execution Time (seconds)')
    plt.title('Execution Time vs. Number of Points per Polygon')
    plt.grid(True)
    plt.show()

if __name__ == "__main__":
    main()
