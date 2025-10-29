dataset_config = {
    'crossera_piano': {
        'target_col': 'style_period',
        'classes': ['baroque', 'classical', 'romantic', 'modern'],
        'filter_col': 'Composer'
    },
    'crossera_orchestra': {
        'target_col': 'style_period',
        'classes': ['baroque', 'classical', 'romantic', 'modern'],
        'filter_col': 'Composer'
    },
    'crossera_full': {
        'target_col': 'style_period',
        'classes': ['baroque', 'classical', 'romantic', 'modern'],
        'filter_col': 'Composer'
    },
    'crosscomp5': {
        'target_col': 'composer',
        'classes': [
            'bach',
            'haydn',
            'beethoven',
            'brahms',
            'shostakovich'
        ],
        'filter_col': 'Artist_filter_no'
    },
    'crosscomp11': {
        'target_col': 'composer',
        'classes': [
            'bach',
            'handel',
            'rameau',
            'haydn',
            'mozart',
            'beethoven',
            'schubert',
            'mendelssohn',
            'brahms',
            'dvorak',
            'shostakovich'
        ],
        'filter_col': 'Artist_filter_no'
    },
    'orchsetera': {
        'target_col': 'style_period',
        'classes': ['classical', 'romantic', 'modern']
    },
    'orchsetcomp': {
        'target_col': 'composer',
        'classes': [
            'beethoven',
            'brahms',
            'dvorak',
            'grieg',
            'haydn',
            'holst',
            'musorgski',
            'prokofiev',
            'ravel',
            'rimski-korsakov',
            'schubert',
            'smetana',
            'strauss',
            'tchaikovsky',
            'wagner'
        ]
    }
}
