import click
import pandas as pd
import numpy as np
from matplotlib import pyplot as plt
from sklearn.manifold import MDS
from TIVlib import TIV, TIVCollection
from src.data.constants.others import EXTERNAL_DIR
from src.data.constants.feature_groups import CHROMA_FEATS


@click.command()
@click.argument('dataset', type=str)
@click.argument('piece', type=str)
def main(dataset, piece):
    df = pd.read_csv(f'{EXTERNAL_DIR}/{dataset}.csv', dtype={"piece": str})
    df = df.fillna(method='ffill')
    df = df.set_index(['piece', 'time'])

    piece_df = df.loc[piece]

    mean_chroma_vector = np.mean(piece_df[CHROMA_FEATS].values, axis=0)
    tonal_center = TIVCollection.from_pcp(mean_chroma_vector)

    tivs = TIVCollection.from_pcp(piece_df[CHROMA_FEATS].values.T)

    _, ax = plt.subplots()

    ax.set_xlim(-2, 2)
    ax.set_ylim(-2, 2)

    # Append tonal center to compute MDS
    tivs = np.append(tivs.vectors, tonal_center.vectors, axis=0)
    x, y = _apply_mds(tivs)

    ax.scatter(x[:-1], y[:-1], alpha=0.1)
    ax.scatter(x[-1], y[-1], c='red')

    plt.show()


def _apply_mds(tivs):
    expanded_tivs = np.apply_along_axis(_expand_tivs, 1, tivs)

    reduced = MDS(n_components=2).fit_transform(expanded_tivs)
    x = reduced[:, 0]
    y = reduced[:, 1]

    return x, y


def _expand_tivs(tiv):
    weighted_tiv = tiv / TIV.weights

    real = weighted_tiv.real
    im = weighted_tiv.imag

    expanded = np.empty((real.size + im.size, ), dtype=real.dtype)
    expanded[0::2] = real
    expanded[1::2] = im

    return expanded


if __name__ == '__main__':
    main()
