import click
from joblib import load
import pandas as pd
from sklearn.metrics import accuracy_score
from src.data.constants.others import MODELS_DIR, PROCESSED_DIR
from src.data.dataset_config import dataset_config


@click.command()
@click.argument('dataset', type=str)
@click.argument('pipeline_name', type=str)
@click.argument('test_set', type=str)
def main(dataset, pipeline_name, test_set):
    model_path = f'{MODELS_DIR}/{dataset}/{pipeline_name}'

    df = pd.read_csv(
        f'{PROCESSED_DIR}/{test_set}/{pipeline_name}.csv',
        dtype={"piece": str},
        index_col="piece"
    )

    target_col = dataset_config[dataset]['target_col']

    y_true = df[target_col].values
    X_test = df.drop(target_col, axis=1).values

    clf = load(model_path)

    y_pred = clf.predict(X_test)

    acc = accuracy_score(y_true, y_pred)

    print(f'Accuracy: {acc}')


if __name__ == '__main__':
    main()
