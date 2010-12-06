function [f_pitch,sideinfo] = audio_to_pitchSTMSP_via_FB(f_audio,parameter,sideinfo)
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Name: audio_to_pitchSTMSP_via_FB 
% Date of Revision: 15.12.2009
% Programmer: Meinard Mueller, Sebastian Ewert
%
% Description:
% Computing and saving FB-based STMSP (short-time mean-square power)
% features. For each window length of parameter.win_len the following 
% is computed:
% - STMSP for each MIDI pitch between sideinfo.midi_min and sideinfo.midi_max 
% - STMSP subbands are stored in f_pitch, where f_pitch(:,p) contains 
%   STMSP of subband of pitch p 
% - sideinfo contains information of original pcm, which is saved along
%   with f_pitch into a single mat-file
% - Information f_pitch and sideinfo is stored in mat-file:
%   save(strcat(parameter.saveDir,parameter.saveFilename),'f_pitch','sideinfo');
%
% Input: 
%        f_audio
%        parameter.midi_min = 21;
%        parameter.midi_max = 108;
%        parameter.win_len
%        parameter.fs
%        parameter.shiftFB = 0;
%
%        parameter.visualize = 0;
%        parameter.save = 0;
%            parameter.saveDir;
%            parameter.saveFilename;
%
% Required files:
%        'MIDI_FB_ellip_pitch_60_96_22050_Q25.mat'
%        'MIDI_FB_ellip_pitch_60_96_22050_Q25_minusHalf.mat'
%        'MIDI_FB_ellip_pitch_60_96_22050_Q25_minusQuarter.mat'
%        'MIDI_FB_ellip_pitch_60_96_22050_Q25_minusThird.mat'
%        'MIDI_FB_ellip_pitch_60_96_22050_Q25_minusThreeQuarters.mat'
%        'MIDI_FB_ellip_pitch_60_96_22050_Q25_minusTwoThird.mat'
% 
% Output:
%        f_pitch
%        sideinfo.pitchSTMSP.version
%        sideinfo.pitchSTMSP.midi_min
%        sideinfo.pitchSTMSP.midi_max
%        sideinfo.pitchSTMSP.win_len
%        sideinfo.pitchSTMSP.win_ov
%        sideinfo.pitchSTMSP.featureRate
%
%
% Example:
%  
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Check parameters
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

if nargin<3
   sideinfo=[]; 
end

if nargin<2
    parameter=[];
end
if isfield(parameter,'visualize')==0
   parameter.visualize = 0;
end

if isfield(parameter,'save')==0
   parameter.save = 0;
end

if isfield(parameter,'saveDir')==0
    parameter.saveDir = '';
end
if isfield(parameter,'saveFilename')==0
    parameter.saveFilename = '';
end

if isfield(parameter,'fs')==0
    parameter.fs = 22050;
else
    if parameter.fs ~= 22050
        error('audio_to_FBpitchSTMSP not implemented yet for sample rates other than 22050.');
    end
end

if isfield(parameter,'midi_min')==0
   parameter.midi_min = 21;
end

if isfield(parameter,'midi_max')==0
   parameter.midi_max = 108;
end

if isfield(parameter,'win_len')==0
    parameter.win_len = 4410;
    %parameter.win_len = [882 4410];
end

if isfield(parameter,'shiftFB')==0
    parameter.shiftFB = 0;
end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Update sideinfo
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

sideinfo.pitchSTMSP.version = 1;
sideinfo.pitchSTMSP.midi_min = parameter.midi_min;
sideinfo.pitchSTMSP.midi_max = parameter.midi_max; 

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Main program
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

if parameter.shiftFB == 0
    load MIDI_FB_ellip_pitch_60_96_22050_Q25.mat
elseif parameter.shiftFB == 1
    load MIDI_FB_ellip_pitch_60_96_22050_Q25_minusQuarter.mat    
elseif parameter.shiftFB == 2
    load MIDI_FB_ellip_pitch_60_96_22050_Q25_minusThird.mat
elseif parameter.shiftFB == 3    
    load MIDI_FB_ellip_pitch_60_96_22050_Q25_minusHalf.mat
elseif parameter.shiftFB == 4
    load MIDI_FB_ellip_pitch_60_96_22050_Q25_minusTwoThird.mat    
elseif parameter.shiftFB == 5
    load MIDI_FB_ellip_pitch_60_96_22050_Q25_minusThreeQuarters.mat
else
    error('Wrong shift parameter!')
end

fs_pitch = zeros(1,128);
fs_index = zeros(1,128);

