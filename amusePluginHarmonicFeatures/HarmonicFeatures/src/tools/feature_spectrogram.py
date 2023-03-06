import click
import pandas as pd
import numpy as np
from matplotlib import pyplot as plt
from src.data.constants.others import EXTERNAL_DIR, HCDF_SEGMENTED_DIR
from src.features.tis_vertical import TISVertical
from src.features.chroma_resolution import ChromaResolution
from src.data.constants.feature_groups import TIS_BASIC_FEATS


@click.command()
@click.argument('dataset', type=str)
@click.argument('piece', type=str)
@click.argument('res', type=float)
def main(dataset, piece, res):
    # harmonic
    if res == 0:
        input_filepath = f'{HCDF_SEGMENTED_DIR}/{dataset}.csv'
    else:
        input_filepath = f'{EXTERNAL_DIR}/{dataset}.csv'

    df = pd.read_csv(input_filepath, dtype={'piece': str})
    df = df.fillna(method='ffill')

    if res != 0:
        df = ChromaResolution(res).run(df)
        df = df.reset_index()
        df['time'] = df['time'].dt.total_seconds()

    desired_duration_seconds = 30

    piece_time_labels = df.loc[df['piece'] == piece, 'time'].values
    last_time_idx = np.argmax(piece_time_labels >= desired_duration_seconds)

    feats = TISVertical().run(df)

    df = df.join(feats, rsuffix='_')
    df = df.loc[df['piece'] == piece]
    print(df)

    offset_frames = int(len(df.index) * 0.2)
    last_time_idx = np.argmax(df['time'].values >= desired_duration_seconds)

    print(offset_frames)
    print(last_time_idx)

    fig, ax = plt.subplots()

    feats = df[TIS_BASIC_FEATS].values[
        offset_frames:offset_frames + last_time_idx].T

    im = ax.imshow(
        feats, cmap='gray_r', origin='lower', aspect='auto',
        extent=[0, desired_duration_seconds, 0, len(TIS_BASIC_FEATS)]
    )

    y_ticks = np.arange(len(TIS_BASIC_FEATS)) + 0.5
    ax.set_yticks(y_ticks)

    ax.set_yticklabels(TIS_BASIC_FEATS)
    ax.set_xlabel('Time (s)')
    ax.set_ylabel('Feature')

    fig.colorbar(im)

    plt.show()

if __name__ == '__main__':
    main()
