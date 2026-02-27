from plot_utils import run_chart_generation
import os
import pickle
import matplotlib.pyplot as plt
import numpy as np
import seaborn as sns
import pandas as pd
sns.set_context("talk", rc={"axes.titlesize": 18, "axes.labelsize": 16, "xtick.labelsize": 14, "ytick.labelsize": 14})
sns.set_style("whitegrid", {"axes.grid": True, "grid.linestyle": "-", "grid.linewidth": 0.5})
    
experiments = [
    "baseline-random-failure",
    "oracle-random-failure",
    "runtime-random-failure"
]

def load_is_not_in_cache(experiment):
    print(f"Loading {experiment} data from cache...")
    cache_path = f"cache/{experiment}.pkl"
    if not os.path.exists("cache"):
        os.makedirs("cache")
    if os.path.exists(cache_path):
        with open(cache_path, "rb") as f:
            data = pickle.load(f)
    else:
        mean, std, full = run_chart_generation(
            f"data/{experiment}",
            [experiment],
            '{: 0.3f}',
            100,
            "time",
            ["seed"],
            100,
            10000
        )
        data = {
            "mean": mean[experiment],
            "std": std[experiment],
            "full": full[experiment]
        }
        with open(cache_path, "wb") as f:
            pickle.dump(data, f)

    return data

data_loaded = {
    experiment: load_is_not_in_cache(experiment) for experiment in experiments
}

def extract_max_is_done_percentage(data, std):
    max_moving_time = data["lastMovingTime[max]"].max().item()
    # get the index of the first time where lastMovingTime[max] is maximum
    index = data["lastMovingTime[max]"].argmax().item()
    std_max_moving_time = std["lastMovingTime[max]"].isel(time=index).max().item()
    return std_max_moving_time, max_moving_time

def extract_max_time_from_full(data):
    all = []
    for seed in data.seed.values:
        current_data = data.sel(seed=seed.item())
        max_moving_time = current_data["lastMovingTime[max]"].max().item()
        all.append(max_moving_time)
    return all

def extract_max_replanning_from_full(data):
    all = []
    for seed in data.seed.values:
        current_data = data.sel(seed=seed.item())
        max_replanning = current_data["totalReplanning[mean]"].max().item()
        all.append(max_replanning)
    return all
def extract_max_replanning(data, std):
    max_replanning = data["totalReplanning[mean]"].max().item()
    # get the index of the first time where lastMovingTime[max] is maximum
    index = data["totalReplanning[mean]"].argmax().item()
    # take the minimum of the std at that index
    min_replanning = data["totalReplanning[mean]"].min().item()
    std_max_replanning = std["totalReplanning[mean]"].isel(time=index).max().item()
    return 0, max_replanning

