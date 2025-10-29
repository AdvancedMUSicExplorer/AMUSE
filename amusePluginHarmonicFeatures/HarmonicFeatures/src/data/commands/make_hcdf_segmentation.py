import click
import pathlib
import pandas as pd
from src.data.pipelines.pipeline import Pipeline
from src.features.hcdf_segmentation import HCDFSegmentation
from src.data.constants.others import EXTERNAL_DIR, HCDF_SEGMENTED_DIR


@click.command()
@click.argument('dataset', type=str)
def main(dataset):
    input_filepath = EXTERNAL_DIR
    output_filepath = HCDF_SEGMENTED_DIR

    pathlib.Path(output_filepath).mkdir(exist_ok=True)

    data = pd.read_csv(
        f'{input_filepath}/{dataset}.csv', dtype={'piece': str}
    )
    data = data.fillna(method='ffill')
    data = data.set_index(['piece', 'time'])

    pipeline = make_pipeline()

    processed = pipeline.run(data)

    processed.to_csv(f'{output_filepath}/{dataset}.csv')


def make_pipeline():
    pipeline = Pipeline()

    pipeline.add_task(HCDFSegmentation())

    return pipeline


if __name__ == '__main__':
    main()
