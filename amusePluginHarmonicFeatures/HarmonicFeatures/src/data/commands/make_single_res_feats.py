import click
import src.data.commands.make_res_feats as make_res_feats
from src.data.pipelines.catalogue import single_res_pipelines
from src.data.pipelines.feature_pipeline import FeaturePipeline


@click.command()
@click.argument('dataset', type=str)
@click.argument('pipeline_name', type=str)
def main(dataset, pipeline_name):
    execute(dataset, pipeline_name)


def execute(dataset: str, pipeline_name: str):
    _, resolution = single_res_pipelines[pipeline_name]
    catalogue = _format_catalogue(single_res_pipelines)

    make_res_feats.execute(
        dataset, pipeline_name, [resolution], catalogue
    )


def _format_catalogue(
    catalogue: dict[str, (FeaturePipeline, float)]
) -> dict[str, FeaturePipeline]:
    formatted_catalogue = {}

    for pipeline_name, value in catalogue.items():
        pipeline, _ = value
        formatted_catalogue[pipeline_name] = pipeline

    return formatted_catalogue


if __name__ == '__main__':
    main()
