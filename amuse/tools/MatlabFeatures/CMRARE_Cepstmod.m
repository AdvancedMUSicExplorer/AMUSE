function  [powerspec,cepstrum,modcepst_cont,counter,fvec,modfvec,time] = CMRARE_Cepstmod(x,fs,fft_len,winSpec,ceps_len,winMod,fadvance,modCepsAdvance,firstCC)
%   cepstmod_basic3: Function which performs a spectral, cepstral and
%   cepstral modulation analysis on audio signals.
%
%   [powerspec,cepstrum,modcepst_cont,counter,fvec,modfvec,time] =
%   cepstmod_basic3(x,fs,fft_len,winSpec,ceps_len,winMod,fadvance,modCepsAdvance,firstCC)
%
%   Input parameters:
%   x:               audio signal frame
%   fs:              sampling frequency
%   fft_len:         subframe length (N)
%   ceps_len:        modulation analysis window length (K)
%   winSpec:         window for spectral analysis
%   winMod:          window for cepstral modulation analysis
%   fadvance:        subframe shift (R)
%   modCepsAdvance:  modulation analysis window shift (S)
%   firstCC = 0:     0-th cepstral coefficient is included
%   firstCC = 1:     0-th cepstral coefficient is omitted
%
%   Output parameters:
%   powerspec:       power spectra of all subframes of x
%   cepstrum:        cepstra of all subframes of x
%   modcepst_cont:   cepstral modulation spectrum for all cepstral
%                   coefficients
%   counter:         number of subframes
%   fvec:            frequency vector
%   modfvec:         modulation frequency vector
%   time:            computation time


starttime=clock;

L       = fft_len;  	   % Fensterlaenge
veclen   = fft_len/2+1;    % Laenge der halben Frequenzbereichsvektoren

% Parameter für fortlaufendes Modulationscepstrum
ModCepsLen = ceps_len;


% window for spectral analysis
switch winSpec;
    case 'rect'
        win = ones(L,1);
    case 'hann'
        % Standard Hann-Fenster
        win = 0.5*(1.- cos(2*pi*(0:(L-1))/L))'; % Initialisierung des Hann-Fensters
end

% window for computation of continuous modulation cepstrum
switch winMod;
    case 'rect'
        winC = repmat(ones(ModCepsLen,1),1,veclen-1);
    case 'hann'
        winC = repmat(0.5*(1.- cos(2*pi*(0:(ModCepsLen-1))/ModCepsLen))',1,veclen-1);
end

% Spaltendarstellung fuer das Eingangssignal erzwingen
x = x(:);
lmin = length(x);

% pad signals with zeros
x=[zeros(3*L/4,1);x;zeros(3*L/4,1)];

% generate empty data vectors
phi_xx = zeros(veclen,1);
powerspec = zeros(veclen,floor((lmin+fft_len)./fadvance));

cepstrum = zeros(fft_len,floor((lmin+fft_len)./fadvance));
cepstrumPrBlock = zeros(veclen-1,ModCepsLen);

modcepstPrBlck = zeros(ModCepsLen,veclen-1);
% modulation frequency bin, frame index, cepstral bin
% without conjugate complex part:
modcepst_cont = zeros(ModCepsLen/2+1,floor((floor((lmin+fft_len)./fadvance)-ModCepsLen)/modCepsAdvance),veclen-1);

%Initialisierung der Fensterindizes
min_ind = L+1;   
max_ind = min_ind + L - 1;


% signal segment counter
counter = 1;
modcounter = 1;

% reverse spectral bin index vector for symmetric expansion
reverseindx = veclen-1:-1:2;

log_K = zeros(fft_len,1);
ifft_log_K = zeros(fft_len,1);

% main loop
while(max_ind < lmin)
	 
      % Signalsegmente mit Hammingfenster gewichten
      xw = win .* x(min_ind:max_ind);
	 
      % Kurzzeitspektren
      Xfull = fft(xw,fft_len);
      X = Xfull(1:veclen);      % FFT vector without conjugate symmetric part

      % temporally smoothed spectrogram
      phi_xx = real(X.*conj(X));
      phi_xx = max(1e-16,phi_xx);

      K = phi_xx;

      K_full = [K;K(reverseindx)];     % expanded by conjugate symmetric part
      log_K(:) = reallog(K_full);
      %log_K(:) = (real(K_full)).^(1/3.35);
      ifft_log_K(:) = real(ifft(log_K));
      
      % write to data arrays        
      % half fft length
      powerspec(:,counter) = phi_xx;
      % full fft length
      cepstrum(:,counter) = ifft_log_K;
      
      % prepare for next signal segment
      min_ind = min_ind + fadvance;
      max_ind = min_ind + L - 1;
      
      % continuous Modulation cepstrum without considering conjugate
      % complex part of cepstrum
      if counter >= ModCepsLen
          if mod(counter-ModCepsLen+modCepsAdvance,modCepsAdvance) == 0
              if firstCC == 0
                  cepstrumPrBlock = cepstrum(1:veclen-1,counter-ModCepsLen+1:counter).*winC';
              elseif firstCC == 1
                  cepstrumPrBlock = cepstrum(2:veclen,counter-ModCepsLen+1:counter).*winC'; % 0th coeff. not considered (log of stand. dev. of exc. power)
              end
              modcepstPrBlck = abs(fft(cepstrumPrBlock'));
              modcepst_cont(:,modcounter,:) = modcepstPrBlck(1:ModCepsLen/2+1,1:veclen-1);
              modcounter = modcounter + 1;
          end
      end
      
      counter = counter +1;
          
end

powerspec = powerspec(:,1:counter-1);
if firstCC == 0
    cepstrum = cepstrum(1:end,1:counter-1);
elseif firstCC == 1
    cepstrum = cepstrum(2:end,1:counter-1); % 0th coeff. not considered (log of stand. dev. of exc. power)
end
    
% plot all of this
fvec =  0:(0.5.*fs./veclen):0.5.*fs.*(1-1./veclen); % frequency vector
                                                    
% modfvec_len = ModCepsLen/2+1;
% modfvec = 0:(0.5.*fs./fadvance./modfvec_len):0.5.*fs./fadvance.*(1-1./modfvec_len);
modfvec_len = ModCepsLen/2;
modfvec = 0:(0.5.*fs./fadvance./modfvec_len):0.5.*fs./fadvance;

clear cepstrumPrBlock;
clear phi_xx;
clear modcepstPrBlck;

time=num2str(etime(clock,starttime));

