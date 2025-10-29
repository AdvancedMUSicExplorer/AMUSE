import click
import pathlib
import csv
import pandas as pd
import numpy as np
from matplotlib import pyplot as plt
from lib.HCDF import HCDF
import src.tools.extract_chroma as extract_chroma
from src.data.constants.feature_groups import CHROMA_FEATS


@click.command()
@click.argument('filepath', type=click.Path(exists=True))
def main(filepath):
    CHROMA_RESOLUTION = 0.1

    COL_NAMES = ['piece', 'time']
    COL_NAMES.extend(CHROMA_FEATS)

    path = pathlib.Path(filepath)

    data = extract_chroma.extract(path)
    df = pd.DataFrame(data, columns=COL_NAMES)
    # df = df.set_index(['piece', 'time'])

    peak_indexes, peak_mags, hcdf = HCDF.harmonic_change(
        df[CHROMA_FEATS].values
    )

    fig, ax = plt.subplots()
    fig.canvas.set_window_title(f'HCDF - {path.stem}')

    ax.set(xlabel='Time (s)', ylabel='HCDF magnitude')

    hcdf_x = np.arange(0, hcdf.size) * CHROMA_RESOLUTION
    ax.plot(hcdf_x, hcdf, label='hcdf values')

    peak_indexes_x = df.iloc[peak_indexes]['time'].values
    print(peak_indexes_x)
    ax.scatter(
        peak_indexes_x, peak_mags, label='hcdf peaks', color='red', s=10
    )

    ax.xaxis.set_ticks(np.arange(min(hcdf_x), max(hcdf_x) + 1, 5))
    ax.grid(ls='dashed')

    with open('hcdf.csv', 'w', newline='') as outfile:
        writer = csv.writer(outfile)

        for i in range(peak_indexes.size):
            writer.writerow([peak_indexes_x[i], peak_mags[i]])

    plt.show()


if __name__ == '__main__':
    main()
