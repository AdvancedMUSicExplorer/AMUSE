import click
import src.data.commands.make_single_res_feats as make_single_res_feats
from src.data.pipelines.catalogue import single_res_pipelines


@click.command()
@click.argument('dataset', type=str)
def main(dataset):
    for pipeline_name in single_res_pipelines.keys():
        make_single_res_feats.execute(dataset, pipeline_name)


if __name__ == '__main__':
    main()
