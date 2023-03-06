import click
import pathlib
import pandas as pd
import numpy as np
import csv
from joblib import dump
import matplotlib.pyplot as plt
from sklearn import svm
from sklearn.discriminant_analysis import LinearDiscriminantAnalysis
from sklearn.model_selection import (
    GridSearchCV, StratifiedKFold, StratifiedGroupKFold
)
from sklearn.metrics import (
    confusion_matrix, ConfusionMatrixDisplay, accuracy_score
)
from src.data.constants.others import (
    ANNOTATIONS_DIR, PROCESSED_DIR, TRAINOUT_DIR, CONFUSION_MATRICES_DIR
)
from src.data.dataset_config import dataset_config
from src.data.constants.others import MODELS_DIR


@click.command()
@click.argument('dataset', type=str)
@click.argument('pipeline_name', type=str)
@click.option('--composer-filter', '-cf', is_flag=True)
def main(dataset, pipeline_name, composer_filter):
    execute(dataset, pipeline_name, composer_filter)


def execute(dataset: str, pipeline_name: str, composer_filter: bool):
    # Set random seed for reproducibility
    np.random.seed(42)

    input_filepath = f'{PROCESSED_DIR}/{dataset}/{pipeline_name}'

    target_col = dataset_config[dataset]['target_col']
    classes = dataset_config[dataset]['classes']
    n_classes = len(classes)

    print('-' * 75)
    print(f'Dataset -> {dataset}')
    print(f'Pipeline -> {pipeline_name}')
    print('-' * 75)

    trainout_filepath = (
        f'{TRAINOUT_DIR}/{dataset}_'
        f'{"filter_" if composer_filter else ""}'
        'trainout.csv'
    )

    with open(trainout_filepath, 'a', newline='') as trainout_file:
        writer = csv.writer(trainout_file)

        data = pd.read_csv(
            f'{input_filepath}.csv', dtype={'piece': str}, index_col='piece')

        X = data.drop(target_col, axis=1).values
        y = data[target_col].values

        overall_mean_acc = 0
        run_mean_accuracy_values = []
        inter_fold_dev = 0
        inter_class_dev = 0

        runs = 10
        curr_run = 0
        n_splits = 3

        while curr_run < runs:
            print(f'Run {curr_run}')

            fold_mean_accuracy_values = []
            inter_class_dev_run_values = []

            split = _get_cv_split(X, y, composer_filter, dataset, n_splits)
            indexes = _check_cv_split(X, y, split, n_classes)

            if indexes is None:
                # invalid run
                print(
                    f'Imbalanced CV split in run {curr_run}.'
                    'Re-initializing folds.')
                continue

            for split in indexes:
                train_index, test_index = split

                X_train, X_test = X[train_index], X[test_index]
                y_train, y_test = y[train_index], y[test_index]

                X_train_transformed, X_test_transformed = lda_transform(
                    X_train, y_train, X_test
                )

                clf = train_classifier(X_train_transformed, y_train)

                y_pred = clf.predict(X_test_transformed)
                acc = accuracy_score(y_test, y_pred)

                conf_mat = confusion_matrix(y_test, y_pred)
                class_accuracy = conf_mat.diagonal() / conf_mat.sum(axis=1)

                # inter_class_dev += np.std(class_accuracy)
                run_inter_class_dev = np.std(class_accuracy)
                inter_class_dev_run_values.append(run_inter_class_dev)

                # overall_mean_acc += acc
                fold_mean_accuracy_values.append(acc)

                print(clf.best_params_)
                print(acc)

            overall_mean_acc = np.sum(fold_mean_accuracy_values)

            mean_run_accuracy = np.mean(fold_mean_accuracy_values)
            run_mean_accuracy_values.append(mean_run_accuracy)

            inter_fold_dev += np.std(fold_mean_accuracy_values)

            inter_class_dev += np.sum(inter_class_dev_run_values)

            curr_run += 1

        inter_run_dev = np.std(run_mean_accuracy_values)
        inter_fold_dev /= runs
        inter_class_dev /= (runs * n_splits)

        overall_mean_acc = np.mean(run_mean_accuracy_values)

        print(f'Mean Accuracy: {overall_mean_acc}')
        print(f'Inter-run Deviation: {inter_run_dev}')
        print(f'Inter-fold Deviation: {inter_fold_dev}')
        print(f'Inter-class Deviation: {inter_class_dev}')

        writer.writerow([
            pipeline_name,
            overall_mean_acc,
            inter_run_dev,
            inter_fold_dev,
            inter_class_dev
        ])

        _create_conf_matrix(
            clf,
            X_test_transformed,
            y_test,
            f'{CONFUSION_MATRICES_DIR}/{dataset}',
            pipeline_name,
            composer_filter,
            classes
        )

        _save_model(clf, dataset, pipeline_name, composer_filter)

    print('-' * 75)


