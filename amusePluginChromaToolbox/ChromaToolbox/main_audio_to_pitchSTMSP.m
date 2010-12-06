function [f_pitch,sideinfo] = main_audio_to_pitchSTMSP(input_file);
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Name: test_convert_audio_to_pitchSTMSP.m
% Date of Revision: 15.12.2009
% Programmer: Meinard Mueller, Sebastian Ewert
% I/O modified for AMUSE: Igor Vatolkin, 17.05.2010
%
% Description: 
% * Computes pitch subband decomposition of WAV file
%   (default: MIDI pitches 21 to 108) 
% * each pitch subband contains short time mean-square power (STMSP) 
% * Features are computed in a batch modus
% * Features are stored in folder 'data_feature/'
%
% Reference: 
% Details on the feature computation can be found in the following book:
%
% Meinard Mueller: Information Retrieval for Music and Motion,
%                  Springer 2007
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
close all hidden;

% Split the file name as required for wav_to_audio
p=strfind(input_file,'/');
dir_abs = input_file(1:p(end-4));
dir_rel = input_file(p(end-4)+1:p(end));
wavfilename = input_file(p(end)+1:length(input_file));
[f_audio,sideinfo] = wav_to_audio(dir_abs, dir_rel, wavfilename);

parameter.win_len = 4410;
parameter.fs = sideinfo.wav.fs;
parameter.save = 0; % Do not save any results!
parameter.saveFilename = strcat(sideinfo.wav.filename(1:length(sideinfo.wav.filename)-4));
parameter.visualize = 0; % Do not make any visualization!

[f_pitch,sideinfo] = audio_to_pitchSTMSP_via_FB(f_audio,parameter,sideinfo);

