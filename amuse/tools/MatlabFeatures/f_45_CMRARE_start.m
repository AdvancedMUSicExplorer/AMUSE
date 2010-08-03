function features = f_45_CMRARE_start(pathToWave)

% frame length N_T: 5 sec
nSamples = 110250;
% sampling frequency fs:
sampFreq = 22050;
% window for spectral analysis:
winSpec = 'hann';
% window for modulation analysis:
winMod = 'rect';
% subframe length N:
nFramesSpec = 512;
% subframe shift R
fadvance = 256;
% modulation analysis window length K
modCeps_len = 16;
% modulation analysis window shift S
modadvance = 8;
% polynomial order p (to avoid numerical problems: p <= 10)
polyOrder = 3;
% modulation frequency bin for computation of 1st CMR
nu = 2;
% lower modulation frequency bin for computation of 2nd CMR
nu1 = 3;
% upper modulation frequency bin for computation of 2nd CMR
nu2 = modCeps_len/2+1;

% compute and store features
%warning off
features = CMRARE_Analyzer(pathToWave,sampFreq,nFramesSpec,winSpec,winMod,fadvance,modadvance,modCeps_len,nSamples,polyOrder,nu,nu1,nu2);
