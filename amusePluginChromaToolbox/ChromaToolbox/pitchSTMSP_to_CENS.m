function [f_CENS,f_chroma_energy_distr,sideinfo] = pitchSTMSP_to_CENS(f_pitch,parameter,sideinfo);
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Name: pitchSTMSP_to_CENS
% Date of Revision: 15.12.2009
% Programmer: Meinard Mueller, Sebastian Ewert
%
% Description:
% Normalized statistical chroma-based energy distribution feature (CENS).
% The following is computed:
% * energy for each chroma band
% * normalisation of the chroma vectors
% * local statistics:
%   - component-wise quantisation of the normalized chroma vectors
%   - upfirdn filters and downsamples each column of f_stat_help
%   - normalize each vector with its l^2 norm
%
% Input:
%         f_pitch
%         parameter.midi_min = 21
%         parameter.midi_max = 108
%         parameter.featureRate = 10
%         parameter.vec_energy = [40 20 10 5] / 100;
%         parameter.vec_weight = [ 1 1 1 1]/4;
%         parameter.stat_thresh = 0.001;
%         parameter.stat_window_length  = 41;
%         parameter.stat_downsample     = 10;
%
%         parameter.save = 0;
%            parameter.save_dir = '';
%            parameter.save_filename = '';
%
% Output:
%         f_CENS
%         f_chroma_energy_distr 
%         sideinfo
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Check parameters
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

if nargin<3
    sideinfo=[];
end

if nargin<2
    parameter=[];
end

if nargin<1
    error('Please specify input data f_pitch');
end

if isfield(parameter,'save')==0
    parameter.save = 0;
end

if isfield(parameter,'print')==0
    parameter.print = 0;
end

if isfield(parameter,'midi_min')==0
    parameter.midi_min = 21;
end

if isfield(parameter,'midi_max')==0
    parameter.midi_max = 108;
end

if isfield(parameter,'featureRate')==0
    parameter.featureRate = 10;
end

if isfield(parameter,'vec_energy')==0
    parameter.vec_energy = [40 20 10 5] / 100;
end

if isfield(parameter,'vec_weight')==0
    parameter.vec_weight = [ 1 1 1 1]/4;
end

if isfield(parameter,'stat_thresh')==0
    parameter.stat_thresh = 0.001;
end

if isfield(parameter,'stat_window_length')==0
    parameter.stat_window_length = 41;
end

if isfield(parameter,'stat_downsample')==0
    parameter.stat_downsample = 10;
end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Update sideinfo
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

sideinfo.CENS.version = 1;
sideinfo.CENS.midi_min = parameter.midi_min;
sideinfo.CENS.midi_max = parameter.midi_max;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Main program
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

seg_num = size(f_pitch,1);

% calculate energy for each chroma band
f_chroma_energy = zeros(seg_num,12);
for p=parameter.midi_min:parameter.midi_max
    chroma = mod(p,12)+1;
    f_chroma_energy(:,chroma) = f_chroma_energy(:,chroma)+f_pitch(:,p);
end

% normalize the chroma vectors
f_chroma_energy_distr = zeros(seg_num,12);
for k=1:seg_num
    if sum(f_chroma_energy(k,:)>parameter.stat_thresh)>0
        seg_energy_square = sum(f_chroma_energy(k,:));
        %f_chroma_energy_distr(k,:) = 100*((f_chroma_energy(k,:))/seg_energy_square);
        f_chroma_energy_distr(k,:) = ((f_chroma_energy(k,:))/seg_energy_square);
    end
end

% calculate a CENS featur
stat_window_length = parameter.stat_window_length;
stat_downsample = parameter.stat_downsample;
stat_window = hanning(stat_window_length);
stat_window = stat_window/sum(stat_window);
sideinfo.CENS.featureRate = parameter.featureRate/stat_downsample;

% component-wise quantisation of the normalized chroma vectors
f_stat_help = zeros(seg_num,12);
for n=1:length(parameter.vec_energy)
    f_stat_help = f_stat_help + (f_chroma_energy_distr>parameter.vec_energy(n))*parameter.vec_weight(n);
end

% upfirdn filters and downsamples each column of f_stat_help
f_chroma_energy_stat = zeros(seg_num,12);
f_chroma_energy_stat = upfirdn(f_stat_help,stat_window,1,stat_downsample);
stat_num = ceil(seg_num/stat_downsample);
cut = floor((stat_window_length-1)/(2*stat_downsample));
f_chroma_energy_stat = f_chroma_energy_stat((1+cut:stat_num+cut),:);            %adjust group delay

% last step: normalize each vector with its l^2 norm
unit_vec = ones(1,12);
unit_vec = unit_vec/norm(unit_vec);
f_CENS = zeros(stat_num,12);
for k=1:stat_num
    n = norm(f_chroma_energy_stat(k,:));
    if n==0
        f_CENS(k,:) = unit_vec;
    else
        f_CENS(k,:) = f_chroma_energy_stat(k,:)/n;
    end
end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Update sideinfo
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
sideinfo.CENS.vec_energy = parameter.vec_energy;
sideinfo.CENS.vec_weight = parameter.vec_weight;
sideinfo.CENS.stat_thresh = parameter.stat_thresh;
sideinfo.CENS.stat_window_length = parameter.stat_window_length;
sideinfo.CENS.stat_downsample = parameter.stat_downsample;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Saving to file
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
if parameter.save == 1
    if isfield(parameter,'save_dir')==0
        parameter.save_dir = '';
    end
    if isfield(parameter,'save_filename')==0
        parameter.save_filename = '';
    end
    filename = strcat(parameter.save_filename,'_CENS_',num2str(parameter.stat_window_length),'_',num2str(parameter.stat_downsample));
    save(strcat(parameter.save_dir,filename),'f_CENS','sideinfo');
end

