import click
import re
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import cross_validate
from sklearn import preprocessing
from src.data.dataset_config import dataset_config
from src.data.constants.others import PROCESSED_DIR


@click.command()
@click.argument('dataset', type=str)
@click.argument('pipeline_name', type=str)
@click.argument('res', type=str, required=False)
def main(dataset, pipeline_name, res):
    df = pd.read_csv(
        f'{PROCESSED_DIR}/{dataset}/{pipeline_name}.csv', dtype={'piece': str},
        index_col='piece')

    target_col = dataset_config[dataset]['target_col']

    col_names = [col for col in df.columns]

    if res is not None:
        col_names = [col for col in col_names if res in col or 'hcdf' in col]

    X = df[col_names].values
    y = df[target_col].values

    scaler = preprocessing.StandardScaler().fit(X)

    X_scaled = scaler.transform(X)

    clf = LogisticRegression(max_iter=300)
    cv_results = cross_validate(clf, X_scaled, y, cv=10, return_estimator=True)

    coef = np.mean(
        [model.coef_[0] for model in cv_results['estimator']], axis=0
    )

    feature_importance = abs(coef)
    feature_importance = 100.0 * (
        feature_importance / feature_importance.max())
    feature_importance = np.array(feature_importance)

    sorted_idx = np.argsort(feature_importance)
    pos = np.arange(sorted_idx.shape[0]) * 2.5

    formatted_cols = _format_col_names(col_names)

    _, ax = plt.subplots()

    plt.rcParams.update({'font.size': 6})

    ax.set_yticks(pos)
    ax.set_yticklabels(np.array(formatted_cols)[sorted_idx], fontsize=8)

    ax.barh(pos, feature_importance[sorted_idx], align='center', height=1.5)
    plt.tight_layout()
    plt.show()


def _format_col_names(col_names):
    formatted = []

    for name in col_names:
        res = re.findall(r'\w+', name)
        feat = res[0]
        stat = res[1]

        formatted.append(f'{feat} ({stat})')

    return formatted


if __name__ == '__main__':
    main()
