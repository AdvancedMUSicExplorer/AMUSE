import click
import librosa
import vamp
import pathlib
import pandas as pd
from src.data.constants.others import AUDIO_DIR, EXTERNAL_DIR
from src.data.constants.feature_groups import CHROMA_FEATS


@click.command()
@click.argument('dataset', type=str)
def main(dataset):
    output_filepath = f'{EXTERNAL_DIR}/{dataset}'

    df = execute(dataset)

    df.to_csv(f'{output_filepath}/{dataset}.csv')

    print(df)


def execute(dataset):
    input_filepath = f'{AUDIO_DIR}/{dataset}'

    COL_NAMES = ['piece', 'time']
    COL_NAMES.extend(CHROMA_FEATS)

    rows = []

    for path in pathlib.Path(input_filepath).iterdir():
        if path.is_file():
            piece_rows = extract(path)
            rows.extend(piece_rows)

    df = pd.DataFrame(rows, columns=COL_NAMES)
    df = df.set_index(['piece', 'time'])

    return df


def extract(filepath):
    y, sr = librosa.load(filepath)

    chroma = _get_nnls(y, sr, 16384, 2048)

    rows = []

    for i, entry in enumerate(chroma):
        time, chroma_vector = entry
        # TODO: Fix resolution
        # data = {'piece': filepath.stem, 'time': (i+1) * RESOLUTION}
        data = {'piece': filepath.stem, 'time': float(time)}

        for j, c in enumerate(chroma_vector):
            data[CHROMA_FEATS[j]] = c

        rows.append(data)

    return rows


def _get_nnls(samples, sr, block_size, off):
    """
        returns nnls chromagram
        Parameters
        ----------
        y : number > 0 [scalar]
            audio
        sr: number > 0 [scalar]
            chroma-samplerate-framesize-overlap
        fr: number [scalar]
            frame size of windos
        off: number [scalar]
            overlap
        Returns
        -------
        list of chromagrams
    """
    plugin = 'nnls-chroma:nnls-chroma'
    chroma = list(vamp.process_audio(
        samples,
        sr,
        plugin,
        output='chroma',
        block_size=block_size,
        step_size=off
    ))
    vectors = []

    for c in chroma:
        chroma_vector = c['values'].tolist()
        vectors.append((c['timestamp'], chroma_vector))

    return vectors


if __name__ == "__main__":
    main()
