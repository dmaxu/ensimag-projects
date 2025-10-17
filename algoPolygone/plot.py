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
    num_polygons_list = [100,250,500,1000,1750,2500,3500,4096]  
    execution_times = []

    for num_polygons in num_polygons_list:
        #polygons = generate_polygons(num_polygons, 5, 0, 100000) 
        filename = f"test_{num_polygons}.poly"
        #test_gen.generate_poly_file(filename, polygons)
        execution_time = measure_execution_time(filename)
        execution_times.append(execution_time)

    plt.plot(num_polygons_list, execution_times, marker='o')
    plt.xlabel('Number of Polygons')
    plt.ylabel('Execution Time (seconds)')
    plt.title('Execution Time vs. Number of Polygons')
    plt.grid(True)
    plt.show()

if __name__ == "__main__":
    main()

 