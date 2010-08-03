function featureMatrix=CMRARE_Analyzer(pathToWave,sampFreq,nFramesSpec,winSpec,winMod,fadvance,modadvance,modCeps_len,nSamples,polyOrder,nu,nu1,nu2)%%,PathName,AudioType,Type,outPath)
%   batch_CMRARE_continuous: Batch function which computes the CMRARE
%   features continuously for a given audio input
%   
%
%   batch_CMRARE_continuous(sampFreq,nFramesSpec,winSpec,winMod,fadvance,modadvance
%   ,modCeps_len,nSamples,polyOrder,nu,nu1,nu2,PathName,AudioType,Type,outPath)
%
%   Input parameters:
%   samFreq:            sampling frequency
%   nFramesSpec:        short-time analysis window length (N)
%   winSpec:            window for spectral analysis
%   winMod:             window for modulation analysis
%   fadvance:           subframe shift (R)
%   modadvance:         modulation analysis window shift (S)   
%   modCeps_len:        modulation analysis window length (K)
%   nSamples:           frame length (N_T)
%   polyOrder:          polynomial order for CMR fit
%   nu:                 modulation frequency bin for computation of 1st CMR
%   nu1:                lower modulation frequency bin for computation of
%                       2nd CMR
%   nu2:                upper modulation frequency bin for computation of
%                       2nd CMR

 
tic
% compute number of features = (polyOrder+1)*2
nFeatures = CMRARE_GetFeatures(polyOrder);
    
% read audio data
[inl,fs] = wavread(char(pathToWave));
        
% change sampling rate to desired value if necessary
if fs ~= sampFreq
    inl = resample(inl,sampFreq,fs);        
end
    
% number of partitions
nFrames = floor(size(inl,1)/nSamples);
    
% initialize matrix to store acoustic features
featureMatrix = zeros(nFeatures,nFrames);
    
% interate through partitions of a song, compute features and store
% them in the feature matrix
for frameCount = 1:nFrames
    inls = inl(1+(frameCount-1)*nSamples:frameCount*nSamples);
  
    inls = inls./repmat(sqrt(100*(var(inls)+mean(inls).^2)),size(inls,1),1);
    disp(['Processing frame ' num2str(frameCount) ' of ' num2str(nFrames) ' (file ' char(pathToWave) ')...'])
    
    % begin: compute acoustic features ------------------------------------
    features = CMRARE_GetFeatures(polyOrder,inls,fs,nFramesSpec,winSpec,modCeps_len,winMod,fadvance,modadvance,nu,nu1,nu2);
    featureMatrix(:,frameCount) = features;
    % end: compute acoustic features --------------------------------------
end

%%% eliminate NaNs - GGF. LASSEN!!
%%featureMatrix(:,find(isnan(featureMatrix(2,:)))) = [];
%%eval([varname '= featureMatrix;']);

toc