import click
import pandas as pd
from matplotlib import pyplot as plt
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

    tivs = TIVCollection.from_pcp(piece_df[CHROMA_FEATS].values.T)

    piece_tiv = TIV.from_pcp(piece_df[CHROMA_FEATS].sum().values)

    _, axs = plt.subplots(2, 3)

    titles = [
        'Chromaticity',
        'Dyadicity',
        'Triadicity',
        'Diminished Quality',
        'Diatonicity',
        'Whole-toneness'
    ]

    for i, ax in enumerate(axs.flat):
        ax.set_xlim(-1.5, 1.5)
        ax.set_ylim(-1.5, 1.5)
        ax.set_aspect('equal', adjustable='box')
        ax.grid()

        frame_coefs = tivs.vectors[:, i] / TIV.weights[i]

        piece_coef = piece_tiv.vector[i] / TIV.weights[i]

        ax.scatter(frame_coefs.real, frame_coefs.imag, s=5, alpha=0.1)
        ax.scatter(piece_coef.real, piece_coef.imag, s=20, c='red')

        circle = plt.Circle((0, 0), 1, fill=False)
        ax.add_patch(circle)

        ax.set_title(titles[i])

    plt.show()


if __name__ == '__main__':
    main()
