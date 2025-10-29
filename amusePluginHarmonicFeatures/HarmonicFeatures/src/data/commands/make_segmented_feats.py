import click
import pathlib
import pandas as pd
from src.data.pipelines.catalogue import segmented_pipelines
from src.data.constants.others import INTERIM_DIR, HCDF_SEGMENTED_DIR


@click.command()
@click.argument('dataset', type=str)
@click.argument("pipeline_name", type=str)
def main(dataset, pipeline_name):
    execute(dataset, pipeline_name)


def execute(dataset, pipeline_name):
    input_filepath = HCDF_SEGMENTED_DIR
    output_filepath = f'{INTERIM_DIR}/{dataset}'

    pathlib.Path(output_filepath).mkdir(exist_ok=True)

    print(
        f'Processing segmented features for pipeline: {pipeline_name}'
    )

    data = pd.read_csv(f'{input_filepath}/{dataset}.csv', dtype={'piece': str})
    data = data.fillna(method='ffill')
    data = data.set_index(['piece', 'time'])

    pipeline = segmented_pipelines[pipeline_name]()

    processed = pipeline.run(data)

    outfile = f'{output_filepath}/{pipeline_name}.csv'

    processed.to_csv(outfile)

    print(f'Wrote {outfile}')


if __name__ == '__main__':
    main()
