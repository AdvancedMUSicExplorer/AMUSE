import click
import pathlib
import pandas as pd
from src.features.chroma_resolution import ChromaResolution
from src.data.pipelines.feature_pipeline import FeaturePipeline
from src.utils.formatters import format_chroma_resolution
from src.data.pipelines.catalogue import res_pipelines
from src.data.constants.others import EXTERNAL_DIR, INTERIM_DIR


@click.command()
@click.argument("dataset", type=str)
@click.argument("pipeline_name", type=str)
def main(dataset, pipeline_name):
    RESOLUTIONS = [0.1, 0.5, 10, ChromaResolution.GLOBAL]

    execute(dataset, pipeline_name, RESOLUTIONS, res_pipelines)


def execute(
    dataset: str, pipeline_name: str,
    resolutions: list[float], catalogue: dict[str, FeaturePipeline]
):

    input_filepath = EXTERNAL_DIR
    output_filepath = f'{INTERIM_DIR}/{dataset}'

    pathlib.Path(output_filepath).mkdir(exist_ok=True)

    print(
        f'Processing resolution features for pipeline: {pipeline_name}'
    )

    data = pd.read_csv(f'{input_filepath}/{dataset}.csv', dtype={"piece": str})
    data = data.fillna(method="ffill")

    for res in resolutions:
        pipeline = catalogue[pipeline_name](res)

        processed = pipeline.run(data)

        formatted_res = format_chroma_resolution(res)

        outfile = f'{output_filepath}/{pipeline_name}__{formatted_res}.csv'
        processed.to_csv(outfile)
        print(f'Wrote {outfile}')

    files = list(
        pathlib.Path(output_filepath).glob(f"{pipeline_name}_*")
    )

    joined = join_datasets(files)
    joined.to_csv(f'{output_filepath}/{pipeline_name}.csv')

    cleanup_output_dir(output_filepath)


def join_datasets(files: list[str]):
    joined = None

    for f in files:
        data = pd.read_csv(f, dtype={"piece": str}, index_col="piece")

        resolution = f.name.split("__")[-1]
        resolution = resolution.replace(".csv", "")

        data = data.add_suffix(f"_{resolution}")

        if joined is None:
            joined = data.copy()
        else:
            joined = joined.join(data)

    return joined


def cleanup_output_dir(output_dir):
    path = pathlib.Path(output_dir).glob('*__*')

    for file in path:
        file.unlink()


if __name__ == "__main__":
    main()