def plot_last_time_is_done_in_runtime_failure(oracle, baseline, runtime, node: int, task_factor: float, ax=None, leader_based=False):
    # Set a larger font context and a compact style for the plot
    
    failure_times = [ft.item() for ft in oracle["mean"].failureTimeAverage]
    bar_data = []

    def add_entries(failure_time, approach_label, data):
        stable_times = extract_max_time_from_full(data)
        ft_str = str(int(failure_time))
        for stable_time in stable_times:
            bar_data.append({
                'Failure Time': ft_str,
                'Approach': approach_label,
                'Stable Time': stable_time,
            })

    for failure_time in failure_times:

        if(failure_time == 10000.0):
            continue
        # Oracle and Baseline entries
        for label, data_source in (('Oracle', oracle), ('Baseline', baseline)):
            current = data_source["full"].sel(totalNodes=node, totalTaskFactor=task_factor, failureTimeAverage=failure_time)
            add_entries(failure_time, label, current)

        # Runtime entries for each communication value
        communications = [comm.item() for comm in runtime["mean"].communication]
        for communication in sorted(communications):
            extract_max_time_from_full(
                runtime["full"].sel(
                    totalNodes=node, totalTaskFactor=task_factor, failureTimeAverage=failure_time,
                    communication=communication, leaderBased=leader_based
                )
            )
            current = runtime["full"].sel(
                totalNodes=node, totalTaskFactor=task_factor, failureTimeAverage=failure_time, 
                communication=communication, leaderBased=leader_based
            )
            if(communication == 500.0):
                add_entries(failure_time, f'R=∞', current)
            else:
                add_entries(failure_time, f'R={communication}', current)

    df = pd.DataFrame(bar_data)
    
    ax = sns.barplot(
        data=df,
        x='Failure Time',
        y='Stable Time',
        hue='Approach',
        hue_order=['Oracle',  'R=∞', 'R=100.0', 'R=50.0', 'R=20.0','Baseline',],
        errorbar='ci',
        capsize=0.1,
        err_kws={'linewidth': 1, 'color': 'black'},
        palette='colorblind',
        ax=ax
    )

    #ax.set_yscale('log')
    #ax.set_ylim(600, 10000)

    # Apply compact layout modifications and larger font sizes
    ax.set_xlabel(ax.get_xlabel(), fontsize=16)
    ax.set_ylabel(ax.get_ylabel(), fontsize=16)
    ax.tick_params(axis='both', which='major', labelsize=14)
    
    # Adjust legend with larger fonts and a compact layout
    if ax.get_legend():
        leg = ax.legend(title='Approach', fontsize=14, title_fontsize=16)
        for t in leg.get_texts():
            t.set_fontsize(14)
    
def plot_runtime_replanning_count(runtime, node: int, task_factor: float, ax=None):
    # Set a larger font context and a compact style for the plot
    
    failure_times = [ft.item() for ft in runtime["mean"].failureTimeAverage]
    bar_data = []

    def add_entries(failure_time, comm_value, approach_type, data):
        replanning_counts = extract_max_replanning_from_full(data)
        ft_str = str(int(failure_time))
        for replanning_count in replanning_counts:
            bar_data.append({
                'Failure Time': ft_str,
                'Communication': comm_value,
                'Approach': approach_type,
                'Replanning Count': replanning_count,
            })

    for failure_time in failure_times:
        if(failure_time == 10000.0):
            continue
        # Runtime entries for each communication value
        communications = [comm.item() for comm in runtime["mean"].communication if comm.item() != 20]
        for communication in sorted(communications):
            # Leader-based
            leader_full = runtime["full"].sel(
                totalNodes=node, totalTaskFactor=task_factor, failureTimeAverage=failure_time, 
                communication=communication, leaderBased=True
            )
            leader_label = f'(L) R=∞' if communication == 500.0 else f'(L) R={communication}'
            add_entries(failure_time, leader_label, 'Leader', leader_full)
            
            # Gossip-based
            gossip_full = runtime["full"].sel(
                totalNodes=node, totalTaskFactor=task_factor, failureTimeAverage=failure_time, 
                communication=communication, leaderBased=False
            )
            gossip_label = f'(G) R=∞' if communication == 500.0 else f'(G) R={communication}'
            add_entries(failure_time, gossip_label, 'Gossip', gossip_full)

    df = pd.DataFrame(bar_data)
    
   
    ax = sns.barplot(
        data=df,
        x='Failure Time',
        y='Replanning Count',
        hue='Communication',
        errorbar='ci',
        capsize=0.1,
        err_kws={'linewidth': 1, 'color': 'black'},
        palette='colorblind',
        ax=ax
    )

    ax.set_yscale('log')
    # put y limit to max and min
    # min value 
    # Apply compact layout modifications and larger font sizes
    ax.set_xlabel(ax.get_xlabel(), fontsize=16)
    ax.set_ylabel(ax.get_ylabel(), fontsize=16)
    ax.tick_params(axis='both', which='major', labelsize=14)
    
    # Adjust legend with larger fonts and a compact layout
    if ax.get_legend():
        leg = ax.legend(title='Communication', fontsize=14, title_fontsize=16)
        for t in leg.get_texts():
            t.set_fontsize(14)
    

        
