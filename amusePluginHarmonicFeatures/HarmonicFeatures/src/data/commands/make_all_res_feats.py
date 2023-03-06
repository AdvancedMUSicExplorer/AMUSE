import click
import src.data.commands.make_res_feats as make_res_feats
from src.data.pipelines.catalogue import res_pipelines
from src.features.chroma_resolution import ChromaResolution


@click.command()
@click.argument('dataset', type=str)
def main(dataset):
    RESOLUTIONS = [0.1, 0.5, 10, ChromaResolution.GLOBAL]

    for pipeline_name in res_pipelines.keys():
        make_res_feats.execute(
            dataset, pipeline_name, RESOLUTIONS, res_pipelines
        )


if __name__ == '__main__':
    main()
