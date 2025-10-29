# AMUSE framework (Advanced MUsic Explorer) plugin - Mid-level Harmonic Audio Features for Musical Style Classification

## For AMUSE framework

Copyright 2006-2023 by code authors

Created at TU Dortmund, Department of Computer Science, Chair of Algorithm Engineering
(Contact: <http://ls11-www.cs.tu-dortmund.de>) 

AMUSE is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

AMUSE is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with AMUSE. If not, see <http://www.gnu.org/licenses/>.

## For Harmonic Features

This plugin contains the implementation of the musical style classification system proposed in the publication by F. Almeida, G. Bernardes and C. Wei&szlig;: Mid-level Harmonic Audio Features for Musical Style Classification, Proc. ISMIR 2022, pp. 210-217
(see also <https://github.com/fcfalmeida/style-ident>)

### Usage/Requirements for Linux

1. Install the required system dependencies:
    - Python 3.9  
    - Pip package manager 
    - NNLS Chroma
        - Download and place under /home/user/vamp
2. Provide a virtual python environment:
    - `python3.9 -m venv name-of-the-venv`
    - `source name-of-the-venv/bin/activate`
3. Install required packages and librarys into virtual environment as follows:
    - use `cd` command to navigate to /AMUSE/amusePluginHarmonicFeatures/HarmonicFeatures 
        - `pip install wheel`  
        - `python3 -m pip install -r requirements.txt`  
        - `python3 -m pip install lib/TIVlib`  
4. Deactivate virtual environment:
    - `deactivate`
5. Place path to python executable in AMUSE General settings:
    - python executable: `path-to-venv/name-of-the-venv/bin/python3`
6. Install plugin to AMUSE

### Usage/Requirements for Windows

1. Install the required system dependencies:
    - Microsoft C++ Build Tools
        - <https://visualstudio.microsoft.com/de/visual-cpp-build-tools/>
        - install and select Workloads, check "Desktop development using C++" option
    - Python 3.9  
    - Pip package manager 
    - NNLS Chroma and Chordino
        - <https://code.soundsoftware.ac.uk/projects/vamp-plugin-pack>
2. Provide a virtual python environment:
    - `python -m venv name-of-the-venv`
    - `name-of-the-venv\Scripts\activate`
3. Install required packages and librarys into virtual environment as follows:
    - use `cd` command to navigate to \AMUSE\amusePluginHarmonicFeatures\HarmonicFeatures 
        - `pip install wheel`  
        - `python -m pip install -r requirements.txt`  
        - `python -m pip install lib\TIVlib`  
4. Deactivate virtual environment:
    - `deactivate`
5. Place path to python executable in AMUSE General settings:
    - python executable: `path-to-venv\name-of-the-venv\Scripts\python.exe`
6. Install plugin to AMUSE

## Please note:

* Along with the core AMUSE classes you have received some software
  packages and libraries not all of which are subject to the AMUSE
  license terms. You can find the license texts of these libraries
  in the 'licenses' directory of AMUSE.
  
* This version of AMUSE is licensed under the AGPL (see the
  corresponding license file). Please contact us if you are
  interested in a different license, e.g. an OEM license for
  integrating AMUSE into a proprietary software product
  not licensed under the AGPL.

 