def _get_cv_split(X, y, composer_filter, dataset, n_splits):
    split_cv = None
    groups = None

    if composer_filter:
        filter_col = dataset_config[dataset]['filter_col']

        split_cv = StratifiedGroupKFold(n_splits=n_splits, shuffle=True)

        annotations = pd.read_csv(f'{ANNOTATIONS_DIR}/{dataset}.csv')
        # Trim groups to exclude addons
        groups = annotations[filter_col].values[:X.shape[0]]
    else:
        split_cv = StratifiedKFold(n_splits=n_splits, shuffle=True)

    split = split_cv.split(X, y, groups)

    return split


def _check_cv_split(X, y, split, n_classes):
    indexes = []

    # pre-check CV split to ensure all classes are represented
    for train_index, test_index in split:
        y_train, y_test = y[train_index], y[test_index]

        y_train_classes, _ = np.unique(y_train, return_counts=True)
        y_test_classes, _ = np.unique(y_test, return_counts=True)

        if (
            y_train_classes.size < n_classes or
            y_test_classes.size < n_classes
        ):
            return None

        indexes.append((train_index, test_index))

    return indexes


def lda_transform(X_train, y_train, X_test):
    lda = LinearDiscriminantAnalysis()
    lda.fit(X_train, y_train)

    X_train_transformed = lda.transform(X_train)
    X_test_transformed = lda.transform(X_test)

    return X_train_transformed, X_test_transformed


def train_classifier(X_train, y_train):
    c = [2 ** x for x in range(-5, 17, 2)]
    gamma = [2 ** x for x in range(-15, 5, 2)]

    search_params = {
        'C': c,
    }

    clf = svm.SVC(kernel='linear')

    gs_cv = StratifiedKFold(n_splits=5, shuffle=True)
    clf = GridSearchCV(clf, search_params, cv=gs_cv)

    clf.fit(X_train, y_train)

    return clf


def _create_conf_matrix(
        clf, X_test, y_test, path, pipeline_name, filter, classes):
    _, ax = plt.subplots(figsize=(8, 8))

    ConfusionMatrixDisplay.from_estimator(
        clf, X_test, y_test, normalize='true',
        values_format='.2%', cmap='Greys',
        colorbar=False, ax=ax, labels=classes
    )

    plt.xticks(rotation=45)

    pathlib.Path(path).mkdir(exist_ok=True)

    outfile = (
        f'{path}/{pipeline_name}_'
        f'{"filter" if filter else ""}'
        '.png'
    )

    plt.savefig(outfile)


def _save_model(clf, dataset, pipeline_name, filter):
    output_filepath = f'{MODELS_DIR}/{dataset}'

    pathlib.Path(output_filepath).mkdir(exist_ok=True)

    outfile = (
        f'{output_filepath}/{pipeline_name}_'
        f'{"filter_" if filter else ""}'
    )

    dump(clf, outfile)


if __name__ == '__main__':
    main()
