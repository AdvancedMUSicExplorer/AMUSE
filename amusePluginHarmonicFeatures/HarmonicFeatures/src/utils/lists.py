import itertools


def all_combinations(lst: list):
    combs = []

    for L in range(1, len(lst)+1):
        for subset in itertools.combinations(lst, L):
            combs.append(list(subset))

    return combs
