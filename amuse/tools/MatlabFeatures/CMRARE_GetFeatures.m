function [features] = CMRARE_GetFeatures(polyOrder,x,fs,spec_len,winSpec,modCeps_len,winMod,fadvance,modadvance,nu,nu1,nu2)
%   getFeatures_CMRARE: Function which computes the CMRARE features for a
%   given audio signal frame
%
%   [features] =
%   getFeatures_CMRARE(polyOrder,x,fs,spec_len,winSpec,modCeps_len,winMod,fadvance,modadvance,nu,nu1,nu2)
%
%   Input parameters:
%   polyOrder:          order for polynomial fit of CEPSTRAL MODULATION
%                       RATIOS (CMR) (p)
%   x:                  audio signal frame
%   fs:                 sampling frequency
%   spec_len:           subframe length (N)
%   winSpec:            window for spectral analysis
%   modCeps_len:        modulation analysis window length (K)
%   winMod:             window for cepstral modulation analysis
%   fadvance:           subframe shift (R)
%   modadvance:         modulation analysis window shift(S)
%   nu:                 modulation frequency bin for computation of 1st CMR
%   nu1:                lower modulation frequency bin for computation of
%                       2nd CMR
%   nu2:                upper modulation frequency bin for computation of
%                       2nd CMR
%
%   Output parameters:
%   features:           CMRARE feature vector with 2*(polyOrder+1) elements


if nargin == 1
    features = 2*(polyOrder+1);
    return
end

% compute spectrum, cepstrum, modulation spectrum and modulation cepstrum
[powerspec,cepstrum,modcepst_cont,counter,fvec,modfvec,time] = ...
    CMRARE_Cepstmod(x,fs,spec_len,winSpec,modCeps_len,winMod,fadvance,modadvance,1);

% temporally averaged MCMMS
mean_modcepst_cont = mean(modcepst_cont,2);
mean_modcepst_cont = squeeze(mean_modcepst_cont);
% find cepstral modulations ratios (CMR)
ModCepsRatioNUto1 = mean_modcepst_cont(nu,:)./mean_modcepst_cont(1,:);
ModCepsRatioNU1_NU2to1 = mean(mean_modcepst_cont(nu1:nu2,:),1)./mean_modcepst_cont(1,:);
% compute CMRARE features
polyRatioNUto1 = polyfit(zscore(1:spec_len/2),ModCepsRatioNUto1,polyOrder);
polyRatioNU1_NU2to1 = polyfit(zscore(1:spec_len/2),ModCepsRatioNU1_NU2to1,polyOrder);
% polyRatioNUto1 = polyfit((1:spec_len/2),ModCepsRatioNUto1,polyOrder);
% polyRatioNU1_NU2to1 = polyfit((1:spec_len/2),ModCepsRatioNU1_NU2to1,polyOrder);

features = ...
[ ...
polyRatioNUto1';
polyRatioNU1_NU2to1'; ...
];