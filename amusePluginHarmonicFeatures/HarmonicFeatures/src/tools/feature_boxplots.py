import click
import pandas as pd
from matplotlib import pyplot as plt
from src.data.dataset_config import dataset_config
from src.data.constants.others import PROCESSED_DIR


@click.command()
@click.argument('dataset', type=str)
@click.argument('pipeline_name', type=str)
@click.argument('feature', type=str)
@click.argument('stat', type=str)
@click.argument('res', type=str, required=False)
def main(dataset, pipeline_name, feature, stat, res):
    input_filepath = f'{PROCESSED_DIR}/{dataset}/{pipeline_name}'

    df = pd.read_csv(
            f'{input_filepath}.csv', dtype={'piece': str})

    target_col = dataset_config[dataset]['target_col']
    classes = dataset_config[dataset]['classes']

    df[target_col] = pd.Categorical(df[target_col], classes)
    df = df.sort_values(by=[target_col, 'piece'])
    df = df.set_index('piece')

    _, ax = plt.subplots()

    ax.set_xticklabels(classes)

    col_names = [col for col in df.columns if feature in col and stat in col]

    if res is not None:
        col_names = [col for col in col_names if res in col]

    dfs_by_class = _split_dfs_by_class(df, target_col, classes)

    for c in col_names:
        y = []
        for class_data in dfs_by_class:
            y.append(class_data[c].values)

        ax.boxplot(y)

    plt.show()


def _split_dfs_by_class(df, target_col, classes):
    dfs_by_class = []

    for class_name in classes:
        data = df.loc[df[target_col] == class_name]
        dfs_by_class.append(data)

    return dfs_by_class


if __name__ == '__main__':
    main()
