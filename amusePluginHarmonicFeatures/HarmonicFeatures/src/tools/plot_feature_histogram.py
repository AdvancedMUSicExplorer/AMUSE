import click
import pandas as pd
from matplotlib import pyplot as plt


@click.command()
@click.argument('filepath', type=click.Path(exists=True))
@click.argument('feature', type=str)
def main(filepath, feature):
    df = pd.read_csv(filepath, dtype={'piece': str})
    df = df.fillna(method='ffill')

    fig, ax = plt.subplots()

    ax.set(xlabel=feature, ylabel='Frequency')

    ax.hist(df[feature].values)

    plt.show()


if __name__ == '__main__':
    main()
