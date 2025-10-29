import unittest
import src.utils.lists as lst_utils


class TestLists(unittest.TestCase):
    def test_all_combinations(self):
        expected = [[1], [2], [3], [1, 2], [1, 3], [2, 3], [1, 2, 3]]
        result = lst_utils.all_combinations([1, 2, 3])
        self.assertCountEqual(result, expected)

        expected = [
            ['elem1'],
            ['elem2'],
            ['elem3'],
            ['elem4'],
            ['elem1', 'elem2'],
            ['elem1', 'elem3'],
            ['elem1', 'elem4'],
            ['elem2', 'elem3'],
            ['elem2', 'elem4'],
            ['elem3', 'elem4'],
            ['elem1', 'elem2', 'elem3'],
            ['elem1', 'elem2', 'elem4'],
            ['elem1', 'elem3', 'elem4'],
            ['elem2', 'elem3', 'elem4'],
            ['elem1', 'elem2', 'elem3', 'elem4']
        ]
        result = lst_utils.all_combinations(
            ['elem1', 'elem2', 'elem3', 'elem4']
        )
        self.assertCountEqual(result, expected)
