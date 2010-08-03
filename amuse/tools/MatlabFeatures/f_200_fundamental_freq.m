function res = fundamental_frequ(x,fs,wlength,woffset)
%
% computes fundamental frequency values (in Hz) of time signal x for windows of 
% length wlength and offset woffset between adjacent windows
% The sampling frequency fs is provided as input.
%
% Reference: K. Jensen, "Timbre Models of Musical Sounds", Copenhagen
% University, http://www.aaue.dk/~krist/TMoMS.pdf
%
% contact: Wolfgang.Theimer@nokia.com
% last modification: 15.11.2007
%

% consider only first channel of signal
x = x(:,1);

% define Hamming window of wlength samples
w = hamming(wlength);

% determine number of windows
s = size(x);
nwin = floor(1 + (s(1)-wlength)/woffset);

% set constants
epsilon = 1e-6;      % minimum value for log computation
epsilon2 = 2e-4;     % minimum average amplitude required for frequency estimation
Nw = 5;              % window for thresholding has a length of Nw
Nl = 2;              % number of partials included in inharmonicity correction
Nw2 = floor(Nw/2)+1; 

% compute resulting fundamental frequency values
res = zeros(1,nwin);
for i = 1:nwin
   % perform Hamming windowing for window i
   y = x(1+(i-1)*woffset:wlength+(i-1)*woffset) .* w;

   % compute magnitude of FFT for window i
   A = abs(fft(y,wlength));
   A = A(1:wlength/2);

   % perform fundamental frequency computation only if signal energy is above threshold
   if (sum(A(1:wlength/2)) > wlength/2 * epsilon2)

      % select maxima of spectral amplitudes and set rest to zero
      p = peak_positions(A);
   
      % find indices (ind) of peak positions (where p is one) and number of
      % indices Ni
      ind = find(p);
      sind = size(ind);
      Ni = sind(1);

      % compute correction values to improve frequency / amplitude resolution
      A = log(A+epsilon);
      denominator = A(ind-1)-2*A(ind)+A(ind+1);
      if (abs(denominator) > epsilon)
         cor = 0.5*(A(ind-1)-A(ind+1))./denominator;
      else
         cor = 0;
      end

      % create subarray with corrected frequency values of peak positions
      f = fs * (ind-1) / wlength + fs*cor/wlength;

      % compute corrected amplitudes for subarray
      B = exp(A(ind)-0.25*cor.*(A(ind-1)-A(ind+1)));

      % perform dynamical thresholding by removing all amplitudes that
      % are less than 90% of the maximum value in a window of width Nw
      % m: maximum threshold for window Nw
      m = zeros(Ni,1);
      for j = 1:Ni
          m(j) = max(B((ind > ind(j)-Nw2) & (ind < ind(j)+Nw2)));
      end
      pw = (B > 0.9*m) & (B > 0.1*max(B));
      wind = find(pw);
      f = f(wind);
      B = B(wind);

      % compute first estimate of fundamental frequency as mean of all candidates
      % = frequency differences of subsequent partials
      f = [0 ; f];
      fin = size(f);
      Nf = fin(1);
      fd = f(2:Nf)-f(1:Nf-1);
      if (Nf > 1)
          res(i) = sum(fd)/(Nf-1);
      else
          res(i) = 0;
      end % of (Nf > 1)
   end % of sum(A...)
end

% perform median post-processing to eliminate outliers
% assumption: minimum duration of a tone is 50 ms 
% -> within a 50 ms window the values are median-filtered
min_dur = 0.05;                          % minimum duration of a tone
Nm = floor(0.5*min_dur*fs/woffset)*2+1;  % odd number of signal windows for median filtering
res = medfilt1(res,Nm);                  % perform median filtering
