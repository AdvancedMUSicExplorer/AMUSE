function [f_audio,sideinfo] = wav_to_audio(dir_abs,dir_rel,wavfilename,parameter);
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Name: wav_to_audio
% Version: 2
% Date: 15.07.2007, 15.07.2009 added mp3 support
% Programmer: Sebastian Ewert, Peter  Grosche
%
% Description:
% Loads a Wav or mp3 file and fills a sideinfo variable according to AGtoolbox
% specifications. Resampling and single channel conversion is default, but
% optional.
%
% Input:
%        dir_abs
%        dir_rel
%        wavfilename
%        parameter.use_resampling = 1;
%        parameter.dest_samplerate = 22050;
%        parameter.convert_to_mono = 1;
%        parameter.mono_convert_mode = 'downmix';
%
%        parameter.vis = 0;
%        parameter.save = 0;
%            parameter.save_dir = '';
%            parameter.save_filename = '';
%
% Output:
%        f_audio
%        sideinfo.wav.version
%        sideinfo.wav.filename
%        sideinfo.wav.dir_rel
%        sideinfo.wav.size
%        sideinfo.wav.duration
%        sideinfo.wav.energy
%        sideinfo.wav.fs
%        sideinfo.wav.nbits
%        sideinfo.wav.channels
%        sideinfo.wav.resampled
%        sideinfo.wav.mono_converted
%        sideinfo.wav.mono_convert_mode
%
%
% Example:
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Check parameters
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

if nargin<4
    parameter=[];
end

if nargin<3
    error('Please specify at least the path and filename of the wav file')
end

if isfield(parameter,'message')==0
    parameter.message = 0;
end

if isfield(parameter,'vis')==0
    parameter.vis = 0;
end

if isfield(parameter,'save')==0
    parameter.save = 0;
end

if isfield(parameter,'use_resampling')==0
    parameter.use_resampling = 1;
end

if isfield(parameter,'dest_samplerate')==0
    parameter.dest_samplerate = 22050;
end

if isfield(parameter,'convert_to_mono')==0
    parameter.convert_to_mono = 1;
end

if isfield(parameter,'mono_convert_mode')==0
    parameter.mono_convert_mode = 'downmix';
end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Main program
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
if parameter.message == 1
    fprintf('wav_to_audio: processing %s, ',wavfilename);
end

[pathstr,name,ext] = fileparts(wavfilename);
if strcmp(ext,'.wav')
    [f_audio,fs,nbits] = wavread(strcat(dir_abs,dir_rel,wavfilename));
elseif strcmp(ext,'.mp3')
    [f_audio,fs,nbits] = mp3read(strcat(dir_abs,dir_rel,wavfilename));
else
    error(['Unknown file format ' ext]);
end
    

bConverted_to_mono = 0;
if parameter.convert_to_mono
    if size(f_audio,2)>1
        bConverted_to_mono = 1;
        if parameter.message == 1
            fprintf('converting to mono, ');
        end
        switch parameter.mono_convert_mode
            case 'leftmost_channel'
                f_audio= f_audio(:,1);
            case 'rightmost_channel'
                f_audio= f_audio(:,size(f_audio,2));
            case 'downmix'
                % pay attention to energy loss due to differences in phase
                % when using this method. This is often the case for bad
                % stereo mixes
                nChannels = size(f_audio,2);
                
                f_audio = sum(f_audio,2);
                f_audio = f_audio / nChannels;
            otherwise
                disp('wav_to_audio: mono_convert_mode : Unknown method')
        end
    end
end

bResampled = 0;
if parameter.use_resampling
    if (fs ~= parameter.dest_samplerate)
        bResampled = 1;
        if parameter.message == 1
            fprintf('Resampling to %d, ', parameter.dest_samplerate);
        end
        f_audio = resample (f_audio,parameter.dest_samplerate,fs,100);
        fs = parameter.dest_samplerate;
    end
end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Update sideinfo
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
sideinfo.wav.version    = 1;
sideinfo.wav.filename   = wavfilename;
sideinfo.wav.dir_rel    = dir_rel;
sideinfo.wav.size       = size(f_audio,1);
sideinfo.wav.duration   = (sideinfo.wav.size-1)/fs;
sideinfo.wav.energy     = sum(f_audio.^2);
sideinfo.wav.fs         = fs;
sideinfo.wav.nbits      = nbits;
sideinfo.wav.channels   = size(f_audio,2);
sideinfo.wav.resampled  = bResampled;
sideinfo.wav.mono_converted = bConverted_to_mono;
if bConverted_to_mono
    sideinfo.wav.mono_convert_mode = parameter.mono_convert_mode;
else
    sideinfo.wav.mono_convert_mode = 'none';
end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Saving data
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
if parameter.save == 1
    if parameter.message == 1
        fprintf('Saving to file, ');
    end
    if isfield(parameter,'save_dir')==0
        parameter.save_dir = '';
    end
    if isfield(parameter,'save_filename')==0
        parameter.save_filename = '';
    end

    filename = strcat(parameter.save_filename,'_audio');
    save(strcat(parameter.save_dir,filename),'f_audio','sideinfo');

end

if parameter.message == 1
    fprintf('Done\n');
end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Visualization
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
if parameter.vis
    figure;
    for k=1:sideinfo.wav.channels
        if sideinfo.wav.channels > 1
            subplot(sideinfo.wav.channels,1,k);
        end
        plot( [0:sideinfo.wav.size-1] / sideinfo.wav.fs , f_audio(:,k));
        axis tight;
    end
end