def create_grid_plot_base(plot_func, data_sources, x_label, y_label, plot_title, filename, plot_legend=True, extra_args=None):
    """
    Generic grid plotting function to reduce code duplication
    """
    if extra_args is None:
        extra_args = {}
    
    # Get dimensions from first data source
    first_data_key = list(data_sources.keys())[0]
    nodes = data_sources[first_data_key]["mean"].totalNodes
    task_factors = data_sources[first_data_key]["mean"].totalTaskFactor

    n_rows = len(nodes)
    n_cols = len(task_factors)
    # More rectangular aspect ratio with greater width compared to height
    fig, axes = plt.subplots(n_rows, n_cols, figsize=(n_cols * 6, n_rows * 3.5), sharex=True) 

    # Handle the case where axes might be 1D
    if n_rows == 1 and n_cols == 1:
        axes = np.array([[axes]])
    elif n_rows == 1:
        axes = axes.reshape(1, -1)
    elif n_cols == 1:
        axes = axes.reshape(-1, 1)

    # Loop through the grid and fill each subplot
    for i, node in enumerate(nodes):
        for j, task_factor in enumerate(task_factors):
            ax = axes[i, j]
            plot_func(
                *[data_sources[key] for key in data_sources],
                int(node.item()),
                float(task_factor.item()),
                ax=ax,
                **extra_args
            )
            
            # Add title to each subplot
            ax.set_title(f"Node {int(node.item())}, Task Factor {float(task_factor.item())}")
            
            # Only add x-label to bottom row
            if i == n_rows - 1:
                ax.set_xlabel(x_label)
            
            # Only add y-label to leftmost column, remove for others
            if j == 0:
                ax.set_ylabel(y_label)
            else:
                ax.set_ylabel("")  # Remove y-label for non-leftmost columns
            
            ax.grid(True)
            
            # Remove individual legends
            if ax.get_legend():
                ax.get_legend().remove()

    # Add a single legend for the entire figure
    handles, labels = axes[0, 0].get_legend_handles_labels()
    if (plot_legend):
        fig.legend(handles, labels, loc='lower center', bbox_to_anchor=(0.5, 0.06), ncol=4, frameon=True, fontsize=12)
        plt.tight_layout(rect=[0, 0.1, 1, 0.95])
    else:
        plt.tight_layout(rect=[0, 0.0, 1, 0.95])
    # Adjust layout with more spacing horizontally than vertically
    plt.subplots_adjust(wspace=0.25, hspace=0.35)
    plt.suptitle(plot_title, fontsize=22)

    # Create directory if it doesn't exist
    os.makedirs("charts", exist_ok=True)
    # Save the figure
    plt.savefig(f"charts/{filename}")
    plt.close()

def create_grid_plot_failure(leader_based, plot_title, filename, legend=True):
    create_grid_plot_base(
        plot_last_time_is_done_in_runtime_failure,
        {"oracle": data_loaded["oracle-random-failure"], 
         "baseline": data_loaded["baseline-random-failure"], 
         "runtime": data_loaded["runtime-random-failure"]},
        "Mean Failure Time",
        "Stable Time",
        plot_title,
        filename,
        legend,
        {"leader_based": leader_based}
    )

def create_bar_plot(plot_title, filename):
    create_grid_plot_base(
        plot_bar_plot_percentage_time,
        {"oracle": data_loaded["oracle-random-failure"], 
         "baseline": data_loaded["baseline-random-failure"], 
         "runtime": data_loaded["runtime-random-failure"]},
        "Communication",
        "Effectiveness Ratio",
        plot_title,
        filename
    )

create_grid_plot_failure(
    leader_based=False,
    plot_title="Stable time with different times between failures - Gossip", 
    filename="is_done_percentage_grid_gossip_failure.pdf",
    legend=False
)

create_grid_plot_failure(
    leader_based=True,
    plot_title="Stable time with different times between failures - Leader", 
    filename="is_done_percentage_grid_leader_failure.pdf"
)

create_grid_plot_base(
    plot_runtime_replanning_count,
    {"runtime": data_loaded["runtime-random-failure"]},
    "Mean Failure Time",
    "Replanning Count",
    "Replanning Count time with different times between failures",
    "replanning_count.png",
)
