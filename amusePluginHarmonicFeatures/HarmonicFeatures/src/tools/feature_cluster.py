import click
import re
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from scipy.stats import spearmanr
from scipy.cluster import hierarchy
from scipy.spatial.distance import squareform
from src.data.constants.others import INTERIM_DIR


@click.command()
@click.argument('dataset', type=str)
@click.argument('pipeline_name', type=str)
@click.option('--stat', type=str)
@click.option('--res', type=str)
def main(dataset, pipeline_name, stat, res):
    df = pd.read_csv(
        f'{INTERIM_DIR}/{dataset}/{pipeline_name}.csv', dtype={'piece': str},
        index_col='piece')

    col_names = [col for col in df.columns]

    if stat is not None:
        col_names = [col for col in col_names if stat in col]

    if res is not None:
        col_names = [col for col in col_names if res in col or 'hcdf' in col]

    X = df[col_names].values

    fig, ax = plt.subplots(figsize=(12, 8))

    corr = spearmanr(X).correlation
    corr = np.nan_to_num(corr)
    corr = (corr + corr.T) / 2
    np.fill_diagonal(corr, 1)

    distance_matrix = 1 - np.abs(corr)
    dist_linkage = hierarchy.ward(squareform(distance_matrix))

    hierarchy.dendrogram(
        dist_linkage,
        color_threshold=0.75*np.max(dist_linkage[:, 2]),
        labels=_format_col_names(col_names),
        ax=ax,
        # leaf_rotation=90,
        orientation='right'
    )

    fig.tight_layout()
    plt.show()


def _format_col_names(col_names):
    formatted = []

    for name in col_names:
        res = re.findall(r'\w+', name)
        feat = res[0]
        stat = res[1]

        formatted.append(f'{feat} ({stat})')
        # formatted.append(f'{feat}')

    return formatted


if __name__ == '__main__':
    main()