fs_pitch(21:59) = 882;
fs_pitch(60:95) = 4410;
fs_pitch(96:120) = 22050;

fs_index(21:59) = 3;
fs_index(60:95) = 2;
fs_index(96:120) = 1;

pcm_ds = cell(3,1);
pcm_ds{1} = f_audio;
pcm_ds{2} = resample(pcm_ds{1},1,5,100);
pcm_ds{3} = resample(pcm_ds{2},1,5,100);

fprintf('Computing subbands and STMSP for all pitches: (%i-%i): %4i',parameter.midi_min,parameter.midi_max,0);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Compute features for all pitches
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

win_len =  parameter.win_len;
win_ov  =  round(win_len/2); 
featureRate =  parameter.fs./(win_len-win_ov);  %formerly win_res
wav_size = size(f_audio,1);

num_window = length(win_len);
f_pitch_energy = cell(num_window,1);
seg_pcm_num = cell(num_window,1);
seg_pcm_start = cell(num_window,1);
seg_pcm_stop = cell(num_window,1);
for w=1:num_window;
    step_size = win_len(w)-win_ov(w);
    group_delay = round(win_len(w)/2);
    seg_pcm_start{w} = [1 1:step_size:wav_size]';   %group delay is adjusted
    seg_pcm_stop{w} = min(seg_pcm_start{w}+win_len(w),wav_size);
    seg_pcm_stop{w}(1) = min(group_delay,wav_size);
    seg_pcm_num{w} = size(seg_pcm_start{w},1);        
    f_pitch_energy{w} = zeros(seg_pcm_num{w},120);
end


for p=parameter.midi_min:parameter.midi_max    
    fprintf('\b\b\b\b');fprintf('%4i',p);
    index = fs_index(p); 
    f_filtfilt = filtfilt(h(p).b, h(p).a, pcm_ds{index});
    f_square = f_filtfilt.^2;    
    
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    % f_pitch_energy
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%        
    for w=1:length(win_len)
        factor = (parameter.fs/fs_pitch(p));      %adjustment for sampling rate
        for k=1:seg_pcm_num{w}
            start = ceil((seg_pcm_start{w}(k)/parameter.fs)*fs_pitch(p));
            stop = floor((seg_pcm_stop{w}(k)/parameter.fs)*fs_pitch(p));
            f_pitch_energy{w}(k,p)=sum(f_square(start:stop))*factor;
        end
    end
end
fprintf('\n');
    
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Save f_pitch_energy for each window size separately as f_pitch
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%    

if parameter.save == 1
    for w=1:num_window;
        f_pitch = f_pitch_energy{w};
        sideinfo.pitchSTMSP.win_len = win_len(w);
        sideinfo.pitchSTMSP.win_ov = win_ov(w);
        sideinfo.pitchSTMSP.featureRate = featureRate(w);
        if parameter.shiftFB == 0
            filename = strcat(parameter.saveFilename,'_pitchSTMSP_',num2str(win_len(w)));
        elseif parameter.shiftFB == 1
            filename = strcat(parameter.saveFilename,'_pitchSTMSP_',num2str(win_len(w)),'_minusQuarter');
        elseif parameter.shiftFB == 2
            filename = strcat(parameter.saveFilename,'_pitchSTMSP_',num2str(win_len(w)),'_minusThird');
        elseif parameter.shiftFB == 3
            filename = strcat(parameter.saveFilename,'_pitchSTMSP_',num2str(win_len(w)),'_minusHalf');
        elseif parameter.shiftFB == 4
            filename = strcat(parameter.saveFilename,'_pitchSTMSP_',num2str(win_len(w)),'_minusTwoThird');
        elseif parameter.shiftFB == 5
            filename = strcat(parameter.saveFilename,'_pitchSTMSP_',num2str(win_len(w)),'_minusThreeQuarter');
        end
        save(strcat(parameter.saveDir,filename),'f_pitch','sideinfo');
    end
else
    f_pitch = f_pitch_energy{num_window};
    sideinfo.pitchSTMSP.win_len = win_len(num_window);
    sideinfo.pitchSTMSP.win_ov = win_ov(num_window);
    sideinfo.pitchSTMSP.featureRate = featureRate(num_window);
end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Visualization
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
if parameter.visualize == 1
    for w=1:num_window;
        figure;
        if w==1
            hot2 = hot;
            hot2 = [hot2; hot2(32:64,:); hot2(32:64,:)];
            hot2 = sort(hot2);
        end
        f_image = f_pitch_energy{w}';
        imagesc(f_image);
         set(gca,'YDir','normal');
        colormap(hot2);
        h = colorbar;
        drawnow;
    end
end




