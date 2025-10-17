import subprocess
import psutil
import time
import matplotlib.pyplot as plt

def monitor_energy(command, duration=10):
    """
    Monitors the energy consumption while executing a shell command.
    
    Args:
        command (str): The shell command to execute.
        duration (int): Time (in seconds) for monitoring.
    
    Returns:
        dict: Dictionary with timestamps and energy data.
    """
    # Initialize data
    timestamps = []
    cpu_usages = []
    energy_estimations = []
    start_time = time.time()
    
    # Start the process
    process = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    
    try:
        while process.poll() is None:
            # Measure CPU usage and estimate energy
            cpu_usage = psutil.cpu_percent(interval=1)
            timestamps.append(time.time() - start_time)
            cpu_usages.append(cpu_usage)
            # Energy estimation: Assuming 1% CPU = 1.2 watts (adjust this for your machine)
            energy_estimations.append(cpu_usage * 1.2)
            print(f"Time: {timestamps[-1]:.2f}s | CPU Usage: {cpu_usage}% | Energy: {energy_estimations[-1]:.2f} watts")
    except KeyboardInterrupt:
        process.terminate()
        raise
    
    # Collect the remaining data after process ends
    process.wait()
    return {"timestamps": timestamps, "energy": energy_estimations, "cpu": cpu_usages}

def plot_energy(data, title, ax, energy_color, cpu_color):
    """
    Plots the energy consumption over time on the given axes with custom colors.
    
    Args:
        data (dict): Dictionary with timestamps and energy data.
        title (str): Title of the graph.
        ax (matplotlib.axes.Axes): The axes on which to plot the graph.
        energy_color (str): Color for the energy consumption line.
        cpu_color (str): Color for the CPU usage line.
    """
    ax.plot(data["timestamps"], data["energy"], label=f"{title} - Energy Consumption (Watts)", color=energy_color)
    ax.plot(data["timestamps"], data["cpu"], label=f"{title} - CPU Usage (%)", color=cpu_color, linestyle="--")
    ax.set_xlabel("Time (s)")
    ax.set_ylabel("Energy / CPU Usage")
    ax.set_title("Energy Consumption over Time")
    ax.grid(True)

if __name__ == "__main__":
    # List of commands to monitor
    commands = [
        "./src/test/script/basic-gencode.sh",
        "./src/test/script/basic-synt.sh",
        "./src/test/script/basic-context.sh"
    ]
    
    # Define custom colors for each command
    colors = [
        ("blue", "orange"),     # Colors for mvn test
        ("green", "red"),       # Colors for basic-gencode.sh
        ("purple", "brown"),    # Colors for basic-synt.sh
        ("cyan", "magenta")     # Colors for basic-context.sh
    ]
    
    # Create a figure and axis to plot all graphs on the same figure
    fig, ax = plt.subplots(figsize=(10, 6))
    
    for idx, cmd in enumerate(commands):
        print(f"Running command: {cmd}")
        data = monitor_energy(cmd)
        energy_color, cpu_color = colors[idx]
        plot_energy(data, f"Energy Consumption for '{cmd}'", ax, energy_color, cpu_color)
    
    # Add a legend and save the figure to a file
    ax.legend(loc="upper right")
    plt.tight_layout()  # Adjust layout to prevent clipping
    plt.savefig("conso.png")  # Save the figure to a file
    print("Energy consumption graphs saved to 'conso.png'")
