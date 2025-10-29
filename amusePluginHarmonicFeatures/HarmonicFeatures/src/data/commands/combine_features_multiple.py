import click
import src.data.commands.combine_pipeline_output as combine_pipeline_output
from src.data.pipelines.catalogue import all_pipeline_combinations


@click.command()
@click.argument('dataset', type=str)
def main(dataset):
    pipelines = all_pipeline_combinations()

    for p in pipelines:
        if len(p) > 1:
            combine_pipeline_output.execute(dataset, p)

            print(f'Combined features: {p}')


if __name__ == '__main__':
    main()
