import numpy as np
from scipy.ndimage.filters import gaussian_filter
from scipy.spatial.distance import euclidean, cosine
from TIVlib import TIV


def everything_is_zero(vector):
    for element in vector:
        if element != 0:
            return False
    return True


def complex_to_vector(vector):
    ans = []
    for i in range(0, vector.shape[1]):
        row1 = []
        row2 = []
        for j in range(0, vector.shape[0]):
            row1.append(vector[j][i].real)
            row2.append(vector[j][i].imag)
        ans.append(row1)
        ans.append(row2)
    return np.array(ans)


def tonal_interval_space(chroma):
    centroid_vector = []
    for i in range(0, chroma.shape[1]):
        each_chroma = [chroma[j][i] for j in range(0, chroma.shape[0])]

        if everything_is_zero(each_chroma):
            centroid = [
                0.0 + 0.0j,
                0.0 + 0.0j,
                0.0 + 0.0j,
                0.0 + 0.0j,
                0.0 + 0.0j,
                0.0 + 0.0j,
            ]
        else:
            tonal = TIV.from_pcp(each_chroma)
            centroid = tonal.vector
        centroid_vector.append(centroid)
    return complex_to_vector(np.array(centroid_vector))


def gaussian_blur(centroid_vector, sigma):
    centroid_vector = gaussian_filter(centroid_vector, sigma=sigma)
    return centroid_vector


def get_distance(centroids, dist):
    ans = [0]
    if dist == "euclidean":
        for j in range(1, centroids.shape[1] - 1):
            sum = 0
            for i in range(0, centroids.shape[0]):
                sum += (centroids[i][j + 1] - centroids[i][j - 1]) ** 2
            sum = np.math.sqrt(sum)

            ans.append(sum)

    if dist == "cosine":
        for j in range(1, centroids.shape[1] - 1):
            a = centroids[:, j - 1]
            b = centroids[:, j + 1]
            if everything_is_zero(a) or everything_is_zero(b):
                distance_computed = euclidean(a, b)
            else:
                distance_computed = cosine(a, b)
            ans.append(distance_computed)
    ans.append(0)

    return np.array(ans)


def get_peaks_hcdf(hcdf_function, rate_centroids_second, symbolic=False):
    changes = [0]
    hcdf_changes = [0]
    for i in range(2, hcdf_function.shape[0] - 1):
        if (
            hcdf_function[i - 1] < hcdf_function[i]
            and hcdf_function[i + 1] < hcdf_function[i]
        ):
            hcdf_changes.append(hcdf_function[i])
            """
            if not symbolic:
                changes.append(i / rate_centroids_second)
            else:
                changes.append(i)
            """
            changes.append(i)
    return np.array(changes), np.array(hcdf_changes)


def harmonic_change(
    chroma: list,
    window_size: int = 2048,
    symbolic: bool = False,
    sigma: int = 5,
    dist: str = "euclidean",
):
    chroma = np.array(chroma).transpose()
    centroid_vector = tonal_interval_space(chroma)

    # blur
    centroid_vector_blurred = gaussian_blur(centroid_vector, sigma)

    # harmonic distance and calculate peaks
    harmonic_function = get_distance(centroid_vector_blurred, dist)

    changes, hcdf_changes = get_peaks_hcdf(
        harmonic_function, window_size, symbolic
    )

    return changes, hcdf_changes, harmonic_function
