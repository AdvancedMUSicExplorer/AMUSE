import click
import pathlib
from src.data.constants.others import INTERIM_DIR
import src.data.commands.make_trainset as make_trainset


@click.command()
@click.argument('dataset', type=str)
def main(dataset):
    input_filepath = f'{INTERIM_DIR}/{dataset}'

    for path in pathlib.Path(input_filepath).iterdir():
        if path.is_file():
            make_trainset.execute(dataset, path.stem)


if __name__ == '__main__':
    main()
