class ChromaFeats:
    C1 = 'c1'
    C2 = 'c2'
    C3 = 'c3'
    C4 = 'c4'
    C5 = 'c5'
    C6 = 'c6'
    C7 = 'c7'
    C8 = 'c8'
    C9 = 'c9'
    C10 = 'c10'
    C11 = 'c11'
    C12 = 'c12'


class IntervalFeats:
    IC1 = 'ic1'
    IC2 = 'ic2'
    IC3 = 'ic3'
    IC4 = 'ic4'
    IC5 = 'ic5'
    IC6 = 'ic6'


class ChordFeats:
    MAJ = 'chord_maj'
    MIN = 'chord_min'
    DIM = 'chord_dim'
    AUG = 'chord_aug'


class ComplexityFeats:
    DIFF = 'comp_diff'
    STD = 'comp_std'
    SLOPE = 'comp_slope'
    ENTROPY = 'comp_entr'
    NON_SPARSENESS = 'comp_sparse'
    FLATNESS = 'comp_flat'
    FIFTH_ANG_DEV = 'comp_fifth'


class TISFeats:
    TIV = 'TIV'
    DISSONANCE = 'dissonance'
    CHROMATICITY = 'chromaticity'
    DYADICITY = 'dyadicity'
    TRIADICITY = 'triadicity'
    DIMINISHED_QUALITTY = 'dim_quality'
    DIATONICITY = 'diatonicity'
    WHOLETONENESS = 'wholetoneness'
    COS_TONAL_DISP = 'cos_tonal_disp'
    EUC_TONAL_DISP = 'euc_tonal_disp'
    COS_DIST = 'cos_dist'
    EUC_DIST = 'euc_dist'
    COEF_ENTROPY = 'coef_entropy'


class HarmRhythmFeats:
    HCDF_PEAK_IDX = 'hcdf_peak_idx'
    HCDF_PEAK_MAG = 'hcdf_peak_mag'
    HCDF_PEAK_INT = 'hcdf_peak_interval'
