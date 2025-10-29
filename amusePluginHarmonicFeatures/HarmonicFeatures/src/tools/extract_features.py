import click
import pandas as pd
import numpy as np
import pathlib
import csv
import sys
import src.tools.extract_chroma as extract_chroma
from src.data.constants.feature_groups import CHROMA_FEATS
from src.features.tis_vertical import TISVertical
from src.features.tis_horizontal import TISHorizontal
from src.features.harm_rhythm import HarmRhythm


#@click.command()
#@click.argument('filepath', type=str)
#@click.argument('features', type=str, nargs=-1)
def main(filepath, features):
    COL_NAMES = ['piece', 'time']
    COL_NAMES.extend(CHROMA_FEATS)
    path = pathlib.Path(filepath)

    data = extract_chroma.extract(path)
    df = pd.DataFrame(data, columns=COL_NAMES)
    df = df.set_index(['piece', 'time'])

    piece = path.stem

    df = TISVertical().run(df)
    
    piece_df = df.loc[piece]

    #with open('extract.arff', 'w', newline='') as outfile:
    #    writer = csv.writer(outfile)
    
    for f in features:
        values = np.array((piece_df.index.values, piece_df[f].values))
    #        writer.writerow([f'-----{f}-----'])

    #        for time, feature_value in values:
    #            writer.writerow([time, feature_value])

    return values

if __name__ == '__main__':
    main()
