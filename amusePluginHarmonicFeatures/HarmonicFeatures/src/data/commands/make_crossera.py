import click
import pathlib
import pandas as pd
import numpy as np
from numpy.typing import ArrayLike
from src.data.constants.feature_groups import CHROMA_FEATS


@click.command()
@click.argument("input_filepath", type=click.Path(exists=True))
@click.argument("output_filepath", type=click.Path())
def main(input_filepath, output_filepath):  # pragma no cover
    COL_NAMES = ["piece", "time"]
    COL_NAMES.extend(CHROMA_FEATS)

    glob_patterns = ["chroma-nnls_piano*", "chroma-nnls_orchestra*"]
    file_names = [
        "chroma-nnls_piano.csv",
        "chroma-nnls_orchestra.csv",
        "chroma-nnls_full.csv",
    ]
    datasets = make_datasets(
        glob_patterns,
        file_names,
        input_filepath,
        COL_NAMES
    )

    for file, df in datasets:
        path = f"{output_filepath}/{file}"
        df.to_csv(path, index=False)
        print(f"Wrote {path}")


def make_datasets(
    glob_patterns: list[str], file_names: list[str], input_filepath,
    columns: list[str]
):
    p = pathlib.Path(input_filepath)

    datasets = []

    for i, pattern in enumerate(glob_patterns):
        files = list(p.glob(pattern))

        data = join_datasets(files, columns)

        datasets.append(data)

    full = np.empty((0, len(columns)))

    for i, data in enumerate(datasets):
        full = np.append(full, data, axis=0)

        df = pd.DataFrame(data, columns=columns)
        # Numpy sets the dtype of the chroma columns as object
        # because of the "piece" column
        # so we force dtype float64
        df[CHROMA_FEATS] = df[CHROMA_FEATS].astype(np.float64)

        datasets[i] = (file_names[i], df)

    full = pd.DataFrame(full, columns=columns)
    full[CHROMA_FEATS] = full[CHROMA_FEATS].astype(np.float64)

    datasets.append((file_names[-1], full))

    return datasets


def join_datasets(files: list[str], columns: list[str]) -> ArrayLike:
    joined = np.empty((0, len(columns)))

    for f in files:
        data = pd.read_csv(f, header=None, names=columns, dtype={"piece": str})

        joined = np.append(joined, data.to_numpy(), axis=0)

    return joined


if __name__ == "__main__":
    main()  # pragma: no cover
