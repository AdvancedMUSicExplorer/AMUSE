
%
% matlab_base.m
%
% loads one input wav files and extracts all Matlab features and saves results as text files
% one text file per feature is created
%
% The files have been decoded, subsampled by a factor of 2 and
% converted to mono. 
%

function matlab_base(input_file,output_dir)

% Add the paths to AMUSE Matlab libraries
addpath('/Users/aylinozek/Desktop/RWTH/hiwiamuse/AMUSE/amuse/tools/MatlabFeatures');

% define window length of analysis window in number of samples
wlength = 512;

% define window offset in number of samples
woffset = wlength;

% for the cutting of the path to file and its extension
p=strfind(input_file,filesep);
k=strfind(input_file,'.');

% create Amuse ARFF feature file
output_file_prefix = [output_dir,filesep,input_file(p(end)+1:k(end)-1),'_'];

% loads wav input file, called input.wav
[x,fs] = amuse_audio_read(input_file);

% determine number of windows
s = size(x);
nwin = floor(1 + (s(1)-wlength)/woffset);

% compute the fundamental frequency
ff = f_200_fundamental_freq(x,fs,wlength,woffset);

% compute first two tristimulus values and normalized energy of harmonic components
res = f_7_10_harmonic_analysis(x,fs,wlength,woffset,ff);

% save the results
arff_saver_with_windows_number([output_file_prefix,'10.arff'],'Tristimulus',[res(1,:)' res(2,:)']',wlength,woffset);

exit
