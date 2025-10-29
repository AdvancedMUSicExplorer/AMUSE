import click
import src.models.weiss as weiss
from src.data.pipelines.catalogue import all_pipeline_combinations


@click.command()
@click.argument('dataset', type=str)
def main(dataset):
    for pipeline_comb in all_pipeline_combinations():
        pipeline_name = '_'.join(pipeline_comb)
        weiss.execute(dataset, pipeline_name)


if __name__ == '__main__':
    main()
