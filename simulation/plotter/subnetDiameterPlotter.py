import pandas as pd
import matplotlib.pyplot as plt
import os

def plot_csv(inputFile):
    data = pd.read_csv(inputFile, delimiter=r'\s+', comment='#', header=None, names=['time', 'subnetDiameterValue'])
    
    if not os.path.exists('simulation/plot'):
        os.makedirs('simulation/plot')
    
    filtered_data = data[data['time'] <= 50]

    plt.figure(figsize=(10, 6))
    plt.plot(filtered_data['time'], filtered_data['subnetDiameterValue'], label='Subnet Diameter Value', color='blue')
    
    plt.title('Plot of Subnet Diameter Value Over Time')
    plt.xlabel('Time')
    plt.ylabel('Subnet Diameter (hop distance to source)')
    
    output_file = os.path.join('simulation/plot', 'tutorial-example.png')
    plt.savefig(output_file)
    
inputFile = 'simulation/data/tutorial-example.csv'
absolute_path = os.path.abspath(inputFile)
plot_csv(absolute_path)
