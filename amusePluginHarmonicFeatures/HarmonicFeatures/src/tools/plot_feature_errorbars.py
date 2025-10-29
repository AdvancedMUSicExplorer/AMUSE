import click
import re
import pandas as pd
from matplotlib import pyplot as plt
from src.data.dataset_config import dataset_config
from src.data.constants.others import PROCESSED_DIR


@click.command()
@click.argument('dataset', type=str)
@click.argument('pipeline_name', type=str)
def main(dataset, pipeline_name):
    input_filepath = f'{PROCESSED_DIR}/{dataset}/{pipeline_name}'

    df = pd.read_csv(
            f'{input_filepath}.csv', dtype={'piece': str})

    target_col = dataset_config[dataset]['target_col']
    classes = dataset_config[dataset]['classes']

    df[target_col] = pd.Categorical(df[target_col], classes)
    df = df.sort_values(by=[target_col, 'piece'])
    df = df.set_index('piece')

    count_per_class = df[target_col].value_counts().values

    # xticks = list(range(0, len(df.index), count_per_class[0]))
    xticks = _make_xticks(count_per_class)

    fig, ax = plt.subplots()

    plt.xticks(xticks, classes, rotation=-45)

    col_names = [col for col in df.columns if '100ms' in col and 'mean' in col]
    # col_names = [col for col in df.columns if 'hcdf_peak_mag' in col and 'mean' in col]

    dfs_by_class = _split_dfs_by_class(df, target_col, classes)

    # _plot_harm_rhythm(ax, xticks, dfs_by_class)

    for j, c in enumerate(col_names):
        errors = []
        x = []
        y = []
        for i, class_data in enumerate(dfs_by_class):
            std = class_data[c].std()
            mean = class_data[c].mean()
            errors.append(std)
            index = xticks[i]
            x.append(index)
            y.append(mean)

        eb = ax.errorbar(x, y, errors, marker='.', label=_format_label(c), alpha=0.7)

    ax.legend()
    plt.show()


def _make_xticks(count_per_class):
    xticks = [0]

    for i in range(len(count_per_class) - 1):
        count = count_per_class[i]
        xticks.append(xticks[i] + count)

    return xticks


def _split_dfs_by_class(df, target_col, classes):
    dfs_by_class = []

    for class_name in classes:
        data = df.loc[df[target_col] == class_name]
        dfs_by_class.append(data)

    return dfs_by_class


def _format_label(label):
    res = re.search(r'\w+', label).group(0)

    return res


def _plot_harm_rhythm(ax, xticks, dfs_by_class):
    col_names = ["('hcdf_peak_interval', 'mean')", "('hcdf_peak_mag', 'mean')"]

    ax2 = ax.twinx()

    for c in col_names:
        errors = []
        x = []
        y = []
        for i, class_data in enumerate(dfs_by_class):
            std = class_data[c].std()
            mean = class_data[c].mean()
            errors.append(std)
            index = xticks[i]
            x.append(index)
            y.append(mean)

        if c == "('hcdf_peak_interval', 'mean')":
            ax.errorbar(x, y, errors, marker='.', label=_format_label(c), linewidth=2, color='blue')
        else:
            eb2 = ax2.errorbar(x, y, errors, marker='.', label=_format_label(c), linewidth=2, color='red', linestyle='--')
            eb2[-1][0].set_linestyle('--')

    lines, labels = ax.get_legend_handles_labels()
    lines2, labels2 = ax2.get_legend_handles_labels()
    ax2.legend(lines + lines2, labels + labels2, loc=0)

    plt.show()


if __name__ == '__main__':
    main()
