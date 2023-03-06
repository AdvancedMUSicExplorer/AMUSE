import click
import pathlib
from src.data.pipeline_output_combiner import PipelineOutputCombiner
from src.data.constants.others import INTERIM_DIR


@click.command()
@click.argument('dataset', type=str)
@click.argument('pipelines', type=str, nargs=-1)
def main(dataset, pipelines):
    execute(dataset, pipelines)


def execute(dataset, pipelines):
    pipelines_str = '_'.join(pipelines)

    data = PipelineOutputCombiner.combine(dataset, pipelines, 'piece')

    output_dir = f'{INTERIM_DIR}/{dataset}'
    pathlib.Path(output_dir).mkdir(exist_ok=True)

    data.to_csv(f'{output_dir}/{pipelines_str}.csv')


if __name__ == '__main__':
    main()
